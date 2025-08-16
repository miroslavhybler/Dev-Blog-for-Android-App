@file:OptIn(JetExperimental::class, ExperimentalComposeUiApi::class)

package com.jet.article.example.devblog.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jet.article.example.devblog.AndroidDevBlogApp
import com.jet.article.example.devblog.data.SettingsStorage
import com.jet.article.example.devblog.isAppDark
import com.jet.article.example.devblog.rememberSystemBarsStyle
import com.jet.article.example.devblog.ui.home.HomeScreen
import com.jet.article.example.devblog.ui.settings.AboutLibsScreen
import com.jet.article.example.devblog.ui.settings.AboutScreen
import com.jet.article.example.devblog.ui.settings.ChangelogScreen
import com.jet.article.example.devblog.ui.settings.SettingsScreen
import com.jet.tts.TtsClient
import com.jet.tts.rememberTtsClient
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mir.oslav.jet.annotations.JetExperimental
import java.util.Locale
import javax.inject.Inject

/**
 * @author Miroslav Hýbler <br>
 * created on 09.08.2024
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    companion object {

        /**
         * True when [MainActivity] was created and is running (both, on foreground or hanging in
         * Task Manager.
         */
        var isActive: Boolean = false


        /**
         * Holding current deeplink from intent. If null, there is no deeplink. Value is provided
         * by [LocalDeepLink] for composable screens. When link is opened and handled by the app,
         * don't forget to call [MainActivity.onDeeplinkOpened] to clear the value.
         */
        private var deeplink: String? by mutableStateOf(value = null)

        /**
         * True when splash screen should be visible.
         */
        var isSplashScreenVisible: Boolean = true
            private set

        fun onDeeplinkOpened() {
            deeplink = null
        }


        /**
         * Should be called when data for [com.jet.article.example.devblog.ui.home.list.HomeListPane]
         * are loaded. This is needed to hide splashscreen and handle the case of "Double SplashScreen"
         * issue.
         */
        fun onDataLoaded() {
            isSplashScreenVisible = false
        }
    }

    private val viewModel: MainViewModel by viewModels()


    /**
     * [Job] for updating network callback in [AndroidDevBlogApp] based on actual settings. Callback
     * has to be updated because of [SettingsStorage.Settings.isCellularDataUsageAllowed] flag may
     * change.
     */
    private var updateNetworkCallbackJob: Job? = null


    @Inject
    lateinit var settingsStorage: SettingsStorage


    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition(condition = { isSplashScreenVisible })
        super.onCreate(savedInstanceState)


        lifecycleScope.launch {
            delay(timeMillis = 1_000)
            //Just in case that onDataLoaded() will not be called after 1 sec (this can be due
            //to bad connection) so we will hide splashscreen even when data are not loaded yet
            if (!isSplashScreenVisible) {
                isSplashScreenVisible = true
            }
        }

        updateNetworkCallbackJob = lifecycleScope.launch {
            settingsStorage.settings.collect { newSettings ->
                //Updating network callback, moved to MainActivity from AndroidDevBlogApp for
                //collection of settings can use activity's lifecycleScope
                AndroidDevBlogApp.registerNetworkCallback(
                    context = application,
                    settings = newSettings,
                )
            }
        }

//        viewModel.load(
//            onLoaded = {
//                isSplashScreenVisible = false
//                if (viewModel.settingsStorage.isFirstTimeLoad) {
//                    viewModel.settingsStorage.isFirstTimeLoad = false
//                }
//            }
//        )

        isActive = true

        checkDeeplink(intent = intent)

        setContent {
            val ttsClient = rememberTtsClient(
                onInitialized = { client ->
                    client.setLanguage(language = Locale.getDefault())
                    client.highlightMode =
                        TtsClient.HighlightMode.SPOKEN_RANGE_FROM_BEGINNING_INCLUDING_PREVIOUS_UTTERANCES
                }
            )
            val settings by viewModel.settings.collectAsState(
                initial = SettingsStorage.Settings()
            )
            val systemBarsStyle = rememberSystemBarsStyle(settings = settings)
            val dimensions = rememberDimensions()

            DevBlogAppTheme(
                isUsingDynamicColors = settings.isUsingDynamicColors,
                darkTheme = isAppDark(settings = settings),
            ) {
                CompositionLocalProvider(
                    LocalDimensions provides dimensions,
                    LocalDeepLink provides deeplink,
                    LocalTtsClient provides ttsClient,
                ) {
                    LaunchedEffect(key1 = systemBarsStyle) {
                        enableEdgeToEdge(
                            statusBarStyle = systemBarsStyle,
                            navigationBarStyle = systemBarsStyle,
                        )
                    }
                    val navHostController = rememberNavController()
                    NavHost(
                        modifier = Modifier
                            .semantics { testTagsAsResourceId = true },
                        navController = navHostController,
                        startDestination = Routes.main,
                    ) {
                        composable(route = Routes.main) {
                            HomeScreen(
                                viewModel = hiltViewModel(),
                                navHostController = navHostController,
                            )
                        }

                        composable(
                            route = Routes.settings,
                            enterTransition = { fadeIn() },
                            exitTransition = { fadeOut() },
                        ) {
                            SettingsScreen(
                                navHostController = navHostController,
                                viewModel = hiltViewModel(),
                            )
                        }

                        composable(
                            route = Routes.aboutLibs,
                            enterTransition = { fadeIn() },
                            exitTransition = { fadeOut() },
                        ) {
                            AboutLibsScreen(
                                navHostController = navHostController,
                            )
                        }
                        composable(
                            route = Routes.changeLog,
                            enterTransition = { fadeIn() },
                            exitTransition = { fadeOut() },
                        ) {
                            ChangelogScreen(
                                navHostController = navHostController,
                            )
                        }
                        composable(
                            route = Routes.about,
                            enterTransition = { fadeIn() },
                            exitTransition = { fadeOut() },
                        ) {
                            AboutScreen(
                                navHostController = navHostController,
                            )
                        }
                    }
                }
            }
        }
    }


    /**
     * Activity is using `android:launchMode="singleTask"` in manifest, so onNewIntent is called
     * with intent holding a deeplink when activity is already running.
     */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        checkDeeplink(intent = intent)
    }


    override fun onDestroy() {
        isActive = false
        updateNetworkCallbackJob?.cancel()
        updateNetworkCallbackJob = null
        AndroidDevBlogApp.unregisterNetworkCallback(context = application)
        super.onDestroy()
    }


    /**
     * Checks if [intent] contains a deeplink which can be a link to concrete post or just to
     * developer blog index page.
     * @param intent Intent to extract deeplink from.
     */
    private fun checkDeeplink(intent: Intent) {
        deeplink = if (
            intent.action == Intent.ACTION_VIEW
            && intent.data != null
            && (intent.data?.path?.length ?: 0) > 1
        ) {
            //Path must be longer that one because path "/" is not url to post detail but for the index
            intent.data!!.toString()
        } else null
    }
}

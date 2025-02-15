@file:OptIn(JetExperimental::class, ExperimentalComposeUiApi::class)

package com.jet.article.example.devblog.ui

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
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import mir.oslav.jet.annotations.JetExperimental
import javax.inject.Inject

/**
 * @author Miroslav Hýbler <br>
 * created on 09.08.2024
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    companion object {
        var isActive: Boolean = false
    }

    private val viewModel: MainViewModel by viewModels()

    //TODO show when first items are loaded
    private var isSplashScreenVisible: Boolean = false

    private var updateNetworkCallbackJob: Job? = null

    @Inject
    lateinit var settingsStorage: SettingsStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition(condition = { isSplashScreenVisible })
        super.onCreate(savedInstanceState)

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
        setContent {
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
                    value = LocalDimensions provides dimensions,
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
                            route = Routes.channelLog,
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


    override fun onDestroy() {
        isActive = false
        updateNetworkCallbackJob?.cancel()
        updateNetworkCallbackJob = null
        AndroidDevBlogApp.unregisterNetworkCallback(context = application)
        super.onDestroy()
    }
}
package com.jet.article.example.devblog.ui.settings

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Paint.Align
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.jet.article.example.devblog.BuildConfig
import com.jet.article.example.devblog.R
import com.jet.article.example.devblog.composables.TitleTopBar
import com.jet.article.example.devblog.data.SettingsStorage
import com.jet.article.example.devblog.horizontalPadding
import com.jet.article.example.devblog.openNotificationSettings
import com.jet.article.example.devblog.openWeb
import com.jet.article.example.devblog.ui.DevBlogAppTheme
import com.jet.article.example.devblog.ui.LocalDimensions
import com.jet.article.example.devblog.ui.Routes
import com.jet.utils.plus
import kotlinx.coroutines.launch
import androidx.compose.runtime.setValue
import com.jet.article.example.devblog.OnLifecycleEvent


/**
 * @author Miroslav Hýbler <br>
 * created on 19.08.2024
 */
val darkModeOptions: List<Int> = listOf(
    AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM,
    AppCompatDelegate.MODE_NIGHT_YES,
    AppCompatDelegate.MODE_NIGHT_NO,
)


@Composable
fun SettingsScreen(
    navHostController: NavHostController,
    viewModel: SettingsViewModel,
) {


    val settings by viewModel.settings.collectAsState(initial = SettingsStorage.Settings())

    SettingsScreenContent(
        navHostController = navHostController,
        settings = settings,
        onNewSettings = {
            viewModel.viewModelScope.launch {
                viewModel.settingsStorage.saveSettings(settings = it)
            }
        },
    )
}


@Composable
private fun SettingsScreenContent(
    navHostController: NavHostController,
    settings: SettingsStorage.Settings,
    onNewSettings: (SettingsStorage.Settings) -> Unit,
) {
    val dimensions = LocalDimensions.current
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TitleTopBar(
                text = stringResource(R.string.settings_title),
                onNavigationIcon = navHostController::navigateUp,
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(state = rememberScrollState())
                    .padding(
                        paddingValues = PaddingValues(
                            top = dimensions.topLinePadding,
                            bottom = dimensions.bottomLinePadding
                        ) + paddingValues
                    )
            ) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    SettingsSwitch(
                        modifier = Modifier,
                        title = stringResource(R.string.settings_dynamic_colors_label),
                        isChecked = settings.isUsingDynamicColors,
                        onCheckedChange = {
                            onNewSettings(settings.copy(isUsingDynamicColors = it))
                        }
                    )
                }

                SettingsDropdown(
                    modifier = Modifier,
                    title = stringResource(R.string.settings_dark_mode_label),
                    items = darkModeOptions,
                    transform = {
                        SettingsStorage.Settings.nightModeString(
                            context = context,
                            flags = it,
                        )
                    },
                    subtitle = settings.nightModeString(context = context),
                    onSelected = {
                        onNewSettings(settings.copy(nightModeFlags = it))
                    }
                )


                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    var areNotificationsEnabled by remember {
                        mutableStateOf(
                            value = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) == PackageManager.PERMISSION_GRANTED
                        )
                    }


                    OnLifecycleEvent { event ->
                        if (event == Lifecycle.Event.ON_RESUME) {
                            areNotificationsEnabled = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) == PackageManager.PERMISSION_GRANTED
                        }
                    }

                    SettingsSwitch(
                        modifier = Modifier,
                        title = stringResource(R.string.setting_notifications_label),
                        isChecked = areNotificationsEnabled,
                        onCheckedChange = {
                            context.openNotificationSettings()
                        }
                    )
                }

                Spacer(modifier = Modifier.weight(weight = 1f))

                SettingsRow(
                    title = stringResource(R.string.settings_changelog_title),
                    onClick = { navHostController.navigate(route = Routes.channelLog) },
                    icon = null,
                )
                SettingsRow(
                    title = stringResource(R.string.settings_about_title),
                    onClick = { navHostController.navigate(route = Routes.about) },
                    icon = null,
                )
                SettingsRow(
                    title = stringResource(R.string.settings_about_libs_title),
                    onClick = { navHostController.navigate(route = Routes.aboutLibs) },
                    icon = null,
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalPadding(),
                    verticalAlignment = Alignment.Bottom,
                ) {
                    Text(
                        text = stringResource(R.string.settings_version),
                        style = MaterialTheme.typography.titleSmall,
                    )

                    Spacer(modifier = Modifier.width(width = 24.dp))

                    Text(
                        modifier = Modifier,
                        text = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
        },
        bottomBar = {
            GithubBottomBar()
        }
    )
}



/**
 * Bottom bar with link to github repository. Handling insets itself as it's supposed to be used
 * as [Scaffold]'s botomBar.
 */
@Composable
fun GithubBottomBar() {
    val dimensions = LocalDimensions.current
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
    ) {
        Row(
            modifier = Modifier
                .align(alignment = Alignment.Center)
                .horizontalPadding()
                .padding(bottom = dimensions.bottomLinePadding)
                .wrapContentSize()
                .background(
                    color = MaterialTheme.colorScheme.background,
                    shape = CircleShape,
                )
                .clip(shape = CircleShape)
                .clickable(
                    onClick = {
                        context.openWeb(
                            url = "https://github.com/miroslavhybler/Dev-Blog-for-Android-App",
                        )
                    }
                )
                .padding(horizontal = 24.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(space = 12.dp)
        ) {

            Icon(
                modifier = Modifier.size(size = 16.dp),
                painter = painterResource(id = R.drawable.ic_logo_github),
                contentDescription = null,
            )

            Text(
                modifier = Modifier,
                text = stringResource(R.string.settings_github),
            )
        }
    }
}


@Composable
@PreviewLightDark
private fun SettingsScreenPreview() {
    DevBlogAppTheme {
        SettingsScreenContent(
            navHostController = rememberNavController(),
            settings = SettingsStorage.Settings(),
            onNewSettings = { _ -> }
        )
    }
}
package com.jet.article.example.devblog.ui.settings

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.jet.article.example.devblog.composables.TitleTopBar
import com.mikepenz.aboutlibraries.ui.compose.LibrariesContainer
import com.mikepenz.aboutlibraries.ui.compose.LibraryDefaults
import com.jet.article.example.devblog.R

/**
 * @author Miroslav Hýbler <br>
 * created on 17.09.2024
 */
@Composable
fun AboutLibsScreen(
    navHostController: NavHostController,
) {
    Scaffold(
        topBar = {
            TitleTopBar(
                text = stringResource(id = R.string.settings_about_libs_title),
                onNavigationIcon = navHostController::navigateUp,
            )
        },
        content = { paddingValues ->
            LibrariesContainer(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues = paddingValues),
                colors = LibraryDefaults.libraryColors(
                    backgroundColor = MaterialTheme.colorScheme.background,
                    contentColor = MaterialTheme.colorScheme.onBackground,
                    badgeBackgroundColor = MaterialTheme.colorScheme.primary,
                    badgeContentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            )
        }
    )
}



@Composable
@PreviewLightDark
private fun AboutLibsScreenPreview() {
    AboutLibsScreen(navHostController = rememberNavController())
}
package com.jet.article.example.devblog.ui.settings

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.jet.article.example.devblog.R
import com.jet.article.example.devblog.composables.TitleTopBar
import com.jet.article.example.devblog.ui.DevBlogAppTheme
import com.mikepenz.aboutlibraries.ui.compose.LibraryDefaults
import com.mikepenz.aboutlibraries.ui.compose.android.produceLibraries
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer
import com.mikepenz.aboutlibraries.ui.compose.m3.chipColors
import com.mikepenz.aboutlibraries.ui.compose.m3.libraryColors

/**
 * Using [about-libraries library](https://github.com/mikepenz/AboutLibraries) to show list of libraries
 * used in the app.
 * @author Miroslav Hýbler <br>
 * created on 17.09.2024
 */
@Composable
fun AboutLibsScreen(
    onBack: () -> Unit,
) {
    val libraries by produceLibraries()

    Scaffold(
        topBar = {
            TitleTopBar(
                text = stringResource(id = R.string.settings_about_libs_title),
                onNavigationIcon = onBack,
            )
        },
        content = { paddingValues ->
            LibrariesContainer(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues = paddingValues),
                libraries = libraries,
                colors = LibraryDefaults.libraryColors(
                    libraryBackgroundColor = MaterialTheme.colorScheme.background,
                    libraryContentColor = MaterialTheme.colorScheme.onBackground,
                    versionChipColors = LibraryDefaults.chipColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                ),
            )
        }
    )
}


@Composable
@PreviewLightDark
private fun AboutLibsScreenPreview() {
    DevBlogAppTheme {
        AboutLibsScreen(
            onBack = {},
        )
    }
}
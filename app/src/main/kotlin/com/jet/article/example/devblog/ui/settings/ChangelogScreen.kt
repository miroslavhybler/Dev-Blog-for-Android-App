package com.jet.article.example.devblog.ui.settings

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.jet.article.example.devblog.composables.TitleTopBar
import com.jet.article.example.devblog.R
import com.jet.article.example.devblog.horizontalPadding
import com.jet.article.example.devblog.ui.DevBlogAppTheme
import com.jet.article.example.devblog.ui.LocalDimensions


/**
 * List holding simple changelog of the app.
 */
private  val changelog: List<Changelog> = listOf(
    Changelog(
        version = "1.0.3",
        dateFormatted = "18.10.2024",
        titleRes = R.string.settings_changelog_title_1_0_3,
        changes = listOf(
            R.string.settings_changelog_1_0_3__0,
            R.string.settings_changelog_1_0_3__1,
            R.string.settings_changelog_1_0_3__2,
        )
    ),
    Changelog(
        version = "1.0.2",
        dateFormatted = "01.10.2024",
        titleRes = R.string.settings_changelog_title_1_0_2,
        changes = listOf(
            R.string.settings_changelog_1_0_2__0,
            R.string.settings_changelog_1_0_2__1,
        )
    ),
    Changelog(
        version = "1.0.1",
        dateFormatted = "29.09.2024",
        titleRes = R.string.settings_changelog_title_1_0_1,
        changes = listOf(
            R.string.settings_changelog_1_0_1__0,
            R.string.settings_changelog_1_0_1__1,
            R.string.settings_changelog_1_0_1__2,
            R.string.settings_changelog_1_0_1__3,
        )
    ),
    Changelog(
        version = "1.0.0",
        dateFormatted = "23.09.2024",
        titleRes = R.string.settings_changelog_title_1_0_0,
        changes = listOf(
            R.string.settings_changelog_1_0_0__0,
            R.string.settings_changelog_1_0_0__1,
            R.string.settings_changelog_1_0_0__2,
        )
    ),
)


/**
 * Showing changelog of the app. Some really minor updates are not shown here as the changelog is
 * rather for better orientation and as reminder that app is being actively developed.
 * More details can be found on github repository.
 * @author Miroslav Hýbler <br>
 * created on 17.09.2024
 */
@Composable
fun ChangelogScreen(
    navHostController: NavHostController,
) {
    val dimensions = LocalDimensions.current
    Scaffold(
        topBar = {
            TitleTopBar(
                text = stringResource(id = R.string.settings_changelog_title),
                onNavigationIcon = navHostController::navigateUp,
            )
        },
        content =  { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues = paddingValues),
                contentPadding = PaddingValues(
                    top = dimensions.topLinePadding,
                    bottom = dimensions.bottomLinePadding,
                ),
                verticalArrangement = Arrangement.spacedBy(space = 16.dp),
            ) {
                items(items = changelog) {
                    ChangelogItem(item = it)
                }
            }
        },
        bottomBar = {
            GithubBottomBar()
        }
    )
}


/**
 * Showing single changelog item
 */
@Composable
private fun ChangelogItem(
    modifier: Modifier = Modifier,
    item: Changelog,
    versionStyle: TextStyle = MaterialTheme.typography.headlineMedium,
    dateStyle: TextStyle = MaterialTheme.typography.labelSmall,
) {
    val density = LocalDensity.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .horizontalPadding()
    ) {
        Row(
            modifier = Modifier
                .wrapContentWidth(),
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                modifier = Modifier
                    .alignByBaseline(),
                text = item.version,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
            )

            Spacer(
                modifier = Modifier.width(width = 4.dp)
            )

            Text(
                modifier = Modifier
                    .alignByBaseline(),
                text = "(${item.dateFormatted})",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
        Text(
            text = stringResource(id = item.titleRes),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )

        item.changes.forEach { changeStringRes ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
            ) {
                Icon(
                    modifier = Modifier
                        .padding(
                            top = with(density) {
                                MaterialTheme.typography.bodyMedium.lineHeight.toDp() / 2f - 4.dp
                            }
                        )
                        .size(width = 8.dp, height = 8.dp),
                    painter = painterResource(id = R.drawable.ic_list_item),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onBackground,
                )

                Spacer(modifier = Modifier.width(width = 4.dp))

                Text(
                    modifier = Modifier
                        .weight(weight = 1f),
                    text = stringResource(id = changeStringRes),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }
        }
    }
}


/**
 * @param version Version of the app
 * @param dateFormatted Formatted date string of [version] release
 * @param titleRes Title of the change summarizing changes
 * @param changes List of changes for new [version], must be localised string resources
 */
data class Changelog constructor(
    val version: String,
    val dateFormatted: String,
    @StringRes val titleRes: Int,
    @StringRes val changes: List<Int>
)


@Composable
@PreviewLightDark
private fun ChangelogScreenPreview() {
    DevBlogAppTheme {
        ChangelogScreen(
            navHostController = rememberNavController(),
        )
    }
}
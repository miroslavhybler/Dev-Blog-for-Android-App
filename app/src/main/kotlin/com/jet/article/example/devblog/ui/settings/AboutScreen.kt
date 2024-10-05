package com.jet.article.example.devblog.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.jet.article.example.devblog.composables.TitleTopBar
import com.jet.article.example.devblog.R
import com.jet.article.example.devblog.composables.Android
import com.jet.article.example.devblog.composables.EmptyAnimation
import com.jet.article.example.devblog.horizontalPadding
import com.jet.article.example.devblog.ui.DevBlogAppTheme
import com.jet.article.example.devblog.ui.LocalDimensions


/**
 * Simple screen showing brief information about the app.
 * @author Miroslav Hýbler <br>
 * created on 17.09.2024
 */
@Composable
fun AboutScreen(
    navHostController: NavHostController,
) {
    val dimensions = LocalDimensions.current
    Scaffold(
        topBar = {
            TitleTopBar(
                text = stringResource(id = R.string.settings_about_title),
                onNavigationIcon = navHostController::navigateUp,
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(state = rememberScrollState())
                    .padding(paddingValues = paddingValues)
                    .padding(
                        top = dimensions.topLinePadding,
                        bottom = dimensions.bottomLinePadding,
                    )
            ) {

                EmptyAnimation()

                Spacer(modifier = Modifier.height(height = 8.dp))

                Android(
                    modifier = Modifier
                        .align(alignment = Alignment.CenterHorizontally),
                )

                Spacer(
                    modifier = Modifier
                        .height(height = 32.dp),
                )

                Text(
                    modifier = Modifier.horizontalPadding(),
                    text = buildAnnotatedString {
                        append(text = stringResource(id = R.string.settings_about_0_0))
                        append(char = ' ')
                        withStyle(
                            style = SpanStyle(
                                textDecoration = TextDecoration.Underline,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        ) {
                            pushLink(
                                link = LinkAnnotation.Url(url = "https://github.com/miroslavhybler/jet-article")
                            )
                            append(
                                text = stringResource(id = R.string.settings_about_0_1)
                            )
                            pop()
                        }
                        append(char = ' ')
                        append(text = stringResource(id = R.string.settings_about_0_2))
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Spacer(
                    modifier = Modifier
                        .height(height = 8.dp),
                )

                Text(
                    modifier = Modifier.horizontalPadding(),
                    text = stringResource(id = R.string.settings_about_1),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                )

                Spacer(
                    modifier = Modifier
                        .weight(weight = 1f)
                        .defaultMinSize(minHeight = 32.dp)
                )

                Text(
                    modifier = Modifier.horizontalPadding(),
                    text = stringResource(id = R.string.settings_about_2),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                )

            }
        },
        bottomBar = {
            GithubBottomBar()
        }
    )
}


@Composable
@PreviewLightDark
private fun AboutScreenPreview() {
    DevBlogAppTheme {
        AboutScreen(navHostController = rememberNavController())
    }
}
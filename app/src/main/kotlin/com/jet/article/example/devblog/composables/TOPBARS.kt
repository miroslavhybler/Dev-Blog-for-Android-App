@file:OptIn(ExperimentalMaterial3Api::class)

package com.jet.article.example.devblog.composables

import androidx.annotation.FloatRange
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavHostController
import com.jet.article.example.devblog.R
import com.jet.article.example.devblog.horizontalPadding
import com.jet.article.example.devblog.ui.Routes
import com.jet.article.ui.elements.HtmlTextBlock


/**
 * @author Miroslav Hýbler <br>
 * created on 13.08.2024
 */
@Composable
fun TitleTopBar(
    modifier: Modifier = Modifier,
    text: String,
    onNavigationIcon: (() -> Unit)? = null,
    contentDescription: String? = null,
) {
    TopAppBar(
        modifier = Modifier
            .background(color = MaterialTheme.colorScheme.background)
            .then(other = modifier)
            .horizontalPadding()
            .statusBarsPadding(),
        title = { Text(text = text) },
        navigationIcon = {
            if (onNavigationIcon != null) {
                IconButton(
                    onClick = onNavigationIcon,
                    content = {
                        Icon(
                            painter = painterResource(R.drawable.ic_back),
                            contentDescription = contentDescription,
                            tint = MaterialTheme.colorScheme.onBackground,
                        )

                    }
                )
            }

        }
    )

}


@Composable
fun MainTopBar(
    modifier: Modifier = Modifier,
    text: String,
    navHostController: NavHostController,
) {
    TopAppBar(
        modifier = Modifier
            .background(color = MaterialTheme.colorScheme.background)
            .horizontalPadding()
            .statusBarsPadding()
            .then(other = modifier),
        title = {
            Text(
                text = text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        navigationIcon = {
            Icon(
                painter = painterResource(R.drawable.ic_android),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
        },
        actions = {
            IconButton(
                onClick = {
                    navHostController.navigate(route = Routes.settings)
                },
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = MaterialTheme.colorScheme.onBackground
                )
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_settings),
                    contentDescription = stringResource(R.string.content_desc_open_settings),
                )
            }
        }
    )

}


@Composable
fun PostTopBar(
    modifier: Modifier = Modifier,
    title: String,
    @FloatRange(from = 0.0, to = 1.0)
    backgroundAlpha: Float,
    actions: @Composable RowScope.() -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior,
    onNavigationIcon: (() -> Unit)? = null,
    navigationActionContentDescription: String? = null,
    titleColor: Color = MaterialTheme.colorScheme.onBackground,
) {
    LargeTopAppBar(
        modifier = modifier,
        title = {
            HtmlTextBlock(
                modifier = Modifier.horizontalPadding(),
                text = title,
                key = Int.MIN_VALUE,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleLarge,
            )
        },
        colors = TopAppBarDefaults.largeTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.background.copy(alpha = backgroundAlpha),
            titleContentColor = titleColor,
            scrolledContainerColor = MaterialTheme.colorScheme.background.copy(alpha = backgroundAlpha),
            actionIconContentColor = MaterialTheme.colorScheme.onBackground,
            navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
        ),
        actions = actions,
        navigationIcon = {
            if (onNavigationIcon != null) {
                IconButton(
                    onClick = onNavigationIcon,
                    content = {
                        Icon(
                            painter = painterResource(R.drawable.ic_back),
                            contentDescription = navigationActionContentDescription,
                            tint = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                )
            }
        },
        scrollBehavior = scrollBehavior,
    )
}
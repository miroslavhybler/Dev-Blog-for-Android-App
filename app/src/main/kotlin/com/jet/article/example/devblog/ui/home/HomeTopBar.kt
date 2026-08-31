package com.jet.article.example.devblog.ui.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.jet.article.example.devblog.R

@Composable
fun HomeTopBar(
    isSearchOpen: Boolean,
    query: String,
    onQueryChange: (String) -> Unit,
    onOpenSearch: () -> Unit,
    onCloseSearch: () -> Unit,
    onSettings: () -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(key1 = isSearchOpen) {
        if (isSearchOpen) focusRequester.requestFocus()
    }

    TopAppBar(
        title = {
            AnimatedContent(
                targetState = isSearchOpen,
                transitionSpec = {
                    (fadeIn() + slideInHorizontally { width -> width / 4 }) togetherWith
                            (fadeOut() + slideOutHorizontally { width -> -width / 4 })
                },
                label = "home_search_title",
            ) { searching ->
                if (searching) {
                    TextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester = focusRequester),
                        value = query,
                        onValueChange = onQueryChange,
                        placeholder = { Text(stringResource(R.string.search_placeholder)) },
                        singleLine = true,
                    )
                } else {
                    Text(
                        text = stringResource(R.string.app_name),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        },
        navigationIcon = {
            Icon(
                painter = painterResource(R.drawable.ic_android),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
        },
        actions = {
            AnimatedContent(
                targetState = isSearchOpen,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "home_search_actions",
            ) { searching ->
                if (searching) {
                    IconButton(onClick = {
                        focusManager.clearFocus()
                        onCloseSearch()
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_close),
                            contentDescription = stringResource(id = R.string.content_desc_close_search),
                        )
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(space = 10.dp),
                    ) {
                        IconButton(onClick = onOpenSearch) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_search),
                                contentDescription = stringResource(id = R.string.content_desc_open_search),
                            )
                        }
                        IconButton(onClick = onSettings) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_settings),
                                contentDescription = stringResource(id = R.string.content_desc_open_settings),
                            )
                        }
                    }
                }
            }
        },
    )
}
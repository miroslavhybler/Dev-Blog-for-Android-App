@file:OptIn(ExperimentalMaterial3Api::class)

package com.jet.article.example.devblog.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.trace
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.jet.article.example.devblog.AndroidDevBlogApp
import com.jet.article.example.devblog.R
import com.jet.article.example.devblog.composables.ErrorLayout
import com.jet.article.example.devblog.composables.MainTopBar
import com.jet.article.example.devblog.composables.SmallNoConnectionLayout
import com.jet.article.example.devblog.data.database.PostItem
import com.jet.article.example.devblog.isExpanded
import com.jet.article.example.devblog.isMedium
import com.jet.article.example.devblog.shared.Tracing
import com.jet.article.example.devblog.ui.LocalBackstack
import com.jet.article.example.devblog.ui.LocalDimensions
import com.jet.article.example.devblog.ui.Route
import com.jet.article.example.devblog.ui.containsEntry


/**
 * Shows list of articles from [Android Dev Blog](https://android-developers.googleblog.com/) index site.
 * Now this is not the way JetHtmlArticle library is ment to be used, but it's possible.
 * @author Miroslav Hýbler <br>
 * created on 13.08.2024
 */
@Composable
fun HomeListPane(
    onNavigate: (route: Route) -> Unit,
    viewModel: HomeListPaneViewModel = hiltViewModel(),
) = trace(sectionName = Tracing.Section.homeListPane) {

    val posts = viewModel.posts.collectAsLazyPagingItems()

    HomeListPaneContent(
        data = posts,
        onRefresh = viewModel::refresh,
        onToggleFavorite = viewModel::toggleFavoriteItem,
        onNavigate = onNavigate,
    )
}

private const val errorLazyKey: Int = Int.MAX_VALUE
private const val loadingLazyKey: Int = Int.MIN_VALUE - 1


@Composable
private fun HomeListPaneContent(
    onNavigate: (route: Route) -> Unit,
    data: LazyPagingItems<PostItem>,
    onRefresh: () -> Unit,
    onToggleFavorite: (postItem: PostItem) -> Unit,
) {
    val backstack = LocalBackstack.current
    val dimensions = LocalDimensions.current
    val windowInfo = currentWindowAdaptiveInfo()
    val windowWidth = windowInfo.windowSizeClass.windowWidthSizeClass

    val isExpanded by rememberUpdatedState(
        newValue = backstack.containsEntry(clazz = Route.Post::class)
    )

    val isLargeWidth = windowWidth.isExpanded || windowWidth.isMedium

    val isConnectedToInternet = AndroidDevBlogApp.isConnectedToInternet


    val isRefreshing by remember(key1 = data.loadState.refresh) {
        derivedStateOf { data.loadState.refresh is LoadState.Loading }
    }


    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag(tag = Tracing.Tag.homeListPane),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            MainTopBar(
                text = stringResource(id = R.string.app_name),
                onSettings = {
                    onNavigate(Route.Settings)
                },
            )

        },
        content = { paddingValues ->
            PullToRefreshBox(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues = paddingValues),
                isRefreshing = isRefreshing,
                onRefresh = onRefresh,
            ) {

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag(tag = Tracing.Tag.posts),
                    contentPadding = PaddingValues(
                        start = dimensions.sidePadding,
                        end = dimensions.sidePadding,
                        top = dimensions.topLinePadding,
                        bottom = dimensions.bottomLinePadding,
                    ),
                    verticalArrangement = Arrangement.spacedBy(
                        space = when {
                            isExpanded && isLargeWidth -> 12.dp
                            else -> 24.dp
                        }
                    ),
                ) {

                    if (data.loadState.append is LoadState.Error) {
                        item(key = errorLazyKey) {
                            ErrorLayout(
                                modifier = Modifier.animateItem(),
                                title = stringResource(R.string.error_unable_to_load_posts),
                                cause = (data.loadState.append as LoadState.Error).error,
                                onRefresh = onRefresh
                            )
                        }

                    }

                    items(
                        count = data.itemCount,
                    ) { index ->
                        data[index]?.let {
                            HomeListItem(
                                modifier = if (index == 0) {
                                    Modifier
                                        .testTag(tag = Tracing.Tag.firstPostItem)
                                        .animateItem()
                                } else Modifier
                                    .animateItem(),
                                onOpenPost = { index, item ->
                                    val route = Route.Post(item = item)
                                    onNavigate(route)
                                },
                                item = it,
                                index = index,
                                onToggleFavorite = onToggleFavorite,
                                isSelected = false,
                            )
                        }
                    }

                    if (data.loadState.append is LoadState.Loading) {
                        item(key = loadingLazyKey) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .animateItem(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier
                                        .size(size = 24.dp)
                                )
                            }
                        }
                    }

                }

                AnimatedVisibility(
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .align(alignment = Alignment.TopCenter),
                    visible = !isConnectedToInternet,
                    enter = fadeIn(),
                    exit = fadeOut(),
                ) {
                    SmallNoConnectionLayout()
                }
            }
        },
    )
}


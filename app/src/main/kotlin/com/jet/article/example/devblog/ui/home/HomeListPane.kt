package com.jet.article.example.devblog.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.trace
import androidx.navigation.NavHostController
import com.jet.article.example.devblog.AndroidDevBlogApp
import com.jet.article.example.devblog.R
import com.jet.article.example.devblog.composables.ErrorLayout
import com.jet.article.example.devblog.composables.MainTopBar
import com.jet.article.example.devblog.composables.NoConnectionLayout
import com.jet.article.example.devblog.data.database.PostItem
import com.jet.article.example.devblog.isExpanded
import com.jet.article.example.devblog.isMedium
import com.jet.article.example.devblog.shared.Tracing
import com.jet.article.example.devblog.ui.LocalDimensions
import com.jet.utils.plus


/**
 * Shows list of articles from [Android Dev Blog](https://android-developers.googleblog.com/) index site.
 * Now this is not the way JetHtmlArticle library is ment to be used, but it's possible.
 * @author Miroslav Hýbler <br>
 * created on 13.08.2024
 */
@Composable
fun HomeListPane(
    onOpenPost: (index: Int, item: PostItem) -> Unit,
    viewModel: HomeListPaneViewModel,
    navHostController: NavHostController,
) = trace(sectionName = Tracing.Section.homeListPane) {

    val posts by viewModel.posts.collectAsState()

    HomeListPaneContent(
        onOpenPost = onOpenPost,
        data = posts,
        lazyListState = viewModel.lazyListState,
        navHostController = navHostController,
    )
}


@Composable
private fun HomeListPaneContent(
    onOpenPost: (index: Int, item: PostItem) -> Unit,
    data: Result<List<PostItem>>?,
    lazyListState: LazyListState,
    navHostController: NavHostController,
) = trace(sectionName = Tracing.Section.homeListPaneContent) {
    val mainState = LocalHomeScreenState.current
    val dimensions = LocalDimensions.current
    val windowInfo = currentWindowAdaptiveInfo()
    val windowWidth = windowInfo.windowSizeClass.windowWidthSizeClass

    val isExpanded = mainState.role == ListDetailPaneScaffoldRole.Detail
            || mainState.role == ListDetailPaneScaffoldRole.Extra
    val isLargeWidth = windowWidth.isExpanded || windowWidth.isMedium

    val isConnectedToInternet = AndroidDevBlogApp.isConnectedToInternet

    val postList = remember(key1 = data) {
        data?.getOrNull()
    }

    Scaffold(
        modifier = Modifier
            .testTag(tag = Tracing.Tag.homeListPane),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column() {
                MainTopBar(
                    text = stringResource(id = R.string.app_name),
                    navHostController = navHostController,
                )

                AnimatedVisibility(
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .align(alignment = Alignment.CenterHorizontally),
                    visible = !isConnectedToInternet,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    NoConnectionLayout()
                }
            }
        },
        content = { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .testTag(tag = Tracing.Tag.posts)
                    .animateContentSize(),
                state = lazyListState,
                contentPadding = paddingValues + PaddingValues(
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
                if (data?.isFailure == true) {
                    item {
                        ErrorLayout(
                            title = stringResource(R.string.error_unable_to_load_posts),
                            cause = data.exceptionOrNull(),
                        )
                    }

                }

                if (data == null) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .size(size = 24.dp)
                            )
                        }
                    }
                }


                if (data?.isSuccess == true && !postList.isNullOrEmpty()) {
                    itemsIndexed(
                        items = postList,
                        key = { _, item -> item.id },
                    ) { index, item ->
                        HomeListItem(
                            modifier = if (index == 0) {
                                Modifier.testTag( tag = Tracing.Tag.firstPostItem)
                            } else Modifier,
                            onOpenPost = onOpenPost,
                            item = item,
                            index = index,
                        )
                    }
                }
            }
        }
    )
}


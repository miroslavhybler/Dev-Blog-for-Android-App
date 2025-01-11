@file:OptIn(ExperimentalMaterial3AdaptiveApi::class)

package com.jet.article.example.devblog.ui.home

import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.VerticalDragHandle
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.PaneScaffoldDirective
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldRole
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.navigation.ThreePaneScaffoldNavigator
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.jet.article.example.devblog.data.AdjustedPostData
import com.jet.article.example.devblog.data.database.PostItem
import com.jet.utils.dpToPx
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


/**
 * Main screen showing lists of posts and post detail using [HomeListPane] and [PostPane].
 * @author Miroslav Hýbler <br>
 * created on 14.08.2024
 */
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    navHostController: NavHostController,
) {
    val state = rememberHomeScreenState()
    val postData by viewModel.postData.collectAsState()
    val selectedPost by viewModel.selectedPost.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val directive = calculatePaneScaffoldDirective(currentWindowAdaptiveInfo())
    val navigator = rememberListDetailPaneScaffoldNavigator<Nothing>(
        scaffoldDirective = PaneScaffoldDirective(
            maxHorizontalPartitions = directive.maxHorizontalPartitions,
            maxVerticalPartitions = 2,
            defaultPanePreferredWidth = directive.defaultPanePreferredWidth,
            verticalPartitionSpacerSize = directive.verticalPartitionSpacerSize,
            horizontalPartitionSpacerSize = directive.horizontalPartitionSpacerSize,
            excludedBounds = directive.excludedBounds,
        ),

        )

    fun onBack() {
        coroutineScope.launch {
            state.onNavigateBack()
            navigator.navigateBack()

            if (state.role == ListDetailPaneScaffoldRole.List) {
                viewModel.onBack()
                coroutineScope.launch {
                    delay(timeMillis = 200)
                    state.isEmptyPaneVisible = true
                }
            }

            if (state.role == ListDetailPaneScaffoldRole.Detail && postData == null) {
                navigator.navigateTo(pane = ListDetailPaneScaffoldRole.List)
            }
        }
    }
    BackHandler(
        enabled = state.role != ListDetailPaneScaffoldRole.List
    ) {
        onBack()
    }

    HomeScreenContent(
        state = state,
        postData = postData,
        onLoad = viewModel::loadPost,
        navigator = navigator,
        navHostController = navHostController,
        onCloseExtra = ::onBack,
        selectedPostItem = selectedPost,
        onRefreshDetail = { item ->
            viewModel.loadPost(item = item, isRefresh = true)
        }
    )
}


@Composable
fun HomeScreenContent(
    state: HomeScreenState,
    postData: Result<AdjustedPostData>?,
    selectedPostItem: PostItem?,
    onLoad: (item: PostItem) -> Unit,
    navigator: ThreePaneScaffoldNavigator<Nothing>,
    onCloseExtra: () -> Unit,
    navHostController: NavHostController,
    onRefreshDetail: (PostItem) -> Unit,
) {
    val density = LocalDensity.current

    //LazyList state for single post, don't misunderstood it for post list
    val postLazyListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val selectedPost = remember(key1 = postData) {
        postData?.getOrNull()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface,
    ) {
        CompositionLocalProvider(
            LocalMainScreenNavigator provides navigator,
            LocalHomeScreenState provides state,
        ) {

            ListDetailPaneScaffold(
                modifier = Modifier.fillMaxSize(),
                directive = navigator.scaffoldDirective,
                value = navigator.scaffoldValue,
                listPane = {
                    AnimatedPane {
                        HomeListPane(
                            onOpenPost = { index, item ->
                                coroutineScope.launch() {
                                    state.openPost(url = item.url, index = index)
                                    onLoad(item)
                                    navigator.navigateTo(pane = ListDetailPaneScaffoldRole.Detail)
                                }

                            },
                            viewModel = hiltViewModel(),
                            navHostController = navHostController,
                        )
                    }
                },
                paneExpansionDragHandle = { state ->
                    val interactionSource = remember { MutableInteractionSource() }

                    LaunchedEffect(key1 = selectedPost) {
                        if (selectedPost == null) {
                            state.clear()
                        }
                    }

                    if (selectedPost != null) {
                        VerticalDragHandle(
                            modifier = Modifier
                                .paneExpansionDraggable(
                                    state = state,
                                    minTouchTargetSize = LocalMinimumInteractiveComponentSize.current,
                                    interactionSource = interactionSource,
                                ),
                            interactionSource = interactionSource,
                        )
                    }
                },
                detailPane = {
                    AnimatedPane {
                        when {
                            postData != null -> {
                                PostPane(
                                    onOpenContests = {
                                        coroutineScope.launch() {
                                            if (state.role == ListDetailPaneScaffoldRole.Extra) {
                                                onCloseExtra()
                                            } else {
                                                state.openContest()
                                                navigator.navigateTo(pane = ListDetailPaneScaffoldRole.Extra)
                                            }
                                        }
                                    },
                                    data = postData,
                                    listState = postLazyListState,
                                    selectedPost = selectedPostItem,
                                    onRefresh = onRefreshDetail,
                                )
                            }

                            state.isEmptyPaneVisible -> {
                                PostEmptyPane()
                            }
                        }
                    }
                },
                extraPane = {
                    AnimatedPane {
                        ContentsPane(
                            data = selectedPost,
                            onSelected = { index, title ->
                                coroutineScope.launch {
                                    state.onNavigateBack()
                                    navigator.navigateBack()

                                    delay(timeMillis = 400)
                                    postLazyListState.animateScrollToItem(
                                        index = index,
                                        scrollOffset = density.dpToPx(dp = 24.dp).toInt(),
                                    )
                                }
                            },
                        )
                    }
                }
            )
        }
    }
}


/**
 * @param initialRole
 * @param initialIsEmptyPaneVisible
 * @param initialIndex
 * @param initialUrl
 */
class HomeScreenState constructor(
    initialRole: ThreePaneScaffoldRole,
    initialIsEmptyPaneVisible: Boolean,
    initialIndex: Int,
    initialUrl: String,
) {

    var role: ThreePaneScaffoldRole by mutableStateOf(value = initialRole)
        private set
    var selectedIndex: Int by mutableIntStateOf(value = initialIndex)
        private set
    var actualUrl: String by mutableStateOf(value = initialUrl)
        private set
    var isEmptyPaneVisible: Boolean by mutableStateOf(value = initialIsEmptyPaneVisible)

    fun openPost(url: String, index: Int) {
        this.isEmptyPaneVisible = false
        this.actualUrl = url
        this.selectedIndex = index
        this.role = ListDetailPaneScaffoldRole.Detail
    }


    fun openContest() {
        this.role = ListDetailPaneScaffoldRole.Extra
    }

    fun onNavigateBack() {
        role = when (role) {
            ListDetailPaneScaffoldRole.Extra -> ListDetailPaneScaffoldRole.Detail
            ListDetailPaneScaffoldRole.Detail -> ListDetailPaneScaffoldRole.List
            ListDetailPaneScaffoldRole.List -> ListDetailPaneScaffoldRole.List
            else -> throw IllegalStateException("")
        }

        if (role == ListDetailPaneScaffoldRole.List) {
            selectedIndex = -1
            actualUrl = ""
        }
    }

    object Saver : androidx.compose.runtime.saveable.Saver<HomeScreenState, Bundle> {

        private val ThreePaneScaffoldRole.saveAbleName: String
            get() {
                return when (this) {
                    ListDetailPaneScaffoldRole.List -> "list"
                    ListDetailPaneScaffoldRole.Detail -> "detail"
                    ListDetailPaneScaffoldRole.Extra -> "extra"
                    else -> throw IllegalStateException("Unsupported role: $this")
                }
            }

        private fun fromSaveableName(name: String): ThreePaneScaffoldRole {
            return when (name) {
                "list" -> ListDetailPaneScaffoldRole.List
                "detail" -> ListDetailPaneScaffoldRole.Detail
                "extra" -> ListDetailPaneScaffoldRole.Extra
                else -> throw IllegalStateException("Unsupported role: $name")
            }
        }

        override fun SaverScope.save(value: HomeScreenState): Bundle {
            return Bundle().apply {
                putString("mss_role", value.role.saveAbleName)
                putBoolean("mss_is_empty_pane_visible", value.isEmptyPaneVisible)
                putString("mss_url", value.actualUrl)
                putInt("mss_index", value.selectedIndex)
            }
        }

        override fun restore(value: Bundle): HomeScreenState {
            return HomeScreenState(
                initialRole = fromSaveableName(name = value.getString("mss_role") ?: ""),
                initialIsEmptyPaneVisible = value.getBoolean("mss_is_empty_pane_visible"),
                initialIndex = value.getInt("mss_index", -1),
                initialUrl = value.getString("mss_url", ""),
            )
        }
    }
}


@Composable
fun rememberHomeScreenState(
    initialRole: ThreePaneScaffoldRole = ListDetailPaneScaffoldRole.List,
    initialIsEmptyPaneVisible: Boolean = true,
    initialIndex: Int = -1,
    initialUrl: String = "",
): HomeScreenState {
    return rememberSaveable(saver = HomeScreenState.Saver) {
        HomeScreenState(
            initialRole = initialRole,
            initialIsEmptyPaneVisible = initialIsEmptyPaneVisible,
            initialIndex = initialIndex,
            initialUrl = initialUrl,
        )
    }
}


val LocalMainScreenNavigator: ProvidableCompositionLocal<ThreePaneScaffoldNavigator<Nothing>> =
    compositionLocalOf(
        defaultFactory = {
            error(
                message = "LocalMainScreenNavigator was not initialized yet or you called it outside the scope." +
                        " LocalMainScreenNavigator should be used only in MainScreen and it's content."
            )
        }
    )

val LocalHomeScreenState: ProvidableCompositionLocal<HomeScreenState> =
    compositionLocalOf(
        defaultFactory = {
            HomeScreenState(
                initialRole = ListDetailPaneScaffoldRole.List,
                initialIsEmptyPaneVisible = false,
                initialIndex = -1,
                initialUrl = "",
            )
//            error(
//                message = "LocalMainScreenStae was not initialized yet or you called it outside the scope." +
//                        " LocalMainScreenStae should be used only in MainScreen and it's content."
//            )
        }
    )
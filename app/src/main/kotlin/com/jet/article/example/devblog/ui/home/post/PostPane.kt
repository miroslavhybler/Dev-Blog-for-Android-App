@file:OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalSharedTransitionApi::class,
)

package com.jet.article.example.devblog.ui.home.post

import android.animation.ArgbEvaluator
import android.content.ActivityNotFoundException
import android.content.Intent
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.Animatable
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.trace
import androidx.core.net.toUri
import com.jet.article.ArticleParser
import com.jet.article.data.HtmlElement
import com.jet.article.example.devblog.R
import com.jet.article.example.devblog.composables.CustomHtmlImage
import com.jet.article.example.devblog.composables.ErrorLayout
import com.jet.article.example.devblog.composables.PostTopBar
import com.jet.article.example.devblog.data.AdjustedPostData
import com.jet.article.example.devblog.data.database.PostItem
import com.jet.article.example.devblog.horizontalPadding
import com.jet.article.example.devblog.rememberCurrentOffset
import com.jet.article.example.devblog.shared.Tracing
import com.jet.article.example.devblog.ui.DevBlogAppTheme
import com.jet.article.example.devblog.ui.LocalDimensions
import com.jet.article.example.devblog.ui.LocalTtsClient
import com.jet.article.example.devblog.ui.home.LocalHomeScreenState
import com.jet.article.example.devblog.ui.home.list.NewPostMark
import com.jet.article.ui.JetHtmlArticleContent
import com.jet.article.ui.Link
import com.jet.article.ui.LinkClickHandler
import com.jet.article.ui.rememberJetHtmlArticleState
import com.jet.tts.TextTts
import com.jet.tts.Utterance
import com.jet.tts.rememberTtsState
import com.jet.utils.dpToPx
import com.jet.utils.pxToDp
import kotlinx.coroutines.launch


/**
 * Showing single [PostItem] selected on [com.jet.article.example.devblog.ui.home.list.HomeListPane].
 * [AdjustedPostData] are parsed from Html using [com.jet.article.ArticleParser] in [com.jet.article.example.devblog.data.CoreRepo].
 * @author Miroslav Hýbler <br>
 * created on 13.08.2024
 */
@Composable
fun PostPane(
    data: Result<AdjustedPostData>?,
    onOpenContests: () -> Unit,
    listState: LazyListState,
    selectedPost: PostItem?,
    onRefresh: (PostItem) -> Unit,
) = trace(sectionName = Tracing.Section.postPane) {

    val context = LocalContext.current
    val dimensions = LocalDimensions.current
    val mainState = LocalHomeScreenState.current
    val density = LocalDensity.current
    val ttsClient = LocalTtsClient.current

    val ttsState = rememberTtsState()

    val colorScheme = MaterialTheme.colorScheme
    val post = remember(key1 = data) {
        data?.getOrNull()
    }
    var lastUrl: String? by remember { mutableStateOf(value = null) }
    val colorEvaluator = remember { ArgbEvaluator() }
    val coroutineScope = rememberCoroutineScope()
    val scrollOffset by rememberCurrentOffset(state = listState)
    var topBarAlpha by rememberSaveable { mutableFloatStateOf(value = 0f) }

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(
        state = rememberTopAppBarState()
    )
    val statusBarPadding = WindowInsets.statusBars.getTop(density = density)

    var headerImageHeight by rememberSaveable() {
        mutableFloatStateOf(
            value = density.dpToPx(dp = TopAppBarDefaults.LargeAppBarExpandedHeight)
                .plus(other = statusBarPadding)
        )
    }
    val state = rememberJetHtmlArticleState(listState = listState)
    var titleStartColor by remember { mutableStateOf(value = colorScheme.background) }
    val titleEndColor = colorScheme.onBackground
    val titleColor = remember { Animatable(initialValue = colorScheme.onBackground) }

    val linkCallback = remember {
        object : LinkClickHandler.LinkCallback() {
            override fun onOtherDomainLink(link: Link.OtherDomainLink) {
                try {
                    context.startActivity(
                        Intent(Intent.ACTION_VIEW)
                            .setData(link.fullLink.toUri())
                    )
                } catch (e: ActivityNotFoundException) {
                    e.printStackTrace()
                }
            }

            override fun onSameDomainLink(link: Link.SameDomainLink) {
            }

            override fun onUriLink(link: Link.UriLink) {
            }

            override fun onSectionLink(
                link: Link.SectionLink,
            ) {
                coroutineScope.launch {
                    val i = state.data.elements.indexOfFirst { element ->
                        element.id == link.rawLink.removePrefix(prefix = "#")
                    }

                    i.takeIf { index -> index != -1 }
                        ?.let { index ->
                            //Tries to scroll to the right section given by element id
                            state.listState.animateScrollToItem(
                                index = index,
                                scrollOffset = scrollOffset,
                            )
                        }
                }
            }
        }
    }

    var isRefreshing by rememberSaveable { mutableStateOf(value = false) }
    var selectedImageUrl: String? by rememberSaveable { mutableStateOf(value = null) }

    LaunchedEffect(key1 = data) {
        if (
            data != null
            && data.isSuccess
            && data.getOrNull()?.postData?.url != lastUrl
            && post != null
        ) {
            if (post.postData.linkHandler.callback == null) {
                post.postData.linkHandler.callback = linkCallback
            }
            state.show(data = post.postData)
            listState.scrollToItem(index = 0, scrollOffset = 0)
            lastUrl = data.getOrNull()?.postData?.url
        }
        if (isRefreshing) {
            isRefreshing = false
        }
    }

    LaunchedEffect(key1 = post) {
        val mPost = post ?: return@LaunchedEffect
        //Adding content from post to TTS State so text can be spoken

        mPost.postData.elements.forEach { element ->
            when (element) {
                is HtmlElement.Title -> {
                    ttsState["${element.key}"] =
                        ArticleParser.Utils.clearTagsAndReplaceEntitiesFromText(
                            input = element.text
                        )
                }

                is HtmlElement.TextBlock -> {
                    ttsState["${element.key}"] =
                        ArticleParser.Utils.clearTagsAndReplaceEntitiesFromText(
                            input = element.text.toString()
                        )
                }

                else -> return@forEach
            }
        }

//TODO use for speaking
//TODO scroll is working but there is problem with contentPadding
//        ttsState.values.sortedBy(selector = Utterance::sequence)
//            .forEachIndexed { index, utterance ->
//                if (index == 0)
//                    ttsClient.flushAndSpeak(utterance = utterance)
//                else
//                    ttsClient.add(utterance = utterance)
//            }
    }

    LaunchedEffect(
        key1 = scrollOffset,
        key2 = titleStartColor,
    ) {
        val alpha = if (scrollOffset < 128) (scrollOffset / (128f)) else 0.85f
        topBarAlpha = alpha.coerceIn(minimumValue = 0.15f, maximumValue = 0.85f)
        titleColor.snapTo(
            targetValue = Color(
                color = colorEvaluator.evaluate(
                    alpha.coerceIn(minimumValue = 0f, maximumValue = 1f),
                    titleStartColor.toArgb(),
                    titleEndColor.toArgb(),
                ) as Int
            )
        )
    }


    BackHandler(enabled = selectedImageUrl != null) {
        selectedImageUrl = null
    }


    Scaffold(
        modifier = Modifier
            .nestedScroll(connection = scrollBehavior.nestedScrollConnection),
        topBar = {
            PostTopBar(
                modifier = Modifier
                    .onSizeChanged { newSize ->
                        headerImageHeight = newSize.height.toFloat()
                    },
                title = remember(key1 = selectedPost?.id) {
                    selectedPost?.title ?: ""
                },
                scrollBehavior = scrollBehavior,
                backgroundAlpha = topBarAlpha,
                titleColor = titleColor.value,
            )
        },
        content = { paddingValues ->
            SharedTransitionLayout {
                AnimatedContent(
                    targetState = selectedImageUrl,
                    label = "basic_transition"
                ) { openedImageUrl ->
                    if (openedImageUrl == null) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                        ) {
                            PullToRefreshBox(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(
                                        top = paddingValues.calculateTopPadding(),
                                        start = paddingValues.calculateStartPadding(
                                            LocalLayoutDirection.current
                                        ),
                                        end = paddingValues.calculateEndPadding(LocalLayoutDirection.current),
                                    ),
                                isRefreshing = isRefreshing,
                                onRefresh = {
                                    selectedPost?.let {
                                        isRefreshing = true
                                        onRefresh(it)
                                    }
                                },
                            ) {
                                if (
                                    data?.isFailure == true
                                    || (data?.isSuccess == true && post?.postData?.elements.isNullOrEmpty())
                                ) {
                                    //Vertical scroll for the refresh to enable refresh in error case
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .verticalScroll(state = rememberScrollState())
                                            .nestedScroll(connection = scrollBehavior.nestedScrollConnection)

                                    ) {
                                        ErrorLayout(
                                            modifier = Modifier
                                                .statusBarsPadding()
                                                .padding(top = dimensions.topLinePadding)
                                                .horizontalPadding()
                                                .align(alignment = Alignment.TopCenter),
                                            title = stringResource(R.string.error_unable_load_post),
                                            cause = data.exceptionOrNull(),
                                            onRefresh = {
                                                onRefresh(selectedPost ?: return@ErrorLayout)
                                            }
                                        )
                                    }
                                }


                                if (post != null) {
                                    JetHtmlArticleContent(
                                        modifier = Modifier
                                            .testTag(tag = Tracing.Tag.jetHtmlArticle)
                                            .fillMaxSize()
                                            .nestedScroll(connection = scrollBehavior.nestedScrollConnection),
                                        state = state,
                                        contentPadding = PaddingValues(
                                            start = dimensions.topLinePadding,
                                            top = dimensions.topLinePadding,
                                            end = dimensions.sidePadding,
                                            //56.dp from FabPrimaryTokens.ContainerHeight
                                            bottom = paddingValues.calculateBottomPadding() + dimensions.bottomLinePadding + 56.dp,
                                        ),
                                        verticalArrangement = Arrangement.spacedBy(space = 24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        header = {
                                            if (selectedPost?.isUnread == true) {
                                                //Using isUnread instead of isUnreadState on purpose, post is marked
                                                //as read when opened, so this will keep the mark visible
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .wrapContentHeight()
                                                ) {
                                                    NewPostMark(
                                                        modifier = Modifier
                                                            .align(alignment = Alignment.TopStart)
                                                    )
                                                }
                                            }
                                        },
                                        text = { text ->
                                            //TODO custom title too
                                            TextTts(
                                                text = text.text,
                                                utteranceId = "${text.key}",
                                                ttsClient = ttsClient,
                                                scrollableState = state.listState,
                                                highlightStyle = TextStyle(
                                                    color = MaterialTheme.colorScheme.secondary,
                                                )
                                            )
                                        },
                                        image = { image ->
                                            CustomHtmlImage(
                                                modifier = Modifier
                                                    .animateContentSize()
                                                    .clickable(
                                                        onClick = {
                                                            selectedImageUrl = image.url
                                                        }
                                                    ),
                                                image = image,
                                            )
                                        }
                                    )
                                }


                                if (data == null) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.align(alignment = Alignment.Center)
                                    )
                                }
                            }

                            //Image background of the topbar
                            post?.headerImage?.let { headerImage ->
                                CustomHtmlImage(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(height = density.pxToDp(px = headerImageHeight))
                                        .animateContentSize(),
                                    image = headerImage,
                                )
                            }
                        }
                    } else {
                        ImageDetailLayout(
                            modifier = Modifier
                                .padding(paddingValues = paddingValues)
                                .fillMaxSize()
                                .horizontalPadding(),
                            url = openedImageUrl,
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (
                post != null
                && (mainState.role == ListDetailPaneScaffoldRole.Detail
                        || mainState.role == ListDetailPaneScaffoldRole.Extra)
                && post.contest.isNotEmpty()
            ) {
                AnimatedVisibility(
                    visible = selectedImageUrl == null,
                    enter = fadeIn(),
                    exit = fadeOut(),
                ) {
                    FloatingActionButton(
                        modifier = Modifier.horizontalPadding(),
                        onClick = onOpenContests,
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_content),
                            contentDescription = stringResource(R.string.content_desc_show_contest),
                        )
                    }
                }
            }
        },
        bottomBar = {
            //TODO  PostBottomBar()
        }
    )
}


@Composable
@PreviewLightDark
private fun PostPanePreview1() {
    DevBlogAppTheme {
        PostPane(
            data = null,
            onOpenContests = {},
            listState = rememberLazyListState(),
            selectedPost = null,
            onRefresh = {},
        )
    }
}


@Composable
@PreviewLightDark
private fun PostPanePreview2() {
    DevBlogAppTheme {
        PostPane(
            data = Result.failure(exception = IllegalStateException()),
            onOpenContests = {},
            listState = rememberLazyListState(),
            selectedPost = null,
            onRefresh = {},
        )
    }
}
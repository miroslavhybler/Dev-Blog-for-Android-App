@file:OptIn(ExperimentalMaterial3Api::class)

package com.jet.article.example.devblog.ui.home

import android.animation.ArgbEvaluator
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.animation.Animatable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.safeGesturesPadding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.trace
import androidx.core.net.toUri
import com.jet.article.data.HtmlArticleData
import com.jet.article.example.devblog.R
import com.jet.article.example.devblog.composables.CustomHtmlImage
import com.jet.article.example.devblog.composables.ErrorLayout
import com.jet.article.example.devblog.composables.PostTopBar
import com.jet.article.example.devblog.data.AdjustedPostData
import com.jet.article.example.devblog.horizontalPadding
import com.jet.article.example.devblog.rememberCurrentOffset
import com.jet.article.example.devblog.ui.DevBlogAppTheme
import com.jet.article.example.devblog.ui.LocalDimensions
import com.jet.article.ui.JetHtmlArticleContent
import com.jet.article.ui.Link
import com.jet.article.ui.LinkClickHandler
import com.jet.utils.dpToPx
import com.jet.utils.plus
import com.jet.utils.pxToDp
import kotlinx.coroutines.launch


/**
 * @author Miroslav Hýbler <br>
 * created on 13.08.2024
 */
@Composable
fun PostPane(
    data: Result<AdjustedPostData>?,
    onOpenContests: () -> Unit,
    listState: LazyListState,
) = trace(sectionName = "PostPane") {
    val context = LocalContext.current
    val dimensions = LocalDimensions.current
    val mainState = LocalHomeScreenState.current
    val density = LocalDensity.current
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

            override fun onUriLink(link: Link.UriLink, context: Context) {
            }

            override fun onSectionLink(
                link: Link.SectionLink,
                lazyListState: LazyListState,
                data: HtmlArticleData,
                scrollOffset: Int,
            ) {
                coroutineScope.launch {
                    val i = data.elements.indexOfFirst { element ->
                        element.id == link.rawLink.removePrefix(prefix = "#")
                    }

                    i.takeIf { index -> index != -1 }
                        ?.let { index ->
                            lazyListState.animateScrollToItem(
                                index = index,
                                scrollOffset = scrollOffset
                            )
                        }

                }
            }
        }
    }


    LaunchedEffect(key1 = data) {
        if (data != null && data.isSuccess && data.getOrNull()?.postData?.url != lastUrl) {
            listState.scrollToItem(index = 0, scrollOffset = 0)
            lastUrl = data.getOrNull()?.postData?.url
        }
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


    Scaffold(
        modifier = Modifier
            .nestedScroll(connection = scrollBehavior.nestedScrollConnection),
        topBar = {
            PostTopBar(
                modifier = Modifier
                    .onSizeChanged { newSize ->
                        headerImageHeight = newSize.height.toFloat()
                    },
                title = remember(key1 = post?.title?.key) {
                    post?.title?.text ?: ""
                },
                scrollBehavior = scrollBehavior,
                backgroundAlpha = topBarAlpha,
                titleColor = titleColor.value,
            )
        },
        content = { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize(),
            ) {
                if (
                    data?.isFailure == true
                    || (data?.isSuccess == true && post?.postData?.elements.isNullOrEmpty())
                ) {
                    Log.d("mirek", "recompose error")

                    ErrorLayout(
                        modifier = Modifier
                            .statusBarsPadding()
                            .padding(paddingValues = paddingValues)
                            .padding(top = dimensions.topLinePadding)
                            .horizontalPadding()
                            .align(alignment = Alignment.TopCenter),
                        title = stringResource(R.string.error_unable_load_post),
                        cause = data.exceptionOrNull(),
                    )
                }

                if (post != null) {
                    Log.d("mirek", "recompose post")
                    JetHtmlArticleContent(
                        modifier = Modifier
                            .testTag(tag = "jet_html_article")
                            .fillMaxSize()
                            .nestedScroll(connection = scrollBehavior.nestedScrollConnection),
                        containerColor = Color.Transparent,
                        listState = listState,
                        contentPadding = paddingValues + PaddingValues(
                            start = dimensions.topLinePadding,
                            top = dimensions.topLinePadding,
                            end = dimensions.sidePadding,
                            //56.dp from FabPrimaryTokens.ContainerHeight
                            bottom = dimensions.bottomLinePadding + 56.dp,
                        ),
                        data = post.postData,
                        verticalArrangement = Arrangement.spacedBy(space = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        linkClickCallback = linkCallback,
                        image = { image ->
                            CustomHtmlImage(
                                modifier = Modifier.animateContentSize(),
                                image = image,
                            )
                        }
                    )

                    CustomHtmlImage(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(height = density.pxToDp(px = headerImageHeight))
                            .animateContentSize(),
                        image = post.headerImage,
                    )
                }

                if (data == null || post == null) {
                    Log.d("mirek", "recompose loading")
                    CircularProgressIndicator(
                        modifier = Modifier.align(alignment = Alignment.Center)
                    )
                }
//                AnimatedVisibility(
//                    modifier = Modifier.align(alignment = Alignment.Center),
//                    visible = data == null,
//                    enter = fadeIn() + scaleIn(),
//                    exit = fadeOut() + scaleOut()
//                ) {
//                    CircularProgressIndicator(
//                        modifier = Modifier.align(alignment = Alignment.Center)
//                    )
//                }

                Log.d("mirek", "recompose")
            }
        },
        floatingActionButton = {
            //TODO check if titles are empty & scroll to top action
            if (
                post != null
                && (mainState.role == ListDetailPaneScaffoldRole.Detail
                        || mainState.role == ListDetailPaneScaffoldRole.Extra)
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
        )
    }
}
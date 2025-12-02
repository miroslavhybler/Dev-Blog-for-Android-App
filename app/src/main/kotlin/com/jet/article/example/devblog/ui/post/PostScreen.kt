@file:OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalSharedTransitionApi::class,
)

package com.jet.article.example.devblog.ui.post

import android.animation.ArgbEvaluator
import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.Animatable
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.trace
import androidx.core.net.toUri
import androidx.core.text.toSpannable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jet.article.ArticleParser
import com.jet.article.data.HtmlArticleData
import com.jet.article.data.HtmlElement
import com.jet.article.example.devblog.R
import com.jet.article.example.devblog.composables.CustomHtmlImage
import com.jet.article.example.devblog.composables.ErrorLayout
import com.jet.article.example.devblog.composables.PostTopBar
import com.jet.article.example.devblog.data.AdjustedPostData
import com.jet.article.example.devblog.data.Month
import com.jet.article.example.devblog.data.SettingsStorage
import com.jet.article.example.devblog.data.SimpleDate
import com.jet.article.example.devblog.data.database.PostItem
import com.jet.article.example.devblog.horizontalPadding
import com.jet.article.example.devblog.openWeb
import com.jet.article.example.devblog.rememberCurrentOffset
import com.jet.article.example.devblog.shareUrl
import com.jet.article.example.devblog.shared.Tracing
import com.jet.article.example.devblog.ui.DevBlogAppTheme
import com.jet.article.example.devblog.ui.LocalDeepLink
import com.jet.article.example.devblog.ui.LocalDimensions
import com.jet.article.example.devblog.ui.LocalTtsClient
import com.jet.article.example.devblog.ui.MainActivity
import com.jet.article.example.devblog.ui.Route
import com.jet.article.example.devblog.ui.SectionSelectedEvent
import com.jet.article.example.devblog.ui.home.NewPostMark
import com.jet.article.ui.JetHtmlArticleContent
import com.jet.article.ui.Link
import com.jet.article.ui.LinkClickHandler
import com.jet.article.ui.elements.HtmlBasicList
import com.jet.article.ui.rememberJetHtmlArticleState
import com.jet.tts.TextTts
import com.jet.tts.rememberTtsState
import com.jet.utils.dpToPx
import com.jet.utils.pxToDp
import com.jet.article.example.devblog.composables.MessageSnackbar
import com.jet.article.example.devblog.composables.rememberSnackbarState
import com.jet.article.example.devblog.overrideSpecifiedTextColors
import com.jet.article.ui.JetHtmlArticleState
import com.jet.tts.TtsLifecycleAwareEffect
import com.jet.tts.TtsState
import com.jet.tts.rememberTtsClient
import com.jet.utils.statusBarsPaddingPx
import kotlinx.coroutines.launch


/**
 * Showing single [PostItem] selected on [com.jet.article.example.devblog.ui.home.HomeListPane].
 * [AdjustedPostData] are parsed from Html using [ArticleParser] in [com.jet.article.example.devblog.data.CoreRepo].
 * @author Miroslav Hýbler <br>
 * created on 13.08.2024
 */
@Composable
fun PostScreen(
    route: Route.Post,
    onNavigate: (Route) -> Unit,
    onBack: () -> Unit,
    selectedSectionEvent: SectionSelectedEvent?,
    viewModel: PostViewModel = hiltViewModel(),
) = trace(sectionName = Tracing.Section.postPane) {

    val selectedPost = route.item
    val deeplink = LocalDeepLink.current
    val density = LocalDensity.current
    val ttsClient = LocalTtsClient.current

    val settings by viewModel.settings.collectAsState(
        initial = SettingsStorage.Settings.Default,
    )

    val data by viewModel.postData.collectAsStateWithLifecycle()


    val ttsState = rememberTtsState(key = selectedPost.id)

    val state = rememberJetHtmlArticleState(listState = rememberLazyListState())


    var selectedImageUrl: String? by rememberSaveable { mutableStateOf(value = null) }

    //Initializes ttsClient with the state
    TtsLifecycleAwareEffect(
        client = ttsClient ?: throw NullPointerException("TtsClient not provided"),
        state = ttsState,
    )

    LaunchedEffect(key1 = deeplink) {
        if (deeplink != null) {
            viewModel.loadPostFromDeeplink(
                url = deeplink,
                onFinal = {
                    MainActivity.onDeeplinkOpened()
                },
            )
        } else {
            viewModel.loadPostDetail(item = selectedPost)
        }
    }



    LaunchedEffect(key1 = selectedSectionEvent) {
        val event = selectedSectionEvent ?: return@LaunchedEffect
        state.listState.animateScrollToItem(
            index = event.index,
            scrollOffset = density.dpToPx(dp = 24.dp).toInt(),
        )
        event.isConsumed = true
    }



    DisposableEffect(key1 = Unit) {
        onDispose {
            ttsClient.stop()
            viewModel.clear()
        }
    }

    BackHandler(enabled = selectedImageUrl != null) {
        selectedImageUrl = null
    }

    PostScreenImpl(
        route = route,
        onNavigate = onNavigate,
        onBack = onBack,
        state = state,
        selectedSectionEvent = selectedSectionEvent,
        ttsState = ttsState,
        settings = settings,
        data = data,
        onRefresh = {
            viewModel.loadPostDetail(
                item = selectedPost,
                isRefresh = true
            )
        },
        onToggleFavorite = {
            viewModel.toggleFavoriteItem(item = selectedPost)
        }
    )
}


@Composable
private fun PostScreenImpl(
    route: Route.Post,
    onNavigate: (Route) -> Unit,
    onBack: () -> Unit,
    state: JetHtmlArticleState,
    selectedSectionEvent: SectionSelectedEvent?,
    ttsState: TtsState,
    settings: SettingsStorage.Settings,
    data: Result<AdjustedPostData>?,
    onRefresh: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    val selectedPost = route.item
    val colorScheme = MaterialTheme.colorScheme
    val listState = state.listState

    val density = LocalDensity.current
    val dimensions = LocalDimensions.current
    val ttsClient = LocalTtsClient.current
    val context = LocalContext.current

    val statusBarPadding = density.statusBarsPaddingPx()

    val colorEvaluator = remember { ArgbEvaluator() }
    val coroutineScope = rememberCoroutineScope()
    val scrollOffsetY by rememberCurrentOffset(state = listState)
    var lastScrollOffsetY by remember() { mutableIntStateOf(value = scrollOffsetY) }
    var topBarAlpha by rememberSaveable { mutableFloatStateOf(value = 0f) }
    val topBarScrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(
        state = rememberTopAppBarState()
    )
    var titleStartColor by remember { mutableStateOf(value = colorScheme.background) }
    val titleEndColor = colorScheme.onBackground
    val titleColor = remember { Animatable(initialValue = colorScheme.onBackground) }
    var headerImageHeight by rememberSaveable() {
        mutableFloatStateOf(
            value = density.dpToPx(dp = TopAppBarDefaults.LargeAppBarExpandedHeight)
                .plus(other = statusBarPadding)
        )
    }
    var selectedImageUrl: String? by rememberSaveable { mutableStateOf(value = null) }

    val snackbarState = rememberSnackbarState()
    var isRefreshing by rememberSaveable { mutableStateOf(value = false) }
    val post = remember(key1 = data) { data?.getOrNull() }
    var lastUrl: String? by remember { mutableStateOf(value = null) }


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
                                scrollOffset = scrollOffsetY,
                            )
                        }
                }
            }
        }
    }


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
            state.listState.scrollToItem(index = 0, scrollOffset = 0)
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
                    ttsState["${selectedPost.id}_${element.key}"] =
                        ArticleParser
                            .Utils.clearTagsAndReplaceEntitiesFromText(
                                input = element.text.toString(),
                            )
                }

                is HtmlElement.TextBlock -> {
                    ttsState["${selectedPost.id}_${element.key}"] =
                        ArticleParser.Utils.clearTagsAndReplaceEntitiesFromText(
                            input = element.text.toString()
                        )
                }

                is HtmlElement.BasicList -> {
                    element.items.forEachIndexed { index, item ->
                        val cleanTextForTTS =
                            ArticleParser.Utils.clearTagsAndReplaceEntitiesFromText(
                                input = item.text,
                            )
                        val utteranceId = "${selectedPost.id}_${element.key + 1_000_000 + index}"
                        ttsState[utteranceId] = cleanTextForTTS
                    }
                }

                else -> return@forEach
            }
        }
    }



    LaunchedEffect(
        key1 = scrollOffsetY,
        key2 = titleStartColor,
    ) {
        val alpha = if (scrollOffsetY < 128) (scrollOffsetY / (128f)) else 0.85f
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

        val limit = topBarScrollBehavior.state.heightOffsetLimit
        val height = topBarScrollBehavior.state.heightOffset

        //using + (plus) because topBarScrollBehavior.state.heightOffset is negative value
        val availableY = -(scrollOffsetY + height)

        if (
            scrollOffsetY > lastScrollOffsetY
        ) {
            val consumedByAppBar = topBarScrollBehavior.nestedScrollConnection.onPreScroll(
                available = Offset(x = 0f, y = availableY),
                source = NestedScrollSource.UserInput,
            )
            lastScrollOffsetY = scrollOffsetY
        }

        // }
    }

    Scaffold(
        modifier = Modifier
            .nestedScroll(connection = topBarScrollBehavior.nestedScrollConnection),
        topBar = {
            PostTopBar(
                modifier = Modifier
                    .onSizeChanged { newSize ->
                        headerImageHeight = newSize.height.toFloat()
                    },
                title = remember(key1 = selectedPost.id) {
                    AnnotatedString(text = selectedPost.title)
                },
                scrollBehavior = topBarScrollBehavior,
                backgroundAlpha = topBarAlpha,
                titleColor = titleColor.value,
                onNavigationIcon = onBack,
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
                                            layoutDirection = LocalLayoutDirection.current
                                        ),
                                        end = paddingValues.calculateEndPadding(layoutDirection = LocalLayoutDirection.current),
                                    ),
                                isRefreshing = isRefreshing,
                                onRefresh = {
                                    isRefreshing = true
                                    onRefresh()
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
                                            .nestedScroll(connection = topBarScrollBehavior.nestedScrollConnection)

                                    ) {
                                        ErrorLayout(
                                            modifier = Modifier
                                                .statusBarsPadding()
                                                .padding(top = dimensions.topLinePadding)
                                                .horizontalPadding()
                                                .align(alignment = Alignment.TopCenter),
                                            title = stringResource(id = R.string.error_unable_load_post),
                                            cause = data.exceptionOrNull(),
                                            onRefresh = onRefresh,
                                        )
                                    }
                                }

                                if (post != null) {
                                    JetHtmlArticleContent(
                                        modifier = Modifier
                                            .testTag(tag = Tracing.Tag.jetHtmlArticle)
                                            .fillMaxSize()
                                            .nestedScroll(connection = topBarScrollBehavior.nestedScrollConnection),
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
                                            if (selectedPost.isUnread) {
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
                                        title = { title ->
                                            val style = when (title.titleTag) {
                                                "h1", "h2" -> MaterialTheme.typography.displaySmall
                                                else -> MaterialTheme.typography.titleLarge
                                            }
                                            TextTts(
                                                text = title.text,
                                                utteranceId = "${selectedPost.id}_${title.key}",
                                                ttsClient = ttsClient
                                                    ?: throw NullPointerException("TtsClient not provided"),
                                                scrollableState = state.listState,
                                                style = style,
                                                highlightStyle = style.copy(
                                                    color = MaterialTheme.colorScheme.secondary,
                                                ),
                                                color = MaterialTheme.colorScheme.onBackground,
                                            )
                                        },
                                        text = { text ->
                                            TextTts(
                                                text = text.text.overrideSpecifiedTextColors(
                                                    newColor = MaterialTheme.colorScheme.onBackground,
                                                ),
                                                utteranceId = "${selectedPost.id}_${text.key}",
                                                ttsClient = ttsClient
                                                    ?: throw NullPointerException("TtsClient not provided"),
                                                scrollableState = state.listState,
                                                highlightStyle = TextStyle(
                                                    color = MaterialTheme.colorScheme.secondary,
                                                ),
                                                color = MaterialTheme.colorScheme.onBackground,
                                            )
                                        },
                                        basicList = { basicList ->
                                            HtmlBasicList(
                                                list = basicList,
                                                textContent = { text, index ->
                                                    TextTts(
                                                        text = text.overrideSpecifiedTextColors(
                                                            newColor = MaterialTheme.colorScheme.onBackground,
                                                        ),
                                                        utteranceId = "${selectedPost.id}_${basicList.key + 1_000_000 + index}",
                                                        ttsClient = ttsClient
                                                            ?: throw NullPointerException("TtsClient not provided"),
                                                        scrollableState = state.listState,
                                                        highlightStyle = TextStyle(
                                                            color = MaterialTheme.colorScheme.secondary,
                                                        ),
                                                        color = MaterialTheme.colorScheme.onBackground,
                                                    )
                                                }
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
        snackbarHost = {
            MessageSnackbar(
                state = snackbarState,
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                contentAlignment = Alignment.CenterEnd,
            ) {
                PostBottomBar(
                    ttsState = ttsState,
                    onToggleFavorite = onToggleFavorite,
                    onShowContest = {
                        onNavigate(Route.Contest(item = selectedPost))
                    },
                    onOpenWeb = {
                        context.openWeb(url = selectedPost.url)
                    },
                    onShare = {
                        context.shareUrl(
                            url = selectedPost.url,
                            title = selectedPost.title
                        )
                    },
                    isFavorite = selectedPost.isFavoriteState,
                    isUsingTTS = settings.isUsingTTS,
                    snackbarState = snackbarState,
                )
            }
        }
    )
}


@Composable
@PreviewLightDark
private fun PostPanePreview1() {

    val articeData = remember {
        HtmlArticleData(
            url = "https://android-developers.googleblog.com/2025/09/weareplay-meet-the-people-building-vibrant-communities-with-their-apps-and-games.html",
            elements = listOf(
                HtmlElement.TextBlock(
                    text = buildAnnotatedString {
                        append(
                            text = "In our latest #WeArePlay stories, we meet the founders who turned their interests into vibrant communities through their apps and games on Google Play - from democratizing music production for artists in Brazil to building a global network for Black professionals.\n" +
                                    "\n" +
                                    "Here are a few of our favorites:"
                        )
                    },
                    id = null,
                    key = 3,
                ),
                HtmlElement.Image(
                    url = "https://blogger.googleusercontent.com/img/b/R29vZ2xl/AVvXsEi-Ux4E4BpQ4Ojs3ifq6bcYEyfx2Ehb8TGYSXt0ELGoN4ztjwxFOx3i0z5R22U08MM_225-8j395cT840zcn7313BbWFXEOxpHA4gnX2PUQPiDBeMptoOlF9dBDGVSzAm7ULXfnrBVCFzRxWQ0yAIKv1XpSfnrTuBxgUiZBe0vPalqzKkl14h5DcMisHR4/s1600/01_BRAZIL_MURB_FOUNDER_ERICK%20MACEDO_KELVIN%20MACEDO_edited.jpg",
                    alt = null,
                    description = null,
                    id = null,
                    key = 4,
                    defaultSize = IntSize.Zero,
                ),
                HtmlElement.Title(
                    text = buildAnnotatedString {
                        append(
                            text = "Erik and Kelvin’s app Murb connects and enables indie artists in Brazil to create and distribute music from their phones."
                        )
                    },
                    id = null,
                    key = 5,
                    titleTag = "h3",
                ),
                HtmlElement.TextBlock(
                    text = buildAnnotatedString {
                        append(
                            text = "Brothers Erick and Kelvin combined their passion for Brazil's urban culture from skateboarding to graffiti with their professional experience in tech to create Murb. Their app empowers young, independent artists in the rap, trap, and funk communities to produce music directly on their phones, providing a vital platform for those who lack access to studios. With recent features like producer profiles to help musicians sell their work and plans to integrate with major streaming platforms, Murb is on a mission to democratize music production."
                        )
                    },
                    id = null,
                    key = 6,
                )
            )
        )
    }

    CompositionLocalProvider(
        LocalTtsClient provides rememberTtsClient(),
    ) {
        DevBlogAppTheme {
            PostScreenImpl(
                onNavigate = { _ -> },
                route = Route.Post(
                    item = PostItem(
                        title = "#WeArePlay: Meet the people building vibrant communities with their apps and games",
                        url = "https://android-developers.googleblog.com/2025/09/weareplay-meet-the-people-building-vibrant-communities-with-their-apps-and-games.html",
                        date = SimpleDate(
                            dayOfMonth = 24,
                            month = Month.SEPTEMBER,
                            year = 2025,
                        ),
                        isUnread = true,
                        isFavorite = false,
                        description = "Description",
                        image = "https://blogger.googleusercontent.com/img/b/R29vZ2xl/AVvXsEhXHK03KiFLi-Ej2Pewi30s2R58kkiiIkl7mzZx9PNC28ac7zbsIvkTqsQZJoKh3QeogtqCwP2vF2iwBaWqE7H770IUfFyZlZX41ZvdqN-3Cvd1hsBowXGOaMR2w0IB4vyBLzX6xfbT7qGbuIeEXZOZzMShMLs9QrTbJeaNkSeImX9GCiqdTOEiJD8kycc/s1600/Android%20Devs%20_%20Google%20Devs%20-Blog_Header_1200x600.jpg",
                        dateTimeStamp = 0,
                    )
                ),
                onBack = {},
                ttsState = rememberTtsState(), //Safe to use for preview
                selectedSectionEvent = null,
                settings = SettingsStorage.Settings.Default,
                data = Result.success(
                    value = AdjustedPostData(
                        headerImage = HtmlElement.Image(
                            url = "https://blogger.googleusercontent.com/img/b/R29vZ2xl/AVvXsEhXHK03KiFLi-Ej2Pewi30s2R58kkiiIkl7mzZx9PNC28ac7zbsIvkTqsQZJoKh3QeogtqCwP2vF2iwBaWqE7H770IUfFyZlZX41ZvdqN-3Cvd1hsBowXGOaMR2w0IB4vyBLzX6xfbT7qGbuIeEXZOZzMShMLs9QrTbJeaNkSeImX9GCiqdTOEiJD8kycc/s1600/Android%20Devs%20_%20Google%20Devs%20-Blog_Header_1200x600.jpg",
                            description = null,
                            defaultSize = IntSize.Zero,
                            alt = null,
                            id = null,
                            key = 1,
                        ),
                        date = SimpleDate(
                            dayOfMonth = 24,
                            month = Month.SEPTEMBER,
                            year = 2025,
                        ),
                        title = HtmlElement.Title(
                            text = buildAnnotatedString {
                                append(text = "#WeArePlay: Meet the people building vibrant communities with their apps and games")
                            },
                            key = 0,
                            titleTag = "h1",
                            id = null,
                        ),
                        postData = articeData,
                        contest = emptyList(),
                    )
                ),
                onRefresh = {},
                onToggleFavorite = {},
                state = rememberJetHtmlArticleState(initialData = articeData),
            )
        }
    }
}
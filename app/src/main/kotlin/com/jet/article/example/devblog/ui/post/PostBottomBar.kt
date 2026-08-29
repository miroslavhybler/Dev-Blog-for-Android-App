@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.jet.article.example.devblog.ui.post

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ButtonGroupMenuState
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.jet.article.example.devblog.R
import com.jet.article.example.devblog.ui.DevBlogAppTheme
import com.jet.article.example.devblog.ui.LocalDimensions
import com.jet.article.example.devblog.ui.LocalTtsClient
import com.jet.article.example.devblog.ui.colorFavorited
import com.jet.tts.TtsState
import com.jet.tts.rememberTtsState
import kotlinx.coroutines.delay


const val DEFAULT_WEIGHT: Float = 1f
const val NOT_FAVORITE_WEIGHT: Float = 1.5f
const val MAJOR_ITEM_WEIGHT: Float = 2f

/** Post actions presented as an expressive Material button group. */
@Composable
fun PostBottomBar(
    modifier: Modifier = Modifier,
    isExpanded: Boolean,
    onExpandedChange: (isExpanded: Boolean) -> Unit,
    ttsState: TtsState,
    onToggleFavorite: () -> Unit,
    onShowContest: () -> Unit,
    onOpenWeb: () -> Unit,
    onShare: () -> Unit,
    isFavorite: Boolean,
    isUsingTTS: Boolean,
    shape: Shape = CircleShape,
    containerColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onSecondaryContainer,
) {
    val ttsClient = LocalTtsClient.current
    val dimensions = LocalDimensions.current
    var togglePositionX by remember { mutableFloatStateOf(Float.NaN) }

    val favoriteLabel = stringResource(
        id = if (isFavorite) R.string.content_desc_remove_from_favorites else R.string.content_desc_add_to_favorites,
    )
    val ttsLabel = stringResource(
        id = if (ttsClient?.isSpeaking == true) R.string.content_desc_stop_tts else R.string.content_desc_speak_tts,
    )
    val openInBrowserLabel = stringResource(id = R.string.content_desc_open_in_browser)
    val shareLabel = stringResource(id = R.string.content_desc_share)
    val showContentsLabel = stringResource(id = R.string.content_desc_show_contest)

    val favoriteInteractionSource = remember { MutableInteractionSource() }
    val ttsInteractionSource = remember { MutableInteractionSource() }
    val browserInteractionSource = remember { MutableInteractionSource() }
    val shareInteractionSource = remember { MutableInteractionSource() }
    val contentsInteractionSource = remember { MutableInteractionSource() }
    val toggleInteractionSource = remember { MutableInteractionSource() }
    val toggleLabel = stringResource(
        id = if (isExpanded) R.string.content_desc_hide_menu else R.string.content_desc_show_menu,
    )
    val itemCount = if (isUsingTTS) 5 else 4

    Box(
        modifier = modifier
            .navigationBarsPadding()
            .padding(horizontal = 12.dp)
            .padding(bottom = dimensions.bottomLinePadding)
            .fillMaxWidth(),
        contentAlignment = Alignment.CenterEnd,
    ) {
        ButtonGroup(
            overflowIndicator = { menuState ->
                if (isExpanded) {
                    ButtonGroupDefaults.OverflowIndicator(
                        menuState = menuState,
                        shape = shape,
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = containerColor,
                            contentColor = contentColor,
                        ),
                    )
                } else {
                    // Preserve overflow measurement while actions are visually collapsed.
                    Spacer(modifier = Modifier.size(56.dp))
                }
            },
            // Keep a dedicated slot for the persistent expand/collapse control.
            modifier = Modifier
                .padding(end = 64.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            customItem(
                buttonGroupContent = {
                    StaggeredPostActionButton(
                        modifier = Modifier
                            .animateWidth(interactionSource = favoriteInteractionSource)
                            .weight(weight = if (!isFavorite) NOT_FAVORITE_WEIGHT else DEFAULT_WEIGHT),
                        iconRes = if (isFavorite) R.drawable.ic_favorite_filled else R.drawable.ic_favorite_outlined,
                        label = favoriteLabel,
                        onClick = onToggleFavorite,
                        shape = shape,
                        containerColor = containerColor,
                        contentColor = if (isFavorite) colorFavorited else contentColor,
                        interactionSource = favoriteInteractionSource,
                        isGroupExpanded = isExpanded,
                        togglePositionX = togglePositionX,
                        index = 0,
                        itemCount = itemCount,
                    )
                },
                menuContent = { menuState ->
                    PostActionMenuItem(
                        menuState = menuState,
                        iconRes = if (isFavorite) R.drawable.ic_favorite_filled else R.drawable.ic_favorite_outlined,
                        label = favoriteLabel,
                        onClick = onToggleFavorite,
                    )
                },
            )

            if (isUsingTTS) {
                customItem(
                    buttonGroupContent = {
                        StaggeredPostActionButton(
                            modifier = Modifier
                                .animateWidth(interactionSource = ttsInteractionSource)
                                .weight(weight = DEFAULT_WEIGHT),
                            iconRes = R.drawable.ic_tts,
                            label = ttsLabel,
                            enabled = ttsClient?.isInitialized == true,
                            onClick = {
                                if (ttsClient?.isSpeaking == true) {
                                    ttsClient.stop()
                                } else {
                                    ttsClient?.speak(state = ttsState)
                                }
                            },
                            shape = shape,
                            containerColor = containerColor,
                            contentColor = contentColor,
                            interactionSource = ttsInteractionSource,
                            isGroupExpanded = isExpanded,
                            togglePositionX = togglePositionX,
                            index = 1,
                            itemCount = itemCount,
                        )
                    },
                    menuContent = { menuState ->
                        PostActionMenuItem(
                            menuState = menuState,
                            iconRes = R.drawable.ic_tts,
                            label = ttsLabel,
                            enabled = ttsClient?.isInitialized == true,
                            onClick = {
                                if (ttsClient?.isSpeaking == true) {
                                    ttsClient.stop()
                                } else {
                                    ttsClient?.speak(state = ttsState)
                                }
                            },
                        )
                    },
                )
            }

            customItem(
                buttonGroupContent = {
                    StaggeredPostActionButton(
                        modifier = Modifier
                            .animateWidth(interactionSource = browserInteractionSource)
                            .weight(weight = MAJOR_ITEM_WEIGHT),
                        iconRes = R.drawable.ic_open_external,
                        label = openInBrowserLabel,
                        onClick = onOpenWeb,
                        shape = MaterialTheme.shapes.large,
                        containerColor = containerColor,
                        contentColor = contentColor,
                        interactionSource = browserInteractionSource,
                        isGroupExpanded = isExpanded,
                        togglePositionX = togglePositionX,
                        index = if (isUsingTTS) 2 else 1,
                        itemCount = itemCount,
                    )
                },
                menuContent = { menuState ->
                    PostActionMenuItem(
                        menuState,
                        R.drawable.ic_open_external,
                        openInBrowserLabel,
                        onOpenWeb,
                    )
                },
            )

            customItem(
                buttonGroupContent = {
                    StaggeredPostActionButton(
                        modifier = Modifier
                            .animateWidth(interactionSource = shareInteractionSource)
                            .weight(weight = DEFAULT_WEIGHT),
                        iconRes = R.drawable.ic_share,
                        label = shareLabel,
                        onClick = onShare,
                        shape = shape,
                        containerColor = containerColor,
                        contentColor = contentColor,
                        interactionSource = shareInteractionSource,
                        isGroupExpanded = isExpanded,
                        togglePositionX = togglePositionX,
                        index = if (isUsingTTS) 3 else 2,
                        itemCount = itemCount,
                    )
                },
                menuContent = { menuState ->
                    PostActionMenuItem(menuState, R.drawable.ic_share, shareLabel, onShare)
                },
            )

            customItem(
                buttonGroupContent = {
                    StaggeredPostActionButton(
                        modifier = Modifier
                            .animateWidth(interactionSource = contentsInteractionSource)
                            .weight(weight = DEFAULT_WEIGHT),
                        iconRes = R.drawable.ic_content,
                        label = showContentsLabel,
                        onClick = onShowContest,
                        shape = shape,
                        containerColor = containerColor,
                        contentColor = contentColor,
                        interactionSource = contentsInteractionSource,
                        isGroupExpanded = isExpanded,
                        togglePositionX = togglePositionX,
                        index = if (isUsingTTS) 4 else 3,
                        itemCount = itemCount,
                    )
                },
                menuContent = { menuState ->
                    PostActionMenuItem(
                        menuState = menuState,
                        iconRes = R.drawable.ic_content,
                        label = showContentsLabel,
                        onClick = onShowContest,
                    )
                },
            )

        }

        PostActionButton(
            modifier = Modifier.onGloballyPositioned { coordinates ->
                togglePositionX = coordinates.positionInRoot().x
            },
            iconRes = R.drawable.ic_content,
            label = toggleLabel,
            onClick = { onExpandedChange(!isExpanded) },
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            interactionSource = toggleInteractionSource,
            shape = MaterialTheme.shapes.large,
        )
    }
}

@Composable
private fun StaggeredPostActionButton(
    modifier: Modifier,
    @DrawableRes iconRes: Int,
    label: String,
    onClick: () -> Unit,
    containerColor: Color,
    contentColor: Color,
    interactionSource: MutableInteractionSource,
    isGroupExpanded: Boolean,
    togglePositionX: Float,
    index: Int,
    itemCount: Int,
    enabled: Boolean = true,
    shape: Shape = MaterialTheme.shapes.extraLarge,
) {
    var itemPositionX by remember { mutableFloatStateOf(Float.NaN) }
    var isVisible by remember { mutableStateOf(false) }
    val canAnimate = togglePositionX.isFinite() && itemPositionX.isFinite()
    val delayMillis = if (isGroupExpanded) (itemCount - index - 1) * 80 else index * 50

    LaunchedEffect(isGroupExpanded, canAnimate, delayMillis) {
        if (isGroupExpanded && canAnimate) {
            delay(timeMillis = delayMillis.toLong())
            isVisible = true
        }
    }

    val progress by animateFloatAsState(
        targetValue = if (isGroupExpanded && canAnimate) 1f else 0f,
        animationSpec = tween(durationMillis = 360 + index * 30, delayMillis = delayMillis),
        label = "Post action $index expansion",
        finishedListener = {
            if (!isGroupExpanded) {
                isVisible = false
            }
        },
    )
    val offsetToToggle = if (canAnimate) togglePositionX - itemPositionX else 0f

    PostActionButton(
        modifier = modifier
            .onGloballyPositioned { coordinates ->
                itemPositionX = coordinates.positionInRoot().x
            }
            .graphicsLayer {
                alpha = if (isVisible) 1f else 0f
                translationX = offsetToToggle * (1f - progress)
            },
        iconRes = iconRes,
        label = label,
        // Keep Material colors stable while collapse is in progress, but ignore hidden actions.
        onClick = {
            if (isGroupExpanded) {
                onClick()
            }
        },
        containerColor = containerColor,
        contentColor = contentColor,
        interactionSource = interactionSource,
        enabled = enabled,
        shape = shape,
    )
}

@Composable
private fun PostActionButton(
    modifier: Modifier,
    @DrawableRes iconRes: Int,
    label: String,
    onClick: () -> Unit,
    containerColor: Color,
    contentColor: Color,
    interactionSource: MutableInteractionSource,
    enabled: Boolean = true,
    shape: Shape = MaterialTheme.shapes.extraLarge,

    ) {
    FilledTonalIconButton(
        onClick = onClick,
        modifier = modifier.defaultMinSize(minHeight = 56.dp, minWidth = 56.dp),
        enabled = enabled,
        shape = shape,
        colors = IconButtonDefaults.filledTonalIconButtonColors(
            containerColor = containerColor,
            contentColor = contentColor,
        ),
        interactionSource = interactionSource,
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = label,
        )
    }
}

@Composable
private fun PostActionMenuItem(
    menuState: ButtonGroupMenuState,
    @DrawableRes iconRes: Int,
    label: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
) {
    DropdownMenuItem(
        text = { Text(text = label) },
        leadingIcon = {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
            )
        },
        onClick = {
            onClick()
            menuState.dismiss()
        },
        enabled = enabled,
    )
}

@Composable
@PreviewLightDark
private fun PostBottomBarPreview() {
    DevBlogAppTheme {
        Box(modifier = Modifier.fillMaxWidth()) {
            PostBottomBar(
                isExpanded = true,
                onExpandedChange = { _ -> },
                ttsState = rememberTtsState(),
                onToggleFavorite = {},
                onShowContest = {},
                onOpenWeb = {},
                onShare = {},
                isFavorite = false,
                isUsingTTS = true,
            )
        }
    }
}

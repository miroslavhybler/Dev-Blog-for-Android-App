@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)
@file:Suppress("RemoveRedundantQualifierName")

package com.jet.article.example.devblog.ui.post

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jet.article.example.devblog.ui.LocalDimensions
import com.jet.article.example.devblog.R
import com.jet.article.example.devblog.ui.DevBlogAppTheme
import com.jet.article.example.devblog.ui.LocalTtsClient
import com.jet.article.example.devblog.ui.colorFavorited
import com.jet.tts.TtsState
import com.jet.tts.rememberTtsState
import com.jet.article.example.devblog.composables.SnackbarData
import com.jet.article.example.devblog.composables.SnackbarState
import com.jet.article.example.devblog.composables.rememberSnackbarState
import kotlinx.coroutines.delay

private val ITEM_SIZE: Dp = 76.dp
private val ITEM_PADDING: Dp = 18.dp
private val TOGGLE_ITEM_PADDING: Dp = 14.dp
private const val PRESSED_ANIM_SCALE: Float = 1.5f


/**
 * @author Miroslav Hýbler <br>
 * created on 29.04.2025
 */
@Composable
fun PostBottomBar(
    modifier: Modifier = Modifier,
    ttsState: TtsState,
    onToggleFavorite: () -> Unit,
    onShowContest: () -> Unit,
    onOpenWeb: () -> Unit,
    onShare: () -> Unit,
    isFavorite: Boolean,
    isUsingTTS: Boolean,
    snackbarState: SnackbarState,
    shape: Shape = MaterialTheme.shapes.extraLarge,
    containerColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onSecondaryContainer,
) {
    val isInspection = LocalInspectionMode.current
    val ttsClient = LocalTtsClient.current
    val dimensions = LocalDimensions.current

    var isExpanded by rememberSaveable { mutableStateOf(value = isInspection) }

    Box(
        modifier = Modifier
            .navigationBarsPadding()
            .padding(bottom = dimensions.bottomLinePadding)
            .padding(horizontal = 12.dp) //Not using horizontalPadding() because its too large
            .wrapContentWidth()
            .height(height = ITEM_SIZE)
            .clip(shape = shape)
            .then(other = modifier),
    ) {
        AnimatedVisibility(
            modifier = Modifier.fillMaxWidth(),
            visible = isExpanded,
            enter = slideInHorizontally(
                initialOffsetX = { fullWidth -> fullWidth }
            ) + expandHorizontally(expandFrom = Alignment.End),
            exit = slideOutHorizontally(
                targetOffsetX = { fullWidth -> fullWidth },
            ) + shrinkHorizontally(shrinkTowards = Alignment.End)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = containerColor, shape = shape)
                    .horizontalScroll(state = rememberScrollState()),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Item(
                    iconRes = if (isFavorite)
                        R.drawable.ic_favorite_filled
                    else
                        R.drawable.ic_favorite_outlined,
                    onClick = onToggleFavorite,
                    tint = if (isFavorite) colorFavorited else contentColor,
                    label = stringResource(
                        id = if (isFavorite)
                            R.string.content_desc_add_to_favorites
                        else
                            R.string.content_desc_remove_from_favorites,
                    ),
                    snackbarState = snackbarState,
                    shape = shape,
                )

                if (isUsingTTS) {

                    ItemDivider()

                    Item(
                        iconRes = R.drawable.ic_tts,
                        isReady = ttsClient?.isInitialized == true,
                        onClick = {
                            if (ttsClient?.isSpeaking == true) {
                                ttsClient.stop()
                            } else {
                                ttsClient?.speak(state = ttsState)
                            }
                        },
                        tint = contentColor,
                        snackbarState = snackbarState,
                        label = stringResource(
                            id = if (ttsClient?.isSpeaking == true)
                                R.string.content_desc_speak_tts
                            else
                                R.string.content_desc_stop_tts,
                        ),
                        shape = shape,
                    )
                }

                ItemDivider()

                Item(
                    iconRes = R.drawable.ic_open_external,
                    isReady = true,
                    onClick = onOpenWeb,
                    tint = contentColor,
                    snackbarState = snackbarState,
                    label = stringResource(id = R.string.content_desc_open_in_browser),
                    shape = shape,
                )

                ItemDivider()

                Item(
                    iconRes = R.drawable.ic_share,
                    onClick = onShare,
                    tint = contentColor,
                    snackbarState = snackbarState,
                    label = stringResource(id = R.string.content_desc_share),
                    shape = shape,
                )

                ItemDivider()

                Item(
                    iconRes = R.drawable.ic_content,
                    onClick = onShowContest,
                    tint = contentColor,
                    snackbarState = snackbarState,
                    label = stringResource(id = R.string.content_desc_show_contest),
                    shape = shape,
                )


                Spacer(
                    modifier = Modifier
                        .size(size = ITEM_SIZE),
                )
            }
        }


        ToggleItem(
            modifier = Modifier
                .align(alignment = Alignment.CenterEnd),
            iconRes = R.drawable.ic_content,
            onClick = { isExpanded = !isExpanded },
            shape = shape,
            label = stringResource(id = R.string.content_desc_show_menu),
        )

    }
}


@Composable
private fun ItemDivider(
    modifier: Modifier = Modifier,
) {
    VerticalDivider(
        modifier = modifier.padding(vertical = ITEM_PADDING),
        thickness = 2.dp,
        color = MaterialTheme.colorScheme.outline,
    )
}


@Composable
private fun RowScope.Item(
    modifier: Modifier = Modifier,
    @DrawableRes iconRes: Int,
    onClick: () -> Unit,
    tint: Color,
    label: String,
    snackbarState: SnackbarState,
    isEnabled: Boolean = true,
    isReady: Boolean = true,
    useWeight: Boolean = true,
    shape: Shape,
) {

    val indication = LocalIndication.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    var isClicked by remember { mutableStateOf(value = false) }

    // Animate horizontal scale when pressed
    val scale by animateFloatAsState(
        targetValue = if (isPressed || isClicked) PRESSED_ANIM_SCALE else 1f,
        label = "PressScale",
    )
    val weight by animateFloatAsState(
        targetValue = if (isPressed || isClicked) PRESSED_ANIM_SCALE else 1f,
        label = "WeightAnim"
    )

    LaunchedEffect(key1 = interactionSource) {
        interactionSource.interactions.collect { interaction ->
            if (interaction is PressInteraction.Release) {
                //Using Release Interaction as click indicator to animate scale and weight
                isClicked = true
                delay(timeMillis = 250)
                isClicked = false
            }
        }
    }
    Box(
        modifier = modifier
            .size(size = ITEM_SIZE)
            .weight(weight = if (useWeight) weight else 1f)
            .clip(shape = shape)
            .combinedClickable(
                interactionSource = interactionSource,
                indication = indication,
                enabled = isEnabled,
                onClickLabel = label,
                role = Role.Button,
                onClick = onClick,
                onLongClick = {
                    snackbarState.data = SnackbarData(
                        message = label,
                    )
                },
                onLongClickLabel = label,
            )
            .graphicsLayer(
                block = {
                    scaleY = scale
                    scaleX = scale
                }
            )
            .semantics(
                properties = {
                    role = Role.Button
                    contentDescription = label
                }
            ),
        contentAlignment = Alignment.Center,
    ) {


        androidx.compose.animation.AnimatedVisibility(
            modifier = Modifier
                .align(alignment = Alignment.Center)
                .matchParentSize(),
            visible = !isReady,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(size = 24.dp),
                color = tint,
            )
        }

        androidx.compose.animation.AnimatedVisibility(
            modifier = Modifier
                .align(alignment = Alignment.Center)
                .matchParentSize(),
            visible = isReady,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            Icon(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(all = ITEM_PADDING),
                painter = painterResource(id = iconRes),
                contentDescription = label,
                tint = tint,
            )
        }
    }
}


@Composable
private fun ToggleItem(
    modifier: Modifier = Modifier,
    @DrawableRes iconRes: Int,
    onClick: () -> Unit,
    shape: Shape,
    label: String,
) {

    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .size(size = ITEM_SIZE)
            .background(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = shape
            )
            .clip(shape = shape)
            .clickable(
                interactionSource = interactionSource,
                onClickLabel = label,
                onClick = onClick,
            )
            .semantics(
                properties = {
                    role = Role.Button
                    contentDescription = label
                }
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            modifier = Modifier
                .align(alignment = Alignment.Center)
                .matchParentSize()
                .padding(all = TOGGLE_ITEM_PADDING),
            painter = painterResource(id = iconRes),
            contentDescription = label,
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
}


@Composable
@PreviewLightDark
private fun PostBottomBarPreview() {
    DevBlogAppTheme {
        Box(modifier = Modifier.fillMaxWidth()) {
            PostBottomBar(
                ttsState = rememberTtsState(),
                onToggleFavorite = {},
                onShowContest = {},
                isFavorite = false,
                isUsingTTS = true,
                onOpenWeb = {},
                onShare = {},
                snackbarState = rememberSnackbarState(),
            )
        }
    }
}
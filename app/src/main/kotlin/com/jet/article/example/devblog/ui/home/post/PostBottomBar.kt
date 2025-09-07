package com.jet.article.example.devblog.ui.home.post

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jet.article.example.devblog.ui.LocalDimensions
import com.jet.article.example.devblog.R
import com.jet.article.example.devblog.ui.LocalTtsClient
import com.jet.article.example.devblog.ui.colorFavorited
import com.jet.tts.TtsState

private val ITEM_SIZE: Dp = 48.dp


/**
 * @author Miroslav Hýbler <br>
 * created on 29.04.2025
 */
@Composable
fun PostBottomBar(
    modifier: Modifier = Modifier,
    ttsState: TtsState,
    onToggleFavorite: () -> Unit,
    isFavorite: Boolean,
    isUsingTTS: Boolean,
) {
    val ttsClient = LocalTtsClient.current
    val dimensions = LocalDimensions.current

    BottomAppBar(
        modifier = Modifier
            .background(color = MaterialTheme.colorScheme.background)
            .navigationBarsPadding()
            .padding(bottom = dimensions.bottomLinePadding)
            .then(other = modifier),
        actions = {
            Item(
                iconRes = if (isFavorite)
                    R.drawable.ic_favorite_filled
                else
                    R.drawable.ic_favorite_outlined,
                onClick = onToggleFavorite,
                tint = if (isFavorite)
                    colorFavorited
                else
                    MaterialTheme.colorScheme.onBackground,
            )

            if (isUsingTTS) {
                Item(
                    iconRes = R.drawable.ic_tts,
                    isReady = ttsClient.isInitialized,
                    onClick = {
                        if (ttsClient.isSpeaking) {
                            ttsClient.stop()
                        } else {
                            ttsClient.speak(state = ttsState)
                        }
                    },
                )
            }
        },
    )
}


@Composable
private fun Item(
    modifier: Modifier = Modifier,
    @DrawableRes iconRes: Int,
    onClick: () -> Unit,
    label: String = "",
    isEnabled: Boolean = true,
    isReady: Boolean = true,
    tint: Color = MaterialTheme.colorScheme.onBackground,
) {


    Box(
        modifier = modifier
            .padding(horizontal = 8.dp)
            .size(size = ITEM_SIZE)
            .clickable(onClick = onClick, enabled = isEnabled),
        contentAlignment = Alignment.Center,
    ) {


        AnimatedVisibility(visible = !isReady) {
            CircularProgressIndicator()
        }

        AnimatedVisibility(visible = isReady) {
            IconButton(
                modifier = Modifier.matchParentSize(),
                onClick = onClick,
                enabled = isEnabled,
            ) {
                Icon(
                    modifier = Modifier
                        .size(size = 42.dp)
                        .padding(all = 4.dp),
                    painter = painterResource(id = iconRes),
                    contentDescription = label,
                    tint = tint,
                )
            }
        }
    }
}
@file:OptIn(ExperimentalSharedTransitionApi::class)

package com.jet.article.example.devblog.ui.home.post

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import com.jet.article.example.devblog.composables.CustomHtmlImage
import kotlin.math.abs


/**
 * @author Miroslav Hýbler <br>
 * created on 12.01.2025
 */
@Composable
fun ImageDetailLayout(
    modifier: Modifier = Modifier,
    url: String?,
) {

    var containerSize by remember { mutableStateOf(value = IntSize.Zero) }
    var contentSize by remember { mutableStateOf(value = IntSize.Zero) }

    var scale by rememberSaveable { mutableFloatStateOf(value = 1f) }
    var offsetX by remember { mutableFloatStateOf(value = 0f) }
    var offsetY by remember { mutableFloatStateOf(value = 0f) }

    val transformableState = rememberTransformableState { zoomChange, offsetChange, _ ->
        scale = (scale * zoomChange)
            .coerceIn(
                minimumValue = 1f,
                maximumValue = 3f,
            )

        val xDiff = ((contentSize.width * scale) - containerSize.width) * 0.5f
        val yDiff = ((contentSize.height * scale) - containerSize.height) * 0.5f

        offsetX = (offsetX + (offsetChange.x * scale))
            .coerceIn(
                minimumValue = if (xDiff > 0) xDiff * -1f else xDiff,
                maximumValue = abs(x = xDiff),
            )

        offsetY = (offsetY + (offsetChange.y * scale))
            .coerceIn(
                minimumValue = if (yDiff > 0) yDiff * -1f else yDiff,
                maximumValue = abs(x = yDiff),
            )
    }



    Box(
        modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background)
            .fillMaxSize()
            .onSizeChanged { newSize ->
                containerSize = newSize
            }
    ) {
        if (url != null) {
            CustomHtmlImage(
                modifier = Modifier
                    .align(alignment = Alignment.Center)
                    .graphicsLayer(
                        block = {
                            this.scaleX = scale
                            this.scaleY = scale
                            this.translationX = offsetX
                            this.translationY = offsetY
                        },
                    )
                    .transformable(state = transformableState)
                    .onSizeChanged { newSize ->
                        contentSize = newSize
                    },
                url = url,
            )
        }
    }
}
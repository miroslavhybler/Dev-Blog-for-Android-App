@file:Suppress(
    "DATA_CLASS_COPY_VISIBILITY_WILL_BE_CHANGED_WARNING",
    "DataClassPrivateConstructor",
)

package com.jet.article.example.devblog.composables

import androidx.annotation.Keep
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.jet.article.example.devblog.horizontalPadding
import com.jet.article.example.devblog.ui.DevBlogAppTheme
import com.jet.article.example.devblog.ui.LocalDimensions
import kotlinx.coroutines.delay


/**
 * @author Miroslav Hýbler <br>
 * created on 14.09.2025
 */

private const val animDuration: Long = 300
private const val visibleDuration: Long = 2500

@Keep
@Immutable
data class SnackbarData constructor(
    val message: String,
) {

}


class SnackbarState constructor() {

    var data: SnackbarData? by mutableStateOf(value = null)
    var isVisible by mutableStateOf(value = false)

}

@Composable
fun rememberSnackbarState(): SnackbarState {
    return remember { SnackbarState() }
}


@Composable
fun MessageSnackbar(
    modifier: Modifier = Modifier,
    state: SnackbarState,
    contentColor: Color = MaterialTheme.colorScheme.onBackground,
    containerColor: Color = MaterialTheme.colorScheme.background,
) {
    val dimensions = LocalDimensions.current

    LaunchedEffect(
        key1 = state.data,
    ) {
        if (state.data == null) {
            return@LaunchedEffect
        }

        state.isVisible = true
        delay(timeMillis = visibleDuration)
        state.isVisible = false
        delay(timeMillis = animDuration)
        state.data = null
    }

    AnimatedVisibility(
        modifier = modifier
            .horizontalPadding()
            .statusBarsPadding()
            .padding(
                top = dimensions.topLinePadding,
                bottom = 8.dp,
            )
            .fillMaxWidth()
            .wrapContentHeight(),
        visible = state.isVisible && state.data != null,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
    ) {
        state.data?.let {
            SnackbarImpl(
                state = state,
                data = it,
                contentColor = contentColor,
                containerColor = containerColor,
            )
        }
    }
}


@Composable
private fun SnackbarImpl(
    state: SnackbarState,
    data: SnackbarData,
    contentColor: Color,
    containerColor: Color,
    shape: Shape = RoundedCornerShape(size = 16.dp),
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .clip(shape = shape)
            .background(
                color = containerColor,
                shape = shape,
            )
            .clickable(
                onClick = {
                    state.isVisible = false
                    state.data = null
                }
            )
            .padding(
                horizontal = 12.dp,
                vertical = 20.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {

        Text(
            text = data.message,
            style = MaterialTheme.typography.bodyMedium,
            color = contentColor,
        )
    }
}


@Composable
@PreviewLightDark
private fun SnackbarPreview() {
    DevBlogAppTheme {
        MessageSnackbar(
            state = remember {
                SnackbarState().also {
                    it.data = SnackbarData(message = "Hello Snackbar!")
                }
            }
        )
    }
}
package com.jet.article.example.devblog.composables

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.jet.article.example.devblog.R
import com.jet.article.example.devblog.ui.DevBlogAppTheme
import com.jet.utils.dpToPx
import kotlin.math.cos
import kotlin.math.sin


/**
 * Logo of Android holding a wrench.
 * @author Miroslav Hýbler <br>
 * created on 19.08.2024
 */
@Composable
fun Android(
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    Box(
        modifier = modifier
            .wrapContentSize()
            .clipToBounds()
            .padding(horizontal = 32.dp)
    ) {
        Icon(
            modifier = Modifier
                .wrapContentSize()
                .scale(scale = 1.3f),
            painter = painterResource(id = R.drawable.android_robot),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
        )
        Icon(
            modifier = Modifier
                .align(alignment = Alignment.BottomEnd)
                .scale(scale = 1.5f)
                .graphicsLayer(
                    rotationZ = 75f,
                    translationX = density.dpToPx(dp = 16.dp),
                    translationY = density.dpToPx(dp = 2.dp)
                ),
            painter = painterResource(id = R.drawable.ic_wrench),
            tint = MaterialTheme.colorScheme.secondary,
            contentDescription = null,
        )
    }
}


@Composable
fun EmptyAnimation(
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    val wheel1Path = remember { Path() }
    val wheel2Path = remember { Path() }
    val wheel3Path = remember { Path() }

    var wheel1Center by remember { mutableStateOf(value = Offset.Zero) }
    var wheel2Center by remember { mutableStateOf(value = Offset.Zero) }
    var wheel3Center by remember { mutableStateOf(value = Offset.Zero) }


    val infiniteTransition = rememberInfiniteTransition(label = "developer animation")
    val rotation1 by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wheel 1 rotation",
    )


    val rotation2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 12_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wheel 2 rotation",
    )


    val rotation3 by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6_500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "wheel 3 rotation"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .sizeIn(
                minWidth = 256.dp,
                maxWidth = 700.dp,
                minHeight = 128.dp,
            )
            .background(color = MaterialTheme.colorScheme.background)
    ) {
        if (wheel2Center == Offset.Zero) {
            wheel2Center = Offset(x = size.width * 0.5f, y = size.height * 0.35f)
        }
        withTransform(
            transformBlock = {
                rotate(degrees = rotation2, pivot = wheel2Center)
            }
        ) {
            drawSprocketWheel(
                color = colorScheme.primary,
                center = wheel2Center,
                radius = 56.dp.toPx(),
                path = wheel2Path,
                segments = 16,
                offsetMultiplier = 1.35f,
                style = Fill,
            )
        }


        if (wheel1Center == Offset.Zero) {
            wheel1Center = Offset(x = size.width * 0.33f, y = size.height * 0.7f)
        }
        withTransform(
            transformBlock = {
                rotate(degrees = rotation1, pivot = wheel1Center)
            }
        ) {
            drawSprocketWheel(
                color = colorScheme.secondary,
                radius = 32.dp.toPx(),
                center = wheel1Center,
                path = wheel1Path,
                style = Fill,
            )
        }

        if (wheel3Center == Offset.Zero) {
            wheel3Center = Offset(x = size.width * 0.71f, y = size.height * 0.42f)
        }
        withTransform(
            transformBlock = {
                rotate(degrees = rotation3, pivot = wheel3Center)
            }
        ) {
            drawSprocketWheel(
                color = colorScheme.secondary,
                center = wheel3Center,
                radius = 42.dp.toPx(),
                path = wheel3Path,
                segments = 16,
                offsetMultiplier = 1.35f,
                style = Stroke(width = 8.dp.toPx())
            )
        }
    }
}


private fun DrawScope.drawSprocketWheel(
    color: Color,
    path: Path,
    center: Offset,
    radius: Float,
    segments: Int = 12,
    offsetMultiplier: Float = 0.8f,
    style: DrawStyle = Stroke(width = 5.dp.toPx())
) {
    drawSprocketWheel(
        color = color,
        path = path,
        centerX = center.x,
        centerY = center.y,
        radius = radius,
        segments = segments,
        offsetMultiplier = offsetMultiplier,
        style = style
    )
}


/**
 * @param color
 * @param path
 * @param centerX
 * @param centerY
 * @param radius
 * @param segments
 * @param offsetMultiplier
 */
private fun DrawScope.drawSprocketWheel(
    color: Color,
    path: Path,
    centerX: Float,
    centerY: Float,
    radius: Float,
    segments: Int = 12,
    offsetMultiplier: Float = 0.8f,
    style: DrawStyle = Stroke(width = 5.dp.toPx())
) {
    path.reset()
    var lastX = 0f
    var lastY = 0f

    for (i in 0 until segments) {
        val angle = Math.toRadians(i * 360.0 / (segments - 1)).toFloat()
        val x = centerX + radius * cos(x = angle)
        val y = centerY + radius * sin(x = angle)

        if (i == 0) {
            path.moveTo(x = x, y = y)
        } else {
            val midX = (lastX + x) / 2
            val midY = (lastY + y) / 2
            val directionX = x - lastX
            val directionY = y - lastY
            val controlX = midX + directionY * offsetMultiplier
            val controlY = midY + (-directionX) * offsetMultiplier

            path.quadraticTo(
                x1 = controlX,
                y1 = controlY,
                x2 = x,
                y2 = y,
            )
        }
        lastX = x
        lastY = y
    }
    path.close()

    drawPath(
        path = path,
        color = color,
        style = style,
    )
}


@Composable
@PreviewLightDark
private fun DevelopmentAnimationPreview() {
    DevBlogAppTheme {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            EmptyAnimation()

            Android()
        }
    }
}
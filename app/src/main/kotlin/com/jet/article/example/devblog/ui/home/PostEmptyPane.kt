package com.jet.article.example.devblog.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.jet.article.example.devblog.R
import com.jet.article.example.devblog.composables.Android
import com.jet.article.example.devblog.composables.EmptyAnimation
import com.jet.article.example.devblog.horizontalPadding
import com.jet.article.example.devblog.ui.DevBlogAppTheme
import com.jet.utils.dpToPx


/**
 * Empty screen for large screens, visible when no post is actually selected.
 * @author Miroslav Hýbler <br>
 * created on 19.08.2024
 */
@Composable
fun PostEmptyPane() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        EmptyAnimation(modifier = Modifier)

        Spacer(modifier = Modifier.height(height = 8.dp))

        Android()

        Text(
            text = stringResource(id = R.string.app_name),
            style = MaterialTheme.typography.headlineMedium,
            maxLines = 1,
        )

        Text(
            modifier = Modifier
                .horizontalPadding(),
            text = stringResource(R.string.post_empty_desc),
            style = MaterialTheme.typography.labelMedium,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
@PreviewLightDark
private fun PostEmptyPanePreview() {
    DevBlogAppTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            PostEmptyPane()
        }
    }
}
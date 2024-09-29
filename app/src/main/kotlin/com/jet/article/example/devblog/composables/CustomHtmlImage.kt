package com.jet.article.example.devblog.composables

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jet.article.data.HtmlElement
import com.jet.article.ui.elements.HtmlImage
import com.jet.article.ui.elements.HtmlImageDefaults

/**
 * @author Miroslav Hýbler <br>
 * created on 26.08.2024
 */
@Composable
fun CustomHtmlImage(
    modifier: Modifier = Modifier,
    image: HtmlElement.Image,
) {
    HtmlImage(
        modifier = modifier.animateContentSize(),
        data = image,
        loading = {
            CustomHtmlImageDefaults.Loading()
        },
        error = { HtmlImageDefaults.ErrorLayout() }
    )
}

@Composable
fun CustomHtmlImage(
    modifier: Modifier = Modifier,
    url: String,
) {
    HtmlImage(
        modifier = modifier.animateContentSize(),
        url = url,
        loading = {
            CustomHtmlImageDefaults.Loading()
        },
        error = { HtmlImageDefaults.ErrorLayout() }
    )
}


object CustomHtmlImageDefaults {

    @Composable
    fun Loading() {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height = 164.dp)
                .background(
                    color = MaterialTheme.colorScheme.outline,
                    shape = MaterialTheme.shapes.medium,
                )
        )
    }
}
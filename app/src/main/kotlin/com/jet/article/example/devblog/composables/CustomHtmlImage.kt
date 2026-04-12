package com.jet.article.example.devblog.composables

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jet.article.core.ArticleElement
import com.jet.article.ui.elements.HtmlImageDefaults
import com.jet.article.ui.elements.ImageElement

/**
 * @author Miroslav Hýbler <br>
 * created on 26.08.2024
 */
@Composable
fun CustomHtmlImage(
    modifier: Modifier = Modifier,
    image: ArticleElement.Image,
) {
    ImageElement(
        modifier = modifier.animateContentSize(),
        image = image,
        loading = {
            CustomHtmlImageDefaults.Loading()
        },
        error = { HtmlImageDefaults.ErrorLayout() },
    )
}

@Composable
fun CustomHtmlImage(
    modifier: Modifier = Modifier,
    url: String,
) {
    ImageElement(
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
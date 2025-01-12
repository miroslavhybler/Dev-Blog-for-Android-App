package com.jet.article.example.devblog.ui.home.post

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jet.article.ArticleParser
import com.jet.article.data.HtmlElement
import com.jet.article.example.devblog.R
import com.jet.article.example.devblog.composables.TitleTopBar
import com.jet.article.example.devblog.data.AdjustedPostData
import com.jet.article.example.devblog.horizontalPadding


/**
 * Extra pane, showing contents of the post based on the titles in the post.
 * @author Miroslav Hýbler <br>
 * created on 14.08.2024
 */
@Composable
fun ContentsPane(
    data: AdjustedPostData?,
    onSelected: (index: Int, element: HtmlElement.Title) -> Unit,
) {

    Scaffold(
        topBar = {
            TitleTopBar(text = stringResource(R.string.contents_title))
        },
        content = { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .padding(paddingValues = paddingValues)
            ) {
                itemsIndexed(items = data?.contest ?: emptyList()) { index, item ->
                    Text(
                        modifier = Modifier
                            .fillParentMaxWidth()
                            .clickable(
                                onClick = {
                                    onSelected(item.originalIndex, item.title)
                                }
                            )
                            .horizontalPadding()
                            .padding(vertical = 16.dp),
                        text = "${index + 1}. - ${
                            ArticleParser.Utils.clearTagsAndReplaceEntitiesFromText(
                                input = item.title.text
                            )
                        }",
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }

        }
    )
}

data class TitleWithOriginalIndex constructor(
    val title: HtmlElement.Title,
    val originalIndex: Int,
)
package com.jet.article.example.devblog.ui.post

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.jet.article.ArticleParser
import com.jet.article.data.HtmlElement
import com.jet.article.example.devblog.R
import com.jet.article.example.devblog.composables.TitleTopBar
import com.jet.article.example.devblog.horizontalPadding
import com.jet.article.example.devblog.ui.Route
import com.jet.article.example.devblog.ui.SectionSelectedEvent


/**
 * Extra pane, showing contents of the post based on the titles in the post.
 * @author Miroslav Hýbler <br>
 * created on 14.08.2024
 */
@Composable
fun ContentsScreen(
    route: Route.Contest,
    onSelected: (SectionSelectedEvent) -> Unit,
    viewModel: ContestViewModel = hiltViewModel(),
) {
    val postData by viewModel.postData.collectAsState()
    val data = postData?.getOrNull()

    LaunchedEffect(key1 = Unit) {
        viewModel.loadPostDetail(item = route.item)
    }

    Scaffold(
        topBar = {
            TitleTopBar(text = stringResource(id = R.string.contents_title))
        },
        content = { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .padding(paddingValues = paddingValues)
            ) {
                if (data?.contest != null && data.contest.isEmpty()) {
                    item(key = Int.MIN_VALUE) {

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(space = 4.dp),
                        ) {
                            Text(
                                text = "This article doesn't have sections",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onBackground,
                            )

                        }
                    }
                }

                itemsIndexed(
                    items = data?.contest ?: emptyList(),
                    key = { index, item -> item.title.key },
                ) { index, item ->
                    Text(
                        modifier = Modifier
                            .fillParentMaxWidth()
                            .clickable(
                                onClick = {
                                    onSelected(
                                        SectionSelectedEvent(
                                            index = item.originalIndex,
                                            element = item.title,
                                        )
                                    )
                                }
                            )
                            .horizontalPadding()
                            .padding(vertical = 16.dp),
                        text = "${index + 1}. - ${
                            ArticleParser.Utils.clearTagsAndReplaceEntitiesFromText(
                                input = item.title.text.toString(),
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
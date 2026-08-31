package com.jet.article.example.devblog.data

import android.content.Context
import android.util.Log
import androidx.compose.ui.text.AnnotatedString
import com.contentdive.api.AnchorRef
import com.contentdive.api.ContentDive
import com.contentdive.api.DestinationRef
import com.contentdive.api.SearchFragment
import com.contentdive.api.SearchFragmentId
import com.contentdive.api.SearchFragmentKind
import com.contentdive.api.SearchItem
import com.contentdive.api.SearchItemId
import com.contentdive.api.SearchProjection
import com.contentdive.api.SearchQuery
import com.contentdive.api.SearchScope
import com.contentdive.backend.appsearch.createAppSearchContentDive
import com.contentdive.compose.toSearchFragment
import com.jet.article.core.ArticleElement
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Maintains the derived search snapshot for post details the user has opened.
 *
 * The post repository remains authoritative; ContentDive stores only enough metadata for a later
 * search result to reopen the post and focus the matching visible block.
 */
@Singleton
class PostContentDiveIndexer @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val contentDive: ContentDive = createAppSearchContentDive(
        context = context,
        databaseName = DATABASE_NAME,
    )

    suspend fun index(post: AdjustedPostData) {
        runCatching {
            contentDive.replace(projection = projectionFor(post = post))
        }.onFailure { error ->
            // Search is an enhancement; a storage failure must not prevent viewing the post.
            Log.w(TAG, "Could not index opened post for search", error)
        }
    }

    /** Searches the visible text of posts that have previously been opened and indexed. */
    suspend fun search(query: String): List<IndexedPostSearchResult> {
        if (query.isBlank()) return emptyList()
        return runCatching {
            contentDive.search(
                query = SearchQuery(
                    text = query,
                    scopes = setOf(SearchScope(value = POST_SCOPE)),
                ),
            ).matches.mapNotNull { match ->
                match.destination.payload.takeIf {
                    match.destination.type == DESTINATION_TYPE
                }?.let { url ->
                    IndexedPostSearchResult(
                        postUrl = url,
                        snippet = match.snippet,
                    )
                }
            }
        }.getOrElse { error ->
            Log.w(TAG, "Could not search indexed posts", error)
            emptyList()
        }
    }

    private fun projectionFor(post: AdjustedPostData): SearchProjection {
        val scope = SearchScope(value = POST_SCOPE)
        val itemId = SearchItemId(value = "post:${post.postData.url}")
        val title = post.title?.text?.text?.takeIf(predicate = String::isNotBlank)
            ?: post.postData.url
        val item = SearchItem(
            id = itemId,
            scope = scope,
            title = title,
            destination = DestinationRef(
                type = DESTINATION_TYPE,
                version = DESTINATION_VERSION,
                payload = post.postData.url,
            ),
        )
        val fragments = buildList {
            post.title?.text?.let { titleText ->
                addFragment(
                    text = titleText,
                    fragmentId = "title",
                    itemId = itemId,
                    scope = scope,
                    kind = SearchFragmentKind.TITLE,
                    anchor = null,
                )
            }
            post.postData.elements.forEachIndexed { elementIndex, element ->
                element.visibleTextBlocks().forEachIndexed { blockIndex, text ->
                    addFragment(
                        text = text,
                        fragmentId = "block-$elementIndex-$blockIndex",
                        itemId = itemId,
                        scope = scope,
                        kind = if (element is ArticleElement.Text && element.isTitle) {
                            SearchFragmentKind.HEADING
                        } else {
                            SearchFragmentKind.BODY
                        },
                        anchor = AnchorRef(
                            type = ANCHOR_TYPE,
                            version = ANCHOR_VERSION,
                            payload = elementIndex.toString(),
                        ),
                    )
                }
            }
        }
        require(fragments.isNotEmpty()) { "Opened post has no visible text to index" }
        return SearchProjection(item = item, fragments = fragments)
    }

    private fun MutableList<SearchFragment>.addFragment(
        text: AnnotatedString,
        fragmentId: String,
        itemId: SearchItemId,
        scope: SearchScope,
        kind: SearchFragmentKind,
        anchor: AnchorRef?,
    ) {
        if (text.text.isNotBlank()) {
            add(
                element = text.toSearchFragment(
                    id = SearchFragmentId(value = fragmentId),
                    itemId = itemId,
                    scope = scope,
                    kind = kind,
                    anchor = anchor,
                ),
            )
        }
    }

    private fun ArticleElement.visibleTextBlocks(): List<AnnotatedString> = when (this) {
        is ArticleElement.Text -> listOf(text)
        is ArticleElement.Quote -> listOf(text)
        is ArticleElement.ContentList -> items.flatMap { item ->
            item.flatMap { element -> element.visibleTextBlocks() }
        }

        else -> emptyList()
    }

    private companion object {
        const val TAG = "PostContentDiveIndexer"
        const val DATABASE_NAME = "dev-blog-post-content"
        const val POST_SCOPE = "android-dev-blog-posts"
        const val DESTINATION_TYPE = "post-url"
        const val DESTINATION_VERSION = 1
        const val ANCHOR_TYPE = "article-element-index"
        const val ANCHOR_VERSION = 1
    }
}

data class IndexedPostSearchResult constructor(
    val postUrl: String,
    val snippet: String,
)

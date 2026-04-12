@file:Suppress("RedundantVisibilityModifier")

package com.jet.article.example.devblog.data

import com.jet.article.core.ArticleData
import com.jet.article.core.ArticleElement
import com.jet.article.example.devblog.ui.post.TitleWithOriginalIndex


/**
 * @param headerImage Image used as background for TopBar in [com.jet.article.example.devblog.ui.post.PostScreen]
 * @param postData Adjusted post data to be shown as content
 * @param date Date of post
 * @param title Title of the post
 * @param contest List of titles of the content making contest of post
 * @author Miroslav Hýbler <br>
 * created on 23.08.2024
 */
public data class AdjustedPostData public constructor(
    val headerImage: ArticleElement.Image?,
    val postData: ArticleData,
    val date: SimpleDate?,
    val title: ArticleElement.Text?,
    val contest: List<TitleWithOriginalIndex>,
) {


}
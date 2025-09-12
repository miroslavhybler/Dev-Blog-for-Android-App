@file:Suppress("RedundantVisibilityModifier")

package com.jet.article.example.devblog.data

import com.jet.article.data.HtmlArticleData
import com.jet.article.data.HtmlElement
import com.jet.article.example.devblog.ui.home.post.TitleWithOriginalIndex


/**
 * @param headerImage Image used as background for TopBar in [com.jet.article.example.devblog.ui.home.post.PostScreen]
 * @param postData Adjusted post data to be shown as content
 * @param date Date of post
 * @param title Title of the post
 * @param contest List of titles of the content making contest of post
 * @author Miroslav Hýbler <br>
 * created on 23.08.2024
 */
public data class AdjustedPostData public constructor(
    val headerImage: HtmlElement.Image,
    val postData: HtmlArticleData,
    val date: SimpleDate,
    val title: HtmlElement.Title,
    val contest: List<TitleWithOriginalIndex>,
) {


}
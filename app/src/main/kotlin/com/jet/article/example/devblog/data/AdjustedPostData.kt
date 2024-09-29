package com.jet.article.example.devblog.data

import com.jet.article.data.HtmlArticleData
import com.jet.article.data.HtmlElement


/**
 * @author Miroslav Hýbler <br>
 * created on 23.08.2024
 */
data class AdjustedPostData constructor(
    val headerImage: HtmlElement.Image,
    val postData: HtmlArticleData,
    val date: SimpleDate,
    val title: HtmlElement.Title,
) {


}
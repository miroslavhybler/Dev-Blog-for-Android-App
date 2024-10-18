@file:Suppress("ConstPropertyName")

package com.jet.article.example.devblog.shared


/**
 * Holds constant names of sections for tracing and test tags
 * @author Miroslav Hýbler <br>
 * created on 15.10.2024
 */
data object Tracing {


    /**
     * Section names to be used with `trace(sectionName)`
     */
    data object Section {
        const val homeListPane: String = "HomeListPane"
        const val homeListPaneContent: String = "HomeListPaneContent"
        const val postPane: String = "PostPane"
    }

    /**
     * Tags to be used with `Modifier.testTag(tag)`, used to access UI elements inside tests and
     * benchmarks.
     */
    data object Tag {
        const val homeListPane: String = "home_list_pane"
        const val posts: String = "posts"
        const val firstPostItem: String = "firstPostItem"
        const val jetHtmlArticle: String = "jet_html_article"
    }
}
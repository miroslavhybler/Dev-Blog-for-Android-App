package com.jet.article.example.devblog.data

/**
 * @author Miroslav Hýbler <br>
 * created on 09.08.2024
 */
public data class ExcludeOption public constructor(
    val tag: String = "",
    val clazz: String = "",
    val id: String = "",
    val keyword: String = "",
) {

    companion object {


        /**
         * List of options to exclude from the article of https://android-developers.googleblog.com/
         */
        val devBlogExcludeRules: List<ExcludeOption>
            get() = listOf(
                ExcludeOption(tag = "footer"),
                ExcludeOption(tag = "div", clazz = "adb-hero-area"),
                ExcludeOption(tag = "div", clazz = "dropdown-nav"),
                ExcludeOption(tag = "div", clazz = "popout-nav"),
                ExcludeOption(tag = "div", clazz = "adb-header"),
                ExcludeOption(tag = "div", clazz = "icon-sidebar"),
                ExcludeOption(tag = "div", clazz = "adb-footer-section"),
                ExcludeOption(tag = "div", clazz = "copy-tooltip"),
                ExcludeOption(tag=  "div", clazz = "blog-pager"),
                ExcludeOption(tag = "div", clazz = "blog-label-container"),
            )
    }
}
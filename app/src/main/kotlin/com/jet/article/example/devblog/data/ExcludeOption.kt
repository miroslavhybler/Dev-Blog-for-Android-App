package com.jet.article.example.devblog.data

/**
 * Specifion option to exclude certain elements of html code.
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


    /**
     * Filters html tag based on the exclude options.
     *
     * @param tag The html tag name.
     * @param attributes The map of html attributes.
     * @return `false` if all specified conditions in this [ExcludeOption] match the provided tag and attributes, `true` otherwise.
     */
    fun filter(tag: String, attributes: Map<String, String>): Boolean {
        val conditions = mutableListOf<Boolean>()

        if (this.tag.isNotEmpty()) {
            conditions.add(this.tag == tag)
        }

        if (this.clazz.isNotEmpty()) {
            val classAttr = attributes["class"]
            conditions.add(classAttr != null && classAttr.split(' ').contains(this.clazz))
        }

        if (this.id.isNotEmpty()) {
            val idAttr = attributes["id"]
            conditions.add(idAttr != null && idAttr == this.id)
        }

        if (this.keyword.isNotEmpty()) {
            conditions.add(attributes.values.any { it.contains(this.keyword) })
        }

        if (conditions.isEmpty()) {
            return true // No conditions specified, so don't exclude.
        }

        // Return false (exclude) only if all specified conditions are met.
        return !conditions.all { it }
    }
}

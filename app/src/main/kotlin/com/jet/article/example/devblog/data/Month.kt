package com.jet.article.example.devblog.data

import androidx.annotation.IntRange


/**
 * @author Miroslav Hýbler <br>
 * created on 12.09.2024
 */
enum class Month private constructor(
    @IntRange(from = 1, to = 12)
    val value: Int,
    val displayName: String,
) {
    JANUARY(value = 1, displayName = "January"),
    FEBRUARY(value = 2, displayName = "February"),
    MARCH(value = 3, displayName = "March"),
    APRIL(value = 4, displayName = "April"),
    MAY(value = 5, displayName = "May"),
    JUNE(value = 6, displayName = "June"),
    JULY(value = 7, displayName = "July"),
    AUGUST(value = 8, displayName = "August"),
    SEPTEMBER(value = 9, displayName = "September"),
    OCTOBER(value = 10, displayName = "October"),
    NOVEMBER(value = 11, displayName = "November"),
    DECEMBER(value = 12, displayName = "December");

}
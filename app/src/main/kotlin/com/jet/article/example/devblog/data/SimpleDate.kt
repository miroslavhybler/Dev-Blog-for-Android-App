package com.jet.article.example.devblog.data

import androidx.annotation.IntRange
import androidx.annotation.Keep
import kotlinx.serialization.Serializable
import java.util.Calendar
import java.util.Date


/**
 * @author Miroslav Hýbler <br>
 * created on 12.09.2024
 */
@Keep
@Serializable
data class SimpleDate constructor(
    val year: Int,
    val month: Month,
    @IntRange(from = 0, to = 31)
    val dayOfMonth: Int,
) : Comparable<SimpleDate> {

    private fun createCalendar(): Calendar = Calendar.getInstance().apply {
        set(Calendar.YEAR, year)
        set(Calendar.MONTH, month.value - 1)
        set(Calendar.DAY_OF_MONTH, dayOfMonth)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }


    /**
     * Timestamp in seconds
     */
    val timestamp: Int
        get() = (createCalendar().timeInMillis / 1000).toInt()


    fun getDateString(): String {
        return "$dayOfMonth ${month.displayName} $year"
    }


    fun toDate(): Date {
        return createCalendar().time
    }


    override fun compareTo(other: SimpleDate): Int {
        return when {
            this.year > other.year -> 1
            this.year < other.year -> -1
            else -> {
                when {
                    this.month > other.month -> 1
                    this.month < other.month -> -1
                    else -> {
                        when {
                            this.dayOfMonth > other.dayOfMonth -> 1
                            this.dayOfMonth < other.dayOfMonth -> -1
                            else -> 0
                        }
                    }
                }
            }
        }
    }
}

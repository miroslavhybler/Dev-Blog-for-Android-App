package com.jet.article.example.devblog.data

import android.icu.util.Calendar
import android.util.Log
import androidx.annotation.IntRange
import androidx.annotation.Keep
import java.time.LocalDate
import java.util.Date


/**
 * @author Miroslav Hýbler <br>
 * created on 12.09.2024
 */
@Keep
data class SimpleDate constructor(
    val year: Int,
    val month: Month,
    @IntRange(from = 0, to = 31)
    val dayOfMonth: Int,
) : Comparable<SimpleDate> {


    /**
     * Timestamp in seconds
     */
    val timestamp: Int
        get() {
            val calendar = Calendar.getInstance()
            calendar.set(year, month.value, dayOfMonth, 0, 0)
            return (calendar.timeInMillis / 1000).toInt()
        }


    fun getDateString(): String {
        return "$dayOfMonth ${month.displayName} $year"
    }


    fun toDate(): Date {
        val calendar = Calendar.getInstance()
        calendar.set(year, month.value - 1, dayOfMonth, 0, 0)
        val date = calendar.time
        return date
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
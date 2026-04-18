package com.jet.article.example.devblog.data

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar

class SimpleDateTest {

    @Test
    fun timestamp_usesSameMonthIndexingAsDateConversion() {
        val date = SimpleDate(
            year = 2025,
            month = Month.MARCH,
            dayOfMonth = 3,
        )

        val expectedTimestamp = Calendar.getInstance().run {
            set(2025, Calendar.MARCH, 3, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
            (timeInMillis / 1000).toInt()
        }

        assertEquals(expectedTimestamp, date.timestamp)
        assertEquals(expectedTimestamp, (date.toDate().time / 1000).toInt())
    }
}

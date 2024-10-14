@file:OptIn(ExperimentalMetricApi::class)

package com.jet.article.example.devblog.benchmark

import android.graphics.Point
import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.ExperimentalMetricApi
import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.TraceSectionMetric
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.Until
import org.junit.Test
import org.junit.runner.RunWith


/**
 * @author Miroslav Hýbler <br>
 * created on 09.10.2024
 */
@LargeTest
@RunWith(AndroidJUnit4::class)
class OpenPostBenchMark : BaseBenchMark() {


    /**
     * Benchmark test for this flow:
     * 1. Open Home screen and wait for load of posts
     * 2. Open first post on the list
     * 3. Load and show post detail
     * 4. Scroll (simulating reading of the post)
     */
    @Test
    fun openHomeAndOpenPost() = benchmark(
        metrics = listOf(
            StartupTimingMetric(),
            TraceSectionMetric(
                "PostPane",
                TraceSectionMetric.Mode.Sum,
            ),
        ),
        iterations = 1,
    ) {
        pressHome()
        startActivityAndWait()
        val firstPost = device.ensureObject(tag = "first_item")
        firstPost.click()
        val jetHtmlArticle = device.ensureObject(tag = "jet_html_article")
        repeat(2) {
            jetHtmlArticle.fling(
                Direction.DOWN,
                1024,
            )
            Thread.sleep(500)
        }
    }
}
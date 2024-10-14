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
import androidx.test.uiautomator.Until
import org.junit.Test
import org.junit.runner.RunWith


/**
 * @author Miroslav Hýbler <br>
 * created on 09.10.2024
 */
@LargeTest
@RunWith(AndroidJUnit4::class)
class HomeListBenchmark : BaseBenchMark() {

    @Test
    fun openHomeAndScrollList() = benchmarkRule.measureRepeated(
        packageName = "com.jet.article.example.devblog",
        metrics = listOf(
            StartupTimingMetric(),
            TraceSectionMetric("HomeListPane", TraceSectionMetric.Mode.Sum),
        ),
        iterations = 5,
        startupMode = StartupMode.WARM,
        compilationMode = CompilationMode.Full(),
    ) {
        pressHome()
        startActivityAndWait()
        val feed = device.ensureObject(tag="test_posts")
        feed.setGestureMargin(device.displayWidth / 5)
        repeat(2) {
            feed.drag(Point(feed.visibleCenter.x, feed.visibleBounds.top))
            Thread.sleep(500)
        }
    }

}
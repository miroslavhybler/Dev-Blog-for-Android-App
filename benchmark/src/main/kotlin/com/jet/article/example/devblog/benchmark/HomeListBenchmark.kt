@file:OptIn(ExperimentalMetricApi::class)

package com.jet.article.example.devblog.benchmark

import android.graphics.Point
import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.ExperimentalMetricApi
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.TraceSectionMetric
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.jet.article.example.devblog.shared.Tracing
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
    fun openHomeAndScrollList() = benchmark(
        metrics = listOf(
            StartupTimingMetric(),
            TraceSectionMetric(
                sectionName = Tracing.Section.homeListPane,
                mode = TraceSectionMetric.Mode.Sum,
            ),
        ),
    ) {
        pressHome()
        startActivityAndWait()
        val postsLazyColumn = device.ensureObject(tag = Tracing.Tag.posts)
        postsLazyColumn.setGestureMargin(device.displayWidth / 5)
        repeat(2) {
            postsLazyColumn.drag(
                Point(
                    postsLazyColumn.visibleCenter.x,
                    postsLazyColumn.visibleBounds.top
                )
            )
            Thread.sleep(500)
        }
    }
}
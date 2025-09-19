@file:OptIn(ExperimentalMetricApi::class)

package com.jet.article.example.devblog.benchmark

import androidx.benchmark.macro.ExperimentalMetricApi
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.TraceSectionMetric
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.uiautomator.Direction
import com.jet.article.example.devblog.shared.Tracing
import com.jet.article.example.devblog.tests.shared.Scenarios
import org.junit.Test
import org.junit.runner.RunWith


/**
 * @author Miroslav Hýbler <br>
 * created on 09.10.2024
 */
@LargeTest
@RunWith(value = AndroidJUnit4::class)
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
                sectionName = Tracing.Section.postPane,
                mode = TraceSectionMetric.Mode.Sum,
            ),
        ),
    ) {
        Scenarios.openPost(
            scope = this,
        )
    }
}
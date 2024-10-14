package com.jet.article.example.devblog.benchmark

import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.benchmark.macro.Metric
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until
import org.junit.Rule


/**
 * @author Miroslav Hýbler <br>
 * created on 09.10.2024
 */
abstract class BaseBenchMark constructor() {


    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()


    val packageName: String
        get() = "com.jet.article.example.devblog"


    /**
     * Dont forget to annotate final function with [org.junit.Test]
     */
    fun benchmark(
        metrics: List<Metric> = listOf(StartupTimingMetric()),
        startupMode: StartupMode = StartupMode.WARM,
        compilationMode: CompilationMode = CompilationMode.Full(),
        iterations: Int = 5,
        setupBlock: MacrobenchmarkScope.() -> Unit = {},
        measureBlock: MacrobenchmarkScope.() -> Unit,
    ) {
        benchmarkRule.measureRepeated(
            packageName = packageName,
            metrics = metrics,
            compilationMode = compilationMode,
            startupMode = startupMode,
            iterations = iterations,
            setupBlock = setupBlock,
            measureBlock = measureBlock,
        )
    }


    /**
     * Tries to find [UiObject2] by given [tag] while also waiting for object to be available
     * by given [delay].
     * @return Found [UiObject2] or throws [NullPointerException] when not found by given [tag]
     * @throws NullPointerException When object was not found. Common causes could be wrong [tag]
     * or disabled testTagsAsResourceId. Check you have used sematics modifier like this:
     * ```kotlin
     *  modifier = Modifier
     *      .semantics { testTagsAsResourceId = true },
     * ```
     */
    fun UiDevice.ensureObject(
        tag: String,
        delay: Long = 3_000,
    ): UiObject2 {
        val testTag = By.res(tag)
        this.wait(Until.hasObject(testTag), delay)
        val obj = this.findObject(testTag)

        return obj ?: throw NullPointerException("No Ui element for tag $tag")
    }
}
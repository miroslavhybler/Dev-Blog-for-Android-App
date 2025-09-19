@file:Suppress("RedundantVisibilityModifier")

package com.jet.article.example.devblog.tests.shared

import android.app.Instrumentation
import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.junit.Rule
import org.junit.rules.TestRule

/**
 * **IMPORTANT!!** - Don't forget co call `.semantics { this.testTagsAsResourceId = true }` on some
 * root level modifier (like NavHost, NavDisplay, ...) or apply it every time togeteher with `testTag()`
 * modifier.
 * @author Miroslav Hýbler <br>
 * created on 18.09.2024
 */
public abstract class BaseSingleRuleTest<R : TestRule> constructor(
    @get:Rule
    public val rule: R,
) {

    protected val instrumentation: Instrumentation = InstrumentationRegistry.getInstrumentation()


    protected val context: Context = instrumentation.context

    protected val coroutineScope: CoroutineScope = CoroutineScope(context = Dispatchers.Main)


    public val targetPackageName: String
        get() = Scenarios.TARGET_PACKAGE_NAME


}
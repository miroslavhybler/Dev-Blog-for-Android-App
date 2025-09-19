@file:Suppress("RedundantUnitReturnType")

package com.jet.article.example.devblog.baselineprofile

import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.benchmark.macro.junit4.BaselineProfileRule
import com.jet.article.example.devblog.tests.shared.BaseSingleRuleTest

/**
 * @author Miroslav Hýbler <br>
 * created on 17.09.2025
 */
abstract class BaseBaselineProfileTest : BaseSingleRuleTest<BaselineProfileRule>(
    rule = BaselineProfileRule(),
) {

    /**
     * Don't forget to annotate final function with [org.junit.Test]
     * @param includeInStartupProfile   determines whether the generated profile should be also used
     * as a startup profile. A startup profile is utilized during the build process in order to
     * determine which classes are needed in the primary dex to optimize the startup time.
     * This flag should be used only for startup flows, such as main application startup pre and
     * post login or other entry points of the app. Note that methods collected in a startup profiles
     * are also utilized for baseline profiles.
     * See [this](https://d.android.com/topic/performance/baselineprofiles/dex-layout-optimizations).
     */
    fun baselineProfiles(
        includeInStartupProfile: Boolean= true,
        maxIterations: Int = 15,
        stableIterations: Int = 3,
        strictStability: Boolean = false,
        block:  MacrobenchmarkScope.() -> Unit,
    ): Unit {
        rule.collect(
            packageName = targetPackageName,
            maxIterations=maxIterations,
            stableIterations=stableIterations,
            includeInStartupProfile = includeInStartupProfile,
            strictStability=strictStability,
            profileBlock= block,
        )
    }

}
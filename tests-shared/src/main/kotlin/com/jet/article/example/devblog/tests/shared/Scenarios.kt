package com.jet.article.example.devblog.tests.shared

import android.graphics.Point
import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.UiDevice
import com.jet.article.example.devblog.shared.Tracing


/**
 * @author Miroslav Hýbler <br>
 * created on 18.09.2025
 */
object Scenarios {

    const val TARGET_PACKAGE_NAME: String = "com.jet.article.example.devblog"


    fun openHomeAndScroll(
        scope: MacrobenchmarkScope,
    ) = with(receiver = scope) {
        val device = this.device
        pressHome()
        startActivityAndWait()
        val postsLazyColumn = device.ensureObject(tag = Tracing.Tag.posts)
        postsLazyColumn.setGestureMargin(device.displayWidth / 5)
        repeat(times = 2) {
            postsLazyColumn.drag(
                Point(
                    postsLazyColumn.visibleCenter.x,
                    postsLazyColumn.visibleBounds.top
                )
            )
            Thread.sleep(300)
        }
        killProcess()
    }


    fun openPost(
        scope: MacrobenchmarkScope,
    ): Unit = with(receiver = scope) {
        val device = this.device
        pressHome()
        startActivityAndWait()
        //Waiting for first post item to appear
        val firstPost = device.ensureObject(tag = Tracing.Tag.firstPostItem)
        //Performing click, opening PostPane with selected post
        firstPost.click()
        //Waiting for post to be loaded and displayed
        val jetHtmlArticle = device.ensureObject(tag = Tracing.Tag.jetHtmlArticle)

        //Scrolling down in PostPane, simulation of reading post
        repeat(times = 2) {
            jetHtmlArticle.fling(Direction.DOWN, 1024)
            Thread.sleep(300)
        }
        killProcess()
    }

}
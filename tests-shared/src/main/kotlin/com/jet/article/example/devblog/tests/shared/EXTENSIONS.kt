package com.jet.article.example.devblog.tests.shared

import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until


/**
 * Tries to find [androidx.test.uiautomator.UiObject2] by given [tag] while also waiting for object to be available
 * by given [timeOut].
 * @return Found [androidx.test.uiautomator.UiObject2] or throws [NullPointerException] when not found by given [tag]
 * @throws NullPointerException When object was not found. Common causes could be wrong [tag]
 * or disabled testTagsAsResourceId. Check you have used sematics modifier like this:
 * ```kotlin
 *  modifier = Modifier
 *      .semantics { testTagsAsResourceId = true },
 * ```
 * @author Miroslav Hýbler <br>
 * created on 18.09.2024
 */
fun UiDevice.ensureObject(
    tag: String,
    timeOut: Long = 1_000,
): UiObject2 {
    val testTag = By.res( tag)
    this.wait(Until.hasObject(testTag), timeOut)
    val obj: UiObject2? = this.findObject(testTag)
    return obj ?: throw NullPointerException("No Ui element for tag $tag")
}

fun UiDevice.ensureObjectOrNull(
    tag: String,
    timeOut: Long = 1_000,
): UiObject2? {
    val testTag = By.res( tag)
    this.wait(Until.hasObject(testTag), timeOut)
    val obj: UiObject2? = try {
        this.findObject(testTag)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }

    return obj
}
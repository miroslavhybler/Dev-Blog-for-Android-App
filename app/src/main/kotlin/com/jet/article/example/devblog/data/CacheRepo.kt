package com.jet.article.example.devblog.data

import android.content.Context
import androidx.datastore.core.FileStorage
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton


/**
 * Persistent cache for downloaded posts
 * @author Miroslav Hýbler <br>
 * created on 27.09.2024
 */
@Singleton
class CacheRepo @Inject constructor(
    @ApplicationContext context: Context,
) {

    companion object {
        const val maxNumberOfEntries: Int = 10
    }

    private val rootDir: File = File(context.cacheDir, "posts-cache")
        get() {
            if (!field.exists()) {
                field.mkdirs()
            }
            return field
        }

    private val entries: Array<File>
        get() = rootDir.listFiles() ?: emptyArray()


    init {
        invalidateCache()
    }


    /**
     *
     */
    fun getCachedResponse(url: String): String? {
        val key = getCacheKey(url = url)
        val file = entries.find { it.name == key } ?: return null

        val text = file.readText()
        return text
    }


    /**
     *
     */
    fun saveToCache(url: String, content: String) {
        val key = getCacheKey(url = url)
        val file = File(rootDir, key).also { file ->
            file.createNewFile()
        }
        file.writeText(text = content)
    }


    /**
     * @return MD5 hash of the URL used as key
     */
    private fun getCacheKey(url: String): String {
        val md = MessageDigest.getInstance("MD5")
        val digest = md.digest(url.toByteArray())
        return digest.joinToString(separator = "", transform = { "%02x".format(it) })
    }


    /**
     *
     */
    private fun invalidateCache() {
        val files = entries.sortedByDescending { it.lastModified() }

        if (files.size > maxNumberOfEntries) {
            //Keep 20 latest items in cache
            val toBeDeleted = files.drop(n = maxNumberOfEntries)
            toBeDeleted.forEach { file ->
                file.delete()
            }
        }
    }

}
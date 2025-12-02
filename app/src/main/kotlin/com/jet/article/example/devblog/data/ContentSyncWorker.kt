@file:Suppress("RedundantVisibilityModifier")

package com.jet.article.example.devblog.data

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.PendingIntentCompat
import androidx.core.content.ContextCompat
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.jet.article.example.devblog.AndroidDevBlogApp
import com.jet.article.example.devblog.R
import com.jet.article.example.devblog.data.database.DatabaseRepo
import com.jet.article.example.devblog.ui.MainActivity
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.Date
import java.util.concurrent.TimeUnit


/**
 * Load index site on the background and loads articles from it. Should run every 2 days when
 * device is connected to the internet. When new posts are loaded, notification is also shown.
 * @author Miroslav Hýbler <br>
 * created on 16.08.2024
 */
@HiltWorker
public class ContentSyncWorker @AssistedInject public constructor(
    @Assisted context: Context,
    @Assisted workerParameters: WorkerParameters,
    private val databaseRepo: DatabaseRepo,
    private val coreRepo: CoreRepo,
) : CoroutineWorker(
    appContext = context,
    params = workerParameters,
) {


    companion object {

        /**
         * Adds [ContentSyncWorker] into [WorkManager]
         */
        fun register(context: Context) {
            val request = PeriodicWorkRequestBuilder<ContentSyncWorker>(
                repeatInterval = 2,
                repeatIntervalTimeUnit = TimeUnit.DAYS,
            )
                .setConstraints(
                    constraints = Constraints.Builder()
                        .setRequiredNetworkType(networkType = NetworkType.CONNECTED)
                        .build()
                )
                .setInputData(Data.Builder().putBoolean("", true).build())
                .build()
            val workManager = WorkManager.getInstance(context)

            workManager.enqueueUniquePeriodicWork(
                uniqueWorkName = "update-post-list",
                existingPeriodicWorkPolicy = ExistingPeriodicWorkPolicy.KEEP,
                request = request,
            )
        }
    }


    override suspend fun doWork(): Result {
        try {
            val newlySavedPosts = coreRepo.loadPostsFromRemote()
            val count = newlySavedPosts.getOrNull()

            if (newlySavedPosts.isFailure || count == null) {
                return Result.failure()
            }

            if (count > 0 && !MainActivity.isActive) {
                val newPost = databaseRepo.postDao.getLastPost()
                tryShowNotification(
                    title = newPost.title,
                    content = newPost.description.take(n = 100) + "...",
                    localPostId = newPost.id,
                )
            }
        } catch (e: IndexOutOfBoundsException) {
            e.printStackTrace()
        } finally {
            if (MainActivity.isActive) {
                @SuppressLint("CheckResult")
                coreRepo.loadPostsFromRemote(
                    updatedMax = Date(),
                    start = 0,
                    maxResults = 5,
                )
            }
        }
        return Result.success()
    }


    private fun tryShowNotification(
        title: String,
        content: String,
        localPostId: Int,
    ) {

        val notification = NotificationCompat.Builder(
            applicationContext,
            AndroidDevBlogApp.notificationNewPostsChannelId
        )
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(R.drawable.ic_android)
            .setSound(null)
            .setVibrate(null)
            .setContentIntent(
                PendingIntentCompat.getActivity(
                    applicationContext,
                    0,
                    Intent(applicationContext, MainActivity::class.java)
                        .putExtra("postId", localPostId),
                    PendingIntent.FLAG_UPDATE_CURRENT,
                    false
                )
            )
            .build()

        val manager = NotificationManagerCompat.from(applicationContext)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                manager.notify(10_000, notification)
            }
        } else {
            manager.notify(10_000, notification)
        }
    }
}
@file:Suppress("ConstPropertyName")

package com.jet.article.example.devblog

import android.Manifest
import android.app.Application
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationChannelGroupCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.CachePolicy
import com.jet.article.example.devblog.data.ContentSyncWorker
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import com.jet.article.example.devblog.data.CoreRepo
import com.jet.article.example.devblog.data.SettingsStorage
import com.jet.article.example.devblog.data.database.DatabaseRepo


/**
 * @author Miroslav Hýbler <br>
 * created on 13.08.2024
 */
@HiltAndroidApp
class AndroidDevBlogApp : Application(),
    Configuration.Provider,
    ImageLoaderFactory {

    companion object {
        const val notificationGroupId: String = "default-group"
        const val notificationNewPostsChannelId: String = "new-posts"

        /**
         * True when device is connected to internet, false otherwise.
         */
        var isConnectedToInternet: Boolean by mutableStateOf(value = true)
            private set


        /**
         * Flag indicates if [networkCallback] is registered in [ConnectivityManager].
         * True when [networkCallback] is registered, false otherwise.
         * @since 1.1.1
         */
        private var isNetworkCallbackRegistered: Boolean = false

        /**
         * NetworkCallback used to manage [isConnectedToInternet] flag
         */
        private val networkCallback: ConnectivityManager.NetworkCallback =
            object : ConnectivityManager.NetworkCallback() {

                override fun onAvailable(network: Network) {
                    isConnectedToInternet = true
                }

                override fun onLost(network: Network) {
                    isConnectedToInternet = false
                }

                override fun onUnavailable() {
                    isConnectedToInternet = false
                }
            }


        /**
         * Registers [networkCallback] to manage [isConnectedToInternet] flag, wifi network is preferred.
         * @param context
         * @param settings New user settings obtained from [SettingsStorage].
         */
        fun registerNetworkCallback(
            context: Context,
            settings: SettingsStorage.Settings,
        ) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.CHANGE_NETWORK_STATE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                unregisterNetworkCallback(context = context)
                val connectivityManager = context.getSystemService(
                    CONNECTIVITY_SERVICE
                ) as ConnectivityManager

                connectivityManager.registerNetworkCallback(
                    NetworkRequest.Builder()
                        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                        .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                        .also { builder ->
                            if (settings.isCellularDataUsageAllowed) {
                                builder.addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                            }
                        }
                        .build(),
                    networkCallback
                )
                isNetworkCallbackRegistered = true
                isConnectedToInternet = connectivityManager.activeNetwork != null
            }
        }


        /**
         * Unregisters [networkCallback], should be called when application task is closed.
         */
        fun unregisterNetworkCallback(
            context: Context,
        ) {
            if (!isNetworkCallbackRegistered) {
                return
            }

            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.CHANGE_NETWORK_STATE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                val connectivityManager =
                    context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager

                connectivityManager.unregisterNetworkCallback(networkCallback)
                isNetworkCallbackRegistered = false
            }
        }
    }

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var databaseRepo: DatabaseRepo

    @Inject
    lateinit var coreRepo: CoreRepo

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(
                workerFactory
//                ContentSyncWorker.Factory(
//                    databaseRepo = databaseRepo,
//                    coreRepo = coreRepo,
//                )
            ).build()


    override fun onCreate() {
        super.onCreate()
        prepareNotificationsGroupAndChannel()
        ContentSyncWorker.register(context = this)
        //Loading c++ jet-article library for parsing html
        System.loadLibrary("jet-article")

    }


    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(context = this)
            .diskCachePolicy(policy = CachePolicy.ENABLED)
            .memoryCachePolicy(policy = CachePolicy.ENABLED)
            .components {
                //Adding support for gifs
                if (Build.VERSION.SDK_INT >= 28) {
                    add(factory = ImageDecoderDecoder.Factory())
                } else {
                    add(factory = GifDecoder.Factory())
                }
            }
            .build()
    }


    /**
     * Creates default notification channel for new posts. When new post is added to [com.jet.article.example.devblog.data.database.LocalDatabase]
     * from [ContentSyncWorker], notification is shown.
     */
    private fun prepareNotificationsGroupAndChannel() {
        val defaultGroup = NotificationChannelGroupCompat.Builder(notificationGroupId)
            .setName(getString(R.string.ntfc_def_group_name))
            .setDescription(getString(R.string.ntfc_def_group_desc))
            .build()

        val newPostsChannel = NotificationChannelCompat.Builder(
            notificationNewPostsChannelId,
            NotificationManager.IMPORTANCE_MIN
        )
            .setName(getString(R.string.ntfc_def_channel_name))
            .setDescription(getString(R.string.ntfc_def_channel_desc))
            .setGroup(notificationGroupId)
            .setShowBadge(true)
            .setSound(null, null)
            .setVibrationPattern(null)
            .setVibrationEnabled(false)
            .build()

        val manager = NotificationManagerCompat.from(this)

        manager.createNotificationChannelGroup(defaultGroup)
        manager.createNotificationChannel(newPostsChannel)
    }
}
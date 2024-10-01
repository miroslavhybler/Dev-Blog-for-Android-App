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
import androidx.compose.runtime.MutableState
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
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat


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

        var isConnectedToInternet: Boolean by mutableStateOf(value = true)
            private set

    }

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().setWorkerFactory(workerFactory).build()


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


    override fun onCreate() {
        super.onCreate()
        prepareNotificationsGroupAndChannel()
        ContentSyncWorker.register(context = this)
        System.loadLibrary("jet-article")
        initNetworkCallback()
    }


    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(context = this)
            .diskCachePolicy(policy = CachePolicy.ENABLED)
            .memoryCachePolicy(policy = CachePolicy.ENABLED)
            .components {
                //Ading support for gifs
                if (Build.VERSION.SDK_INT >= 28) {
                    add(factory = ImageDecoderDecoder.Factory())
                } else {
                    add(factory = GifDecoder.Factory())
                }
            }
            .build()

    }


    private fun initNetworkCallback() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CHANGE_NETWORK_STATE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
            connectivityManager.registerNetworkCallback(
                NetworkRequest.Builder()
                    .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                    .build(),
                networkCallback
            )

            isConnectedToInternet = connectivityManager.activeNetwork != null
        }
    }


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
@file:Suppress("RedundantConstructorKeyword")

package com.jet.article.example.devblog.data

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.Keep
import androidx.appcompat.app.AppCompatDelegate
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.jet.article.example.devblog.R
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton


/**
 * @author Miroslav Hýbler <br>
 * created on 30.08.2024
 */
@Singleton
class SettingsStorage @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    companion object {
        private val Context.preferences: DataStore<Preferences> by preferencesDataStore(name = "settings")

        private val dynamicColorKey: Preferences.Key<Boolean> =
            booleanPreferencesKey(name = "dymanic_colors")
        private val darkModeKey: Preferences.Key<Int> =
            intPreferencesKey(name = "dark_mode")
        private val cellularDataKey: Preferences.Key<Boolean> =
            booleanPreferencesKey(name = "cellular_data_usage_allowed")
        private val usingTTSKey: Preferences.Key<Boolean> =
            booleanPreferencesKey(name = "using_tts")
    }


    /**
     * Datastore preferences instance for saving user settings
     */
    private val preferences: DataStore<Preferences>
        get() = context.preferences


    /**
     * Provides [Settings] from the datastore [preferences] as [Flow]
     */
    val settings: Flow<Settings> = preferences.data
        .map(transform = this::getSettings)


    /**
     * Saves [Settings] to the datastore [preferences]
     */
    suspend fun saveSettings(settings: Settings) {
        preferences.edit {
            it[dynamicColorKey] = settings.isUsingDynamicColors
            it[darkModeKey] = settings.nightModeFlags
            it[cellularDataKey] = settings.isCellularDataUsageAllowed
            it[usingTTSKey] = settings.isUsingTTS
        }
    }


    private fun getSettings(preferences: Preferences): Settings {
        return Settings(
            isUsingDynamicColors = preferences[dynamicColorKey]
                ?: Settings.Default.isUsingDynamicColors, //False by default
            nightModeFlags = preferences[darkModeKey]
                ?: Settings.Default.nightModeFlags,
            isCellularDataUsageAllowed = preferences[cellularDataKey]
                ?: Settings.Default.isCellularDataUsageAllowed, //True by default
            isUsingTTS = preferences[usingTTSKey]
                ?: Settings.Default.isUsingTTS, //True by default
        )
    }


    /**
     * User settings for the app
     * @param isUsingDynamicColors True when dynamic colors are enabled, false otherwise, this is
     * available only for **Android 12 and bigger**.
     * @param nightModeFlags [AppCompatDelegate.NightMode] flags for usage of dark mode.
     * @param isCellularDataUsageAllowed True when cellular data usage is allowed, when false, app
     * will download data only on WiFi. This param was added in version 1.1.1 with true as default
     * value.
     */
    @Keep
    data class Settings constructor(
        val isUsingDynamicColors: Boolean,
        @AppCompatDelegate.NightMode
        val nightModeFlags: Int,
        val isCellularDataUsageAllowed: Boolean,
        val isUsingTTS: Boolean,
    ) {

        companion object {


            /**
             * Default settings for the app
             */
            val Default: Settings = Settings(
                isUsingDynamicColors = false,
                nightModeFlags = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM,
                isCellularDataUsageAllowed = true,
                isUsingTTS = true,
            )


            /**
             * @return User friendly description of dark mode flag
             */
            fun nightModeString(
                settings: Settings,
                context: Context,
            ): String {
                return nightModeString(context = context, flags = settings.nightModeFlags)
            }

            /**
             * @return User friendly description of dark mode flag
             */
            fun nightModeString(
                context: Context,
                @AppCompatDelegate.NightMode flags: Int
            ): String {
                return when (flags) {
                    AppCompatDelegate.MODE_NIGHT_YES -> context.getString(R.string.dark_mode_yes)
                    AppCompatDelegate.MODE_NIGHT_NO -> context.getString(R.string.dark_mode_no)
                    else -> context.getString(R.string.dark_mode_system)
                }
            }
        }

        /**
         * @return User friendly description of dark mode flag
         */
        fun nightModeString(context: Context): String {
            return nightModeString(context = context, settings = this)
        }

    }
}
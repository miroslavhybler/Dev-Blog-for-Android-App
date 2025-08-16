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

        private val dynamicColorKey: Preferences.Key<Boolean>  = booleanPreferencesKey(name = "dymanic_colors")
        private val darkModeKey = intPreferencesKey(name = "dark_mode")
        private val cellularDataKey: Preferences.Key<Boolean>  = booleanPreferencesKey(name = "cellular_data_usage_allowed")
    }


    private val preferences: DataStore<Preferences>
        get() = context.preferences


    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        "app-preferences",
        Context.MODE_PRIVATE
    )

    val settings: Flow<Settings> = preferences.data.map(this::getSettings)


    suspend fun saveSettings(settings: Settings) {
        preferences.edit {
            it[dynamicColorKey] = settings.isUsingDynamicColors
            it[darkModeKey] = settings.nightModeFlags
            it[cellularDataKey] = settings.isCellularDataUsageAllowed
        }
    }


    private fun getSettings(preferences: Preferences): Settings {
        return Settings(
            isUsingDynamicColors = preferences[dynamicColorKey] == true,
            nightModeFlags = preferences[darkModeKey] ?: AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM,
            isCellularDataUsageAllowed = preferences[cellularDataKey] != false,
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
        val isUsingDynamicColors: Boolean = false,
        @AppCompatDelegate.NightMode
        val nightModeFlags: Int = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM,
        val isCellularDataUsageAllowed: Boolean = true,
    ) {

        companion object {

            /**
             * @return User friendly description of dark mode
             */
            fun nightModeString(
                settings: Settings,
                context: Context,
            ): String {
                return nightModeString(context = context, flags = settings.nightModeFlags)
            }

            /**
             * @return User friendly description of dark mode
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

        fun nightModeString(context: Context): String {
            return nightModeString(context = context, settings = this)
        }

    }
}
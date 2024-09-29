package com.jet.article.example.devblog.data

import android.content.Context
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

        private val dynamicColorKey = booleanPreferencesKey(name = "dymanic_colors")
        private val darkModeKey = intPreferencesKey(name = "dark_mode")

    }


    private val preferences: DataStore<Preferences>
        get() = context.preferences

    val settings: Flow<Settings> = preferences.data.map(this::getSettings)

    suspend fun saveSettings(settings: Settings) {
        preferences.edit {
            it[dynamicColorKey] = settings.isUsingDynamicColors
            it[darkModeKey] = settings.nightModeFlags
        }
    }

    private fun getSettings(preferences: Preferences): Settings {
        return Settings(
            isUsingDynamicColors = preferences[dynamicColorKey] == true,
            nightModeFlags = preferences[darkModeKey] ?: AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        )
    }


    @Keep
    data class Settings constructor(
        val isUsingDynamicColors: Boolean = false,
        @AppCompatDelegate.NightMode
        val nightModeFlags: Int = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM,
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
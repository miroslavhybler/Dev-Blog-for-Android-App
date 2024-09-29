package com.jet.article.example.devblog.ui.settings

import android.app.Application
import com.jet.article.example.devblog.data.SettingsStorage
import com.jet.article.example.devblog.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


/**
 * @author Miroslav Hýbler <br>
 * created on 19.08.2024
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    application: Application,
    settingsStorage :SettingsStorage,
) : BaseViewModel(
    application = application,
    settingsStorage = settingsStorage,
) {
}
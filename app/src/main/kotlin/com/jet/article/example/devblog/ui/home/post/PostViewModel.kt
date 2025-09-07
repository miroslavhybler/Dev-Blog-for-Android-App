package com.jet.article.example.devblog.ui.home.post

import android.app.Application
import com.jet.article.example.devblog.data.SettingsStorage
import com.jet.article.example.devblog.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


/**
 * @author Miroslav Hýbler <br>
 * created on 19.08.2025
 */
@HiltViewModel
class PostViewModel @Inject constructor(
    application: Application,
    settingsStorage: SettingsStorage,
) : BaseViewModel(
    application = application,
    settingsStorage=settingsStorage,
) {
}
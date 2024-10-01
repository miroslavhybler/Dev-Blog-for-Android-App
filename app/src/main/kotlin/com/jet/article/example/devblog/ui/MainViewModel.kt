package com.jet.article.example.devblog.ui

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.jet.article.example.devblog.data.SettingsStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


/**
 * @author Miroslav Hýbler <br>
 * created on 30.08.2024
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    application: Application,
    settingsStorage: SettingsStorage,
) : BaseViewModel(
    application = application,
    settingsStorage = settingsStorage,
) {

    fun load() {
        viewModelScope.launch {
            coreRepo.loadPosts(isRefresh = true)
        }
    }
}
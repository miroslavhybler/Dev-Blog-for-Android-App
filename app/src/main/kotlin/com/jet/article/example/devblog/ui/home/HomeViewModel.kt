package com.jet.article.example.devblog.ui.home

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.jet.article.example.devblog.data.AdjustedPostData
import com.jet.article.example.devblog.data.SettingsStorage
import com.jet.article.example.devblog.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


/**
 * @author Miroslav Hýbler <br>
 * created on 14.08.2024
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    application: Application,
    settingsStorage: SettingsStorage,
) : BaseViewModel(
    application,
    settingsStorage = settingsStorage,
) {


    private val mPostData: MutableStateFlow<Result<AdjustedPostData>?> =
        MutableStateFlow(value = null)
    val postData: StateFlow<Result<AdjustedPostData>?> = mPostData.asStateFlow()


    fun loadPost(
        url: String,
        isRefresh: Boolean = false,
    ) {
        viewModelScope.launch {
            mPostData.value = coreRepo.loadPostDetail(url = url, isRefresh = isRefresh)
        }
    }


    fun onBack() {
        mPostData.value = null
    }
}
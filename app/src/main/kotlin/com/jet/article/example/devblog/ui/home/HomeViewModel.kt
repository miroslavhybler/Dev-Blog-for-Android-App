package com.jet.article.example.devblog.ui.home

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.jet.article.example.devblog.data.AdjustedPostData
import com.jet.article.example.devblog.data.SettingsStorage
import com.jet.article.example.devblog.data.database.PostItem
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

    private val mSelectedPost: MutableStateFlow<PostItem?> = MutableStateFlow(value = null)
    val selectedPost: StateFlow<PostItem?> = mSelectedPost.asStateFlow()

    fun loadPostDetail(
        item: PostItem,
        isRefresh: Boolean = false,
    ) {
        mSelectedPost.value = item
        viewModelScope.launch {
            mPostData.value = coreRepo.loadPostDetail(
                url = item.url,
                isRefresh = isRefresh,
            )
        }

        if (item.isUnreadState) {
            viewModelScope.launch() {
                item.isUnreadState = false
                databaseRepo.updateReadedPost(id = item.id)
            }
        }
    }


    fun loadPostFromDeeplink(
        url: String,
        isRefresh: Boolean = false,
        onFinal: suspend () -> Unit,
    ) {
        viewModelScope.launch {
            mPostData.value = coreRepo.loadPostDetail(
                url = url,
                isRefresh = isRefresh,
            )
            onFinal()
        }
    }


    fun onBack() {
        mPostData.value = null
        mSelectedPost.value = null
    }
}
package com.jet.article.example.devblog.ui.home.post

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


    /**
     * Holding detail of [mSelectedPost] for [com.jet.article.example.devblog.ui.home.post.PostScreen].
     */
    private val mPostData: MutableStateFlow<Result<AdjustedPostData>?> =
        MutableStateFlow(value = null)
    val postData: StateFlow<Result<AdjustedPostData>?> = mPostData.asStateFlow()


    /**
     * Loads [PostItem] detail and updates [PostItem.isUnread] flag.
     */
    fun loadPostDetail(
        item: PostItem,
        isRefresh: Boolean = false,
    ) {
        viewModelScope.launch {
            mPostData.value = coreRepo.loadPostDetail(
                url = item.url,
                isRefresh = isRefresh,
            )
        }

        if (item.isUnreadState) {
            viewModelScope.launch() {
                item.isUnreadState = false
                databaseRepo.updateReadPost(id = item.id)
            }
        }
    }


    /**
     * Loads post detail from deeplink [url]
     * @param url Url to post detail that was passed as a deeplink into the app
     */
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
    }
}
package com.jet.article.example.devblog.ui.post

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.jet.article.example.devblog.data.AdjustedPostData
import com.jet.article.example.devblog.data.SettingsStorage
import com.jet.article.example.devblog.data.database.PostItem
import com.jet.article.example.devblog.ui.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


/**
 * @author Miroslav Hýbler <br>
 * created on 20.09.2025
 */
abstract class BasePostViewModel constructor(
    application: Application,
    settingsStorage: SettingsStorage,
) : BaseViewModel(
    application = application,
    settingsStorage = settingsStorage,
) {

    protected val mPostData: MutableStateFlow<Result<AdjustedPostData>?> =
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


    fun clear() {
        mPostData.value = null
    }
}
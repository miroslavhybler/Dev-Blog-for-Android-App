package com.jet.article.example.devblog.ui.post

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.jet.article.example.devblog.data.AdjustedPostData
import com.jet.article.example.devblog.data.PostContentDiveIndexer
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
    private val postContentDiveIndexer: PostContentDiveIndexer,
) : BasePostViewModel(
    application = application,
    settingsStorage=settingsStorage,
    postContentDiveIndexer = postContentDiveIndexer,
) {


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
            val loadedPost = coreRepo.loadPostDetail(
                url = url,
                isRefresh = isRefresh,
            )
            mPostData.value = loadedPost
            if (loadedPost.isSuccess) {
                postContentDiveIndexer.index(loadedPost.getOrThrow())
            }
            onFinal()
        }
    }

}

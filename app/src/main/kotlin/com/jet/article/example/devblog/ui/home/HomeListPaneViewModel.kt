package com.jet.article.example.devblog.ui.home

import android.app.Application
import androidx.compose.foundation.lazy.LazyListState
import androidx.lifecycle.viewModelScope
import com.jet.article.example.devblog.data.SettingsStorage
import com.jet.article.example.devblog.data.database.PostItem
import com.jet.article.example.devblog.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


/**
 * @author Miroslav Hýbler <br>
 * created on 15.08.2024
 */
@HiltViewModel
class HomeListPaneViewModel @Inject constructor(
    application: Application,
    settingsStorage: SettingsStorage,
) : BaseViewModel(
    application,
    settingsStorage = settingsStorage,
) {

    val posts: StateFlow<Result<List<PostItem>>?>
        get() = coreRepo.posts

    val hasError: StateFlow<Boolean>
        get() = coreRepo.hasErrorFromRemote

    /**
     *
     */
    val lazyListState: LazyListState = LazyListState()

}
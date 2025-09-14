package com.jet.article.example.devblog.ui.home

import android.app.Application
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.jet.article.example.devblog.data.PostsPagingSource
import com.jet.article.example.devblog.data.SettingsStorage
import com.jet.article.example.devblog.data.database.PostItem
import com.jet.article.example.devblog.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
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

    private var currentSource: PostsPagingSource? = null

    val posts: Flow<PagingData<PostItem>> = Pager(
        config = PagingConfig(
            pageSize = 7,
            initialLoadSize = 7,
            prefetchDistance = 2,
        ),
        pagingSourceFactory = {
            PostsPagingSource(coreRepo = coreRepo).also {
                currentSource = it
            }
        }
    ).flow.cachedIn(scope = viewModelScope)


    fun refresh() {
        currentSource?.invalidate()
    }
}
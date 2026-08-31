package com.jet.article.example.devblog.ui.home

import android.app.Application
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.jet.article.example.devblog.data.PostsPagingSource
import com.jet.article.example.devblog.data.PostContentDiveIndexer
import com.jet.article.example.devblog.data.SettingsStorage
import com.jet.article.example.devblog.data.database.DatabaseRepo
import com.jet.article.example.devblog.data.database.PostItem
import com.jet.article.example.devblog.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds


/**
 * @author Miroslav Hýbler <br>
 * created on 15.08.2024
 */
@HiltViewModel
class HomeListPaneViewModel @Inject constructor(
    application: Application,
    settingsStorage: SettingsStorage,
    private val postContentDiveIndexer: PostContentDiveIndexer,
    private val searchDatabaseRepo: DatabaseRepo,
) : BaseViewModel(
    application,
    settingsStorage = settingsStorage,
) {

    private var currentSource: PostsPagingSource? = null
    private var searchJob: Job? = null

    private val mSearchResults = MutableStateFlow<List<PostSearchResult>>(value = emptyList())
    val searchResults: StateFlow<List<PostSearchResult>> = mSearchResults.asStateFlow()

    private val mIsSearching = MutableStateFlow(value = false)
    val isSearching: StateFlow<Boolean> = mIsSearching.asStateFlow()

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

    fun search(query: String) {
        searchJob?.cancel()
        if (query.isBlank()) {
            mIsSearching.value = false
            mSearchResults.value = emptyList()
            return
        }
        searchJob = viewModelScope.launch {
            mIsSearching.value = true
            delay(duration = 200.milliseconds)
            mSearchResults.value = postContentDiveIndexer.search(query = query)
                .mapNotNull { result ->
                    searchDatabaseRepo.getPostByUrlOrNull(url = result.postUrl)?.let { post ->
                        PostSearchResult(post = post, snippet = result.snippet)
                    }
                }
            mIsSearching.value = false
        }
    }
}

data class PostSearchResult constructor(
    val post: PostItem,
    val snippet: String,
)

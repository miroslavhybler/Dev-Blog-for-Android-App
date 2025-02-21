package com.jet.article.example.devblog.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.jet.article.example.devblog.AndroidDevBlogApp
import com.jet.article.example.devblog.NotConnectedToInternetException
import com.jet.article.example.devblog.data.database.PostItem
import com.jet.article.example.devblog.ui.MainActivity
import java.util.Date


/**
 * @author Miroslav Hýbler <br>
 * created on 11.02.2025
 */
class PostsPagingSource constructor(
    private val coreRepo: CoreRepo,
) : PagingSource<Int, PostItem>() {

    var lastDate: Date = Date()

    //TODO take look at https://developer.android.com/topic/libraries/architecture/paging/v3-network-db
    override suspend fun load(
        params: LoadParams<Int>
    ): LoadResult<Int, PostItem> {
        val page = params.key ?: 0
        val remoteResult = if (AndroidDevBlogApp.isConnectedToInternet) {
            coreRepo.loadPostsFromRemote(
                updatedMax = lastDate,
                maxResults = params.loadSize,
                start = page * params.loadSize,
            )
        } else {
            Result.failure(exception = NotConnectedToInternetException())
        }

        if (MainActivity.isSplashScreenVisible) {
            MainActivity.onDataLoaded()
        }

        return if (
            remoteResult.isSuccess
            || remoteResult.exceptionOrNull() is NotConnectedToInternetException
        ) {
            val posts = coreRepo.loadFromLocal(page = page, count = params.loadSize)
            val newLastDate = if (!posts.isEmpty()) posts.last().date.toDate() else null
            val nextKey = if (!posts.isEmpty()) page + 1 else null // endless
            val previousKey = if (page == 0) null else page - 1
            if (newLastDate != null) {
                this@PostsPagingSource.lastDate = newLastDate
            }
            LoadResult.Page(
                data = posts,
                prevKey = previousKey,
                nextKey = nextKey
            )
        } else {
            LoadResult.Error(throwable = remoteResult.exceptionOrNull() ?: UnknownError())
        }
    }

    override fun getRefreshKey(state: PagingState<Int, PostItem>): Int? {
        return 0
    }
}
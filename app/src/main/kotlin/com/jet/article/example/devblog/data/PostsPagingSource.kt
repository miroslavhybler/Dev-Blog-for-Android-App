package com.jet.article.example.devblog.data

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.jet.article.example.devblog.AndroidDevBlogApp
import com.jet.article.example.devblog.NotConnectedToInternetException
import com.jet.article.example.devblog.data.database.PostItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.util.Date


/**
 * @author Miroslav Hýbler <br>
 * created on 11.02.2025
 */
class PostsPagingSource constructor(
    private val coreRepo: CoreRepo,
) : PagingSource<Int, PostItem>() {

    var lastDate: Date = Date()

    override suspend fun load(
        params: LoadParams<Int>
    ): LoadResult<Int, PostItem> {
        val page = params.key ?: 0
        Log.d("mirek", "load: page: $page")
        if (!AndroidDevBlogApp.isConnectedToInternet) {
            return LoadResult.Error(throwable = NotConnectedToInternetException())
        }

        val result = coreRepo.loadPostsFromRemote(
            updatedMax = lastDate,
            maxResults = params.loadSize,
            start = page * params.loadSize,
        )
        return if (result.isSuccess) {
            val posts = coreRepo.loadFromLocal(page = page, limit = params.loadSize)
            val newLastDate = if (!posts.isEmpty()) posts.last().date.toDate() else null
            val nextKey = page + 1 //else null // endless
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
            LoadResult.Error(throwable = result.exceptionOrNull() ?: UnknownError())
        }
    }

    override fun getRefreshKey(state: PagingState<Int, PostItem>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val closestPage = state.closestPageToPosition(anchorPosition)
            closestPage?.nextKey?.minus(1) ?: closestPage?.prevKey?.plus(1)
        }
    }
}
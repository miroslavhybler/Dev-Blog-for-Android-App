@file:OptIn(JetExperimental::class)

package com.jet.article.example.devblog.data

import android.content.Context
import androidx.annotation.CheckResult
import androidx.compose.ui.util.fastForEach
import com.jet.article.ArticleAnalyzer
import com.jet.article.ArticleParser
import com.jet.article.data.HtmlArticleData
import com.jet.article.data.HtmlElement
import com.jet.article.data.TagInfo
import com.jet.article.example.devblog.AndroidDevBlogApp
import com.jet.article.example.devblog.Constants
import com.jet.article.example.devblog.ContentParseException
import com.jet.article.example.devblog.data.database.DatabaseRepo
import com.jet.article.example.devblog.data.database.PostItem
import com.jet.article.example.devblog.parseWithInitialization
import dagger.hilt.android.qualifiers.ApplicationContext
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.cache.InvalidCacheStateException
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import mir.oslav.jet.annotations.JetExperimental
import okio.IOException
import java.net.UnknownHostException
import javax.inject.Inject
import javax.inject.Singleton


/**
 * @author Miroslav Hýbler <br>
 * created on 20.08.2024
 */
@Singleton
class CoreRepo @Inject constructor(
    @ApplicationContext context: Context,
    private val databaseRepo: DatabaseRepo,
    private val cacheRepo: CacheRepo,
) {

    companion object {
        private const val TIME_OUT = 3_000
    }

    private val mPosts: MutableStateFlow<Result<List<PostItem>>?> = MutableStateFlow(value = null)
    val posts: StateFlow<Result<List<PostItem>>?> = mPosts.asStateFlow()

    private val ktorHttpClient: HttpClient = HttpClient(
        engineFactory = Android,
        block = {
            engine {
                this.connectTimeout = TIME_OUT
                this.socketTimeout = TIME_OUT
            }
        }
    )


    /**
     * Loads list of posts for [com.jet.article.example.devblog.ui.home.HomeListPane]
     * @param isRefresh When set to true, list is refreshed from remote source and saved to local database.
     * False by default.
     */
    suspend fun loadPosts(
        isRefresh: Boolean = false,
    ): Unit = withContext(context = Dispatchers.Default) {
        if (isRefresh && AndroidDevBlogApp.isConnectedToInternet) {
            val result = loadPostsFromRemote()
            when {
                result.isSuccess -> {
                    loadPostsFromLocal()
                }
                result.isFailure -> {
                    mPosts.value = Result.failure(
                        exception = result.exceptionOrNull() ?: UnknownError()
                    )
                }
            }

            return@withContext
        }
        loadPostsFromLocal()
    }


    /**
     * Loads single post detail by given [url]
     * @param url Url of the post
     * @param isRefresh When true, post is refreshed from remote source. False by default.
     */
    suspend fun loadPostDetail(
        url: String,
        isRefresh: Boolean = false,
    ): Result<AdjustedPostData> = withContext(context = Dispatchers.IO) {
        val postResult = loadHtmlFromUrl(
            url = url,
            isRefresh = isRefresh,
            isCachingResult = true,
        )
        val htmlCode = postResult.getOrNull()
        return@withContext when {
            postResult.isSuccess && htmlCode != null -> {
                parsePostDetail(htmlCode = htmlCode, url = url)
            }

            else -> {
                Result.failure(
                    exception = postResult.exceptionOrNull() ?: UnknownError()
                )
            }
        }
    }


    /**
     * @return Result of loading posts from remote source. Result data is count of newly saved posts.
     */
    @CheckResult
    suspend fun loadPostsFromRemote(): Result<Int> = withContext(
        context = Dispatchers.IO
    ) {
        //Posts are saved to room database, no  need to cache response for index site
        val indexSiteCodeResult = loadHtmlFromUrl(
            url = Constants.indexUrl,
            isRefresh = true,
            isCachingResult = false,
        )
        val htmlCode = indexSiteCodeResult.getOrNull()

        return@withContext when {
            indexSiteCodeResult.isSuccess && htmlCode != null -> {
                val posts = parsePosts(htmlCode = htmlCode)
                val list = posts.getOrNull()
                return@withContext if (posts.isSuccess && list != null) {
                    val newlySaved = saveNewPosts(posts = list)
                    Result.success(value = newlySaved)
                } else
                    Result.failure(
                        exception = posts.exceptionOrNull()
                            ?: IllegalStateException("Unknown error during loading post list from remote")
                    )
            }

            else -> {
                return@withContext Result.failure(
                    exception = NullPointerException("Html code is null")
                )
            }
        }
    }


    /**
     * Tries to save new posts to local database, only if there was no post saved before for
     * given url
     * @return Count of saved posts
     */
    suspend fun saveNewPosts(
        posts: List<PostItem>,
    ): Int {
        val dao = databaseRepo.postDao
        var count = 0
        posts.fastForEach { post ->
            databaseRepo.withTransaction {
                if (!dao.contains(url = post.url)) {
                    dao.insert(item = post)
                    count += 1
                }
            }
        }
        return count
    }


    /**
     *
     */
    private suspend fun parsePostDetail(
        htmlCode: String,
        url: String,
    ): Result<AdjustedPostData> {
        val original = ArticleParser.parseWithInitialization(
            content = htmlCode,
            url = url,
        )

        try {
            val title = original.elements
                .first { it is HtmlElement.Title } as HtmlElement.Title
            val headerImage = original.elements
                .first { it is HtmlElement.Image } as HtmlElement.Image
            val date = original.elements
                .first { it is HtmlElement.TextBlock } as HtmlElement.TextBlock


            val simpleDate = processDate(date = date.text)
            val newElements = ArrayList(original.elements).apply {
                remove(element = headerImage)
                remove(element = title)
                remove(element = date)
            }

            if (simpleDate == null) {
                return Result.failure(
                    exception = NullPointerException("Unable to get date")
                )
            }

            return Result.success(
                value = AdjustedPostData(
                    postData = original.copy(elements = newElements),
                    headerImage = headerImage,
                    date = simpleDate,
                    title = title,
                )
            )
        } catch (e: NoSuchElementException) {
            e.printStackTrace()
            return Result.failure(
                exception = NoSuchElementException("Unable to adjust html data")
            )
        }
    }


    /**
     *
     */
    private suspend fun parsePosts(
        htmlCode: String
    ): Result<List<PostItem>> {
        ArticleParser.initialize(
            isLoggingEnabled = false,
            areImagesEnabled = true,
            isSimpleTextFormatAllowed = true,
            isQueringTextOutsideTextTags = true,
        )
        var hasFeaturedItem: Boolean = false
        val links: ArrayList<TagInfo> = ArrayList()
        ArticleAnalyzer.process(
            content = htmlCode,
            onTag = { tag ->
                if (tag.tag == "a" && tag.clazz == "adb-card__href") {
                    links.add(element = tag)
                }

                if (tag.tag == "div" && tag.clazz == "featured__wrapper") {
                    hasFeaturedItem = true
                }
            }
        )
        val data = ArticleParser.parseWithInitialization(
            content = htmlCode,
            url = Constants.indexUrl,
        )

        val finalData = data.getPostList(links = links, hasFeaturedItem = hasFeaturedItem)
        return finalData
    }


    /**
     * Loads list of posts from local database.
     */
    private suspend fun loadPostsFromLocal() {
        mPosts.value = Result.success(value = databaseRepo.postDao.getAll())
    }


    /**
     * Loads html code from given [url]
     * @param isRefresh
     * @param isCachingResult Flag indicates that response is cached locally and should be returned from cache.
     * True by default.
     */
    private suspend fun loadHtmlFromUrl(
        url: String,
        isRefresh: Boolean = false,
        isCachingResult: Boolean = true,
    ): Result<String> {
        return try {
            if (isCachingResult && !isRefresh) {
                val fromCache = cacheRepo.getCachedResponse(url = url)
                if (fromCache != null) {
                    return Result.success(value = fromCache)
                }
            }

            val response: HttpResponse = ktorHttpClient.get(urlString = url)
            val body = response.bodyAsText()

            if (isCachingResult) {
                cacheRepo.saveToCache(url = url, content = body)
            }
            Result.success(value = body)
        } catch (e: InvalidCacheStateException) {
            e.printStackTrace()
            Result.failure(exception = e)
        } catch (e: UnknownHostException) {
            e.printStackTrace()
            Result.failure(exception = e)
        } catch (e: SocketTimeoutException) {
            Result.failure(exception = e)
        } catch (e: IOException) {
            e.printStackTrace()
            Result.failure(exception = e)
        }
    }


    /**
     *
     */
    private fun HtmlArticleData.getPostList(
        links: List<TagInfo>,
        hasFeaturedItem: Boolean,
    ): Result<List<PostItem>> {
        try {
            val newList = ArrayList<HtmlElement>()
            newList.addAll(elements = elements)

            if (hasFeaturedItem) {
                //Removing "featured" item
                newList.removeAt(index = 0)
                newList.removeAt(index = 0)
                newList.removeAt(index = 0)
                newList.removeAt(index = 0)
            }
            val chunked = newList.chunked(size = 4)

            if (chunked.size != links.size) {
                return Result.failure(
                    exception = IllegalStateException("List size mismatch")
                )
            }

            val list = chunked.mapIndexed { index, sublist ->
                val dateString = (sublist[2] as HtmlElement.TextBlock).text
                val date = processDate(date = dateString)!!
                PostItem(
                    image = (sublist[0] as HtmlElement.Image).url,

                    title = ArticleParser.Utils.clearTagsAndReplaceEntitiesFromText(
                        input = (sublist[1] as HtmlElement.TextBlock).text
                    ),
                    date = date,
                    dateTimeStamp = date.timestamp,
                    description = ArticleParser.Utils.clearTagsAndReplaceEntitiesFromText(
                        input = (sublist[3] as HtmlElement.TextBlock).text,
                    ),
                    url = links[index].tagAttributes["href"]
                        ?: throw NullPointerException("Unable to extract href from ${links[index]}"),
                )
            }
            return Result.success(value = list)
        } catch (e: ClassCastException) {
            e.printStackTrace()
            return Result.failure(exception = ContentParseException(cause = e))
        } catch (e: NoSuchElementException) {
            e.printStackTrace()
            return Result.failure(exception = ContentParseException(cause = e))
        } catch (e: IndexOutOfBoundsException) {
            e.printStackTrace()
            return Result.failure(exception = ContentParseException(cause = e))
        }
    }


    /**
     *
     */
    fun processDate(
        date: String
    ): SimpleDate? {
        val array = date.split(' ')
        val day = array.getOrNull(index = 0)?.toIntOrNull()
        val monthString = array.getOrNull(index = 1)
        val month = Month.entries.find { it.displayName == monthString }
        val year = array.getOrNull(index = 2)?.toInt()


        return if (day != null && month != null && year != null) {
            SimpleDate(year = year, month = month, dayOfMonth = day)
        } else null
    }
}
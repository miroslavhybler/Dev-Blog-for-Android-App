@file:OptIn(JetExperimental::class)

package com.jet.article.example.devblog.data

import androidx.annotation.CheckResult
import androidx.annotation.Keep
import androidx.compose.ui.util.fastForEach
import com.jet.article.example.devblog.AndroidDevBlogApp
import com.jet.article.example.devblog.Constants
import com.jet.article.example.devblog.ContentParseException
import com.jet.article.example.devblog.NotConnectedToInternetException
import com.jet.article.example.devblog.RequestNotSucesfullException
import com.jet.article.example.devblog.data.database.DatabaseRepo
import com.jet.article.example.devblog.data.database.PostItem
import com.jet.article.example.devblog.ui.post.TitleWithOriginalIndex
import com.jet.article.nativelib.ArticleContentTransformer
import com.jet.article.nativelib.ArticleData
import com.jet.article.nativelib.ArticleElement
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.plugins.cache.InvalidCacheStateException
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mir.oslav.jet.annotations.JetExperimental
import okio.IOException
import java.net.UnknownHostException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject
import javax.inject.Singleton


/**
 * Repository holding functionality for loading post list from index site of [Android Developer blog](https://android-developers.googleblog.com/)
 * and single posts by url.
 * @author Miroslav Hýbler <br>
 * created on 20.08.2024
 */
@Singleton
class CoreRepo @Inject constructor(
    private val databaseRepo: DatabaseRepo,
    private val cacheRepo: CacheRepo,
) {

    companion object {
        private const val TIME_OUT = 5_000
    }

    /**
     * Used to format date into string.
     * E.g. 2024-12-18T11:00:00-08:00
     * @since 1.1.2
     */
    private val simpleDateFormat: SimpleDateFormat = SimpleDateFormat(
        "yyyy-MM-dd'T'HH:mm:ssXXX",
        Locale.US,
    ).also { format ->
        format.timeZone = TimeZone.getTimeZone("GMT-08:00")
    }


    @Keep
    private data class LinkInfo(val href: String?)


    /**
     * Http client for network calls
     * @since 1.0.0
     */
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
     * Loads single post detail by given [url]
     * @param url Url of the post
     * @param isRefresh When true, post is refreshed from remote source. False by default.
     * @since 1.0.0
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
        val exception = postResult.exceptionOrNull()
        return@withContext when {
            postResult.isSuccess && htmlCode != null -> {
                parsePostDetail(htmlCode = htmlCode, url = url)
            }

            exception != null -> {
                Result.failure(
                    exception = IllegalStateException(exception)
                )
            }

            !AndroidDevBlogApp.isConnectedToInternet -> {
                Result.failure(
                    exception = IllegalStateException(
                        "Connection lost while making request"
                    )
                )
            }

            else -> {
                Result.failure(
                    exception = NullPointerException(
                        "Html code of post detail is null and error cause is not provided"
                    )
                )
            }
        }
    }


    /**
     * Loads list of posts from remote source. Source is index side on [Constants.indexUrl] containing
     * list of posts. Post are then parsed and converted via [ArticleParser] and [ArticleAnalyzer]
     * into [PostItem].
     * @return Result of loading posts from remote source. Result data is count of newly saved posts.
     * @since 1.0.0
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
        val exception = indexSiteCodeResult.exceptionOrNull()
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

            exception != null -> {
                return@withContext Result.failure(exception = exception)
            }

            else -> {
                return@withContext Result.failure(
                    exception = NullPointerException("Html code and case are both null")
                )
            }
        }
    }

    /**
     * Loads list of posts from remote source. Source is index side on [Constants.indexUrl] containing
     * list of posts. Post are then parsed and converted via [ArticleParser] and [ArticleAnalyzer]
     * into [PostItem].
     * @return Result of loading posts from remote source. Result data is count of newly saved posts.
     */
    @CheckResult
    suspend fun loadPostsFromRemote(
        updatedMax: Date,
        start: Int,
        maxResults: Int,
    ): Result<Int> = withContext(
        context = Dispatchers.IO,
    ) {
        val date = simpleDateFormat.format(updatedMax)

        val url = buildString {
            append(Constants.indexUrl)
            append("search?")
            append("updated-max=${date}")
            append("&max-results=${maxResults}")
            append("&start=${start}")
            append("&reverse-paginate=true")
            // append("&by-date=false")
        }

        //Posts are saved to room database, no  need to cache response for index site
        val indexSiteCodeResult = loadHtmlFromUrl(
            url = url,
            isRefresh = true,
            isCachingResult = false,
        )
        val htmlCode = indexSiteCodeResult.getOrNull()
        val exception = indexSiteCodeResult.exceptionOrNull()
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

            exception != null -> {
                return@withContext Result.failure(exception = exception)
            }

            else -> {
                return@withContext Result.failure(
                    exception = NullPointerException("Html code and case are both null")
                )
            }
        }
    }


    /**
     * Tries to save new posts to local database, only if there was no post saved before for
     * given url.
     * @param posts List of posts to be saved, most likely posts loded with [loadPostsFromRemote]
     * @return Count of saved posts
     * @since 1.0.0
     */
    suspend fun saveNewPosts(
        posts: List<PostItem>,
    ): Int {
        val dao = databaseRepo.postDao
        var count = 0
        databaseRepo.withTransaction {
            posts.fastForEach { post ->
                if (!dao.contains(url = post.url)) {
                    dao.insert(item = post)
                    count += 1
                }
            }
        }
        return count
    }


    /**
     * Used for paging, loads posts from local database.
     * @param count Number of posts to load
     * @param page Page number
     * @since 1.1.2
     */
    suspend fun loadFromLocal(
        page: Int,
        count: Int,
    ): List<PostItem> = databaseRepo.withTransaction {
        databaseRepo.postDao.getByDate(
            count = count,
            offset = page * count,
        )
    }


    /**
     * Parses [htmlCode] of post detail into [AdjustedPostData]
     * @param htmlCode Html code of post detail
     * @param url Original url of the post
     * @since 1.0.0
     */
    private suspend fun parsePostDetail(
        htmlCode: String,
        url: String,
    ): Result<AdjustedPostData> {
        val original = ArticleContentTransformer()
            .transform(
                html = htmlCode,
                url = url,
                customTagFilter = { tag, attributes ->
                    ExcludeOption.devBlogExcludeRules.fastForEach { option ->
                        if (!option.filter(tag = tag, attributes = attributes)) {
                            return@transform false
                        }
                    }
                    return@transform true
                })

        try {
            val title = original.elements
                .firstOrNull { it is ArticleElement.Text && it.isTitle } as? ArticleElement.Text
            val headerImage = original.elements
                .firstOrNull { it is ArticleElement.Image } as? ArticleElement.Image
            val date = original.elements
                .firstOrNull() { it is ArticleElement.Text } as? ArticleElement.Text


            val simpleDate = processDate(date = date!!.text.toString())
            val newElements = ArrayList(original.elements).apply {
                remove(element = headerImage)
                remove(element = title)
                remove(element = date)
            }

            val data = original.copy(elements = newElements)
            return Result.success(
                value = AdjustedPostData(
                    postData = data,
                    headerImage = headerImage,
                    date = simpleDate,
                    title = title,
                    contest = data.elements.mapIndexedNotNull { index, element ->
                        if (element is ArticleElement.Text && element.isTitle) {
                            TitleWithOriginalIndex(
                                title = element,
                                originalIndex = index,
                            )
                        } else null
                    }
                )
            )
        } catch (e: NoSuchElementException) {
            e.printStackTrace()
            return Result.failure(exception = e)
        }
    }


    /**
     * Converts index site code into [HtmlArticleData] and then into [List] of [PostItem]
     * @return Result of parsing
     * @since 1.0.0
     */
    private suspend fun parsePosts(
        htmlCode: String
    ): Result<List<PostItem>> {
//        ArticleParser.initialize(
//            isLoggingEnabled = false,
//            areImagesEnabled = true,
//            isTextFormattingEnabled = true,
//            isQueryingTextOutsideTextTags = true,
//        )
        var hasFeaturedItem: Boolean = false
        val links: ArrayList<LinkInfo> = ArrayList()


        val data = ArticleContentTransformer()
            .transform(
                html = htmlCode,
                url = Constants.indexUrl,
                customTagFilter = { tag, attributes ->
                    ExcludeOption.devBlogExcludeRules.fastForEach { option ->
                        if (!option.filter(tag = tag, attributes = attributes)) {
                            return@transform false
                        }
                    }

                    val classes = ArticleContentTransformer.getClasses(attributes = attributes)
                    if (tag == "a" && classes.contains(element = "adb-card__href")) {
                        links.add(element = LinkInfo(href = attributes["href"]))
                    }

                    //Has reatured item outside the list, this needs to be removed
                    if (tag == "div" && classes.contains(element = "featured__wrapper")) {
                        hasFeaturedItem = true
                    }
                    true
                }
            )

        val finalData = data.getPostList(
            links = links,
            hasFeaturedItem = hasFeaturedItem,
        )
        return finalData
    }


    /**
     * Loads html code from given [url]
     * @param url Url of the page
     * @param isRefresh True when request is refresh and needs to be loaded from remote source even
     * when it was cached before.
     * @param isCachingResult Flag indicates that response is cached locally and should be returned from cache.
     * True by default.
     * @since 1.0.0
     */
    private suspend fun loadHtmlFromUrl(
        url: String,
        isRefresh: Boolean = false,
        isCachingResult: Boolean = true,
    ): Result<String> {
        return try {
            val isConnected = AndroidDevBlogApp.isConnectedToInternet

            if (isCachingResult) {
                //We can load from cache
                if (!isRefresh || !isConnected) {
                    //Request is not refresh request or we are not connected to the internet
                    //Only chance to show post is try to load it from cache
                    //TODO add check if cached results are valid (HDR and User Interfaces, cache is invalid and remote returns 404)
                    val fromCache = cacheRepo.getCachedResponse(url = url)
                    if (fromCache != null) {
                        return Result.success(value = fromCache)
                    }
                }
            }

            if (!isConnected) {
                return Result.failure(
                    exception = NotConnectedToInternetException()
                )
            }

            val response: HttpResponse = ktorHttpClient.get(urlString = url)
            val body = response.bodyAsText()

            if (isCachingResult) {
                cacheRepo.saveToCache(url = url, content = body)
            }

            when (response.status.value) {
                in 200..300 -> Result.success(value = body)
                else -> Result.failure(
                    exception = RequestNotSucesfullException(
                        "${response.status.value}",
                        response.status.value,
                    )
                )
            }

        } catch (e: ClientRequestException) {
            e.printStackTrace()
            Result.failure(exception = e)
        } catch (e: ServerResponseException) {
            e.printStackTrace()
            Result.failure(exception = e)
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
     * When html code is parsed, one [PostItem] consist of 4 [ArticleElement]:
     * * Image
     * * Title
     * * Date
     * * Description
     * So, when featured item is presented, is removed by removing first 4 elements from list. Then
     * remaining items are chunked into groups of 4. Each chunk contains data for creating [PostItem],
     * excepts for link which is taken from [links] list.
     * #### Example for [first post](https://android-developers.googleblog.com/search?updated-max=2024-09-30T09:00:00-07:00&max-results=10)
     * * [Image](https://blogger.googleusercontent.com/img/b/R29vZ2xl/AVvXsEikFIJcygIrGJJy0Mb-13Tn9-rk8d29RvMenYTGJj11JZVUr2nt0ZqC1xvElwyVuE_EL3JklDjn-b3muY58rCDXzM-NtSprpY3hAuvlHejDFepHbA39v2TijL-ZNcqqB9jm08Sn-rEZj2eg1Kl22IETuvqr6M9LdG02OMSxmDwCPJRCsWWtWRGUsSNxDjI/s1600/Android-Studio-Social%20%281%29.png)
     * * Title: "Gemini in Android Studio: Code Completion Gains Powerful Model Improvements"
     * * Date: "30 September 2024"
     * * Description: "Posted by Sandhya Mohan  &#8211; Product Manager, Android Studio and Sarmad Hashmi &#8211; Software Engineer, Labs      The Android team believes AI ..."
     * Converts [HtmlArticleData] into [List] of [PostItem].
     * @param links List of links extracted from index site.
     * @param hasFeaturedItem True when featured item is presented in the data. Dev blog sometimes
     * has featured item on the top of the list, which needs to be removed.
     * @return [Result] of conversion
     * @since 1.0.0
     */
    private fun ArticleData.getPostList(
        links: List<LinkInfo>,
        hasFeaturedItem: Boolean,
    ): Result<List<PostItem>> {
        try {
            val newList = ArrayList<ArticleElement>()
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
                val dateString = (sublist[2] as ArticleElement.Text).text
                val date = processDate(date = dateString.toString())!!
                PostItem(
                    image = (sublist[0] as ArticleElement.Image).url,
                    title = (sublist[1] as ArticleElement.Text).text.toString(),
                    date = date,
                    dateTimeStamp = date.timestamp,
                    description = (sublist[3] as ArticleElement.Text).text.toString(),
                    url = links[index].href
                        ?: throw NullPointerException("Unable to extract href from ${links[index]}"),
                    isUnread = true,
                    isFavorite = false,
                )
            }
            return Result.success(value = list)
        } catch (e: ClassCastException) {
            //When class cast error occurs it means that html code of index site might change as the
            //elements are differ from what is expected, or it could be a bug inside jet-article library.
            e.printStackTrace()
            return Result.failure(exception = ContentParseException(cause = e))
        } catch (e: NoSuchElementException) {
            //Occurs when its not possible access lists items on index, mostly because list is empty.
            e.printStackTrace()
            return Result.failure(exception = ContentParseException(cause = e))
        } catch (e: IndexOutOfBoundsException) {
            //When index out of bounds error occurs it means that last chunk is not 4 elements long,
            //indicating that html code of index site might have changed or bug in jet-article library.
            e.printStackTrace()
            return Result.failure(exception = ContentParseException(cause = e))
        }
    }


    /**
     * Converts [date] into [SimpleDate] class. Date is obtained as formatted string, e.g. "3 March 2025",
     * so it needs to be converted into more usable format. This is needed bacouse [date] is obtained
     * formatted.
     * @param date String in format "3 March 2025"
     * @return [SimpleDate] or null
     * @since 1.0.0
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
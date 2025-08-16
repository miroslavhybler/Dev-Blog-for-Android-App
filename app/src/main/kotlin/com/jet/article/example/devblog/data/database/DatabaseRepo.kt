package com.jet.article.example.devblog.data.database

import android.content.Context
import androidx.room.withTransaction
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton


/**
 * @author Miroslav Hýbler <br>
 * created on 20.08.2024
 */
@Singleton
class DatabaseRepo @Inject constructor(
    @ApplicationContext context: Context,
) {

    private val database = LocalDatabase.create(context = context)

    val postDao: LocalDatabase.PostDao
        get() = database.postDao


    suspend fun <R> withTransaction(
        block: suspend () -> R,
    ): R = database.withTransaction(block = block)



    suspend fun updateReadPost(id: Int): Unit = withTransaction {
        postDao.updateReaded(id = id)
    }


    suspend fun updatePostIsFavorite(id: Int, isFavorite: Boolean) : Unit= withTransaction {
        postDao.updateIsFavorite(id = id, isFavorite = isFavorite)

    }
}
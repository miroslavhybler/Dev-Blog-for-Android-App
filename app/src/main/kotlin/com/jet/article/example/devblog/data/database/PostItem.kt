@file:Suppress("RedundantConstructorKeyword")

package com.jet.article.example.devblog.data.database

import androidx.annotation.Keep
import androidx.compose.runtime.mutableStateOf
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.jet.article.data.HtmlElement
import com.jet.article.example.devblog.data.SimpleDate
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import kotlinx.serialization.Serializable

/**
 * @param title Title of the post
 * @param url Url of the post
 * @param description Short description of the post
 * @param image Thumbnail of the post
 * @param isUnread TODO
 * @param isFavorite TODO
 */
@Keep
@Serializable
@Entity(tableName = "posts")
data class PostItem constructor(
    @ColumnInfo(name = "title", typeAffinity = ColumnInfo.TEXT)
    val title: String,
    @ColumnInfo(name = "url", typeAffinity = ColumnInfo.TEXT)
    val url: String,
    @ColumnInfo(name = "date", typeAffinity = ColumnInfo.TEXT)
    val date: SimpleDate,
    @ColumnInfo(name = "date_timestamp", typeAffinity = ColumnInfo.INTEGER)
    val dateTimeStamp: Int,
    @ColumnInfo(name = "description", typeAffinity = ColumnInfo.TEXT)
    val description: String,
    @ColumnInfo(name = "image_url", typeAffinity = ColumnInfo.TEXT)
    val image: String,
    @ColumnInfo(name = "is_unread", typeAffinity = ColumnInfo.INTEGER, defaultValue = "0")
    val isUnread: Boolean,
    @ColumnInfo(name = "is_favorite", typeAffinity = ColumnInfo.INTEGER, defaultValue = "0")
    val isFavorite: Boolean,
) {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id", typeAffinity = ColumnInfo.INTEGER)
    var id: Int = 0


    var isUnreadState: Boolean by mutableStateOf(value = isUnread)


    var isFavoriteState: Boolean by mutableStateOf(value = isFavorite)
}
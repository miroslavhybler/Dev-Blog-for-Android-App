package com.jet.article.example.devblog.data.database

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.ProvidedTypeConverter
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.jet.article.example.devblog.data.Month
import com.jet.article.example.devblog.data.SimpleDate


/**
 * @author Miroslav Hýbler <br>
 * created on 20.08.2024
 */
@Database(
    entities = [
        PostItem::class
    ],
    version = 2,
    exportSchema = true,
    autoMigrations = [
        //Added isUnread flag to postItem
        AutoMigration(from = 1, to = 2)
    ]
)
@TypeConverters(
    value = [
        LocalDatabase.SimpleDateConverter::class,
    ]
)
abstract class LocalDatabase constructor() : RoomDatabase() {

    companion object {
        fun create(context: Context): LocalDatabase {
            return Room.databaseBuilder(
                context = context,
                klass = LocalDatabase::class.java,
                name = "local-database"
            ).fallbackToDestructiveMigration()
                .build()
        }
    }

    val postDao: PostDao
        get() = postDao()


    protected abstract fun postDao(): PostDao


    @Dao
    interface PostDao : BaseDao<PostItem> {
        @Query("SELECT * FROM posts ORDER BY date_timestamp DESC")
        fun getAll(): List<PostItem>


        @Query("SELECT EXISTS(SELECT id FROM posts WHERE url=:url)")
        fun contains(url: String): Boolean

        @Query("SELECT * FROM posts WHERE url=:url")
        fun getByUrl(url: String): PostItem

        @Query("SELECT * FROM posts ORDER BY id DESC LIMIT 1")
        fun getLastPost(): PostItem

        @Query("SELECT last_insert_rowid()")
        fun getLastPostId(): Int


        @Query("UPDATE posts SET is_unread=0 WHERE id=:id")
        fun updateReaded(id: Int)
    }


    interface BaseDao<T> {

        @Insert
        fun insert(item: T)

        @Insert
        fun insert(items: List<T>)
    }


    object SimpleDateConverter {

        @TypeConverter
        fun simpleDateToString(input: SimpleDate): String {
            return "${input.year}-${input.month.value}-${input.dayOfMonth}"
        }


        @TypeConverter
        fun stringToSimpleDate(input: String): SimpleDate {
            val array = input.split('-')
            val year = array[0].toInt()
            val monthNumber = array[1].toInt()
            val day = array[2].toInt()
            val month = Month.entries.first { it.value == monthNumber }
            return SimpleDate(year = year, month = month, dayOfMonth = day)
        }
    }


}
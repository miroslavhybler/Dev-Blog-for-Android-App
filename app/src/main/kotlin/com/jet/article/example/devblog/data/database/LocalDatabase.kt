package com.jet.article.example.devblog.data.database

import android.content.Context
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
    version = 1,
    exportSchema = true,
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


        @Query("SELECT last_insert_rowid()")
        fun getLastPostId(): Int
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
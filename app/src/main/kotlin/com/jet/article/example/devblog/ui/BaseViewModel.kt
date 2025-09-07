package com.jet.article.example.devblog.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jet.article.example.devblog.data.CoreRepo
import com.jet.article.example.devblog.data.SettingsStorage
import com.jet.article.example.devblog.data.database.DatabaseRepo
import com.jet.article.example.devblog.data.database.PostItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject


/**
 * @author Miroslav Hýbler <br>
 * created on 15.08.2024
 */
abstract class BaseViewModel constructor(
    application: Application,
    val settingsStorage: SettingsStorage,
) : AndroidViewModel(
    application
) {

    @Inject
    lateinit var coreRepo: CoreRepo

    @Inject
    lateinit var databaseRepo: DatabaseRepo

    protected val context: Context
        get() = getApplication()


    val settings: Flow<SettingsStorage.Settings>
        get() = settingsStorage.settings



    fun toggleFavoriteItem(
        item: PostItem
    ) {
        viewModelScope.launch {
            databaseRepo.updatePostIsFavorite(
                id = item.id,
                isFavorite = !item.isFavoriteState,
            )
            item.isFavoriteState = !item.isFavoriteState
        }
    }

}
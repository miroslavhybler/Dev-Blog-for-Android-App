package com.jet.article.example.devblog.ui.post

import android.app.Application
import com.jet.article.example.devblog.data.PostContentDiveIndexer
import com.jet.article.example.devblog.data.SettingsStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


/**
 * @author Miroslav Hýbler <br>
 * created on 20.09.2025
 */
@HiltViewModel
class ContestViewModel @Inject constructor(
    application: Application,
    settingsStorage: SettingsStorage,
    postContentDiveIndexer: PostContentDiveIndexer,
) : BasePostViewModel(
    application = application,
    settingsStorage = settingsStorage,
    postContentDiveIndexer = postContentDiveIndexer,
) {


}

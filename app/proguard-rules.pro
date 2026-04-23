-dontwarn org.slf4j.impl.StaticLoggerBinder

-keep class com.jet.tts.*


-keepclassmembers class * extends androidx.work.Worker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}

-keepclassmembers class * extends androidx.work.CoroutineWorker {
        public <init>(android.content.Context, androidx.work.WorkerParameters);
    }

-keepclassmembers class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}

-keepclassmembers class com.jet.article.example.devblog.data.ContentSyncWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}


-dontwarn com.jet.article.ArticleAnalyzer
-dontwarn com.jet.article.ArticleParser$Utils
-dontwarn com.jet.article.ArticleParser
-dontwarn com.jet.article.ui.JetHtmlArticleKt
-dontwarn com.jet.article.ui.LinkClickHandler$LinkCallback
-dontwarn com.jet.article.ui.elements.HtmlImageDefaults
-dontwarn com.jet.article.ui.elements.HtmlImageKt
-dontwarn com.jet.article.ui.elements.HtmlTextBlockKt

# Keep methods called from native code
-keepclassmembers class com.jet.article.core.TagCallback {
    public void onStartTag(java.lang.String, java.util.Map);
    public void onEndTag(java.lang.String);
    public void onText(java.lang.String);
}

# Dev blog for Android App

Dev blog for Android is simple app for keeeping you on track
with [Android Developers blog](https://android-developers.googleblog.com/). App is
using [Adaptive Apis](https://developer.android.com/jetpack/androidx/releases/compose-material3-adaptive)
to maximaze user experience on all devices, with support of Dark Theme and Dynamic Colors.

* `Paging` for pagination of posts
* `WorkManager` for downloading new posts on background
* `ListDetailPaneScaffold` for list of posts and post detail
* `MaterialTheme` for UI/UX
* `Ktor` for http requests
* `Jet-Article` for parsing html content

![Ilustration image](/images/image1.png)


Also, this app is example for [jet-article](https://github.com/miroslavhybler/jet-article) project
showing full power of the library, parsing index site to list of posts and showing post detail using
[Jetpack Compose](https://developer.android.com/compose) and [Material3](https://m3.material.io/).
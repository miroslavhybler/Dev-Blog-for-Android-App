@file:OptIn(ExperimentalMaterial3AdaptiveApi::class)
@file:Suppress("RedundantVisibilityModifier")

package com.jet.article.example.devblog.ui

import androidx.annotation.Keep
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.PaneScaffoldDirective
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.navigation.BackNavigationBehavior
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.jet.article.data.HtmlElement
import com.jet.article.example.devblog.data.database.PostItem
import com.jet.article.example.devblog.ui.home.list.HomeListPane
import com.jet.article.example.devblog.ui.home.post.ContentsScreen
import com.jet.article.example.devblog.ui.home.post.PostEmptyPane
import com.jet.article.example.devblog.ui.home.post.PostScreen
import com.jet.article.example.devblog.ui.settings.AboutLibsScreen
import com.jet.article.example.devblog.ui.settings.AboutScreen
import com.jet.article.example.devblog.ui.settings.ChangelogScreen
import com.jet.article.example.devblog.ui.settings.SettingsScreen
import io.ktor.util.reflect.instanceOf
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass

typealias MutableBackstack = SnapshotStateList<NavKey>
typealias Backstack = List<NavKey>


public val LocalBackstack: ProvidableCompositionLocal<Backstack> = staticCompositionLocalOf(
    defaultFactory = {
        mutableStateListOf()
    }
)


public data class SectionSelectedEvent public constructor(
    val index: Int,
    val element: HtmlElement.Title,
) {

    var isConsumed: Boolean = false
}


/**
 * @author Miroslav Hýbler <br>
 * created on 08.09.2025
 */
@Composable
fun MainNavDisplay() {
    val backstack: MutableBackstack = rememberNavBackStack(
        Route.Home,
    //    Route.EmptyPostPlaceholder,
    )
    val directive = calculatePaneScaffoldDirective(
        windowAdaptiveInfo = currentWindowAdaptiveInfo(),
    )

    var selectedSectionEvent: SectionSelectedEvent? by remember { mutableStateOf(value = null) }



    CompositionLocalProvider(
        LocalBackstack provides backstack
    ) {
        //TODO animations
        NavDisplay(
            modifier = Modifier
                .fillMaxSize()
                .semantics(properties = { testTagsAsResourceId = true }),
            backStack = backstack,
            sceneStrategy = rememberListDetailSceneStrategy(
                backNavigationBehavior = BackNavigationBehavior.PopLatest,
                directive = PaneScaffoldDirective(
                    maxHorizontalPartitions = directive.maxHorizontalPartitions,
                    maxVerticalPartitions = 2,
                    defaultPanePreferredWidth = directive.defaultPanePreferredWidth,
                    verticalPartitionSpacerSize = directive.verticalPartitionSpacerSize,
                    horizontalPartitionSpacerSize = directive.horizontalPartitionSpacerSize,
                    excludedBounds = directive.excludedBounds,
                )
            ),
            onBack = { backstack.pop() },
            entryProvider = entryProvider(
                fallback = {
                    error(message = "Unknown route: $it")
                },
                builder = {
                    entry<Route.Home>(
                        metadata = ListDetailSceneStrategy.listPane()
                    ) {
                        HomeListPane(
                            onNavigate = { route ->
                                backstack.add(element = route)
                            },
                        )
                    }

                    entry<Route.EmptyPostPlaceholder>(
                        metadata = ListDetailSceneStrategy.detailPane(),
                    ) {
                        PostEmptyPane()
                    }

                    entry<Route.Post>(
                        metadata = ListDetailSceneStrategy.detailPane(),
                    ) { route ->
                        PostScreen(
                            onBack = { backstack.pop() },
                            onNavigate = { backstack.add(element = it) },
                            route = route,
                            listState = rememberLazyListState(),
                            selectedSectionEvent = selectedSectionEvent,
                        )
                    }
                    entry<Route.Contest>(
                        metadata = ListDetailSceneStrategy.extraPane(),
                    ) { route ->
                        ContentsScreen(
                            route = route,
                            onSelected = { event ->
                                selectedSectionEvent = event //Setting up new event
                                backstack.pop() //Closing ContentsScreen
                            }
                        )
                    }

                    entry<Route.Settings>(
                        metadata = ListDetailSceneStrategy.listPane(),
                    ) {
                        SettingsScreen(
                            onBack = {
                                backstack.pop()
                            },
                            onNavigate = { route ->
                                backstack.add(element = route)
                            }
                        )
                    }

                    entry<Route.Changelog>(
                        metadata = ListDetailSceneStrategy.detailPane(),
                    ) {
                        ChangelogScreen(
                            onBack = {
                                backstack.pop()
                            },
                        )
                    }
                    entry<Route.About>(
                        metadata = ListDetailSceneStrategy.detailPane(),
                    ) {
                        AboutScreen(
                            onBack = {
                                backstack.pop()
                            },
                        )
                    }
                    entry<Route.AboutLibs>(
                        metadata = ListDetailSceneStrategy.detailPane(),
                    ) {
                        AboutLibsScreen(
                            onBack = {
                                backstack.pop()
                            },
                        )
                    }

                }
            ),
        )
    }
}


@Keep
@Serializable
sealed class Route private constructor() : NavKey {

    @Keep
    @Serializable
    object Home : Route()

    @Keep
    @Serializable
    object EmptyPostPlaceholder : Route()

    @Keep
    @Serializable
    data class Post constructor(
        val item: PostItem,
    ) : Route()


    @Keep
    @Serializable
    data class Contest constructor(
        val item: PostItem,
    ) : Route()


    @Keep
    @Serializable
    object Settings : Route()

    @Keep
    @Serializable
    object Changelog : Route()

    @Keep
    @Serializable
    object About : Route()

    @Keep
    @Serializable
    object AboutLibs : Route()
}


fun MutableBackstack.pop() {
    if (this.size > 1) {
        this.removeLastOrNull()
    }
}

fun <T : NavKey> Backstack.containsEntry(clazz: KClass<T>): Boolean {
    return this.find { entry -> entry.instanceOf(type = clazz) } != null
}
@file:Suppress(
    "RedundantVisibilityModifier", "DataClassPrivateConstructor",
    "MemberVisibilityCanBePrivate", "ConstPropertyName"
)

package com.jet.article.example.devblog.ui

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.adaptive.WindowAdaptiveInfo
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jet.article.example.devblog.isCompat
import com.jet.article.example.devblog.isExpanded
import com.jet.article.example.devblog.isMedium
import com.jet.utils.theme.MaterialColors

private val Green80: Color = Color(color = 0xFF13B041)
private val Green90: Color = Color(color = 0xFF07882B)
private val Green40: Color = Color(color = 0xFF3ddc84)

private val LightColorScheme: ColorScheme = lightColorScheme(
    primary = Green80,
    onPrimary = Color.White,
    primaryContainer = Green90,
    onPrimaryContainer = Color.White,
    secondary = Color(color = 0xFF08677f),
    onSecondary = Color(color = 0xFFffffff),
    secondaryContainer = Color(color = 0xFFC8EBFF),
    onSecondaryContainer = Color(color = 0xFF001f28),
    error = Color(color = 0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(color = 0xFFFFEDED),
    onErrorContainer = Color(color = 0xFF410000),
    background = Color(color = 0xFFEFEFEF),
    onBackground = Color.Black,
    surface = Color(color = 0xFFEFEFEF),
    onSurface = Color.Black,
    surfaceVariant = Color(color = 0xFFE7E0EC),
    onSurfaceVariant = Color(color = 0xFF49454F),
    outline = Color(color = 0xFF79747E),
)


private val DarkColorScheme = darkColorScheme(
    primary = Green80,
    onPrimary = Color.White,
    primaryContainer = Green90,
    onPrimaryContainer = Color.Black,
    secondary = Color(color = 0xFF88d1ec),
    onSecondary = Color(color = 0xFF003544),
    secondaryContainer = Color(color = 0xFF003544),
    onSecondaryContainer = Color(color = 0xFFBDE8FF),
    tertiary = Green80,
    onTertiary = Color.Black,
    tertiaryContainer = Green90,
    onTertiaryContainer = Color.Black,
    error = Color(color = 0xFF420000),
    onError = Color(color = 0xFFFFFFFF),
    errorContainer = Color(color = 0xFF420000),
    onErrorContainer = Color(0xFFFFFFFF),
    background = Color(color = 0xFF181818),
    onBackground = Color(color = 0xFFEFEFEF),
    surface = Color(color = 0xFF181818),
    onSurface = Color.White,
    surfaceVariant = Color.Black,
    onSurfaceVariant = Color(color = 0xFF181818),
    outline = Color(color = 0xFF938F99),
)

@Composable
fun DevBlogAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    isUsingDynamicColors: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        isUsingDynamicColors && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content,
    )
}

object DevBlogAppTheme {

    val colorAndroid: Color
        get() = Green40
}


/**
 * @param topLinePadding Extra content padding between topBar component and content.
 * @param bottomLinePadding Extra content padding between bottomBar component and content.
 * Is also used as extra bottom padding in scrollable containers too.
 * @param sidePadding Horizontal content padding from window edge
 * @see <a href="https://developer.android.com/develop/ui/compose/layouts/adaptive/window-size-classes">WindowSizeClass</a>
 * @author Miroslav Hýbler <br>
 * created on 04.11.2023
 */
public data class Dimensions private constructor(
    val topLinePadding: Dp,
    val bottomLinePadding: Dp,
    val sidePadding: Dp,
) {
    companion object {
        public val default: Dimensions = Dimensions(
            sidePadding = 16.dp,
            bottomLinePadding = 16.dp,
            topLinePadding = 24.dp,
        )
        public val landscape: Dimensions = Dimensions(
            sidePadding = 20.dp,
            bottomLinePadding = 16.dp,
            topLinePadding = 12.dp,
        )
        public val tablet: Dimensions = Dimensions(
            sidePadding = 32.dp,
            bottomLinePadding = 20.dp,
            topLinePadding = 28.dp,
        )


        /**
         * @see <a href="https://developer.android.com/develop/ui/compose/layouts/adaptive/window-size-classes">WindowSizeClass</a>
         */
        fun getForWindow(info: WindowAdaptiveInfo): Dimensions {
            val width = info.windowSizeClass.windowWidthSizeClass
            val height = info.windowSizeClass.windowHeightSizeClass

            if (width.isMedium || width.isExpanded) {
                //Probably landscape or opened flip
                return when {
                    //Tablet landscape
                    height.isMedium -> tablet
                    //Phone landscape
                    height.isCompat -> landscape
                    else -> default
                }
            }

            //Default, phone portrait
            return default
        }
    }
}

@Composable
fun rememberDimensions(): Dimensions {
    val windowInfo = currentWindowAdaptiveInfo()
    return remember(key1 = windowInfo) {
        Dimensions.getForWindow(info = windowInfo)
    }
}


/**
 * Local provider for dimensions, should be applied in top level composable functions
 */
val LocalDimensions: ProvidableCompositionLocal<Dimensions> = compositionLocalOf(
    defaultFactory = Dimensions.Companion::default,
)


data object Routes {
    const val main: String = "main"
    const val settings: String = "settings"
    const val aboutLibs: String = "settings/about-libs"
    const val channelLog: String = "settings/channel-log"
    const val about: String = "settings/about"
}


@Composable
@PreviewLightDark
private fun ThemePreview() {
    DevBlogAppTheme {
        MaterialColors(
            modifier = Modifier
                .background(color = MaterialTheme.colorScheme.background)
        )
    }
}
package com.jet.article.example.devblog.ui.home.list

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.jet.article.example.devblog.R
import com.jet.article.example.devblog.composables.CustomHtmlImage
import com.jet.article.example.devblog.data.database.PostItem
import com.jet.article.example.devblog.isExpanded
import com.jet.article.example.devblog.isMedium
import com.jet.article.example.devblog.ui.LocalBackstack
import com.jet.article.example.devblog.ui.Route
import com.jet.article.example.devblog.ui.colorFavorited
import com.jet.article.example.devblog.ui.containsEntry
import com.jet.article.ui.elements.HtmlImage
import com.jet.article.ui.elements.HtmlTextBlock


/**
 * @author Miroslav Hýbler <br>
 * created on 19.08.2024
 */
@Composable
fun HomeListItem(
    modifier: Modifier = Modifier,
    onOpenPost: (index: Int, item: PostItem) -> Unit,
    onToggleFavorite: (item: PostItem) -> Unit,
    item: PostItem,
    index: Int,
    isSelected: Boolean,
) {
    val backstack = LocalBackstack.current
    val windowInfo = currentWindowAdaptiveInfo()
    val windowWidth = windowInfo.windowSizeClass.windowWidthSizeClass

    val containerColor = if (isSelected)
        MaterialTheme.colorScheme.secondaryContainer
    else
        MaterialTheme.colorScheme.primaryContainer

    val contentColor = if (isSelected)
        MaterialTheme.colorScheme.onSecondaryContainer
    else
        MaterialTheme.colorScheme.onPrimaryContainer


    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .animateContentSize()
    ) {
        val isExpanded by rememberUpdatedState(
            newValue = backstack.containsEntry(clazz = Route.Post::class)
        )

        val isLargeWidth = windowWidth.isExpanded || windowWidth.isMedium
        when {
            isExpanded && isLargeWidth -> {
                HomeListItemRow(
                    modifier = modifier,
                    onOpenPost = onOpenPost,
                    item = item,
                    index = index,
                    containerColor = containerColor,
                    contentColor = contentColor,
                    onToggleFavorite = onToggleFavorite,
                )
            }

            else -> {
                HomeListItemColumn(
                    modifier = modifier,
                    onOpenPost = onOpenPost,
                    item = item,
                    index = index,
                    containerColor = containerColor,
                    contentColor = contentColor,
                    onToggleFavorite = onToggleFavorite,
                )
            }
        }
    }

}


@Composable
private fun HomeListItemColumn(
    modifier: Modifier = Modifier,
    onOpenPost: (index: Int, item: PostItem) -> Unit,
    item: PostItem,
    index: Int,
    containerColor: Color,
    contentColor: Color,
    onToggleFavorite: (item: PostItem) -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = { onOpenPost(index, item) })
            .background(
                color = containerColor,
                shape = MaterialTheme.shapes.medium,
            )
            .padding(
                start = 14.dp,
                end = 14.dp,
                top = 10.dp,
                bottom = 16.dp,
            )
    ) {
        Box(modifier = Modifier.wrapContentSize()) {
            CustomHtmlImage(
                modifier = Modifier,
                url = item.image,
            )

            if (item.isUnreadState) {
                NewPostMark(
                    modifier = Modifier
                        .align(alignment = Alignment.TopStart)
                        .padding(start = 12.dp, top = 8.dp)
                )
            }

            IconButton(
                modifier = Modifier
                    .align(alignment = Alignment.TopEnd)
                    .padding(end = 12.dp, top = 8.dp),
                onClick = { onToggleFavorite(item) },
            ) {
                Icon(
                    painter = painterResource(
                        id = if (item.isFavoriteState)
                            R.drawable.ic_favorite_filled
                        else
                            R.drawable.ic_favorite_outlined
                    ),
                    contentDescription = null,
                    tint = if (item.isFavoriteState)
                        colorFavorited
                    else
                        MaterialTheme.colorScheme.onBackground
                )
            }
        }

        Spacer(modifier = Modifier.height(height = 4.dp))

        Text(
            modifier = Modifier,
            text = item.date.getDateString(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onBackground,
        )

        HtmlTextBlock(
            modifier = Modifier,
            text = item.title,
            key = index,
            style = MaterialTheme.typography.titleLarge,
            color = contentColor,
        )
        HtmlTextBlock(
            modifier = Modifier,
            text = item.description,
            key = index,
            style = MaterialTheme.typography.bodyMedium,
            color = contentColor,
        )
    }
}


@Composable
private fun HomeListItemRow(
    modifier: Modifier = Modifier,
    onOpenPost: (index: Int, item: PostItem) -> Unit,
    item: PostItem,
    index: Int,
    contentColor: Color,
    containerColor: Color,
    onToggleFavorite: (item: PostItem) -> Unit,
) {

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = { onOpenPost(index, item) })
            .background(
                color = containerColor,
                shape = MaterialTheme.shapes.medium,
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(
                    start = 6.dp,
                    end = 6.dp,
                    top = 8.dp,
                    bottom = 8.dp,
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            HtmlImage(
                modifier = Modifier.size(size = 48.dp),
                url = item.image,
                contentScale = ContentScale.Crop,
            )

            Spacer(modifier = Modifier.width(width = 12.dp))

            Column(
                modifier = Modifier
                    .weight(weight = 1f),
            ) {

                Text(
                    modifier = Modifier,
                    text = item.date.getDateString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                )
                HtmlTextBlock(
                    modifier = Modifier,
                    text = item.title,
                    key = index,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    color = contentColor,

                    )

                HtmlTextBlock(
                    modifier = Modifier,
                    text = item.description,
                    key = index,
                    style = MaterialTheme.typography.bodySmall,
                    color = contentColor,
                    maxLines = 1,
                )
            }
        }

        if (item.isUnreadState) {
            NewPostMark(
                modifier = Modifier
                    .align(alignment = Alignment.TopStart)
                    .padding(start = 12.dp, top = 8.dp)
            )
        }
    }
}

@Composable
fun NewPostMark(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .wrapContentSize()
            .background(
                color = MaterialTheme.colorScheme.tertiary,
                shape = CircleShape,
            )
    ) {
        Text(
            modifier = Modifier
                .align(alignment = Alignment.Center)
                .padding(horizontal = 8.dp, vertical = 2.dp),
            text = stringResource(R.string.general_unread),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onTertiary,
        )
    }
}
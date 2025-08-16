package com.jet.article.example.devblog.ui.home.post

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.jet.article.example.devblog.ui.LocalDimensions
import com.jet.article.example.devblog.R

/**
 * @author Miroslav Hýbler <br>
 * created on 29.04.2025
 */
@Composable
fun PostBottomBar(
    modifier: Modifier = Modifier,
) {
    val dimensions = LocalDimensions.current

    BottomAppBar(
        modifier = Modifier
            .background(color = MaterialTheme.colorScheme.background)
            .navigationBarsPadding()
            .padding(bottom = dimensions.bottomLinePadding)
            .then(other = modifier),
        actions = {
            Item(
                iconRes = R.drawable.ic_favorite_outlined,
                onClick = {
                    //TODO
                },
            )

            Item(
                iconRes = R.drawable.ic_tts,
                onClick = {
                    //TODO
                },
            )

        },
    )
}


@Composable
private fun Item(
    modifier: Modifier = Modifier,
    @DrawableRes iconRes: Int,
    onClick: () -> Unit,
    label: String = "",
    isEnabled: Boolean = true,
    colors: IconButtonColors = IconButtonDefaults.iconButtonColors(),
) {


    IconButton(
        modifier = modifier
            .padding(horizontal = 8.dp)
            .size(size = 48.dp),
        onClick = onClick,
        enabled = isEnabled,
        colors = colors,
    ) {
        Icon(
            modifier = Modifier.size(size = 42.dp),
            painter = painterResource(id = iconRes),
            contentDescription = label,
        )
    }
}
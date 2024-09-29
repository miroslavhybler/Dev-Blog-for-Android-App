package com.jet.article.example.devblog.ui.settings

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.jet.article.example.devblog.horizontalPadding
import com.jet.article.example.devblog.ui.LocalDimensions

@Composable
fun SettingsRow(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit = {},
    @DrawableRes iconResId: Int? = null
) {

    SettingsRow(
        modifier = modifier,
        title = title,
        subtitle = subtitle,
        onClick = onClick,
        icon = if (iconResId != null) {
            {
                Icon(
                    painter = painterResource(id = iconResId),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            }
        } else null
    )
}

@Composable
fun SettingsRow(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit = {},
    icon: (@Composable () -> Unit)? = null
) {

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .clickable(onClick = onClick)
                .horizontalPadding()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {

            Column(
                modifier = Modifier.weight(weight = 1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                )

                Text(
                    text = subtitle ?: "",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                )

            }

            if (icon != null) {
                icon()
            }
        }
    }
}
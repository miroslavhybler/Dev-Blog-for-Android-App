package com.jet.article.example.devblog.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jet.article.example.devblog.horizontalPadding
import com.jet.article.example.devblog.ui.LocalDimensions

/**
 * @author Miroslav Hýbler <br>
 * created on 30.08.2024
 */
@Composable
fun SettingsSwitch(
    modifier: Modifier = Modifier,
    title: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit = {},
    subtitle: String? = null,
) {
    val dimensions = LocalDimensions.current
    Row(
        modifier = modifier
            .clickable(onClick = { onCheckedChange(!isChecked) })
            .horizontalPadding()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
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

        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
        )
    }
}
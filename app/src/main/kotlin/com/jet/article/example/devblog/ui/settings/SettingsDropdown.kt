package com.jet.article.example.devblog.ui.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.jet.article.example.devblog.R
import com.jet.article.example.devblog.horizontalPadding

/**
 * @author Miroslav Hýbler <br>
 * created on 30.08.2024
 */
@Composable
fun <T> SettingsDropdown(
    modifier: Modifier = Modifier,
    title: String,
    items: List<T>,
    transform: (T) -> String,
    onSelected: (T) -> Unit,
    subtitle: String? = null,
) {
    var isDropdownMenuOpened by remember { mutableStateOf(value = false) }


    SettingsRow(
        modifier = modifier,
        title = title,
        subtitle = subtitle,
        icon = {
            Box(
                modifier = Modifier
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_dropdown),
                    contentDescription = null,
                    modifier = Modifier
                )
                DropdownMenu(
                    modifier = Modifier
                        .align(alignment = Alignment.TopEnd)
                        .wrapContentWidth(),
                    //     offset = DpOffset(x = 192.dp, y = 0.dp),
                    expanded = isDropdownMenuOpened,
                    onDismissRequest = { isDropdownMenuOpened = false },
                ) {
                    items.forEach { item ->
                        DropdownMenuItem(
                            text = {
                                val text = transform(item)
                                Text(
                                    text = text,
                                    style = MaterialTheme.typography.titleMedium,
                                )
                            },
                            onClick = {
                                isDropdownMenuOpened = false
                                onSelected(item)
                            }
                        )
                    }
                }
            }
        },
        onClick = {
            isDropdownMenuOpened = true
        }
    )
}
package com.jet.article.example.devblog.composables

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jet.article.example.devblog.AndroidDevBlogApp
import com.jet.article.example.devblog.R
import com.jet.article.example.devblog.openEmail
import com.jet.article.example.devblog.openWeb
import com.jet.article.example.devblog.ui.DevBlogAppTheme

/**
 * @author Miroslav Hýbler<br>
 * created on 14.09.2024
 */
private val innerPadding: Dp = 24.dp


@Composable
fun NoConnectionLayout(
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .wrapContentSize()
            .background(color = MaterialTheme.colorScheme.secondaryContainer, shape = CircleShape)
            .padding(vertical = 4.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 4.dp)
    ) {

        Icon(
            modifier = Modifier.size(size = 12.dp),
            painter = painterResource(id = R.drawable.ic_wifi_off),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSecondaryContainer,
        )

        Text(
            text = stringResource(R.string.error_connection),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            fontSize = 9.sp,
        )
    }
}


@Composable
fun ErrorLayout(
    modifier: Modifier = Modifier,
    title: String,
    cause: Throwable?,
    useBackground: Boolean = true,
) {
    if (!AndroidDevBlogApp.isConnectedToInternet) {
        ErrorLayoutNoConnection(
            modifier = modifier,
        )
    } else {
        ErrorLayoutThrowable(
            modifier = modifier,
            title = title,
            cause = cause,
            useBackground = useBackground,
        )
    }


}

@Composable
private fun ErrorLayoutNoConnection(
    modifier: Modifier = Modifier,
) {

    Column(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            modifier = Modifier
                .padding(top = innerPadding)
                .size(size = 48.dp),
            painter = painterResource(id = R.drawable.ic_wifi_off),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.secondary,
        )

        Spacer(modifier = Modifier.height(height = 24.dp))

        Text(
            text = stringResource(id = R.string.error_connection),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.secondary,
        )
    }
}


@Composable
private fun ErrorLayoutThrowable(
    modifier: Modifier = Modifier,
    title: String,
    cause: Throwable?,
    useBackground: Boolean = true,
) {
    val context = LocalContext.current

    val contentColor = if (useBackground)
        MaterialTheme.colorScheme.onErrorContainer
    else
        MaterialTheme.colorScheme.error
    Column(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentSize()
            .background(
                color = if (useBackground)
                    MaterialTheme.colorScheme.errorContainer
                else
                    Color.Unspecified,
                shape = if (useBackground)
                    MaterialTheme.shapes.large
                else
                    RectangleShape
            ),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            modifier = Modifier
                .padding(top = innerPadding)
                .size(size = 48.dp),
            painter = painterResource(id = R.drawable.ic_bug),
            contentDescription = null,
            tint = contentColor,
        )

        Spacer(modifier = Modifier.height(height = 24.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = contentColor,
        )

        Text(
            modifier = Modifier
                .padding(horizontal = innerPadding),
            text = stringResource(R.string.general_error_description),
            style = MaterialTheme.typography.bodyMedium,
            color = contentColor,
            textAlign = TextAlign.Center,
        )

        if (cause != null) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = innerPadding),
                text = cause::class.java.name,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 8.sp,
                lineHeight = 10.sp,
                color = contentColor,
                textAlign = TextAlign.Start,
                maxLines = 1,
            )
        }

        Spacer(modifier = Modifier.height(height = 16.dp))


        ReportButton(
            modifier = Modifier
                .align(alignment = Alignment.Start),
            title = stringResource(R.string.report_on_mail),
            iconRes = R.drawable.ic_email,
            onClick = {
                context.openEmail(
                    email = "miroslav.hybler.development@gmail.com",
                    subject = "Dev Blog for Android Bug",
                    text = cause?.stackTraceToString() ?: "",
                )
            },
            onClickLabel = stringResource(id = R.string.content_desc_report_on_mail),
        )

        ReportButton(
            modifier = Modifier
                .align(alignment = Alignment.Start),
            title = stringResource(R.string.report_on_github),
            iconRes = R.drawable.ic_logo_github,
            onClick = {
                context.openWeb(
                    url = "https://github.com/miroslavhybler/Dev-Blog-for-Android-App/issues/",
                )
            },
            onClickLabel = stringResource(id = R.string.content_desc_report_on_github),
        )

        Spacer(modifier = Modifier.height(height = 32.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = innerPadding,
                    end = innerPadding,
                    bottom = innerPadding
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                modifier = Modifier,
                text = stringResource(R.string.general_error_label),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )

            Spacer(modifier = Modifier.weight(weight = 1f))

            if (cause != null) {
                IconButton(
                    modifier = Modifier,
                    onClick = {
                        val clipboardManager = context.getSystemService(
                            Context.CLIPBOARD_SERVICE
                        ) as ClipboardManager

                        clipboardManager.setPrimaryClip(
                            ClipData.newPlainText(
                                "Stack Trace",
                                cause.stackTraceToString(),
                            )
                        )
                    }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_copy),
                        contentDescription = stringResource(id = R.string.content_desc_copy_stacktrace),
                        tint = contentColor,
                    )
                }
            }
        }
    }
}


@Composable
private fun ReportButton(
    modifier: Modifier = Modifier,
    title: String,
    @DrawableRes iconRes: Int,
    onClick: () -> Unit,
    onClickLabel: String? = null,
) {
    Row(
        modifier = modifier
            .clip(shape = CircleShape)
            .clickable(
                onClick = onClick,
                onClickLabel = onClickLabel,
                role = Role.Button,
            )
            .padding(horizontal = innerPadding, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {

        Icon(
            modifier = Modifier.size(size = 12.dp),
            painter = painterResource(id = iconRes),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onBackground,
        )

        Spacer(modifier = Modifier.width(width = 4.dp))

        Text(
            modifier = Modifier,
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}


@Composable
@PreviewLightDark
private fun ErrorLayoutPreview() {

    DevBlogAppTheme {
        Column(
            Modifier
                .background(color = MaterialTheme.colorScheme.background),
            verticalArrangement = Arrangement.spacedBy(space = 32.dp)
        ) {

            NoConnectionLayout()

            ErrorLayoutNoConnection(
                modifier = Modifier
            )

            ErrorLayoutThrowable(
                title = "Something went wrong",
                cause = IllegalStateException()
            )
        }
    }
}
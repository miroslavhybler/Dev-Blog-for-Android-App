package com.jet.article.example.devblog


/**
 * @author Miroslav Hýbler <br>
 * created on 24.09.2024
 */
class ContentParseException @JvmOverloads constructor(
    message: String? = null,
    cause: Throwable? = null,
) : IllegalStateException(
    message,
    cause,
)
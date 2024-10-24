package com.jet.article.example.devblog


/**
 * @author Miroslav Hýbler <br>
 * created on 22.10.2024
 */
class NotConnectedToInternetException(
    message: String = ""
) : IllegalStateException(message) {
}
package com.jet.article.example.devblog


/**
 * @author Miroslav Hýbler <br>
 * created on 23.10.2024
 */
class RequestNotSucesfullException constructor(
    message: String = "",
   val  code: Int,
) : IllegalStateException(message) {
}
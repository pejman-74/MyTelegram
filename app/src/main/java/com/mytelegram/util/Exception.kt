package com.mytelegram.util

sealed class ApiException : Throwable() {
    class UnknownException(override val message: String? = null) : ApiException()
    class FailedException(override val message: String? = null) : ApiException()
    class ErrorException(override val message: String? = null) : ApiException()
    class ConvertException(override val message: String? = null) : ApiException()
}


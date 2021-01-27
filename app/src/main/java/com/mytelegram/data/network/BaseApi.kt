package com.mytelegram.data.network

import com.mytelegram.util.ApiException
import com.mytelegram.util.stringToJsonObject

/*
* This abstract uses for convert apis response securely.
* if API returns an object, the function convert response to the entered object type.
* if API returns nothing, the function just returns a boolean to sure successfully server did the request or not.
* */

abstract class BaseApi {

    fun <T> safeApiResultConverter(ackData: Array<Any?>, objType: Class<T>): T {
        val methodName = Thread.currentThread().stackTrace[2].methodName

        if (ackData[0] == null || ackData[0].toString().isEmpty())
            throw ApiException.UnknownException()

        val result = ackData[0].toString()

        if (result.startsWith("e:"))
            throw ApiException.ErrorException(result.removePrefix("e:"))

        if (result == "failed")
            throw ApiException.FailedException("$methodName failed")

        try {
            return stringToJsonObject(result, objType)
        } catch (err: Throwable) {
            throw ApiException.ConvertException(err.message)
        }

    }

    fun safeApiResultConverter(ackData: Array<Any?>): Boolean {
        val methodName = Thread.currentThread().stackTrace[2].methodName

        if (ackData[0] == null || ackData[0].toString().isEmpty())
            throw ApiException.FailedException("$methodName failed")

        val result: String = ackData[0].toString()
        if (result == "ok")
            return true

        if (result.startsWith("e:"))
            throw ApiException.ErrorException(result.removePrefix("e:"))

        if (result == "failed")
            throw ApiException.FailedException("$methodName failed")

        return true
    }
}
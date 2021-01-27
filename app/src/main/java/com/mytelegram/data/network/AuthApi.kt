package com.mytelegram.data.network

import com.mytelegram.data.model.MainUser
import com.mytelegram.util.AuthSocket
import io.socket.client.Ack
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/*
* Before user login uses this apis for authenticate user
* */
class AuthApi(private val authSocket: AuthSocket) : BaseApi() {

    init {
        authSocket.getSocket()?.connect()
    }

    suspend fun login(phoneNumber: String, countryCode: String): Boolean =
        suspendCoroutine { cont ->
            authSocket.getSocket()?.emit(
                "verifySmsDetection",
                phoneNumber,
                countryCode,
                Ack { ackData ->
                    try {
                        cont.resume(safeApiResultConverter(ackData))
                    } catch (err: Throwable) {
                        cont.resumeWithException(err)
                    }
                })
        }

    suspend fun tokenValidation(phoneNumber: String, token: String): MainUser =
        suspendCoroutine { cont ->
            authSocket.getSocket()?.emit(
                "tokenValidationDetection",
                phoneNumber,
                token,
                Ack { ackData ->
                    try {
                        val mainUser = safeApiResultConverter(ackData, MainUser::class.java)
                        cont.resume(mainUser)
                    } catch (err: Throwable) {
                        cont.resumeWithException(err)
                    }
                })
        }

}


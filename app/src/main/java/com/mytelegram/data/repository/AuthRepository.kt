package com.mytelegram.data.repository

import com.mytelegram.data.local.Database
import com.mytelegram.data.model.MainUser
import com.mytelegram.data.network.AuthApi
import com.mytelegram.util.AuthSocket
import dagger.hilt.android.scopes.ActivityRetainedScoped
import javax.inject.Inject

@ActivityRetainedScoped
class AuthRepository @Inject constructor(
        private val authSocket: AuthSocket,
        private val authApi: AuthApi,
        private val database: Database,
) : BaseRepository() {

    fun connectToAuthServer(token: String) = authSocket.connect(token)

    fun disconnectFromAuthServer() = authSocket.disconnect()

    suspend fun login(phoneNumber: String, countryCode: String) =
            safeApiCall { authApi.login(phoneNumber, countryCode) }

    suspend fun codeValidation(phoneNumber: String, token: String) =
            safeApiCall { authApi.tokenValidation(phoneNumber, token) }

    suspend fun saveMainUser(mainUser: MainUser) = database.mainUserDao().insert(mainUser)


}
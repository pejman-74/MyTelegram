package com.mytelegram.di.module

import com.mytelegram.data.network.UserApi
import com.mytelegram.util.UserSocket
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/*
* provides globally requirements at all the app life-circle.
* */
@InstallIn(SingletonComponent::class)
@Module
object UserModule {
    @Provides
    @Singleton
    fun provideUserSocket() = UserSocket()

    @Provides
    @Singleton
    fun provideUserSocketApi(userSocket: UserSocket): UserApi = UserApi(userSocket)
}
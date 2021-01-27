package com.mytelegram.di.module

import com.mytelegram.data.network.AuthApi
import com.mytelegram.util.AuthSocket
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped

/*
* provides just for auth fragments.
* uses in nested auth_graph.
* also disposes in authViewModel disposed. because used 'ActivityRetainedComponent'
* */
@InstallIn(ActivityRetainedComponent::class)
@Module
object AuthModule {

    @Provides
    @ActivityRetainedScoped
    fun provideAuthSocket() = AuthSocket()

    @Provides
    @ActivityRetainedScoped
    fun provideAuthSocketApi(authSocket: AuthSocket): AuthApi = AuthApi(authSocket)

}
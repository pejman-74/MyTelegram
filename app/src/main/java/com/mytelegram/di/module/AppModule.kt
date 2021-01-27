package com.mytelegram.di.module

import android.content.Context
import androidx.room.Room
import com.mytelegram.data.local.Database
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/*
* provides globally requirements at all the app life-circle.
* */
@InstallIn(SingletonComponent::class)
@Module
object AppModule {
    @Singleton
    @Provides
    fun provideMessageDatabase(@ApplicationContext context: Context): Database =
        Room.databaseBuilder(context, Database::class.java, "messageDB").build()

}

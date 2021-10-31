package com.efuntikov.contactsapp.di.module

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job

@Module
class RepositoryModule {

    @Provides
    fun provideCoroutineScope(): CoroutineScope {
        return CoroutineScope(Job() + Dispatchers.Main)
    }

    @Provides
    fun provideSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences("efuntikov.contactsapp", Context.MODE_PRIVATE)
    }
}
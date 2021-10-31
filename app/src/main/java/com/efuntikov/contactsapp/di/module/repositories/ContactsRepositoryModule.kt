package com.efuntikov.contactsapp.di.module.repositories

import android.content.Context
import android.content.SharedPreferences
import com.efuntikov.contactsapp.domain.repository.contacts.ContactsRepository
import com.efuntikov.contactsapp.domain.repository.contacts.ContactsRepositoryImpl
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient

@Module
class ContactsRepositoryModule {

    @Provides
    fun provideContactsRepository(context: Context,
                                  gson: Gson,
                                  httpClient: OkHttpClient,
                                  preferences: SharedPreferences): ContactsRepository {
        return ContactsRepositoryImpl(context, gson, httpClient, preferences)
    }
}
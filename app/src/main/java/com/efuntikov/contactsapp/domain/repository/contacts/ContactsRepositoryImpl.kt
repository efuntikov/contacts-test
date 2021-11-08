package com.efuntikov.contactsapp.domain.repository.contacts

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.efuntikov.contactsapp.ContactsViewModel
import com.efuntikov.contactsapp.LOG_TAG
import com.efuntikov.contactsapp.domain.entity.Contact
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*
import java.util.Collections.synchronizedMap

class ContactsRepositoryImpl(
    private val applicationContext: Context,
    private val gson: Gson,
    private val httpClient: OkHttpClient,
    private val preferences: SharedPreferences
) : ContactsRepository {

    private var contacts = synchronizedMap(mutableMapOf<String, Contact>())

    companion object {
        const val CONTACTS_FETCH_TIMESTAMP_PREFS_KEY = "contacts_fetch_timestamp"
        private val DATA_SOURCE_LIST = listOf(
            Pair(
                "generated-01.json",
                "https://raw.githubusercontent.com/SkbkonturMobile/mobile-test-droid/master/json/generated-01.json"
            ), Pair(
                "generated-02.json",
                "https://raw.githubusercontent.com/SkbkonturMobile/mobile-test-droid/master/json/generated-02.json"
            ), Pair(
                "generated-03.json",
                "https://raw.githubusercontent.com/SkbkonturMobile/mobile-test-droid/master/json/generated-03.json"
            )
        )
    }

    @Throws(Exception::class)
    override suspend fun getContacts(forced: Boolean): ContactsMap {
        if (forced) {
            contacts.clear()
            try {
                Log.d(LOG_TAG, "Clearing cached files")
                DATA_SOURCE_LIST.forEach {
                    File(applicationContext.filesDir, it.first).run {
                        if (exists()) {
                            delete()
                        }
                    }
                }
            } catch (ex: SecurityException) {
                Log.e(LOG_TAG, "Failed to clear the sources folder", ex)
            }
        }

        if (contacts.isEmpty()) {
            return fetchContacts()
        }

        return contacts
    }

    @Throws(Exception::class)
    private suspend fun fetchContacts(): ContactsMap {
        return withContext(Dispatchers.Main) {
            val deferres = mutableListOf<Deferred<List<Contact>>>()
            DATA_SOURCE_LIST.forEach {
                deferres.add(async(Dispatchers.IO) { fetchContactByResource(it) })
            }

            val allResourcesDeferred = deferres.awaitAll()
            val result = mutableMapOf<String, Contact>()
            allResourcesDeferred.map { list ->
                list.forEach { contact ->
                    contact.id?.let { id -> result.putIfAbsent(id, contact) }
                }
            }

            return@withContext result
        }
    }

    @Throws(Exception::class)
    private fun fetchContactByResource(contactsResource: Pair<String, String>): List<Contact> {
        var rawBytesResult = getFromFile(contactsResource.first)
        if (rawBytesResult == null) {
            Log.d(LOG_TAG, "Fetching contacts data from network: ${contactsResource.second}")
            val request = Request.Builder().url(contactsResource.second).build()

            val response: Response
            try {
                response = httpClient.newCall(request).execute()
            } catch (ex: IOException) {
                throw Exception("Failed to load contacts from network")
            }

            val result = response.body ?: return emptyList()
            rawBytesResult = result.bytes()

            saveToFile(contactsResource.first, rawBytesResult)
        }

        try {
            val objects = gson.fromJson(rawBytesResult.decodeToString(), Array<Contact>::class.java)
            Log.d(LOG_TAG, "Successfully parsed contacts from ${contactsResource.first}")
            return objects.asList()
        } catch (exception: JsonSyntaxException) {
            Log.e(LOG_TAG, "Failed to parse contacts!", exception)

            throw Exception("Failed to parse contacts!")
        }
    }

    private fun getFromFile(fileName: String): ByteArray? {
        try {
            applicationContext.openFileInput(fileName).use {
                if (it.available() > 0) {
                    Log.d(LOG_TAG, "Loading from file $fileName")
                    return it.readBytes()
                }
            }
        } catch (ex: IOException) {
            Log.e(LOG_TAG, "Failed to read temporary file $fileName, reason: ${ex.message}")
        }

        Log.w(LOG_TAG, "File $fileName doesn't exist yet")
        return null
    }

    private fun saveToFile(fileName: String, data: ByteArray) {
        try {
            applicationContext.openFileOutput(fileName, Context.MODE_PRIVATE).use {
                Log.d(LOG_TAG, "Saving data (size: ${data.size}) to file $fileName")
                it.write(data)
            }
            preferences.edit().putLong(CONTACTS_FETCH_TIMESTAMP_PREFS_KEY, Date().time).apply()
        } catch (ex: IOException) {
            Log.e(
                LOG_TAG,
                "Failed to create/write temporary file $fileName, reason: ${ex.message}", ex
            )
        }
    }
}
package com.efuntikov.contactsapp.domain.repository.contacts

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
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
import java.util.Collections.synchronizedList
import kotlin.jvm.Throws

class ContactsRepositoryImpl(
    private val applicationContext: Context,
    private val gson: Gson,
    private val httpClient: OkHttpClient,
    private val preferences: SharedPreferences
) : ContactsRepository {

    private var contacts = synchronizedList(mutableListOf<Contact>())

    companion object {
        private val DATA_SOURCE_1 =
            Pair(
                "generated-01.json",
                "https://raw.githubusercontent.com/SkbkonturMobile/mobile-test-droid/master/json/generated-01.json"
            )
        private val DATA_SOURCE_2 =
            Pair(
                "generated-02.json",
                "https://raw.githubusercontent.com/SkbkonturMobile/mobile-test-droid/master/json/generated-02.json"
            )
        private val DATA_SOURCE_3 =
            Pair(
                "generated-03.json",
                "https://raw.githubusercontent.com/SkbkonturMobile/mobile-test-droid/master/json/generated-03.json"
            )
    }

    @Throws(Exception::class)
    override suspend fun getContacts(forced: Boolean): List<Contact> {
        if (forced) {
            contacts.clear()
            try {
                File(applicationContext.filesDir, DATA_SOURCE_1.first).delete()
                File(applicationContext.filesDir, DATA_SOURCE_2.first).delete()
                File(applicationContext.filesDir, DATA_SOURCE_3.first).delete()
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
    private suspend fun fetchContacts(): List<Contact> {
        return withContext(Dispatchers.Main) {
            val resource1 = async(Dispatchers.IO) { fetchContactByResource(DATA_SOURCE_1) }
            val resource2 = async(Dispatchers.IO) { fetchContactByResource(DATA_SOURCE_2) }
            val resource3 = async(Dispatchers.IO) { fetchContactByResource(DATA_SOURCE_3) }

            val allResourcesDeferred = awaitAll(resource1, resource2, resource3)
            val result = arrayListOf<Contact>()
            allResourcesDeferred.map { list -> result.addAll(list) }

            return@withContext result
        }
    }

    @Throws(Exception::class)
    private fun fetchContactByResource(contactsResource: Pair<String, String>): List<Contact> {
        Log.d(LOG_TAG, "Contacts fetch started: $contactsResource")

        var rawBytesResult = getFromFile(contactsResource.first)
        if (rawBytesResult == null) {

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

//        return emptyList()
    }

    private fun getFromFile(fileName: String): ByteArray? {
        try {
            val fileInputStream = applicationContext.openFileInput(fileName)
            if (fileInputStream.available() > 0) {
                Log.d(LOG_TAG, "Loading from file $fileName")
                return fileInputStream.readBytes()
            }
        } catch (ex: IOException) {
            Log.e(LOG_TAG, "Failed to read temporary file $fileName, reason: ${ex.message}", ex)
        }

        Log.w(LOG_TAG, "File $fileName doesn't exist yet")
        return null
    }

    private fun saveToFile(fileName: String, data: ByteArray) {
        var contactsFile: FileOutputStream? = null
        try {
            contactsFile = applicationContext.openFileOutput(fileName, Context.MODE_PRIVATE)
            Log.d(LOG_TAG, "Saving data (size: ${data.size}) to file $fileName")
            contactsFile.write(data)
        } catch (ex: IOException) {
            Log.e(
                LOG_TAG,
                "Failed to create/write temporary file $fileName, reason: ${ex.message}", ex
            )
        } finally {
            try {
                contactsFile?.let {
                    it.flush()
                    it.close()
                }
            } catch (ex: IOException) {
                Log.d(LOG_TAG, "Failed to close/flush file stream for $fileName")
            }
        }
    }
}
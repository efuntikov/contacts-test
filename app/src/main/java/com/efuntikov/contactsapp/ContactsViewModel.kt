package com.efuntikov.contactsapp

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.efuntikov.contactsapp.domain.entity.Contact
import com.efuntikov.contactsapp.domain.repository.contacts.ContactsRepository
import com.efuntikov.contactsapp.domain.repository.contacts.ContactsRepositoryImpl.Companion.CONTACTS_FETCH_TIMESTAMP_PREFS_KEY
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

class ContactsViewModel @Inject constructor(
    private val contactsRepository: ContactsRepository,
    private val preferences: SharedPreferences
) : ViewModel() {

    enum class Navigation {
        CONTACT_LIST,
        CONTACT_DETAIL
    }

    companion object {
        const val CONTACT_ID_ARG_KEY = "contactId"
        private const val MINUTE_IN_MILLIS = 60 * 1000
    }

    private val isLoadingVisible: MutableLiveData<Boolean> = MutableLiveData(false)
    private val contacts: MutableLiveData<List<Contact>> = MutableLiveData()
    private val error: MutableLiveData<String> = MutableLiveData()

    private var contactsMap: MutableMap<String, Contact>? = null

    fun isLoadingVisible(): LiveData<Boolean> = isLoadingVisible

    fun getContacts(): LiveData<List<Contact>> = contacts

    fun getError(): LiveData<String> = error

    fun fetchContacts(forced: Boolean = false) {
        var expired = false
        val lastFetchTimestamp = preferences.getLong(CONTACTS_FETCH_TIMESTAMP_PREFS_KEY, -1L)
        if (lastFetchTimestamp != -1L) {
            if (lastFetchTimestamp + MINUTE_IN_MILLIS < Date().time) {
                Log.w(LOG_TAG, "One minute expired, forcing reloading")
                expired = true
            }
        }
        val reload = forced or expired
        if (reload) {
            contacts.value = emptyList()
            contactsMap?.clear()
            loadContacts(reload)
        } else {
            contactsMap?.let { map ->
                if (map.isEmpty()) {
                    loadContacts(reload)
                }
            } ?: loadContacts(reload)
        }
    }

    fun getContactById(contactId: String) = contactsMap?.get(contactId)

    private fun loadContacts(forced: Boolean) {
        isLoadingVisible.value = true
        viewModelScope.launch {
            try {
                (contactsRepository.getContacts(forced) as MutableMap<String, Contact>).let {
                    contactsMap = it
                    contacts.value = it.values.toList()
                }
            } catch (ex: Exception) {
                Log.e(LOG_TAG, "Failed to get contacts", ex)
                error.value = "No network connection"
            } finally {
                isLoadingVisible.value = false
            }
        }
    }

}
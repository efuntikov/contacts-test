package com.efuntikov.contactsapp

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.efuntikov.contactsapp.domain.entity.Contact
import com.efuntikov.contactsapp.domain.repository.contacts.ContactsRepository
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
        private const val MINUTE_IN_MILLIS = 60 * 1000
        private const val CONTACTS_FETCH_TIMESTAMP_PREFS_KEY = "contacts_fetch_timestamp"
    }

    private val isLoadingVisible: MutableLiveData<Boolean> = MutableLiveData(false)
    private val contacts: MutableLiveData<List<Contact>> = MutableLiveData()
    private val navigation: MutableLiveData<Pair<Navigation, String>> = MutableLiveData()
    private val error: MutableLiveData<String> = MutableLiveData()

    fun isLoadingVisible(): LiveData<Boolean> = isLoadingVisible

    fun getContacts(): LiveData<List<Contact>> = contacts

    fun getNavigation(): LiveData<Pair<Navigation, String>> = navigation

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
            loadContacts(reload)
        } else {
            contacts.value?.let {
                if (it.isEmpty()) {
                    loadContacts(reload)
                }
            } ?: loadContacts(reload)
        }
    }

    fun navigate(destination: Navigation, payload: String) {
        navigation.value = Pair(destination, payload)
    }

    private fun loadContacts(forced: Boolean) {
        isLoadingVisible.value = true
        viewModelScope.launch {
            try {
                contacts.value = contactsRepository.getContacts(forced)
                preferences.edit().putLong(CONTACTS_FETCH_TIMESTAMP_PREFS_KEY, Date().time).apply()
            } catch (ex: Exception) {
                Log.e(LOG_TAG, "Failed to get contacts", ex)
                error.value = "No network connection"
            } finally {
                isLoadingVisible.value = false
            }
        }
    }

}
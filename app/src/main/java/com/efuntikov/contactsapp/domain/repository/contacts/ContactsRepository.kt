package com.efuntikov.contactsapp.domain.repository.contacts

import com.efuntikov.contactsapp.domain.entity.Contact

typealias ContactsMap = Map<String, Contact>

interface ContactsRepository {
    suspend fun getContacts(forced: Boolean): ContactsMap
}
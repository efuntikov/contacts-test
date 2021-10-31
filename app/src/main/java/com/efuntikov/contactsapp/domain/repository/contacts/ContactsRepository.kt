package com.efuntikov.contactsapp.domain.repository.contacts

import com.efuntikov.contactsapp.domain.entity.Contact

interface ContactsRepository {
    suspend fun getContacts(forced: Boolean): List<Contact>
}
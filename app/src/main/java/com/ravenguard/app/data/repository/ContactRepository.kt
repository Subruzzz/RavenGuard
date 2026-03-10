package com.ravenguard.app.data.repository

import com.ravenguard.app.data.database.ContactDao
import com.ravenguard.app.data.database.ContactEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ContactRepository @Inject constructor(
    private val dao: ContactDao
) {
    fun getAllContacts(): Flow<List<ContactEntity>> = dao.getAllContacts()
    suspend fun insertContact(contact: ContactEntity) = dao.insertContact(contact)
    suspend fun updateContact(contact: ContactEntity) = dao.updateContact(contact)
    suspend fun deleteContact(contact: ContactEntity) = dao.deleteContact(contact)
}

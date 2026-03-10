package com.ravenguard.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ravenguard.app.data.database.ContactEntity
import com.ravenguard.app.data.repository.ContactRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: ContactRepository
) : ViewModel() {
    val contacts: StateFlow<List<ContactEntity>> = repository.getAllContacts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun save(contact: ContactEntity) = viewModelScope.launch {
        if (contact.id == 0) repository.insertContact(contact) else repository.updateContact(contact)
    }

    fun delete(contact: ContactEntity) = viewModelScope.launch { repository.deleteContact(contact) }
}

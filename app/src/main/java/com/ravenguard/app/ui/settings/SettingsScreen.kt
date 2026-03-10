package com.ravenguard.app.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ravenguard.app.data.database.ContactEntity

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val contacts by viewModel.contacts.collectAsStateWithLifecycle()
    var showDialog by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<ContactEntity?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(MaterialTheme.colorScheme.background, MaterialTheme.colorScheme.surface)))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Emergency Contacts", style = MaterialTheme.typography.headlineSmall)
        Button(onClick = { if (contacts.size < 5) { editing = null; showDialog = true } }) { Text("Add Contact") }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(contacts, key = { it.id }) { contact ->
                ContactItem(
                    contact = contact,
                    onEdit = { editing = contact; showDialog = true },
                    onDelete = { viewModel.delete(contact) }
                )
            }
        }
    }

    if (showDialog) {
        AddContactDialog(contact = editing, onDismiss = { showDialog = false }) {
            viewModel.save(it)
            showDialog = false
        }
    }
}

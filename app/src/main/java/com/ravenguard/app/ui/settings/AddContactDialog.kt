package com.ravenguard.app.ui.settings

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.ravenguard.app.data.database.ContactEntity

@Composable
fun AddContactDialog(
    contact: ContactEntity? = null,
    onDismiss: () -> Unit,
    onSave: (ContactEntity) -> Unit
) {
    var name by remember { mutableStateOf(contact?.name ?: "") }
    var phone by remember { mutableStateOf(contact?.phoneNumber ?: "") }
    var message by remember { mutableStateOf(contact?.message ?: "") }
    val valid = name.isNotBlank() && phone.length >= 8 && message.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (contact == null) "Add Contact" else "Edit Contact") },
        text = {
            androidx.compose.foundation.layout.Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") })
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone Number") })
                OutlinedTextField(value = message, onValueChange = { message = it }, label = { Text("Emergency Message") })
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (valid) {
                    onSave(ContactEntity(id = contact?.id ?: 0, name = name.trim(), phoneNumber = phone.trim(), message = message.trim()))
                }
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

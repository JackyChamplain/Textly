package com.example.messageapp.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.messageapp.contact.ContactViewModel
import com.example.messageapp.utilities.SettingsViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.foundation.combinedClickable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.compose.ui.graphics.Color
import com.example.messageapp.roomdb.Contact
import com.example.messageapp.roomdb.ContactGroup

@Composable
fun Home(navController: NavController, contactViewModel: ContactViewModel, settingsViewModel: SettingsViewModel) {
    var selectedGroup by remember { mutableStateOf<ContactGroup?>(null) }
    val context = LocalContext.current
    val contacts by contactViewModel.contacts.collectAsState(initial = emptyList())

    // Filtered contacts based on the selected group
    val contactsToDisplay by remember(contacts, selectedGroup) {
        derivedStateOf {
            val filtered = if (selectedGroup == null) {
                contacts
            } else {
                contacts.filter { it.group == selectedGroup }
            }
            // Explicitly specify the type for the comparator
            filtered.sortedWith(
                compareByDescending<Contact> { it.isPinned }.thenBy { it.name }
            )
        }
    }

    var showPasswordDialog by remember { mutableStateOf<Contact?>(null) }
    var newPassword by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Contacts",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(16.dp),
            fontSize = settingsViewModel.fontSize.value.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Group Filter Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            // Business button
            Button(
                onClick = { selectedGroup = if (selectedGroup == ContactGroup.BUSINESS) null else ContactGroup.BUSINESS },
                modifier = Modifier.padding(8.dp)
            ) {
                Text("Business")
            }
            // Personal button
            Button(
                onClick = { selectedGroup = if (selectedGroup == ContactGroup.PERSONAL) null else ContactGroup.PERSONAL },
                modifier = Modifier.padding(8.dp)
            ) {
                Text("Personal")
            }
            // Spam button
            Button(
                onClick = { selectedGroup = if (selectedGroup == ContactGroup.SPAM) null else ContactGroup.SPAM },
                modifier = Modifier.padding(8.dp)
            ) {
                Text("Spam")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Contact list
        LazyColumn {
            items(contactsToDisplay) { contact ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        // Long press to pin chat/contact
                        .combinedClickable(
                            onClick = { navController.navigate("chat/${contact.id}") },
                            onLongClick = {
                                // Create a new contact instance with updated pin status
                                val updatedContact = contact.copy(isPinned = !contact.isPinned)
                                contactViewModel.addContact(updatedContact) // This will replace due to REPLACE strategy
                                Toast.makeText(
                                    context,
                                    if (updatedContact.isPinned) "Pinned chat with ${contact.name}" else "Unpinned chat",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        )
                        .padding(8.dp),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(modifier = Modifier.weight(1f)) {
                            // Moved emojis to the left of the name
                            if (contact.isPinned) {
                                Text("📌", modifier = Modifier.padding(end = 4.dp))
                            }
                            if (contact.isBlocked) {
                                Text("🚫", modifier = Modifier.padding(end = 4.dp))
                            }
                            Text(
                                text = "${contact.name} - ${contact.group}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontSize = settingsViewModel.fontSize.value.sp,
                                fontWeight = if (contact.isPinned) FontWeight.Bold else FontWeight.Normal
                            )
                        }

                        // Block/Unblock button
                        IconButton(onClick = {
                            if (contact.isBlocked) {
                                contactViewModel.unblockContact(contact.id)
                                Toast.makeText(context, "Contact unblocked", Toast.LENGTH_SHORT).show()
                            } else {
                                contactViewModel.blockContact(contact.id)
                                Toast.makeText(context, "Contact blocked", Toast.LENGTH_SHORT).show()
                            }
                        }) {
                            Icon(
                                if (contact.isBlocked) Icons.Default.Clear else Icons.Default.Check,
                                contentDescription = if (contact.isBlocked) "Unblock" else "Block",
                                tint = if (contact.isBlocked) Color.Red else Color.Gray
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(onClick = {
                            // Show dialog or inline TextField
                            showPasswordDialog = contact // set the contact to lock
                        }) {
                            Icon(Icons.Default.Lock, contentDescription = "Set Password")
                        }
                        IconButton(onClick = { contactViewModel.removeContact(contact) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Remove Contact")
                        }
                    }
                }
            }
        }

        if (showPasswordDialog != null) {
            AlertDialog(
                onDismissRequest = { showPasswordDialog = null },
                confirmButton = {
                    TextButton(onClick = {
                        showPasswordDialog?.let { contact ->
                            val updatedContact = contact.copy(
                                password = newPassword,
                                hasPassword = true
                            )
                            contactViewModel.addContact(updatedContact) // This will replace due to REPLACE strategy
                        }
                        newPassword = ""
                        showPasswordDialog = null
                    }) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        newPassword = ""
                        showPasswordDialog = null
                    }) {
                        Text("Cancel")
                    }
                },
                title = { Text("Set Password for ${showPasswordDialog?.name}") },
                text = {
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        placeholder = { Text("Enter password") }
                    )
                }
            )
        }
    }
}
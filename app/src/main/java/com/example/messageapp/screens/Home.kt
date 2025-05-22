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
import com.example.messageapp.contact.ContactGroup
import com.example.messageapp.contact.ContactViewModel
import com.example.messageapp.utilities.SettingsViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.foundation.combinedClickable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.compose.material.icons.filled.Lock
import com.example.messageapp.contact.Contact

@Composable
fun Home(navController: NavController, contactViewModel: ContactViewModel, settingsViewModel: SettingsViewModel) {
    var selectedGroup by remember { mutableStateOf<ContactGroup?>(null) }
    val context = LocalContext.current

    // Filtered contacts based on the selected group
    val contactsToDisplay by remember {
        derivedStateOf {
            val filtered = if (selectedGroup == null) {
                contactViewModel.contacts
            } else {
                contactViewModel.contacts.filter { it.group == selectedGroup }
            }
            filtered.sortedWith(compareByDescending<com.example.messageapp.contact.Contact> { it.isPinned }.thenBy { it.name })
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
            fontSize = settingsViewModel.fontSize.floatValue.sp
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
                                contact.isPinned = !contact.isPinned
                                Toast.makeText(
                                    context,
                                    if (contact.isPinned) "Pinned chat with ${contact.name}" else "Unpinned chat",
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
                            Text(
                                text = "${contact.name} - ${contact.group}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontSize = settingsViewModel.fontSize.floatValue.sp,
                                fontWeight = if (contact.isPinned) FontWeight.Bold else FontWeight.Normal
                            )
                            if (contact.isPinned) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("📌") // Pinned chats/contacts marked with pin emoji
                            }
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
                        showPasswordDialog?.let {
                            it.password = newPassword
                            it.hasPassword = true
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

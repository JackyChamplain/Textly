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

@Composable
fun Home(navController: NavController, contactViewModel: ContactViewModel, settingsViewModel: SettingsViewModel) {
    var selectedGroup by remember { mutableStateOf<ContactGroup?>(null) }

    // Filtered contacts based on the selected group
    val contactsToDisplay = remember(selectedGroup) {
        if (selectedGroup == null) {
            contactViewModel.contacts
        } else {
            contactViewModel.contacts.filter { it.group == selectedGroup }
        }
    }

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
                        .padding(8.dp)
                        .clickable { navController.navigate("chat/${contact.id}") },
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${contact.name} - ${contact.group}",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f),
                            fontSize = settingsViewModel.fontSize.floatValue.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(onClick = { contactViewModel.removeContact(contact) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Remove Contact")
                        }
                    }
                }
            }
        }
    }
}

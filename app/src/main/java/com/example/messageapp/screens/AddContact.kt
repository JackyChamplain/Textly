package com.example.messageapp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.messageapp.data.Contact
import com.example.messageapp.data.ContactGroup
import com.example.messageapp.data.ContactViewModel
import com.example.messageapp.viewmodel.SettingsViewModel

@Composable
fun AddContact(
    navController: NavController,
    contactViewModel: ContactViewModel,
    settingsViewModel: SettingsViewModel
) {
    var contactName by remember { mutableStateOf("") }
    var phoneNum by remember { mutableStateOf("") }
    var selectedGroup by remember { mutableStateOf(ContactGroup.PERSONAL) } // Default selection

    Box(
        modifier = Modifier.fillMaxSize().background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Add a contact",
                color = Color.Black,
                fontSize = settingsViewModel.fontSize.value.sp
            )
            Spacer(modifier = Modifier.height(20.dp))

            TextField(
                value = contactName,
                onValueChange = { contactName = it },
                label = { Text("Name", fontSize = settingsViewModel.fontSize.value.sp) },
                textStyle = TextStyle(fontSize = settingsViewModel.fontSize.value.sp)
            )
            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = phoneNum,
                onValueChange = { phoneNum = it },
                label = { Text("Phone Number", fontSize = settingsViewModel.fontSize.value.sp) },
                textStyle = TextStyle(fontSize = settingsViewModel.fontSize.value.sp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Group Selection
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ContactGroup.values().forEach { group ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = selectedGroup == group,
                            onClick = { selectedGroup = group }
                        )
                        Text(
                            text = group.name.lowercase().replaceFirstChar { it.uppercase() },
                            fontSize = settingsViewModel.fontSize.value.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Save Contact Button
            Button(onClick = {
                if (contactName.isNotBlank() && phoneNum.isNotBlank()) {
                    contactViewModel.addContact(Contact(name = contactName, phoneNumber = phoneNum, group = selectedGroup))
                    navController.navigate("home")
                }
            }) {
                Text("Save Contact", fontSize = settingsViewModel.fontSize.value.sp)
            }

            // Cancel Button
            Button(onClick = { navController.navigate("home") }) {
                Text("Cancel", fontSize = settingsViewModel.fontSize.value.sp)
            }
        }
    }
}
package com.example.messageapp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.messageapp.utilities.SettingsViewModel

@Composable
fun Settings(navController: NavController,
                 settingsViewModel: SettingsViewModel = viewModel()) {
    var fontSizeInput by remember { mutableStateOf(settingsViewModel.fontSize.value.toString()) }

    Box(
        modifier = Modifier.fillMaxSize().background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Settings",
                Modifier.padding(10.dp),
                color = Color.Black,
                fontSize = settingsViewModel.fontSize.value.sp
            )
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Current Font Size: ${settingsViewModel.fontSize.value}",
                Modifier.padding(20.dp),
                color = Color.Black,
                fontSize = settingsViewModel.fontSize.value.sp

            )

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = fontSizeInput,
                onValueChange = { fontSizeInput = it },
                label = { Text("Enter Font Size") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.padding(16.dp)
            )

            Button(onClick = {
                fontSizeInput.toFloatOrNull()?.let {
                    settingsViewModel.setFontSize(it)
                    navController.navigate("home")
                }
            }) {
                Text("Apply")
            }
            Button(onClick = {
                fontSizeInput.toFloatOrNull()?.let {
                    settingsViewModel.setFontSize(18f)
                    navController.navigate("home")
                }
            }) {
                Text("Default")
            }
        }
    }
}

package com.example.messageapp

import android.Manifest
import android.content.Context
import android.telephony.SmsManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import com.example.messageapp.utilities.Message
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.*
import com.example.messageapp.contact.ContactViewModel
import com.example.messageapp.utilities.SettingsViewModel
import com.example.messageapp.screens.AddContact
import com.example.messageapp.screens.Home
import com.example.messageapp.screens.Settings
import com.example.messageapp.ui.theme.MessageAPPTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MessageAPPTheme(dynamicColor = false, darkTheme = false) {
                Surface(color = Color.White) {
                    val settingsViewModel: SettingsViewModel = viewModel() // Create the ViewModel
                    AppContent(settingsViewModel = settingsViewModel) // Pass it to AppContent
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppContent(settingsViewModel: SettingsViewModel) { // Receive SettingsViewModel as a parameter
    val navController = rememberNavController()
    val contactViewModel: ContactViewModel = viewModel()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerContent = {
            ModalDrawerSheet(modifier = Modifier.background(Color.LightGray)) {
                Spacer(modifier = Modifier.height(16.dp))
                Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    listOf("Home", "Settings", "Add Contact").forEach { screen ->
                        Text(
                            text = screen,
                            fontSize = settingsViewModel.fontSize.value.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable {
                                    navController.navigate(screen.lowercase().replace(" ", ""))
                                    scope.launch { drawerState.close() }
                                }
                                .padding(12.dp)
                        )
                    }
                }
            }
        },
        drawerState = drawerState
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Textly", fontSize = settingsViewModel.fontSize.value.sp) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    actions = {
                        IconButton(onClick = { navController.navigate("addcontact") }) {
                            Icon(Icons.Default.Add, contentDescription = "Add")
                        }
                    }
                )
            }
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues)) {
                NavHost(navController = navController, startDestination = "home") {
                    composable("home") { Home(navController, contactViewModel, settingsViewModel = settingsViewModel) }
                    composable("addcontact") { AddContact(navController, contactViewModel, settingsViewModel = settingsViewModel) }
                    composable("settings") { Settings(navController = navController,settingsViewModel = settingsViewModel) }
                    composable("chat/{contactId}") { backStackEntry ->
                        val contactId = backStackEntry.arguments?.getString("contactId") ?: ""
                        ChatScreen(contactId = contactId, navController = navController, contactViewModel = contactViewModel)
                    }
                }
            }
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    contactId: String,
    navController: NavController,
    contactViewModel: ContactViewModel,
) {
    var messageText by remember { mutableStateOf("") }
    val context = LocalContext.current  // Correct usage of LocalContext
    val contact = contactViewModel.contacts.find { it.id == contactId }

    // Launcher for requesting permission
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (!granted) {
            Toast.makeText(context, "SMS permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        // Request permission to send SMS when the screen is displayed
        launcher.launch(Manifest.permission.SEND_SMS)
    }

    // Handle sending SMS and adding the message
    fun sendMessage() {
        if (messageText.isNotBlank() && contact != null) {
            // Send SMS if permission granted
            try {
                val smsManager = SmsManager.getDefault()
                smsManager.sendTextMessage(contact.phoneNumber, null, messageText, null, null)
                contactViewModel.addMessageToContact(context,contact.id, messageText) // Add to local contact messages list
                messageText = "" // Reset message input field
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to send SMS", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // UI Layout
    Column(modifier = Modifier.fillMaxSize()) {
        if (contact != null) {
            TopAppBar(
                title = { Text("${contact.name} (${contact.phoneNumber})") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp),
                contentPadding = PaddingValues(8.dp)
            ) {
                items(contact.messages) { message ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .background(Color.LightGray),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "You: ",
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = message.content,
                            modifier = Modifier.weight(1f).padding(8.dp)
                        )
                    }
                }
            }

            Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                BasicTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Send
                    ),
                    keyboardActions = KeyboardActions(
                        onSend = { sendMessage() }
                    ),
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = { sendMessage() }) {
                    Icon(Icons.Default.Send, contentDescription = "Send")
                }
            }
        }
    }
}
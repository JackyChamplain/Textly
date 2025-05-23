package com.example.messageapp

import android.Manifest
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.telephony.SmsManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.*
import com.example.messageapp.contact.ContactViewModel
import com.example.messageapp.roomdb.AppDatabase
import com.example.messageapp.roomdb.Contact
import com.example.messageapp.roomdb.Message
import com.example.messageapp.screens.AddContact
import com.example.messageapp.screens.Home
import com.example.messageapp.screens.Settings
import com.example.messageapp.ui.theme.MessageAPPTheme
import com.example.messageapp.utilities.SettingsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MessageAPPTheme(dynamicColor = false, darkTheme = false) {
                Surface(color = Color.White) {
                    val settingsViewModel: SettingsViewModel = viewModel()
                    AppContent(settingsViewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppContent(settingsViewModel: SettingsViewModel) {
    val navController = rememberNavController()
    val contactViewModel: ContactViewModel = viewModel()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    ContactViewModel.ContactViewModelProvider.init(contactViewModel)

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
                    composable("home") {
                        Home(navController, contactViewModel, settingsViewModel)
                    }
                    composable("addcontact") {
                        AddContact(navController, contactViewModel, settingsViewModel)
                    }
                    composable("settings") {
                        Settings(navController = navController, settingsViewModel = settingsViewModel)
                    }
                    composable("chat/{contactId}") { backStackEntry ->
                        val contactId = backStackEntry.arguments?.getString("contactId") ?: ""
                        val context = LocalContext.current
                        val db = remember { AppDatabase.getDatabase(context) }

                        var contact by remember { mutableStateOf<Contact?>(null) }
                        var isPasswordVerified by remember { mutableStateOf(false) }

                        LaunchedEffect(contactId) {
                            contact = db.contactDao().getContactById(contactId)
                        }

                        contact?.let {
                            if (it.hasPassword && !isPasswordVerified) {
                                PasswordGate(contactId = contactId) {
                                    isPasswordVerified = true
                                }
                            } else {
                                ChatScreen(contact = it, navController = navController)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PasswordGate(contactId: String, onSuccess: () -> Unit) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    var contact by remember { mutableStateOf<Contact?>(null) }

    var input by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }

    LaunchedEffect(contactId) {
        contact = db.contactDao().getContactById(contactId)
    }

    contact?.let {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Enter password for ${it.name}")
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = input,
                onValueChange = {
                    input = it
                    error = false
                },
                label = { Text("Password") },
                isError = error,
                singleLine = true,
                visualTransformation = PasswordVisualTransformation()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                if (input == it.password) {
                    onSuccess()
                } else {
                    error = true
                }
            }) {
                Text("Enter")
            }
            if (error) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Incorrect password", color = Color.Red)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(contact: Contact, navController: NavController) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val messageDao = db.messageDao()

    var messageText by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }

    val messages by messageDao.getMessagesForContact(contact.id).collectAsState(initial = emptyList())
    val filteredMessages = messages.filter {
        it.content.contains(searchQuery, ignoreCase = true)
    }

    val smsPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (!granted) {
            Toast.makeText(context, "SMS permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    var messageToDelete by remember { mutableStateOf<Message?>(null) }

    LaunchedEffect(Unit) {
        smsPermissionLauncher.launch(Manifest.permission.SEND_SMS)
    }

    fun sendMessage() {
        if (messageText.isNotBlank()) {
            val currentMessageContent = messageText

            try {
                val smsManager = SmsManager.getDefault()
                smsManager.sendTextMessage(contact.phoneNumber, null, currentMessageContent, null, null)

                CoroutineScope(Dispatchers.IO).launch {
                    messageDao.insert(
                        Message(
                            contactId = contact.id,
                            senderId = "You",
                            content = currentMessageContent,
                            timestamp = System.currentTimeMillis(),
                            isSent = true,
                            isDelivered = false,
                            isFailed = false
                        )
                    )
                }
                messageText = ""
            } catch (e: Exception) {
                CoroutineScope(Dispatchers.IO).launch {
                    messageDao.insert(
                        Message(
                            contactId = contact.id,
                            senderId = "You",
                            content = currentMessageContent,
                            timestamp = System.currentTimeMillis(),
                            isSent = false,
                            isDelivered = false,
                            isFailed = true
                        )
                    )
                }
                Toast.makeText(context, "Failed to send SMS: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun deleteMessage(message: Message) {
        CoroutineScope(Dispatchers.IO).launch {
            messageDao.delete(message)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("${contact.name} (${contact.phoneNumber})") },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            }
        )

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search messages...") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            singleLine = true
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(8.dp),
            contentPadding = PaddingValues(8.dp)
        ) {
            items(filteredMessages) { message ->
                // Long click to trigger delete confirmation
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .combinedClickable(
                        onClick = {},
                        onLongClick = { messageToDelete = message }
                    )
                ) {
                    MessageItem(message)
                }
            }
        }

        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            BasicTextField(
                value = messageText,
                onValueChange = { messageText = it },
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { sendMessage() }),
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = { sendMessage() }) {
                Icon(Icons.Default.Send, contentDescription = "Send")
            }
        }

        // Confirmation dialog for deleting a message
        if (messageToDelete != null) {
            AlertDialog(
                onDismissRequest = { messageToDelete = null },
                title = { Text("Delete Message") },
                text = { Text("Are you sure you want to delete this message?") },
                confirmButton = {
                    TextButton(onClick = {
                        messageToDelete?.let { deleteMessage(it) }
                        messageToDelete = null
                    }) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { messageToDelete = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun MessageItem(message: Message) {
    val statusIcon = when {
        message.isFailed -> "❌ Failed"
        message.isDelivered -> "✅ Delivered"
        message.isSent -> "📤 Sent"
        else -> "🟡"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(Color(0xFFEFEFEF))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = statusIcon,
            modifier = Modifier.padding(end = 8.dp)
        )
        Column {
            Text(text = message.content)
            Text(
                text = "From: ${message.senderId}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun MyScreen() {
    val configuration = LocalConfiguration.current
    when (configuration.orientation) {
        Configuration.ORIENTATION_LANDSCAPE -> {
        }
        Configuration.ORIENTATION_PORTRAIT -> {
        }
    }
}


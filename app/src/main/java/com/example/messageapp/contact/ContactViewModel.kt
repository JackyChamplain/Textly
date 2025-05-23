package com.example.messageapp.contact

import android.app.Activity
import android.app.Application
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.telephony.SmsManager
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.messageapp.roomdb.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ContactViewModel : AndroidViewModel {

    private lateinit var contactDao: ContactDao
    private lateinit var messageDao: MessageDao

    constructor(application: Application) : super(application) {
        val db = AppDatabase.getDatabase(application)
        contactDao = db.contactDao()
        messageDao = db.messageDao()
    }

    constructor(
        application: Application,
        injectedContactDao: ContactDao,
        injectedMessageDao: MessageDao
    ) : super(application) {
        contactDao = injectedContactDao
        messageDao = injectedMessageDao
    }

    var lastSentMessage: String? = null

    val contacts: Flow<List<Contact>> by lazy {
        contactDao.getAllContacts()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    fun addContact(contact: Contact) {
        viewModelScope.launch {
            contactDao.insert(contact)
        }
    }

    fun removeContact(contact: Contact) {
        viewModelScope.launch {
            contactDao.delete(contact)
        }
    }

    fun addMessageToContact(
        context: Context,
        contactId: String,
        messageContent: String,
        senderId: String = "You"
    ) {
        viewModelScope.launch {
            val contact = contactDao.getContactById(contactId)
            if (contact != null) {
                try {
                    val message = Message(
                        contactId = contact.id,
                        senderId = senderId,
                        content = messageContent
                    )
                    val messageId = message.id
                    messageDao.insert(message)

                    if (senderId == "You") {
                        val smsManager = SmsManager.getDefault()

                        val sentIntent = Intent("SMS_SENT").putExtra("messageId", messageId)
                        val deliveredIntent = Intent("SMS_DELIVERED").putExtra("messageId", messageId)

                        val sentPI = PendingIntent.getBroadcast(context, 0, sentIntent, PendingIntent.FLAG_IMMUTABLE)
                        val deliveredPI = PendingIntent.getBroadcast(context, 0, deliveredIntent, PendingIntent.FLAG_IMMUTABLE)

                        ContextCompat.registerReceiver(context, object : BroadcastReceiver() {
                            override fun onReceive(ctx: Context?, intent: Intent?) {
                                if (resultCode == Activity.RESULT_OK) {
                                    Toast.makeText(context, "SMS sent", Toast.LENGTH_SHORT).show()
                                    intent?.getStringExtra("messageId")?.let { id ->
                                        viewModelScope.launch {
                                            messageDao.updateSentStatus(id, true, System.currentTimeMillis())
                                        }
                                    }
                                }
                            }
                        }, IntentFilter("SMS_SENT"), ContextCompat.RECEIVER_NOT_EXPORTED)

                        ContextCompat.registerReceiver(context, object : BroadcastReceiver() {
                            override fun onReceive(ctx: Context?, intent: Intent?) {
                                if (resultCode == Activity.RESULT_OK) {
                                    Toast.makeText(context, "SMS delivered", Toast.LENGTH_SHORT).show()
                                    intent?.getStringExtra("messageId")?.let { id ->
                                        viewModelScope.launch {
                                            messageDao.updateDeliveryStatus(id, true, System.currentTimeMillis())
                                        }
                                    }
                                }
                            }
                        }, IntentFilter("SMS_DELIVERED"), ContextCompat.RECEIVER_NOT_EXPORTED)

                        smsManager.sendTextMessage(contact.phoneNumber, null, messageContent, sentPI, deliveredPI)
                    }

                    lastSentMessage = messageContent
                } catch (e: Exception) {
                    Log.e("SMS", "Failed to send SMS", e)
                }
            }
        }
    }

    fun deleteMessage(message: Message) {
        viewModelScope.launch {
            messageDao.delete(message)
        }
    }

    fun getMessagesForContact(contactId: String): Flow<List<Message>> {
        return messageDao.getMessagesForContact(contactId)
    }

    fun updateContactGroup(contactId: String, newGroup: ContactGroup) {
        viewModelScope.launch {
            val contact = contactDao.getContactById(contactId)
            if (contact != null && contact.group != newGroup) {
                contactDao.insert(contact.copy(group = newGroup))
            }
        }
    }

    fun blockContact(contactId: String) {
        viewModelScope.launch {
            val contact = contactDao.getContactById(contactId)
            contact?.let {
                contactDao.insert(it.copy(isBlocked = true))
            }
        }
    }

    fun unblockContact(contactId: String) {
        viewModelScope.launch {
            val contact = contactDao.getContactById(contactId)
            contact?.let {
                contactDao.insert(it.copy(isBlocked = false))
            }
        }
    }

    object ContactViewModelProvider {
        private var viewModel: ContactViewModel? = null

        fun init(vm: ContactViewModel) {
            viewModel = vm
        }

        fun get(): ContactViewModel {
            return viewModel ?: throw IllegalStateException("ViewModel not initialized")
        }
    }
}

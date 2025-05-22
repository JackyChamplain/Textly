package com.example.messageapp.contact

import android.app.Activity
import android.app.Application
import android.content.Context
import android.telephony.SmsManager
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.messageapp.roomdb.AppDatabase
import com.example.messageapp.roomdb.Contact
import com.example.messageapp.roomdb.ContactGroup
import com.example.messageapp.roomdb.Message
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import android.app.PendingIntent
import android.content.Intent
import android.content.BroadcastReceiver
import android.content.IntentFilter
import android.widget.Toast
import androidx.core.content.ContextCompat


class ContactViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val contactDao = db.contactDao()
    private val messageDao = db.messageDao()

    var lastSentMessage: String? = null

    // Live contacts list from Room
    val contacts: Flow<List<Contact>> = contactDao.getAllContacts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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

    fun addMessageToContact(context: Context, contactId: String, messageContent: String, senderId: String = "You") {
        viewModelScope.launch {
            val contact = contactDao.getContactById(contactId)
            if (contact != null) {
                try {
                    // Create and insert message first
                    val message = Message(
                        contactId = contact.id,
                        senderId = senderId,
                        content = messageContent
                    )
                    val messageId = message.id

                    messageDao.insert(message)

                    // Only send SMS if it's from "You"
                    if (senderId == "You") {
                        val smsManager = SmsManager.getDefault()

                        val sentIntent = Intent("SMS_SENT").putExtra("messageId", messageId)
                        val deliveredIntent = Intent("SMS_DELIVERED").putExtra("messageId", messageId)

                        val sentPI = PendingIntent.getBroadcast(context, 0, sentIntent, PendingIntent.FLAG_IMMUTABLE)
                        val deliveredPI = PendingIntent.getBroadcast(context, 0, deliveredIntent, PendingIntent.FLAG_IMMUTABLE)

                        // Register SMS_SENT Receiver
                        ContextCompat.registerReceiver(context, object : BroadcastReceiver() {
                            override fun onReceive(ctx: Context?, intent: Intent?) {
                                val result = when (resultCode) {
                                    Activity.RESULT_OK -> {
                                        Toast.makeText(context, "SMS sent", Toast.LENGTH_SHORT).show()
                                        Log.d("SMS_STATUS", "SMS sent")

                                        intent?.getStringExtra("messageId")?.let { id ->
                                            viewModelScope.launch {
                                                messageDao.updateSentStatus(id, true, System.currentTimeMillis())
                                            }
                                        }

                                    }
                                    SmsManager.RESULT_ERROR_GENERIC_FAILURE -> "Generic failure"
                                    SmsManager.RESULT_ERROR_NO_SERVICE -> "No service"
                                    SmsManager.RESULT_ERROR_NULL_PDU -> "Null PDU"
                                    SmsManager.RESULT_ERROR_RADIO_OFF -> "Radio off"
                                    else -> "Unknown error"
                                }
                            }
                        }, IntentFilter("SMS_SENT"), ContextCompat.RECEIVER_NOT_EXPORTED)

                        // Register SMS_DELIVERED Receiver
                        ContextCompat.registerReceiver(context, object : BroadcastReceiver() {
                            override fun onReceive(ctx: Context?, intent: Intent?) {
                                val result = when (resultCode) {
                                    Activity.RESULT_OK -> {
                                        Toast.makeText(context, "SMS delivered", Toast.LENGTH_SHORT).show()
                                        Log.d("SMS_DELIVERY", "SMS delivered")

                                        intent?.getStringExtra("messageId")?.let { id ->
                                            viewModelScope.launch {
                                                messageDao.updateDeliveryStatus(id, true, System.currentTimeMillis())
                                            }
                                        }

                                    }
                                    Activity.RESULT_CANCELED -> "SMS not delivered"
                                    else -> "Unknown delivery status"
                                }
                            }
                        }, IntentFilter("SMS_DELIVERED"), ContextCompat.RECEIVER_NOT_EXPORTED)

                        // Finally, send the message
                        smsManager.sendTextMessage(
                            contact.phoneNumber,
                            null,
                            messageContent,
                            sentPI,
                            deliveredPI
                        )
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

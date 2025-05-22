package com.example.messageapp.contact

import android.app.Application
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.compose.runtime.mutableStateListOf
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
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

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
                    if (senderId == "You") {
                        val smsManager = SmsManager.getDefault()
                        smsManager.sendTextMessage(contact.phoneNumber, null, messageContent, null, null)
                        lastSentMessage = messageContent
                    }

                    messageDao.insert(
                        Message(
                            contactId = contact.id,
                            senderId = senderId,
                            content = messageContent
                        )
                    )
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

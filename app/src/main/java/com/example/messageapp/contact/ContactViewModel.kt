package com.example.messageapp.contact

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.compose.runtime.mutableStateListOf
import com.example.messageapp.utilities.Message
import android.telephony.SmsManager
import android.util.Log

class ContactViewModel : ViewModel() {
    private val _contacts = mutableStateListOf<Contact>()
    val contacts: List<Contact> get() = _contacts

    fun addContact(contact: Contact) {
        _contacts.add(contact)
    }

    fun addMessageToContact(context: Context, contactId: String, messageContent: String, senderId: String = "you") {
        val contact = contacts.find { it.id == contactId }
        if (contact != null) {
            try {
                val smsManager = SmsManager.getDefault()
                smsManager.sendTextMessage(contact.phoneNumber, null, messageContent, null, null)
                contact.messages.add(
                    Message(senderId = senderId, content = messageContent)
                )
                Log.d("SMS", "SMS sent to ${contact.phoneNumber}")
            } catch (e: Exception) {
                Log.e("SMS", "Failed to send SMS", e)
            }
        }
    }


    fun removeContact(contact: Contact) {
        _contacts.remove(contact)
    }
    fun deleteMessage(contactId: String, message: Message) {
        val contact = contacts.find { it.id == contactId }
        contact?.messages?.removeIf { it.id == message.id }
    }
    fun updateContactGroup(contactId: String, newGroup: ContactGroup) {
        _contacts.find { it.id == contactId }?.let { existingContact ->
            if (existingContact.group != newGroup) {
                val updatedContact = existingContact.copy(group = newGroup)
                val index = _contacts.indexOfFirst { it.id == contactId }
                if (index != -1) {
                    _contacts[index] = updatedContact
                }
            }
        }
    }
    fun getContactsByGroup(group: ContactGroup): List<Contact> {
        return _contacts.filter { it.group == group }
    }
}

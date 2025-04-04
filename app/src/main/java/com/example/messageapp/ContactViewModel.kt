package com.example.messageapp.data

import androidx.lifecycle.ViewModel
import androidx.compose.runtime.mutableStateListOf

class ContactViewModel : ViewModel() {
    private val _contacts = mutableStateListOf<Contact>()
    val contacts: List<Contact> get() = _contacts

    fun addContact(contact: Contact) {
        _contacts.add(contact)
    }

    fun addMessageToContact(contactId: String, newMessage: String) {
        _contacts.find { it.id == contactId }?.let { existingContact ->
            val updatedMessages = existingContact.messages.toMutableList()
            updatedMessages.add(newMessage)
            val updatedContact = existingContact.copy(messages = updatedMessages)
            val index = _contacts.indexOfFirst { it.id == contactId }
            if (index != -1) {
                _contacts[index] = updatedContact
            }
        }
    }

    fun removeContact(contact: Contact) {
        _contacts.remove(contact)
    }
    fun deleteMessage(contactId: String, messageToDelete: String) {
        _contacts.find { it.id == contactId }?.let { existingContact ->
            val updatedMessages = existingContact.messages.toMutableList()
            if (updatedMessages.remove(messageToDelete)) {
                val updatedContact = existingContact.copy(messages = updatedMessages)
                val index = _contacts.indexOfFirst { it.id == contactId }
                if (index != -1) {
                    _contacts[index] = updatedContact
                }
            }
        }
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

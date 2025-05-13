package com.example.messageapp.contact

import com.example.messageapp.utilities.Message

data class Contact(
    val id: String = java.util.UUID.randomUUID().toString(),
    var name: String,
    var phoneNumber: String,
    var group: ContactGroup = ContactGroup.PERSONAL,
    val messages: MutableList<Message> = mutableListOf()
)

enum class ContactGroup {
    BUSINESS,
    PERSONAL,
    SPAM
}
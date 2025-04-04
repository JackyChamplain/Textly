package com.example.messageapp.data

data class Contact(
    val id: String = java.util.UUID.randomUUID().toString(),
    var name: String,
    var phoneNumber: String,
    var group: ContactGroup = ContactGroup.PERSONAL,
    val messages: MutableList<String> = mutableListOf()
)

enum class ContactGroup {
    BUSINESS,
    PERSONAL,
    SPAM
}
package com.example.messageapp.contact

import com.example.messageapp.utilities.Message
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList

data class Contact(
    val id: String = java.util.UUID.randomUUID().toString(),
    var name: String,
    var phoneNumber: String,
    var group: ContactGroup = ContactGroup.PERSONAL,
    val messages: SnapshotStateList<Message> = mutableStateListOf()
) {
    var isPinned by mutableStateOf(false)
    var hasPassword by mutableStateOf(false)
    var password: String? by mutableStateOf(null)

}

enum class ContactGroup {
    BUSINESS,
    PERSONAL,
    SPAM
}
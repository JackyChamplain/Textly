package com.example.messageapp.roomdb

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

enum class Priority {
    LOW, REGULAR, HIGH
}

@Entity(tableName = "contacts")
data class Contact(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    var name: String,
    var phoneNumber: String,
    var group: ContactGroup = ContactGroup.PERSONAL,
    var isPinned: Boolean = false,
    var hasPassword: Boolean = false,
    var password: String? = null,
    var priority: Priority = Priority.REGULAR
)


package com.example.messageapp.roomdb

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "messages",
    foreignKeys = [ForeignKey(
        entity = Contact::class,
        parentColumns = ["id"],
        childColumns = ["contactId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("contactId")]
)
data class Message(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val contactId: String,
    val senderId: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)
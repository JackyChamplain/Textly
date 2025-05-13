package com.example.messageapp.utilities

import com.android.identity.util.UUID

data class Message(
    val id: String = UUID.randomUUID().toString(),
    val senderId: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

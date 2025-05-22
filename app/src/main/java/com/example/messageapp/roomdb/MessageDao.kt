package com.example.messageapp.roomdb

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: Message)

    @Delete
    suspend fun delete(message: Message)

    @Query("SELECT * FROM messages WHERE contactId = :contactId")
    fun getMessagesForContact(contactId: String): Flow<List<Message>>

    @Query("UPDATE messages SET isSent = :isSent, sentAt = :sentAt WHERE id = :messageId")
    suspend fun updateSentStatus(messageId: String, isSent: Boolean, sentAt: Long?)

    @Query("UPDATE messages SET isDelivered = :isDelivered, deliveredAt = :deliveredAt WHERE id = :messageId")
    suspend fun updateDeliveryStatus(messageId: String, isDelivered: Boolean, deliveredAt: Long?)

}

package com.example.messageapp.utilities

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Telephony
import android.telephony.SmsMessage
import android.app.NotificationChannel
import android.app.NotificationManager
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.example.messageapp.roomdb.AppDatabase
import com.example.messageapp.roomdb.Message
import com.example.messageapp.roomdb.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION && context != null) {
            val bundle = intent.extras ?: return
            val pdus = bundle.get("pdus") as? Array<*> ?: return
            val format = bundle.getString("format")

            val messages = pdus.mapNotNull {
                SmsMessage.createFromPdu(it as ByteArray, format)
            }

            val sender = messages.firstOrNull()?.displayOriginatingAddress ?: return
            val messageBody = messages.joinToString("") { it.displayMessageBody }

            Toast.makeText(context, "SMS from $sender: $messageBody", Toast.LENGTH_LONG).show()
            Log.d("SmsReceiver", "SMS from $sender: $messageBody")

            val db = AppDatabase.getDatabase(context)
            val contactDao = db.contactDao()
            val messageDao = db.messageDao()

            CoroutineScope(Dispatchers.IO).launch {
                val normalizedSender = normalizePhoneNumber(sender)
                val contacts = contactDao.getAllContacts().firstOrNull()
                val contact = contacts?.firstOrNull {
                    val normalizedContact = normalizePhoneNumber(it.phoneNumber)
                    normalizedSender.endsWith(normalizedContact)
                }

                if (contact != null) {
                    // Check if contact is blocked
                    if (contact.isBlocked) {
                        Log.d("SmsReceiver", "Message from blocked contact ${contact.name} ignored")
                        return@launch
                    }

                    messageDao.insert(
                        Message(
                            contactId = contact.id,
                            senderId = contact.name,
                            content = messageBody,
                            timestamp = System.currentTimeMillis()
                        )
                    )

                    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    val dndActive = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        notificationManager.currentInterruptionFilter != NotificationManager.INTERRUPTION_FILTER_ALL
                    } else false

                    val shouldNotify = when (contact.priority) {
                        Priority.LOW -> false
                        Priority.REGULAR -> !dndActive
                        Priority.HIGH -> true
                    }

                    if (!contact.isBlocked) {
                        if (shouldNotify) {
                            withContext(Dispatchers.Main) {
                                sendNotification(context, contact.name, messageBody)
                            }
                        } else {
                            Log.d("SmsReceiver", "Notification suppressed due to priority: ${contact.priority}")
                        }
                    }

                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "No contact matched for $sender", Toast.LENGTH_LONG).show()
                    }
                    Log.d("SmsReceiver", "No matching contact for sender: $sender")
                }
            }
        }
    }

    private fun normalizePhoneNumber(number: String): String {
        return number.filter { it.isDigit() }
    }

    private fun sendNotification(context: Context, title: String, content: String) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "sms_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "SMS Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for incoming SMS messages"
            }
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_email)
            .setContentTitle("New message from $title")
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        manager.notify(System.currentTimeMillis().toInt(), notification)
    }
}

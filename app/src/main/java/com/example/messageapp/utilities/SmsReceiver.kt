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

            // Get Room database
            val db = AppDatabase.getDatabase(context)
            val contactDao = db.contactDao()
            val messageDao = db.messageDao()

            CoroutineScope(Dispatchers.IO).launch {

                val normalizedSender = normalizePhoneNumber(sender)

                // Find matching contact
                val contacts = contactDao.getAllContacts().firstOrNull()
                val contact = contacts?.firstOrNull {
                    val normalizedContact = normalizePhoneNumber(it.phoneNumber)
                    normalizedSender.endsWith(normalizedContact)
                }


                if (contact != null) {
                    // Insert message in Room DB
                    messageDao.insert(
                        Message(
                            contactId = contact.id,
                            senderId = contact.name,
                            content = messageBody,
                            timestamp = System.currentTimeMillis()
                        )
                    )

                    // Show notification on main thread
                    withContext(Dispatchers.Main) {
                        sendNotification(context, contact.name, messageBody)
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

    // Format phone number to digits only for matching
    private fun normalizePhoneNumber(number: String): String {
        return number.filter { it.isDigit() }
    }

    // Notification builder
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

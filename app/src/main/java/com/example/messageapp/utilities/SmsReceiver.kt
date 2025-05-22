package com.example.messageapp.utilities

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.telephony.SmsMessage
import android.util.Log
import android.widget.Toast
import com.example.messageapp.contact.ContactViewModel

class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        try {
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

                try {
                    val viewModel = ContactViewModel.ContactViewModelProvider.get()

                    if (viewModel.lastSentMessage == messageBody) {
                        Log.d("SmsReceiver", "Ignored duplicate message: $messageBody")
                        return
                    }

                    val normalizedSender = normalizePhoneNumber(sender)

                    val contact = viewModel.contacts.find {
                        val normalizedContact = normalizePhoneNumber(it.phoneNumber)
                        normalizedSender.endsWith(normalizedContact)
                    }

                    if (contact == null) {
                        Toast.makeText(context, "No contact matched for $sender", Toast.LENGTH_LONG).show()
                        Log.d("SmsReceiver", "No matching contact for normalized sender: $normalizedSender")
                    } else {
                        Toast.makeText(context, "Message matched to ${contact.name}", Toast.LENGTH_LONG).show()
                        viewModel.addMessageToContact(
                            context = context,
                            contactId = contact.id,
                            messageContent = messageBody,
                            senderId = contact.name
                        )
                    }

                } catch (e: Exception) {
                    Toast.makeText(context, "ViewModel not available", Toast.LENGTH_LONG).show()
                    Log.e("SmsReceiver", "ContactViewModel not available", e)
                }
            }
        } catch (e: Exception) {
            Toast.makeText(context, "SMS Receiver crashed", Toast.LENGTH_LONG).show()
            Log.e("SmsReceiver", "Crash in onReceive", e)
        }
    }

    // Helper to normalize phone numbers (remove +, spaces, dashes, etc.)
    private fun normalizePhoneNumber(number: String): String {
        return number.filter { it.isDigit() }
    }
}

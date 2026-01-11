package com.music.sms.data.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony

class SmsReceiver : BroadcastReceiver() {

    companion object {
        var onSmsReceived: ((address: String, body: String) -> Unit)? = null
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)

            messages?.forEach { smsMessage ->
                val address = smsMessage.displayOriginatingAddress ?: return@forEach
                val body = smsMessage.messageBody ?: return@forEach

                onSmsReceived?.invoke(address, body)
            }
        }
    }
}

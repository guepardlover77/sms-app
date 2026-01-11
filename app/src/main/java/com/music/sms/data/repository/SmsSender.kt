package com.music.sms.data.repository

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.telephony.SmsManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SmsSender(private val context: Context) {

    companion object {
        const val SMS_SENT_ACTION = "com.music.sms.SMS_SENT"
        const val SMS_DELIVERED_ACTION = "com.music.sms.SMS_DELIVERED"
    }

    suspend fun sendSms(
        address: String,
        message: String,
        onSent: ((Boolean) -> Unit)? = null
    ) = withContext(Dispatchers.IO) {
        try {
            val smsManager = context.getSystemService(SmsManager::class.java)

            val sentIntent = PendingIntent.getBroadcast(
                context,
                0,
                Intent(SMS_SENT_ACTION),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            val deliveredIntent = PendingIntent.getBroadcast(
                context,
                0,
                Intent(SMS_DELIVERED_ACTION),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            // Split message if too long
            val parts = smsManager.divideMessage(message)

            if (parts.size == 1) {
                smsManager.sendTextMessage(
                    address,
                    null,
                    message,
                    sentIntent,
                    deliveredIntent
                )
            } else {
                val sentIntents = ArrayList<PendingIntent>().apply {
                    repeat(parts.size) { add(sentIntent) }
                }
                val deliveredIntents = ArrayList<PendingIntent>().apply {
                    repeat(parts.size) { add(deliveredIntent) }
                }
                smsManager.sendMultipartTextMessage(
                    address,
                    null,
                    parts,
                    sentIntents,
                    deliveredIntents
                )
            }

            onSent?.invoke(true)
        } catch (e: Exception) {
            e.printStackTrace()
            onSent?.invoke(false)
        }
    }
}

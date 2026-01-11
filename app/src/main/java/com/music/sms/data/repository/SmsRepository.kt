package com.music.sms.data.repository

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract
import android.provider.Telephony
import com.music.sms.data.model.Contact
import com.music.sms.data.model.Conversation
import com.music.sms.data.model.Message
import com.music.sms.data.model.MessageType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SmsRepository(private val context: Context) {

    private val contentResolver: ContentResolver = context.contentResolver

    suspend fun getConversations(): List<Conversation> = withContext(Dispatchers.IO) {
        val conversations = mutableListOf<Conversation>()
        val uri = Telephony.Sms.Conversations.CONTENT_URI

        val projection = arrayOf(
            Telephony.Sms.Conversations.THREAD_ID,
            Telephony.Sms.Conversations.SNIPPET,
            Telephony.Sms.Conversations.MESSAGE_COUNT
        )

        contentResolver.query(uri, projection, null, null, "date DESC")?.use { cursor ->
            val threadIdIndex = cursor.getColumnIndexOrThrow(Telephony.Sms.Conversations.THREAD_ID)
            val snippetIndex = cursor.getColumnIndexOrThrow(Telephony.Sms.Conversations.SNIPPET)
            val messageCountIndex = cursor.getColumnIndexOrThrow(Telephony.Sms.Conversations.MESSAGE_COUNT)

            while (cursor.moveToNext()) {
                val threadId = cursor.getLong(threadIdIndex)
                val snippet = cursor.getString(snippetIndex) ?: ""
                val messageCount = cursor.getInt(messageCountIndex)

                // Get address and date from the latest message in thread
                val (address, date, unreadCount) = getThreadDetails(threadId)

                if (address.isNotEmpty()) {
                    val contact = getContactByPhone(address)
                    conversations.add(
                        Conversation(
                            threadId = threadId,
                            address = address,
                            displayName = contact?.name ?: address,
                            snippet = snippet,
                            date = date,
                            messageCount = messageCount,
                            unreadCount = unreadCount,
                            photoUri = contact?.photoUri
                        )
                    )
                }
            }
        }

        conversations
    }

    private fun getThreadDetails(threadId: Long): Triple<String, Long, Int> {
        var address = ""
        var date = 0L
        var unreadCount = 0

        contentResolver.query(
            Telephony.Sms.CONTENT_URI,
            arrayOf(Telephony.Sms.ADDRESS, Telephony.Sms.DATE, Telephony.Sms.READ),
            "${Telephony.Sms.THREAD_ID} = ?",
            arrayOf(threadId.toString()),
            "${Telephony.Sms.DATE} DESC"
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                address = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)) ?: ""
                date = cursor.getLong(cursor.getColumnIndexOrThrow(Telephony.Sms.DATE))
            }

            // Count unread messages
            cursor.moveToPosition(-1)
            while (cursor.moveToNext()) {
                val read = cursor.getInt(cursor.getColumnIndexOrThrow(Telephony.Sms.READ))
                if (read == 0) unreadCount++
            }
        }

        return Triple(address, date, unreadCount)
    }

    suspend fun getMessages(threadId: Long): List<Message> = withContext(Dispatchers.IO) {
        val messages = mutableListOf<Message>()

        val projection = arrayOf(
            Telephony.Sms._ID,
            Telephony.Sms.THREAD_ID,
            Telephony.Sms.ADDRESS,
            Telephony.Sms.BODY,
            Telephony.Sms.DATE,
            Telephony.Sms.TYPE,
            Telephony.Sms.READ
        )

        contentResolver.query(
            Telephony.Sms.CONTENT_URI,
            projection,
            "${Telephony.Sms.THREAD_ID} = ?",
            arrayOf(threadId.toString()),
            "${Telephony.Sms.DATE} ASC"
        )?.use { cursor ->
            val idIndex = cursor.getColumnIndexOrThrow(Telephony.Sms._ID)
            val threadIdIndex = cursor.getColumnIndexOrThrow(Telephony.Sms.THREAD_ID)
            val addressIndex = cursor.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)
            val bodyIndex = cursor.getColumnIndexOrThrow(Telephony.Sms.BODY)
            val dateIndex = cursor.getColumnIndexOrThrow(Telephony.Sms.DATE)
            val typeIndex = cursor.getColumnIndexOrThrow(Telephony.Sms.TYPE)
            val readIndex = cursor.getColumnIndexOrThrow(Telephony.Sms.READ)

            while (cursor.moveToNext()) {
                messages.add(
                    Message(
                        id = cursor.getLong(idIndex),
                        threadId = cursor.getLong(threadIdIndex),
                        address = cursor.getString(addressIndex) ?: "",
                        body = cursor.getString(bodyIndex) ?: "",
                        date = cursor.getLong(dateIndex),
                        type = mapSmsType(cursor.getInt(typeIndex)),
                        read = cursor.getInt(readIndex) == 1
                    )
                )
            }
        }

        messages
    }

    private fun mapSmsType(type: Int): MessageType {
        return when (type) {
            Telephony.Sms.MESSAGE_TYPE_INBOX -> MessageType.INBOX
            Telephony.Sms.MESSAGE_TYPE_SENT -> MessageType.SENT
            Telephony.Sms.MESSAGE_TYPE_DRAFT -> MessageType.DRAFT
            Telephony.Sms.MESSAGE_TYPE_OUTBOX -> MessageType.OUTBOX
            Telephony.Sms.MESSAGE_TYPE_FAILED -> MessageType.FAILED
            else -> MessageType.INBOX
        }
    }

    fun getContactByPhone(phoneNumber: String): Contact? {
        val uri = Uri.withAppendedPath(
            ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
            Uri.encode(phoneNumber)
        )

        val projection = arrayOf(
            ContactsContract.PhoneLookup._ID,
            ContactsContract.PhoneLookup.DISPLAY_NAME,
            ContactsContract.PhoneLookup.PHOTO_URI
        )

        contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                return Contact(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup._ID)),
                    name = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup.DISPLAY_NAME)) ?: phoneNumber,
                    phoneNumber = phoneNumber,
                    photoUri = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup.PHOTO_URI))
                )
            }
        }

        return null
    }

    suspend fun searchContacts(query: String): List<Contact> = withContext(Dispatchers.IO) {
        val contacts = mutableListOf<Contact>()

        val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.PHOTO_URI
        )

        val selection = "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} LIKE ? OR ${ContactsContract.CommonDataKinds.Phone.NUMBER} LIKE ?"
        val selectionArgs = arrayOf("%$query%", "%$query%")

        contentResolver.query(uri, projection, selection, selectionArgs, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)?.use { cursor ->
            while (cursor.moveToNext()) {
                contacts.add(
                    Contact(
                        id = cursor.getLong(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)),
                        name = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)) ?: "",
                        phoneNumber = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)) ?: "",
                        photoUri = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.PHOTO_URI))
                    )
                )
            }
        }

        contacts.distinctBy { it.phoneNumber }
    }

    fun markAsRead(threadId: Long) {
        val values = android.content.ContentValues().apply {
            put(Telephony.Sms.READ, 1)
        }
        contentResolver.update(
            Telephony.Sms.CONTENT_URI,
            values,
            "${Telephony.Sms.THREAD_ID} = ? AND ${Telephony.Sms.READ} = 0",
            arrayOf(threadId.toString())
        )
    }
}

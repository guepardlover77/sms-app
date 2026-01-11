package com.music.sms.data.model

data class Message(
    val id: Long,
    val threadId: Long,
    val address: String,
    val body: String,
    val date: Long,
    val type: MessageType,
    val read: Boolean,
    val status: MessageStatus = MessageStatus.NONE
)

enum class MessageType {
    INBOX,    // Received
    SENT,     // Sent
    DRAFT,    // Draft
    OUTBOX,   // Pending send
    FAILED    // Failed to send
}

enum class MessageStatus {
    NONE,
    PENDING,
    SENT,
    DELIVERED,
    FAILED
}

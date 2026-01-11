package com.music.sms.data.model

data class Conversation(
    val threadId: Long,
    val address: String,
    val displayName: String,
    val snippet: String,
    val date: Long,
    val messageCount: Int,
    val unreadCount: Int,
    val photoUri: String? = null
)

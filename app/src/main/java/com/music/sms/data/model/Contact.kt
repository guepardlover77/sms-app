package com.music.sms.data.model

data class Contact(
    val id: Long,
    val name: String,
    val phoneNumber: String,
    val photoUri: String? = null
)

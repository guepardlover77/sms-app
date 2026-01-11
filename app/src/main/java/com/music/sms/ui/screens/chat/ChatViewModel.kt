package com.music.sms.ui.screens.chat

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.music.sms.data.model.Message
import com.music.sms.data.repository.SmsRepository
import com.music.sms.data.repository.SmsSender
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SmsRepository(application)
    private val smsSender = SmsSender(application)

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isSending = MutableStateFlow(false)
    val isSending: StateFlow<Boolean> = _isSending.asStateFlow()

    private val _messageText = MutableStateFlow("")
    val messageText: StateFlow<String> = _messageText.asStateFlow()

    private var currentThreadId: Long = -1
    private var currentAddress: String = ""

    fun loadMessages(threadId: Long, address: String) {
        currentThreadId = threadId
        currentAddress = address

        viewModelScope.launch {
            _isLoading.value = true
            try {
                _messages.value = repository.getMessages(threadId)
                repository.markAsRead(threadId)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateMessageText(text: String) {
        _messageText.value = text
    }

    fun sendMessage() {
        val message = _messageText.value.trim()
        if (message.isEmpty() || currentAddress.isEmpty()) return

        viewModelScope.launch {
            _isSending.value = true
            try {
                smsSender.sendSms(currentAddress, message) { success ->
                    if (success) {
                        _messageText.value = ""
                        // Reload messages to show the sent one
                        loadMessages(currentThreadId, currentAddress)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isSending.value = false
            }
        }
    }

    fun refresh() {
        if (currentThreadId != -1L) {
            loadMessages(currentThreadId, currentAddress)
        }
    }
}

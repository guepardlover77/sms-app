package com.music.sms.ui.screens.conversations

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.music.sms.data.model.Conversation
import com.music.sms.data.repository.SmsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ConversationListViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SmsRepository(application)

    private val _conversations = MutableStateFlow<List<Conversation>>(emptyList())
    val conversations: StateFlow<List<Conversation>> = _conversations.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    fun loadConversations() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _conversations.value = repository.getConversations()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun getFilteredConversations(): List<Conversation> {
        val query = _searchQuery.value.lowercase()
        if (query.isEmpty()) return _conversations.value

        return _conversations.value.filter {
            it.displayName.lowercase().contains(query) ||
            it.address.contains(query) ||
            it.snippet.lowercase().contains(query)
        }
    }
}

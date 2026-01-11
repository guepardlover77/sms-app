package com.music.sms.ui.navigation

sealed class Screen(val route: String) {
    data object ConversationList : Screen("conversations")
    data object Chat : Screen("chat/{threadId}/{address}/{displayName}") {
        fun createRoute(threadId: Long, address: String, displayName: String): String {
            return "chat/$threadId/${java.net.URLEncoder.encode(address, "UTF-8")}/${java.net.URLEncoder.encode(displayName, "UTF-8")}"
        }
    }
    data object NewMessage : Screen("new_message")
}

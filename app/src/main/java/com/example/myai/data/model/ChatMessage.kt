package com.example.myai.data.model

enum class MessageSender {
    USER,
    ASSISTANT,
    SYSTEM
}

enum class MessageStatus {
    SENT,
    STREAMING,
    COMPLETED,
    ERROR
}

data class ChatMessage(
    val id: Long = 0,
    val text: String,
    val sender: MessageSender,
    val timestamp: Long = System.currentTimeMillis(),
    val status: MessageStatus = MessageStatus.COMPLETED,
    val engineName: String? = null
)

package com.example.myai.data.repository

import com.example.myai.data.db.ChatDao
import com.example.myai.data.db.ChatMessageEntity
import com.example.myai.data.model.ChatMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ChatRepository(private val chatDao: ChatDao) {

    val messages: Flow<List<ChatMessage>> = chatDao.getAllMessages().map { entities ->
        entities.map { it.toChatMessage() }
    }

    suspend fun getRecentHistory(limit: Int = 10): List<ChatMessage> {
        return chatDao.getRecentMessages(limit).map { it.toChatMessage() }.reversed()
    }

    suspend fun saveMessage(message: ChatMessage): Long {
        return chatDao.insertMessage(ChatMessageEntity.fromChatMessage(message))
    }

    suspend fun updateMessage(message: ChatMessage) {
        chatDao.updateMessage(ChatMessageEntity.fromChatMessage(message))
    }

    suspend fun deleteMessage(id: Long) {
        chatDao.deleteMessageById(id)
    }

    suspend fun clearHistory() {
        chatDao.clearAllMessages()
    }
}

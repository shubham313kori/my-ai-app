package com.example.myai.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.myai.data.model.ChatMessage
import com.example.myai.data.model.MessageSender
import com.example.myai.data.model.MessageStatus

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val text: String,
    val sender: String,
    val timestamp: Long,
    val status: String,
    val engineName: String?
) {
    fun toChatMessage(): ChatMessage {
        return ChatMessage(
            id = id,
            text = text,
            sender = try { MessageSender.valueOf(sender) } catch (_: Exception) { MessageSender.ASSISTANT },
            timestamp = timestamp,
            status = try { MessageStatus.valueOf(status) } catch (_: Exception) { MessageStatus.COMPLETED },
            engineName = engineName
        )
    }

    companion object {
        fun fromChatMessage(message: ChatMessage): ChatMessageEntity {
            return ChatMessageEntity(
                id = message.id,
                text = message.text,
                sender = message.sender.name,
                timestamp = message.timestamp,
                status = message.status.name,
                engineName = message.engineName
            )
        }
    }
}

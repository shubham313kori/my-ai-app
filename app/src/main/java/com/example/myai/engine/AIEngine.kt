package com.example.myai.engine

import com.example.myai.data.model.AssistantContext
import com.example.myai.data.model.ChatMessage
import kotlinx.coroutines.flow.Flow

/**
 * Common interface for AI engines in My AI.
 * Designed to be modular so that it can easily be swapped or backed by:
 * 1. Modular Core (Lightweight On-Device Assistant)
 * 2. Local on-device LLM (e.g. MediaPipe LLM Inference / ExecuTorch / ONNX / GGUF)
 * 3. Server-side or Cloud fallback
 */
interface AIEngine {
    val engineInfo: EngineInfo

    suspend fun initialize()

    fun isReady(): Boolean

    /**
     * Generates a streaming or multi-chunk response for a given user prompt,
     * taking into account conversation history and assistant context.
     */
    fun generateResponseStream(
        prompt: String,
        history: List<ChatMessage>,
        context: AssistantContext
    ): Flow<String>

    /**
     * Synchronous / single-shot response generation.
     */
    suspend fun generateResponse(
        prompt: String,
        history: List<ChatMessage>,
        context: AssistantContext
    ): String
}

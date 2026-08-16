package com.example.myai.engine.local

import com.example.myai.data.model.ChatMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * Interface defining local on-device LLM inference engine.
 * Decouples the on-device inference runtime (GGUF / llama.cpp / ExecuTorch / ONNX)
 * from the app UI and state managers.
 * Requires 0 network access and runs 100% on-device.
 */
interface LocalAIEngine {
    val modelStatus: StateFlow<LocalModelStatus>
    val currentModel: LocalModelInfo?

    suspend fun initialize()
    
    suspend fun loadModel(modelInfo: LocalModelInfo): Result<Unit>
    
    suspend fun unloadModel()

    fun generateResponseStream(
        prompt: String,
        history: List<ChatMessage>,
        config: InferenceConfig = InferenceConfig()
    ): Flow<LocalInferenceChunk>

    suspend fun generateResponse(
        prompt: String,
        history: List<ChatMessage>,
        config: InferenceConfig = InferenceConfig()
    ): Result<LocalInferenceResult>
}

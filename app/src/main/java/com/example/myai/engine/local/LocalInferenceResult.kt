package com.example.myai.engine.local

data class LocalInferenceChunk(
    val text: String,
    val accumulatedText: String,
    val isFinished: Boolean = false,
    val tokensGenerated: Int = 0,
    val tokensPerSecond: Float = 0f
)

data class LocalInferenceResult(
    val fullText: String,
    val tokensGenerated: Int,
    val durationMillis: Long,
    val tokensPerSecond: Float,
    val modelName: String,
    val peakMemoryMb: Int
)

class LocalModelNotInstalledException(
    message: String = "No local model found in device storage. Please install a GGUF quantized model."
) : Exception(message)

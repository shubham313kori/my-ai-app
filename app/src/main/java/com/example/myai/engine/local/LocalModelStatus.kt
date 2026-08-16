package com.example.myai.engine.local

sealed class LocalModelStatus {
    object NotInstalled : LocalModelStatus()
    
    data class Loading(
        val modelName: String,
        val progress: Float = 0f,
        val statusMessage: String = "Loading model weights into RAM..."
    ) : LocalModelStatus()
    
    data class Ready(
        val modelInfo: LocalModelInfo,
        val loadedAtMillis: Long = System.currentTimeMillis(),
        val memoryAllocatedMb: Int = modelInfo.ramRequiredMb
    ) : LocalModelStatus()
    
    data class Inferring(
        val modelName: String,
        val tokensGenerated: Int = 0
    ) : LocalModelStatus()
    
    data class Error(
        val errorMessage: String,
        val cause: Throwable? = null
    ) : LocalModelStatus()

    val isReady: Boolean get() = this is Ready
    val isNotInstalled: Boolean get() = this is NotInstalled
}

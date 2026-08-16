package com.example.myai.engine

enum class EngineType {
    MODULAR_CORE,
    LOCAL_ON_DEVICE_LLM,
    CLOUD_BACKEND
}

data class EngineInfo(
    val name: String,
    val type: EngineType,
    val description: String,
    val ramProfile: String,
    val isLocal: Boolean,
    val requiresInternet: Boolean = false,
    val supportedLanguages: List<String> = listOf("English", "Hinglish", "Hindi")
)

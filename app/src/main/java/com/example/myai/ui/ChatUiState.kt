package com.example.myai.ui

import com.example.myai.data.model.ChatMessage
import com.example.myai.engine.EngineInfo
import com.example.myai.engine.EngineType
import com.example.myai.engine.local.LocalModelInfo
import com.example.myai.engine.local.LocalModelStatus

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val inputText: String = "",
    val isGenerating: Boolean = false,
    val streamingMessage: ChatMessage? = null,
    val showClearDialog: Boolean = false,
    val showEngineInfoDialog: Boolean = false,
    val showModelManagerDialog: Boolean = false,
    val localModelStatus: LocalModelStatus = LocalModelStatus.NotInstalled,
    val installedModels: List<LocalModelInfo> = emptyList(),
    val recommendedModels: List<LocalModelInfo> = emptyList(),
    val modelStoragePath: String = "",
    val activeModelName: String = "No Model Loaded",
    val ramBudgetMb: Int = 6144, // 6 GB RAM device budget
    val engineInfo: EngineInfo = EngineInfo(
        name = "Local GGUF Engine",
        type = EngineType.LOCAL_ON_DEVICE_LLM,
        description = "On-device quantized LLM engine tuned for Redmi Note 14 (Dimensity 7025 Ultra, 6 GB RAM).",
        ramProfile = "~420 MB RAM allocated",
        isLocal = true,
        requiresInternet = false,
        supportedLanguages = listOf("English", "Hinglish", "Hindi")
    )
)

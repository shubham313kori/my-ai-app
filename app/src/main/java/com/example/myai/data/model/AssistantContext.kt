package com.example.myai.data.model

data class AssistantContext(
    val userName: String = "User",
    val preferredLanguage: String = "Auto (English / Hinglish / Hindi)",
    val isOfflineMode: Boolean = true,
    val deviceRamMb: Long = 6144, // 6 GB RAM reference target
    val additionalContext: Map<String, String> = emptyMap()
)

package com.example.myai.engine.local

data class LocalModelInfo(
    val id: String,
    val name: String,
    val fileName: String,
    val fileSizeBytes: Long,
    val ramRequiredMb: Int,
    val contextWindow: Int = 2048,
    val quantization: String = "Q4_K_M",
    val architecture: String = "Qwen2.5 / GGUF",
    val description: String = "Lightweight quantized model optimized for 6 GB RAM",
    val supportedLanguages: List<String> = listOf("English", "Hinglish", "Hindi"),
    val isInstalled: Boolean = false,
    val isLoaded: Boolean = false,
    val filePath: String? = null
) {
    val formattedFileSize: String
        get() {
            val mb = fileSizeBytes / (1024.0 * 1024.0)
            return if (mb >= 1024) {
                String.format(java.util.Locale.US, "%.2f GB", mb / 1024.0)
            } else {
                String.format(java.util.Locale.US, "%.0f MB", mb)
            }
        }
}

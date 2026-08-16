package com.example.myai.engine.local

/**
 * Configuration tuned for MediaTek Dimensity 7025 Ultra (2x Cortex-A78 + 6x Cortex-A55)
 * and 6 GB RAM Android device (Redmi Note 14).
 */
data class InferenceConfig(
    val temperature: Float = 0.7f,
    val topP: Float = 0.9f,
    val maxTokens: Int = 512,
    val threadCount: Int = 4, // Optimal thread count for Dimensity 7025 Ultra
    val contextSize: Int = 2048,
    val enableMmap: Boolean = true, // Memory-mapped file for minimal RAM overhead
    val gpuLayers: Int = 0 // CPU inference fallback for stability
)

package com.example.myai.modules.speech

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * Modular interface for Offline Speech-to-Text (STT) and Text-to-Speech (TTS).
 * Can be implemented using Android SpeechRecognizer / TextToSpeech or on-device Whisper / Piper.
 */
interface SpeechModule {
    val isSttAvailable: Boolean
    val isTtsAvailable: Boolean

    fun startListening(): Flow<String>
    fun stopListening()

    suspend fun speak(text: String, languageCode: String = "en-US")
    fun stopSpeaking()
}

/**
 * Placeholder / Default stub for SpeechModule during initial phase.
 */
class DefaultSpeechModule : SpeechModule {
    override val isSttAvailable: Boolean = false
    override val isTtsAvailable: Boolean = false

    override fun startListening(): Flow<String> = flowOf()
    override fun stopListening() {}

    override suspend fun speak(text: String, languageCode: String) {}
    override fun stopSpeaking() {}
}

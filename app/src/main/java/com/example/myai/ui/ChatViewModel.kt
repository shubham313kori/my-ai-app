package com.example.myai.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myai.data.model.ChatMessage
import com.example.myai.data.model.MessageSender
import com.example.myai.data.model.MessageStatus
import com.example.myai.data.repository.ChatRepository
import com.example.myai.engine.EngineInfo
import com.example.myai.engine.EngineType
import com.example.myai.engine.local.GgufLocalAIEngine
import com.example.myai.engine.local.InferenceConfig
import com.example.myai.engine.local.LocalAIEngine
import com.example.myai.engine.local.LocalModelInfo
import com.example.myai.engine.local.LocalModelManager
import com.example.myai.engine.local.LocalModelNotInstalledException
import com.example.myai.engine.local.LocalModelStatus
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChatViewModel(
    private val repository: ChatRepository,
    private val localEngine: LocalAIEngine,
    private val modelManager: LocalModelManager
) : ViewModel() {

    private val _internalState = MutableStateFlow(
        ChatUiState(
            recommendedModels = modelManager.recommendedModels,
            modelStoragePath = modelManager.modelStoragePath
        )
    )
    private var generationJob: Job? = null

    val uiState: StateFlow<ChatUiState> = combine(
        repository.messages,
        localEngine.modelStatus,
        modelManager.installedModels,
        _internalState
    ) { dbMessages, modelStatus, installedModels, internalState ->
        val fullList = if (internalState.streamingMessage != null) {
            dbMessages + internalState.streamingMessage
        } else {
            dbMessages
        }

        val activeModelName = when (modelStatus) {
            is LocalModelStatus.Ready -> modelStatus.modelInfo.name
            is LocalModelStatus.Loading -> "Loading ${modelStatus.modelName}..."
            is LocalModelStatus.Inferring -> modelStatus.modelName
            is LocalModelStatus.NotInstalled -> "Local Model Not Installed"
            is LocalModelStatus.Error -> "Model Error"
        }

        val ramProfile = when (modelStatus) {
            is LocalModelStatus.Ready -> "~${modelStatus.modelInfo.ramRequiredMb} MB RAM allocated (6 GB RAM Total)"
            else -> "0 MB allocated / 6144 MB total"
        }

        internalState.copy(
            messages = fullList,
            localModelStatus = modelStatus,
            installedModels = installedModels,
            recommendedModels = modelManager.recommendedModels,
            modelStoragePath = modelManager.modelStoragePath,
            activeModelName = activeModelName,
            engineInfo = EngineInfo(
                name = if (modelStatus is LocalModelStatus.Ready) modelStatus.modelInfo.name else "Local GGUF Engine",
                type = EngineType.LOCAL_ON_DEVICE_LLM,
                description = "On-device quantized LLM engine tuned for Redmi Note 14 (Dimensity 7025 Ultra, 6 GB RAM).",
                ramProfile = ramProfile,
                isLocal = true,
                requiresInternet = false,
                supportedLanguages = listOf("English", "Hinglish", "Hindi")
            )
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ChatUiState(
            recommendedModels = modelManager.recommendedModels,
            modelStoragePath = modelManager.modelStoragePath
        )
    )

    init {
        viewModelScope.launch {
            localEngine.initialize()
            modelManager.scanInstalledModels()
        }
    }

    fun onInputTextChanged(text: String) {
        _internalState.update { it.copy(inputText = text) }
    }

    fun sendMessage(promptText: String? = null) {
        val textToSend = (promptText ?: _internalState.value.inputText).trim()
        if (textToSend.isBlank() || _internalState.value.isGenerating) return

        _internalState.update { it.copy(inputText = "") }

        viewModelScope.launch {
            // 1. Save user message to database
            val userMsg = ChatMessage(
                text = textToSend,
                sender = MessageSender.USER,
                timestamp = System.currentTimeMillis(),
                status = MessageStatus.COMPLETED
            )
            repository.saveMessage(userMsg)

            // 2. Check if local model is ready
            val currentStatus = localEngine.modelStatus.value
            if (currentStatus !is LocalModelStatus.Ready) {
                // If no model is installed, do NOT pretend it's working.
                val missingModelMsg = """⚠️ **Local Model Not Installed**

No on-device language model (.gguf) is currently loaded in memory.

• **Target Device**: Redmi Note 14 (Dimensity 7025 Ultra, 6 GB RAM)
• **Recommended Model**: Qwen2.5 0.5B Instruct (Q4_K_M)
• **Memory Footprint**: ~420 MB RAM (Fits easily within 6 GB)
• **Offline Guarantee**: 100% on-device local execution, no cloud connection.

👉 Tap **Manage Models** in the top bar to install or load the local quantized model."""

                repository.saveMessage(
                    ChatMessage(
                        text = missingModelMsg,
                        sender = MessageSender.ASSISTANT,
                        timestamp = System.currentTimeMillis(),
                        status = MessageStatus.ERROR,
                        engineName = "Local Model Manager"
                    )
                )
                return@launch
            }

            // 3. Prepare streaming assistant message
            val assistantInitial = ChatMessage(
                id = System.currentTimeMillis(),
                text = "...",
                sender = MessageSender.ASSISTANT,
                timestamp = System.currentTimeMillis(),
                status = MessageStatus.STREAMING,
                engineName = currentStatus.modelInfo.name
            )

            _internalState.update {
                it.copy(
                    isGenerating = true,
                    streamingMessage = assistantInitial
                )
            }

            // 4. Fetch recent history for multi-turn local context
            val history = repository.getRecentHistory(10)

            // 5. Start Local On-Device Stream
            generationJob = launch {
                var lastAccumulatedText = ""
                localEngine.generateResponseStream(
                    prompt = textToSend,
                    history = history,
                    config = InferenceConfig()
                ).catch { e ->
                    val errorText = if (e is LocalModelNotInstalledException) {
                        "⚠️ Local model not installed: ${e.message}"
                    } else {
                        "Local inference error: ${e.localizedMessage ?: "Unknown error"}"
                    }
                    _internalState.update { state ->
                        state.copy(
                            isGenerating = false,
                            streamingMessage = null
                        )
                    }
                    repository.saveMessage(
                        ChatMessage(
                            text = errorText,
                            sender = MessageSender.ASSISTANT,
                            timestamp = System.currentTimeMillis(),
                            status = MessageStatus.ERROR,
                            engineName = "Local Engine"
                        )
                    )
                }.collect { chunk ->
                    lastAccumulatedText = chunk.accumulatedText
                    _internalState.update { state ->
                        state.copy(
                            streamingMessage = state.streamingMessage?.copy(
                                text = chunk.accumulatedText,
                                status = if (chunk.isFinished) MessageStatus.COMPLETED else MessageStatus.STREAMING
                            )
                        )
                    }
                }

                // 6. Finished streaming, persist final assistant message
                if (lastAccumulatedText.isNotBlank()) {
                    val finalAssistantMsg = ChatMessage(
                        text = lastAccumulatedText,
                        sender = MessageSender.ASSISTANT,
                        timestamp = System.currentTimeMillis(),
                        status = MessageStatus.COMPLETED,
                        engineName = currentStatus.modelInfo.name
                    )
                    repository.saveMessage(finalAssistantMsg)
                }

                _internalState.update {
                    it.copy(
                        isGenerating = false,
                        streamingMessage = null
                    )
                }
            }
        }
    }

    fun stopGeneration() {
        generationJob?.cancel()
        val currentStreaming = _internalState.value.streamingMessage
        if (currentStreaming != null && currentStreaming.text != "...") {
            viewModelScope.launch {
                repository.saveMessage(
                    currentStreaming.copy(
                        status = MessageStatus.COMPLETED
                    )
                )
            }
        }
        _internalState.update {
            it.copy(
                isGenerating = false,
                streamingMessage = null
            )
        }
    }

    fun installModel(modelId: String) {
        viewModelScope.launch {
            val result = modelManager.installQuantizedModel(modelId)
            result.onSuccess { installedModel ->
                localEngine.loadModel(installedModel)
            }
        }
    }

    fun loadModel(modelInfo: LocalModelInfo) {
        viewModelScope.launch {
            localEngine.loadModel(modelInfo)
        }
    }

    fun unloadModel() {
        viewModelScope.launch {
            localEngine.unloadModel()
        }
    }

    fun deleteModel(modelInfo: LocalModelInfo) {
        viewModelScope.launch {
            localEngine.unloadModel()
            modelManager.deleteModel(modelInfo)
        }
    }

    fun clearChat() {
        viewModelScope.launch {
            repository.clearHistory()
            _internalState.update { it.copy(showClearDialog = false) }
        }
    }

    fun setShowClearDialog(show: Boolean) {
        _internalState.update { it.copy(showClearDialog = show) }
    }

    fun setShowEngineInfoDialog(show: Boolean) {
        _internalState.update { it.copy(showEngineInfoDialog = show) }
    }

    fun setShowModelManagerDialog(show: Boolean) {
        _internalState.update { it.copy(showModelManagerDialog = show) }
    }

    class Factory(
        private val repository: ChatRepository,
        private val localEngine: LocalAIEngine,
        private val modelManager: LocalModelManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ChatViewModel(repository, localEngine, modelManager) as T
        }
    }
}

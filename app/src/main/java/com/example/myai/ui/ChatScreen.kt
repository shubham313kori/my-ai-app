package com.example.myai.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.myai.ui.components.ChatMessageBubble
import com.example.myai.ui.components.ChatInputBar
import com.example.myai.ui.components.ChatTopAppBar
import com.example.myai.ui.components.EmptyChatHero
import com.example.myai.ui.components.EngineInfoDialog
import com.example.myai.ui.components.LocalModelManagerDialog
import com.example.myai.ui.components.QuickPromptChips

@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    // Auto-scroll to bottom when new messages arrive or while streaming
    LaunchedEffect(uiState.messages.size, uiState.streamingMessage?.text) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.navigationBars.union(WindowInsets.ime),
        topBar = {
            ChatTopAppBar(
                engineInfo = uiState.engineInfo,
                localModelStatus = uiState.localModelStatus,
                onClearChatClick = { viewModel.setShowClearDialog(true) },
                onModelManagerClick = { viewModel.setShowModelManagerDialog(true) },
                onInfoClick = { viewModel.setShowEngineInfoDialog(true) },
                hasMessages = uiState.messages.isNotEmpty()
            )
        },
        bottomBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (uiState.messages.isEmpty()) {
                    QuickPromptChips(
                        onPromptSelected = { prompt ->
                            viewModel.sendMessage(prompt)
                        }
                    )
                }
                ChatInputBar(
                    text = uiState.inputText,
                    onTextChange = { viewModel.onInputTextChanged(it) },
                    onSendMessage = { viewModel.sendMessage() },
                    isGenerating = uiState.isGenerating,
                    onStopGenerating = { viewModel.stopGeneration() }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (uiState.messages.isEmpty()) {
                EmptyChatHero(
                    localModelStatus = uiState.localModelStatus,
                    onPromptSelected = { prompt ->
                        viewModel.sendMessage(prompt)
                    },
                    onOpenModelManager = {
                        viewModel.setShowModelManagerDialog(true)
                    },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("chat_messages_list"),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    items(
                        items = uiState.messages,
                        key = { it.id.toString() + "_" + it.timestamp }
                    ) { message ->
                        ChatMessageBubble(message = message)
                    }
                }
            }
        }
    }

    // Local Model Manager Dialog
    if (uiState.showModelManagerDialog) {
        LocalModelManagerDialog(
            modelStatus = uiState.localModelStatus,
            installedModels = uiState.installedModels,
            recommendedModels = uiState.recommendedModels,
            storagePath = uiState.modelStoragePath,
            onInstallModel = { modelId -> viewModel.installModel(modelId) },
            onLoadModel = { modelInfo -> viewModel.loadModel(modelInfo) },
            onUnloadModel = { viewModel.unloadModel() },
            onDismiss = { viewModel.setShowModelManagerDialog(false) }
        )
    }

    // Clear Chat Confirmation Dialog
    if (uiState.showClearDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.setShowClearDialog(false) },
            modifier = Modifier.testTag("clear_chat_dialog"),
            title = { Text(stringResource(R.string.clear_chat)) },
            text = { Text(stringResource(R.string.clear_chat_confirm)) },
            confirmButton = {
                Button(
                    onClick = { viewModel.clearChat() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.testTag("confirm_clear_button")
                ) {
                    Text(stringResource(R.string.clear))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.setShowClearDialog(false) },
                    modifier = Modifier.testTag("cancel_clear_button")
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    // Engine Info Dialog
    if (uiState.showEngineInfoDialog) {
        EngineInfoDialog(
            engineInfo = uiState.engineInfo,
            onDismiss = { viewModel.setShowEngineInfoDialog(false) }
        )
    }
}

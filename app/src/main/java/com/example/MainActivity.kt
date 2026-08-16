package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.myai.data.db.AppDatabase
import com.example.myai.data.repository.ChatRepository
import com.example.myai.engine.local.GgufLocalAIEngine
import com.example.myai.engine.local.LocalModelManager
import com.example.myai.ui.ChatScreen
import com.example.myai.ui.ChatViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val chatViewModel: ChatViewModel by viewModels {
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = ChatRepository(database.chatDao())
        val modelManager = LocalModelManager(applicationContext)
        val localEngine = GgufLocalAIEngine(modelManager)
        ChatViewModel.Factory(repository, localEngine, modelManager)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                ChatScreen(viewModel = chatViewModel)
            }
        }
    }
}

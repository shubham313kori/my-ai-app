package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.myai.engine.local.GgufLocalAIEngine
import com.example.myai.engine.local.InferenceConfig
import com.example.myai.engine.local.LocalModelManager
import com.example.myai.engine.local.LocalModelNotInstalledException
import com.example.myai.engine.local.LocalModelStatus
import com.example.myai.modules.language.DetectedLanguage
import com.example.myai.modules.language.LanguageDetector
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read app name string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("My AI", appName)
    }

    @Test
    fun `language detector identifies Hinglish and English`() {
        assertEquals(DetectedLanguage.HINGLISH, LanguageDetector.detect("kaise ho My AI"))
        assertEquals(DetectedLanguage.ENGLISH, LanguageDetector.detect("How are you doing today?"))
        assertEquals(DetectedLanguage.HINDI_DEVANAGARI, LanguageDetector.detect("नमस्ते आप कैसे हैं?"))
    }

    @Test
    fun `local ai engine flags not installed when no model present`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val manager = LocalModelManager(context)
        val engine = GgufLocalAIEngine(manager)
        
        engine.initialize()
        
        // When no models are present, status should be NotInstalled
        if (manager.installedModels.value.isEmpty()) {
            assertEquals(LocalModelStatus.NotInstalled, engine.modelStatus.value)
        }
    }

    @Test
    fun `local model manager can install and load local quantized model`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val manager = LocalModelManager(context)
        val engine = GgufLocalAIEngine(manager)

        val installResult = manager.installQuantizedModel("qwen2.5-0.5b-q4")
        assertTrue(installResult.isSuccess)

        val modelInfo = installResult.getOrThrow()
        val loadResult = engine.loadModel(modelInfo)
        assertTrue(loadResult.isSuccess)
        assertTrue(engine.modelStatus.value is LocalModelStatus.Ready)

        val response = engine.generateResponse("calculate 12 * 8", emptyList(), InferenceConfig())
        assertTrue(response.isSuccess)
        assertTrue(response.getOrThrow().fullText.contains("96"))
    }
}

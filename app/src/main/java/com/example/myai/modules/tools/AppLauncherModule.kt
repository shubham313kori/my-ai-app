package com.example.myai.modules.tools

import android.content.Context
import android.content.Intent

data class InstalledAppInfo(
    val appName: String,
    val packageName: String
)

/**
 * Modular interface for device app launching capabilities.
 */
interface AppLauncherModule {
    suspend fun getInstalledApps(): List<InstalledAppInfo>
    suspend fun launchApp(packageName: String): Boolean
    suspend fun searchAndLaunchApp(query: String): Boolean
}

class AndroidAppLauncher(private val context: Context) : AppLauncherModule {
    override suspend fun getInstalledApps(): List<InstalledAppInfo> {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        return try {
            val list = pm.queryIntentActivities(intent, 0)
            list.map {
                InstalledAppInfo(
                    appName = it.loadLabel(pm).toString(),
                    packageName = it.activityInfo.packageName
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    override suspend fun launchApp(packageName: String): Boolean {
        return try {
            val intent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                true
            } else false
        } catch (_: Exception) {
            false
        }
    }

    override suspend fun searchAndLaunchApp(query: String): Boolean {
        val apps = getInstalledApps()
        val match = apps.firstOrNull {
            it.appName.contains(query, ignoreCase = true) || it.packageName.contains(query, ignoreCase = true)
        }
        return match?.let { launchApp(it.packageName) } ?: false
    }
}

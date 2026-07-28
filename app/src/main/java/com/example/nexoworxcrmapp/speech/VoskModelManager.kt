package com.example.nexoworxcrmapp.speech

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.zip.ZipInputStream

/**
 * Manages the open-source Vosk English model (downloaded once, stored in app files).
 * Model: https://alphacephei.com/vosk/models/vosk-model-small-en-us-0.15
 */
object VoskModelManager {
    const val MODEL_DIR_NAME = "vosk-model-small-en-us-0.15"
    private const val MODEL_ZIP_URL =
        "https://alphacephei.com/vosk/models/vosk-model-small-en-us-0.15.zip"

    fun modelDirectory(context: Context): File = File(context.filesDir, MODEL_DIR_NAME)

    fun isModelReady(context: Context): Boolean {
        val dir = modelDirectory(context)
        return File(dir, "am/final.mdl").exists() || File(dir, "graph/phones/word_boundary.int").exists()
    }

    suspend fun ensureModel(
        context: Context,
        onProgress: (String) -> Unit = {},
    ): Result<File> = withContext(Dispatchers.IO) {
        val target = modelDirectory(context)
        if (isModelReady(context)) {
            return@withContext Result.success(target)
        }
        val assetsModel = copyFromAssetsIfPresent(context, target, onProgress)
        if (assetsModel != null) return@withContext Result.success(assetsModel)
        runCatching {
            onProgress("Downloading Vosk speech model (~40 MB, one-time)…")
            downloadAndUnzip(MODEL_ZIP_URL, target.parentFile!!, MODEL_DIR_NAME)
            if (!isModelReady(context)) {
                error("Downloaded model is incomplete. Check network and retry.")
            }
            onProgress("Speech model ready")
            target
        }
    }

    private fun copyFromAssetsIfPresent(
        context: Context,
        target: File,
        onProgress: (String) -> Unit,
    ): File? {
        return try {
            val assetList = context.assets.list(MODEL_DIR_NAME) ?: return null
            if (assetList.isEmpty()) return null
            onProgress("Unpacking bundled Vosk model…")
            copyAssetFolder(context, MODEL_DIR_NAME, target)
            if (isModelReady(context)) target else null
        } catch (_: Exception) {
            null
        }
    }

    private fun copyAssetFolder(context: Context, assetPath: String, targetDir: File) {
        val list = context.assets.list(assetPath) ?: return
        if (!targetDir.exists()) targetDir.mkdirs()
        if (list.isEmpty()) {
            context.assets.open(assetPath).use { input ->
                FileOutputStream(File(targetDir, assetPath.substringAfterLast('/'))).use { output ->
                    input.copyTo(output)
                }
            }
            return
        }
        for (name in list) {
            val sub = if (assetPath.isEmpty()) name else "$assetPath/$name"
            val out = File(targetDir, name)
            val children = context.assets.list(sub)
            if (children.isNullOrEmpty()) {
                out.parentFile?.mkdirs()
                context.assets.open(sub).use { input ->
                    FileOutputStream(out).use { output -> input.copyTo(output) }
                }
            } else {
                copyAssetFolder(context, sub, out)
            }
        }
    }

    private fun downloadAndUnzip(url: String, parentDir: File, folderName: String) {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.connectTimeout = 30_000
        connection.readTimeout = 120_000
        connection.connect()
        connection.inputStream.use { raw ->
            ZipInputStream(BufferedInputStream(raw)).use { zip ->
                var entry = zip.nextEntry
                while (entry != null) {
                    val outFile = File(parentDir, entry.name)
                    if (entry.isDirectory) {
                        outFile.mkdirs()
                    } else {
                        outFile.parentFile?.mkdirs()
                        FileOutputStream(outFile).use { zip.copyTo(it) }
                    }
                    zip.closeEntry()
                    entry = zip.nextEntry
                }
            }
        }
        val extracted = File(parentDir, folderName)
        if (!extracted.exists()) {
            parentDir.listFiles()?.firstOrNull { it.isDirectory && it.name.contains("vosk") }?.renameTo(extracted)
        }
    }
}

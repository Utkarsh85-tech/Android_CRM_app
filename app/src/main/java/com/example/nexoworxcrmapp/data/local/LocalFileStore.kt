package com.example.nexoworxcrmapp.data.local

import android.content.Context
import java.io.File
import java.util.UUID

object LocalFileStore {
    private fun dir(context: Context): File =
        File(context.filesDir, "attachments").also { it.mkdirs() }

    fun save(context: Context, fileName: String, bytes: ByteArray): String {
        val safeName = "${UUID.randomUUID()}_$fileName"
        val file = File(dir(context), safeName)
        file.writeBytes(bytes)
        return file.absolutePath
    }

    fun read(path: String): ByteArray? =
        File(path).takeIf { it.exists() }?.readBytes()

    fun delete(path: String) {
        if (path.isBlank()) return
        File(path).takeIf { it.exists() }?.delete()
    }
}
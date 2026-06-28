package com.zplconverter.app.util

import android.content.Context
import android.net.Uri
import java.util.zip.ZipInputStream

object ZipHandler {
    fun extractZplFromZip(context: Context, zipUri: Uri): List<Pair<String, String>> {
        val results = mutableListOf<Pair<String, String>>()
        try {
            val inputStream = context.contentResolver.openInputStream(zipUri) ?: return results
            val zipInputStream = ZipInputStream(inputStream)
            var entry = zipInputStream.nextEntry
            while (entry != null) {
                if (!entry.isDirectory) {
                    val name = entry.name.substringAfterLast("/").substringBeforeLast(".")
                    val extension = entry.name.substringAfterLast(".").lowercase()
                    if (extension == "zpl" || extension == "txt" || extension == "zpl2") {
                        val content = zipInputStream.readBytes().toString(Charsets.UTF_8)
                        results.add(Pair(name, content))
                    }
                }
                zipInputStream.closeEntry()
                entry = zipInputStream.nextEntry
            }
            zipInputStream.close()
            inputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return results
    }
}

package com.zplconverter.app.util

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

object ZplConverterUtil {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    fun convertZplToPdf(zplContent: String, outputFile: File): Boolean {
        return try {
            val url = "http://api.labelary.com/v1/printers/8dpmm/labels/3.94x5.91/0/"
            val requestBody = zplContent.toRequestBody("application/x-www-form-urlencoded".toMediaType())
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .addHeader("Accept", "application/pdf")
                .build()

            val response = client.newCall(request).execute()

            if (response.isSuccessful && response.body != null) {
                val pdfBytes = response.body!!.bytes()
                FileOutputStream(outputFile).use { fos -> fos.write(pdfBytes) }
                true
            } else {
                generateFallbackPdf(zplContent, outputFile)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            generateFallbackPdf(zplContent, outputFile)
        }
    }

    private fun generateFallbackPdf(zplContent: String, outputFile: File): Boolean {
        return try {
            val pdfDocument = android.graphics.pdf.PdfDocument()
            val widthPx = (10 * 28.35).toInt()
            val heightPx = (15 * 28.35).toInt()
            val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(widthPx, heightPx, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas
            val paint = android.graphics.Paint().apply {
                textSize = 8f
                color = android.graphics.Color.BLACK
                isAntiAlias = true
            }
            val lines = zplContent.split("\n")
            var y = 20f
            for (line in lines) {
                if (y > heightPx - 20) break
                canvas.drawText(line, 10f, y, paint)
                y += 12f
            }
            pdfDocument.finishPage(page)
            FileOutputStream(outputFile).use { fos -> pdfDocument.writeTo(fos) }
            pdfDocument.close()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}

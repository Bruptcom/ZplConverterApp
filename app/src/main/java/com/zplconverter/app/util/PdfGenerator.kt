package com.zplconverter.app.util

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object PdfGenerator {

    fun generateDeclarationPdf(
        context: Context, outputFile: File,
        senderName: String, senderPhone: String, senderEmail: String,
        senderAddress: String, senderNumber: String, senderComplement: String,
        senderNeighborhood: String, senderCity: String, senderState: String, senderCep: String,
        receiverName: String, receiverPhone: String, receiverEmail: String,
        receiverAddress: String, receiverNumber: String, receiverComplement: String,
        receiverNeighborhood: String, receiverCity: String, receiverState: String, receiverCep: String,
        contentDescription: String, contentValue: String, contentWeight: String, orderNumber: String
    ): Boolean {
        return try {
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            val titlePaint = Paint().apply { textSize = 18f; color = Color.BLACK; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD); isAntiAlias = true }
            val subtitlePaint = Paint().apply { textSize = 12f; color = Color.DKGRAY; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD); isAntiAlias = true }
            val bodyPaint = Paint().apply { textSize = 10f; color = Color.BLACK; isAntiAlias = true }
            val smallPaint = Paint().apply { textSize = 8f; color = Color.GRAY; isAntiAlias = true }

            var y = 40f

            drawCenteredText(canvas, "DECLARACAO DE CONTEUDO", titlePaint, 595, y); y += 25f
            drawCenteredText(canvas, "Mercado Livre - Envio de Mercadorias", smallPaint, 595, y); y += 25f

            if (orderNumber.isNotBlank()) { canvas.drawText("N do Pedido: $orderNumber", 40f, y, subtitlePaint); y += 20f }

            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
            canvas.drawText("Data: ${dateFormat.format(Date())}", 40f, y, bodyPaint); y += 30f

            canvas.drawLine(40f, y, 555f, y, Paint().apply { strokeWidth = 1f; color = Color.LTGRAY }); y += 20f

            canvas.drawText("REMETENTE:", 40f, y, subtitlePaint); y += 18f
            canvas.drawText("Nome: $senderName", 50f, y, bodyPaint); y += 15f
            canvas.drawText("Telefone: $senderPhone    E-mail: $senderEmail", 50f, y, bodyPaint); y += 15f
            canvas.drawText("Endereco: $senderAddress, $senderNumber ${if (senderComplement.isNotBlank()) "- $senderComplement" else ""}", 50f, y, bodyPaint); y += 15f
            canvas.drawText("Bairro: $senderNeighborhood    CEP: $senderCep", 50f, y, bodyPaint); y += 15f
            canvas.drawText("Cidade: $senderCity - $senderState", 50f, y, bodyPaint); y += 25f

            canvas.drawLine(40f, y, 555f, y, Paint().apply { strokeWidth = 1f; color = Color.LTGRAY }); y += 20f

            canvas.drawText("DESTINATARIO:", 40f, y, subtitlePaint); y += 18f
            canvas.drawText("Nome: $receiverName", 50f, y, bodyPaint); y += 15f
            canvas.drawText("Telefone: $receiverPhone    E-mail: $receiverEmail", 50f, y, bodyPaint); y += 15f
            canvas.drawText("Endereco: $receiverAddress, $receiverNumber ${if (receiverComplement.isNotBlank()) "- $receiverComplement" else ""}", 50f, y, bodyPaint); y += 15f
            canvas.drawText("Bairro: $receiverNeighborhood    CEP: $receiverCep", 50f, y, bodyPaint); y += 15f
            canvas.drawText("Cidade: $receiverCity - $receiverState", 50f, y, bodyPaint); y += 25f

            canvas.drawLine(40f, y, 555f, y, Paint().apply { strokeWidth = 1f; color = Color.LTGRAY }); y += 20f

            canvas.drawText("CONTEUDO DA REMESSA:", 40f, y, subtitlePaint); y += 18f
            canvas.drawText("Descricao: $contentDescription", 50f, y, bodyPaint); y += 15f
            canvas.drawText("Valor Declarado: R$ $contentValue", 50f, y, bodyPaint); y += 15f
            canvas.drawText("Peso: $contentWeight kg", 50f, y, bodyPaint); y += 30f

            canvas.drawLine(40f, y, 555f, y, Paint().apply { strokeWidth = 1f; color = Color.LTGRAY }); y += 20f

            val legalText = "Declaro que as informacoes acima sao verdadeiras e que o conteudo nao contem produtos proibidos ou restritos conforme legislacao vigente. Estou ciente de que informacoes falsas podem acarretar penalidades legais."
            drawWrappedText(canvas, legalText, 50f, y, 505f, bodyPaint); y += 60f

            y += 30f
            canvas.drawLine(150f, y, 445f, y, Paint().apply { strokeWidth = 1f; color = Color.BLACK }); y += 15f
            drawCenteredText(canvas, "Assinatura do Remetente", bodyPaint, 595, y); y += 15f
            drawCenteredText(canvas, senderName, smallPaint, 595, y)

            y = 810f
            drawCenteredText(canvas, "Documento gerado automaticamente - ZPL Converter App", smallPaint, 595, y)

            pdfDocument.finishPage(page)
            FileOutputStream(outputFile).use { fos -> pdfDocument.writeTo(fos) }
            pdfDocument.close()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun drawCenteredText(canvas: android.graphics.Canvas, text: String, paint: Paint, canvasWidth: Int, y: Float) {
        val textWidth = paint.measureText(text)
        val x = (canvasWidth - textWidth) / 2
        canvas.drawText(text, x, y, paint)
    }

    private fun drawWrappedText(canvas: android.graphics.Canvas, text: String, x: Float, startY: Float, maxWidth: Float, paint: Paint) {
        val words = text.split(" ")
        var line = ""
        var y = startY
        for (word in words) {
            val testLine = if (line.isEmpty()) word else "$line $word"
            val width = paint.measureText(testLine)
            if (width > maxWidth) {
                canvas.drawText(line, x, y, paint)
                line = word
                y += paint.textSize + 4
            } else { line = testLine }
        }
        if (line.isNotEmpty()) canvas.drawText(line, x, y, paint)
    }
}

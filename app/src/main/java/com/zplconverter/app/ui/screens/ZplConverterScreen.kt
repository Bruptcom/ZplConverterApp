package com.zplconverter.app.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.zplconverter.app.util.ZplConverterUtil
import com.zplconverter.app.util.ZipHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZplConverterScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf("") }
    var convertedFiles by remember { mutableStateOf<List<File>>(emptyList()) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            isLoading = true
            statusMessage = "Processando arquivos..."
            convertedFiles = emptyList()

            scope.launch {
                val results = mutableListOf<File>()
                val outputDir = File(context.getExternalFilesDir(null), "converted_pdfs")
                outputDir.mkdirs()
                outputDir.listFiles()?.forEach { it.delete() }

                for (uri in uris) {
                    try {
                        val fileName = getFileName(context, uri) ?: "unknown"
                        if (fileName.endsWith(".zip", ignoreCase = true)) {
                            val zplFiles = ZipHandler.extractZplFromZip(context, uri)
                            for ((name, content) in zplFiles) {
                                val pdfFile = File(outputDir, "$name.pdf")
                                val success = ZplConverterUtil.convertZplToPdf(content, pdfFile)
                                if (success) results.add(pdfFile)
                            }
                        } else if (fileName.endsWith(".zpl", ignoreCase = true) || fileName.endsWith(".txt", ignoreCase = true)) {
                            val content = context.contentResolver.openInputStream(uri)?.bufferedReader()?.readText() ?: ""
                            val baseName = fileName.substringBeforeLast(".")
                            val pdfFile = File(outputDir, "$baseName.pdf")
                            val success = ZplConverterUtil.convertZplToPdf(content, pdfFile)
                            if (success) results.add(pdfFile)
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            statusMessage = "Erro: ${e.message}"
                        }
                    }
                }

                withContext(Dispatchers.Main) {
                    convertedFiles = results
                    isLoading = false
                    statusMessage = if (results.isNotEmpty()) {
                        "${results.size} PDF(s) gerado(s) com sucesso!"
                    } else {
                        "Nenhum PDF foi gerado. Verifique os arquivos."
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Conversor ZPL para PDF", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)
        ) {
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Selecione arquivos ZPL, TXT ou ZIP", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Aceita: .zpl, .txt, .zip (com ZPLs dentro)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { filePickerLauncher.launch("*/*") },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Selecionar Arquivo(s)", fontSize = 16.sp)
                    }
                }
            }

            if (isLoading) {
                Spacer(modifier = Modifier.height(24.dp))
                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Convertendo...", fontSize = 14.sp)
                }
            }

            if (statusMessage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (statusMessage.contains("sucesso")) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(text = statusMessage, modifier = Modifier.padding(16.dp), fontSize = 14.sp)
                }
            }

            if (convertedFiles.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text("PDFs Gerados:", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn {
                    items(convertedFiles) { file ->
                        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), shape = RoundedCornerShape(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = file.name, fontSize = 14.sp, modifier = Modifier.weight(1f))
                                TextButton(onClick = { shareFile(context, file) }) { Text("Compartilhar") }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun getFileName(context: Context, uri: Uri): String? {
    var name: String? = null
    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
        if (cursor.moveToFirst() && nameIndex >= 0) name = cursor.getString(nameIndex)
    }
    return name ?: uri.lastPathSegment?.substringAfterLast("/")
}

private fun shareFile(context: Context, file: File) {
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "application/pdf"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Compartilhar PDF"))
}

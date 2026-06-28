package com.zplconverter.app.ui.screens

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.zplconverter.app.data.api.ViaCepService
import com.zplconverter.app.util.PdfGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeclarationScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var senderName by remember { mutableStateOf("") }
    var senderPhone by remember { mutableStateOf("") }
    var senderEmail by remember { mutableStateOf("") }
    var senderCep by remember { mutableStateOf("") }
    var senderAddress by remember { mutableStateOf("") }
    var senderNumber by remember { mutableStateOf("") }
    var senderComplement by remember { mutableStateOf("") }
    var senderNeighborhood by remember { mutableStateOf("") }
    var senderCity by remember { mutableStateOf("") }
    var senderState by remember { mutableStateOf("") }

    var receiverName by remember { mutableStateOf("") }
    var receiverPhone by remember { mutableStateOf("") }
    var receiverEmail by remember { mutableStateOf("") }
    var receiverCep by remember { mutableStateOf("") }
    var receiverAddress by remember { mutableStateOf("") }
    var receiverNumber by remember { mutableStateOf("") }
    var receiverComplement by remember { mutableStateOf("") }
    var receiverNeighborhood by remember { mutableStateOf("") }
    var receiverCity by remember { mutableStateOf("") }
    var receiverState by remember { mutableStateOf("") }

    var contentDescription by remember { mutableStateOf("") }
    var contentValue by remember { mutableStateOf("") }
    var contentWeight by remember { mutableStateOf("") }
    var orderNumber by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(false) }

    val viaCepService = remember {
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build()
        Retrofit.Builder()
            .baseUrl("https://viacep.com.br/ws/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ViaCepService::class.java)
    }

    fun searchCep(cep: String, isSender: Boolean) {
        val cleanCep = cep.replace("[^0-9]".toRegex(), "")
        if (cleanCep.length != 8) {
            Toast.makeText(context, "CEP invalido", Toast.LENGTH_SHORT).show()
            return
        }
        isLoading = true
        scope.launch {
            try {
                val response = withContext(Dispatchers.IO) { viaCepService.getAddressByCep(cleanCep).execute() }
                if (response.isSuccessful && response.body() != null) {
                    val addr = response.body()!!
                    if (addr.erro != true) {
                        if (isSender) {
                            senderAddress = addr.logradouro ?: ""
                            senderNeighborhood = addr.bairro ?: ""
                            senderCity = addr.localidade ?: ""
                            senderState = addr.uf ?: ""
                        } else {
                            receiverAddress = addr.logradouro ?: ""
                            receiverNeighborhood = addr.bairro ?: ""
                            receiverCity = addr.localidade ?: ""
                            receiverState = addr.uf ?: ""
                        }
                    } else {
                        Toast.makeText(context, "CEP nao encontrado", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Erro ao buscar CEP: ${e.message}", Toast.LENGTH_SHORT).show()
            }
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Declaracao de Conteudo", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Voltar") }
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
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(scrollState).padding(16.dp)
        ) {
            Text("REMETENTE", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(value = senderName, onValueChange = { senderName = it }, label = { Text("Nome Completo *") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp))
            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = senderPhone, onValueChange = { senderPhone = it }, label = { Text("Telefone") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), shape = RoundedCornerShape(8.dp))
                OutlinedTextField(value = senderEmail, onValueChange = { senderEmail = it }, label = { Text("E-mail") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), shape = RoundedCornerShape(8.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = senderCep, onValueChange = { senderCep = it }, label = { Text("CEP") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), shape = RoundedCornerShape(8.dp))
                IconButton(onClick = { searchCep(senderCep, true) }) { Icon(Icons.Default.Search, contentDescription = "Buscar CEP") }
            }
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(value = senderAddress, onValueChange = { senderAddress = it }, label = { Text("Logradouro") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp))
            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = senderNumber, onValueChange = { senderNumber = it }, label = { Text("Numero") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), shape = RoundedCornerShape(8.dp))
                OutlinedTextField(value = senderComplement, onValueChange = { senderComplement = it }, label = { Text("Complemento") }, modifier = Modifier.weight(2f), shape = RoundedCornerShape(8.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = senderNeighborhood, onValueChange = { senderNeighborhood = it }, label = { Text("Bairro") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp))
                OutlinedTextField(value = senderCity, onValueChange = { senderCity = it }, label = { Text("Cidade") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp))
                OutlinedTextField(value = senderState, onValueChange = { senderState = it }, label = { Text("UF") }, modifier = Modifier.width(60.dp), shape = RoundedCornerShape(8.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))
            Divider()
            Spacer(modifier = Modifier.height(24.dp))

            Text("DESTINATARIO", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(value = receiverName, onValueChange = { receiverName = it }, label = { Text("Nome Completo *") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp))
            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = receiverPhone, onValueChange = { receiverPhone = it }, label = { Text("Telefone") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), shape = RoundedCornerShape(8.dp))
                OutlinedTextField(value = receiverEmail, onValueChange = { receiverEmail = it }, label = { Text("E-mail") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), shape = RoundedCornerShape(8.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = receiverCep, onValueChange = { receiverCep = it }, label = { Text("CEP") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), shape = RoundedCornerShape(8.dp))
                IconButton(onClick = { searchCep(receiverCep, false) }) { Icon(Icons.Default.Search, contentDescription = "Buscar CEP") }
            }
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(value = receiverAddress, onValueChange = { receiverAddress = it }, label = { Text("Logradouro") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp))
            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = receiverNumber, onValueChange = { receiverNumber = it }, label = { Text("Numero") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), shape = RoundedCornerShape(8.dp))
                OutlinedTextField(value = receiverComplement, onValueChange = { receiverComplement = it }, label = { Text("Complemento") }, modifier = Modifier.weight(2f), shape = RoundedCornerShape(8.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = receiverNeighborhood, onValueChange = { receiverNeighborhood = it }, label = { Text("Bairro") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp))
                OutlinedTextField(value = receiverCity, onValueChange = { receiverCity = it }, label = { Text("Cidade") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp))
                OutlinedTextField(value = receiverState, onValueChange = { receiverState = it }, label = { Text("UF") }, modifier = Modifier.width(60.dp), shape = RoundedCornerShape(8.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))
            Divider()
            Spacer(modifier = Modifier.height(24.dp))

            Text("CONTEUDO", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(value = orderNumber, onValueChange = { orderNumber = it }, label = { Text("N do Pedido (Mercado Livre)") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp))
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(value = contentDescription, onValueChange = { contentDescription = it }, label = { Text("Descricao do Conteudo *") }, modifier = Modifier.fillMaxWidth().height(100.dp), shape = RoundedCornerShape(8.dp))
            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = contentValue, onValueChange = { contentValue = it }, label = { Text("Valor Declarado (R$)") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), shape = RoundedCornerShape(8.dp))
                OutlinedTextField(value = contentWeight, onValueChange = { contentWeight = it }, label = { Text("Peso (kg)") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), shape = RoundedCornerShape(8.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (senderName.isBlank() || receiverName.isBlank() || contentDescription.isBlank()) {
                        Toast.makeText(context, "Preencha os campos obrigatorios (*)", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    val outputDir = File(context.getExternalFilesDir(null), "declarations")
                    outputDir.mkdirs()
                    val fileName = "declaracao_conteudo_${System.currentTimeMillis()}.pdf"
                    val pdfFile = File(outputDir, fileName)

                    val success = PdfGenerator.generateDeclarationPdf(
                        context = context, outputFile = pdfFile,
                        senderName = senderName, senderPhone = senderPhone, senderEmail = senderEmail,
                        senderAddress = senderAddress, senderNumber = senderNumber, senderComplement = senderComplement,
                        senderNeighborhood = senderNeighborhood, senderCity = senderCity, senderState = senderState, senderCep = senderCep,
                        receiverName = receiverName, receiverPhone = receiverPhone, receiverEmail = receiverEmail,
                        receiverAddress = receiverAddress, receiverNumber = receiverNumber, receiverComplement = receiverComplement,
                        receiverNeighborhood = receiverNeighborhood, receiverCity = receiverCity, receiverState = receiverState, receiverCep = receiverCep,
                        contentDescription = contentDescription, contentValue = contentValue, contentWeight = contentWeight, orderNumber = orderNumber
                    )

                    if (success) {
                        Toast.makeText(context, "Declaracao gerada com sucesso!", Toast.LENGTH_SHORT).show()
                        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", pdfFile)
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "application/pdf"
                            putExtra(Intent.EXTRA_STREAM, uri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(intent, "Compartilhar Declaracao"))
                    } else {
                        Toast.makeText(context, "Erro ao gerar declaracao", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Gerar Declaracao em PDF", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            if (isLoading) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

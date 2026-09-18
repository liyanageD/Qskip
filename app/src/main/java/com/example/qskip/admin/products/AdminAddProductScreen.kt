package com.example.qskip.admin.products

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.qskip.domain.model.ProductVariant
import com.example.qskip.utils.QrDownloadUtil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAddProductScreen(
    onNavigateBack: () -> Unit,
    viewModel: AdminProductViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    var name by remember { mutableStateOf("") }
    var productCode by remember { mutableStateOf("QSK-TSHIRT-00" + (1..9).random()) }
    var description by remember { mutableStateOf("") }
    var basePrice by remember { mutableStateOf("2500") }

    // Image Picker State
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var imageBytes by remember { mutableStateOf<ByteArray?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
        if (uri != null) {
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            imageBytes = bytes
        }
    }

    // Variant Form State
    var variantSize by remember { mutableStateOf("M") }
    var variantColor by remember { mutableStateOf("Black") }
    var variantStock by remember { mutableStateOf("10") }
    val variants = remember { mutableStateListOf<ProductVariant>() }

    LaunchedEffect(uiState.isSaveSuccess) {
        if (uiState.isSaveSuccess) {
            viewModel.resetSaveState()
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add New Product") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            if (uiState.error != null) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                ) {
                    Text(
                        text = uiState.error!!,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            // Image Picker UI
            Text("Product Image (Supabase Storage)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                contentAlignment = Alignment.Center
            ) {
                if (selectedImageUri != null) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = "Selected Image",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(onClick = { imagePickerLauncher.launch("image/*") }) {
                            Text("Change Image")
                        }
                    }
                } else {
                    OutlinedButton(
                        onClick = { imagePickerLauncher.launch("image/*") },
                        modifier = Modifier.fillMaxWidth().height(80.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Add, contentDescription = "Upload Image")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Select Image from Gallery")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Product Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = productCode,
                onValueChange = { productCode = it },
                label = { Text("Product QR Code / Identifier") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = basePrice,
                onValueChange = { basePrice = it },
                label = { Text("Base Price (Rs.)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Add Variant Section
            Text("Product Variants (Size / Color / Stock)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = variantSize,
                    onValueChange = { variantSize = it },
                    label = { Text("Size") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = variantColor,
                    onValueChange = { variantColor = it },
                    label = { Text("Color") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = variantStock,
                    onValueChange = { variantStock = it },
                    label = { Text("Stock") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    val price = basePrice.toDoubleOrNull() ?: 0.0
                    val stock = variantStock.toIntOrNull() ?: 0
                    if (variantSize.isNotBlank() && variantColor.isNotBlank()) {
                        variants.add(
                            ProductVariant(
                                size = variantSize,
                                color = variantColor,
                                price = price,
                                stock = stock
                            )
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add Variant")
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Display added variants
            variants.forEachIndexed { index, variant ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("${variant.size} / ${variant.color} - Stock: ${variant.stock} (Rs. ${variant.price})")
                        TextButton(onClick = { variants.removeAt(index) }) {
                            Text("Remove", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Generate & Preview Product QR
            Button(
                onClick = { viewModel.generateProductQr(productCode) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Text("Generate Product QR")
            }

            uiState.generatedQrBitmap?.let { qrBitmap ->
                Spacer(modifier = Modifier.height(16.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        bitmap = qrBitmap.asImageBitmap(),
                        contentDescription = "Generated QR",
                        modifier = Modifier.size(180.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Code: $productCode", style = MaterialTheme.typography.bodySmall)

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = {
                            QrDownloadUtil.saveQrCodeToDownloads(
                                context = context,
                                qrBitmap = qrBitmap,
                                productName = name.ifBlank { "Product" },
                                productCode = productCode
                            )
                        }
                    ) {
                        Text("Download QR PNG")
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    val price = basePrice.toDoubleOrNull() ?: 0.0
                    viewModel.saveProductWithImage(
                        name = name,
                        productCode = productCode,
                        description = description,
                        basePrice = price,
                        imageBytes = imageBytes,
                        variants = variants.toList()
                    )
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = !uiState.isLoading && name.isNotBlank() && productCode.isNotBlank()
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                } else {
                    Text("Save Product to Store")
                }
            }
        }
    }
}

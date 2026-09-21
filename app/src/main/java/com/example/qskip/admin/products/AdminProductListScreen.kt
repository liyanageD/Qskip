package com.example.qskip.admin.products

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.qskip.domain.model.Product
import com.example.qskip.utils.QrDownloadUtil
import com.example.qskip.utils.toCurrency

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminProductListScreen(
    onNavigateToAddProduct: () -> Unit,
    onNavigateToEditProduct: (String) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: AdminProductViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    var productToDelete by remember { mutableStateOf<Product?>(null) }
    var productForQr by remember { mutableStateOf<Product?>(null) }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.resetSaveState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Product Catalog Management") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAddProduct) {
                Icon(Icons.Default.Add, contentDescription = "Add Product")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Search Input
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::onSearchQueryChanged,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search products by name, code, size, color...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            val filteredProducts = uiState.products.filter { p ->
                p.name.contains(uiState.searchQuery, ignoreCase = true) ||
                        p.productCode.contains(uiState.searchQuery, ignoreCase = true) ||
                        p.size.contains(uiState.searchQuery, ignoreCase = true) ||
                        p.color.contains(uiState.searchQuery, ignoreCase = true)
            }

            if (uiState.isLoading && uiState.products.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (filteredProducts.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No products in catalog matching query.")
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredProducts, key = { it.productId }) { product ->
                        AdminProductCard(
                            product = product,
                            onEdit = { onNavigateToEditProduct(product.productId) },
                            onDelete = { productToDelete = product },
                            onShowQr = {
                                productForQr = product
                                viewModel.generateProductQr(product.productCode)
                            }
                        )
                    }
                }
            }
        }

        // Delete Confirmation Dialog
        productToDelete?.let { product ->
            AlertDialog(
                onDismissRequest = { productToDelete = null },
                title = { Text("Delete Product?") },
                text = { Text("Are you sure you want to delete '${product.name}' (${product.size}/${product.color})? This action will deactivate the product from the store.") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteProduct(product.productId)
                            productToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { productToDelete = null }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // QR Preview & Download Dialog
        productForQr?.let { product ->
            AlertDialog(
                onDismissRequest = { productForQr = null },
                title = { Text("Product QR Code") },
                text = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        uiState.generatedQrBitmap?.let { bitmap ->
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "Product QR",
                                modifier = Modifier.size(200.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(product.name, fontWeight = FontWeight.Bold)
                        Text("Code: ${product.productCode} • ${product.size} / ${product.color}", style = MaterialTheme.typography.bodySmall)
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            uiState.generatedQrBitmap?.let { bitmap ->
                                QrDownloadUtil.saveQrCodeToDownloads(
                                    context = context,
                                    qrBitmap = bitmap,
                                    productName = product.name,
                                    productCode = product.productCode
                                )
                            }
                            productForQr = null
                        }
                    ) {
                        Text("Download QR PNG")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { productForQr = null }) {
                        Text("Close")
                    }
                }
            )
        }
    }
}

@Composable
fun AdminProductCard(
    product: Product,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onShowQr: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val imageUrl = product.imageUrls.firstOrNull()
                if (imageUrl != null) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = product.name,
                        modifier = Modifier
                            .size(70.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No Img", style = MaterialTheme.typography.bodySmall)
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(product.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Code: ${product.productCode}", style = MaterialTheme.typography.bodySmall)
                    Text("${product.size} • ${product.color} • Stock: ${product.stockQuantity}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Rs. ${product.price.toCurrency()}", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row {
                    OutlinedButton(onClick = onEdit, modifier = Modifier.padding(end = 8.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Edit")
                    }

                    OutlinedButton(onClick = onShowQr) {
                        Icon(Icons.Default.List, contentDescription = "QR", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("QR")
                    }
                }

                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

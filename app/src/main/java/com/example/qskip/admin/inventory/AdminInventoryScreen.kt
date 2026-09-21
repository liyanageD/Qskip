package com.example.qskip.admin.inventory

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.qskip.domain.model.Product
import com.example.qskip.utils.toCurrency

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminInventoryScreen(
    onNavigateBack: () -> Unit,
    viewModel: AdminInventoryViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearFeedback()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Inventory Stock Control") },
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
        ) {
            // Search Bar
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::onSearchQueryChanged,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search inventory by name, code, size, color...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Filter Chips
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = uiState.filterLowStockOnly,
                    onClick = viewModel::toggleLowStockFilter,
                    label = { Text("Low Stock") }
                )
                FilterChip(
                    selected = uiState.filterOutOfStockOnly,
                    onClick = viewModel::toggleOutOfStockFilter,
                    label = { Text("Out of Stock") }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Filtered Products
            val filteredProducts = uiState.products.filter { p ->
                val matchesSearch = p.name.contains(uiState.searchQuery, ignoreCase = true) ||
                        p.productCode.contains(uiState.searchQuery, ignoreCase = true) ||
                        p.color.contains(uiState.searchQuery, ignoreCase = true) ||
                        p.size.contains(uiState.searchQuery, ignoreCase = true)

                val matchesStock = when {
                    uiState.filterLowStockOnly -> p.stockQuantity <= p.lowStockThreshold
                    uiState.filterOutOfStockOnly -> p.stockQuantity == 0
                    else -> true
                }

                matchesSearch && matchesStock
            }

            if (uiState.isLoading && uiState.products.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (filteredProducts.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No matching inventory items found.")
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredProducts, key = { it.productId }) { product ->
                        InventoryItemCard(
                            product = product,
                            onUpdateStock = { newStock -> viewModel.updateStock(product.productId, newStock) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InventoryItemCard(
    product: Product,
    onUpdateStock: (Int) -> Unit
) {
    var editStockText by remember(product.stockQuantity) { mutableStateOf(product.stockQuantity.toString()) }

    val statusColor = when {
        product.stockQuantity == 0 -> MaterialTheme.colorScheme.error
        product.stockQuantity <= product.lowStockThreshold -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.primary
    }

    val statusText = when {
        product.stockQuantity == 0 -> "OUT OF STOCK"
        product.stockQuantity <= product.lowStockThreshold -> "LOW STOCK"
        else -> "IN STOCK"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(product.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Code: ${product.productCode} • ${product.size} / ${product.color} • Rs. ${product.price.toCurrency()}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Surface(
                    color = statusColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = statusText,
                        color = statusColor,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Stock Quantity:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(
                        onClick = { onUpdateStock(maxOf(0, product.stockQuantity - 1)) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        MinusIcon(tint = MaterialTheme.colorScheme.onSurface)
                    }

                    StockInputField(
                        value = editStockText,
                        onValueChange = {
                            editStockText = it
                            val newStock = it.toIntOrNull()
                            if (newStock != null && newStock >= 0) {
                                onUpdateStock(newStock)
                            }
                        }
                    )

                    IconButton(
                        onClick = { onUpdateStock(product.stockQuantity + 1) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Increase", modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun MinusIcon(
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onSurface
) {
    Box(
        modifier = modifier
            .width(14.dp)
            .height(2.5.dp)
            .background(tint, shape = RoundedCornerShape(1.dp))
    )
}

@Composable
fun StockInputField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier
            .widthIn(min = 72.dp, max = 110.dp)
            .height(44.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                singleLine = true,
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary)
            )
        }
    }
}

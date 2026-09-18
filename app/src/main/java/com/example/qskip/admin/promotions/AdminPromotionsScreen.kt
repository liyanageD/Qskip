package com.example.qskip.admin.promotions

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.qskip.domain.model.Promotion

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPromotionsScreen(
    onNavigateBack: () -> Unit,
    viewModel: AdminPromotionsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var titleInput by remember { mutableStateOf("") }
    var codeInput by remember { mutableStateOf("") }
    var valueInput by remember { mutableStateOf("10") }
    var discountType by remember { mutableStateOf("PERCENTAGE") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Promotions & Discounts") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.setShowAddDialog(true) }) {
                Icon(Icons.Default.Add, contentDescription = "Add Promotion")
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            if (uiState.isLoading && uiState.promotions.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.promotions.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No promotions created yet.")
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.promotions, key = { it.promotionId }) { promo ->
                        PromotionCard(
                            promotion = promo,
                            onToggleActive = { active -> viewModel.togglePromotion(promo.promotionId, active) }
                        )
                    }
                }
            }

            if (uiState.showAddDialog) {
                AlertDialog(
                    onDismissRequest = { viewModel.setShowAddDialog(false) },
                    title = { Text("Create New Promotion") },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = titleInput,
                                onValueChange = { titleInput = it },
                                label = { Text("Title (e.g. Summer Sale)") },
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = codeInput,
                                onValueChange = { codeInput = it },
                                label = { Text("Promo Code (e.g. SUMMER10)") },
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = valueInput,
                                onValueChange = { valueInput = it },
                                label = { Text("Discount Value (% or Rs.)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilterChip(
                                    selected = discountType == "PERCENTAGE",
                                    onClick = { discountType = "PERCENTAGE" },
                                    label = { Text("% Off") }
                                )
                                FilterChip(
                                    selected = discountType == "FIXED",
                                    onClick = { discountType = "FIXED" },
                                    label = { Text("Fixed Rs. Off") }
                                )
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                val value = valueInput.toDoubleOrNull() ?: 0.0
                                viewModel.savePromotion(titleInput, codeInput, discountType, value)
                                titleInput = ""
                                codeInput = ""
                            }
                        ) {
                            Text("Save")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { viewModel.setShowAddDialog(false) }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun PromotionCard(
    promotion: Promotion,
    onToggleActive: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(promotion.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text("Code: ${promotion.code}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                Text(
                    text = if (promotion.discountType == "PERCENTAGE") "${promotion.discountValue}% Discount" else "Rs. ${promotion.discountValue} Off",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Switch(
                checked = promotion.active,
                onCheckedChange = onToggleActive
            )
        }
    }
}

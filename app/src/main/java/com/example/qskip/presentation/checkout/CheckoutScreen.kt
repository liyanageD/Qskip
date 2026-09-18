package com.example.qskip.presentation.checkout

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    onNavigateBack: () -> Unit,
    onPaymentSuccess: () -> Unit, // Will navigate to Success/Exit QR
    viewModel: CheckoutViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val order = uiState.order

    LaunchedEffect(uiState.paymentSuccess) {
        if (uiState.paymentSuccess) {
            onPaymentSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Checkout") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            order?.let {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    shadowElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .windowInsetsPadding(WindowInsets.navigationBars)
                    ) {
                        Button(
                            onClick = viewModel::processMockPayment,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            enabled = !uiState.isPaymentProcessing && !uiState.paymentSuccess
                        ) {
                            if (uiState.isPaymentProcessing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Processing...")
                            } else {
                                Text("Pay Rs. ${it.total}", style = MaterialTheme.typography.titleMedium)
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (order != null) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (uiState.error != null) {
                    item {
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = uiState.error!!,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }

                // Order Summary Section
                item {
                    Text(
                        text = "Order Summary",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            order.items.forEach { item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("${item.quantity}x ${item.name} (${item.size}, ${item.color})", modifier = Modifier.weight(1f))
                                    Text("Rs. ${item.subtotal}", fontWeight = FontWeight.SemiBold)
                                }
                            }
                            
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                            
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Total", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text("Rs. ${order.total}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Rewards Section
                item {
                    Text(
                        text = "Rewards",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Star, contentDescription = "Rewards", tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("Available Points: ${uiState.availableRewardPoints}", fontWeight = FontWeight.Bold)
                                    Text("1 point = Rs. 0.5 discount", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                            
                            if (uiState.availableRewardPoints > 0 && order.rewardDiscount == 0.0) {
                                Button(onClick = { viewModel.applyRewardPoints(uiState.availableRewardPoints) }) {
                                    Text("Apply")
                                }
                            } else if (order.rewardDiscount > 0) {
                                OutlinedButton(onClick = { viewModel.applyRewardPoints(0) }) {
                                    Text("Remove")
                                }
                            }
                        }
                    }
                }

                // Payment Method (Mock)
                item {
                    Text(
                        text = "Payment Method",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = "Card")
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("Qskip Demo Card", fontWeight = FontWeight.Bold)
                                Text("**** **** **** 4242", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Note: This is a demo environment. No real financial transaction will occur.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

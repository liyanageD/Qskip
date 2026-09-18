package com.example.qskip.presentation.receipt

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.qskip.presentation.exit.ExitQrViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptScreen(
    orderId: String,
    onNavigateBack: () -> Unit,
    onShowExitQr: (String) -> Unit,
    viewModel: ExitQrViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val order = uiState.order

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Digital Receipt") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (order != null) {
            val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
            val dateString = dateFormat.format(Date(order.createdAt))

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Qskip Store",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text("Smart Fashion Self-Checkout", style = MaterialTheme.typography.bodySmall)
                                
                                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Order ID:")
                                    Text("#${order.orderId}", fontWeight = FontWeight.Bold)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Date:")
                                    Text(dateString)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Payment Ref:")
                                    Text(order.paymentReference ?: "N/A")
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Status:")
                                    Text(order.orderStatus, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }

                                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                                Text("Purchased Items", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))

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
                                    Text("Subtotal")
                                    Text("Rs. ${order.subtotal}")
                                }
                                if (order.discount > 0) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Discount")
                                        Text("- Rs. ${order.discount}")
                                    }
                                }
                                if (order.rewardDiscount > 0) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Rewards Applied")
                                        Text("- Rs. ${order.rewardDiscount}")
                                    }
                                }

                                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Total Paid", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                    Text("Rs. ${order.total}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }

                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Button(
                            onClick = { onShowExitQr(order.orderId) },
                            modifier = Modifier.fillMaxWidth().height(50.dp)
                        ) {
                            Text("View Exit QR Code")
                        }
                    }
                }
            }
        }
    }
}

package com.example.qskip.admin.exitverification

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.qskip.presentation.scanner.CameraPermissionWrapper
import com.example.qskip.presentation.scanner.CameraPreview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminExitScannerScreen(
    onNavigateBack: () -> Unit,
    viewModel: AdminExitScannerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val order = uiState.scannedOrder

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Exit Verification Scanner") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (order == null) {
                // Show Camera preview with permission wrapper to scan exit token
                CameraPermissionWrapper {
                    CameraPreview(
                        onQrCodeScanned = viewModel::onExitQrScanned
                    )
                }

                if (uiState.isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }

                if (uiState.error != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(24.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = uiState.error!!,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = viewModel::resetScannerState) {
                                Text("Try Again")
                            }
                        }
                    }
                }
            } else {
                // Display Order Details for Staff Comparison
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("ORDER BILL TOTAL", style = MaterialTheme.typography.labelMedium)
                            Text("Rs. ${order.total}", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text("Order #${order.orderId}", style = MaterialTheme.typography.bodyMedium)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Compare physical items with bill:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(order.items) { item ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(item.name, fontWeight = FontWeight.Bold)
                                        Text("Size: ${item.size} | Color: ${item.color}", style = MaterialTheme.typography.bodySmall)
                                    }
                                    Text("Qty: ${item.quantity}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (uiState.isSuccess) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "Exit Verified & Order Completed!",
                                modifier = Modifier.padding(16.dp),
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = viewModel::resetScannerState,
                            modifier = Modifier.fillMaxWidth().height(50.dp)
                        ) {
                            Text("Scan Next Customer")
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.setShowFlagDialog(true) },
                                modifier = Modifier.weight(1f).height(50.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                            ) {
                                Text("REJECT / FLAG")
                            }

                            Button(
                                onClick = viewModel::verifyExit,
                                modifier = Modifier.weight(1f).height(50.dp),
                                enabled = !uiState.isLoading
                            ) {
                                Text("VERIFY EXIT")
                            }
                        }
                    }
                }
            }

            if (uiState.showFlagDialog) {
                AlertDialog(
                    onDismissRequest = { viewModel.setShowFlagDialog(false) },
                    title = { Text("Flag Order Exit") },
                    text = {
                        OutlinedTextField(
                            value = uiState.flagReason,
                            onValueChange = viewModel::onFlagReasonChanged,
                            label = { Text("Reason (e.g. Item count mismatch)") },
                            singleLine = true
                        )
                    },
                    confirmButton = {
                        Button(onClick = viewModel::flagExit) {
                            Text("Confirm Flag")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { viewModel.setShowFlagDialog(false) }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}

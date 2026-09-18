package com.example.qskip.presentation.receipt

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun SuccessScreen(
    onShowExitQr: () -> Unit,
    onContinueShopping: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(100.dp)
        ) {
            Icon(
                Icons.Default.Check,
                contentDescription = "Success",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Payment Successful!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Your order has been completed successfully.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onShowExitQr,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("Show Exit QR Code", style = MaterialTheme.typography.titleMedium)
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = onContinueShopping,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("Continue Shopping", style = MaterialTheme.typography.titleMedium)
        }
    }
}

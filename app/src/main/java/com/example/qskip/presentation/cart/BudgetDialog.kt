package com.example.qskip.presentation.cart

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.text.input.KeyboardType

@Composable
fun BudgetDialog(
    currentBudget: Double,
    onDismissRequest: () -> Unit,
    onBudgetUpdated: (Double) -> Unit
) {
    var budgetInput by remember { mutableStateOf(if (currentBudget > 0) currentBudget.toString() else "") }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Set Shopping Budget") },
        text = {
            OutlinedTextField(
                value = budgetInput,
                onValueChange = { 
                    budgetInput = it
                    isError = false
                },
                label = { Text("Budget (Rs.)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = isError,
                singleLine = true
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    val budget = budgetInput.toDoubleOrNull()
                    if (budget != null && budget >= 0) {
                        onBudgetUpdated(budget)
                    } else {
                        isError = true
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancel")
            }
        }
    )
}

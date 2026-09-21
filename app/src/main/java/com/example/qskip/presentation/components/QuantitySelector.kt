package com.example.qskip.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun QuantitySelector(
    quantity: Int,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    modifier: Modifier = Modifier,
    minQuantity: Int = 1,
    maxQuantity: Int = Int.MAX_VALUE,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    contentColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        ) {
            val canDecrease = quantity > minQuantity
            val decreaseTint = if (canDecrease) contentColor else contentColor.copy(alpha = 0.38f)

            // Decrease (−) Button
            IconButton(
                onClick = onDecrease,
                enabled = canDecrease,
                modifier = Modifier.size(36.dp)
            ) {
                MinusIcon(
                    size = 14.dp,
                    strokeHeight = 2.5.dp,
                    tint = decreaseTint
                )
            }

            // Quantity Number Display
            Text(
                text = quantity.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = contentColor,
                modifier = Modifier
                    .widthIn(min = 28.dp)
                    .padding(horizontal = 4.dp)
            )

            val canIncrease = quantity < maxQuantity
            val increaseTint = if (canIncrease) contentColor else contentColor.copy(alpha = 0.38f)

            // Increase (+) Button
            IconButton(
                onClick = onIncrease,
                enabled = canIncrease,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Increase",
                    tint = increaseTint,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

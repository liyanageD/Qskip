package com.example.qskip.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun MinusIcon(
    modifier: Modifier = Modifier,
    size: Dp = 14.dp,
    strokeHeight: Dp = 2.5.dp,
    tint: Color = MaterialTheme.colorScheme.onSurface
) {
    Box(
        modifier = modifier
            .width(size)
            .height(strokeHeight)
            .background(tint, shape = RoundedCornerShape(1.dp))
    )
}

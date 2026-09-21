package com.example.qskip.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun QrScannerIcon(
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    tint: Color = MaterialTheme.colorScheme.primary
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val stroke = w * 0.1f
        val cornerLen = w * 0.28f

        // Top-Left corner
        drawPath(
            path = Path().apply {
                moveTo(0f, cornerLen)
                lineTo(0f, stroke / 2)
                lineTo(cornerLen, stroke / 2)
            },
            color = tint,
            style = Stroke(width = stroke)
        )

        // Top-Right corner
        drawPath(
            path = Path().apply {
                moveTo(w - cornerLen, stroke / 2)
                lineTo(w, stroke / 2)
                lineTo(w, cornerLen)
            },
            color = tint,
            style = Stroke(width = stroke)
        )

        // Bottom-Left corner
        drawPath(
            path = Path().apply {
                moveTo(0f, h - cornerLen)
                lineTo(0f, h - stroke / 2)
                lineTo(cornerLen, h - stroke / 2)
            },
            color = tint,
            style = Stroke(width = stroke)
        )

        // Bottom-Right corner
        drawPath(
            path = Path().apply {
                moveTo(w - cornerLen, h - stroke / 2)
                lineTo(w, h - stroke / 2)
                lineTo(w, h - cornerLen)
            },
            color = tint,
            style = Stroke(width = stroke)
        )

        // Laser scan line
        drawLine(
            color = tint,
            start = Offset(w * 0.15f, h * 0.5f),
            end = Offset(w * 0.85f, h * 0.5f),
            strokeWidth = stroke * 0.8f
        )
    }
}

@Composable
fun WalletIcon(
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    tint: Color = MaterialTheme.colorScheme.primary
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        // Wallet main body
        drawRoundRect(
            color = tint,
            topLeft = Offset(0f, h * 0.15f),
            size = Size(w, h * 0.7f),
            cornerRadius = CornerRadius(w * 0.15f)
        )

        // Flap / Clasp cutout
        drawRoundRect(
            color = Color.White.copy(alpha = 0.35f),
            topLeft = Offset(w * 0.6f, h * 0.35f),
            size = Size(w * 0.4f, h * 0.3f),
            cornerRadius = CornerRadius(w * 0.08f)
        )

        // Clasp dot
        drawCircle(
            color = Color.White,
            radius = w * 0.06f,
            center = Offset(w * 0.75f, h * 0.5f)
        )
    }
}

@Composable
fun MoneyIcon(
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    tint: Color = MaterialTheme.colorScheme.primary
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        // Banknote Rectangle Body
        drawRoundRect(
            color = tint,
            topLeft = Offset(0f, h * 0.2f),
            size = Size(w, h * 0.6f),
            cornerRadius = CornerRadius(w * 0.1f)
        )

        // Inner Currency Circle
        drawCircle(
            color = Color.White,
            radius = w * 0.16f,
            center = Offset(w * 0.5f, h * 0.5f)
        )

        // Corner circles
        drawCircle(
            color = Color.White.copy(alpha = 0.4f),
            radius = w * 0.05f,
            center = Offset(w * 0.15f, h * 0.35f)
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.4f),
            radius = w * 0.05f,
            center = Offset(w * 0.85f, h * 0.35f)
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.4f),
            radius = w * 0.05f,
            center = Offset(w * 0.15f, h * 0.65f)
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.4f),
            radius = w * 0.05f,
            center = Offset(w * 0.85f, h * 0.65f)
        )
    }
}

@Composable
fun PriceTagIcon(
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    tint: Color = MaterialTheme.colorScheme.primary
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        // Tag polygon
        val path = Path().apply {
            moveTo(w * 0.15f, h * 0.15f)
            lineTo(w * 0.6f, h * 0.15f)
            lineTo(w * 0.95f, h * 0.5f)
            lineTo(w * 0.5f, h * 0.95f)
            lineTo(w * 0.15f, h * 0.6f)
            close()
        }
        drawPath(path = path, color = tint)

        // Hole
        drawCircle(
            color = Color.White,
            radius = w * 0.08f,
            center = Offset(w * 0.35f, h * 0.35f)
        )
    }
}

@Composable
fun LoyaltyGiftIcon(
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    tint: Color = MaterialTheme.colorScheme.primary
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        // Gift Box Base
        drawRoundRect(
            color = tint,
            topLeft = Offset(w * 0.1f, h * 0.4f),
            size = Size(w * 0.8f, h * 0.55f),
            cornerRadius = CornerRadius(w * 0.08f)
        )

        // Gift Box Lid
        drawRoundRect(
            color = tint,
            topLeft = Offset(w * 0.05f, h * 0.28f),
            size = Size(w * 0.9f, h * 0.15f),
            cornerRadius = CornerRadius(w * 0.05f)
        )

        // Vertical Ribbon
        drawRect(
            color = Color.White.copy(alpha = 0.6f),
            topLeft = Offset(w * 0.42f, h * 0.28f),
            size = Size(w * 0.16f, h * 0.67f)
        )

        // Bow Loops
        drawCircle(
            color = tint,
            radius = w * 0.12f,
            center = Offset(w * 0.35f, h * 0.18f),
            style = Stroke(width = w * 0.06f)
        )
        drawCircle(
            color = tint,
            radius = w * 0.12f,
            center = Offset(w * 0.65f, h * 0.18f),
            style = Stroke(width = w * 0.06f)
        )
    }
}

@Composable
fun MenCategoryIcon(
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    tint: Color = MaterialTheme.colorScheme.primary
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        val bodyPath = Path().apply {
            moveTo(w * 0.2f, h * 0.2f)
            lineTo(w * 0.4f, h * 0.2f)
            lineTo(w * 0.5f, h * 0.35f)
            lineTo(w * 0.6f, h * 0.2f)
            lineTo(w * 0.8f, h * 0.2f)
            lineTo(w * 0.85f, h * 0.9f)
            lineTo(w * 0.15f, h * 0.9f)
            close()
        }
        drawPath(path = bodyPath, color = tint)

        val tiePath = Path().apply {
            moveTo(w * 0.45f, h * 0.35f)
            lineTo(w * 0.55f, h * 0.35f)
            lineTo(w * 0.58f, h * 0.75f)
            lineTo(w * 0.5f, h * 0.85f)
            lineTo(w * 0.42f, h * 0.75f)
            close()
        }
        drawPath(path = tiePath, color = Color.White)
    }
}

@Composable
fun WomenCategoryIcon(
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    tint: Color = MaterialTheme.colorScheme.primary
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        val dressPath = Path().apply {
            moveTo(w * 0.35f, h * 0.15f)
            lineTo(w * 0.65f, h * 0.15f)
            lineTo(w * 0.58f, h * 0.45f)
            lineTo(w * 0.85f, h * 0.9f)
            lineTo(w * 0.15f, h * 0.9f)
            lineTo(w * 0.42f, h * 0.45f)
            close()
        }
        drawPath(path = dressPath, color = tint)
    }
}

@Composable
fun KidCategoryIcon(
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    tint: Color = MaterialTheme.colorScheme.primary
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        drawCircle(
            color = tint,
            radius = w * 0.28f,
            center = Offset(w * 0.5f, h * 0.35f)
        )
        drawLine(
            color = tint,
            start = Offset(w * 0.5f, h * 0.63f),
            end = Offset(w * 0.4f, h * 0.9f),
            strokeWidth = w * 0.08f
        )
    }
}

@Composable
fun BagCategoryIcon(
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    tint: Color = MaterialTheme.colorScheme.primary
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        drawCircle(
            color = tint,
            radius = w * 0.22f,
            center = Offset(w * 0.5f, h * 0.35f),
            style = Stroke(width = w * 0.08f)
        )

        drawRoundRect(
            color = tint,
            topLeft = Offset(w * 0.15f, h * 0.38f),
            size = Size(w * 0.7f, h * 0.52f),
            cornerRadius = CornerRadius(w * 0.12f)
        )
    }
}

@Composable
fun OtherCategoryIcon(
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    tint: Color = MaterialTheme.colorScheme.primary
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val gap = w * 0.08f
        val boxW = (w - gap) / 2
        val boxH = (h - gap) / 2

        drawRoundRect(color = tint, topLeft = Offset(0f, 0f), size = Size(boxW, boxH), cornerRadius = CornerRadius(w * 0.06f))
        drawRoundRect(color = tint, topLeft = Offset(boxW + gap, 0f), size = Size(boxW, boxH), cornerRadius = CornerRadius(w * 0.06f))
        drawRoundRect(color = tint, topLeft = Offset(0f, boxH + gap), size = Size(boxW, boxH), cornerRadius = CornerRadius(w * 0.06f))
        drawRoundRect(color = tint, topLeft = Offset(boxW + gap, boxH + gap), size = Size(boxW, boxH), cornerRadius = CornerRadius(w * 0.06f))
    }
}

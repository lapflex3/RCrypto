package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CandleStick
import com.example.ui.theme.*

@Composable
fun BinaCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = BinaSurface,
    borderColor: Color = BinaBorder,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val cardModifier = if (onClick != null) {
        modifier
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    } else {
        modifier
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
    }

    Column(
        modifier = cardModifier.padding(16.dp),
        content = content
    )
}

@Composable
fun SparklineChart(
    data: List<Double>,
    color: Color,
    modifier: Modifier = Modifier
) {
    if (data.size < 2) return

    Canvas(modifier = modifier) {
        val minVal = data.minOrNull() ?: 0.0
        val maxVal = data.maxOrNull() ?: 1.0
        val range = (maxVal - minVal).coerceAtLeast(0.001)

        val path = Path()
        val stepX = size.width / (data.size - 1)

        data.forEachIndexed { index, value ->
            val x = index * stepX
            val y = size.height - (((value - minVal) / range) * size.height).toFloat()
            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }

        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}

@Composable
fun CandlestickChart(
    candles: List<CandleStick>,
    showBollinger: Boolean,
    modifier: Modifier = Modifier
) {
    if (candles.isEmpty()) return

    Canvas(modifier = modifier.fillMaxWidth()) {
        val minPrice = candles.minOf { it.low }
        val maxPrice = candles.maxOf { it.high }
        val priceRange = (maxPrice - minPrice).coerceAtLeast(0.01)

        val candleCount = candles.size
        val candleWidth = (size.width / candleCount) * 0.65f
        val slotWidth = size.width / candleCount

        // Draw candles
        candles.forEachIndexed { index, c ->
            val isGreen = c.close >= c.open
            val candleColor = if (isGreen) Color(0xFF00E676) else Color(0xFFFF3B69)

            val centerX = (index * slotWidth) + (slotWidth / 2f)

            // High/Low Wick
            val highY = size.height - (((c.high - minPrice) / priceRange) * (size.height * 0.75f) + (size.height * 0.2f)).toFloat()
            val lowY = size.height - (((c.low - minPrice) / priceRange) * (size.height * 0.75f) + (size.height * 0.2f)).toFloat()

            drawLine(
                color = candleColor,
                start = Offset(centerX, highY),
                end = Offset(centerX, lowY),
                strokeWidth = 1.5.dp.toPx()
            )

            // Open/Close Body
            val openY = size.height - (((c.open - minPrice) / priceRange) * (size.height * 0.75f) + (size.height * 0.2f)).toFloat()
            val closeY = size.height - (((c.close - minPrice) / priceRange) * (size.height * 0.75f) + (size.height * 0.2f)).toFloat()

            val topY = minOf(openY, closeY)
            val bodyHeight = maxOf(2.dp.toPx(), kotlin.math.abs(closeY - openY))

            drawRect(
                color = candleColor,
                topLeft = Offset(centerX - (candleWidth / 2f), topY),
                size = Size(candleWidth, bodyHeight)
            )

            // Volume bar at bottom
            val maxVol = candles.maxOf { it.volume }.coerceAtLeast(1.0)
            val volHeight = (c.volume / maxVol * (size.height * 0.18f)).toFloat()
            drawRect(
                color = candleColor.copy(alpha = 0.35f),
                topLeft = Offset(centerX - (candleWidth / 2f), size.height - volHeight),
                size = Size(candleWidth, volHeight)
            )
        }

        // Draw Bollinger Upper & Lower bands if enabled
        if (showBollinger && candles.size >= 10) {
            val upperPath = Path()
            val lowerPath = Path()
            candles.forEachIndexed { index, c ->
                val centerX = (index * slotWidth) + (slotWidth / 2f)
                val uPrice = c.close * 1.018
                val lPrice = c.close * 0.982
                val uY = size.height - (((uPrice - minPrice) / priceRange) * (size.height * 0.75f) + (size.height * 0.2f)).toFloat()
                val lY = size.height - (((lPrice - minPrice) / priceRange) * (size.height * 0.75f) + (size.height * 0.2f)).toFloat()
                if (index == 0) {
                    upperPath.moveTo(centerX, uY)
                    lowerPath.moveTo(centerX, lY)
                } else {
                    upperPath.lineTo(centerX, uY)
                    lowerPath.lineTo(centerX, lY)
                }
            }
            drawPath(
                path = upperPath,
                color = BinaSecondary.copy(alpha = 0.6f),
                style = Stroke(width = 1.dp.toPx())
            )
            drawPath(
                path = lowerPath,
                color = BinaSecondary.copy(alpha = 0.6f),
                style = Stroke(width = 1.dp.toPx())
            )
        }
    }
}

@Composable
fun StatusPill(
    text: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun CryptoIconBadge(
    symbol: String,
    color: Color,
    modifier: Modifier = Modifier,
    sizeDp: Int = 40
) {
    Box(
        modifier = modifier
            .size(sizeDp.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.2f))
            .border(1.dp, color.copy(alpha = 0.4f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = symbol.take(3),
            color = color,
            fontWeight = FontWeight.Bold,
            fontSize = if (sizeDp > 36) 13.sp else 10.sp
        )
    }
}

@Composable
fun TransparentWatermark(
    modifier: Modifier = Modifier
) {
    Text(
        text = "RazifApps@Nasadef™",
        color = Color.White.copy(alpha = 0.35f),
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 1.sp,
        modifier = modifier.padding(vertical = 4.dp)
    )
}


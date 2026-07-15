package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.viewmodel.MainViewModel

@Composable
fun ChartScreen(viewModel: MainViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val klines = uiState.klines

    if (klines.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("لا توجد بيانات للعرض", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    var scale by remember { mutableStateOf(1f) }
    var offsetX by remember { mutableStateOf(0f) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        val lastKline = klines.last()
        val percentChange = if (klines.size > 1) {
            val prevClose = klines[klines.size - 2].close
            ((lastKline.close - prevClose) / prevClose) * 100
        } else {
            0.0
        }
        
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text("السعر الحالي", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${lastKline.close}", style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.onSurface)
                }
                
                val bgColor = if (percentChange >= 0) Color(0xFFB8F397) else Color(0xFFFFB4AB)
                val textColor = if (percentChange >= 0) Color(0xFF133D00) else Color(0xFF690005)
                val sign = if (percentChange >= 0) "+" else ""
                
                Box(
                    modifier = Modifier.background(bgColor, RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("$sign${String.format("%.2f", percentChange)}%", style = MaterialTheme.typography.labelSmall, color = textColor)
                }
            }
        }
        
        Card(
            modifier = Modifier.fillMaxWidth().weight(1f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale = (scale * zoom).coerceIn(0.1f, 10f)
                            offsetX += pan.x
                        }
                    }
            ) {
                val maxPrice = klines.maxOf { it.high }.toFloat()
                val minPrice = klines.minOf { it.low }.toFloat()
                val priceRange = maxPrice - minPrice
                val candleWidth = 10f * scale
                val spacing = 4f * scale

                klines.forEachIndexed { index, kline ->
                    val x = offsetX + index * (candleWidth + spacing)
                    if (x < size.width && x + candleWidth > 0) {
                        val openY = size.height - ((kline.open.toFloat() - minPrice) / priceRange) * size.height
                        val closeY = size.height - ((kline.close.toFloat() - minPrice) / priceRange) * size.height
                        val highY = size.height - ((kline.high.toFloat() - minPrice) / priceRange) * size.height
                        val lowY = size.height - ((kline.low.toFloat() - minPrice) / priceRange) * size.height

                        val color = if (kline.close > kline.open) Color(0xFF4ADE80) else Color(0xFFF87171)

                        // Draw wick
                        drawLine(
                            color = color,
                            start = Offset(x + candleWidth / 2, highY),
                            end = Offset(x + candleWidth / 2, lowY),
                            strokeWidth = 2f
                        )

                        // Draw body
                        val top = minOf(openY, closeY)
                        val bottom = maxOf(openY, closeY)
                        val height = maxOf(bottom - top, 1f)
                        drawRect(
                            color = color,
                            topLeft = Offset(x, top),
                            size = Size(candleWidth, height)
                        )
                    }
                }
            }
        }
    }
}

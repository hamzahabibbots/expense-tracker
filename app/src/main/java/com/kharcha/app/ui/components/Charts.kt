package com.kharcha.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kharcha.app.data.model.CategorySpending
import com.kharcha.app.data.model.DailySpending
import com.kharcha.app.ui.theme.ChartColors
import com.kharcha.app.ui.theme.Teal
import com.kharcha.app.ui.theme.parseColor
import com.kharcha.app.util.FormatUtils
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** Custom Compose Canvas pie chart */
@Composable
fun PieChartView(
    data: List<CategorySpending>,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) {
        Box(modifier = modifier.height(200.dp), contentAlignment = Alignment.Center) {
            Text("No data available", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    val total = data.sumOf { it.total }
    if (total <= 0) return

    Row(
        modifier = modifier.fillMaxWidth().padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Pie chart
        Canvas(modifier = Modifier.size(160.dp)) {
            val canvasSize = size.minDimension
            val radius = canvasSize / 2 * 0.85f
            val center = Offset(size.width / 2, size.height / 2)
            var startAngle = -90f

            data.forEachIndexed { index, item ->
                val sweepAngle = (item.total / total * 360).toFloat()
                val color = parseColor(item.color)
                drawArc(
                    color = color,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = true,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2)
                )
                startAngle += sweepAngle
            }

            // Center hole for donut effect
            drawCircle(
                color = Color.Transparent,
                radius = radius * 0.45f,
                center = center,
                blendMode = androidx.compose.ui.graphics.BlendMode.Clear
            )
        }

        Spacer(Modifier.width(16.dp))

        // Legend
        Column(modifier = Modifier.weight(1f)) {
            data.take(6).forEach { item ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 3.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .padding(0.dp)
                    ) {
                        Canvas(Modifier.fillMaxSize()) {
                            drawCircle(color = parseColor(item.color))
                        }
                    }
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = item.categoryName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = FormatUtils.formatAmount(item.total),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/** Premium Interactive Compose Bar Chart */
@Composable
fun InteractiveBarChartView(
    data: List<DailySpending>,
    modifier: Modifier = Modifier,
    isYearly: Boolean = false
) {
    if (data.isEmpty()) {
        Box(modifier = modifier.height(200.dp), contentAlignment = Alignment.Center) {
            Text("No data available", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    var tapOffset by remember { mutableStateOf(Offset.Zero) }

    val maxVal = data.maxOf { it.total }.coerceAtLeast(1.0)
    val barColor = Teal
    val selectedBarColor = MaterialTheme.colorScheme.primary
    val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)

    Column(modifier = modifier.fillMaxWidth()) {
        Box(modifier = Modifier.fillMaxWidth().height(220.dp)) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 8.dp, end = 8.dp, bottom = 24.dp, top = 20.dp)
                    .pointerInput(data) {
                        detectTapGestures { offset ->
                            val w = size.width.toFloat()
                            val barWidth = w / (data.size * 1.5f)
                            val spacing = w / data.size
                            
                            val index = (offset.x / spacing).toInt().coerceIn(0, data.size - 1)
                            val xPos = (index + 0.5f) * spacing
                            // If tap is near the bar horizontally
                            if (kotlin.math.abs(offset.x - xPos) < spacing) {
                                selectedIndex = index
                                tapOffset = Offset(xPos, offset.y)
                            } else {
                                selectedIndex = null
                            }
                        }
                    }
            ) {
                val w = size.width
                val h = size.height

                // Draw subtle grid lines
                for (i in 0..3) {
                    val y = h * (i / 3f)
                    drawLine(gridColor, Offset(0f, y), Offset(w, y), strokeWidth = 1f)
                }

                val spacing = w / data.size
                val barWidth = spacing * 0.6f

                // Draw bars
                data.forEachIndexed { i, d ->
                    val x = (i + 0.5f) * spacing - (barWidth / 2)
                    val barHeight = (d.total / maxVal * h).toFloat()
                    val y = h - barHeight
                    
                    val isSelected = selectedIndex == i
                    val color = if (isSelected) selectedBarColor else barColor.copy(alpha = if (selectedIndex == null) 1f else 0.4f)
                    
                    drawRoundRect(
                        color = color,
                        topLeft = Offset(x, y),
                        size = Size(barWidth, barHeight),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx(), 4.dp.toPx())
                    )
                }
            }

            // Draw interactive tooltip overlay
            selectedIndex?.let { idx ->
                val d = data[idx]
                Box(
                    modifier = Modifier
                        .offset(
                            x = with(androidx.compose.ui.platform.LocalDensity.current) { tapOffset.x.toDp() } - 60.dp,
                            y = with(androidx.compose.ui.platform.LocalDensity.current) { tapOffset.y.toDp() } - 40.dp
                        )
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = FormatUtils.formatChartDate(d.date, isYearly),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = FormatUtils.formatAmount(d.total),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // X-axis labels
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val labelIndices = listOf(0, data.size / 4, data.size / 2, data.size * 3 / 4, data.size - 1)
                .filter { it in data.indices }.distinct()
            labelIndices.forEach { i ->
                Text(
                    text = FormatUtils.formatChartDate(data[i].date, isYearly),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

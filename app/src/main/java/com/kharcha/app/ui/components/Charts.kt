package com.kharcha.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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

/** Custom Compose Canvas line chart */
@Composable
fun LineChartView(
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

    val maxVal = data.maxOf { it.total }.coerceAtLeast(1.0)
    val lineColor = Teal
    val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    val textColor = MaterialTheme.colorScheme.onSurfaceVariant

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(start = 8.dp, end = 8.dp, bottom = 24.dp, top = 8.dp)
    ) {
        val w = size.width
        val h = size.height
        val points = data.mapIndexed { i, d ->
            val x = if (data.size > 1) (i.toFloat() / (data.size - 1)) * w else w / 2
            val y = h - (d.total / maxVal * h).toFloat()
            Offset(x, y)
        }

        // Grid lines
        for (i in 0..3) {
            val y = h * (i / 3f)
            drawLine(gridColor, Offset(0f, y), Offset(w, y), strokeWidth = 1f)
        }

        // Line path
        if (points.size >= 2) {
            val path = Path().apply {
                moveTo(points[0].x, points[0].y)
                for (i in 1 until points.size) {
                    val prev = points[i - 1]
                    val curr = points[i]
                    val cx1 = (prev.x + curr.x) / 2
                    cubicTo(cx1, prev.y, cx1, curr.y, curr.x, curr.y)
                }
            }
            drawPath(path, lineColor, style = Stroke(width = 2.5f, cap = StrokeCap.Round))

            // Gradient fill
            val fillPath = Path().apply {
                addPath(path)
                lineTo(points.last().x, h)
                lineTo(points.first().x, h)
                close()
            }
            drawPath(fillPath, lineColor.copy(alpha = 0.1f))
        }

        // Dots at data points (every 5th)
        points.forEachIndexed { i, pt ->
            if (i % 5 == 0 || i == points.size - 1) {
                drawCircle(lineColor, 3.5f, pt)
                drawCircle(Color.White, 1.5f, pt)
            }
        }
    }

    // X-axis labels
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
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

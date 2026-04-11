package com.kharcha.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.kharcha.app.data.model.Tip
import com.kharcha.app.data.model.TipSeverity
import com.kharcha.app.data.model.TipType
import com.kharcha.app.ui.theme.TipAlert
import com.kharcha.app.ui.theme.TipInfo
import com.kharcha.app.ui.theme.TipWarning

@Composable
fun TipCard(tip: Tip, modifier: Modifier = Modifier) {
    val borderColor = when (tip.severity) {
        TipSeverity.ALERT -> TipAlert
        TipSeverity.WARNING -> TipWarning
        TipSeverity.INFO -> TipInfo
    }

    val icon = when (tip.type) {
        TipType.CATEGORY_HIGH_SPENDING -> "📊"
        TipType.FREQUENT_MERCHANT -> "🔄"
        TipType.MONTHLY_COMPARISON -> "📅"
        TipType.GENERAL -> "💡"
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // Left accent border
        Box(
            modifier = Modifier
                .width(4.dp)
                .fillMaxHeight()
                .background(borderColor)
                .align(Alignment.CenterStart)
        )

        Column(
            modifier = Modifier.padding(start = 18.dp, end = 14.dp, top = 14.dp, bottom = 14.dp)
        ) {
            // Header row
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = icon, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = tip.title,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(6.dp))

            Text(
                text = tip.message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Details
            tip.percentage?.let {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "$it% of total spending",
                    style = MaterialTheme.typography.labelMedium,
                    color = borderColor
                )
            }

            tip.count?.let {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "$it transactions",
                    style = MaterialTheme.typography.labelMedium,
                    color = borderColor
                )
            }

            if (tip.change != null && tip.isIncrease != null) {
                Spacer(Modifier.height(4.dp))
                val arrow = if (tip.isIncrease) "↑" else "↓"
                val changeColor = if (tip.isIncrease) TipAlert else TipInfo
                Text(
                    text = "$arrow ${tip.change}% vs last month",
                    style = MaterialTheme.typography.labelMedium,
                    color = changeColor
                )
            }
        }
    }
}

package com.kharcha.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.kharcha.app.data.model.Category
import com.kharcha.app.ui.theme.parseColor

@Composable
fun CategoryBadge(
    category: Category?,
    modifier: Modifier = Modifier,
    showName: Boolean = true
) {
    val cat = category ?: Category("other", "Other", "#B8B8B8", null, true)
    val color = parseColor(cat.color)

    if (!showName) {
        Box(
            modifier = modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        return
    }

    Row(
        modifier = modifier
            .background(color.copy(alpha = 0.12f), RoundedCornerShape(20.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(Modifier.width(5.dp))
        Text(
            text = cat.name,
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}

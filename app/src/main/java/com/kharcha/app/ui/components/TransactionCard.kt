package com.kharcha.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kharcha.app.data.model.Category
import com.kharcha.app.data.model.Transaction
import com.kharcha.app.util.FormatUtils

@Composable
fun TransactionCard(
    transaction: Transaction,
    categories: List<Category>,
    modifier: Modifier = Modifier,
    onCategoryClick: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
) {
    val category = categories.find { it.id == transaction.categoryId }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            // Left: Merchant + Date
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.merchant,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                Spacer(Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = FormatUtils.formatDateFull(transaction.date),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (!transaction.bankName.isNullOrEmpty() && transaction.bankName != "Unknown") {
                        Text(
                            text = " · ${transaction.bankName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            // Right: Amount + Category
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = FormatUtils.formatAmount(transaction.amount),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(4.dp))
                if (onCategoryClick != null) {
                    TextButton(
                        onClick = onCategoryClick,
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier.height(24.dp)
                    ) {
                        CategoryBadge(category = category)
                    }
                } else {
                    CategoryBadge(category = category)
                }
            }
        }
    }
}

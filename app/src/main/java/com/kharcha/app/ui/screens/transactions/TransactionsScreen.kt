package com.kharcha.app.ui.screens.transactions

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kharcha.app.ui.components.TransactionCard
import com.kharcha.app.ui.theme.Teal
import com.kharcha.app.ui.theme.parseColor
import com.kharcha.app.util.FormatUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(viewModel: TransactionsViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsState()

    val filtered = when (state.selectedFilter) {
        null -> state.transactions
        "RECEIPTS" -> state.transactions.filter { it.type == "CREDIT" }
        else -> state.transactions.filter { it.categoryId == state.selectedFilter }
    }

    // Group by date
    val grouped = filtered
        .groupBy { it.date.take(10) }
        .toSortedMap(compareByDescending { it })

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Transactions",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                "${filtered.size} transaction${if (filtered.size != 1) "s" else ""}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Category filter chips
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = state.selectedFilter == null,
                onClick = { viewModel.setFilter(null) },
                label = { Text("All") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Teal,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                )
            )
            FilterChip(
                selected = state.selectedFilter == "RECEIPTS",
                onClick = { viewModel.setFilter("RECEIPTS") },
                label = { Text("Receipts") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = parseColor("#A8E6CF"), // Greenish
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                )
            )
            state.categories.forEach { cat ->
                if (cat.id != "income") { // Skip the default income category since we have Receipts
                    FilterChip(
                        selected = state.selectedFilter == cat.id,
                        onClick = { viewModel.setFilter(cat.id) },
                        label = { Text(cat.name) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = parseColor(cat.color),
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }
        }

        Spacer(Modifier.height(4.dp))

        // Transaction list
        if (filtered.isEmpty()) {
            Box(
                Modifier.fillMaxSize().padding(40.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "No Transactions",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Sync SMS to see your transactions here",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                grouped.forEach { (date, dayTransactions) ->
                    item(key = "header_$date") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                FormatUtils.formatDateHeader(date),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                FormatUtils.formatAmount(dayTransactions.sumOf { it.amount }),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    items(dayTransactions, key = { it.id }) { tx ->
                        TransactionCard(
                            transaction = tx,
                            categories = state.categories,
                            onCategoryClick = { viewModel.openCategorySheet(tx) },
                            onDelete = { viewModel.deleteTransaction(tx.id) }
                        )
                    }
                }
            }
        }
    }

    // Category selection bottom sheet
    if (state.showCategorySheet) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.closeCategorySheet() },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    "Select Category",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(Modifier.height(8.dp))

                state.categories.forEach { cat ->
                    val isSelected = state.selectedTransaction?.categoryId == cat.id
                    TextButton(
                        onClick = {
                            state.selectedTransaction?.let {
                                viewModel.changeCategory(it.id, cat.id)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                Modifier
                                    .size(16.dp)
                                    .background(
                                        parseColor(cat.color),
                                        RoundedCornerShape(8.dp)
                                    )
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                cat.name,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            if (isSelected) {
                                Text("✓", color = Teal, style = MaterialTheme.typography.titleMedium)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

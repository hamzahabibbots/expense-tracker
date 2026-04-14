package com.kharcha.app.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.filled.Add
import com.kharcha.app.ui.components.*
import com.kharcha.app.ui.theme.Teal
import com.kharcha.app.util.FormatUtils

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = viewModel(),
    onNavigateTransactions: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    
    var showBalanceDialog by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    var balanceInput by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }

    if (state.isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Teal)
        }
        return
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
    ) {
        Spacer(Modifier.height(16.dp))

        // Header: Total + Sync
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                val remaining = if (state.dashboardData.bankBalances.isNotEmpty()) {
                    state.dashboardData.bankBalances.sumOf { it.balance }
                } else {
                    0.0
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Remaining Balance",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    IconButton(
                        onClick = { showBalanceDialog = true },
                        modifier = Modifier.size(24.dp).padding(start = 4.dp)
                    ) {
                        androidx.compose.material3.Icon(
                            androidx.compose.material.icons.Icons.Default.Add,
                            contentDescription = "Add starting balance",
                            tint = Teal
                        )
                    }
                }
                Text(
                    FormatUtils.formatAmount(remaining),
                    style = MaterialTheme.typography.headlineLarge,
                    color = if (remaining >= 0) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "↓ ${FormatUtils.formatAmount(state.dashboardData.totalReceived)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = Teal,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "↑ ${FormatUtils.formatAmount(state.dashboardData.totalSpending)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Button(
                onClick = { viewModel.syncSms() },
                enabled = !state.isSyncing,
                colors = ButtonDefaults.buttonColors(containerColor = Teal),
                shape = RoundedCornerShape(8.dp)
            ) {
                if (state.isSyncing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(8.dp))
                }
                Icon(Icons.Default.Sync, contentDescription = "Sync")
                Spacer(Modifier.width(4.dp))
                Text(if (state.isSyncing) "Syncing..." else "Sync SMS")
            }
        }
        
        // Bank Balances
        if (state.dashboardData.bankBalances.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                state.dashboardData.bankBalances.forEach { balance ->
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                balance.bankName,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                FormatUtils.formatAmount(balance.balance),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // Quick Tip
        state.quickTip?.let { tip ->
            Card(
                modifier = Modifier.padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .height(40.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Teal)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        tip.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Charts
        if (state.dashboardData.totalSpending > 0) {
            Spacer(Modifier.height(16.dp))

            // Pie Chart
            Card(
                modifier = Modifier.padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        "Spending by Category",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(8.dp))
                    PieChartView(data = state.dashboardData.categorySpending)
                }
            }

            Spacer(Modifier.height(12.dp))

            // Line Chart
            Card(
                modifier = Modifier.padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Spending Trend",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    
                    // Chart Period Toggles
                    Row(
                        modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)).padding(4.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        val periods = listOf(
                            ChartPeriod.WEEKLY to "Week",
                            ChartPeriod.MONTHLY to "Month",
                            ChartPeriod.YEARLY to "Year"
                        )
                        periods.forEach { (period, label) ->
                            val isSelected = state.chartPeriod == period
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        color = if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent,
                                        shape = RoundedCornerShape(6.dp)
                                    )
                                    .padding(vertical = 6.dp)
                                    .clickable { viewModel.setChartPeriod(period) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    label,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                    
                    Spacer(Modifier.height(16.dp))
                    InteractiveBarChartView(
                        data = state.dashboardData.dailySpending,
                        isYearly = state.chartPeriod == ChartPeriod.YEARLY
                    )
                }
            }
        }

        // Top Merchants
        if (state.dashboardData.topMerchants.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            Card(
                modifier = Modifier.padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        "Top Merchants This Month",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(8.dp))
                    state.dashboardData.topMerchants.forEachIndexed { index, merchant ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "#${index + 1}",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.width(30.dp)
                            )
                            Text(
                                merchant.merchant,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                FormatUtils.formatAmount(merchant.total),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        if (index < state.dashboardData.topMerchants.lastIndex) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        }
                    }
                }
            }
        }

        // Tips
        if (state.tips.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            Text(
                "Spending Tips",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(Modifier.height(4.dp))
            state.tips.forEach { tip ->
                TipCard(tip = tip)
            }
        }

        // Recent Transactions
        if (state.dashboardData.recentTransactions.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            Card(
                modifier = Modifier.padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Recent Transactions",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        TextButton(onClick = onNavigateTransactions) {
                            Text("See All", color = Teal)
                        }
                    }
                }
            }

            state.dashboardData.recentTransactions.take(5).forEach { tx ->
                TransactionCard(
                    transaction = tx,
                    categories = state.categories,
                    onDelete = { viewModel.deleteTransaction(tx.id) }
                )
            }
        }

        // Empty State
        if (state.dashboardData.totalSpending == 0.0) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(40.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "No Transactions Yet",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Tap \"Sync SMS\" to import your bank transactions",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(Modifier.height(32.dp))
    }

    if (showBalanceDialog) {
        AlertDialog(
            onDismissRequest = { showBalanceDialog = false },
            title = { Text("Set Starting Balance") },
            text = {
                Column {
                    Text("Carry forward your account balance to track math correctly.")
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = balanceInput,
                        onValueChange = { balanceInput = it },
                        label = { Text("Amount") },
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val amount = balanceInput.toDoubleOrNull()
                        if (amount != null) {
                            viewModel.addStartingBalance(amount)
                        }
                        showBalanceDialog = false
                        balanceInput = ""
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBalanceDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

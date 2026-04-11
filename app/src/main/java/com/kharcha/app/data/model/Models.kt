package com.kharcha.app.data.model

data class Transaction(
    val id: String,
    val amount: Double,
    val merchant: String,
    val date: String,
    val bankName: String?,
    val categoryId: String,
    val rawSms: String?,
    val sender: String?,
    val type: String, // "DEBIT" or "CREDIT"
    val balance: Double?, // Account balance if present in SMS
    val createdAt: String
)

data class Category(
    val id: String,
    val name: String,
    val color: String,
    val icon: String?,
    val isDefault: Boolean = false
)

data class MerchantMapping(
    val merchant: String,
    val categoryId: String,
    val frequency: Int = 1,
    val lastUsed: String?
)

data class CategorySpending(
    val categoryId: String,
    val categoryName: String,
    val color: String,
    val total: Double
)

data class DailySpending(
    val date: String,
    val total: Double
)

data class MerchantSpending(
    val merchant: String,
    val total: Double,
    val count: Int
)

data class MonthlyComparison(
    val current: Double,
    val previous: Double,
    val change: Double,
    val isIncrease: Boolean
)

data class BankAccountBalance(
    val bankName: String,
    val balance: Double
)

data class DashboardData(
    val categorySpending: List<CategorySpending> = emptyList(),
    val dailySpending: List<DailySpending> = emptyList(),
    val topMerchants: List<MerchantSpending> = emptyList(),
    val monthlyComparison: MonthlyComparison? = null,
    val recentTransactions: List<Transaction> = emptyList(),
    val totalSpending: Double = 0.0,
    val totalReceived: Double = 0.0,
    val bankBalances: List<BankAccountBalance> = emptyList()
)

data class SyncResult(
    val success: Boolean,
    val scanned: Int = 0,
    val parsed: Int = 0,
    val added: Int = 0,
    val error: String? = null
)

enum class TipSeverity { INFO, WARNING, ALERT }
enum class TipType { CATEGORY_HIGH_SPENDING, FREQUENT_MERCHANT, MONTHLY_COMPARISON, GENERAL }

data class Tip(
    val id: String,
    val type: TipType,
    val severity: TipSeverity,
    val title: String,
    val message: String,
    val percentage: Int? = null,
    val count: Int? = null,
    val change: Int? = null,
    val isIncrease: Boolean? = null,
    val amount: Double? = null
)

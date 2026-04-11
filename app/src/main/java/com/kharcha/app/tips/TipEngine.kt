package com.kharcha.app.tips

import com.kharcha.app.data.model.*
import com.kharcha.app.util.FormatUtils
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Rule-based spending tip engine — fully offline, no API needed.
 * Ported from tipEngine.js.
 */
object TipEngine {

    fun calculateTips(data: DashboardData): List<Tip> {
        val tips = mutableListOf<Tip>()

        // Rule 1: Category spending thresholds
        if (data.categorySpending.isNotEmpty() && data.totalSpending > 0) {
            tips += checkCategorySpending(data.categorySpending, data.totalSpending)
        }

        // Rule 2: Frequent merchants
        if (data.topMerchants.isNotEmpty()) {
            tips += checkFrequentMerchants(data.topMerchants)
        }

        // Rule 3: Monthly comparison
        data.monthlyComparison?.let { tips += checkMonthlyComparison(it) }

        // Rule 4: General patterns
        if (data.recentTransactions.isNotEmpty()) {
            tips += checkGeneralPatterns(data.recentTransactions)
        }

        // Sort: alerts first, then warnings, then info
        val order = mapOf(TipSeverity.ALERT to 0, TipSeverity.WARNING to 1, TipSeverity.INFO to 2)
        return tips.sortedBy { order[it.severity] ?: 2 }.take(3)
    }

    fun generateQuickTip(categorySpending: List<CategorySpending>, totalSpending: Double): Tip? {
        if (categorySpending.isEmpty() || totalSpending <= 0) return null
        val top = categorySpending.maxByOrNull { it.total } ?: return null
        val pct = ((top.total / totalSpending) * 100).roundToInt()
        return Tip(
            id = "quick_tip",
            type = TipType.GENERAL,
            severity = TipSeverity.INFO,
            title = "Top Category",
            message = "${top.categoryName} is your biggest expense at ${pct}% of total spending",
            percentage = pct
        )
    }

    private fun checkCategorySpending(
        spending: List<CategorySpending>,
        total: Double
    ): List<Tip> {
        val thresholds = mapOf(
            "food" to Pair(0.30, "Food spending is above 30% of your total. Consider meal prepping to reduce food costs."),
            "transport" to Pair(0.20, "Transport costs are above 20% of your spending. Check if a monthly pass is cheaper than daily fares."),
            "entertainment" to Pair(0.15, "Entertainment spending is above 15%. Consider free activities or streaming bundles."),
            "shopping" to Pair(0.25, "Shopping expenses are above 25%. Try the 24-hour rule before making purchases."),
        )

        return spending.mapNotNull { cat ->
            val (maxPct, message) = thresholds[cat.categoryId] ?: return@mapNotNull null
            val pct = cat.total / total
            if (pct < maxPct) return@mapNotNull null

            Tip(
                id = "category_${cat.categoryId}",
                type = TipType.CATEGORY_HIGH_SPENDING,
                severity = if (pct >= maxPct * 1.5) TipSeverity.ALERT else TipSeverity.WARNING,
                title = "${cat.categoryName} Spending Alert",
                message = message,
                percentage = (pct * 100).roundToInt(),
                amount = cat.total
            )
        }
    }

    private fun checkFrequentMerchants(merchants: List<MerchantSpending>): List<Tip> {
        val tips = mutableListOf<Tip>()
        for (m in merchants) {
            if (m.count >= 5) {
                tips += Tip(
                    id = "frequent_${m.merchant}",
                    type = TipType.FREQUENT_MERCHANT,
                    severity = TipSeverity.INFO,
                    title = "Frequent Visitor Detected",
                    message = "You visit ${m.merchant} ${m.count} times this month. Look for a loyalty program or subscription to save.",
                    count = m.count, amount = m.total
                )
            }
            if (m.total >= 5000) {
                tips += Tip(
                    id = "high_spend_${m.merchant}",
                    type = TipType.FREQUENT_MERCHANT,
                    severity = TipSeverity.WARNING,
                    title = "High Spending Alert",
                    message = "You've spent ${FormatUtils.formatAmount(m.total)} at ${m.merchant} this month. Consider if all purchases were necessary.",
                    amount = m.total
                )
            }
        }
        return tips
    }

    private fun checkMonthlyComparison(comparison: MonthlyComparison): List<Tip> {
        if (comparison.previous == 0.0) return emptyList()
        val pctChange = abs(comparison.change.roundToInt())
        if (pctChange < 10) return emptyList()

        return listOf(
            if (comparison.isIncrease) {
                Tip(
                    id = "monthly_increase",
                    type = TipType.MONTHLY_COMPARISON,
                    severity = if (pctChange >= 25) TipSeverity.ALERT else TipSeverity.WARNING,
                    title = "Spending Increase Detected",
                    message = "Your spending is up ${pctChange}% compared to last month. Review your expenses to identify areas to cut back.",
                    change = pctChange, isIncrease = true
                )
            } else {
                Tip(
                    id = "monthly_decrease",
                    type = TipType.MONTHLY_COMPARISON,
                    severity = TipSeverity.INFO,
                    title = "Great Job Saving!",
                    message = "Your spending is down ${pctChange}% compared to last month. Keep it up!",
                    change = pctChange, isIncrease = false
                )
            }
        )
    }

    private fun checkGeneralPatterns(transactions: List<Transaction>): List<Tip> {
        val tips = mutableListOf<Tip>()
        val total = transactions.sumOf { it.amount }
        if (total <= 0) return tips

        // Weekend spending pattern
        val weekendTotal = transactions.filter {
            try {
                val d = java.time.LocalDate.parse(it.date.take(10))
                d.dayOfWeek == java.time.DayOfWeek.SATURDAY || d.dayOfWeek == java.time.DayOfWeek.SUNDAY
            } catch (_: Exception) { false }
        }.sumOf { it.amount }

        if (weekendTotal / total > 0.5) {
            tips += Tip(
                id = "weekend_spending", type = TipType.GENERAL, severity = TipSeverity.INFO,
                title = "Weekend Spending Pattern",
                message = "More than 50% of your spending happens on weekends. Plan weekend activities in advance to control costs.",
                percentage = ((weekendTotal / total) * 100).roundToInt()
            )
        }

        // Small impulse purchases
        val small = transactions.filter { it.amount < 200 }
        if (small.size >= 10) {
            val smallTotal = small.sumOf { it.amount }
            tips += Tip(
                id = "small_purchases", type = TipType.GENERAL, severity = TipSeverity.INFO,
                title = "Small Purchases Add Up",
                message = "You've made ${small.size} purchases under ₹200 this month, totaling ${FormatUtils.formatAmount(smallTotal)}. These small expenses can add up quickly!",
                count = small.size, amount = smallTotal
            )
        }

        // Daily average
        val uniqueDays = transactions.map { it.date.take(10) }.toSet().size
        if (uniqueDays > 0) {
            val dailyAvg = total / uniqueDays
            if (dailyAvg > 1000) {
                tips += Tip(
                    id = "daily_average", type = TipType.GENERAL, severity = TipSeverity.WARNING,
                    title = "High Daily Average",
                    message = "Your daily spending average is ${FormatUtils.formatAmount(dailyAvg)}. Try to set a daily budget limit.",
                    amount = dailyAvg
                )
            }

            // Budget projection
            val projected = dailyAvg * 30
            tips += Tip(
                id = "budget_suggestion", type = TipType.GENERAL, severity = TipSeverity.INFO,
                title = "Budget Suggestion",
                message = "At your current rate, you'll spend approximately ${FormatUtils.formatAmount(projected)} this month. Consider setting a monthly budget.",
                amount = projected
            )
        }

        return tips
    }
}

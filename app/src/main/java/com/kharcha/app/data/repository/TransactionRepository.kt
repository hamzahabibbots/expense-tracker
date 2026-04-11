package com.kharcha.app.data.repository

import com.kharcha.app.data.db.dao.MerchantMappingDao
import com.kharcha.app.data.db.dao.TransactionDao
import com.kharcha.app.data.db.entity.MerchantMappingEntity
import com.kharcha.app.data.db.entity.TransactionEntity
import com.kharcha.app.data.model.*
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class TransactionRepository(
    private val transactionDao: TransactionDao,
    private val merchantMappingDao: MerchantMappingDao
) {
    suspend fun addTransaction(tx: Transaction) {
        transactionDao.insert(tx.toEntity())
        updateMerchantMapping(tx.merchant, tx.categoryId)
    }

    suspend fun addTransactions(txList: List<Transaction>) {
        transactionDao.insertAll(txList.map { it.toEntity() })
        txList.forEach { updateMerchantMapping(it.merchant, it.categoryId) }
    }

    suspend fun getTransactions(
        startDate: String? = null,
        endDate: String? = null,
        categoryId: String? = null,
        limit: Int = 1000
    ): List<Transaction> {
        return transactionDao.getFiltered(startDate, endDate, categoryId, limit)
            .map { it.toDomain() }
    }

    suspend fun updateCategory(transactionId: String, categoryId: String) {
        transactionDao.updateCategory(transactionId, categoryId)
        val tx = transactionDao.getById(transactionId)
        tx?.let { updateMerchantMapping(it.merchant, categoryId) }
    }

    suspend fun deleteTransaction(id: String) {
        transactionDao.delete(id)
    }

    suspend fun deleteAllTransactions() {
        transactionDao.deleteAll()
    }

    suspend fun getCategorySpending(startDate: String?, endDate: String?): List<CategorySpending> {
        return transactionDao.getSpendingByCategory(startDate, endDate)
            .map { CategorySpending(it.categoryId, it.categoryName, it.color, it.total) }
    }

    suspend fun getDailySpending(startDate: String, endDate: String): List<DailySpending> {
        return transactionDao.getDailySpending(startDate, endDate)
            .map { DailySpending(it.day, it.total) }
    }

    suspend fun getSpendingTrend(days: Int = 30): List<DailySpending> {
        val endDate = LocalDate.now()
        val startDate = endDate.minusDays((days - 1).toLong())
        val fmt = DateTimeFormatter.ISO_LOCAL_DATE

        val data = getDailySpending(startDate.format(fmt), endDate.format(fmt))
        val dataMap = data.associateBy { it.date }

        return (0 until days).map { i ->
            val date = startDate.plusDays(i.toLong())
            val dateStr = date.format(fmt)
            DailySpending(dateStr, dataMap[dateStr]?.total ?: 0.0)
        }
    }

    suspend fun getMonthlySpendingTrend(months: Int = 12): List<DailySpending> {
        val endDate = LocalDate.now()
        val startDate = endDate.minusMonths((months - 1).toLong()).withDayOfMonth(1)
        val fmt = DateTimeFormatter.ISO_LOCAL_DATE
        
        val data = transactionDao.getMonthlySpendingTrend(startDate.format(fmt))
        val dataMap = data.associateBy { it.day.take(7) } // year-month

        return (0 until months).map { i ->
            val date = startDate.plusMonths(i.toLong())
            val monthStr = date.format(DateTimeFormatter.ofPattern("yyyy-MM"))
            DailySpending("${monthStr}-01", dataMap[monthStr]?.total ?: 0.0)
        }
    }
    
    suspend fun purgeOldTransactions(cutoffDate: String) {
        transactionDao.deleteOlderThan(cutoffDate)
    }

    suspend fun getTopMerchants(
        limit: Int = 5,
        startDate: String? = null,
        endDate: String? = null
    ): List<MerchantSpending> {
        return transactionDao.getTopMerchants(limit, startDate, endDate)
            .map { MerchantSpending(it.merchant, it.total, it.count) }
    }

    suspend fun getTotalSpending(startDate: String?, endDate: String?): Pair<Double, Int> {
        val result = transactionDao.getTotalSpending(startDate, endDate)
        return (result.total ?: 0.0) to result.count
    }

    suspend fun getTotalReceived(startDate: String?, endDate: String?): Pair<Double, Int> {
        val result = transactionDao.getTotalReceived(startDate, endDate)
        return (result.total ?: 0.0) to result.count
    }

    suspend fun getLatestBankBalances(): List<BankAccountBalance> {
        return transactionDao.getLatestBankBalances().map { 
            BankAccountBalance(it.bankName, it.balance) 
        }
    }

    suspend fun getMonthlyComparison(): MonthlyComparison {
        val now = LocalDate.now()
        val currentStart = now.withDayOfMonth(1).format(DateTimeFormatter.ISO_LOCAL_DATE) + "T00:00:00"
        val currentEnd = now.withDayOfMonth(now.lengthOfMonth()).format(DateTimeFormatter.ISO_LOCAL_DATE) + "T23:59:59"
        val prevStart = now.minusMonths(1).withDayOfMonth(1).format(DateTimeFormatter.ISO_LOCAL_DATE) + "T00:00:00"
        val prevEnd = now.minusMonths(1).let {
            it.withDayOfMonth(it.lengthOfMonth())
        }.format(DateTimeFormatter.ISO_LOCAL_DATE) + "T23:59:59"

        val (currentTotal, _) = getTotalSpending(currentStart, currentEnd)
        val (previousTotal, _) = getTotalSpending(prevStart, prevEnd)

        val change = if (previousTotal > 0) {
            ((currentTotal - previousTotal) / previousTotal) * 100
        } else 0.0

        return MonthlyComparison(
            current = currentTotal,
            previous = previousTotal,
            change = change,
            isIncrease = change > 0
        )
    }

    suspend fun getTransactionCount(): Int = transactionDao.getCount()

    suspend fun getMerchantCategory(merchant: String): String? {
        return merchantMappingDao.getCategoryForMerchant(merchant.lowercase().trim())
    }

    private suspend fun updateMerchantMapping(merchant: String, categoryId: String) {
        val normalized = merchant.lowercase().trim()
        val now = Instant.now().toString()
        val existing = merchantMappingDao.getByMerchant(normalized)
        if (existing != null) {
            merchantMappingDao.updateMapping(normalized, categoryId, now)
        } else {
            merchantMappingDao.insert(
                MerchantMappingEntity(normalized, categoryId, 1, now)
            )
        }
    }

    private fun Transaction.toEntity() = TransactionEntity(
        id, amount, merchant, date, bankName, categoryId, rawSms, sender, type, balance, createdAt
    )

    private fun TransactionEntity.toDomain() = Transaction(
        id, amount, merchant, date, bankName, categoryId, rawSms, sender, type, balance, createdAt
    )
}

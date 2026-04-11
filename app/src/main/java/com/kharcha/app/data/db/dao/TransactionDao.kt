package com.kharcha.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kharcha.app.data.db.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

data class BankBalanceResult(
    val bankName: String,
    val balance: Double
)

data class CategorySpendingResult(
    val categoryId: String,
    val categoryName: String,
    val color: String,
    val total: Double
)

data class DailySpendingResult(
    val day: String,
    val total: Double
)

data class MerchantSpendingResult(
    val merchant: String,
    val total: Double,
    val count: Int
)

data class TotalSpendingResult(
    val total: Double?,
    val count: Int
)

@Dao
interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: TransactionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(transactions: List<TransactionEntity>)

    @Query("SELECT * FROM transactions ORDER BY date DESC LIMIT :limit OFFSET :offset")
    suspend fun getAll(limit: Int = 1000, offset: Int = 0): List<TransactionEntity>

    @Query("""
        SELECT * FROM transactions 
        WHERE (:startDate IS NULL OR date >= :startDate) 
        AND (:endDate IS NULL OR date <= :endDate) 
        AND (:categoryId IS NULL OR category_id = :categoryId) 
        ORDER BY date DESC LIMIT :limit OFFSET :offset
    """)
    suspend fun getFiltered(
        startDate: String? = null,
        endDate: String? = null,
        categoryId: String? = null,
        limit: Int = 1000,
        offset: Int = 0
    ): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getById(id: String): TransactionEntity?

    @Query("UPDATE transactions SET category_id = :categoryId WHERE id = :id")
    suspend fun updateCategory(id: String, categoryId: String)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM transactions")
    suspend fun deleteAll()

    @Query("""
        SELECT c.id as categoryId, c.name as categoryName, c.color, SUM(t.amount) as total
        FROM transactions t 
        JOIN categories c ON t.category_id = c.id
        WHERE (:startDate IS NULL OR t.date >= :startDate) 
        AND (:endDate IS NULL OR t.date <= :endDate)
        AND t.type = 'DEBIT'
        GROUP BY c.id ORDER BY total DESC
    """)
    suspend fun getSpendingByCategory(
        startDate: String? = null,
        endDate: String? = null
    ): List<CategorySpendingResult>

    @Query("""
        SELECT date(date) as day, SUM(amount) as total
        FROM transactions
        WHERE date(date) BETWEEN date(:startDate) AND date(:endDate)
        AND type = 'DEBIT'
        GROUP BY day ORDER BY day ASC
    """)
    suspend fun getDailySpending(startDate: String, endDate: String): List<DailySpendingResult>

    @Query("""
        SELECT merchant, SUM(amount) as total, COUNT(*) as count
        FROM transactions
        WHERE (:startDate IS NULL OR date >= :startDate) 
        AND (:endDate IS NULL OR date <= :endDate)
        AND type = 'DEBIT'
        GROUP BY merchant ORDER BY total DESC LIMIT :limit
    """)
    suspend fun getTopMerchants(
        limit: Int = 5,
        startDate: String? = null,
        endDate: String? = null
    ): List<MerchantSpendingResult>

    @Query("""
        SELECT COALESCE(SUM(amount), 0.0) as total, COUNT(*) as count
        FROM transactions
        WHERE (:startDate IS NULL OR date >= :startDate)
        AND (:endDate IS NULL OR date <= :endDate)
        AND type = 'DEBIT'
    """)
    suspend fun getTotalSpending(
        startDate: String? = null,
        endDate: String? = null
    ): TotalSpendingResult

    @Query("""
        SELECT COALESCE(SUM(amount), 0.0) as total, COUNT(*) as count
        FROM transactions
        WHERE (:startDate IS NULL OR date >= :startDate)
        AND (:endDate IS NULL OR date <= :endDate)
        AND type = 'CREDIT'
    """)
    suspend fun getTotalReceived(
        startDate: String? = null,
        endDate: String? = null
    ): TotalSpendingResult

    @Query("SELECT COUNT(*) FROM transactions")
    suspend fun getCount(): Int

    @Query("""
        SELECT bank_name as bankName, balance
        FROM transactions
        WHERE balance IS NOT NULL AND bank_name IS NOT NULL AND bank_name != 'Unknown'
        GROUP BY bank_name
        HAVING date = MAX(date)
        ORDER BY date DESC
    """)
    suspend fun getLatestBankBalances(): List<BankBalanceResult>

    @Query("SELECT * FROM transactions ORDER BY date DESC LIMIT :limit")
    fun observeRecent(limit: Int = 50): Flow<List<TransactionEntity>>
}

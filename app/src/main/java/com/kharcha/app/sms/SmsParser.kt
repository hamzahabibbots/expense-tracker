package com.kharcha.app.sms

import com.kharcha.app.data.model.Transaction
import com.kharcha.app.util.DefaultCategories
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.abs

data class SmsMessage(
    val id: String,
    val body: String,
    val sender: String?,
    val date: Long // timestamp millis
)

/** SMS parser — extracts transaction data from Indian bank SMS messages */
object SmsParser {

    fun extractAmount(body: String): Double? {
        for (pattern in RegexPatterns.AMOUNT_PATTERNS) {
            val match = pattern.find(body) ?: continue
            val amountStr = match.groupValues[1].replace(",", "")
            val amount = amountStr.toDoubleOrNull()
            if (amount != null && amount > 0) return amount
        }
        return null
    }

    fun extractMerchant(body: String): String? {
        for (pattern in RegexPatterns.MERCHANT_PATTERNS) {
            val match = pattern.find(body) ?: continue
            val merchant = match.groupValues[1].trim()
                .replace(Regex("\\s+"), " ")
                .replace(Regex("[^\\w\\s&@.-]"), "")
                .trim()
            if (merchant.isNotEmpty()) return merchant
        }
        return null
    }

    fun extractDate(body: String, receivedAt: Long): LocalDateTime {
        for (pattern in RegexPatterns.DATE_PATTERNS) {
            val match = pattern.find(body) ?: continue
            try {
                val g1 = match.groupValues[1]
                val g2 = match.groupValues[2]
                val g3 = match.groupValues[3]

                val day = g1.toInt()
                val month: Int
                var year = g3.toInt()

                month = if (g2.toIntOrNull() != null) {
                    g2.toInt()
                } else {
                    getMonthNumber(g2)
                }

                if (year < 100) year += if (year < 50) 2000 else 1900

                return LocalDateTime.of(year, month, day, 0, 0)
            } catch (_: Exception) {
                continue
            }
        }
        return Instant.ofEpochMilli(receivedAt).atZone(ZoneId.systemDefault()).toLocalDateTime()
    }

    fun extractBankName(sender: String?): String? {
        if (sender == null) return null
        for ((bank, pattern) in RegexPatterns.BANK_SENDER_PATTERNS) {
            if (pattern.containsMatchIn(sender)) return bank
        }
        return null
    }

    fun extractBalance(body: String): Double? {
        for (pattern in RegexPatterns.BALANCE_PATTERNS) {
            val match = pattern.find(body) ?: continue
            val balanceStr = match.groupValues[1].replace(",", "")
            return balanceStr.toDoubleOrNull()
        }
        return null
    }

    fun parseSms(sms: SmsMessage): Transaction? {
        // Restrict to only read/interpret from INDUS chat to filter out spam/fakes
        if (sms.sender == null || !sms.sender.contains("INDUS", ignoreCase = true)) return null

        val type = RegexPatterns.getTransactionType(sms.body, sms.sender) ?: return null

        val amount = extractAmount(sms.body) ?: return null
        val merchant = extractMerchant(sms.body) ?: if (type == "CREDIT") "Received" else "Unknown"
        val date = extractDate(sms.body, sms.date)
        
        // Strict cutoff - April 1, 2026
        if (date.toLocalDate().isBefore(LocalDate.of(2026, 4, 1))) return null

        val bankName = extractBankName(sms.sender)
        val balance = extractBalance(sms.body)
        val categoryId = if (type == "CREDIT") "income" else DefaultCategories.suggestCategory(merchant)

        return Transaction(
            id = generateId(sms),
            amount = amount,
            merchant = merchant,
            date = date.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            bankName = bankName ?: "Unknown",
            categoryId = categoryId,
            rawSms = sms.body,
            sender = sms.sender,
            type = type,
            balance = balance,
            createdAt = Instant.now().toString()
        )
    }

    fun parseMultiple(smsList: List<SmsMessage>): List<Transaction> {
        return smsList.mapNotNull { parseSms(it) }
    }

    fun isDuplicate(t1: Transaction, t2: Transaction): Boolean {
        if (t1.amount != t2.amount) return false
        if (!t1.merchant.equals(t2.merchant, ignoreCase = true)) return false
        try {
            val d1 = LocalDateTime.parse(t1.date.take(19))
            val d2 = LocalDateTime.parse(t2.date.take(19))
            val diffMinutes = abs(
                d1.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() -
                d2.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            ) / 60000
            return diffMinutes <= 5
        } catch (_: Exception) {
            return false
        }
    }

    fun filterDuplicates(
        newTransactions: List<Transaction>,
        existing: List<Transaction>
    ): List<Transaction> {
        return newTransactions.filter { newTx ->
            existing.none { isDuplicate(newTx, it) }
        }
    }

    private fun generateId(sms: SmsMessage): String {
        val sender = (sms.sender ?: "UNKNOWN").replace("\\s".toRegex(), "")
        val hash = abs(sms.body.hashCode()).toString(36).take(8)
        return "${sender}_${sms.date}_$hash"
    }

    private fun getMonthNumber(name: String): Int {
        return when (name.lowercase().take(3)) {
            "jan" -> 1; "feb" -> 2; "mar" -> 3; "apr" -> 4
            "may" -> 5; "jun" -> 6; "jul" -> 7; "aug" -> 8
            "sep" -> 9; "oct" -> 10; "nov" -> 11; "dec" -> 12
            else -> 1
        }
    }
}

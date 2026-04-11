package com.kharcha.app.util

import java.text.DecimalFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object FormatUtils {

    /** Format amount in Indian Rupee style: ₹1,23,456.78 */
    fun formatAmount(amount: Double?): String {
        if (amount == null || amount == 0.0) return "₹0.00"
        val parts = String.format(Locale.US, "%.2f", amount).split(".")
        val intPart = parts[0]
        val decPart = parts[1]

        val formatted = buildString {
            val len = intPart.length
            if (len <= 3) {
                append(intPart)
            } else {
                // Last 3 digits
                val last3 = intPart.substring(len - 3)
                val remaining = intPart.substring(0, len - 3)
                // Remaining digits in groups of 2
                var i = remaining.length
                var first = true
                while (i > 0) {
                    val start = maxOf(0, i - 2)
                    if (!first) append(",")
                    append(remaining.substring(start, i))
                    first = false
                    i = start
                }
                append(",")
                append(last3)
            }
        }
        return "₹$formatted.$decPart"
    }

    /** Short date format: "02 Apr" */
    fun formatDateShort(dateString: String): String {
        return try {
            val dt = LocalDateTime.parse(dateString.take(19))
            dt.format(DateTimeFormatter.ofPattern("dd MMM", Locale.ENGLISH))
        } catch (_: Exception) {
            try {
                val d = LocalDate.parse(dateString.take(10))
                d.format(DateTimeFormatter.ofPattern("dd MMM", Locale.ENGLISH))
            } catch (_: Exception) {
                dateString.take(10)
            }
        }
    }

    /** Full date format: "02 Apr, 2025 · 3:45 PM" */
    fun formatDateFull(dateString: String): String {
        return try {
            val dt = LocalDateTime.parse(dateString.take(19))
            dt.format(DateTimeFormatter.ofPattern("dd MMM · h:mm a", Locale.ENGLISH))
        } catch (_: Exception) {
            formatDateShort(dateString)
        }
    }

    /** Group header date: "02 Apr, 2025" */
    fun formatDateHeader(dateString: String): String {
        return try {
            val d = LocalDate.parse(dateString.take(10))
            d.format(DateTimeFormatter.ofPattern("dd MMM, yyyy", Locale.ENGLISH))
        } catch (_: Exception) {
            dateString.take(10)
        }
    }

    /** Chart label: "2/4" */
    fun formatChartDate(dateString: String): String {
        return try {
            val d = LocalDate.parse(dateString.take(10))
            "${d.dayOfMonth}/${d.monthValue}"
        } catch (_: Exception) {
            ""
        }
    }
}

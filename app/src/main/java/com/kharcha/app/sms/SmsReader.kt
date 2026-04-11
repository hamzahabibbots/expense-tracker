package com.kharcha.app.sms

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.content.ContextCompat

/**
 * Native Android SMS reader using ContentResolver.
 * Reads actual SMS messages from the device inbox.
 */
class SmsReader(private val context: Context) {

    fun hasPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context, Manifest.permission.READ_SMS
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Read SMS messages from the device.
     * @param since Only read messages after this timestamp (millis). Null = read all.
     * @param limit Maximum number of messages to read.
     */
    fun readMessages(since: Long? = null, limit: Int = 500): List<SmsMessage> {
        if (!hasPermission()) return emptyList()

        val messages = mutableListOf<SmsMessage>()
        val uri = Uri.parse("content://sms/inbox")

        val selection = if (since != null) "date > ?" else null
        val selectionArgs = if (since != null) arrayOf(since.toString()) else null

        val cursor = context.contentResolver.query(
            uri,
            arrayOf("_id", "address", "body", "date"),
            selection,
            selectionArgs,
            "date DESC"
        )

        cursor?.use {
            val idIdx = it.getColumnIndexOrThrow("_id")
            val addressIdx = it.getColumnIndexOrThrow("address")
            val bodyIdx = it.getColumnIndexOrThrow("body")
            val dateIdx = it.getColumnIndexOrThrow("date")

            var count = 0
            while (it.moveToNext() && count < limit) {
                val id = it.getString(idIdx)
                val address = it.getString(addressIdx)
                val body = it.getString(bodyIdx)
                val date = it.getLong(dateIdx)

                if (!body.isNullOrBlank()) {
                    messages.add(SmsMessage(id, body, address, date))
                    count++
                }
            }
        }

        return messages
    }

    /**
     * Provide mock SMS data for testing/development (same as the RN app).
     */
    fun getMockMessages(): List<SmsMessage> {
        val now = System.currentTimeMillis()
        return listOf(
            SmsMessage(
                "1",
                "Your HDFC Bank account ending 1234 has been debited Rs. 250.00 at SWIGGY on 02-04-2025. Available balance: Rs. 15,234.50",
                "HDFCBK", now - 86400000
            ),
            SmsMessage(
                "2",
                "Transaction Alert: Rs. 1,500.00 debited from your SBI account for rent payment. UPI Ref: 123456789012",
                "SBIMB", now - 172800000
            ),
            SmsMessage(
                "3",
                "ICICI Bank: Rs. 450.50 spent at ZOMATO via card ending 5678 on 01-04-2025",
                "ICICICC", now - 200000000
            ),
            SmsMessage(
                "4",
                "Credited: Rs. 50,000.00 to your account. Salary credit from EMPLOYER",
                "HDFCBK", now - 100000000
            ),
            SmsMessage(
                "5",
                "OTP for login is 123456. Valid for 10 minutes. Do not share.",
                "HDFCBK", now - 50000000
            ),
            SmsMessage(
                "6",
                "Rs. 180.00 deducted for Uber ride on 31-03-2025. Thank you for using HDFC Card",
                "HDFCCC", now - 250000000
            ),
        )
    }
}

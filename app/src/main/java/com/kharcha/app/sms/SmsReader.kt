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
        return emptyList()
    }
}

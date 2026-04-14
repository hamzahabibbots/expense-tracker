package com.kharcha.app.sms

/** Indian bank SMS regex patterns — ported from regexPatterns.js */
object RegexPatterns {

    /** Amount patterns: Rs. 1,234.56 / INR 1234 / debited Rs. 500 etc. */
    val AMOUNT_PATTERNS = listOf(
        Regex("""(?:Rs\.?\s*|INR\s*)([\d,]+(?:\.\d{1,2})?)""", RegexOption.IGNORE_CASE),
        Regex("""(?:debited|deducted|paid|spent|sent)\s*(?:Rs\.?\s*|INR\s*)?([\d,]+(?:\.\d{1,2})?)""", RegexOption.IGNORE_CASE),
        Regex("""([\d,]+(?:\.\d{1,2})?)\s*(?:Rs\.?|INR)?\s*(?:to|at)""", RegexOption.IGNORE_CASE),
    )

    /** Merchant/payee name extraction patterns */
    val MERCHANT_PATTERNS = listOf(
        Regex("""(?:paid|transferred|sent)\s+(?:to\s+)?([A-Z][A-Za-z0-9_\s]*)(?:\s*@\s*upi|\s*\d{4})""", RegexOption.IGNORE_CASE),
        Regex("""(?:at|to|towards)\s+([A-Z][A-Za-z0-9\s&.,_-]+?)(?:\s+(?:on|using|via|for|txn|ref)|\s*\d{1,2}[-/]\d{1,2}[-/]\d{2,4}|\.|$)""", RegexOption.IGNORE_CASE),
        Regex("""([A-Z][A-Za-z0-9\s&.,_-]+?)\s+(?:card\s+(?:ending|xxx)|on\s+\d)""", RegexOption.IGNORE_CASE),
        Regex("""([a-zA-Z0-9._-]+@[a-zA-Z]+)""", RegexOption.IGNORE_CASE),
    )

    /** Bank Balance extraction patterns */
    val BALANCE_PATTERNS = listOf(
        Regex("""(?:bal|balance|avl bal|available balance|avl limit|limit|available(?: bal| balance)?)\s*(?:is|:|-)?\s*(?:rs\.?|inr\.?)?\s*([\d,]+(?:\.\d{1,2})?)""", RegexOption.IGNORE_CASE),
        Regex("""(?:\b(?:bal|balance):?\s*(?:rs\.?|inr\.?)?\s*)([\d,]+(?:\.\d{1,2})?)\b""", RegexOption.IGNORE_CASE)
    )

    /** Date patterns */
    val DATE_PATTERNS = listOf(
        Regex("""(\d{1,2})[-/](\d{1,2})[-/](\d{2,4})"""),
        Regex("""(\d{1,2})[-\s]([A-Za-z]{3})[-\s](\d{2,4})"""),
        Regex("""(\d{1,2})\s+(January|February|March|April|May|June|July|August|September|October|November|December)\s+(\d{2,4})""", RegexOption.IGNORE_CASE),
    )

    /** Bank sender ID patterns (Indian banks) */
    val BANK_SENDER_PATTERNS = mapOf(
        "SBI" to Regex("""^(?:SBI|SBIMB|SBIUPI|SBICRD|SBIPSG)""", RegexOption.IGNORE_CASE),
        "HDFC" to Regex("""^(?:HDFC|HDFCBK|HDFCER|HDFCCC|HDFCDC|HDFCUPI)""", RegexOption.IGNORE_CASE),
        "ICICI" to Regex("""^(?:ICICI|ICICIB|ICICICC|ICICIUPI)""", RegexOption.IGNORE_CASE),
        "AXIS" to Regex("""^(?:AXIS|AXISBK|AXISUPI|AXISCC|AXISDC)""", RegexOption.IGNORE_CASE),
        "KOTAK" to Regex("""^(?:KOTAK|KOTAKB|KOTAKCC|KOTAKUPI)""", RegexOption.IGNORE_CASE),
        "PNB" to Regex("""^(?:PNB|PNBSMS|PNBUPI|PNBCC)""", RegexOption.IGNORE_CASE),
        "BOB" to Regex("""^(?:BOB|BOIBNK|BOIIND|BOBUPI)""", RegexOption.IGNORE_CASE),
        "YES" to Regex("""^(?:YESBNK|YESBANK|YESUPI)""", RegexOption.IGNORE_CASE),
        "INDUSIND" to Regex("""INDUS""", RegexOption.IGNORE_CASE),
        "PHONEPE" to Regex("""PHONEPE""", RegexOption.IGNORE_CASE),
        "GPAY" to Regex("""(?:GPAY|GOOGLEP|GPY|TEZ)""", RegexOption.IGNORE_CASE),
        "PAYTM" to Regex("""(?:PAYTM|PAYTMB|PAYTMPY)""", RegexOption.IGNORE_CASE),
    )

    val DEBIT_KEYWORDS = listOf(
        "debited", "deducted", "paid", "spent", "sent", "withdrawn",
        "transaction successful", "payment successful", "money sent",
        "funds transferred", "purchase", "charged", "autopay", "auto-pay"
    )

    val CREDIT_KEYWORDS = listOf(
        "credited", "received", "deposited", "added to your account",
        "cashback", "refund", "reward", "reversal"
    )

    val EXCLUDE_KEYWORDS = listOf(
        "otp", "one time password", "verification code", "login", "sign in",
        "offers", "discount", "cashback offer", "loan offer",
        "credit card application", "pre-approved", "welcome to",
        "statement generated", "bill generated", "minimum amount due",
        "total amount due", "payment due", "emi reminder"
    )

    fun getTransactionType(body: String, sender: String?): String? {
        val bodyLower = body.lowercase()

        // Check exclusion keywords
        if (EXCLUDE_KEYWORDS.any { bodyLower.contains(it) }) return null

        // Check credit keywords
        if (CREDIT_KEYWORDS.any { bodyLower.contains(it) }) return "CREDIT"

        // Check debit keywords
        if (DEBIT_KEYWORDS.any { bodyLower.contains(it) }) return "DEBIT"

        // If from a bank and has amount, we default to DEBIT if it's a known transaction pattern
        if (sender != null) {
            val isBankSender = BANK_SENDER_PATTERNS.values.any { it.containsMatchIn(sender) }
            if (isBankSender && AMOUNT_PATTERNS.any { it.containsMatchIn(body) }) return "DEBIT"
        }

        return null
    }
}

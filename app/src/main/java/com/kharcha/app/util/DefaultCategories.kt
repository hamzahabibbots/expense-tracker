package com.kharcha.app.util

import com.kharcha.app.data.model.Category

object DefaultCategories {
    val ALL = listOf(
        Category("food", "Food", "#FF6B6B", "restaurant", true),
        Category("transport", "Transport", "#4ECDC4", "car", true),
        Category("rent_bills", "Rent/Bills", "#45B7D1", "home", true),
        Category("health", "Health", "#96CEB4", "medical", true),
        Category("entertainment", "Entertainment", "#FFEAA7", "film", true),
        Category("shopping", "Shopping", "#DDA0DD", "cart", true),
        Category("other", "Other", "#B8B8B8", "ellipsis-horizontal", true),
    )

    /** Keyword map for auto-categorization of transactions by merchant name */
    private val MERCHANT_KEYWORDS = mapOf(
        "food" to listOf(
            "zomato", "swiggy", "restaurant", "cafe", "food", "pizza", "burger",
            "dominos", "mcd", "mcdonalds", "kfc", "subway", "starbucks", "barista",
            "costa", "dunkin", "bakery", "biryani", "dhaba", "hotel", "dining",
            "groceries", "grocery", "supermarket", "mart", "bigbasket", "blinkit",
            "zepto", "instamart", "eat", "kitchen", "catering", "tiffin", "lunch",
            "dinner", "breakfast", "snacks", "sweet", "mithai", "namkeen"
        ),
        "transport" to listOf(
            "uber", "ola", "rapido", "auto", "taxi", "cab", "metro", "bus",
            "train", "railway", "irctc", "petrol", "diesel", "fuel", "hpcl",
            "bp", "shell", "indian oil", "bharat", "vehicle", "parking",
            "toll", "fastag", "traffic", "transport", "travel", "booking",
            "ticket", "pass", "conveyance", "ride", "bike", "scooter"
        ),
        "rent_bills" to listOf(
            "rent", "electricity", "water", "gas", "internet", "broadband",
            "wifi", "phone", "mobile", "recharge", "bill", "utility",
            "maintenance", "society", "housing", "loan", "emi", "credit card",
            "insurance", "premium", "property", "apartment", "flat", "house",
            "electric", "power", "energy", "solar", "lpg", "cylinder"
        ),
        "health" to listOf(
            "hospital", "clinic", "doctor", "medicine", "pharmacy", "medical",
            "health", "wellness", "gym", "fitness", "yoga", "diagnostic",
            "lab", "test", "scan", "dentist", "physician", "specialist",
            "apollo", "max", "fortis", "medanta", "lilavati", "therapy",
            "treatment", "consultation"
        ),
        "entertainment" to listOf(
            "netflix", "prime", "amazon prime", "hotstar", "disney", "spotify",
            "youtube", "music", "movie", "cinema", "theatre", "pvr", "inox",
            "entertainment", "subscription", "ott", "streaming", "gaming",
            "game", "playstation", "xbox", "event", "concert", "show",
            "bookmyshow", "ticketnew", "party", "club", "bar", "pub"
        ),
        "shopping" to listOf(
            "amazon", "flipkart", "myntra", "ajio", "nykaa", "meesho",
            "shop", "store", "mall", "retail", "clothing", "fashion",
            "apparel", "accessories", "electronics", "laptop", "gadget",
            "furniture", "decor", "snapdeal", "tatacliq", "reliance",
            "bigbazaar", "dmart", "market", "purchase", "buy", "sale"
        )
    )

    fun suggestCategory(merchantName: String?): String {
        if (merchantName.isNullOrBlank()) return "other"
        val lower = merchantName.lowercase()
        for ((categoryId, keywords) in MERCHANT_KEYWORDS) {
            if (keywords.any { lower.contains(it) }) return categoryId
        }
        return "other"
    }
}

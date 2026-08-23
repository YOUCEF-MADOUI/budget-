package com.budgetplusplus.core.designsystem.icons

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

/** Central icon entry point so temporary Material icons can be replaced without touching features. */
object BudgetIcons {
    val Add = Icons.Default.Add
    val Home = Icons.Default.Home
    val Account = Icons.Default.AccountBalanceWallet
    val Category = Icons.Default.Category
    val Search = Icons.Default.Search
}

/** Stable keys persisted in Room for the category icon catalogue. */
object CategoryIconCatalog {
    val keys = listOf(
        "category", "food", "transport", "housing", "health", "leisure",
        "utilities", "education", "family", "clothing", "taxes", "salary",
        "freelance", "pension", "benefits", "gift", "other", "shopping",
        "coffee", "fuel", "travel", "pets", "phone", "savings", "sports",
        "tools", "construction", "electricity", "water", "painting",
    )

    fun icon(key: String): ImageVector = when (key) {
        "food" -> Icons.Default.Restaurant
        "transport" -> Icons.Default.DirectionsCar
        "housing" -> Icons.Default.Home
        "health" -> Icons.Default.LocalHospital
        "leisure" -> Icons.Default.SportsEsports
        "utilities" -> Icons.Default.ReceiptLong
        "education" -> Icons.Default.School
        "family" -> Icons.Default.FamilyRestroom
        "clothing" -> Icons.Default.Checkroom
        "taxes" -> Icons.Default.AccountBalance
        "salary" -> Icons.Default.Payments
        "freelance" -> Icons.Default.Work
        "pension" -> Icons.Default.Elderly
        "benefits" -> Icons.Default.VolunteerActivism
        "gift" -> Icons.Default.CardGiftcard
        "other" -> Icons.Default.MoreHoriz
        "shopping" -> Icons.Default.ShoppingCart
        "coffee" -> Icons.Default.LocalCafe
        "fuel" -> Icons.Default.LocalGasStation
        "travel" -> Icons.Default.Flight
        "pets" -> Icons.Default.Pets
        "phone" -> Icons.Default.PhoneAndroid
        "savings" -> Icons.Default.Savings
        "sports" -> Icons.Default.FitnessCenter
        "tools" -> Icons.Default.Build
        "construction" -> Icons.Default.Construction
        "electricity" -> Icons.Default.ElectricalServices
        "water" -> Icons.Default.WaterDrop
        "painting" -> Icons.Default.Palette
        else -> BudgetIcons.Category
    }

    fun effectiveKey(iconKey: String, nameKey: String?): String {
        if (iconKey != "category") return iconKey
        return when (nameKey) {
            "category_food" -> "food"
            "category_transport" -> "transport"
            "category_housing" -> "housing"
            "category_health" -> "health"
            "category_leisure" -> "leisure"
            "category_utilities" -> "utilities"
            "category_education" -> "education"
            "category_family" -> "family"
            "category_clothing" -> "clothing"
            "category_taxes" -> "taxes"
            "category_salary" -> "salary"
            "category_freelance" -> "freelance"
            "category_pension" -> "pension"
            "category_benefits" -> "benefits"
            "category_gift" -> "gift"
            "category_other_income" -> "other"
            else -> iconKey
        }
    }
}

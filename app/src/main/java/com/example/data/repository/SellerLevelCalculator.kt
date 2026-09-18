package com.example.data.repository

object SellerLevelCalculator {
    data class LevelInfo(
        val level: Int,
        val title: String,
        val badgeName: String,
        val minSales: Int,
        val maxSales: Int,
        val perkDescription: String,
        val starCount: Int
    )

    fun calculateLevel(itemsSoldCount: Int): Int {
        return when {
            itemsSoldCount <= 5 -> 1
            itemsSoldCount <= 15 -> 2
            itemsSoldCount <= 30 -> 3
            itemsSoldCount <= 50 -> 4
            else -> 5
        }
    }

    fun getLevelInfo(itemsSoldCount: Int): LevelInfo {
        val level = calculateLevel(itemsSoldCount)
        return when (level) {
            1 -> LevelInfo(
                level = 1,
                title = "Starter Seller",
                badgeName = "Bronze",
                minSales = 0,
                maxSales = 4,
                perkDescription = "Basic marketplace access",
                starCount = 1
            )
            2 -> LevelInfo(
                level = 2,
                title = "Silver Merchant",
                badgeName = "Silver",
                minSales = 5,
                maxSales = 14,
                perkDescription = "Priority search visibility",
                starCount = 2
            )
            3 -> LevelInfo(
                level = 3,
                title = "Gold Merchant",
                badgeName = "Gold",
                minSales = 15,
                maxSales = 29,
                perkDescription = "Featured listings booster & fast payouts",
                starCount = 3
            )
            4 -> LevelInfo(
                level = 4,
                title = "Platinum Trader",
                badgeName = "Platinum",
                minSales = 30,
                maxSales = 49,
                perkDescription = "Verified badge & zero listing fee discount",
                starCount = 4
            )
            else -> LevelInfo(
                level = 5,
                title = "Master Ambassador",
                badgeName = "Diamond",
                minSales = 50,
                maxSales = 1000,
                perkDescription = "VIP top carousel placement & exclusive merchant support",
                starCount = 5
            )
        }
    }

    fun getNextLevelTarget(itemsSoldCount: Int): String {
        val level = calculateLevel(itemsSoldCount)
        return when (level) {
            1 -> "${5 - itemsSoldCount} sales to Level 2 (Silver)"
            2 -> "${15 - itemsSoldCount} sales to Level 3 (Gold)"
            3 -> "${30 - itemsSoldCount} sales to Level 4 (Platinum)"
            4 -> "${50 - itemsSoldCount} sales to Level 5 (Master)"
            else -> "Maximum Master Level Reached!"
        }
    }
}

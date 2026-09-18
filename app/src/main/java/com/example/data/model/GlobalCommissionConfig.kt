package com.example.data.model

/**
 * Global Commission Configuration Model for D.K.K. Market Platform Owner.
 * Allows live toggling of commission structures, rates, VIP seller discounts,
 * flat fees, and audio room live gift cuts.
 */
data class GlobalCommissionConfig(
    val isTieredCommissionEnabled: Boolean = true,
    val defaultFlatCommissionRatePercent: Double = 8.0,
    val tier1MaxAmount: Double = 10000.0,
    val tier1RatePercent: Double = 10.0,
    val tier2MaxAmount: Double = 20000.0,
    val tier2RatePercent: Double = 8.0,
    val tier3MaxAmount: Double = 35000.0,
    val tier3RatePercent: Double = 7.0,
    val tier4RatePercent: Double = 5.0,
    val isVipDiscountEnabled: Boolean = true,
    val vipDiscountPercent: Double = 2.0,
    val autoDeductOnDelivery: Boolean = true,
    val isPlatformFeeEnabled: Boolean = false,
    val fixedPlatformFeePkr: Double = 50.0,
    val audioGiftsCommissionPercent: Double = 15.0,
    val lastUpdatedTimestamp: Long = System.currentTimeMillis()
)

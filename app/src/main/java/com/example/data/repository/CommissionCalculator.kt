package com.example.data.repository

import com.example.data.model.GlobalCommissionConfig

object CommissionCalculator {
    var config: GlobalCommissionConfig = GlobalCommissionConfig()
        private set

    fun updateConfig(newConfig: GlobalCommissionConfig) {
        config = newConfig
    }

    fun resetToDefaults() {
        config = GlobalCommissionConfig()
    }

    /**
     * Tiered or Flat Commission Formula with dynamic global settings:
     * Supports configurable rates, VIP discount, and flat platform fee.
     */
    fun getCommissionRate(orderAmount: Double, isVipSeller: Boolean = false): Double {
        val baseRate = if (config.isTieredCommissionEnabled) {
            when {
                orderAmount <= config.tier1MaxAmount -> config.tier1RatePercent / 100.0
                orderAmount <= config.tier2MaxAmount -> config.tier2RatePercent / 100.0
                orderAmount <= config.tier3MaxAmount -> config.tier3RatePercent / 100.0
                else -> config.tier4RatePercent / 100.0
            }
        } else {
            config.defaultFlatCommissionRatePercent / 100.0
        }

        val discount = if (isVipSeller && config.isVipDiscountEnabled) {
            config.vipDiscountPercent / 100.0
        } else 0.0

        return (baseRate - discount).coerceAtLeast(0.01)
    }

    fun getCommissionRatePercent(orderAmount: Double, isVipSeller: Boolean = false): Double {
        return getCommissionRate(orderAmount, isVipSeller) * 100.0
    }

    fun calculateCommission(orderAmount: Double, isVipSeller: Boolean = false): Double {
        val rateCommission = orderAmount * getCommissionRate(orderAmount, isVipSeller)
        val platformFee = if (config.isPlatformFeeEnabled) config.fixedPlatformFeePkr else 0.0
        return rateCommission + platformFee
    }

    fun getTierName(orderAmount: Double): String {
        return if (config.isTieredCommissionEnabled) {
            when {
                orderAmount <= config.tier1MaxAmount -> "Tier 1 (≤ Rs ${config.tier1MaxAmount.toInt()} • ${config.tier1RatePercent.toInt()}%)"
                orderAmount <= config.tier2MaxAmount -> "Tier 2 (Rs ${(config.tier1MaxAmount + 1).toInt()} - ${config.tier2MaxAmount.toInt()} • ${config.tier2RatePercent.toInt()}%)"
                orderAmount <= config.tier3MaxAmount -> "Tier 3 (Rs ${(config.tier2MaxAmount + 1).toInt()} - ${config.tier3MaxAmount.toInt()} • ${config.tier3RatePercent.toInt()}%)"
                else -> "Tier 4 (> Rs ${config.tier3MaxAmount.toInt()} • ${config.tier4RatePercent.toInt()}%)"
            }
        } else {
            "Flat Platform Rate (${config.defaultFlatCommissionRatePercent}%)"
        }
    }

    fun getTierNumber(orderAmount: Double): Int {
        return when {
            orderAmount <= config.tier1MaxAmount -> 1
            orderAmount <= config.tier2MaxAmount -> 2
            orderAmount <= config.tier3MaxAmount -> 3
            else -> 4
        }
    }
}

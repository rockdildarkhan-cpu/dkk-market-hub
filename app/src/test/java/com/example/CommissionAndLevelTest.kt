package com.example

import com.example.data.repository.CommissionCalculator
import com.example.data.repository.SellerLevelCalculator
import org.junit.Assert.assertEquals
import org.junit.Test

class CommissionAndLevelTest {

    @Test
    fun testTieredCommissionFormula() {
        // Tier 1: <= 10,000 -> 10%
        assertEquals(10.0, CommissionCalculator.getCommissionRatePercent(5000.0), 0.01)
        assertEquals(500.0, CommissionCalculator.calculateCommission(5000.0), 0.01)
        assertEquals(1000.0, CommissionCalculator.calculateCommission(10000.0), 0.01)

        // Tier 2: 10,001 - 20,000 -> 8%
        assertEquals(8.0, CommissionCalculator.getCommissionRatePercent(15000.0), 0.01)
        assertEquals(1200.0, CommissionCalculator.calculateCommission(15000.0), 0.01)

        // Tier 3: 20,001 - 35,000 -> 7%
        assertEquals(7.0, CommissionCalculator.getCommissionRatePercent(30000.0), 0.01)
        assertEquals(2100.0, CommissionCalculator.calculateCommission(30000.0), 0.01)

        // Tier 4: > 35,000 -> 5%
        assertEquals(5.0, CommissionCalculator.getCommissionRatePercent(85000.0), 0.01)
        assertEquals(4250.0, CommissionCalculator.calculateCommission(85000.0), 0.01)
    }

    @Test
    fun testSellerLevelProgression() {
        // Level 1: 0-5 sales
        assertEquals(1, SellerLevelCalculator.calculateLevel(0))
        assertEquals(1, SellerLevelCalculator.calculateLevel(5))

        // Level 2: 6-15 sales
        assertEquals(2, SellerLevelCalculator.calculateLevel(6))
        assertEquals(2, SellerLevelCalculator.calculateLevel(15))

        // Level 3: 16-30 sales
        assertEquals(3, SellerLevelCalculator.calculateLevel(16))
        assertEquals(3, SellerLevelCalculator.calculateLevel(30))

        // Level 4: 31-50 sales
        assertEquals(4, SellerLevelCalculator.calculateLevel(31))
        assertEquals(4, SellerLevelCalculator.calculateLevel(50))

        // Level 5: 51+ sales
        assertEquals(5, SellerLevelCalculator.calculateLevel(51))
        assertEquals(5, SellerLevelCalculator.calculateLevel(100))
    }
}

package com.example.data.security

/**
 * DKK Marketing - 1st Seller (Muhammad Aslam - Universe Jewellery) Identity & Security Guard
 */
object SellerSecurityGuard {
    const val SELLER_1_PHONE = "03108219408"
    const val SELLER_1_PHONE_INTL = "+923108219408"
    const val SELLER_1_PHONE_FORMATTED = "+92 310 8219408"
    const val SELLER_1_NAME = "Muhammad Aslam"
    const val SELLER_1_BUSINESS = "Universe Jewellery"
    const val SELLER_1_FULL_DISPLAY = "Muhammad Aslam (Universe Jewellery)"
    const val SELLER_1_PASSWORD = "aslamdkk1"
    const val SELLER_1_EMAIL = "aslam.universe@dkk.pk"
    const val SELLER_1_UID = "SELLER_001"

    fun normalizePhone(phone: String): String {
        val clean = phone.trim().replace(" ", "").replace("-", "")
        return if (clean.startsWith("+92")) "0" + clean.removePrefix("+92")
        else if (clean.startsWith("92") && clean.length == 12) "0" + clean.removePrefix("92")
        else clean
    }

    fun isSeller1Phone(phone: String): Boolean {
        val clean = normalizePhone(phone)
        return clean == SELLER_1_PHONE || clean == "03108219408" || clean == "3108219408"
    }

    fun verifySellerPassword(phone: String, inputPassword: String): Boolean {
        if (isSeller1Phone(phone)) {
            return inputPassword.trim() == SELLER_1_PASSWORD
        }
        return inputPassword.trim() == SELLER_1_PASSWORD
    }
}

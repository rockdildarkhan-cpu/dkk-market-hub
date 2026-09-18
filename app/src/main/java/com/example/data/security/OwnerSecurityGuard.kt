package com.example.data.security

/**
 * DKK Marketing - Strict Owner Password Validation & Identity Guard
 * Implements Ufone Number Identity Guard & High-Security Alphanumeric Password Verification.
 */
object OwnerSecurityGuard {
    const val MASTER_ADMIN_PHONE = "03330206001"
    const val MASTER_ADMIN_NAME = "Daro Khan"
    const val MASTER_ADMIN_EMAIL = "rockdildarkhan@gmail.com"

    // High-Security Alphanumeric Master Passwords (Uppercase, lowercase, numbers, special characters)
    val VALID_MASTER_PASSWORDS = listOf(
        "Naqeeb231#\$_1"
    )

    fun isOwnerPhone(phone: String): Boolean {
        val clean = phone.trim().replace(" ", "").replace("-", "")
        return clean == MASTER_ADMIN_PHONE || clean == "+923330206001" || clean == "923330206001"
    }

    fun isOwnerEmail(email: String): Boolean {
        return email.trim().equals(MASTER_ADMIN_EMAIL, ignoreCase = true)
    }

    /**
     * DKK Marketing - Strict Owner Password Validation Code
     * Verifies Ufone identity and alphanumeric secure password.
     */
    fun verifyOwnerAccess(phoneNumber: String, inputPassword: String): Boolean {
        if (!isOwnerPhone(phoneNumber)) {
            return false
        }
        val trimmedPassword = inputPassword.trim()
        return VALID_MASTER_PASSWORDS.any { it == trimmedPassword }
    }
}

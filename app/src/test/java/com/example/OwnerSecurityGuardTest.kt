package com.example

import com.example.data.security.OwnerSecurityGuard
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OwnerSecurityGuardTest {

    @Test
    fun testOwnerPhoneIdentification() {
        assertTrue(OwnerSecurityGuard.isOwnerPhone("03330206001"))
        assertTrue(OwnerSecurityGuard.isOwnerPhone("+923330206001"))
        assertTrue(OwnerSecurityGuard.isOwnerPhone("923330206001"))
        assertTrue(OwnerSecurityGuard.isOwnerPhone(" 0333-0206001 "))

        // Other numbers should fail
        assertFalse(OwnerSecurityGuard.isOwnerPhone("03219876543"))
        assertFalse(OwnerSecurityGuard.isOwnerPhone("03273856001"))
        assertFalse(OwnerSecurityGuard.isOwnerPhone("03001234567"))
    }

    @Test
    fun testOwnerHighSecurityPasswordVerification() {
        // Correct owner + only valid master password
        assertTrue(OwnerSecurityGuard.verifyOwnerAccess("03330206001", "Naqeeb231#\$_1"))

        // Removed old passwords MUST FAIL
        assertFalse(OwnerSecurityGuard.verifyOwnerAccess("03330206001", "AapKaBadaStrongPassword123!"))
        assertFalse(OwnerSecurityGuard.verifyOwnerAccess("03330206001", "DaroKhan#7860$"))
        assertFalse(OwnerSecurityGuard.verifyOwnerAccess("03330206001", "DkkMarketing@Admin2026!"))
        assertFalse(OwnerSecurityGuard.verifyOwnerAccess("03330206001", "DaroKhan@786!"))

        // Legacy 4-digit PINs MUST FAIL
        assertFalse(OwnerSecurityGuard.verifyOwnerAccess("03330206001", "7860"))
        assertFalse(OwnerSecurityGuard.verifyOwnerAccess("03330206001", "1234"))
        assertFalse(OwnerSecurityGuard.verifyOwnerAccess("03330206001", "0000"))

        // Random password MUST FAIL
        assertFalse(OwnerSecurityGuard.verifyOwnerAccess("03330206001", "wrong_password"))

        // Other phone number with correct password MUST FAIL (Double Verification)
        assertFalse(OwnerSecurityGuard.verifyOwnerAccess("03219876543", "Naqeeb231#\$_1"))
        assertFalse(OwnerSecurityGuard.verifyOwnerAccess("03273856001", "Naqeeb231#\$_1"))
    }
}

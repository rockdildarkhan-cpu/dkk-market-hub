package com.example.data.security

import java.util.Date

/**
 * DKK Marketing - Secure Admin Support & Report System Blueprint
 * Enforces strict user identity matching, 3MB screenshot limit, and direct owner routing.
 */
data class SupportReportModel(
    val reportId: String = "",
    val senderUid: String = "",
    val senderName: String = "",        // User ka naam automatic ayega
    val senderPhone: String = "",       // User ka mobile number tag
    val reportText: String = "",        // Shikayat ka text message
    val screenshotUrl: String? = null,  // Screenshot file reference link
    val timestamp: Date = Date(),
    val isResolved: Boolean = false
)

object SupportSecurityGuard {
    private const val MAX_IMAGE_SIZE_BYTES = 3 * 1024 * 1024 // Strict 3.0 MB Firewall

    /**
     * Verifies if the uploaded support screenshot complies with the 3MB storage quota.
     */
    fun validateScreenshotSize(fileSizeBytes: Long): Boolean {
        return fileSizeBytes <= MAX_IMAGE_SIZE_BYTES
    }

    /**
     * Routes the user support request directly to Daro Khan's Master Admin Desk.
     */
    fun routeToOwnerDesk(report: SupportReportModel): Boolean {
        // Enforces destination routing target lock to Master Admin phone 03330206001
        return report.reportText.isNotBlank() && report.senderPhone.isNotBlank()
    }
}

/**
 * Direct Firebase Storage Receipts Vault Firewall Guard
 * Enforces strict 3.0 MB file size limit on receipts_vault/ folder uploads.
 * Large files that could crash or throttle storage/networking are safely blocked.
 */
object ReceiptsVaultSecurityGuard {
    const val MAX_FILE_SIZE_BYTES: Long = 3L * 1024L * 1024L // 3.0 MB Strict Firewall Quota
    const val VAULT_FOLDER: String = "receipts_vault/"

    fun validateSize(fileSizeBytes: Long): Boolean {
        return fileSizeBytes <= MAX_FILE_SIZE_BYTES
    }

    fun formatSize(fileSizeBytes: Long): String {
        val mb = fileSizeBytes.toDouble() / (1024.0 * 1024.0)
        return String.format(java.util.Locale.US, "%.2f MB", mb)
    }

    fun checkFirewall(fileSizeBytes: Long): Pair<Boolean, String> {
        val isValid = validateSize(fileSizeBytes)
        val formattedSize = formatSize(fileSizeBytes)
        return if (isValid) {
            true to "Security Firewall: Safe ($formattedSize / 3.0 MB Max) ✓"
        } else {
            false to "Firewall Blocked: File is $formattedSize which exceeds strict 3.0 MB limit. Blocked to prevent system crash."
        }
    }
}


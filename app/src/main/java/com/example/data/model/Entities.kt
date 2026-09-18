package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val uid: String,
    val phoneNumber: String,
    val email: String,
    val name: String,
    val profilePicture: String,
    val role: UserRole = UserRole.BUYER,
    val isProVip: Boolean = false,
    val isVerified: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "sellers")
data class SellerProfileEntity(
    @PrimaryKey
    val uid: String,
    val businessName: String,
    val faceScanVerified: Boolean = false,
    val itemsSoldCount: Int = 0,
    val sellerLevel: Int = 1,
    val totalEarnings: Double = 0.0,
    val commissionPaid: Double = 0.0,
    val rating: Double = 5.0,
    val approvalStatus: SellerApprovalStatus = SellerApprovalStatus.APPROVED,
    val idDocumentType: String = "CNIC / National ID",
    val verifiedAt: Long = System.currentTimeMillis(),
    val bankName: String = "",
    val bankAccountTitle: String = "",
    val bankAccountNumber: String = "",
    val bankIban: String = "",
    val easypaisaNumber: String = "",
    val easypaisaTitle: String = "",
    val payoutSchedule: String = "Every Friday (جمعہ کے روز)",
    val returnWindowDays: Int = 2
)

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey
    val productId: String,
    val sellerId: String,
    val sellerName: String,
    val title: String,
    val description: String,
    val price: Double,
    val category: String,
    val imageUrl: String,
    val isProVip: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val isAvailable: Boolean = true,
    val isFreeDelivery: Boolean = true,
    val deliveryFee: Double = 0.0
)

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey
    val orderId: String,
    val buyerId: String,
    val buyerPhone: String,
    val sellerId: String,
    val sellerName: String,
    val productId: String,
    val productTitle: String,
    val orderAmount: Double,
    val commissionAmount: Double,
    val commissionRatePercent: Double,
    val paymentMethod: String = "Easypaisa",
    val paymentRef: String = "",
    val deliveryStatus: DeliveryStatus = DeliveryStatus.PENDING,
    val otpCode: String = "1234",
    val otpVerified: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val deliveredAt: Long? = null,
    val returnReason: String = "",
    val returnProofUrl: String = "",
    val returnStatus: String = "NONE", // NONE, REQUESTED, ACCEPTED, REJECTED
    val isFridayCleared: Boolean = false,
    val payoutClearedAt: Long? = null
)

data class TierCommissionBreakdown(
    val tier1Amount: Double = 0.0, // ≤ 10,000 (10%)
    val tier2Amount: Double = 0.0, // 10,001 - 20,000 (8%)
    val tier3Amount: Double = 0.0, // 20,001 - 35,000 (7%)
    val tier4Amount: Double = 0.0, // > 35,000 (5%)
    val totalCommissionRevenue: Double = 0.0,
    val totalOrderVolume: Double = 0.0,
    val totalOrdersCount: Int = 0
)

data class AdminOverviewStats(
    val totalLiveUsers: Int = 0,
    val totalBuyers: Int = 0,
    val totalSellers: Int = 0,
    val activeProVipCount: Int = 0,
    val proVipRevenue: Double = 0.0, // activeProVipCount * 5000.0
    val commissionBreakdown: TierCommissionBreakdown = TierCommissionBreakdown(),
    val pendingSellerApprovalsCount: Int = 0
)

data class PendingProductItem(
    val id: String = "",
    val productTitle: String = "",
    val basePrice: Double = 0.0,
    val category: String = "General",
    val status: String = "pending",
    val sellerId: String = "",
    val sellerName: String = "",
    val imageUrl: String = ""
)

data class SupportTicketItem(
    val id: String = "",
    val subject: String = "",
    val userEmail: String = "",
    val userRole: String = "buyer",
    val message: String = "",
    val status: String = "open",
    val priority: String = "medium", // "high", "medium", "low"
    val createdAt: Long = System.currentTimeMillis(),
    val senderName: String = "",
    val senderPhone: String = "",
    val screenshotUrl: String? = null
)

fun getTicketPriorityWeight(priority: String): Int = when (priority.lowercase().trim()) {
    "high" -> 3
    "medium" -> 2
    "low" -> 1
    else -> 2
}

/**
 * Official Platform Escrow & Settlement Bank Account
 */
object DkkOfficialBankAccount {
    const val ACCOUNT_TITLE = "DARO KHAN"
    const val BANK_NAME = "JS Bank"
    const val ACCOUNT_NUMBER = "0003015314"
    const val IBAN = "PK18JSBL9620000003015314"
    const val CURRENCY = "PKR"
}

/**
 * Official Platform Easypaisa Account
 */
object DkkOfficialEasypaisaAccount {
    const val ACCOUNT_TITLE = "DARO KHAN"
    const val ACCOUNT_NUMBER = "03273856001"
    const val PROVIDER = "Easypaisa"
    const val WALLET_TYPE = "Mobile Wallet & Raast"
}

/**
 * Direct Firebase Storage Receipts Vault Record (receipts_vault/)
 * Protected by strict 3.0 MB Security Firewall
 */
data class ReceiptVaultItem(
    val id: String = "",
    val folderPath: String = "receipts_vault/",
    val title: String = "",
    val senderName: String = "",
    val senderPhone: String = "",
    val senderRole: String = "buyer", // buyer, seller
    val slipType: String = "vip_payment", // vip_payment, escrow_payment, return_defect, dispute
    val referenceId: String = "",
    val amount: Double = 0.0,
    val fileSizeBytes: Long = 0L,
    val imageUrl: String = "",
    val status: String = "pending_review", // pending_review, verified, rejected
    val uploadedAt: Long = System.currentTimeMillis(),
    val firewallPassed: Boolean = true,
    val firewallNote: String = "3.0 MB Security Firewall: Safe ✓",
    val reviewNotes: String = ""
) {
    val fileSizeFormatted: String
        get() {
            val mb = fileSizeBytes.toDouble() / (1024.0 * 1024.0)
            return String.format(java.util.Locale.US, "%.2f MB", mb)
        }
}

fun resolveProductImageModel(imageUrl: String): Any {
    if (imageUrl.contains("img_ladies_watch") || (imageUrl.contains("watch") && (imageUrl.contains("ladies") || imageUrl.contains("universe") || imageUrl.contains("arrival")))) {
        return com.example.R.drawable.img_ladies_watch_1789383289787
    }
    if (imageUrl.startsWith("drawable/")) {
        return com.example.R.drawable.img_ladies_watch_1789383289787
    }
    return imageUrl.ifBlank { "https://images.unsplash.com/photo-1522335789203-aabd1fc54bc9?w=600&auto=format&fit=crop&q=80" }
}



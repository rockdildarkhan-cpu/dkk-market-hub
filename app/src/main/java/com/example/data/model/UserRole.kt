package com.example.data.model

enum class UserRole(val displayName: String, val badgeColorHex: Long) {
    BUYER("Buyer", 0xFF0284C7),
    SELLER("Seller", 0xFF059669),
    OWNER("Owner / Admin", 0xFFD97706)
}

enum class DeliveryStatus(val label: String, val colorHex: Long) {
    PENDING("Pending Dispatch", 0xFFF59E0B),
    DISPATCHED("Dispatched", 0xFF3B82F6),
    DELIVERED("Delivered", 0xFF10B981),
    RETURN_REQUESTED("Return Requested (2-Day Window)", 0xFFF97316),
    RETURNED("Returned & Refunded", 0xFF8B5CF6),
    CANCELLED("Cancelled", 0xFFEF4444)
}

enum class SellerApprovalStatus {
    PENDING_VERIFICATION,
    PENDING_APPROVAL,
    APPROVED,
    REJECTED
}

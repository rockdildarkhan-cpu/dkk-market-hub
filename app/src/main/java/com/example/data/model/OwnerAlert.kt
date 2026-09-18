package com.example.data.model

enum class OwnerAlertType {
    HIGH_VALUE_PRODUCT,
    URGENT_TICKET,
    VIP_PAYMENT,
    RECEIPT_VAULT
}

data class OwnerAlertNotification(
    val id: String,
    val title: String,
    val message: String,
    val type: OwnerAlertType,
    val timestamp: Long = System.currentTimeMillis(),
    val referenceId: String = "",
    val price: Double? = null,
    val senderInfo: String? = null,
    val isRead: Boolean = false,
    val priority: String = "CRITICAL"
)

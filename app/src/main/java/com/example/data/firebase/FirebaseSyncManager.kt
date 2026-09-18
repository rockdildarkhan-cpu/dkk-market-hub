package com.example.data.firebase

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.util.Log
import com.example.data.model.DeliveryStatus
import com.example.data.model.OrderEntity
import com.example.data.model.OwnerAlertNotification
import com.example.data.model.OwnerAlertType
import com.example.data.model.PendingProductItem
import com.example.data.model.ProductEntity
import com.example.data.model.ReceiptVaultItem
import com.example.data.model.SellerApprovalStatus
import com.example.data.model.SellerProfileEntity
import com.example.data.model.SupportTicketItem
import com.example.data.model.getTicketPriorityWeight
import com.example.data.model.UserEntity
import com.example.data.model.UserRole
import com.example.data.security.ReceiptsVaultSecurityGuard
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

data class FirebaseConnectionState(
    val isConfigured: Boolean = false,
    val isOnline: Boolean = false,
    val isAuthenticated: Boolean = false,
    val userEmail: String? = null,
    val lastSyncTimestamp: Long = 0L,
    val syncedProductsCount: Int = 0,
    val syncedOrdersCount: Int = 0,
    val statusSummary: String = "Checking Firebase connection...",
    val projectId: String = "dkk-marketing",
    val projectNumber: String = "509719983571",
    val appId: String = "1:509719983571:android:f5e58df6f2d0d697e62b26",
    val packageName: String = "com.dkk.marketing",
    val sha1: String = "9b:bb:31:86:b2:18:9e:f1:6d:8f:27:2b:da:b5:93:62:c3:95:cb:b4",
    val sha256: String = "07:b9:2e:02:75:d5:e2:f3:b3:51:ef:6c:44:c5:a6:74:c3:6b:e6:f4:36:9f:01:e0:13:34:af:a3:7b:6d:6e:ef"
)

class FirebaseSyncManager(private val context: Context) {

    private val tag = "FirebaseSyncManager"

    private val _connectionState = MutableStateFlow(FirebaseConnectionState())
    val connectionState: StateFlow<FirebaseConnectionState> = _connectionState.asStateFlow()

    private var firestoreInstance: FirebaseFirestore? = null
    private var authInstance: FirebaseAuth? = null

    // 📊 Live Stats States (Matching React Native AdminHomeScreen)
    private val _pendingSellersCount = MutableStateFlow(2)
    val pendingSellersCount: StateFlow<Int> = _pendingSellersCount.asStateFlow()

    private val _pendingProductsCount = MutableStateFlow(4)
    val pendingProductsCount: StateFlow<Int> = _pendingProductsCount.asStateFlow()

    private val _activeTicketsCount = MutableStateFlow(3)
    val activeTicketsCount: StateFlow<Int> = _activeTicketsCount.asStateFlow()

    private val _totalPlatformEarnings = MutableStateFlow(14250.0)
    val totalPlatformEarnings: StateFlow<Double> = _totalPlatformEarnings.asStateFlow()

    private val _pendingProducts = MutableStateFlow<List<PendingProductItem>>(
        listOf(
            PendingProductItem(
                id = "prod_review_01",
                productTitle = "Wholesale Leather Jacket Premium",
                basePrice = 8500.0,
                category = "Fashion",
                status = "pending",
                sellerId = "seller_901",
                sellerName = "Royal Apparel Mart",
                imageUrl = "https://images.unsplash.com/photo-1548036328-c9fa89d128fa?w=600&auto=format&fit=crop&q=80"
            ),
            PendingProductItem(
                id = "prod_review_02",
                productTitle = "Solar Power Inverter Hybrid 3.2kW",
                basePrice = 45000.0,
                category = "Electronics",
                status = "pending",
                sellerId = "seller_902",
                sellerName = "VoltZone Tech",
                imageUrl = "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=600&auto=format&fit=crop&q=80"
            ),
            PendingProductItem(
                id = "prod_review_03",
                productTitle = "Handmade Persian Pattern Rug 6x9",
                basePrice = 18500.0,
                category = "Home",
                status = "pending",
                sellerId = "seller_903",
                sellerName = "Heritage Living",
                imageUrl = "https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=600&auto=format&fit=crop&q=80"
            ),
            PendingProductItem(
                id = "prod_review_04",
                productTitle = "Wireless Noise-Cancelling ANC Earbuds",
                basePrice = 6200.0,
                category = "Electronics",
                status = "pending",
                sellerId = "seller_902",
                sellerName = "VoltZone Tech",
                imageUrl = "https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=600&auto=format&fit=crop&q=80"
            )
        )
    )
    val pendingProducts: StateFlow<List<PendingProductItem>> = _pendingProducts.asStateFlow()

    fun sortTicketsByPriority(tickets: List<SupportTicketItem>): List<SupportTicketItem> {
        return tickets.sortedWith(
            compareByDescending<SupportTicketItem> { getTicketPriorityWeight(it.priority) }
                .thenByDescending { it.createdAt }
        )
    }

    private val _activeTickets = MutableStateFlow<List<SupportTicketItem>>(
        sortTicketsByPriority(
            listOf(
                SupportTicketItem(
                    id = "ticket_102",
                    subject = "🚨 Payment Deducted Rider Not Assigned",
                    userEmail = "tariq.buyer@gmail.com",
                    userRole = "buyer",
                    message = "Easypaisa payment Rs 14,500 done, rider OTP issue. Urgent attention required!",
                    status = "open",
                    priority = "high",
                    createdAt = System.currentTimeMillis() - 1000 * 60 * 15
                ),
                SupportTicketItem(
                    id = "ticket_101",
                    subject = "Delayed Delivery Inquiry",
                    userEmail = "kamran.buyer@gmail.com",
                    userRole = "buyer",
                    message = "Rider abhi tak nahi phoncha, Easypaisa payment ho chuki hai. Please check OTP status.",
                    status = "open",
                    priority = "medium",
                    createdAt = System.currentTimeMillis() - 1000 * 60 * 60
                ),
                SupportTicketItem(
                    id = "ticket_103",
                    subject = "Product Specification Query",
                    userEmail = "bilal.buyer@gmail.com",
                    userRole = "buyer",
                    message = "Wanted to confirm if the leather jacket has internal zipper pocket.",
                    status = "open",
                    priority = "low",
                    createdAt = System.currentTimeMillis() - 1000 * 60 * 180
                )
            )
        )
    )
    val activeTickets: StateFlow<List<SupportTicketItem>> = _activeTickets.asStateFlow()

    // 📦 Firebase Storage Receipts Vault State (receipts_vault/) protected by 3.0 MB Security Firewall
    private val _receiptsVaultItems = MutableStateFlow<List<ReceiptVaultItem>>(
        listOf(
            ReceiptVaultItem(
                id = "vault_slip_001",
                folderPath = ReceiptsVaultSecurityGuard.VAULT_FOLDER,
                title = "VIP Pro Membership Fee Slip (Rs 5,000)",
                senderName = "Raza Premium Garments",
                senderPhone = "03459876543",
                senderRole = "seller",
                slipType = "vip_payment",
                referenceId = "VIP-UPGRADE-5000",
                amount = 5000.0,
                fileSizeBytes = 1_450_000L, // 1.45 MB
                imageUrl = "https://images.unsplash.com/photo-1554224155-8d04cb21cd6c?w=700&auto=format&fit=crop&q=80",
                status = "pending_review",
                uploadedAt = System.currentTimeMillis() - 1000 * 60 * 45,
                firewallPassed = true,
                firewallNote = "3.0 MB Security Firewall: Passed (1.45 MB) ✓"
            ),
            ReceiptVaultItem(
                id = "vault_slip_002",
                folderPath = ReceiptsVaultSecurityGuard.VAULT_FOLDER,
                title = "Order #ORD-782 Defect Evidence (2-Day Window Return)",
                senderName = "Tariq Mehmood",
                senderPhone = "03214567890",
                senderRole = "buyer",
                slipType = "return_defect",
                referenceId = "ORD-782",
                amount = 8500.0,
                fileSizeBytes = 2_150_000L, // 2.15 MB
                imageUrl = "https://images.unsplash.com/photo-1578932750294-f5075e85f44a?w=700&auto=format&fit=crop&q=80",
                status = "pending_review",
                uploadedAt = System.currentTimeMillis() - 1000 * 60 * 90,
                firewallPassed = true,
                firewallNote = "3.0 MB Security Firewall: Passed (2.15 MB) ✓"
            ),
            ReceiptVaultItem(
                id = "vault_slip_003",
                folderPath = ReceiptsVaultSecurityGuard.VAULT_FOLDER,
                title = "JS Bank Escrow Transfer Slip",
                senderName = "Kamran Ali",
                senderPhone = "03001234567",
                senderRole = "buyer",
                slipType = "escrow_payment",
                referenceId = "ORD-904",
                amount = 45000.0,
                fileSizeBytes = 890_000L, // 0.89 MB
                imageUrl = "https://images.unsplash.com/photo-1554224154-26032ffc0d07?w=700&auto=format&fit=crop&q=80",
                status = "verified",
                uploadedAt = System.currentTimeMillis() - 1000 * 60 * 240,
                firewallPassed = true,
                firewallNote = "3.0 MB Security Firewall: Passed (0.89 MB) ✓",
                reviewNotes = "Verified by Daro Khan ✓ Bank transaction confirmed"
            )
        )
    )
    val receiptsVaultItems: StateFlow<List<ReceiptVaultItem>> = _receiptsVaultItems.asStateFlow()

    /**
     * Uploads screenshot to Firebase Storage folder `receipts_vault/`
     * Enforces strict 3.0 MB Security Firewall Check to prevent system crash.
     */
    fun uploadReceiptToVault(
        title: String,
        senderName: String,
        senderPhone: String,
        senderRole: String,
        slipType: String,
        referenceId: String,
        amount: Double,
        fileSizeBytes: Long,
        imageUrl: String
    ): Result<ReceiptVaultItem> {
        val (passed, message) = ReceiptsVaultSecurityGuard.checkFirewall(fileSizeBytes)
        if (!passed) {
            Log.e(tag, "Firewall blocked upload to receipts_vault/: $message")
            return Result.failure(IllegalArgumentException(message))
        }

        val newItem = ReceiptVaultItem(
            id = "vault_slip_${System.currentTimeMillis()}",
            folderPath = ReceiptsVaultSecurityGuard.VAULT_FOLDER,
            title = title,
            senderName = senderName,
            senderPhone = senderPhone,
            senderRole = senderRole,
            slipType = slipType,
            referenceId = referenceId,
            amount = amount,
            fileSizeBytes = fileSizeBytes,
            imageUrl = imageUrl.ifBlank { "https://images.unsplash.com/photo-1554224155-8d04cb21cd6c?w=700&auto=format&fit=crop&q=80" },
            status = "pending_review",
            uploadedAt = System.currentTimeMillis(),
            firewallPassed = true,
            firewallNote = message
        )

        _receiptsVaultItems.value = listOf(newItem) + _receiptsVaultItems.value

        // Dispatch live notification to Daro Khan's Admin Desk
        dispatchOwnerAlert(
            OwnerAlertNotification(
                id = "alert_vault_${newItem.id}",
                title = "📦 New Receipt in receipts_vault/",
                message = "${newItem.title} from ${newItem.senderName} (${newItem.fileSizeFormatted} - Firewall Passed ✓)",
                type = OwnerAlertType.VIP_PAYMENT,
                timestamp = System.currentTimeMillis(),
                referenceId = newItem.id
            )
        )

        return Result.success(newItem)
    }

    fun verifyReceiptSlip(slipId: String) {
        _receiptsVaultItems.value = _receiptsVaultItems.value.map { item ->
            if (item.id == slipId) {
                item.copy(status = "verified", reviewNotes = "Verified by Daro Khan on 'Everything Clear' Desk ✓")
            } else item
        }
    }

    fun rejectReceiptSlip(slipId: String) {
        _receiptsVaultItems.value = _receiptsVaultItems.value.map { item ->
            if (item.id == slipId) {
                item.copy(status = "rejected", reviewNotes = "Declined by Owner: Invalid slip or unreadable")
            } else item
        }
    }

    // 🔔 Real-Time Owner Notification Listener State (Matching AdminHomeScreen Real-Time Engine)
    private val _ownerAlerts = MutableStateFlow<List<OwnerAlertNotification>>(emptyList())
    val ownerAlerts: StateFlow<List<OwnerAlertNotification>> = _ownerAlerts.asStateFlow()

    private val _latestAlert = MutableStateFlow<OwnerAlertNotification?>(null)
    val latestAlert: StateFlow<OwnerAlertNotification?> = _latestAlert.asStateFlow()

    private val seenProductAlertIds = mutableSetOf<String>()
    private val seenTicketAlertIds = mutableSetOf<String>()

    fun dispatchOwnerAlert(alert: OwnerAlertNotification) {
        _ownerAlerts.value = listOf(alert) + _ownerAlerts.value.filter { it.id != alert.id }.take(30)
        _latestAlert.value = alert
        playNotificationChime()
    }

    fun dismissLatestAlert() {
        _latestAlert.value = null
    }

    fun dismissAlert(alertId: String) {
        _ownerAlerts.value = _ownerAlerts.value.filter { it.id != alertId }
        if (_latestAlert.value?.id == alertId) {
            _latestAlert.value = null
        }
    }

    fun clearAllAlerts() {
        _ownerAlerts.value = emptyList()
        _latestAlert.value = null
    }

    private fun playNotificationChime() {
        try {
            val tone = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100)
            tone.startTone(ToneGenerator.TONE_PROP_BEEP2, 350)
        } catch (e: Exception) {
            Log.w(tag, "Could not play alert chime: ${e.message}")
        }
    }

    fun isUrgentTicket(subject: String, message: String): Boolean {
        val urgentKeywords = listOf(
            "urgent", "emergency", "fraud", "scam", "dhoka", "fake",
            "delayed", "delay", "refund", "rider", "otp", "stolen",
            "police", "help", "lost", "missing", "complaint", "cheat"
        )
        val combined = "$subject $message".lowercase()
        return urgentKeywords.any { combined.contains(it) }
    }

    fun checkAndAlertProduct(product: PendingProductItem) {
        if (product.basePrice >= 20000.0 && !seenProductAlertIds.contains(product.id)) {
            seenProductAlertIds.add(product.id)
            val alert = OwnerAlertNotification(
                id = "alert_prod_${product.id}",
                title = "💎 High-Value Product Submitted",
                message = "\"${product.productTitle}\" submitted by ${product.sellerName.ifBlank { "DKK Seller" }} for PKR ${product.basePrice.toInt()}. Immediate quality approval required.",
                type = OwnerAlertType.HIGH_VALUE_PRODUCT,
                timestamp = System.currentTimeMillis(),
                referenceId = product.id,
                price = product.basePrice,
                senderInfo = product.sellerName
            )
            dispatchOwnerAlert(alert)
        }
    }

    fun checkAndAlertTicket(ticket: SupportTicketItem) {
        val isUrgent = ticket.priority.equals("high", ignoreCase = true) || isUrgentTicket(ticket.subject, ticket.message)
        if (isUrgent && !seenTicketAlertIds.contains(ticket.id)) {
            seenTicketAlertIds.add(ticket.id)
            val alert = OwnerAlertNotification(
                id = "alert_ticket_${ticket.id}",
                title = "🚨 Urgent [HIGH Priority] Support Ticket Opened",
                message = "[${ticket.subject}] (${ticket.userRole.uppercase()} - ${ticket.userEmail}): ${ticket.message.take(130)}",
                type = OwnerAlertType.URGENT_TICKET,
                timestamp = System.currentTimeMillis(),
                referenceId = ticket.id,
                senderInfo = "${ticket.userEmail} (Priority: ${ticket.priority.uppercase()})"
            )
            dispatchOwnerAlert(alert)
        }
    }

    fun simulateHighValueProductAlert(
        title: String = "Sony Alpha A7 IV 4K Mirrorless Camera",
        price: Double = 385000.0,
        sellerName: String = "Apex Tech Traders"
    ) {
        val id = "prod_sim_${System.currentTimeMillis()}"
        val item = PendingProductItem(
            id = id,
            productTitle = title,
            basePrice = price,
            category = "Electronics",
            status = "pending",
            sellerId = "seller_sim_01",
            sellerName = sellerName,
            imageUrl = "https://images.unsplash.com/photo-1516035069371-29a1b244cc32?w=600&auto=format&fit=crop&q=80"
        )
        _pendingProducts.value = listOf(item) + _pendingProducts.value
        _pendingProductsCount.value = _pendingProducts.value.size
        checkAndAlertProduct(item)
    }

    fun simulateUrgentTicketAlert(
        subject: String = "URGENT: Easypaisa payment debited but OTP rider missing",
        userEmail: String = "tariq.buyer@gmail.com",
        message: String = "Fraud emergency! Paid PKR 45,000 via Easypaisa, rider cancelled without OTP delivery. Please block seller immediately!",
        priority: String = "high"
    ) {
        val id = "ticket_sim_${System.currentTimeMillis()}"
        val item = SupportTicketItem(
            id = id,
            subject = subject,
            userEmail = userEmail,
            userRole = "buyer",
            message = message,
            status = "open",
            priority = priority
        )
        val updated = sortTicketsByPriority(listOf(item) + _activeTickets.value.filter { it.id != item.id })
        _activeTickets.value = updated
        _activeTicketsCount.value = updated.size
        checkAndAlertTicket(item)
    }

    private val _isLoadingLiveStats = MutableStateFlow(false)
    val isLoadingLiveStats: StateFlow<Boolean> = _isLoadingLiveStats.asStateFlow()

    private var sellersListener: ListenerRegistration? = null
    private var productsListener: ListenerRegistration? = null
    private var ticketsListener: ListenerRegistration? = null
    private var ordersListener: ListenerRegistration? = null

    init {
        checkAndInitialize()
    }

    fun checkAndInitialize() {
        try {
            val app = try {
                FirebaseApp.getInstance()
            } catch (e: IllegalStateException) {
                FirebaseApp.initializeApp(context)
            }

            if (app != null) {
                firestoreInstance = FirebaseFirestore.getInstance()
                authInstance = FirebaseAuth.getInstance()

                val currentUser = authInstance?.currentUser
                _connectionState.value = _connectionState.value.copy(
                    isConfigured = true,
                    isOnline = true,
                    isAuthenticated = currentUser != null,
                    userEmail = currentUser?.email ?: currentUser?.uid,
                    statusSummary = "Firebase Connected (Cloud Firestore & Auth Online)"
                )
                Log.d(tag, "Firebase initialized successfully")
                attachLiveListeners()
            } else {
                _connectionState.value = _connectionState.value.copy(
                    isConfigured = false,
                    isOnline = false,
                    statusSummary = "Firebase Ready (Offline-first Mode. google-services.json pending)"
                )
                Log.d(tag, "FirebaseApp is null, local-first mode")
            }
        } catch (e: Exception) {
            Log.e(tag, "Firebase initialization error: ${e.message}")
            _connectionState.value = _connectionState.value.copy(
                isConfigured = false,
                isOnline = false,
                statusSummary = "Local-first Mode (Firebase Standby)"
            )
        }
    }

    fun attachLiveListeners() {
        val firestore = firestoreInstance ?: return
        try {
            _isLoadingLiveStats.value = true

            // 1. Live listener for Pending Sellers (Face Scan / Full Verification)
            sellersListener?.remove()
            sellersListener = firestore.collection("users")
                .whereEqualTo("role", "seller")
                .whereEqualTo("status", "pending_verification")
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        Log.w(tag, "Sellers listener error: ${e.message}")
                        return@addSnapshotListener
                    }
                    val count = snapshot?.size() ?: 0
                    _pendingSellersCount.value = count
                }

            // 2. Live listener for Pending Products (Admin Quality Moderation)
            productsListener?.remove()
            productsListener = firestore.collection("products")
                .whereEqualTo("status", "pending")
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        Log.w(tag, "Products listener error: ${e.message}")
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        _pendingProductsCount.value = snapshot.size()
                        val prodList = snapshot.documents.map { doc ->
                            PendingProductItem(
                                id = doc.id,
                                productTitle = doc.getString("productTitle") ?: doc.getString("title") ?: "Untitled Product",
                                basePrice = doc.getDouble("basePrice") ?: doc.getDouble("price") ?: 0.0,
                                category = doc.getString("category") ?: "General",
                                status = "pending",
                                sellerId = doc.getString("sellerId") ?: "",
                                sellerName = doc.getString("sellerName") ?: "DKK Seller",
                                imageUrl = doc.getString("imageUrl") ?: ""
                            )
                        }
                        _pendingProducts.value = prodList
                        prodList.forEach { checkAndAlertProduct(it) }
                    }
                }

            // 3. Live listener for Complaints / Support Tickets
            ticketsListener?.remove()
            ticketsListener = firestore.collection("support_tickets")
                .whereEqualTo("status", "open")
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        Log.w(tag, "Tickets listener error: ${e.message}")
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        _activeTicketsCount.value = snapshot.size()
                        val ticketList = snapshot.documents.map { doc ->
                            val subject = doc.getString("subject") ?: "Support Ticket"
                            val message = doc.getString("message") ?: ""
                            val rawPriority = doc.getString("priority")?.lowercase()?.trim()
                            val priority = when (rawPriority) {
                                "high", "medium", "low" -> rawPriority
                                else -> if (isUrgentTicket(subject, message)) "high" else "medium"
                            }
                            SupportTicketItem(
                                id = doc.id,
                                subject = subject,
                                userEmail = doc.getString("userEmail") ?: "Anonymous",
                                userRole = doc.getString("userRole") ?: "user",
                                message = message,
                                status = doc.getString("status") ?: "open",
                                priority = priority,
                                createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                            )
                        }
                        val sortedTickets = sortTicketsByPriority(ticketList)
                        _activeTickets.value = sortedTickets
                        sortedTickets.forEach { checkAndAlertTicket(it) }
                    }
                }

            // 4. Live listener for Global Commission Ledger Tracking (10%, 8%, 7% Earnings)
            ordersListener?.remove()
            ordersListener = firestore.collection("orders")
                .addSnapshotListener { snapshot, e ->
                    _isLoadingLiveStats.value = false
                    if (e != null) {
                        Log.w(tag, "Orders listener error: ${e.message}")
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        var totalCommission = 0.0
                        for (doc in snapshot.documents) {
                            val comm = doc.getDouble("dkkCommissionEarned")
                                ?: doc.getDouble("commissionAmount")
                                ?: 0.0
                            totalCommission += comm
                        }
                        _totalPlatformEarnings.value = totalCommission
                    }
                }
        } catch (e: Exception) {
            Log.e(tag, "attachLiveListeners error: ${e.message}")
            _isLoadingLiveStats.value = false
        }
    }

    suspend fun approveProduct(productId: String): Result<Unit> = withContext(Dispatchers.IO) {
        // Optimistically update local list so the UI responds immediately with zero lag
        _pendingProducts.value = _pendingProducts.value.filter { it.id != productId }
        _pendingProductsCount.value = _pendingProducts.value.size

        val firestore = firestoreInstance ?: return@withContext Result.success(Unit)
        try {
            suspendCancellableCoroutine<Void?> { cont ->
                firestore.collection("products")
                    .document(productId)
                    .update("status", "approved")
                    .addOnSuccessListener { cont.resume(it) }
                    .addOnFailureListener { cont.resumeWithException(it) }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(tag, "Approve product firestore error: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun rejectProduct(productId: String): Result<Unit> = withContext(Dispatchers.IO) {
        // Optimistically remove from pending list so UI responds immediately
        _pendingProducts.value = _pendingProducts.value.filter { it.id != productId }
        _pendingProductsCount.value = _pendingProducts.value.size

        val firestore = firestoreInstance ?: return@withContext Result.success(Unit)
        try {
            suspendCancellableCoroutine<Void?> { cont ->
                firestore.collection("products")
                    .document(productId)
                    .update("status", "rejected")
                    .addOnSuccessListener { cont.resume(it) }
                    .addOnFailureListener { cont.resumeWithException(it) }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(tag, "Reject product firestore error: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun submitSupportTicket(
        subject: String,
        userEmail: String,
        userRole: String,
        message: String,
        priority: String = "medium",
        senderName: String = "",
        senderPhone: String = "",
        screenshotUrl: String? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        val resolvedPriority = when (priority.lowercase().trim()) {
            "high" -> "high"
            "low" -> "low"
            "medium" -> "medium"
            else -> if (isUrgentTicket(subject, message)) "high" else "medium"
        }
        val ticketId = "ticket_" + System.currentTimeMillis()
        val newTicket = SupportTicketItem(
            id = ticketId,
            subject = subject,
            userEmail = userEmail,
            userRole = userRole,
            message = message,
            status = "open",
            priority = resolvedPriority,
            createdAt = System.currentTimeMillis(),
            senderName = senderName,
            senderPhone = senderPhone,
            screenshotUrl = screenshotUrl
        )
        val updatedTickets = sortTicketsByPriority(listOf(newTicket) + _activeTickets.value.filter { it.id != newTicket.id })
        _activeTickets.value = updatedTickets
        _activeTicketsCount.value = updatedTickets.size
        checkAndAlertTicket(newTicket)

        val firestore = firestoreInstance ?: return@withContext Result.success(ticketId)
        try {
            val ticketMap = hashMapOf(
                "subject" to subject,
                "userEmail" to userEmail,
                "userRole" to userRole,
                "message" to message,
                "status" to "open",
                "priority" to resolvedPriority,
                "createdAt" to System.currentTimeMillis(),
                "senderName" to senderName,
                "senderPhone" to senderPhone,
                "screenshotUrl" to (screenshotUrl ?: "")
            )
            suspendCancellableCoroutine<Void?> { cont ->
                firestore.collection("support_tickets")
                    .document(ticketId)
                    .set(ticketMap)
                    .addOnSuccessListener { cont.resume(it) }
                    .addOnFailureListener { cont.resumeWithException(it) }
            }
            Result.success(ticketId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    val isFirebaseReady: Boolean
        get() = _connectionState.value.isConfigured && firestoreInstance != null

    suspend fun signInAnonymouslyOrUser(email: String? = null): Result<String> = withContext(Dispatchers.IO) {
        try {
            val auth = authInstance ?: return@withContext Result.failure(Exception("FirebaseAuth not initialized"))
            if (auth.currentUser != null) {
                val uid = auth.currentUser!!.uid
                _connectionState.value = _connectionState.value.copy(
                    isAuthenticated = true,
                    userEmail = auth.currentUser?.email ?: uid
                )
                return@withContext Result.success(uid)
            }

            val result = suspendCancellableCoroutine<FirebaseUser> { cont ->
                auth.signInAnonymously()
                    .addOnSuccessListener { authResult ->
                        val user = authResult.user
                        if (user != null) cont.resume(user)
                        else cont.resumeWithException(Exception("Empty Firebase user"))
                    }
                    .addOnFailureListener { e ->
                        cont.resumeWithException(e)
                    }
            }

            _connectionState.value = _connectionState.value.copy(
                isAuthenticated = true,
                userEmail = result.email ?: "Anonymous Cloud User"
            )
            Result.success(result.uid)
        } catch (e: Exception) {
            Log.e(tag, "Auth error: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun uploadProduct(product: ProductEntity): Result<Unit> = withContext(Dispatchers.IO) {
        val pendingItem = PendingProductItem(
            id = product.productId,
            productTitle = product.title,
            basePrice = product.price,
            category = product.category,
            status = "pending",
            sellerId = product.sellerId,
            sellerName = product.sellerName,
            imageUrl = product.imageUrl
        )
        _pendingProducts.value = listOf(pendingItem) + _pendingProducts.value.filter { it.id != pendingItem.id }
        _pendingProductsCount.value = _pendingProducts.value.size
        checkAndAlertProduct(pendingItem)

        val firestore = firestoreInstance ?: return@withContext Result.success(Unit)
        try {
            val productMap = hashMapOf(
                "productId" to product.productId,
                "sellerId" to product.sellerId,
                "sellerName" to product.sellerName,
                "title" to product.title,
                "productTitle" to product.title,
                "description" to product.description,
                "price" to product.price,
                "basePrice" to product.price,
                "category" to product.category,
                "imageUrl" to product.imageUrl,
                "isProVip" to product.isProVip,
                "isFreeDelivery" to product.isFreeDelivery,
                "deliveryFee" to product.deliveryFee,
                "status" to "pending",
                "createdAt" to product.createdAt,
                "isAvailable" to product.isAvailable
            )

            suspendCancellableCoroutine<Void?> { cont ->
                firestore.collection("products")
                    .document(product.productId)
                    .set(productMap, SetOptions.merge())
                    .addOnSuccessListener { cont.resume(it) }
                    .addOnFailureListener { cont.resumeWithException(it) }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadOrder(order: OrderEntity): Result<Unit> = withContext(Dispatchers.IO) {
        val firestore = firestoreInstance ?: return@withContext Result.failure(Exception("Firestore not available"))
        try {
            val orderMap = hashMapOf(
                "orderId" to order.orderId,
                "buyerId" to order.buyerId,
                "buyerPhone" to order.buyerPhone,
                "sellerId" to order.sellerId,
                "sellerName" to order.sellerName,
                "productId" to order.productId,
                "productTitle" to order.productTitle,
                "orderAmount" to order.orderAmount,
                "commissionAmount" to order.commissionAmount,
                "dkkCommissionEarned" to order.commissionAmount,
                "commissionRatePercent" to order.commissionRatePercent,
                "paymentMethod" to order.paymentMethod,
                "paymentRef" to order.paymentRef,
                "deliveryStatus" to order.deliveryStatus.name,
                "otpCode" to order.otpCode,
                "otpVerified" to order.otpVerified,
                "createdAt" to order.createdAt
            )

            suspendCancellableCoroutine<Void?> { cont ->
                firestore.collection("orders")
                    .document(order.orderId)
                    .set(orderMap, SetOptions.merge())
                    .addOnSuccessListener { cont.resume(it) }
                    .addOnFailureListener { cont.resumeWithException(it) }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadUser(user: UserEntity): Result<Unit> = withContext(Dispatchers.IO) {
        val firestore = firestoreInstance ?: return@withContext Result.failure(Exception("Firestore not available"))
        try {
            val userMap = hashMapOf(
                "uid" to user.uid,
                "phoneNumber" to user.phoneNumber,
                "email" to user.email,
                "name" to user.name,
                "profilePicture" to user.profilePicture,
                "role" to user.role.name,
                "isProVip" to user.isProVip,
                "isVerified" to user.isVerified,
                "createdAt" to user.createdAt
            )

            suspendCancellableCoroutine<Void?> { cont ->
                firestore.collection("users")
                    .document(user.uid)
                    .set(userMap, SetOptions.merge())
                    .addOnSuccessListener { cont.resume(it) }
                    .addOnFailureListener { cont.resumeWithException(it) }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadSeller(seller: SellerProfileEntity): Result<Unit> = withContext(Dispatchers.IO) {
        val firestore = firestoreInstance ?: return@withContext Result.failure(Exception("Firestore not available"))
        try {
            val sellerMap = hashMapOf(
                "uid" to seller.uid,
                "businessName" to seller.businessName,
                "faceScanVerified" to seller.faceScanVerified,
                "itemsSoldCount" to seller.itemsSoldCount,
                "sellerLevel" to seller.sellerLevel,
                "totalEarnings" to seller.totalEarnings,
                "commissionPaid" to seller.commissionPaid,
                "rating" to seller.rating,
                "approvalStatus" to seller.approvalStatus.name,
                "idDocumentType" to seller.idDocumentType,
                "verifiedAt" to seller.verifiedAt,
                "bankName" to seller.bankName,
                "bankAccountTitle" to seller.bankAccountTitle,
                "bankAccountNumber" to seller.bankAccountNumber,
                "bankIban" to seller.bankIban,
                "easypaisaNumber" to seller.easypaisaNumber,
                "easypaisaTitle" to seller.easypaisaTitle,
                "payoutSchedule" to seller.payoutSchedule,
                "returnWindowDays" to seller.returnWindowDays
            )

            suspendCancellableCoroutine<Void?> { cont ->
                firestore.collection("sellers")
                    .document(seller.uid)
                    .set(sellerMap, SetOptions.merge())
                    .addOnSuccessListener { cont.resume(it) }
                    .addOnFailureListener { cont.resumeWithException(it) }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchCloudProducts(): Result<List<ProductEntity>> = withContext(Dispatchers.IO) {
        val firestore = firestoreInstance ?: return@withContext Result.failure(Exception("Firestore not available"))
        try {
            val snapshot = suspendCancellableCoroutine { cont ->
                firestore.collection("products")
                    .get()
                    .addOnSuccessListener { cont.resume(it) }
                    .addOnFailureListener { cont.resumeWithException(it) }
            }

            val products = snapshot.documents.mapNotNull { doc ->
                try {
                    ProductEntity(
                        productId = doc.getString("productId") ?: doc.id,
                        sellerId = doc.getString("sellerId") ?: "",
                        sellerName = doc.getString("sellerName") ?: "DKK Seller",
                        title = doc.getString("title") ?: "",
                        description = doc.getString("description") ?: "",
                        price = doc.getDouble("price") ?: 0.0,
                        category = doc.getString("category") ?: "General",
                        imageUrl = doc.getString("imageUrl") ?: "",
                        isProVip = doc.getBoolean("isProVip") ?: false,
                        createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                        isAvailable = doc.getBoolean("isAvailable") ?: true,
                        isFreeDelivery = doc.getBoolean("isFreeDelivery") ?: true,
                        deliveryFee = doc.getDouble("deliveryFee") ?: 0.0
                    )
                } catch (e: Exception) {
                    null
                }
            }
            Result.success(products)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun syncAllLocalToCloud(
        products: List<ProductEntity>,
        orders: List<OrderEntity>,
        users: List<UserEntity>
    ): Result<String> = withContext(Dispatchers.IO) {
        if (!isFirebaseReady) {
            // Local fallback acknowledgment
            _connectionState.value = _connectionState.value.copy(
                lastSyncTimestamp = System.currentTimeMillis(),
                syncedProductsCount = products.size,
                syncedOrdersCount = orders.size,
                statusSummary = "Local Cache Updated (${products.size} Products, ${orders.size} Orders ready for Firebase Cloud)"
            )
            return@withContext Result.success("Offline storage synchronized. Connect Firebase to push to live Cloud.")
        }

        try {
            // Sign in if needed
            signInAnonymouslyOrUser()

            var pCount = 0
            var oCount = 0

            for (p in products) {
                uploadProduct(p)
                pCount++
            }
            for (o in orders) {
                uploadOrder(o)
                oCount++
            }
            for (u in users) {
                uploadUser(u)
            }

            _connectionState.value = _connectionState.value.copy(
                isOnline = true,
                lastSyncTimestamp = System.currentTimeMillis(),
                syncedProductsCount = pCount,
                syncedOrdersCount = oCount,
                statusSummary = "Synced $pCount Products & $oCount Orders with Firebase Cloud"
            )

            Result.success("Success: $pCount products and $oCount orders synced to Firebase Cloud!")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

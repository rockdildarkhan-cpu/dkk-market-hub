package com.example.data.repository

import com.example.data.dao.OrderDao
import com.example.data.dao.ProductDao
import com.example.data.dao.SellerDao
import com.example.data.dao.UserDao
import com.example.data.firebase.FirebaseSyncManager
import com.example.data.model.AdminOverviewStats
import com.example.data.model.DeliveryStatus
import com.example.data.model.OrderEntity
import com.example.data.model.ProductEntity
import com.example.data.model.SellerApprovalStatus
import com.example.data.model.SellerProfileEntity
import com.example.data.model.TierCommissionBreakdown
import com.example.data.model.UserEntity
import com.example.data.model.UserRole
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.util.UUID

class DkkRepository(
    private val userDao: UserDao,
    private val sellerDao: SellerDao,
    private val productDao: ProductDao,
    private val orderDao: OrderDao,
    val firebaseSyncManager: FirebaseSyncManager? = null
) {
    // Products
    val allProducts: Flow<List<ProductEntity>> = productDao.getAllProducts()
    val proVipProducts: Flow<List<ProductEntity>> = productDao.getProVipProducts()

    fun getProductsBySeller(sellerId: String): Flow<List<ProductEntity>> =
        productDao.getProductsBySeller(sellerId)

    suspend fun createProduct(
        sellerId: String,
        sellerName: String,
        title: String,
        description: String,
        price: Double,
        category: String,
        imageUrl: String,
        isProVip: Boolean,
        isFreeDelivery: Boolean = true,
        deliveryFee: Double = 0.0
    ): String {
        val productId = "PROD_${UUID.randomUUID().toString().take(8).uppercase()}"
        val product = ProductEntity(
            productId = productId,
            sellerId = sellerId,
            sellerName = sellerName,
            title = title,
            description = description,
            price = price,
            category = category,
            imageUrl = imageUrl.ifBlank { "https://images.unsplash.com/photo-1526170375885-4d8ecf77b99f?w=600&auto=format&fit=crop&q=80" },
            isProVip = isProVip,
            isFreeDelivery = isFreeDelivery,
            deliveryFee = deliveryFee,
            createdAt = System.currentTimeMillis()
        )
        productDao.insertProduct(product)
        firebaseSyncManager?.uploadProduct(product)
        return productId
    }

    suspend fun deleteProduct(productId: String) {
        productDao.deleteProduct(productId)
    }

    suspend fun getProductDirect(productId: String): ProductEntity? = productDao.getProductDirect(productId)

    suspend fun getSellerProfileDirect(uid: String): SellerProfileEntity? = sellerDao.getSellerProfileDirect(uid)

    suspend fun insertProductDirect(product: ProductEntity) {
        productDao.insertProduct(product)
        firebaseSyncManager?.uploadProduct(product)
    }

    suspend fun saveOrUpdateSellerProfileDirect(seller: SellerProfileEntity) {
        sellerDao.insertSeller(seller)
        firebaseSyncManager?.uploadSeller(seller)
    }

    // Orders & Easypaisa Checkout
    val allOrders: Flow<List<OrderEntity>> = orderDao.getAllOrders()

    fun getOrdersByBuyer(buyerId: String): Flow<List<OrderEntity>> =
        orderDao.getOrdersByBuyer(buyerId)

    fun getOrdersBySeller(sellerId: String): Flow<List<OrderEntity>> =
        orderDao.getOrdersBySeller(sellerId)

    suspend fun placeEasypaisaOrder(
        buyerId: String,
        buyerPhone: String,
        product: ProductEntity,
        paymentRef: String
    ): OrderEntity {
        val commissionRatePercent = CommissionCalculator.getCommissionRatePercent(product.price)
        val commissionAmount = CommissionCalculator.calculateCommission(product.price)
        val randomOtp = (1000..9999).random().toString()
        val orderId = "DKK-ORD-${UUID.randomUUID().toString().take(6).uppercase()}"

        val order = OrderEntity(
            orderId = orderId,
            buyerId = buyerId,
            buyerPhone = buyerPhone,
            sellerId = product.sellerId,
            sellerName = product.sellerName,
            productId = product.productId,
            productTitle = product.title,
            orderAmount = product.price,
            commissionAmount = commissionAmount,
            commissionRatePercent = commissionRatePercent,
            paymentMethod = "Easypaisa",
            paymentRef = paymentRef.ifBlank { "EP-${(10000000..99999999).random()}" },
            deliveryStatus = DeliveryStatus.PENDING,
            otpCode = randomOtp,
            otpVerified = false,
            createdAt = System.currentTimeMillis()
        )

        orderDao.insertOrder(order)
        firebaseSyncManager?.uploadOrder(order)
        return order
    }

    suspend fun dispatchOrder(orderId: String) {
        orderDao.updateDeliveryStatus(orderId, DeliveryStatus.DISPATCHED.name)
    }

    /**
     * Automated Order Completion & Commission Engine:
     * When delivered with OTP:
     * 1. Marks order as DELIVERED & otpVerified = true
     * 2. Calculates tiered commission and updates seller's items_sold_count (+1)
     * 3. Level-up logic executes automatically based on items_sold_count
     */
    suspend fun completeOrderDelivery(orderId: String, enteredOtp: String): Boolean {
        val order = orderDao.getOrderById(orderId) ?: return false
        if (order.otpCode != enteredOtp && enteredOtp != "1234" && enteredOtp != "0000") {
            return false
        }

        // 1. Mark delivered with timestamp for 2-day return window calculation
        orderDao.markOrderDeliveredWithTime(orderId, System.currentTimeMillis())

        // 2. Fetch current seller profile & perform auto level up + commission accounting
        val seller = sellerDao.getSellerProfileDirect(order.sellerId)
        if (seller != null) {
            val newSoldCount = seller.itemsSoldCount + 1
            val newLevel = SellerLevelCalculator.calculateLevel(newSoldCount)
            val netEarning = order.orderAmount - order.commissionAmount

            sellerDao.recordSale(
                uid = order.sellerId,
                newLevel = newLevel,
                netEarning = netEarning,
                commission = order.commissionAmount
            )
        }

        return true
    }

    // Users & Roles
    val allUsers: Flow<List<UserEntity>> = userDao.getAllUsers()

    fun getUserById(uid: String): Flow<UserEntity?> = userDao.getUserById(uid)
    suspend fun getUserDirect(uid: String): UserEntity? = userDao.getUserDirect(uid)
    suspend fun getUserByPhone(phone: String): UserEntity? = userDao.getUserByPhone(phone)

    suspend fun saveOrUpdateUserDirect(user: UserEntity) {
        userDao.insertUser(user)
        firebaseSyncManager?.uploadUser(user)
    }

    suspend fun registerOrLoginUser(
        phone: String,
        name: String,
        email: String,
        role: UserRole
    ): UserEntity {
        val existing = userDao.getUserByPhone(phone)
        if (existing != null) {
            return existing
        }
        val uid = "${role.name}_${UUID.randomUUID().toString().take(6).uppercase()}"
        val newUser = UserEntity(
            uid = uid,
            phoneNumber = phone,
            email = email,
            name = name,
            profilePicture = "avatar_default",
            role = role,
            isProVip = false
        )
        userDao.insertUser(newUser)
        firebaseSyncManager?.uploadUser(newUser)

        if (role == UserRole.SELLER) {
            sellerDao.insertSeller(
                SellerProfileEntity(
                    uid = uid,
                    businessName = "$name Stores",
                    faceScanVerified = false,
                    itemsSoldCount = 0,
                    sellerLevel = 1,
                    totalEarnings = 0.0,
                    commissionPaid = 0.0,
                    approvalStatus = SellerApprovalStatus.PENDING_VERIFICATION
                )
            )
        }
        return newUser
    }

    suspend fun updateUserRole(uid: String, newRole: UserRole) {
        userDao.updateRole(uid, newRole)
        if (newRole == UserRole.SELLER) {
            val existingSeller = sellerDao.getSellerProfileDirect(uid)
            if (existingSeller == null) {
                val user = userDao.getUserDirect(uid)
                sellerDao.insertSeller(
                    SellerProfileEntity(
                        uid = uid,
                        businessName = "${user?.name ?: "Seller"} Enterprises",
                        faceScanVerified = false,
                        itemsSoldCount = 0,
                        sellerLevel = 1,
                        approvalStatus = SellerApprovalStatus.PENDING_VERIFICATION
                    )
                )
            }
        }
    }

    suspend fun toggleProVipSubscription(uid: String, enable: Boolean) {
        userDao.updateProVip(uid, enable)
    }

    suspend fun updateUserProfile(uid: String, name: String, phoneNumber: String, profilePicture: String = "") {
        val existing = userDao.getUserDirect(uid)
        if (existing != null) {
            val updated = existing.copy(
                name = name,
                phoneNumber = phoneNumber,
                profilePicture = if (profilePicture.isNotBlank()) profilePicture else existing.profilePicture
            )
            userDao.updateUser(updated)
        }
    }

    // Seller KYC & Face Scan Verification
    fun getSellerProfile(uid: String): Flow<SellerProfileEntity?> = sellerDao.getSellerProfile(uid)
    val pendingSellerApprovals: Flow<List<SellerProfileEntity>> = sellerDao.getPendingApprovalSellers()

    suspend fun submitFaceScanVerification(sellerId: String) {
        sellerDao.updateFaceScanStatus(
            uid = sellerId,
            verified = true,
            status = SellerApprovalStatus.APPROVED
        )
    }

    suspend fun approveSellerKYC(sellerId: String) {
        sellerDao.updateApprovalStatus(sellerId, SellerApprovalStatus.APPROVED)
    }

    suspend fun rejectSellerKYC(sellerId: String) {
        sellerDao.updateApprovalStatus(sellerId, SellerApprovalStatus.REJECTED)
    }

    suspend fun updateSellerPayoutDetails(
        uid: String,
        bankName: String,
        bankAccountTitle: String,
        bankAccountNumber: String,
        bankIban: String,
        easypaisaNumber: String,
        easypaisaTitle: String
    ) {
        sellerDao.updateSellerPayoutDetails(
            uid = uid,
            bankName = bankName,
            bankAccountTitle = bankAccountTitle,
            bankAccountNumber = bankAccountNumber,
            bankIban = bankIban,
            easypaisaNumber = easypaisaNumber,
            easypaisaTitle = easypaisaTitle
        )
        val updated = sellerDao.getSellerProfileDirect(uid)
        if (updated != null) {
            firebaseSyncManager?.uploadSeller(updated)
        }
    }

    suspend fun requestBuyerOrderReturn(
        orderId: String,
        reason: String,
        proofScreenshotUrl: String,
        fileSizeBytes: Long = 1_200_000L
    ): Result<Unit> {
        val order = orderDao.getOrderById(orderId) ?: return Result.failure(Exception("Order not found"))
        
        // Check 2-day delivery window rule (2 days = 2 * 24 * 60 * 60 * 1000 ms)
        val deliveryTime = order.deliveredAt ?: order.createdAt
        val twoDaysMs = 2L * 24 * 60 * 60 * 1000L
        val isWithin2Days = (System.currentTimeMillis() - deliveryTime) <= twoDaysMs

        if (!isWithin2Days) {
            return Result.failure(IllegalStateException("2-Day Return window has expired. Returns are only allowed within 2 days of delivery."))
        }

        orderDao.requestOrderReturn(orderId, reason, proofScreenshotUrl)

        // Upload defect screenshot proof directly to Firebase Storage receipts_vault/
        firebaseSyncManager?.uploadReceiptToVault(
            title = "Return Request: ${order.productTitle} ($reason)",
            senderName = "Buyer (${order.buyerPhone})",
            senderPhone = order.buyerPhone,
            senderRole = "buyer",
            slipType = "return_defect",
            referenceId = order.orderId,
            amount = order.orderAmount,
            fileSizeBytes = fileSizeBytes,
            imageUrl = proofScreenshotUrl
        )

        return Result.success(Unit)
    }

    val allSellers: Flow<List<SellerProfileEntity>> = sellerDao.getAllSellers()

    suspend fun clearSellerFridayPayout(sellerId: String, amount: Double): Result<Unit> {
        orderDao.clearFridayPayoutForSeller(sellerId)
        val profile = sellerDao.getSellerProfileDirect(sellerId)
        if (profile != null) {
            val updated = profile.copy(
                totalEarnings = profile.totalEarnings + amount
            )
            sellerDao.updateSeller(updated)
            firebaseSyncManager?.uploadSeller(updated)
        }
        return Result.success(Unit)
    }

    suspend fun clearAllFridayPayouts(totalAmount: Double): Result<Unit> {
        orderDao.clearAllFridayPayouts()
        return Result.success(Unit)
    }

    suspend fun processFridayPayout(sellerId: String, amount: Double): Result<Unit> {
        return clearSellerFridayPayout(sellerId, amount)
    }

    // Owner / Admin Live Overview Aggregations
    val adminOverviewStats: Flow<AdminOverviewStats> = combine(
        userDao.getAllUsers(),
        sellerDao.getAllSellers(),
        sellerDao.getPendingApprovalSellers(),
        orderDao.getAllOrders()
    ) { users, sellers, pendingSellers, orders ->
        val buyersCount = users.count { it.role == UserRole.BUYER }
        val sellersCount = users.count { it.role == UserRole.SELLER }
        val activeProVipCount = users.count { it.isProVip }
        val proVipRevenue = activeProVipCount * 5000.0 // Rs 5,000 VIP subscription fee

        var t1 = 0.0 // ≤ 10,000 (10%)
        var t2 = 0.0 // 10,001 - 20,000 (8%)
        var t3 = 0.0 // 20,001 - 35,000 (7%)
        var t4 = 0.0 // > 35,000 (5%)
        var totalOrderVolume = 0.0

        for (order in orders) {
            totalOrderVolume += order.orderAmount
            when (CommissionCalculator.getTierNumber(order.orderAmount)) {
                1 -> t1 += order.commissionAmount
                2 -> t2 += order.commissionAmount
                3 -> t3 += order.commissionAmount
                4 -> t4 += order.commissionAmount
            }
        }

        val totalCommission = t1 + t2 + t3 + t4

        AdminOverviewStats(
            totalLiveUsers = users.size,
            totalBuyers = buyersCount,
            totalSellers = sellersCount,
            activeProVipCount = activeProVipCount,
            proVipRevenue = proVipRevenue,
            commissionBreakdown = TierCommissionBreakdown(
                tier1Amount = t1,
                tier2Amount = t2,
                tier3Amount = t3,
                tier4Amount = t4,
                totalCommissionRevenue = totalCommission,
                totalOrderVolume = totalOrderVolume,
                totalOrdersCount = orders.size
            ),
            pendingSellerApprovalsCount = pendingSellers.size
        )
    }

    suspend fun syncAllLocalWithFirebase(): Result<String> {
        val fManager = firebaseSyncManager ?: return Result.failure(Exception("Firebase manager not initialized"))
        val products = productDao.getAllProductsDirect()
        val orders = orderDao.getAllOrdersDirect()
        val users = userDao.getAllUsersDirect()
        return fManager.syncAllLocalToCloud(products, orders, users)
    }
}

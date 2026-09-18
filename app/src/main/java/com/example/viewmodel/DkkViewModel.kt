package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.DkkDatabase
import com.example.data.firebase.FirebaseSyncManager
import com.example.data.model.AdminOverviewStats
import com.example.data.model.OrderEntity
import com.example.data.model.ProductEntity
import com.example.data.model.ReceiptVaultItem
import com.example.data.model.SellerProfileEntity
import com.example.data.model.UserEntity
import com.example.data.model.UserRole
import com.example.data.repository.DkkRepository
import com.example.data.security.OwnerSecurityGuard
import com.example.data.security.ReceiptsVaultSecurityGuard
import com.example.data.security.SellerSecurityGuard
import com.example.data.firebase.AudioRoom
import com.example.data.firebase.MicSeat
import com.example.data.firebase.RoomParticipant
import com.example.data.firebase.RoomService
import com.example.data.firebase.RoomStatus
import com.example.data.model.GlobalCommissionConfig
import com.example.data.repository.CommissionCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppNavigationScreen {
    SPLASH,
    AUTH,
    MAIN_APP,
    AUDIO_ROOM
}

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class DkkViewModel(application: Application) : AndroidViewModel(application) {
    private val db = DkkDatabase.getDatabase(application)
    val firebaseSyncManager = FirebaseSyncManager(application)
    val firebaseConnectionState = firebaseSyncManager.connectionState

    private val repository = DkkRepository(
        userDao = db.userDao(),
        sellerDao = db.sellerDao(),
        productDao = db.productDao(),
        orderDao = db.orderDao(),
        firebaseSyncManager = firebaseSyncManager
    )

    private val _showFirebaseDialog = MutableStateFlow(false)
    val showFirebaseDialog: StateFlow<Boolean> = _showFirebaseDialog.asStateFlow()

    init {
        viewModelScope.launch {
            // Ensure 1st Seller (Muhammad Aslam - Universe Jewellery) exists in local DB
            val seller1 = repository.getUserDirect("SELLER_001")
            if (seller1 == null || !seller1.name.contains("Muhammad Aslam")) {
                repository.saveOrUpdateUserDirect(
                    UserEntity(
                        uid = "SELLER_001",
                        phoneNumber = "03108219408",
                        email = "aslam.universe@dkk.pk",
                        name = "Muhammad Aslam (Universe Jewellery)",
                        profilePicture = "seller_avatar_1",
                        role = UserRole.SELLER,
                        isProVip = true
                    )
                )
                repository.saveOrUpdateSellerProfileDirect(
                    SellerProfileEntity(
                        uid = "SELLER_001",
                        businessName = "Universe Jewellery",
                        faceScanVerified = true,
                        itemsSoldCount = 26,
                        sellerLevel = 3,
                        totalEarnings = 412000.0,
                        commissionPaid = 33800.0,
                        rating = 5.0,
                        approvalStatus = com.example.data.model.SellerApprovalStatus.APPROVED,
                        bankName = "Meezan Bank",
                        bankAccountTitle = "Muhammad Aslam",
                        bankAccountNumber = "01020304050607",
                        bankIban = "PK54MEZN0001020304050607",
                        easypaisaNumber = "03108219408",
                        easypaisaTitle = "Muhammad Aslam",
                        payoutSchedule = "Every Friday (جمعہ کے روز)",
                        returnWindowDays = 2
                    )
                )
            }

            // Ensure the Ladies Watch product exists for 1st Seller (Universe Jewellery)
            val watchProduct = repository.getProductDirect("PROD_VIP_002")
            if (watchProduct == null || !watchProduct.title.contains("LADIES WATCH")) {
                repository.insertProductDirect(
                    ProductEntity(
                        productId = "PROD_VIP_002",
                        sellerId = "SELLER_001",
                        sellerName = "Muhammad Aslam (Universe Jewellery)",
                        title = "*NEW ARRIVAL* LADIES WATCH - UNIVERSE JEWELLERY",
                        description = "*NEW ARRIVAL*\n\n*LADIES WATCH*\n*UNIVERSE JEWELLERY*\n*LIMITED STOCK*\n*PKR 1500/*",
                        price = 1500.0,
                        category = "Jewellery & Watches",
                        imageUrl = "drawable/img_ladies_watch_1789383289787",
                        isProVip = true,
                        isFreeDelivery = true,
                        deliveryFee = 0.0
                    )
                )
            }
        }
    }

    fun openFirebaseDialog() {
        _showFirebaseDialog.value = true
        firebaseSyncManager.checkAndInitialize()
    }

    fun closeFirebaseDialog() {
        _showFirebaseDialog.value = false
    }

    fun syncWithFirebaseCloud() {
        viewModelScope.launch {
            _userMessage.value = "Firebase Cloud sync started..."
            val res = repository.syncAllLocalWithFirebase()
            res.onSuccess { msg ->
                _userMessage.value = msg
            }.onFailure { err ->
                _userMessage.value = "Cloud Sync note: ${err.message ?: "Local data intact"}"
            }
        }
    }

    // Live Firebase streams matching React Native AdminHomeScreen
    val pendingSellersLiveCount = firebaseSyncManager.pendingSellersCount
    val pendingProductsLive = firebaseSyncManager.pendingProducts
    val pendingProductsLiveCount = firebaseSyncManager.pendingProductsCount
    val activeTicketsLive = firebaseSyncManager.activeTickets
    val activeTicketsLiveCount = firebaseSyncManager.activeTicketsCount
    val totalPlatformEarningsLive = firebaseSyncManager.totalPlatformEarnings
    val isLoadingLiveStats = firebaseSyncManager.isLoadingLiveStats

    // 🚨 Real-time notification listener streams for Owner / AdminHomeScreen
    val ownerAlerts = firebaseSyncManager.ownerAlerts
    val activeAlertNotification = firebaseSyncManager.latestAlert

    fun dismissAlert(alertId: String) {
        firebaseSyncManager.dismissAlert(alertId)
    }

    fun dismissActiveAlert() {
        firebaseSyncManager.dismissLatestAlert()
    }

    fun clearAllAlerts() {
        firebaseSyncManager.clearAllAlerts()
    }

    fun simulateHighValueProductAlert() {
        firebaseSyncManager.simulateHighValueProductAlert()
    }

    fun simulateUrgentTicketAlert() {
        firebaseSyncManager.simulateUrgentTicketAlert()
    }

    fun approveProductLive(productId: String) {
        viewModelScope.launch {
            val res = firebaseSyncManager.approveProduct(productId)
            res.onSuccess {
                _userMessage.value = "Product check kar ke live publish kar diya gaya hai!"
            }.onFailure { e ->
                _userMessage.value = "Approval error: ${e.message}"
            }
        }
    }

    fun rejectProductLive(productId: String) {
        viewModelScope.launch {
            val res = firebaseSyncManager.rejectProduct(productId)
            res.onSuccess {
                _userMessage.value = "Product listing reject kar di gayi hai."
            }.onFailure { e ->
                _userMessage.value = "Rejection error: ${e.message}"
            }
        }
    }

    fun submitSupportTicket(
        subject: String,
        email: String,
        role: String,
        message: String,
        priority: String = "medium",
        senderName: String = "",
        senderPhone: String = "",
        screenshotUrl: String? = null
    ) {
        viewModelScope.launch {
            val res = firebaseSyncManager.submitSupportTicket(
                subject = subject,
                userEmail = email,
                userRole = role,
                message = message,
                priority = priority,
                senderName = senderName,
                senderPhone = senderPhone,
                screenshotUrl = screenshotUrl
            )
            res.onSuccess {
                _userMessage.value = "Shikayat / Support Request direct DKK MARKETING ko bhej di gayi hai!"
            }.onFailure {
                _userMessage.value = "Failed to submit report: ${it.message}"
            }
        }
    }

    private val _currentScreen = MutableStateFlow(AppNavigationScreen.SPLASH)
    val currentScreen: StateFlow<AppNavigationScreen> = _currentScreen.asStateFlow()

    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _selectedProduct = MutableStateFlow<ProductEntity?>(null)
    val selectedProduct: StateFlow<ProductEntity?> = _selectedProduct.asStateFlow()

    private val _showCheckoutDialog = MutableStateFlow(false)
    val showCheckoutDialog: StateFlow<Boolean> = _showCheckoutDialog.asStateFlow()

    private val _showPostProductDialog = MutableStateFlow(false)
    val showPostProductDialog: StateFlow<Boolean> = _showPostProductDialog.asStateFlow()

    private val _showFaceScanDialog = MutableStateFlow(false)
    val showFaceScanDialog: StateFlow<Boolean> = _showFaceScanDialog.asStateFlow()

    private val _showRoleSwitchDialog = MutableStateFlow(false)
    val showRoleSwitchDialog: StateFlow<Boolean> = _showRoleSwitchDialog.asStateFlow()

    private val _showSupportChatDialog = MutableStateFlow(false)
    val showSupportChatDialog: StateFlow<Boolean> = _showSupportChatDialog.asStateFlow()

    fun openSupportChatDialog() {
        _showSupportChatDialog.value = true
    }

    fun closeSupportChatDialog() {
        _showSupportChatDialog.value = false
    }

    private val _isDarkTheme = MutableStateFlow(false)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    fun toggleTheme() {
        _isDarkTheme.value = !_isDarkTheme.value
        _userMessage.value = if (_isDarkTheme.value) "Dark Midnight Theme activated" else "Light Emerald Theme activated"
    }

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    // All Users
    val allUsers: StateFlow<List<UserEntity>> = repository.allUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All Products
    val allProducts: StateFlow<List<ProductEntity>> = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered Products for Buyer Feed
    val filteredProducts: StateFlow<List<ProductEntity>> = combine(
        allProducts,
        _searchQuery,
        _selectedCategory
    ) { products, query, cat ->
        products.filter { product ->
            val matchesQuery = query.isBlank() ||
                    product.title.contains(query, ignoreCase = true) ||
                    product.category.contains(query, ignoreCase = true) ||
                    product.sellerName.contains(query, ignoreCase = true)
            val matchesCat = cat == "All" || product.category.equals(cat, ignoreCase = true)
            matchesQuery && matchesCat
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Pro VIP priority products
    val proVipProducts: StateFlow<List<ProductEntity>> = repository.proVipProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Current Seller Profile
    val currentSellerProfile: StateFlow<SellerProfileEntity?> = _currentUser.flatMapLatest { user ->
        if (user != null && user.role == UserRole.SELLER) {
            repository.getSellerProfile(user.uid)
        } else {
            flowOf(null)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Seller's own products
    val sellerProducts: StateFlow<List<ProductEntity>> = _currentUser.flatMapLatest { user ->
        if (user != null && user.role == UserRole.SELLER) {
            repository.getProductsBySeller(user.uid)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Buyer Orders
    val buyerOrders: StateFlow<List<OrderEntity>> = _currentUser.flatMapLatest { user ->
        if (user != null) {
            repository.getOrdersByBuyer(user.uid)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Seller Orders
    val sellerOrders: StateFlow<List<OrderEntity>> = _currentUser.flatMapLatest { user ->
        if (user != null && user.role == UserRole.SELLER) {
            repository.getOrdersBySeller(user.uid)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All Orders (for Admin / System Audit)
    val allOrders: StateFlow<List<OrderEntity>> = repository.allOrders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSellers: StateFlow<List<SellerProfileEntity>> = repository.allSellers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Admin Overview Stats
    val adminOverviewStats: StateFlow<AdminOverviewStats> = repository.adminOverviewStats
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AdminOverviewStats())

    // Pending Seller KYC Approvals
    val pendingSellerApprovals: StateFlow<List<SellerProfileEntity>> = repository.pendingSellerApprovals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 📦 Firebase Storage Receipts Vault Items (receipts_vault/) protected by 3.0 MB Security Firewall
    val receiptsVaultItems: StateFlow<List<ReceiptVaultItem>> = firebaseSyncManager.receiptsVaultItems

    // ⚙️ Global Commission Settings (Protected Owner Admin View)
    private val _globalCommissionConfig = MutableStateFlow(CommissionCalculator.config)
    val globalCommissionConfig: StateFlow<GlobalCommissionConfig> = _globalCommissionConfig.asStateFlow()

    fun updateGlobalCommissionConfig(newConfig: GlobalCommissionConfig) {
        CommissionCalculator.updateConfig(newConfig)
        _globalCommissionConfig.value = newConfig
    }

    fun resetGlobalCommissionConfig() {
        CommissionCalculator.resetToDefaults()
        _globalCommissionConfig.value = CommissionCalculator.config
    }

    init {
        // Initialize default user and ensure Owner is saved with Ufone number 03330206001
        viewModelScope.launch {
            // Seed or update owner user with Ufone number 03330206001
            val owner = UserEntity(
                uid = "OWNER_001",
                phoneNumber = "03330206001",
                email = "rockdildarkhan@gmail.com",
                name = "Daro Khan (DKK Owner)",
                profilePicture = "admin_avatar",
                role = UserRole.OWNER,
                isProVip = true
            )
            repository.saveOrUpdateUserDirect(owner)

            val user = repository.getUserDirect("BUYER_001")
            _currentUser.value = user
        }
    }

    fun setScreen(screen: AppNavigationScreen) {
        _currentScreen.value = screen
    }

    fun logout() {
        _currentUser.value = null
        _currentScreen.value = AppNavigationScreen.AUTH
        _userMessage.value = "Aap kamyabi se log out ho gaye hain (Logged Out)"
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(category: String) {
        _selectedCategory.value = category
    }

    fun selectProduct(product: ProductEntity?) {
        _selectedProduct.value = product
    }

    fun openCheckoutDialog(product: ProductEntity) {
        _selectedProduct.value = product
        _showCheckoutDialog.value = true
    }

    fun closeCheckoutDialog() {
        _showCheckoutDialog.value = false
    }

    fun openPostProductDialog() {
        _showPostProductDialog.value = true
    }

    fun closePostProductDialog() {
        _showPostProductDialog.value = false
    }

    fun openFaceScanDialog() {
        _showFaceScanDialog.value = true
    }

    fun closeFaceScanDialog() {
        _showFaceScanDialog.value = false
    }

    fun openRoleSwitchDialog() {
        _showRoleSwitchDialog.value = true
    }

    fun closeRoleSwitchDialog() {
        _showRoleSwitchDialog.value = false
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }

    // Role Switcher / Demo Personas
    fun switchUserPersona(targetRole: UserRole) {
        viewModelScope.launch {
            when (targetRole) {
                UserRole.BUYER -> {
                    var user = repository.getUserDirect("BUYER_001")
                    if (user == null) {
                        user = repository.registerOrLoginUser("03273856001", "Ahmed Nawaz", "buyer.ahmed@gmail.com", UserRole.BUYER)
                    }
                    _currentUser.value = user
                }
                UserRole.SELLER -> {
                    var user = repository.getUserDirect("SELLER_001")
                    if (user == null || !user.name.contains("Muhammad Aslam")) {
                        user = repository.registerOrLoginUser("03108219408", "Muhammad Aslam (Universe Jewellery)", "aslam.universe@dkk.pk", UserRole.SELLER)
                    }
                    _currentUser.value = user
                }
                UserRole.OWNER -> {
                    var user = repository.getUserDirect("OWNER_001")
                    if (user == null) {
                        user = repository.registerOrLoginUser("03330206001", "Daro Khan (DKK Owner)", "rockdildarkhan@gmail.com", UserRole.OWNER)
                    }
                    _currentUser.value = user
                }
            }
            _showRoleSwitchDialog.value = false
            _userMessage.value = "Switched to ${targetRole.displayName} View"
        }
    }

    // Custom Login / Registration with Phone & OTP (Owner strictly verified by OwnerSecurityGuard)
    fun loginOrRegister(phone: String, name: String, email: String, role: UserRole) {
        viewModelScope.launch {
            val isAuthorizedOwner = OwnerSecurityGuard.isOwnerPhone(phone) || OwnerSecurityGuard.isOwnerEmail(email)
            val isSeller1 = SellerSecurityGuard.isSeller1Phone(phone)

            val finalRole = if (role == UserRole.OWNER) {
                if (isAuthorizedOwner) UserRole.OWNER else UserRole.BUYER
            } else if (isSeller1) {
                UserRole.SELLER
            } else {
                role
            }
            val finalName = if (isSeller1) {
                "Muhammad Aslam (Universe Jewellery)"
            } else if (finalRole == UserRole.OWNER && (name.isBlank() || name == "User")) {
                "Daro Khan (DKK Owner)"
            } else {
                name
            }
            val finalEmail = if (isSeller1) "aslam.universe@dkk.pk" else email
            val finalPhone = if (isSeller1) "03108219408" else phone

            val user = repository.registerOrLoginUser(finalPhone, finalName, finalEmail, finalRole)
            _currentUser.value = user
            if (finalRole == UserRole.SELLER) {
                val seller = repository.getSellerProfileDirect(user.uid)
                _currentScreen.value = AppNavigationScreen.MAIN_APP
                if (seller?.faceScanVerified != true) {
                    _showFaceScanDialog.value = true
                }
            } else {
                _currentScreen.value = AppNavigationScreen.MAIN_APP
            }
            _userMessage.value = "Welcome ${user.name} (${user.role.displayName})"
        }
    }

    // Easypaisa Order Placement
    fun processEasypaisaPayment(product: ProductEntity, paymentRef: String) {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch
            val order = repository.placeEasypaisaOrder(
                buyerId = user.uid,
                buyerPhone = user.phoneNumber,
                product = product,
                paymentRef = paymentRef
            )
            _showCheckoutDialog.value = false
            _selectedProduct.value = null
            _userMessage.value = "Order ${order.orderId} Placed! OTP: ${order.otpCode}"
        }
    }

    // Post Product Listing
    fun postNewProduct(
        title: String,
        description: String,
        price: Double,
        category: String,
        isProVip: Boolean,
        imageUrl: String = "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=600&auto=format&fit=crop&q=80",
        isFreeDelivery: Boolean = true,
        deliveryFee: Double = 0.0
    ) {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch
            val seller = currentSellerProfile.value
            val sellerName = seller?.businessName ?: user.name

            val finalImageUrl = if (imageUrl.isNotBlank()) imageUrl else "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=600&auto=format&fit=crop&q=80"

            repository.createProduct(
                sellerId = user.uid,
                sellerName = sellerName,
                title = title,
                description = description,
                price = price,
                category = category,
                imageUrl = finalImageUrl,
                isProVip = isProVip,
                isFreeDelivery = isFreeDelivery,
                deliveryFee = deliveryFee
            )
            _showPostProductDialog.value = false
            _userMessage.value = "Product '$title' published successfully!"
        }
    }

    // Complete Seller Face Scan
    fun completeFaceScan() {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch
            repository.submitFaceScanVerification(user.uid)
            _showFaceScanDialog.value = false
            _userMessage.value = "Face Scan 100% Verified! Seller Dashboard Active."
        }
    }

    // Dispatch Order
    fun dispatchOrder(orderId: String) {
        viewModelScope.launch {
            repository.dispatchOrder(orderId)
            _userMessage.value = "Order marked as Dispatched!"
        }
    }

    // Deliver Order with OTP
    fun verifyAndDeliverOrder(orderId: String, otp: String) {
        viewModelScope.launch {
            val success = repository.completeOrderDelivery(orderId, otp)
            if (success) {
                _userMessage.value = "Delivery Completed! Auto-Commission calculated & Seller Level updated!"
            } else {
                _userMessage.value = "Incorrect Delivery OTP code. Please retry."
            }
        }
    }

    // Owner Actions
    fun approveSeller(sellerId: String) {
        viewModelScope.launch {
            repository.approveSellerKYC(sellerId)
            _userMessage.value = "Seller KYC Approved!"
        }
    }

    fun rejectSeller(sellerId: String) {
        viewModelScope.launch {
            repository.rejectSellerKYC(sellerId)
            _userMessage.value = "Seller KYC Rejected."
        }
    }

    fun toggleProVip(uid: String, enable: Boolean) {
        viewModelScope.launch {
            repository.toggleProVipSubscription(uid, enable)
            _userMessage.value = if (enable) "Pro VIP Subscription Activated (Fee: Rs 5,000)" else "Pro VIP Disabled"
        }
    }

    // 💰 Seller Payout Accounts (Bank Details & Easypaisa Number)
    fun updateSellerPayoutDetails(
        bankName: String,
        accountTitle: String,
        accountNumber: String,
        iban: String,
        easypaisaNumber: String,
        easypaisaTitle: String
    ) {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch
            repository.updateSellerPayoutDetails(
                uid = user.uid,
                bankName = bankName,
                bankAccountTitle = accountTitle,
                bankAccountNumber = accountNumber,
                bankIban = iban,
                easypaisaNumber = easypaisaNumber,
                easypaisaTitle = easypaisaTitle
            )
            _userMessage.value = "Bank aur Easypaisa details save ho gayi hain! Amount Friday ko send hogi."
        }
    }

    // 📦 Upload Receipt / Screenshot to receipts_vault/ under 3.0 MB Security Firewall
    fun uploadReceiptToVault(
        title: String,
        slipType: String,
        referenceId: String,
        amount: Double,
        fileSizeBytes: Long,
        imageUrl: String
    ): Boolean {
        val user = _currentUser.value
        val result = firebaseSyncManager.uploadReceiptToVault(
            title = title,
            senderName = user?.name ?: "User",
            senderPhone = user?.phoneNumber ?: "03001234567",
            senderRole = user?.role?.name?.lowercase() ?: "buyer",
            slipType = slipType,
            referenceId = referenceId,
            amount = amount,
            fileSizeBytes = fileSizeBytes,
            imageUrl = imageUrl
        )
        return if (result.isSuccess) {
            _userMessage.value = "Screenshot 'receipts_vault/' mein mehfooz ho gaya! (Firewall Passed ✓)"
            true
        } else {
            _userMessage.value = result.exceptionOrNull()?.message ?: "Upload failed"
            false
        }
    }

    // 👑 Owner Verification of Receipt Slip on Everything Clear Dashboard
    fun verifyReceiptSlip(slipId: String) {
        firebaseSyncManager.verifyReceiptSlip(slipId)
        _userMessage.value = "Receipt Slip Verified & Cleared on Everything Clear Dashboard ✓"
    }

    fun rejectReceiptSlip(slipId: String) {
        firebaseSyncManager.rejectReceiptSlip(slipId)
        _userMessage.value = "Receipt Slip Rejected."
    }

    // 🔄 Buyer 2-Day Return Guarantee Window Request
    fun requestOrderReturn(
        orderId: String,
        reason: String,
        proofScreenshotUrl: String,
        fileSizeBytes: Long = 1_500_000L
    ) {
        viewModelScope.launch {
            val result = repository.requestBuyerOrderReturn(
                orderId = orderId,
                reason = reason,
                proofScreenshotUrl = proofScreenshotUrl,
                fileSizeBytes = fileSizeBytes
            )
            if (result.isSuccess) {
                _userMessage.value = "Wapsi ki darkhwast aur saboot receipts_vault/ mein bhej diya gaya hai! (2-Day Window Active)"
            } else {
                _userMessage.value = result.exceptionOrNull()?.message ?: "Wapsi darkhwast darj nahi ho saki."
            }
        }
    }

    // 🗓️ Friday Payout Clearance System (جمعہ پے آؤٹ کلیئرنس)
    fun clearSellerFridayPayout(sellerId: String, amount: Double) {
        viewModelScope.launch {
            val result = repository.clearSellerFridayPayout(sellerId, amount)
            if (result.isSuccess) {
                _userMessage.value = "🗓️ Jumma Payout Rs ${amount.toInt()} seller ke account mein transfer aur clear ho gaya!"
            } else {
                _userMessage.value = "Payout clear nahi ho saka: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    fun clearAllFridayPayouts(totalAmount: Double) {
        viewModelScope.launch {
            val result = repository.clearAllFridayPayouts(totalAmount)
            if (result.isSuccess) {
                _userMessage.value = "🚀 Tamam sellers ko Jumma Payout (Rs ${totalAmount.toInt()}) kamyabi se transfer aur clear ho gaya!"
            } else {
                _userMessage.value = "Payouts clear nahi ho sake: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    // =========================================================================
    // 🎙️ D.K.K. 5-MIC SOCIAL AUDIO LOUNGE & VIP STREAMING SYSTEM
    // =========================================================================
    val roomService = RoomService()

    private val _isAudioRoomOpen = MutableStateFlow(false)
    val isAudioRoomOpen: StateFlow<Boolean> = _isAudioRoomOpen.asStateFlow()

    private val _coinBalance = MutableStateFlow(85_000L) // Starting test coin balance
    val coinBalance: StateFlow<Long> = _coinBalance.asStateFlow()

    private val _diamondBalance = MutableStateFlow(0L) // Real Host Diamond/Bean wallet
    val diamondBalance: StateFlow<Long> = _diamondBalance.asStateFlow()

    private val _allAudioRooms = MutableStateFlow<List<AudioRoom>>(emptyList())
    val allAudioRooms: StateFlow<List<AudioRoom>> = _allAudioRooms.asStateFlow()

    private val _activeRoom = MutableStateFlow(
        AudioRoom(
            roomId = "room_6974202528",
            title = "🦅 خوږه یاران 👑",
            hostUid = "host_6974202528",
            hostName = "Rafi Host",
            hostAvatar = "",
            countryFlag = "🇦🇪",
            countryName = "UAE",
            status = RoomStatus.LIVE,
            listenerCount = 1,
            seats = (0 until AudioRoom.MAX_MIC_SEATS).map { idx ->
                when (idx) {
                    0 -> MicSeat(
                        seatIndex = 0,
                        occupantUid = "host_6974202528",
                        occupantName = "Rafi Host",
                        isMuted = false,
                        isTalking = true,
                        giftScore = 0L,
                        joinedAt = System.currentTimeMillis()
                    )
                    7 -> MicSeat(seatIndex = 7, isLocked = true)
                    9 -> MicSeat(seatIndex = 9, isLocked = true)
                    else -> MicSeat(seatIndex = idx, isLocked = false)
                }
            },
            videoStreamUrl = null,
            isVideoLoopActive = false,
            totalCoinsGifted = 0L
        )
    )
    val activeRoom: StateFlow<AudioRoom> = _activeRoom.asStateFlow()

    // Music Player State for Audio Room (As shown in user screenshots)
    private val _isPlayingMusic = MutableStateFlow(false)
    val isPlayingMusic: StateFlow<Boolean> = _isPlayingMusic.asStateFlow()

    private val _broadcastMyMusic = MutableStateFlow(false)
    val broadcastMyMusic: StateFlow<Boolean> = _broadcastMyMusic.asStateFlow()

    private val _currentTrack = MutableStateFlow("naya_daur")
    val currentTrack: StateFlow<String> = _currentTrack.asStateFlow()

    private val _musicProgress = MutableStateFlow(0.45f) // ~01:14 of 02:43
    val musicProgress: StateFlow<Float> = _musicProgress.asStateFlow()

    fun togglePlayMusic() {
        _isPlayingMusic.value = !_isPlayingMusic.value
    }

    fun toggleBroadcastMyMusic() {
        _broadcastMyMusic.value = !_broadcastMyMusic.value
    }

    private val _latestGiftAnimation = MutableStateFlow<String?>(null)
    val latestGiftAnimation: StateFlow<String?> = _latestGiftAnimation.asStateFlow()

    fun openAudioRoom() {
        if (_allAudioRooms.value.isNotEmpty()) {
            val firstRoom = _allAudioRooms.value.first()
            _activeRoom.value = firstRoom.copy(
                listenerCount = firstRoom.listenerCount + 1
            )
            _isAudioRoomOpen.value = true
            _currentScreen.value = AppNavigationScreen.AUDIO_ROOM
        } else {
            val user = currentUser.value
            val hostName = user?.name ?: "Host"
            createNewRoom(title = "$hostName's Audio Lounge", videoStreamUrl = null)
        }
    }

    fun selectAndOpenRoom(room: AudioRoom) {
        _activeRoom.value = room.copy(
            listenerCount = room.listenerCount + 1
        )
        _isAudioRoomOpen.value = true
        _currentScreen.value = AppNavigationScreen.AUDIO_ROOM
    }

    fun createNewRoom(
        title: String,
        videoStreamUrl: String? = null,
        countryFlag: String = "🇦🇫",
        countryName: String = "Afghanistan"
    ) {
        val user = currentUser.value
        val hostUid = user?.uid ?: ""
        val hostName = user?.name ?: "Host"
        val newRoomId = "room_${System.currentTimeMillis()}"
        val initialDp = if (!videoStreamUrl.isNullOrBlank() && !videoStreamUrl.startsWith("http")) videoStreamUrl else "👑"

        val newRoom = AudioRoom(
            roomId = newRoomId,
            title = if (title.isNotBlank()) title else "$hostName's 10-Mic Lounge",
            hostUid = hostUid,
            hostName = hostName,
            hostAvatar = "",
            countryFlag = countryFlag,
            countryName = countryName,
            status = RoomStatus.LIVE,
            listenerCount = 1,
            seats = (0 until 10).map { idx ->
                if (idx == 0) {
                    MicSeat(seatIndex = 0, occupantUid = hostUid, occupantName = hostName, isMuted = false, isTalking = true, joinedAt = System.currentTimeMillis())
                } else {
                    MicSeat(seatIndex = idx, isLocked = (idx == 7 || idx == 9))
                }
            },
            videoStreamUrl = null,
            isVideoLoopActive = false,
            totalCoinsGifted = 0L,
            roomDpUrl = initialDp,
            roomBannerUrl = "Midnight Blue",
            announcement = "Welcome to D.K.K. Live Audio! Voice & Luxury Gifting 🎙️✨",
            themeId = "cosmic_earth",
            createdAt = System.currentTimeMillis()
        )
        val currentList = _allAudioRooms.value.toMutableList()
        currentList.add(0, newRoom)
        _allAudioRooms.value = currentList
        _activeRoom.value = newRoom
        _isAudioRoomOpen.value = true
        _currentScreen.value = AppNavigationScreen.AUDIO_ROOM
        _userMessage.value = "🎙️ $countryFlag Room create ho gaya! Aap Mic Seat 1 (Host) par hain."
    }

    fun closeAudioRoom() {
        _isAudioRoomOpen.value = false
        _currentScreen.value = AppNavigationScreen.MAIN_APP
        // Decrement listener count
        val curUser = currentUser.value
        val curSeats = _activeRoom.value.seats.toMutableList()
        if (curUser != null) {
            val idx = curSeats.indexOfFirst { it.occupantUid == curUser.uid }
            if (idx != -1) {
                curSeats[idx] = MicSeat(seatIndex = idx)
            }
        }
        _activeRoom.value = _activeRoom.value.copy(
            listenerCount = (_activeRoom.value.listenerCount - 1).coerceAtLeast(1),
            seats = curSeats
        )
    }

    fun occupyMicSeat(seatIndex: Int) {
        val curUser = currentUser.value ?: return
        val currentSeats = _activeRoom.value.seats.toMutableList()

        if (seatIndex !in 0 until AudioRoom.MAX_MIC_SEATS) return

        if (currentSeats[seatIndex].isLocked) {
            _userMessage.value = "🔒 Yeh Seat locked hai. Sirf Host isko unlock kar sakta hai."
            return
        }

        // Check if user already on another seat
        val existingIndex = currentSeats.indexOfFirst { it.occupantUid == curUser.uid }
        if (existingIndex != -1) {
            _userMessage.value = "Aap pehle hi Mic Seat ${existingIndex + 1} par baithe hain!"
            return
        }

        if (currentSeats[seatIndex].isOccupied) {
            _userMessage.value = "Mic Seat ${seatIndex + 1} pehle se kisi aur ke pas hai."
            return
        }

        currentSeats[seatIndex] = MicSeat(
            seatIndex = seatIndex,
            occupantUid = curUser.uid,
            occupantName = curUser.name,
            occupantAvatar = "",
            isMuted = false,
            isTalking = false,
            isLocked = false,
            giftScore = 0L,
            joinedAt = System.currentTimeMillis()
        )

        _activeRoom.value = _activeRoom.value.copy(seats = currentSeats)
        _userMessage.value = "🎙️ Mubarak! Aap Mic Seat ${seatIndex + 1} par live aa gaye!"
    }

    fun leaveMicSeat(seatIndex: Int) {
        val curUser = currentUser.value ?: return
        val currentSeats = _activeRoom.value.seats.toMutableList()

        if (seatIndex !in 0 until AudioRoom.MAX_MIC_SEATS) return
        val seat = currentSeats[seatIndex]

        if (seat.occupantUid == curUser.uid || curUser.role == UserRole.OWNER || curUser.uid == _activeRoom.value.hostUid) {
            currentSeats[seatIndex] = MicSeat(seatIndex = seatIndex, isLocked = seat.isLocked)
            _activeRoom.value = _activeRoom.value.copy(seats = currentSeats)
            _userMessage.value = "Aap Mic Seat ${seatIndex + 1} se audience mein wapis aagaye."
        }
    }

    fun toggleMicSeatMute(seatIndex: Int) {
        val curUser = currentUser.value ?: return
        val currentSeats = _activeRoom.value.seats.toMutableList()

        if (seatIndex !in 0 until AudioRoom.MAX_MIC_SEATS) return
        val seat = currentSeats[seatIndex]

        if (seat.occupantUid == curUser.uid || curUser.role == UserRole.OWNER || curUser.uid == _activeRoom.value.hostUid) {
            val newMute = !seat.isMuted
            currentSeats[seatIndex] = seat.copy(isMuted = newMute)
            _activeRoom.value = _activeRoom.value.copy(seats = currentSeats)
            _userMessage.value = if (newMute) "Mic Muted 🔇" else "Mic Live & Unmuted 🎙️"
        }
    }

    fun toggleSeatLock(seatIndex: Int) {
        if (seatIndex !in 0 until AudioRoom.MAX_MIC_SEATS) return
        val curUser = currentUser.value
        val isHostOrOwner = curUser == null || curUser.role == UserRole.OWNER || curUser.uid == _activeRoom.value.hostUid
        if (isHostOrOwner) {
            val currentSeats = _activeRoom.value.seats.toMutableList()
            val seat = currentSeats[seatIndex]
            val newLock = !seat.isLocked
            currentSeats[seatIndex] = seat.copy(isLocked = newLock)
            _activeRoom.value = _activeRoom.value.copy(seats = currentSeats)
            _userMessage.value = if (newLock) "🔒 Seat ${seatIndex + 1} Locked" else "🔓 Seat ${seatIndex + 1} Unlocked"
        } else {
            _userMessage.value = "Sirf Host ya Admin hi Seat lock/unlock kar sakta hai."
        }
    }

    fun updateUserProfile(name: String, phoneNumber: String, profilePicture: String = "") {
        val curUser = _currentUser.value
        val updatedUser = (curUser ?: UserEntity(
            uid = "user_guest",
            phoneNumber = phoneNumber,
            email = "",
            name = name,
            profilePicture = profilePicture,
            role = UserRole.BUYER
        )).copy(
            name = name,
            phoneNumber = phoneNumber,
            profilePicture = if (profilePicture.isNotBlank()) profilePicture else (curUser?.profilePicture ?: "")
        )
        _currentUser.value = updatedUser
        viewModelScope.launch {
            try {
                repository.updateUserProfile(updatedUser.uid, name, phoneNumber, profilePicture)
            } catch (e: Exception) {
                // Ignore local fallback error
            }
        }
        _userMessage.value = "Profile updated: $name ✅"
    }

    // Room Themes State (Cosmic Earth & Moon is 1st Free Default theme from uploaded photo)
    private val _unlockedThemes = MutableStateFlow<Set<String>>(setOf("cosmic_earth", "dark_slate"))
    val unlockedThemes: StateFlow<Set<String>> = _unlockedThemes.asStateFlow()

    fun updateRoomSettings(
        newTitle: String = "",
        newCountryFlag: String = "",
        newDpUrl: String = "",
        newBannerUrl: String = "",
        newAnnouncement: String = "",
        newThemeId: String = ""
    ) {
        val current = _activeRoom.value
        val updated = current.copy(
            title = if (newTitle.isNotBlank()) newTitle else current.title,
            countryFlag = if (newCountryFlag.isNotBlank()) newCountryFlag else current.countryFlag,
            hostAvatar = if (newDpUrl.isNotBlank()) newDpUrl else current.hostAvatar,
            roomDpUrl = if (newDpUrl.isNotBlank()) newDpUrl else current.roomDpUrl,
            roomBannerUrl = if (newBannerUrl.isNotBlank()) newBannerUrl else current.roomBannerUrl,
            announcement = if (newAnnouncement.isNotBlank()) newAnnouncement else current.announcement,
            themeId = if (newThemeId.isNotBlank()) newThemeId else current.themeId
        )
        _activeRoom.value = updated
        val list = _allAudioRooms.value.toMutableList()
        val idx = list.indexOfFirst { it.roomId == updated.roomId }
        if (idx != -1) {
            list[idx] = updated
            _allAudioRooms.value = list
        }
        _userMessage.value = "Room settings updated successfully! 👑"
    }

    fun purchaseAndApplyTheme(themeId: String, cost: Long) {
        if (_unlockedThemes.value.contains(themeId) || cost == 0L) {
            updateRoomSettings(newThemeId = themeId)
            _userMessage.value = "🎨 Room Theme applied!"
            return
        }
        if (_coinBalance.value < cost) {
            _userMessage.value = "❌ Insufficient coins! This theme requires $cost Coins."
            return
        }
        _coinBalance.value -= cost
        _unlockedThemes.value = _unlockedThemes.value + themeId
        updateRoomSettings(newThemeId = themeId)
        _userMessage.value = "🎨 Luxury Room Theme unlocked and applied! (-$cost Coins)"
    }

    // Real-time VIP Video Daily Quota: 12 videos/day for Room VIP, 15 sec max, auto-deletes in 1 hour
    private val _vipVideosUploadedToday = MutableStateFlow(3)
    val vipVideosUploadedToday: StateFlow<Int> = _vipVideosUploadedToday.asStateFlow()

    fun uploadVipHighlightVideo(title: String = "15-Sec Voice & Highlight") {
        if (_vipVideosUploadedToday.value >= 12) {
            _userMessage.value = "⚠️ Daily VIP Quota reached (12/12)! Kal dobara upload karein."
            return
        }
        _vipVideosUploadedToday.value += 1
        _userMessage.value = "🎥 VIP Video highlight '$title' posted! (${_vipVideosUploadedToday.value}/12 Today • 1-hr auto-delete)"
    }

    fun canUserPayout(user: UserEntity? = currentUser.value): Boolean {
        val curUser = user ?: currentUser.value ?: return false
        return curUser.role == UserRole.OWNER || curUser.uid == _activeRoom.value.hostUid
    }

    // VIP Subscriptions: Type 1 = ROOM VIP ($100/mo, 12 videos/day, max 15s, 1-hr delete), Type 2 = SELLER VIP (3 PIV tag media, 5 videos/day on home)
    private val _isRoomVip = MutableStateFlow(true) // default unlocked for demo/testing
    val isRoomVip: StateFlow<Boolean> = _isRoomVip.asStateFlow()

    private val _isSellerVip = MutableStateFlow(true)
    val isSellerVip: StateFlow<Boolean> = _isSellerVip.asStateFlow()

    fun subscribeRoomVip() {
        _isRoomVip.value = true
        _userMessage.value = "🌟 Room VIP Active ($100/mo)! Daily 12 videos (15 sec, 1-hr auto-delete) post kar sakte hain!"
    }

    fun subscribeSellerVip() {
        _isSellerVip.value = true
        _userMessage.value = "💎 Seller VIP Active! 3 PIV Tag Photos/Videos aur Daily 5 Home Videos quota unlock ho gaya!"
    }

    private val _giftRedemptions = MutableStateFlow<List<PhysicalGiftRedemption>>(
        listOf(
            PhysicalGiftRedemption(
                itemTitle = "Pakistani Designer Silk Bridal Suit 👗",
                diamondsSpent = 15000L,
                recipientName = "Ayesha Khan",
                phone = "0301-7654321",
                city = "Lahore",
                fullAddress = "House 14, Block C, Gulberg III, Lahore",
                status = "Dispatched via DKK Express 🚚"
            )
        )
    )
    val giftRedemptions: StateFlow<List<PhysicalGiftRedemption>> = _giftRedemptions.asStateFlow()

    fun sendLuxuryGift(targetRecipient: String = "Host", giftId: String, giftName: String, coinPrice: Long) {
        val curUser = currentUser.value ?: return
        if (_coinBalance.value < coinPrice) {
            _userMessage.value = "❌ Coins kam hain! Coin Shop se mazeed coins hasil karein."
            return
        }

        // Deduct sender coins
        _coinBalance.value -= coinPrice
        // Recipient gets 30% value in Diamonds
        val diamondsEarned = (coinPrice * 0.30).toLong().coerceAtLeast(1L)
        _diamondBalance.value += diamondsEarned

        _activeRoom.value = _activeRoom.value.copy(
            totalCoinsGifted = _activeRoom.value.totalCoinsGifted + coinPrice
        )

        val banner = "${curUser.name} ne $targetRecipient ko $giftName ($coinPrice Coins) gift kiya! 🎁✨ (30% Share: $diamondsEarned Diamonds)"
        _latestGiftAnimation.value = banner
        _userMessage.value = banner
    }

    fun sendLuxuryGift(giftId: String, giftName: String, coinPrice: Long) {
        sendLuxuryGift(targetRecipient = "Host", giftId = giftId, giftName = giftName, coinPrice = coinPrice)
    }

    fun clearGiftAnimation() {
        _latestGiftAnimation.value = null
    }

    // Pakistan Coin Purchase: 5,000 PKR = 5,000 Coins (1 PKR = 1 Coin)
    fun topUpCoinsPakistan(coinAmount: Long, pkrAmount: Long, paymentMethod: String = "Easypaisa / JazzCash") {
        _coinBalance.value += coinAmount
        _userMessage.value = "🪙 Pakistan Top-Up Kamyab! $coinAmount Coins wallet mein shamil! (Rs $pkrAmount - $paymentMethod) [Rate: 1 PKR = 1 Coin]"
    }

    fun topUpCoinsPakistan(coinAmount: Long, pkrAmount: Double, paymentMethod: String = "Easypaisa / JazzCash") {
        topUpCoinsPakistan(coinAmount, pkrAmount.toLong(), paymentMethod)
    }

    // Overseas / Website Coin Purchase: $50 USD = 5,000 Coins ($10 = 1000, $50 = 5000, $100 = 10000)
    fun topUpCoinsOverseas(coinAmount: Long, usdAmount: Long, paymentMethod: String = "Website Direct Portal") {
        _coinBalance.value += coinAmount
        _userMessage.value = "🪙 Overseas Direct Top-Up Kamyab! $coinAmount Coins wallet mein shamil! ($$usdAmount USD - $paymentMethod) [Rate: $50 = 5,000 Coins]"
    }

    fun topUpCoinsOverseas(coinAmount: Long, usdAmount: Double, paymentMethod: String = "Website Direct Portal") {
        topUpCoinsOverseas(coinAmount, usdAmount.toLong(), paymentMethod)
    }

    fun topUpCoins(coinAmount: Long, pkrAmount: Double, paymentMethod: String = "Easypaisa / JazzCash") {
        topUpCoinsPakistan(coinAmount, pkrAmount.toLong(), paymentMethod)
    }

    // Friday to Friday 30% Diamond Cashout: ONLY Host or Room Admin authorized
    fun cashoutDiamonds(
        diamondAmount: Long,
        payoutMethod: String = "Friday Direct Transfer",
        accountNumber: String = "0327-3856001",
        user: UserEntity? = null,
        accountDetails: String? = null
    ) {
        val acc = accountDetails ?: accountNumber
        if (!canUserPayout(user)) {
            _userMessage.value = "❌ Payout sirf Room Host ya Admin kar sakte hain!"
            return
        }
        if (_diamondBalance.value < diamondAmount) {
            _userMessage.value = "❌ Diamonds kam hain! Maujuda balance: ${_diamondBalance.value}"
            return
        }
        // 30% Earning weekly Friday-to-Friday payout
        val pkrValue = diamondAmount // 1 Diamond = 1 PKR in 30% share
        _diamondBalance.value -= diamondAmount
        _userMessage.value = "💸 30% Host Payout: Rs $pkrValue $payoutMethod ($acc) par scheduled! (Friday to Friday Clearance Cycle)"
    }

    // Physical Luxury Gift Redemption (Dress, Makeup, Watch, etc. delivered in Pakistan)
    fun redeemDiamondsForPhysicalGift(
        itemTitle: String,
        diamondsCost: Long,
        recipientName: String,
        phone: String,
        city: String,
        fullAddress: String
    ) {
        if (_diamondBalance.value < diamondsCost) {
            _userMessage.value = "❌ Diamonds kam hain! Is gift ke liye $diamondsCost Diamonds darkar hain."
            return
        }
        _diamondBalance.value -= diamondsCost
        val redemption = PhysicalGiftRedemption(
            itemTitle = itemTitle,
            diamondsSpent = diamondsCost,
            recipientName = recipientName,
            phone = phone,
            city = city,
            fullAddress = fullAddress
        )
        _giftRedemptions.value = listOf(redemption) + _giftRedemptions.value
        _userMessage.value = "🎁 Mubarak! '$itemTitle' order ho gaya! Pakistan address ($city, $phone) par DKK Courier deliver karega."
    }

    fun redeemPhysicalGift(
        giftTitle: String = "",
        diamondCost: Long = 0L,
        recipientName: String,
        phone: String,
        city: String,
        fullAddress: String,
        itemTitle: String = giftTitle,
        diamondsCost: Long = diamondCost
    ) {
        val title = if (giftTitle.isNotBlank()) giftTitle else itemTitle
        val cost = if (diamondCost > 0L) diamondCost else diamondsCost
        redeemDiamondsForPhysicalGift(title, cost, recipientName, phone, city, fullAddress)
    }

    // Upload Pre-Recorded Brand Video (ROOM VIP: 12 videos/day, max 15s, auto-deletes in 1 hour; SELLER VIP: 5 videos/day on Home)
    fun uploadPreRecordedVideo(title: String, durationMinutes: Int, isVipTier: Boolean) {
        val maxUploads = if (isVipTier) 12 else 5
        _activeRoom.value = _activeRoom.value.copy(
            videoStreamUrl = "https://stream.cloudflare.com/vip_$title.m3u8",
            isVideoLoopActive = true
        )
        _userMessage.value = "🎥 Video '$title' Room VIP stream loop mein shamil ho gayi! (15-Sec Story, 1 Hour mein auto-delete hogi • Daily Quota: $maxUploads videos)"
    }
}

/**
 * Physical Luxury Gift Delivery record (e.g. Dress, Makeup, Watch)
 */
data class PhysicalGiftRedemption(
    val id: String = java.util.UUID.randomUUID().toString(),
    val itemTitle: String,
    val diamondsSpent: Long,
    val recipientName: String,
    val phone: String,
    val city: String,
    val fullAddress: String,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "Dispatching via DKK Courier 🚚"
)

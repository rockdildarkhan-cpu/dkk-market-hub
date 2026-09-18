package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.example.data.model.UserRole
import com.example.ui.components.DkkTopHeader
import com.example.ui.components.EasypaisaPaymentSheet
import com.example.ui.components.FaceScanVerificationSheet
import com.example.ui.components.PostProductSheet
import com.example.ui.components.RoleSwitchBottomSheet
import com.example.ui.components.SupportChatSheet
import com.example.ui.screens.AdminHomeScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.BuyerDashboardScreen
import com.example.ui.screens.DkkAudioRoomScreen
import com.example.ui.screens.OwnerAdminDashboardScreen
import com.example.ui.screens.ProductDetailSheet
import com.example.ui.screens.SellerDashboardScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.AppNavigationScreen
import com.example.viewmodel.DkkViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: DkkViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isDarkTheme by viewModel.isDarkTheme.collectAsState()
            MyApplicationTheme(darkTheme = isDarkTheme) {
                DkkMarketingApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun DkkMarketingApp(viewModel: DkkViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val selectedProduct by viewModel.selectedProduct.collectAsState()
    val showCheckout by viewModel.showCheckoutDialog.collectAsState()
    val showPostProduct by viewModel.showPostProductDialog.collectAsState()
    val showFaceScan by viewModel.showFaceScanDialog.collectAsState()
    val showRoleSwitch by viewModel.showRoleSwitchDialog.collectAsState()
    val showSupportChat by viewModel.showSupportChatDialog.collectAsState()
    val userMessage by viewModel.userMessage.collectAsState()

    // Data streams
    val allProducts by viewModel.allProducts.collectAsState()
    val filteredProducts by viewModel.filteredProducts.collectAsState()
    val proVipProducts by viewModel.proVipProducts.collectAsState()
    val sellerProfile by viewModel.currentSellerProfile.collectAsState()
    val sellerProducts by viewModel.sellerProducts.collectAsState()
    val buyerOrders by viewModel.buyerOrders.collectAsState()
    val sellerOrders by viewModel.sellerOrders.collectAsState()
    val allOrders by viewModel.allOrders.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()
    val allSellers by viewModel.allSellers.collectAsState()
    val adminStats by viewModel.adminOverviewStats.collectAsState()
    val pendingSellerApprovals by viewModel.pendingSellerApprovals.collectAsState()

    val pendingSellersLiveCount by viewModel.pendingSellersLiveCount.collectAsState()
    val pendingProductsLive by viewModel.pendingProductsLive.collectAsState()
    val pendingProductsLiveCount by viewModel.pendingProductsLiveCount.collectAsState()
    val activeTicketsLive by viewModel.activeTicketsLive.collectAsState()
    val activeTicketsLiveCount by viewModel.activeTicketsLiveCount.collectAsState()
    val totalPlatformEarningsLive by viewModel.totalPlatformEarningsLive.collectAsState()
    val isLoadingLiveStats by viewModel.isLoadingLiveStats.collectAsState()
    val ownerAlerts by viewModel.ownerAlerts.collectAsState()
    val activeAlertNotification by viewModel.activeAlertNotification.collectAsState()
    val allAudioRooms by viewModel.allAudioRooms.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(userMessage) {
        userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearUserMessage()
        }
    }

    AnimatedContent(
        targetState = currentScreen,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "main_screen_transition"
    ) { screen ->
        when (screen) {
            AppNavigationScreen.SPLASH -> {
                SplashScreen(
                    onContinue = {
                        viewModel.setScreen(AppNavigationScreen.AUTH)
                    }
                )
            }
            AppNavigationScreen.AUTH -> {
                AuthScreen(
                    onLoginSuccess = { phone, name, email, role ->
                        viewModel.loginOrRegister(phone, name, email, role)
                    },
                    onQuickPersonaSelected = { role ->
                        viewModel.switchUserPersona(role)
                        viewModel.setScreen(AppNavigationScreen.MAIN_APP)
                    }
                )
            }
            AppNavigationScreen.MAIN_APP -> {
                val activeRole = currentUser?.role ?: UserRole.BUYER

                Scaffold(
                    topBar = {
                        if (activeRole != UserRole.BUYER) {
                            DkkTopHeader(
                                currentRole = activeRole,
                                onRoleChange = { newRole ->
                                    viewModel.switchUserPersona(newRole)
                                },
                                userName = currentUser?.name ?: "User",
                                userPhone = currentUser?.phoneNumber ?: "",
                                isProVip = currentUser?.isProVip == true,
                                onOpenRoleDialog = {
                                    viewModel.openRoleSwitchDialog()
                                },
                                onOpenFirebase = null,
                                isDarkTheme = isDarkTheme,
                                onToggleTheme = {
                                    viewModel.toggleTheme()
                                },
                                onOpenSupportChat = if (activeRole != UserRole.OWNER) {
                                    { viewModel.openSupportChatDialog() }
                                } else null,
                                onLogout = { viewModel.logout() }
                            )
                        }
                    },
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        // Method 1: Single-App Role Check Router
                        when (activeRole) {
                            UserRole.BUYER -> {
                                BuyerDashboardScreen(
                                    searchQuery = searchQuery,
                                    onSearchQueryChange = { viewModel.setSearchQuery(it) },
                                    selectedCategory = selectedCategory,
                                    onCategorySelected = { viewModel.setSelectedCategory(it) },
                                    allProducts = filteredProducts,
                                    proVipProducts = proVipProducts,
                                    buyerOrders = buyerOrders,
                                    onProductSelected = { prod -> viewModel.selectProduct(prod) },
                                    onBuyNow = { prod -> viewModel.openCheckoutDialog(prod) },
                                    onOpenSupportChat = { viewModel.openSupportChatDialog() },
                                    onOpenRoleDialog = { viewModel.openRoleSwitchDialog() },
                                    onToggleTheme = { viewModel.toggleTheme() },
                                    isDarkTheme = isDarkTheme,
                                    userName = currentUser?.name ?: "Buyer",
                                    userPhone = currentUser?.phoneNumber ?: "",
                                    allAudioRooms = allAudioRooms,
                                    onSelectRoom = { room -> viewModel.selectAndOpenRoom(room) },
                                    onCreateRoom = { title, videoUrl, flag, name -> viewModel.createNewRoom(title, videoUrl, flag, name) },
                                    onLogout = { viewModel.logout() },
                                    onRequestReturn = { orderId, reason, proofUrl, fileSizeBytes ->
                                        viewModel.requestOrderReturn(orderId, reason, proofUrl, fileSizeBytes)
                                    }
                                )
                            }
                            UserRole.SELLER -> {
                                SellerDashboardScreen(
                                    user = currentUser ?: return@Box,
                                    sellerProfile = sellerProfile,
                                    sellerProducts = sellerProducts,
                                    sellerOrders = sellerOrders,
                                    onOpenPostProduct = { viewModel.openPostProductDialog() },
                                    onOpenFaceScan = { viewModel.openFaceScanDialog() },
                                    onDispatchOrder = { orderId -> viewModel.dispatchOrder(orderId) },
                                    onDeliverOrderWithOtp = { orderId, otp -> viewModel.verifyAndDeliverOrder(orderId, otp) },
                                    onOpenSupportChat = { viewModel.openSupportChatDialog() },
                                    onUpdatePayoutDetails = { bankName, title, accountNum, iban, epNumber, epTitle ->
                                        viewModel.updateSellerPayoutDetails(bankName, title, accountNum, iban, epNumber, epTitle)
                                    },
                                    onLogout = { viewModel.logout() },
                                    onOpenRoleDialog = { viewModel.openRoleSwitchDialog() },
                                    onOpenAudioRoom = { viewModel.openAudioRoom() },
                                    allAudioRooms = allAudioRooms,
                                    onSelectRoom = { room -> viewModel.selectAndOpenRoom(room) },
                                    onCreateRoom = { title, videoUrl, flag, name -> viewModel.createNewRoom(title, videoUrl, flag, name) }
                                )
                            }
                            UserRole.OWNER -> {
                                AdminHomeScreen(
                                    stats = adminStats,
                                    allUsers = allUsers,
                                    pendingSellers = pendingSellerApprovals,
                                    allOrders = allOrders,
                                    allSellersList = allSellers,
                                    onApproveSeller = { sellerId -> viewModel.approveSeller(sellerId) },
                                    onRejectSeller = { sellerId -> viewModel.rejectSeller(sellerId) },
                                    onToggleProVip = { uid, enable -> viewModel.toggleProVip(uid, enable) },
                                    onClearSellerFridayPayout = { sellerId, amount -> viewModel.clearSellerFridayPayout(sellerId, amount) },
                                    onClearAllFridayPayouts = { totalAmount -> viewModel.clearAllFridayPayouts(totalAmount) },
                                    onOpenFirebase = null,
                                    pendingSellersLiveCount = pendingSellersLiveCount,
                                    pendingProductsLive = pendingProductsLive,
                                    pendingProductsLiveCount = pendingProductsLiveCount,
                                    activeTicketsLive = activeTicketsLive,
                                    activeTicketsLiveCount = activeTicketsLiveCount,
                                    totalPlatformEarningsLive = totalPlatformEarningsLive,
                                    onApproveProductLive = { prodId -> viewModel.approveProductLive(prodId) },
                                    onRejectProductLive = { prodId -> viewModel.rejectProductLive(prodId) },
                                    isLoadingLiveStats = isLoadingLiveStats,
                                    activeAlert = activeAlertNotification,
                                    recentAlerts = ownerAlerts,
                                    onDismissAlert = { alertId -> viewModel.dismissAlert(alertId) },
                                    onDismissActiveAlert = { viewModel.dismissActiveAlert() },
                                    onSimulateHighValueProduct = { viewModel.simulateHighValueProductAlert() },
                                    onSimulateUrgentTicket = { viewModel.simulateUrgentTicketAlert() },
                                    viewModel = viewModel
                                )
                            }
                        }
                    }
                }

                // Modals & BottomSheets
                if (selectedProduct != null && !showCheckout) {
                    ProductDetailSheet(
                        product = selectedProduct!!,
                        onDismiss = { viewModel.selectProduct(null) },
                        onBuyNow = { viewModel.openCheckoutDialog(selectedProduct!!) }
                    )
                }

                if (showCheckout && selectedProduct != null) {
                    EasypaisaPaymentSheet(
                        product = selectedProduct!!,
                        buyerPhone = currentUser?.phoneNumber ?: "03273856001",
                        onDismiss = { viewModel.closeCheckoutDialog() },
                        onPaymentSuccess = { paymentRef ->
                            viewModel.processEasypaisaPayment(selectedProduct!!, paymentRef)
                        }
                    )
                }

                if (showPostProduct) {
                    PostProductSheet(
                        sellerId = currentUser?.uid ?: "SELLER_001",
                        sellerName = sellerProfile?.businessName ?: (currentUser?.name ?: "Seller"),
                        onDismiss = { viewModel.closePostProductDialog() },
                        onSubmit = { title, desc, price, cat, isVip, imgUrl, isFreeDelivery, deliveryFee ->
                            viewModel.postNewProduct(title, desc, price, cat, isVip, imgUrl, isFreeDelivery, deliveryFee)
                        }
                    )
                }

                if (showFaceScan) {
                    FaceScanVerificationSheet(
                        sellerName = currentUser?.name ?: "Seller",
                        onDismiss = { viewModel.closeFaceScanDialog() },
                        onVerificationComplete = { viewModel.completeFaceScan() }
                    )
                }

                if (showRoleSwitch) {
                    RoleSwitchBottomSheet(
                        currentRole = activeRole,
                        onDismiss = { viewModel.closeRoleSwitchDialog() },
                        onRoleSelected = { newRole ->
                            viewModel.switchUserPersona(newRole)
                        },
                        onLogout = { viewModel.logout() }
                    )
                }

                if (showSupportChat) {
                    SupportChatSheet(
                        userName = currentUser?.name ?: "Valued User",
                        userPhone = currentUser?.phoneNumber ?: "",
                        userEmail = currentUser?.email ?: "user@dkkmarketing.com",
                        userRole = activeRole.name,
                        onDismiss = { viewModel.closeSupportChatDialog() },
                        onSubmitReport = { subject, message, priority, screenshotUrl ->
                            viewModel.submitSupportTicket(
                                subject = subject,
                                email = currentUser?.email ?: "user@dkkmarketing.com",
                                role = activeRole.name,
                                message = message,
                                priority = priority,
                                senderName = currentUser?.name ?: "User",
                                senderPhone = currentUser?.phoneNumber ?: "",
                                screenshotUrl = screenshotUrl
                            )
                            viewModel.closeSupportChatDialog()
                        }
                    )
                }
            }
            AppNavigationScreen.AUDIO_ROOM -> {
                DkkAudioRoomScreen(
                    viewModel = viewModel,
                    currentUser = currentUser,
                    onCloseRoom = { viewModel.closeAudioRoom() }
                )
            }
        }
    }
}


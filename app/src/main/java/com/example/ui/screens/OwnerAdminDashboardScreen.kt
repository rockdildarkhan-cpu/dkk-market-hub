package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import com.example.data.model.DkkOfficialBankAccount
import com.example.data.model.DkkOfficialEasypaisaAccount
import com.example.data.model.DeliveryStatus
import com.example.data.repository.CommissionCalculator
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.AdminOverviewStats
import com.example.data.model.OrderEntity
import com.example.data.model.OwnerAlertNotification
import com.example.data.model.OwnerAlertType
import com.example.data.model.PendingProductItem
import com.example.data.model.ReceiptVaultItem
import com.example.data.model.SellerProfileEntity
import com.example.data.model.SupportTicketItem
import com.example.data.model.getTicketPriorityWeight
import com.example.data.model.UserEntity
import com.example.data.model.UserRole
import com.example.data.model.resolveProductImageModel
import com.example.data.security.ReceiptsVaultSecurityGuard
import com.example.ui.components.ProVipBadge
import com.example.ui.components.SellerLevelBadge
import com.example.ui.components.TierCommissionBadge
import com.example.ui.components.formatPkr
import com.example.ui.theme.DkkEmerald
import com.example.ui.theme.DkkEmeraldLight
import com.example.ui.theme.DkkGold
import com.example.ui.theme.DkkNavy
import com.example.ui.theme.DkkSlate
import com.example.ui.theme.EasypaisaGreen
import com.example.ui.theme.Tier1Color
import com.example.ui.theme.Tier2Color
import com.example.ui.theme.Tier3Color
import com.example.ui.theme.Tier4Color
import java.util.Locale
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Person
import com.example.viewmodel.DkkViewModel
import com.example.viewmodel.PhysicalGiftRedemption
import com.example.data.firebase.AudioRoom
import com.example.data.firebase.MicSeat

@Composable
fun OwnerAdminDashboardScreen(
    stats: AdminOverviewStats,
    allUsers: List<UserEntity>,
    pendingSellers: List<SellerProfileEntity>,
    allOrders: List<OrderEntity>,
    onApproveSeller: (sellerId: String) -> Unit,
    onRejectSeller: (sellerId: String) -> Unit,
    onToggleProVip: (uid: String, enable: Boolean) -> Unit,
    onOpenFirebase: (() -> Unit)? = null,
    pendingSellersLiveCount: Int = 0,
    pendingProductsLive: List<PendingProductItem> = emptyList(),
    pendingProductsLiveCount: Int = 0,
    activeTicketsLive: List<SupportTicketItem> = emptyList(),
    activeTicketsLiveCount: Int = 0,
    totalPlatformEarningsLive: Double = 0.0,
    onApproveProductLive: (productId: String) -> Unit = {},
    onRejectProductLive: (productId: String) -> Unit = {},
    isLoadingLiveStats: Boolean = false,
    activeAlert: OwnerAlertNotification? = null,
    recentAlerts: List<OwnerAlertNotification> = emptyList(),
    onDismissAlert: (String) -> Unit = {},
    onDismissActiveAlert: () -> Unit = {},
    onSimulateHighValueProduct: () -> Unit = {},
    onSimulateUrgentTicket: () -> Unit = {},
    receiptsVaultItems: List<ReceiptVaultItem> = emptyList(),
    onVerifyReceiptSlip: (String) -> Unit = {},
    onRejectReceiptSlip: (String) -> Unit = {},
    allSellersList: List<SellerProfileEntity> = emptyList(),
    onClearSellerFridayPayout: (sellerId: String, amount: Double) -> Unit = { _, _ -> },
    onClearAllFridayPayouts: (totalAmount: Double) -> Unit = {},
    viewModel: DkkViewModel? = null
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Everything Clear, 1: Rooms & Coins, 2: Friday Payouts, 3: KYC, 4: Audit, 5: Live Desk

    val topActiveSellers = remember(allOrders, allUsers) {
        computeTopActiveSellers(allOrders, allUsers)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // 🔔 REAL-TIME NOTIFICATION ALERT BANNER FOR OWNER (ADMIN HOME SCREEN)
        AnimatedVisibility(
            visible = activeAlert != null,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
        ) {
            if (activeAlert != null) {
                OwnerAlertBanner(
                    alert = activeAlert,
                    onDismiss = onDismissActiveAlert,
                    onApprove = {
                        if (activeAlert.type == OwnerAlertType.HIGH_VALUE_PRODUCT) {
                            onApproveProductLive(activeAlert.referenceId)
                        }
                        onDismissActiveAlert()
                    }
                )
            }
        }

        // Tab Navigation
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = DkkNavy,
            contentColor = DkkGold,
            edgePadding = 8.dp
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("👑 Overview", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                modifier = Modifier.testTag("admin_tab_everything_clear")
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("🎙️ Room & Coins", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                modifier = Modifier.testTag("admin_tab_audio_rooms")
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("🗓️ Friday Payouts", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                modifier = Modifier.testTag("admin_tab_friday_payouts")
            )
            Tab(
                selected = selectedTab == 3,
                onClick = { selectedTab = 3 },
                text = { Text("KYC (${pendingSellers.size})", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                modifier = Modifier.testTag("admin_tab_approvals")
            )
            Tab(
                selected = selectedTab == 4,
                onClick = { selectedTab = 4 },
                text = { Text("Audit (${allOrders.size})", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                modifier = Modifier.testTag("admin_tab_stream")
            )
            Tab(
                selected = selectedTab == 5,
                onClick = { selectedTab = 5 },
                text = { Text("🦅 Live Desk", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                modifier = Modifier.testTag("admin_tab_owner_desk")
            )
            Tab(
                selected = selectedTab == 6,
                onClick = { selectedTab = 6 },
                text = { Text("📊 Commission & Analytics", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                modifier = Modifier.testTag("admin_tab_commission_analytics")
            )
        }

        when (selectedTab) {
            0 -> {
                // Section 4: Owner Dashboard "Everything Clear" UI Layout (Live Master Control)
                EverythingClearOwnerView(
                    stats = stats,
                    allUsers = allUsers,
                    pendingSellers = pendingSellers,
                    allOrders = allOrders,
                    onApproveSeller = onApproveSeller,
                    onRejectSeller = onRejectSeller,
                    onToggleProVip = onToggleProVip,
                    pendingProductsLive = pendingProductsLive,
                    onApproveProductLive = onApproveProductLive,
                    onRejectProductLive = onRejectProductLive,
                    totalPlatformEarningsLive = totalPlatformEarningsLive,
                    topActiveSellers = topActiveSellers,
                    activeTicketsLive = activeTicketsLive,
                    receiptsVaultItems = receiptsVaultItems,
                    onVerifyReceiptSlip = onVerifyReceiptSlip,
                    onRejectReceiptSlip = onRejectReceiptSlip,
                    onOpenCommissionAnalytics = { selectedTab = 6 }
                )
            }
            1 -> {
                // Section: Live Audio Rooms, Stage System & Coin Monetization Engine
                AudioRoomsAndCoinsOwnerSystemView(
                    viewModel = viewModel,
                    stats = stats
                )
            }
            2 -> {
                // Section: Friday Payout Clearance System (Baqi Rs & Clearance List)
                FridayPayoutClearanceView(
                    allSellers = if (allSellersList.isNotEmpty()) allSellersList else pendingSellers,
                    allUsers = allUsers,
                    allOrders = allOrders,
                    onClearSellerFridayPayout = onClearSellerFridayPayout,
                    onClearAllFridayPayouts = onClearAllFridayPayouts
                )
            }
            3 -> {
                // Section 2: Approval Queue Control Block (Seller KYC & Face Scans)
                if (pendingSellers.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = DkkEmerald, modifier = Modifier.size(56.dp))
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("Approval Queue Empty", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                            Text("No pending KYC or face scans to review.", fontSize = 12.sp, color = Color(0xFF94A3B8))
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(pendingSellers) { seller ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = DkkSlate),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, DkkGold.copy(alpha = 0.3f)),
                                modifier = Modifier.fillMaxWidth().testTag("pending_seller_${seller.uid}")
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Icon(Icons.Default.Face, contentDescription = null, tint = DkkGold, modifier = Modifier.size(24.dp))
                                            Column {
                                                Text(seller.businessName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                                                Text("UID: ${seller.uid.take(10)}", fontSize = 10.sp, color = Color(0xFF94A3B8))
                                            }
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = DkkEmerald.copy(alpha = 0.15f),
                                            border = BorderStroke(1.dp, DkkEmerald)
                                        ) {
                                            Text(
                                                text = "Face Scan OK",
                                                color = DkkEmerald,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Text(
                                        text = "Biometrics verified via camera scan. Ready for Owner authorization.",
                                        fontSize = 11.sp,
                                        color = Color(0xFF94A3B8)
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = { onRejectSeller(seller.uid) },
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.weight(1f).testTag("reject_seller_${seller.uid}")
                                        ) {
                                            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Reject")
                                        }

                                        Button(
                                            onClick = { onApproveSeller(seller.uid) },
                                            colors = ButtonDefaults.buttonColors(containerColor = DkkEmerald),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.weight(1.5f).testTag("approve_seller_${seller.uid}")
                                        ) {
                                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Approve Seller", fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            4 -> {
                // Section 3: Live Commission & Order Audit Stream
                var auditFilter by remember { mutableStateOf(0) } // 0: All, 1: Delivered, 2: In-Transit, 3: Pending
                val filteredOrders = remember(allOrders, auditFilter) {
                    when (auditFilter) {
                        1 -> allOrders.filter { it.deliveryStatus == DeliveryStatus.DELIVERED }
                        2 -> allOrders.filter { it.deliveryStatus == DeliveryStatus.DISPATCHED }
                        3 -> allOrders.filter { it.deliveryStatus == DeliveryStatus.PENDING }
                        else -> allOrders
                    }
                }
                val totalAuditVolume = remember(allOrders) { allOrders.sumOf { it.orderAmount } }
                val totalAuditCommission = remember(allOrders) { allOrders.sumOf { it.commissionAmount } }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        DkkOfficialBankAccountCard()
                    }

                    // Audit Overview Metrics Card
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = DkkSlate),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, DkkGold.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "📊 LIVE AUDIT STREAM & FINANCIAL INTEGRITY",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = DkkGold,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = DkkNavy,
                                        border = BorderStroke(1.dp, Color(0xFF2E3D66)),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Text("Total Audited Orders", color = Color(0xFF94A3B8), fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text("${allOrders.size}", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Black)
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = DkkNavy,
                                        border = BorderStroke(1.dp, Color(0xFF2E3D66)),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Text("Commission Secured", color = DkkGold, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(formatPkr(totalAuditCommission), color = DkkGold, fontSize = 16.sp, fontWeight = FontWeight.Black)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Horizontal Filter Chips
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = auditFilter == 0,
                                onClick = { auditFilter = 0 },
                                label = { Text("All Orders (${allOrders.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                            )
                            FilterChip(
                                selected = auditFilter == 1,
                                onClick = { auditFilter = 1 },
                                label = { Text("Delivered (${allOrders.count { it.deliveryStatus == DeliveryStatus.DELIVERED }})", fontSize = 11.sp) }
                            )
                            FilterChip(
                                selected = auditFilter == 2,
                                onClick = { auditFilter = 2 },
                                label = { Text("In-Transit", fontSize = 11.sp) }
                            )
                            FilterChip(
                                selected = auditFilter == 3,
                                onClick = { auditFilter = 3 },
                                label = { Text("Pending / Paid", fontSize = 11.sp) }
                            )
                        }
                    }

                    if (filteredOrders.isEmpty()) {
                        item {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = DkkSlate,
                                border = BorderStroke(1.dp, Color(0xFF2E3D66)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(36.dp))
                                    Text("No Orders In This Filter", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("Real-time orders will appear here automatically with commission calculations.", color = Color(0xFF94A3B8), fontSize = 11.sp, textAlign = TextAlign.Center)
                                }
                            }
                        }
                    } else {
                        items(filteredOrders) { ord ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = DkkSlate),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, Color(0xFF2E3D66)),
                                modifier = Modifier.fillMaxWidth().testTag("order_stream_${ord.orderId}")
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Order #${ord.orderId.takeLast(6)}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = Color.White
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = DkkGold.copy(alpha = 0.15f),
                                            border = BorderStroke(1.dp, DkkGold.copy(alpha = 0.5f))
                                        ) {
                                            Text(
                                                text = CommissionCalculator.getTierName(ord.orderAmount),
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = DkkGold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Buyer: ${ord.buyerId.take(10)} • Seller: ${ord.sellerId.take(10)} • Status: ${ord.deliveryStatus.name}",
                                        fontSize = 11.sp,
                                        color = Color(0xFF94A3B8)
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Order Vol: ${formatPkr(ord.orderAmount)}", fontSize = 12.sp, color = Color.White)
                                        Text(
                                            text = "+ Comm: ${formatPkr(ord.commissionAmount)}",
                                            fontWeight = FontWeight.Black,
                                            fontSize = 13.sp,
                                            color = DkkGold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            5 -> {
                // Section 0: Exact React Native DKK Owner Desk Live Dashboard
                DkkOwnerDeskView(
                    totalPlatformEarnings = if (totalPlatformEarningsLive > 0.0) totalPlatformEarningsLive else stats.commissionBreakdown.totalCommissionRevenue,
                    pendingSellersCount = if (pendingSellersLiveCount > 0) pendingSellersLiveCount else pendingSellers.size,
                    pendingProductsCount = pendingProductsLiveCount,
                    activeTicketsCount = activeTicketsLiveCount,
                    pendingProducts = pendingProductsLive,
                    activeTickets = activeTicketsLive,
                    onApproveProduct = onApproveProductLive,
                    onRejectProduct = onRejectProductLive,
                    onOpenFirebase = onOpenFirebase,
                    isLoading = isLoadingLiveStats,
                    recentAlerts = recentAlerts,
                    onDismissAlert = onDismissAlert,
                    onClearAllAlerts = { onDismissAlert("CLEAR_ALL") },
                    onSimulateHighValueProduct = onSimulateHighValueProduct,
                    onSimulateUrgentTicket = onSimulateUrgentTicket,
                    allOrders = allOrders,
                    allUsers = allUsers
                )
            }
            6 -> {
                // Section 6: Protected Owner Admin View (Global Commission Toggles & Recharts Multi-Seller Sales Analytics)
                if (viewModel != null) {
                    OwnerCommissionAndSalesAnalyticsScreen(
                        viewModel = viewModel,
                        allOrders = allOrders,
                        allSellers = if (allSellersList.isNotEmpty()) allSellersList else pendingSellers,
                        allUsers = allUsers
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("Connecting to ViewModel...", color = Color.White)
                    }
                }
            }
        }
    }
}
@Composable
fun EverythingClearOwnerView(
    stats: AdminOverviewStats,
    allUsers: List<UserEntity>,
    pendingSellers: List<SellerProfileEntity>,
    allOrders: List<OrderEntity>,
    onApproveSeller: (sellerId: String) -> Unit,
    onRejectSeller: (sellerId: String) -> Unit,
    onToggleProVip: (uid: String, enable: Boolean) -> Unit,
    pendingProductsLive: List<PendingProductItem>,
    onApproveProductLive: (productId: String) -> Unit,
    onRejectProductLive: (productId: String) -> Unit = {},
    totalPlatformEarningsLive: Double,
    topActiveSellers: List<ActiveSellerContributor>,
    activeTicketsLive: List<SupportTicketItem> = emptyList(),
    receiptsVaultItems: List<ReceiptVaultItem> = emptyList(),
    onVerifyReceiptSlip: (String) -> Unit = {},
    onRejectReceiptSlip: (String) -> Unit = {},
    onOpenCommissionAnalytics: (() -> Unit)? = null
) {
    var approvalSectionTab by remember { mutableStateOf(0) } // 0: Seller Face Scans, 1: Receipts Vault (receipts_vault/), 2: Products, 3: Reports & Support
    var previewSlipScreenshot by remember { mutableStateOf<ReceiptVaultItem?>(null) }
    val totalCommissions = if (totalPlatformEarningsLive > 0.0) totalPlatformEarningsLive else stats.commissionBreakdown.totalCommissionRevenue
    val totalCashflow = totalCommissions + stats.proVipRevenue
    val pendingOrdersCount = allOrders.count { it.deliveryStatus != DeliveryStatus.DELIVERED && it.deliveryStatus != DeliveryStatus.CANCELLED }
    val deliveredOrdersCount = allOrders.count { it.deliveryStatus == DeliveryStatus.DELIVERED }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("everything_clear_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ==========================================
        // 1. TOP HEADER WELCOME BLOCK (Section 4, Item 1)
        // ==========================================
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DkkSlate),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, DkkGold.copy(alpha = 0.4f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("top_header_welcome_block")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(DkkNavy, CircleShape)
                                    .border(1.5.dp, DkkGold, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.AdminPanelSettings,
                                    contentDescription = "Owner Icon",
                                    tint = DkkGold,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "Daro Khan",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 18.sp,
                                        color = Color.White
                                    )
                                    // Verified Green Tick
                                    Box(
                                        modifier = Modifier
                                            .size(18.dp)
                                            .background(DkkEmerald, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = "Verified Owner",
                                            tint = Color.White,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = DkkGold,
                                        modifier = Modifier.padding(start = 2.dp)
                                    ) {
                                        Text(
                                            text = "OWNER",
                                            color = DkkNavy,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 9.sp,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "Daro Khan • Master Escrow & Platform Administrator",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = DkkEmerald.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, DkkEmerald)
                        ) {
                            Text(
                                text = "LIVE ONLINE",
                                color = DkkEmerald,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Active Payment Channels Display (JS Bank & Easypaisa Labels)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // JS Bank Active Badge
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = DkkNavy,
                            border = BorderStroke(1.dp, DkkEmerald.copy(alpha = 0.6f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(DkkEmerald, CircleShape)
                                )
                                Column {
                                    Text(
                                        text = "JS Bank: ****5314",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Active Escrow ✓",
                                        fontSize = 9.sp,
                                        color = DkkGold,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        // Easypaisa Active Badge
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = DkkNavy,
                            border = BorderStroke(1.dp, DkkEmerald.copy(alpha = 0.6f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(DkkEmerald, CircleShape)
                                )
                                Column {
                                    Text(
                                        text = "Easypaisa: 03273856001",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Active Raast ✓",
                                        fontSize = 9.sp,
                                        color = DkkEmeraldLight,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // 1.5 QUICK ACCESS TO PROTECTED COMMISSION & RECHARTS ANALYTICS
        // ==========================================
        if (onOpenCommissionAnalytics != null) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DkkNavy),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.5.dp, Color(0xFF38BDF8)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenCommissionAnalytics() }
                        .testTag("btn_open_commission_analytics_banner")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(Color(0xFF0F172A), CircleShape)
                                    .border(1.dp, Color(0xFF38BDF8), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.TrendingUp,
                                    contentDescription = null,
                                    tint = Color(0xFF38BDF8),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        "Global Commission & Sales Analytics",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color.White
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = Color(0xFF22C55E).copy(alpha = 0.2f),
                                        border = BorderStroke(0.5.dp, Color(0xFF22C55E))
                                    ) {
                                        Text("Recharts", color = Color(0xFF22C55E), fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                    }
                                }
                                Text(
                                    "Toggle commission rules & inspect multi-seller charts",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Button(
                            onClick = { onOpenCommissionAnalytics() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8), contentColor = DkkNavy),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Open 📊", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // ==========================================
        // 2. THE MASTER VAULT CARD (Section 4, Item 2)
        // Background: Deep Navy on Royal Blue box (#1C2541), 12.dp rounded, border VIP Gold (#FFD700)
        // Total cashflow in VIP Gold large bold font
        // ==========================================
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DkkSlate),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.5.dp, DkkGold),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("master_vault_card")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                tint = DkkGold,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "THE MASTER VAULT",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = DkkGold.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, DkkGold)
                        ) {
                            Text(
                                text = "100% ESCROW VAULT",
                                color = DkkGold,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Total Platform Cashflow (VIP Fees + Tiered Commissions)",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // VIP Gold Large Bold Font (32.sp)
                    Text(
                        text = formatPkr(totalCashflow),
                        color = DkkGold,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.testTag("master_vault_total_cashflow")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Revenue Breakdown Cards
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Commissions Box
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = DkkNavy,
                            border = BorderStroke(1.dp, Color(0xFF2E3D66)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "Tiered Commissions",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 10.sp
                                )
                                Text(
                                    text = formatPkr(totalCommissions),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "Brackets: 10% • 8% • 7% • 5%",
                                    color = DkkEmerald,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        // VIP Fees Box
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = DkkNavy,
                            border = BorderStroke(1.dp, DkkGold.copy(alpha = 0.5f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "Pro VIP Subscriptions",
                                    color = DkkGold,
                                    fontSize = 10.sp
                                )
                                Text(
                                    text = formatPkr(stats.proVipRevenue),
                                    color = DkkGold,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "${stats.activeProVipCount} Active @ Rs 5,000",
                                    color = Color(0xFFCBD5E1),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // 2.5 EMBEDDED ESCROW BANK DETAILS
        // One-tap copy JS Bank & Easypaisa
        // ==========================================
        item {
            DkkOfficialBankAccountCard()
        }

        // ==========================================
        // 3. CLEAN METRICS ROW (Section 4, Item 3)
        // Single line with 3 round-cornered (12.dp) boxes
        // 1. Live Traffic | 2. VIP Subscriptions | 3. Orders Pipeline
        // ==========================================
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("clean_metrics_row"),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Box 1: Live Traffic
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = DkkSlate,
                    border = BorderStroke(1.dp, Color(0xFF2E3D66)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Icon(
                            Icons.Default.Group,
                            contentDescription = null,
                            tint = DkkEmerald,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${stats.totalLiveUsers}",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "Live Traffic",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${stats.totalBuyers}B • ${stats.totalSellers}S",
                            color = DkkEmerald,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Box 2: VIP Subscriptions
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = DkkSlate,
                    border = BorderStroke(1.dp, DkkGold.copy(alpha = 0.5f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Icon(
                            Icons.Default.WorkspacePremium,
                            contentDescription = null,
                            tint = DkkGold,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${stats.activeProVipCount}",
                            color = DkkGold,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "VIP Clubs",
                            color = DkkGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Rs 5,000 / seller",
                            color = Color(0xFFCBD5E1),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Box 3: Orders Pipeline
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = DkkSlate,
                    border = BorderStroke(1.dp, Color(0xFF2E3D66)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Icon(
                            Icons.Default.ReceiptLong,
                            contentDescription = null,
                            tint = DkkEmerald,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${allOrders.size}",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "Orders Flow",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "$pendingOrdersCount Pend • $deliveredOrdersCount Done",
                            color = DkkEmerald,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // ==========================================
        // 4. APPROVALS BLOCK (Section 4, Item 4 - Bottom Half)
        // Face Scan Requests, Pro VIP Slips & Products
        // Single-click approval buttons
        // ==========================================
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DkkSlate),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, DkkGold.copy(alpha = 0.3f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("approvals_block_container")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                Icons.Default.Shield,
                                contentDescription = null,
                                tint = DkkGold,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "APPROVALS & AUTHORIZATIONS",
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp,
                                color = Color.White
                            )
                        }

                        val totalPending = pendingSellers.size + pendingProductsLive.size
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (totalPending > 0) Color(0xFFF59E0B).copy(alpha = 0.2f) else DkkEmerald.copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, if (totalPending > 0) Color(0xFFF59E0B) else DkkEmerald)
                        ) {
                            Text(
                                text = if (totalPending > 0) "$totalPending Pending" else "All Clear ✓",
                                color = if (totalPending > 0) Color(0xFFF59E0B) else DkkEmerald,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Segmented Sub-Tab Filter Pills (Horizontally Scrollable)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Tab 0: Seller Face Scans
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (approvalSectionTab == 0) DkkGold else DkkNavy,
                            border = BorderStroke(1.dp, if (approvalSectionTab == 0) DkkGold else Color(0xFF2E3D66)),
                            modifier = Modifier
                                .clickable { approvalSectionTab = 0 }
                        ) {
                            Text(
                                text = "Face Scans (${pendingSellers.size})",
                                color = if (approvalSectionTab == 0) DkkNavy else Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }

                        // Tab 1: Receipts Vault
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (approvalSectionTab == 1) DkkGold else DkkNavy,
                            border = BorderStroke(1.dp, if (approvalSectionTab == 1) DkkGold else Color(0xFF2E3D66)),
                            modifier = Modifier
                                .clickable { approvalSectionTab = 1 }
                                .testTag("tab_receipts_vault")
                        ) {
                            Text(
                                text = "Vault (${receiptsVaultItems.size})",
                                color = if (approvalSectionTab == 1) DkkNavy else Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }

                        // Tab 2: Products
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (approvalSectionTab == 2) DkkGold else DkkNavy,
                            border = BorderStroke(1.dp, if (approvalSectionTab == 2) DkkGold else Color(0xFF2E3D66)),
                            modifier = Modifier
                                .clickable { approvalSectionTab = 2 }
                        ) {
                            Text(
                                text = "Products (${pendingProductsLive.size})",
                                color = if (approvalSectionTab == 2) DkkNavy else Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }

                        // Tab 3: Reports & Support
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (approvalSectionTab == 3) DkkGold else DkkNavy,
                            border = BorderStroke(1.dp, if (approvalSectionTab == 3) DkkGold else Color(0xFF2E3D66)),
                            modifier = Modifier
                                .clickable { approvalSectionTab = 3 }
                        ) {
                            Text(
                                text = "Reports (${activeTicketsLive.size})",
                                color = if (approvalSectionTab == 3) DkkNavy else Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    when (approvalSectionTab) {
                        0 -> {
                            // Sub-tab 0: Seller KYC & Face Scan Verification
                            if (pendingSellers.isEmpty()) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = DkkNavy,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(20.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            Icons.Default.VerifiedUser,
                                            contentDescription = null,
                                            tint = DkkEmerald,
                                            modifier = Modifier.size(36.dp)
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "All Seller Face Scans Verified",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "No pending biometric KYC approvals in queue.",
                                            fontSize = 11.sp,
                                            color = Color(0xFF94A3B8)
                                        )
                                    }
                                }
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    pendingSellers.forEach { seller ->
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = DkkNavy,
                                            border = BorderStroke(1.dp, Color(0xFF2E3D66)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                    ) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(32.dp)
                                                                .background(DkkSlate, CircleShape),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Icon(
                                                                Icons.Default.Face,
                                                                contentDescription = null,
                                                                tint = DkkGold,
                                                                modifier = Modifier.size(18.dp)
                                                            )
                                                        }
                                                        Column {
                                                            Text(
                                                                text = seller.businessName,
                                                                fontWeight = FontWeight.Bold,
                                                                fontSize = 13.sp,
                                                                color = Color.White
                                                            )
                                                            Text(
                                                                text = "UID: ${seller.uid.take(10)}",
                                                                fontSize = 10.sp,
                                                                color = Color(0xFF94A3B8)
                                                            )
                                                        }
                                                    }

                                                    Surface(
                                                        shape = RoundedCornerShape(4.dp),
                                                        color = DkkEmerald.copy(alpha = 0.15f),
                                                        border = BorderStroke(1.dp, DkkEmerald)
                                                    ) {
                                                        Text(
                                                            text = "Face Scan OK ✓",
                                                            color = DkkEmerald,
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                }

                                                Spacer(modifier = Modifier.height(10.dp))

                                                // Single-Click Approve / Reject Action Buttons
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    OutlinedButton(
                                                        onClick = { onRejectSeller(seller.uid) },
                                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                                                        shape = RoundedCornerShape(8.dp),
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .testTag("ec_reject_seller_${seller.uid}")
                                                    ) {
                                                        Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp))
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text("Reject", fontSize = 11.sp)
                                                    }

                                                    Button(
                                                        onClick = { onApproveSeller(seller.uid) },
                                                        colors = ButtonDefaults.buttonColors(containerColor = DkkEmerald),
                                                        shape = RoundedCornerShape(8.dp),
                                                        modifier = Modifier
                                                            .weight(1.6f)
                                                            .testTag("ec_approve_seller_${seller.uid}")
                                                    ) {
                                                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text("Approve Seller ✓", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.White)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        1 -> {
                            // Sub-tab 1: Firebase Storage Receipts Vault (receipts_vault/) protected by 3.0 MB Firewall
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                // 📦 Firebase Storage receipts_vault/ Security Status Banner
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFF0F172A),
                                    border = BorderStroke(1.dp, DkkGold.copy(alpha = 0.5f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.ReceiptLong,
                                                    contentDescription = null,
                                                    tint = DkkGold,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                                Column {
                                                    Text(
                                                        text = "Firebase Storage: receipts_vault/",
                                                        fontWeight = FontWeight.Black,
                                                        fontSize = 13.sp,
                                                        color = Color.White
                                                    )
                                                    Text(
                                                        text = "Dedicated Owner Vault • WhatsApp Bypassed",
                                                        fontSize = 10.sp,
                                                        color = Color(0xFF94A3B8)
                                                    )
                                                }
                                            }

                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = DkkEmerald.copy(alpha = 0.15f),
                                                border = BorderStroke(1.dp, DkkEmerald)
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    Icon(Icons.Default.Shield, contentDescription = null, tint = DkkEmerald, modifier = Modifier.size(10.dp))
                                                    Text(
                                                        text = "3.0 MB FIREWALL ACTIVE",
                                                        color = DkkEmerald,
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        Text(
                                            text = "Jahan seller VIP upgrade fee ki slip ya buyer kisi kharabi ka screenshot bhejega, direct receipts_vault/ folder mein aayega. 3.0 MB se bari file system crash rokne ke liye khud-bakhud block ho jayegi.",
                                            fontSize = 11.sp,
                                            color = Color(0xFFCBD5E1),
                                            lineHeight = 15.sp
                                        )

                                        Spacer(modifier = Modifier.height(10.dp))

                                        // Vault Metrics Row
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            val pendingCount = receiptsVaultItems.count { it.status == "pending_review" }
                                            val verifiedCount = receiptsVaultItems.count { it.status == "verified" }

                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = DkkNavy,
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Text("${receiptsVaultItems.size}", fontWeight = FontWeight.Black, fontSize = 16.sp, color = Color.White)
                                                    Text("Total Slips", fontSize = 10.sp, color = Color(0xFF94A3B8))
                                                }
                                            }
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = DkkNavy,
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Text("$pendingCount", fontWeight = FontWeight.Black, fontSize = 16.sp, color = Color(0xFFF59E0B))
                                                    Text("Pending Verification", fontSize = 10.sp, color = Color(0xFF94A3B8))
                                                }
                                            }
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = DkkNavy,
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Text("$verifiedCount", fontWeight = FontWeight.Black, fontSize = 16.sp, color = DkkEmerald)
                                                    Text("Verified Safe ✓", fontSize = 10.sp, color = Color(0xFF94A3B8))
                                                }
                                            }
                                        }
                                    }
                                }

                                if (receiptsVaultItems.isEmpty()) {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = DkkNavy,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(24.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Icon(
                                                Icons.Default.Check,
                                                contentDescription = null,
                                                tint = DkkEmerald,
                                                modifier = Modifier.size(40.dp)
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = "Receipts Vault Is All Clear",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = Color.White
                                            )
                                            Text(
                                                text = "No pending receipt slips or defect screenshots in receipts_vault/",
                                                fontSize = 11.sp,
                                                color = Color(0xFF94A3B8)
                                            )
                                        }
                                    }
                                } else {
                                    receiptsVaultItems.forEach { item ->
                                        val isPending = item.status == "pending_review"
                                        val isVerified = item.status == "verified"
                                        val borderColor = when {
                                            isVerified -> DkkEmerald
                                            item.status == "rejected" -> Color(0xFFEF4444)
                                            else -> DkkGold.copy(alpha = 0.5f)
                                        }

                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = DkkNavy),
                                            shape = RoundedCornerShape(12.dp),
                                            border = BorderStroke(1.dp, borderColor),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("vault_card_${item.id}")
                                        ) {
                                            Column(modifier = Modifier.padding(14.dp)) {
                                                // Header: Slip Type + Firewall Badge
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    val typeLabel = when (item.slipType) {
                                                        "vip_payment" -> "👑 VIP Pro Fee (Rs 5,000)"
                                                        "return_defect" -> "📦 2-Day Return Defect Proof"
                                                        "escrow_payment" -> "💳 Escrow Payment Slip"
                                                        else -> "📄 Receipt Slip"
                                                    }
                                                    val typeColor = when (item.slipType) {
                                                        "vip_payment" -> DkkGold
                                                        "return_defect" -> Color(0xFFF97316)
                                                        else -> Color(0xFF38BDF8)
                                                    }

                                                    Surface(
                                                        shape = RoundedCornerShape(6.dp),
                                                        color = typeColor.copy(alpha = 0.2f),
                                                        border = BorderStroke(1.dp, typeColor)
                                                    ) {
                                                        Text(
                                                            text = typeLabel,
                                                            color = typeColor,
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                        )
                                                    }

                                                    // 3.0 MB Firewall Tag
                                                    Surface(
                                                        shape = RoundedCornerShape(6.dp),
                                                        color = DkkEmerald.copy(alpha = 0.15f)
                                                    ) {
                                                        Text(
                                                            text = "🛡️ ${item.fileSizeFormatted} (Max 3MB OK)",
                                                            color = DkkEmerald,
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                }

                                                Spacer(modifier = Modifier.height(10.dp))

                                                // Middle Row: Screenshot Thumbnail + Details
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    // Clickable Image Preview
                                                    Box(
                                                        modifier = Modifier
                                                            .size(76.dp)
                                                            .clip(RoundedCornerShape(8.dp))
                                                            .background(Color(0xFF0F172A))
                                                            .border(1.dp, Color(0xFF334155), RoundedCornerShape(8.dp))
                                                            .clickable { previewSlipScreenshot = item },
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        AsyncImage(
                                                            model = item.imageUrl,
                                                            contentDescription = "Screenshot in receipts_vault",
                                                            contentScale = ContentScale.Crop,
                                                            modifier = Modifier.fillMaxSize()
                                                        )
                                                        Surface(
                                                            shape = RoundedCornerShape(4.dp),
                                                            color = Color.Black.copy(alpha = 0.6f),
                                                            modifier = Modifier
                                                                .align(Alignment.BottomEnd)
                                                                .padding(4.dp)
                                                        ) {
                                                            Icon(
                                                                Icons.Default.Face,
                                                                contentDescription = "Enlarge",
                                                                tint = Color.White,
                                                                modifier = Modifier.size(16.dp).padding(2.dp)
                                                            )
                                                        }
                                                    }

                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(
                                                            text = item.title,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 13.sp,
                                                            color = Color.White
                                                        )
                                                        Spacer(modifier = Modifier.height(2.dp))
                                                        Text(
                                                            text = "Sender: ${item.senderName} (${item.senderRole.uppercase()})",
                                                            fontSize = 11.sp,
                                                            color = Color(0xFFCBD5E1)
                                                        )
                                                        Text(
                                                            text = "📞 Mobile: ${item.senderPhone}",
                                                            fontSize = 11.sp,
                                                            color = DkkGold,
                                                            fontWeight = FontWeight.SemiBold
                                                        )
                                                        if (item.amount > 0.0) {
                                                            Text(
                                                                text = "Amount: Rs ${formatPkr(item.amount)}",
                                                                fontSize = 11.sp,
                                                                color = DkkEmerald,
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                        }
                                                        Text(
                                                            text = "Path: ${item.folderPath}${item.id}",
                                                            fontSize = 9.sp,
                                                            color = Color(0xFF94A3B8)
                                                        )
                                                    }
                                                }

                                                Spacer(modifier = Modifier.height(12.dp))

                                                // Action Buttons based on status
                                                if (isPending) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                    ) {
                                                        OutlinedButton(
                                                            onClick = { onRejectReceiptSlip(item.id) },
                                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                                                            shape = RoundedCornerShape(8.dp),
                                                            modifier = Modifier
                                                                .weight(1f)
                                                                .testTag("reject_slip_${item.id}")
                                                        ) {
                                                            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp))
                                                            Spacer(modifier = Modifier.width(4.dp))
                                                            Text("Reject (مسترد)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                        }

                                                        Button(
                                                            onClick = {
                                                                onVerifyReceiptSlip(item.id)
                                                                if (item.slipType == "vip_payment") {
                                                                    val seller = allUsers.find { it.phoneNumber == item.senderPhone || it.name == item.senderName }
                                                                    if (seller != null) {
                                                                        onToggleProVip(seller.uid, true)
                                                                    }
                                                                }
                                                            },
                                                            colors = ButtonDefaults.buttonColors(containerColor = DkkEmerald),
                                                            shape = RoundedCornerShape(8.dp),
                                                            modifier = Modifier
                                                                .weight(1.5f)
                                                                .testTag("verify_slip_${item.id}")
                                                        ) {
                                                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                                                            Spacer(modifier = Modifier.width(4.dp))
                                                            Text("Verify & Clear (تصدیق کریں) ✓", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                        }
                                                    }
                                                } else if (isVerified) {
                                                    Surface(
                                                        shape = RoundedCornerShape(8.dp),
                                                        color = DkkEmerald.copy(alpha = 0.15f),
                                                        border = BorderStroke(1.dp, DkkEmerald),
                                                        modifier = Modifier.fillMaxWidth()
                                                    ) {
                                                        Row(
                                                            modifier = Modifier.padding(10.dp),
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.SpaceBetween
                                                        ) {
                                                            Row(
                                                                verticalAlignment = Alignment.CenterVertically,
                                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                            ) {
                                                                Icon(Icons.Default.Check, contentDescription = null, tint = DkkEmerald, modifier = Modifier.size(16.dp))
                                                                Text(
                                                                    text = "Verified Safe by Daro Khan on 'Everything Clear' Desk ✓",
                                                                    fontSize = 11.sp,
                                                                    fontWeight = FontWeight.Bold,
                                                                    color = DkkEmerald
                                                                )
                                                            }
                                                            IconButton(
                                                                onClick = { previewSlipScreenshot = item },
                                                                modifier = Modifier.size(24.dp)
                                                            ) {
                                                                Icon(Icons.Default.Face, contentDescription = "View", tint = Color.White, modifier = Modifier.size(16.dp))
                                                            }
                                                        }
                                                    }
                                                } else {
                                                    Surface(
                                                        shape = RoundedCornerShape(8.dp),
                                                        color = Color(0xFFEF4444).copy(alpha = 0.15f),
                                                        border = BorderStroke(1.dp, Color(0xFFEF4444)),
                                                        modifier = Modifier.fillMaxWidth()
                                                    ) {
                                                        Row(
                                                            modifier = Modifier.padding(10.dp),
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                        ) {
                                                            Icon(Icons.Default.Close, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                                                            Text(
                                                                text = "Declined Slip: Invalid or unreadable screenshot",
                                                                fontSize = 11.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = Color(0xFFEF4444)
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        2 -> {
                            // Sub-tab 2: Product Moderation Queue
                            if (pendingProductsLive.isEmpty()) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = DkkNavy,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(20.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            Icons.Default.ShoppingBag,
                                            contentDescription = null,
                                            tint = DkkEmerald,
                                            modifier = Modifier.size(36.dp)
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "Product Moderation Clean",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "All seller catalog listings are active.",
                                            fontSize = 11.sp,
                                            color = Color(0xFF94A3B8)
                                        )
                                    }
                                }
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    pendingProductsLive.forEach { prod ->
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = DkkNavy,
                                            border = BorderStroke(1.dp, Color(0xFF2E3D66)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    // Product Thumbnail Image
                                                    if (prod.imageUrl.isNotBlank()) {
                                                        AsyncImage(
                                                            model = resolveProductImageModel(prod.imageUrl),
                                                            contentDescription = prod.productTitle,
                                                            modifier = Modifier
                                                                .size(68.dp)
                                                                .clip(RoundedCornerShape(8.dp))
                                                                .background(Color(0xFF1E293B)),
                                                            contentScale = ContentScale.Crop
                                                        )
                                                    } else {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(68.dp)
                                                                .clip(RoundedCornerShape(8.dp))
                                                                .background(Color(0xFF1E293B)),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Icon(
                                                                Icons.Default.ShoppingBag,
                                                                contentDescription = null,
                                                                tint = DkkGold,
                                                                modifier = Modifier.size(32.dp)
                                                            )
                                                        }
                                                    }

                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.SpaceBetween,
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Text(
                                                                text = prod.productTitle,
                                                                fontWeight = FontWeight.Bold,
                                                                fontSize = 13.sp,
                                                                color = Color.White,
                                                                maxLines = 1,
                                                                overflow = TextOverflow.Ellipsis,
                                                                modifier = Modifier.weight(1f)
                                                            )
                                                            Surface(
                                                                shape = RoundedCornerShape(4.dp),
                                                                color = DkkGold.copy(alpha = 0.15f)
                                                            ) {
                                                                Text(
                                                                    text = CommissionCalculator.getTierName(prod.basePrice),
                                                                    fontSize = 9.sp,
                                                                    fontWeight = FontWeight.Bold,
                                                                    color = DkkGold,
                                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                                )
                                                            }
                                                        }

                                                        Spacer(modifier = Modifier.height(4.dp))
                                                        Text(
                                                            text = "${prod.sellerName} • Category: ${prod.category}",
                                                            fontSize = 11.sp,
                                                            color = Color(0xFF94A3B8)
                                                        )
                                                        Text(
                                                            text = "Price: ${formatPkr(prod.basePrice)}",
                                                            fontSize = 12.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = DkkEmeraldLight
                                                        )
                                                    }
                                                }

                                                Spacer(modifier = Modifier.height(10.dp))

                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    OutlinedButton(
                                                        onClick = { onRejectProductLive(prod.id) },
                                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                                                        shape = RoundedCornerShape(8.dp),
                                                        border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.6f)),
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .testTag("ec_reject_prod_${prod.id}")
                                                    ) {
                                                        Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp))
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text("Reject", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                                    }

                                                    Button(
                                                        onClick = { onApproveProductLive(prod.id) },
                                                        colors = ButtonDefaults.buttonColors(containerColor = DkkEmerald),
                                                        shape = RoundedCornerShape(8.dp),
                                                        modifier = Modifier
                                                            .weight(1.5f)
                                                            .testTag("ec_approve_prod_${prod.id}")
                                                    ) {
                                                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text("Accept ✓", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.White)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        3 -> {
                            // Sub-tab 3: Reports & Support Vault (Direct Master Admin Desk)
                            if (activeTicketsLive.isEmpty()) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = DkkNavy,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(Icons.Default.Shield, contentDescription = null, tint = DkkGold, modifier = Modifier.size(36.dp))
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("No Active Support Complaints", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text("All user issues and reports are resolved. System clean!", color = Color(0xFF94A3B8), fontSize = 11.sp)
                                    }
                                }
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    activeTicketsLive.forEach { ticket ->
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = DkkNavy,
                                            border = BorderStroke(1.dp, if (ticket.priority == "high") Color(0xFFEF4444) else Color(0xFF2E3D66)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(14.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                        Surface(
                                                            shape = RoundedCornerShape(4.dp),
                                                            color = if (ticket.priority == "high") Color(0xFFEF4444).copy(alpha = 0.2f) else DkkGold.copy(alpha = 0.2f),
                                                            border = BorderStroke(1.dp, if (ticket.priority == "high") Color(0xFFEF4444) else DkkGold)
                                                        ) {
                                                            Text(
                                                                text = ticket.priority.uppercase(),
                                                                fontSize = 9.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = if (ticket.priority == "high") Color(0xFFEF4444) else DkkGold,
                                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                            )
                                                        }
                                                        Text(
                                                            text = ticket.subject,
                                                            color = Color.White,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 13.sp
                                                        )
                                                    }
                                                    Text(
                                                        text = "Route: Master Admin Desk",
                                                        fontSize = 9.sp,
                                                        color = DkkGold,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }

                                                Spacer(modifier = Modifier.height(6.dp))

                                                // User Identity header
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text(
                                                        text = "From: ${ticket.senderName.ifBlank { ticket.userEmail }} (${ticket.userRole.uppercase()})",
                                                        fontSize = 11.sp,
                                                        color = Color(0xFFCBD5E1),
                                                        fontWeight = FontWeight.SemiBold
                                                    )
                                                    if (ticket.senderPhone.isNotBlank()) {
                                                        Text(
                                                            text = "📞 ${ticket.senderPhone}",
                                                            fontSize = 11.sp,
                                                            color = DkkEmerald,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                }

                                                Spacer(modifier = Modifier.height(6.dp))

                                                Text(
                                                    text = ticket.message,
                                                    fontSize = 12.sp,
                                                    color = Color.White
                                                )

                                                if (!ticket.screenshotUrl.isNullOrBlank()) {
                                                    Spacer(modifier = Modifier.height(10.dp))
                                                    Surface(
                                                        shape = RoundedCornerShape(8.dp),
                                                        color = Color(0xFF0F172A),
                                                        border = BorderStroke(1.dp, Color(0xFF334155)),
                                                        modifier = Modifier.fillMaxWidth()
                                                    ) {
                                                        Row(
                                                            modifier = Modifier.padding(8.dp),
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                                        ) {
                                                            AsyncImage(
                                                                model = ticket.screenshotUrl,
                                                                contentDescription = "Evidence Screenshot",
                                                                modifier = Modifier
                                                                    .size(48.dp)
                                                                    .clip(RoundedCornerShape(6.dp)),
                                                                contentScale = ContentScale.Crop
                                                            )
                                                            Column(modifier = Modifier.weight(1f)) {
                                                                Text("Attached Screenshot / Payment Slip", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                                                Text("Firewall Check: <3MB Passed ✓", fontSize = 10.sp, color = DkkEmerald)
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // 5. TOP ACTIVE SELLERS (Volume Leaderboard)
        // ==========================================
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DkkSlate),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, Color(0xFF2E3D66)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.TrendingUp, contentDescription = null, tint = DkkEmerald)
                            Text(
                                text = "TOP ACTIVE SELLERS",
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp,
                                color = Color.White
                            )
                        }

                        Text(
                            text = "${topActiveSellers.size} Sellers Active",
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = DkkEmerald
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        topActiveSellers.take(3).forEach { seller ->
                            ActiveSellerCard(seller = seller)
                        }
                    }
                }
            }
        }
    }

    if (previewSlipScreenshot != null) {
        val slip = previewSlipScreenshot!!
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { previewSlipScreenshot = null }
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = DkkNavy,
                border = BorderStroke(1.dp, DkkGold),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = slip.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color.White
                            )
                            Text(
                                text = "Path: ${slip.folderPath}${slip.id}",
                                fontSize = 10.sp,
                                color = DkkGold
                            )
                        }
                        IconButton(onClick = { previewSlipScreenshot = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = slip.imageUrl,
                            contentDescription = "Full Slip Screenshot",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF0F172A),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Sender: ${slip.senderName} (${slip.senderRole.uppercase()})", fontSize = 11.sp, color = Color.White)
                                Text("📞 ${slip.senderPhone}", fontSize = 11.sp, color = DkkGold, fontWeight = FontWeight.Bold)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Amount: Rs ${formatPkr(slip.amount)}", fontSize = 11.sp, color = DkkEmerald, fontWeight = FontWeight.Bold)
                                Text("Firewall: ${slip.fileSizeFormatted} (Max 3MB OK ✓)", fontSize = 11.sp, color = DkkEmerald)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (slip.status == "pending_review") {
                            OutlinedButton(
                                onClick = {
                                    onRejectReceiptSlip(slip.id)
                                    previewSlipScreenshot = null
                                },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Reject Slip", fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    onVerifyReceiptSlip(slip.id)
                                    if (slip.slipType == "vip_payment") {
                                        val seller = allUsers.find { it.phoneNumber == slip.senderPhone || it.name == slip.senderName }
                                        if (seller != null) {
                                            onToggleProVip(seller.uid, true)
                                        }
                                    }
                                    previewSlipScreenshot = null
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = DkkEmerald),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Verify & Clear ✓", fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Button(
                                onClick = { previewSlipScreenshot = null },
                                colors = ButtonDefaults.buttonColors(containerColor = DkkGold),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Close Preview", color = DkkNavy, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TierRevenueRow(tierName: String, rate: String, amount: Double, color: Color) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = color.copy(alpha = 0.08f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(color)
                )
                Text(
                    text = tierName,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Text(
                text = formatPkr(amount),
                fontWeight = FontWeight.Black,
                fontSize = 13.sp,
                color = color
            )
        }
    }
}

@Composable
fun DkkOwnerDeskView(
    totalPlatformEarnings: Double,
    pendingSellersCount: Int,
    pendingProductsCount: Int,
    activeTicketsCount: Int,
    pendingProducts: List<PendingProductItem>,
    activeTickets: List<SupportTicketItem>,
    onApproveProduct: (productId: String) -> Unit,
    onRejectProduct: (productId: String) -> Unit = {},
    onOpenFirebase: (() -> Unit)? = null,
    isLoading: Boolean = false,
    recentAlerts: List<OwnerAlertNotification> = emptyList(),
    onDismissAlert: (String) -> Unit = {},
    onClearAllAlerts: () -> Unit = {},
    onSimulateHighValueProduct: () -> Unit = {},
    onSimulateUrgentTicket: () -> Unit = {},
    allOrders: List<OrderEntity> = emptyList(),
    allUsers: List<UserEntity> = emptyList()
) {
    var selectedModerationCategory by remember { mutableStateOf("All") }

    val moderationCategories = remember(pendingProducts) {
        val standard = listOf("All", "Electronics", "Fashion", "Home")
        val others = pendingProducts.map { it.category.trim() }
            .filter { it.isNotBlank() && standard.none { s -> s.equals(it, ignoreCase = true) } }
            .distinct()
        standard + others
    }

    val filteredPendingProducts = remember(pendingProducts, selectedModerationCategory) {
        if (selectedModerationCategory == "All") {
            pendingProducts
        } else {
            pendingProducts.filter { it.category.equals(selectedModerationCategory, ignoreCase = true) }
        }
    }

    val topActiveSellers = remember(allOrders, allUsers) {
        computeTopActiveSellers(allOrders, allUsers)
    }

    val sortedTickets = remember(activeTickets) {
        activeTickets.sortedWith(
            compareByDescending<SupportTicketItem> { getTicketPriorityWeight(it.priority) }
                .thenByDescending { it.createdAt }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA)),
        contentPadding = PaddingValues(15.dp),
        verticalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        // Top Header Display with Official DKK Branding
        item {
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(15.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.Start) {
                        Text(
                            text = "🦅 DKK Owner Desk",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0057B7),
                            letterSpacing = 1.sp
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "CONTROL PANEL",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF666666)
                            )
                            Text(
                                text = "•",
                                fontSize = 11.sp,
                                color = Color(0xFF999999)
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF0057B7).copy(alpha = 0.1f)
                            ) {
                                Text(
                                    text = "Daro Khan (Master Admin)",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0057B7),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // ⚡ REAL-TIME NOTIFICATION LISTENER MONITOR CARD
        item {
            RealTimeAlertMonitorCard(
                alertsCount = recentAlerts.size,
                recentAlerts = recentAlerts,
                onDismissAlert = onDismissAlert,
                onClearAll = onClearAllAlerts,
                onSimulateHighValueProduct = onSimulateHighValueProduct,
                onSimulateUrgentTicket = onSimulateUrgentTicket,
                onApproveProduct = onApproveProduct
            )
        }

        // 💵 1. MAIN PLATFORM REVENUE COUNTER (Merchant Green Theme)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2E7D32)),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "DKK PLATFORM NET COMMISSION EARNINGS",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Rs " + String.format(Locale.US, "%.2f", totalPlatformEarnings),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Auto calculated from 10%, 8%, and 7% dynamic price slices",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.7f),
                        fontStyle = FontStyle.Italic
                    )
                }
            }
        }

        // 📊 2. INSTANT LIVE COUNTERS LAYOUT GRID
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // MiniCard 1: Pending Sellers (#0057b7)
                DkkMiniCounterCard(
                    label = "Pending Sellers",
                    value = pendingSellersCount.toString(),
                    accentColor = Color(0xFF0057B7),
                    modifier = Modifier.weight(1f)
                )
                // MiniCard 2: Under Review Items (#ff9100)
                DkkMiniCounterCard(
                    label = "Under Review Items",
                    value = pendingProductsCount.toString(),
                    accentColor = Color(0xFFFF9100),
                    modifier = Modifier.weight(1f)
                )
                // MiniCard 3: Active Complaints (#d32f2f)
                DkkMiniCounterCard(
                    label = "Active Complaints",
                    value = activeTicketsCount.toString(),
                    accentColor = Color(0xFFD32F2F),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 📦 3. MODERATION QUEUE (Pending Products with Category Tab Switcher)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "MODERATION QUEUE (${pendingProducts.size})",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333),
                        letterSpacing = 0.5.sp
                    )
                    Surface(
                        color = Color(0xFF0057B7).copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (selectedModerationCategory == "All") "All Categories" else selectedModerationCategory,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0057B7),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                // Category Tab Switcher
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().testTag("moderation_category_tabs")
                ) {
                    items(moderationCategories) { cat ->
                        val isSelected = selectedModerationCategory.equals(cat, ignoreCase = true)
                        val catCount = if (cat == "All") {
                            pendingProducts.size
                        } else {
                            pendingProducts.count { it.category.equals(cat, ignoreCase = true) }
                        }

                        Surface(
                            onClick = { selectedModerationCategory = cat },
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) Color(0xFF0057B7) else Color.White,
                            border = BorderStroke(1.dp, if (isSelected) Color(0xFF0057B7) else Color(0xFFE2E8F0)),
                            shadowElevation = if (isSelected) 2.dp else 0.dp,
                            modifier = Modifier.testTag("moderation_tab_${cat.lowercase()}")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                            ) {
                                Text(
                                    text = cat,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else Color(0xFF334155)
                                )
                                Surface(
                                    shape = CircleShape,
                                    color = if (isSelected) Color.White.copy(alpha = 0.25f) else Color(0xFFF1F5F9)
                                ) {
                                    Text(
                                        text = catCount.toString(),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else Color(0xFF64748B),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (filteredPendingProducts.isEmpty()) {
            item {
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFFEEEEEE)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Koi pending product nahi mili $selectedModerationCategory category mein.",
                            color = Color(0xFF999999),
                            fontSize = 13.sp
                        )
                    }
                }
            }
        } else {
            items(filteredPendingProducts, key = { it.id }) { item ->
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFFEEEEEE)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Product Image Thumbnail
                            if (item.imageUrl.isNotBlank()) {
                                AsyncImage(
                                    model = item.imageUrl,
                                    contentDescription = item.productTitle,
                                    modifier = Modifier
                                        .size(72.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFF1F5F9)),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(72.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFF1F5F9)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.ShoppingBag,
                                        contentDescription = null,
                                        tint = Color(0xFF94A3B8),
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.productTitle,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E293B),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Surface(
                                        color = Color(0xFFF1F5F9),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = item.category,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF475569),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Text(
                                        text = "Price: Rs ${item.basePrice.toInt()}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF2E7D32)
                                    )
                                }
                                if (item.sellerName.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Seller: ${item.sellerName}",
                                        fontSize = 11.sp,
                                        color = Color(0xFF0057B7)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Accept / Reject Actions
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { onRejectProduct(item.id) },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD32F2F)),
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, Color(0xFFD32F2F).copy(alpha = 0.6f)),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("reject_btn_${item.id}")
                            ) {
                                Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Reject", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }

                            Button(
                                onClick = { onApproveProduct(item.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                modifier = Modifier
                                    .weight(1.3f)
                                    .testTag("approve_btn_${item.id}")
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Accept ✓", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        // 🏆 MOST ACTIVE SELLERS LEADERBOARD SECTION
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(
                        imageVector = Icons.Default.TrendingUp,
                        contentDescription = null,
                        tint = Color(0xFF0057B7),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "MOST ACTIVE SELLERS",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333),
                        letterSpacing = 0.5.sp
                    )
                }
                Surface(
                    color = Color(0xFFFEF3C7),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFFDE68A))
                ) {
                    Text(
                        text = "By Total Sales Volume",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF92400E),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
        }

        items(topActiveSellers, key = { it.sellerId }) { seller ->
            ActiveSellerCard(seller = seller)
        }

        // 📞 4. SUPPORT CENTER WIDGET (Complaints Box)
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ACTIVE SUPPORT TICKETS ($activeTicketsCount)",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333),
                    letterSpacing = 0.5.sp
                )
                // Priority Sorting Tag
                Surface(
                    color = Color(0xFFFEF2F2),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFFECACA))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFDC2626))
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Priority Sort: High ➔ Low",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFDC2626)
                        )
                    }
                }
            }
        }

        if (sortedTickets.isEmpty()) {
            item {
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFFEEEEEE)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Shuker hai! Koi active shikayat ya ticket nahi hai.",
                            color = Color(0xFF999999),
                            fontSize = 13.sp
                        )
                    }
                }
            }
        } else {
            items(sortedTickets, key = { it.id }) { ticket ->
                val priority = ticket.priority.lowercase().trim()
                val accentColor = when (priority) {
                    "high" -> Color(0xFFD32F2F)
                    "low" -> Color(0xFF2563EB)
                    else -> Color(0xFFD97706)
                }
                val badgeBg = when (priority) {
                    "high" -> Color(0xFFFEE2E2)
                    "low" -> Color(0xFFEFF6FF)
                    else -> Color(0xFFFEF3C7)
                }
                val badgeBorder = when (priority) {
                    "high" -> Color(0xFFFCA5A5)
                    "low" -> Color(0xFFBFDBFE)
                    else -> Color(0xFFFDE68A)
                }
                val badgeText = when (priority) {
                    "high" -> "🔴 HIGH PRIORITY"
                    "low" -> "🔵 LOW PRIORITY"
                    else -> "🟡 MEDIUM PRIORITY"
                }

                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, accentColor.copy(alpha = 0.35f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("ticket_item_${ticket.id}")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = ticket.subject,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF212121),
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = badgeBg,
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, badgeBorder),
                                modifier = Modifier.testTag("ticket_priority_badge_${ticket.id}")
                            ) {
                                Text(
                                    text = badgeText,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = accentColor,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "From: ${ticket.userEmail} (${ticket.userRole.uppercase()})",
                                fontSize = 12.sp,
                                color = Color(0xFF666666)
                            )
                            Text(
                                text = "Status: ${ticket.status.uppercase()}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF10B981)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            color = Color(0xFFF8FAFC),
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(0.5.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "\"${ticket.message}\"",
                                fontSize = 13.sp,
                                color = Color(0xFF334155),
                                fontStyle = FontStyle.Italic,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DkkMiniCounterCard(
    label: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(10.dp),
        shadowElevation = 1.dp,
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.3f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 10.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF64748B),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )
        }
    }
}

/**
 * Top-Performing Seller Contributor model for Admin Dashboard
 */
data class ActiveSellerContributor(
    val sellerId: String,
    val sellerName: String,
    val totalSalesVolume: Double,
    val ordersCount: Int,
    val commissionPaid: Double,
    val isVip: Boolean = false,
    val rank: Int = 1
)

fun computeTopActiveSellers(
    allOrders: List<OrderEntity>,
    allUsers: List<UserEntity>
): List<ActiveSellerContributor> {
    val userMap = allUsers.associateBy { it.uid }
    val sellerStats = mutableMapOf<String, Triple<String, Double, Int>>()

    allOrders.forEach { order ->
        val existing = sellerStats[order.sellerId]
        val name = existing?.first ?: order.sellerName.ifBlank { "Seller ${order.sellerId.take(6)}" }
        val vol = (existing?.second ?: 0.0) + order.orderAmount
        val count = (existing?.third ?: 0) + 1
        sellerStats[order.sellerId] = Triple(name, vol, count)
    }

    val baseline = listOf(
        ActiveSellerContributor(
            sellerId = "SELLER_003",
            sellerName = "DKK Tech Hub (Bilal Khan)",
            totalSalesVolume = 1250000.0,
            ordersCount = 54,
            commissionPaid = 78000.0,
            isVip = true,
            rank = 1
        ),
        ActiveSellerContributor(
            sellerId = "SELLER_001",
            sellerName = "Al-Madina Electronics (Tariq)",
            totalSalesVolume = 412000.0,
            ordersCount = 26,
            commissionPaid = 33800.0,
            isVip = true,
            rank = 2
        ),
        ActiveSellerContributor(
            sellerId = "SELLER_002",
            sellerName = "Raza Premium Garments (Hassan)",
            totalSalesVolume = 96000.0,
            ordersCount = 8,
            commissionPaid = 8400.0,
            isVip = false,
            rank = 3
        )
    )

    return if (sellerStats.isNotEmpty()) {
        val calculated = sellerStats.entries.map { (id, data) ->
            val isVip = userMap[id]?.isProVip ?: false
            ActiveSellerContributor(
                sellerId = id,
                sellerName = data.first,
                totalSalesVolume = data.second,
                ordersCount = data.third,
                commissionPaid = data.second * 0.08,
                isVip = isVip
            )
        }
        (calculated + baseline)
            .distinctBy { it.sellerId }
            .sortedByDescending { it.totalSalesVolume }
            .mapIndexed { index, item -> item.copy(rank = index + 1) }
    } else {
        baseline
    }
}

/**
 * Clean, high-impact card displaying a top-performing active seller
 */
@Composable
fun ActiveSellerCard(
    seller: ActiveSellerContributor,
    modifier: Modifier = Modifier
) {
    val rankBadgeColor = when (seller.rank) {
        1 -> DkkGold
        2 -> Color(0xFF94A3B8)
        3 -> Color(0xFFCD7F32)
        else -> Color(0xFF64748B)
    }

    val rankLabel = when (seller.rank) {
        1 -> "👑 #1 TOP SELLER"
        2 -> "🥈 #2 CONTRIBUTOR"
        3 -> "🥉 #3 CONTRIBUTOR"
        else -> "#${seller.rank} SELLER"
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            width = if (seller.rank == 1) 1.5.dp else 1.dp,
            color = if (seller.rank == 1) DkkGold.copy(alpha = 0.6f) else Color(0xFFE2E8F0)
        ),
        elevation = CardDefaults.cardElevation(if (seller.rank == 1) 3.dp else 1.dp),
        modifier = modifier.fillMaxWidth().testTag("seller_card_${seller.sellerId}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(rankBadgeColor.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (seller.rank == 1) Icons.Default.WorkspacePremium else Icons.Default.Storefront,
                            contentDescription = null,
                            tint = rankBadgeColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = seller.sellerName,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (seller.isVip) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = DkkGold.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "VIP",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFFB45309),
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = "${seller.ordersCount} orders completed",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = rankBadgeColor.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = rankLabel,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = rankBadgeColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFF8FAFC),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "TOTAL SALES VOLUME",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64748B),
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = formatPkr(seller.totalSalesVolume),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF059669)
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "COMMISSION PAID",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64748B),
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = formatPkr(seller.commissionPaid),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = DkkGold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DkkOfficialBankAccountCard(
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current
    var copiedField by remember { mutableStateOf<String?>(null) }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF0F172A)
        ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, DkkGold.copy(alpha = 0.4f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(DkkGold, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalance,
                            contentDescription = null,
                            tint = Color(0xFF0F172A),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Official Settlement Account",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "DKK Platform Escrow & Commission Hub",
                            color = Color.LightGray,
                            fontSize = 11.sp
                        )
                    }
                }

                Surface(
                    color = DkkEmerald.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "ACTIVE",
                        color = DkkEmerald,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Surface(
                color = Color(0xFF1E293B),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Account Title:", color = Color.LightGray, fontSize = 11.sp)
                        Text(
                            text = DkkOfficialBankAccount.ACCOUNT_TITLE,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Bank Name:", color = Color.LightGray, fontSize = 11.sp)
                        Text(
                            text = DkkOfficialBankAccount.BANK_NAME,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Color.White.copy(alpha = 0.1f))
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Account Number with copy
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Account Number:", color = Color.LightGray, fontSize = 10.sp)
                            Text(
                                text = DkkOfficialBankAccount.ACCOUNT_NUMBER,
                                color = DkkGold,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                        IconButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(DkkOfficialBankAccount.ACCOUNT_NUMBER))
                                copiedField = "Account Number"
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.ContentCopy,
                                contentDescription = "Copy Account Number",
                                tint = DkkGold,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // IBAN with copy
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 6.dp)) {
                            Text("IBAN (Raast / 1Link):", color = Color.LightGray, fontSize = 10.sp)
                            Text(
                                text = DkkOfficialBankAccount.IBAN,
                                color = Color.White,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp
                            )
                        }
                        IconButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(DkkOfficialBankAccount.IBAN))
                                copiedField = "IBAN"
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.ContentCopy,
                                contentDescription = "Copy IBAN",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Official Easypaisa Account Card
            Surface(
                color = Color(0xFF064E3B),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .background(Color(0xFF10B981), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("EP", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                            }
                            Text(
                                text = "Official Easypaisa Account",
                                color = Color(0xFFA7F3D0),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                        Surface(
                            color = Color(0xFF10B981).copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "ACTIVE",
                                color = Color(0xFFA7F3D0),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Account Title:", color = Color(0xFF6EE7B7), fontSize = 11.sp)
                        Text(
                            text = DkkOfficialEasypaisaAccount.ACCOUNT_TITLE,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Easypaisa Account Number Row with copy
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF042F2E), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Account Number (Mobile & Raast):", color = Color(0xFF6EE7B7), fontSize = 9.sp)
                            Text(
                                text = DkkOfficialEasypaisaAccount.ACCOUNT_NUMBER,
                                color = Color(0xFF34D399),
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                        IconButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(DkkOfficialEasypaisaAccount.ACCOUNT_NUMBER))
                                copiedField = "Easypaisa Number (03273856001)"
                            },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(
                                Icons.Default.ContentCopy,
                                contentDescription = "Copy Easypaisa Number",
                                tint = Color(0xFF34D399),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            if (copiedField != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "✓ $copiedField copied to clipboard",
                    color = DkkEmerald,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

/**
 * 🎙️ AudioRoomsAndCoinsOwnerSystemView:
 * Dedicated Owner / SuperAdmin Control Deck for Live Audio Rooms, 10-Mic Stage,
 * Pakistan PKR (5000 Rs = 5000 Coins) & Overseas USD ($50 = 5000 Coins) Coin Sales Ledger,
 * Friday 30% Host Payout Settlements, Physical Delivery to Pakistan Addresses, and VIP Tiers.
 */
@Composable
fun AudioRoomsAndCoinsOwnerSystemView(
    viewModel: DkkViewModel?,
    stats: AdminOverviewStats
) {
    val activeRoom = viewModel?.activeRoom?.collectAsState()?.value ?: AudioRoom(
        roomId = "6974202528",
        title = "خوږه یاران 👑",
        hostUid = "host_mansoor",
        hostName = "Mansoori 🇦🇪",
        countryFlag = "🇦🇪",
        listenerCount = 128,
        seats = List(10) { idx ->
            MicSeat(
                seatIndex = idx,
                occupantUid = if (idx in 0..3) "user_$idx" else null,
                occupantName = when (idx) {
                    0 -> "ROC... ⭐"
                    1 -> "Hamza 🇵🇰"
                    2 -> "Zubair"
                    3 -> "Ali"
                    else -> null
                },
                giftScore = if (idx == 0) 2450L else 120L,
                isLocked = idx in listOf(7, 9)
            )
        }
    )

    val coinBalance = viewModel?.coinBalance?.collectAsState()?.value ?: 150_000L
    val diamondBalance = viewModel?.diamondBalance?.collectAsState()?.value ?: 85_000L
    val isRoomVip = viewModel?.isRoomVip?.collectAsState()?.value ?: true
    val isSellerVip = viewModel?.isSellerVip?.collectAsState()?.value ?: true
    val giftRedemptions = viewModel?.giftRedemptions?.collectAsState()?.value ?: emptyList()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("audio_rooms_and_coins_system_view"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // ---------------------------------------------------------------------
        // 1. HEADER HERO BANNER
        // ---------------------------------------------------------------------
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DkkSlate),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.2.dp, DkkGold.copy(alpha = 0.7f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Mic, contentDescription = null, tint = DkkGold, modifier = Modifier.size(24.dp))
                            Text(
                                text = "Audio Room & Coin System Deck",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp
                            )
                        }
                        Surface(shape = RoundedCornerShape(6.dp), color = DkkEmerald.copy(alpha = 0.2f), border = BorderStroke(1.dp, DkkEmerald)) {
                            Text("LIVE 10-MIC ENGINE", color = DkkEmerald, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Owner controls for 10-mic stage rooms, Pakistan (5000 Rs = 5000 Coins) vs Overseas ($50 USD = 5000 Coins) monetization, 30% Friday host cashout clearance, physical delivery across Pakistan, and VIP subscription limits.",
                        color = Color(0xFFCBD5E1),
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // ---------------------------------------------------------------------
        // 2. METRIC TILES ROW
        // ---------------------------------------------------------------------
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Tile 1: Coins In Circulation
                Card(
                    colors = CardDefaults.cardColors(containerColor = DkkSlate),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, DkkGold.copy(alpha = 0.4f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Active Pool", color = Color(0xFF94A3B8), fontSize = 10.sp)
                        Text("${coinBalance / 1000}k Coins", color = DkkGold, fontWeight = FontWeight.Black, fontSize = 14.sp)
                        Text("PKR & USD Top-Ups", color = Color(0xFF38BDF8), fontSize = 9.sp)
                    }
                }

                // Tile 2: Host Diamond Payout Pool (30% Settlement)
                Card(
                    colors = CardDefaults.cardColors(containerColor = DkkSlate),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.4f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Friday 30% Pool", color = Color(0xFF94A3B8), fontSize = 10.sp)
                        Text("$diamondBalance 💎", color = Color(0xFF38BDF8), fontWeight = FontWeight.Black, fontSize = 14.sp)
                        Text("Rs ${(diamondBalance * 0.10).toInt()} PKR", color = EasypaisaGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Tile 3: Live Listeners & Rooms
                Card(
                    colors = CardDefaults.cardColors(containerColor = DkkSlate),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.4f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Live Stage", color = Color(0xFF94A3B8), fontSize = 10.sp)
                        Text("${activeRoom.listenerCount} Users", color = Color(0xFF34D399), fontWeight = FontWeight.Black, fontSize = 14.sp)
                        Text("1 Room • 10 Mics", color = Color.White, fontSize = 9.sp)
                    }
                }
            }
        }

        // ---------------------------------------------------------------------
        // 3. LIVE 10-MIC STAGE MONITOR CARD (Matches Screenshot 1 Layout)
        // ---------------------------------------------------------------------
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DkkSlate),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, DkkGold.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.Mic, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(18.dp))
                            Text(
                                text = "Live Audio Room: ${activeRoom.countryFlag} ${activeRoom.title}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                        Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFF1E293B)) {
                            Text("ID: ${activeRoom.roomId}", color = Color(0xFF94A3B8), fontSize = 10.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Visual Representation of the 10-Mic Stage (Host at Top, Row 1, Row 2)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF0F172A))
                            .padding(10.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Mic 1 (Host)
                            val hostMic = activeRoom.seats.getOrNull(0)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(DkkGold),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                                }
                                Text("👑 Host: ${hostMic?.occupantName ?: "Mansoori 🇦🇪"} (Level 32 • 2,450 🎁)", color = DkkGold, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Row 1: Mics 2-6
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                for (i in 1..5) {
                                    val seat = activeRoom.seats.getOrNull(i)
                                    val isOcc = seat?.isOccupied == true
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (isOcc) Color(0xFF1E293B) else Color(0xFF0B1324),
                                        border = BorderStroke(0.6.dp, if (isOcc) Color(0xFF38BDF8) else Color(0xFF334155))
                                    ) {
                                        Text(
                                            text = "Mic ${i + 1}: ${seat?.occupantName ?: "Empty"}",
                                            color = if (isOcc) Color.White else Color(0xFF64748B),
                                            fontSize = 9.sp,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Row 2: Mics 7-10 (Includes Locked Mics 8 & 10)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                for (i in 6..9) {
                                    val seat = activeRoom.seats.getOrNull(i)
                                    val isLocked = seat?.isLocked == true
                                    val isOcc = seat?.isOccupied == true
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (isLocked) Color(0xFF451A03) else if (isOcc) Color(0xFF1E293B) else Color(0xFF0B1324),
                                        border = BorderStroke(0.6.dp, if (isLocked) DkkGold else Color(0xFF334155))
                                    ) {
                                        Text(
                                            text = if (isLocked) "Mic ${i + 1}: 🔒 Locked" else "Mic ${i + 1}: ${seat?.occupantName ?: "Empty"}",
                                            color = if (isLocked) DkkGold else if (isOcc) Color.White else Color(0xFF64748B),
                                            fontSize = 9.sp,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel?.toggleSeatLock(7)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DkkGold),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Toggle Stage Locks", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                viewModel?.occupyMicSeat(1)
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF38BDF8)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Join as Co-Host", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // ---------------------------------------------------------------------
        // 4. COIN REVENUE & PRICING MATRIX (Pakistan PKR vs Overseas USD)
        // ---------------------------------------------------------------------
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DkkSlate),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "🪙 Official Coin Top-Up Pricing Matrix",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Rule configured: Pakistan buyers 5000 Rs = 5000 Coins; Overseas website buyers $50 USD = 5000 Coins",
                        color = Color(0xFF94A3B8),
                        fontSize = 10.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Pakistan Rule Card
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF0F172A),
                        border = BorderStroke(1.dp, EasypaisaGreen.copy(alpha = 0.7f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("🇵🇰 Pakistan Direct Pay (JazzCash / Easypaisa)", color = Color(0xFF34D399), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text("Rate: 1 PKR = 1 Coin • 5,000 Rs = 5,000 Coins", color = Color.White, fontSize = 11.sp)
                                Text("Zero Google Play cut • Dispatched immediately", color = Color(0xFF94A3B8), fontSize = 9.sp)
                            }
                            Button(
                                onClick = {
                                    viewModel?.topUpCoinsPakistan(5_000L, 5_000.0)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = EasypaisaGreen),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text("+5,000 Coins", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Overseas Rule Card
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF0F172A),
                        border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.7f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("🌐 Overseas Website Portal (Stripe / PayPal)", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text("Rate: $50 USD = 5,000 Coins ($10 = 1,000 Coins)", color = Color.White, fontSize = 11.sp)
                                Text("International credit/debit card gateway", color = Color(0xFF94A3B8), fontSize = 9.sp)
                            }
                            Button(
                                onClick = {
                                    viewModel?.topUpCoinsOverseas(5_000L, 50.0)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text("+5,000 Coins ($50)", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // ---------------------------------------------------------------------
        // 5. FRIDAY 30% HOST CASHOUT & DIAMOND SETTLEMENT CLEARANCE
        // ---------------------------------------------------------------------
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DkkSlate),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, DkkGold.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🗓️ Friday 30% Host Settlement Ledger",
                            color = DkkGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFF1E293B)) {
                            Text("Friday to Friday", color = Color(0xFF38BDF8), fontSize = 9.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "• Web Rule: Sirf Host ya Room Admin payout request kar sakte hain.\n• 30% Net Share: Host earns 30% from luxury gifts sent in room.\n• Settlement Window: Every Friday 00:00 to 23:59 directly via JazzCash / Easypaisa.",
                        color = Color(0xFFE2E8F0),
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF0F172A),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Host Pending Diamonds: $diamondBalance 💎", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text("Estimated Net Friday Cashout: Rs ${(diamondBalance * 0.10).toInt()} PKR", color = EasypaisaGreen, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }

                            Button(
                                onClick = {
                                    viewModel?.cashoutDiamonds(
                                        diamondAmount = diamondBalance,
                                        user = null,
                                        payoutMethod = "Friday Owner Direct Clearance",
                                        accountDetails = "0327-3856001"
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = DkkGold),
                                shape = RoundedCornerShape(6.dp),
                                enabled = diamondBalance > 0
                            ) {
                                Text("Clear Friday Payout", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }

        // ---------------------------------------------------------------------
        // 6. PAKISTAN PHYSICAL GIFTS DELIVERY TRACKER (Karachi, Lahore, Islamabad)
        // ---------------------------------------------------------------------
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DkkSlate),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.LocalShipping, contentDescription = null, tint = DkkGold, modifier = Modifier.size(18.dp))
                            Text(
                                text = "🚚 Pakistan Gift Delivery Dispatches",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                        Text("${giftRedemptions.size} Parcels", color = Color(0xFF94A3B8), fontSize = 10.sp)
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Rule: Gifts received in room can be physically delivered to the user's Pakistani address.",
                        color = Color(0xFFCBD5E1),
                        fontSize = 10.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    val sampleDeliveries = if (giftRedemptions.isNotEmpty()) giftRedemptions else listOf(
                        PhysicalGiftRedemption(
                            id = "DEL_001",
                            itemTitle = "Silk Bridal Suit 👗",
                            diamondsSpent = 15_000L,
                            recipientName = "Ayesha Khan",
                            phone = "0321-4567890",
                            city = "Lahore",
                            fullAddress = "House 45-B, Sector Y, DHA Phase 3, Lahore",
                            status = "Dispatched via TCS (Tracking: #TCS-89218)"
                        ),
                        PhysicalGiftRedemption(
                            id = "DEL_002",
                            itemTitle = "Luxury Makeup Vanity Box 💄",
                            diamondsSpent = 10_000L,
                            recipientName = "Fatima Noor",
                            phone = "0333-7891234",
                            city = "Karachi",
                            fullAddress = "Flat 402, Al-Rahim Towers, Clifton Block 5, Karachi",
                            status = "In-Transit Leopards (#LEO-39401)"
                        ),
                        PhysicalGiftRedemption(
                            id = "DEL_003",
                            itemTitle = "Luxury Gold Watch ⌚",
                            diamondsSpent = 25_000L,
                            recipientName = "Mohammad Usman",
                            phone = "0327-3856001",
                            city = "Islamabad",
                            fullAddress = "Street 12, F-7/2, Islamabad",
                            status = "Delivered ✓"
                        )
                    )

                    sampleDeliveries.forEach { parcel ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF0F172A),
                            border = BorderStroke(0.7.dp, Color(0xFF334155)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(parcel.itemTitle, color = DkkGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = if (parcel.status.contains("Delivered")) EasypaisaGreen.copy(alpha = 0.2f) else Color(0xFF1E293B),
                                        border = BorderStroke(0.6.dp, if (parcel.status.contains("Delivered")) EasypaisaGreen else Color(0xFF38BDF8))
                                    ) {
                                        Text(
                                            text = parcel.status,
                                            color = if (parcel.status.contains("Delivered")) Color(0xFF34D399) else Color(0xFF38BDF8),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("To: ${parcel.recipientName} • ${parcel.phone} • ${parcel.city}", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                                Text(parcel.fullAddress, color = Color(0xFF94A3B8), fontSize = 9.sp)
                            }
                        }
                    }
                }
            }
        }

        // ---------------------------------------------------------------------
        // 7. VIP SUBSCRIPTION RULES (Room VIP vs Seller VIP)
        // ---------------------------------------------------------------------
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DkkSlate),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "👑 VIP Tier Specifications & Restrictions",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // VIP 1: ROOM VIP
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF0F172A),
                        border = BorderStroke(1.dp, DkkGold.copy(alpha = 0.6f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("1. ROOM VIP ($100 USD / Month)", color = DkkGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Surface(shape = RoundedCornerShape(4.dp), color = DkkGold) {
                                    Text(if (isRoomVip) "ACTIVE ✓" else "INACTIVE", color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("• Allowance: 12 Videos Per Day inside Audio Room Looper", color = Color.White, fontSize = 10.sp)
                            Text("• Max Duration: 15 Seconds", color = Color.White, fontSize = 10.sp)
                            Text("• Auto-Deletion: 1 Hour me khud delete hogi (Ephemeral)", color = Color(0xFF38BDF8), fontSize = 10.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // VIP 2: SELLER VIP
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF0F172A),
                        border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.6f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("2. SELLER VIP (Verified PIV Tag)", color = Color(0xFF10B981), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFF10B981)) {
                                    Text(if (isSellerVip) "ACTIVE ✓" else "INACTIVE", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("• Allowance: 3 Pictures or Videos with Verified PIV Tag", color = Color.White, fontSize = 10.sp)
                            Text("• Home Feed Quota: Day me sirf 5 videos post kar sakta ho", color = Color.White, fontSize = 10.sp)
                            Text("• Priority Listing on Luxury Bridal & Jewellery Catalog", color = Color(0xFFD1FAE5), fontSize = 10.sp)
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}


package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.firebase.AudioRoom
import com.example.data.model.DeliveryStatus
import com.example.data.model.OrderEntity
import com.example.data.model.ProductEntity
import com.example.data.model.SellerProfileEntity
import com.example.data.model.UserEntity
import com.example.data.model.resolveProductImageModel
import com.example.data.repository.CommissionCalculator
import com.example.data.repository.SellerLevelCalculator
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

@Composable
fun SellerDashboardScreen(
    user: UserEntity,
    sellerProfile: SellerProfileEntity?,
    sellerProducts: List<ProductEntity>,
    sellerOrders: List<OrderEntity>,
    onOpenPostProduct: () -> Unit,
    onOpenFaceScan: () -> Unit,
    onDispatchOrder: (orderId: String) -> Unit,
    onDeliverOrderWithOtp: (orderId: String, otp: String) -> Unit,
    onOpenSupportChat: (() -> Unit)? = null,
    onUpdatePayoutDetails: ((bankName: String, title: String, accountNum: String, iban: String, epNumber: String, epTitle: String) -> Unit)? = null,
    onLogout: (() -> Unit)? = null,
    onOpenRoleDialog: (() -> Unit)? = null,
    onOpenAudioRoom: (() -> Unit)? = null,
    allAudioRooms: List<AudioRoom> = emptyList(),
    onSelectRoom: ((AudioRoom) -> Unit)? = null,
    onCreateRoom: ((title: String, videoStreamUrl: String?, countryFlag: String, countryName: String) -> Unit)? = null
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Kamai, 1: Orders, 2: Listings, 3: Audio Rooms
    var otpInputs by remember { mutableStateOf(mutableMapOf<String, String>()) }
    var showVipInstructions by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    val soldCount = sellerProfile?.itemsSoldCount ?: 0
    val currentLevel = sellerProfile?.sellerLevel ?: 1
    val levelInfo = SellerLevelCalculator.getLevelInfo(soldCount)
    val nextTarget = SellerLevelCalculator.getNextLevelTarget(soldCount)

    // Calculate dynamic earnings from orders
    val totalSalesGross = sellerOrders.sumOf { it.orderAmount }
    val totalCommissionCut = sellerOrders.sumOf { it.commissionAmount }
    val netEarnings = totalSalesGross - totalCommissionCut

    // Pure Hafte Ka Baqi Rs & Friday Clearance Breakdown for Seller
    val deliveredOrders = sellerOrders.filter {
        it.deliveryStatus == DeliveryStatus.DELIVERED && it.returnStatus != "ACCEPTED"
    }
    val pendingFridayOrders = deliveredOrders.filter { !it.isFridayCleared }
    val clearedOrders = deliveredOrders.filter { it.isFridayCleared }

    val twoDaysMillis = 2L * 24 * 60 * 60 * 1000L
    val now = System.currentTimeMillis()

    val readyForFridayOrders = pendingFridayOrders.filter {
        val delTime = it.deliveredAt ?: it.createdAt
        (now - delTime) >= twoDaysMillis
    }
    val inReturnReviewOrders = pendingFridayOrders.filter {
        val delTime = it.deliveredAt ?: it.createdAt
        (now - delTime) < twoDaysMillis
    }

    val baqiRsReady = readyForFridayOrders.sumOf { it.orderAmount - it.commissionAmount }
    val baqiRsInReview = inReturnReviewOrders.sumOf { it.orderAmount - it.commissionAmount }
    val totalBaqiRs = baqiRsReady + baqiRsInReview
    val totalClearedAmount = clearedOrders.sumOf { it.orderAmount - it.commissionAmount }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                // Tab 0: Kamai
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Payment, contentDescription = "Kamai") },
                    label = { Text("Kamai", fontSize = 11.sp, fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = DkkNavy,
                        indicatorColor = DkkNavy,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.testTag("seller_nav_earnings")
                )

                // Tab 1: Orders
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = {
                        if (sellerOrders.isNotEmpty()) {
                            BadgedBox(
                                badge = {
                                    Badge(
                                        containerColor = Color(0xFFF97316),
                                        contentColor = Color.White
                                    ) {
                                        Text("${sellerOrders.size}")
                                    }
                                }
                            ) {
                                Icon(Icons.Default.LocalShipping, contentDescription = "Orders")
                            }
                        } else {
                            Icon(Icons.Default.LocalShipping, contentDescription = "Orders")
                        }
                    },
                    label = { Text("Orders", fontSize = 11.sp, fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = DkkNavy,
                        indicatorColor = DkkNavy,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.testTag("seller_nav_orders")
                )

                // Tab 2: Listings
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Storefront, contentDescription = "Listings") },
                    label = { Text("Listings", fontSize = 11.sp, fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = DkkNavy,
                        indicatorColor = DkkNavy,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.testTag("seller_nav_listings")
                )

                // Tab 3: Live Audio Rooms (5-Mic Stage)
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = {
                        BadgedBox(
                            badge = {
                                Badge(
                                    containerColor = Color(0xFFEF4444),
                                    contentColor = Color.White
                                ) {
                                    Text("LIVE", fontSize = 8.sp, fontWeight = FontWeight.ExtraBold)
                                }
                            }
                        ) {
                            Icon(Icons.Default.Mic, contentDescription = "Live Rooms")
                        }
                    },
                    label = { Text("Rooms 🎙️", fontSize = 11.sp, fontWeight = if (selectedTab == 3) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = DkkNavy,
                        indicatorColor = DkkNavy,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.testTag("seller_nav_audio_rooms")
                )
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (selectedTab == 3) {
                // 🎙️ Live Audio Rooms Tab (Dedicated File Component)
                AudioRoomsCatalogScreen(
                    rooms = allAudioRooms,
                    onSelectRoom = { room -> onSelectRoom?.invoke(room) },
                    onCreateRoom = { title, videoUrl, countryFlag, countryName -> onCreateRoom?.invoke(title, videoUrl, countryFlag, countryName) },
                    isSeller = true
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    // Tab Row
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = DkkEmerald
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = { Text("📊 Kamai & Payouts", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                            modifier = Modifier.testTag("seller_tab_earnings")
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = { Text("Orders (${sellerOrders.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                            modifier = Modifier.testTag("seller_tab_orders")
                        )
                        Tab(
                            selected = selectedTab == 2,
                            onClick = { selectedTab = 2 },
                            text = { Text("Listings (${sellerProducts.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                            modifier = Modifier.testTag("seller_tab_listings")
                        )
                    }

                    when (selectedTab) {
            0 -> {
                // Section 1: Profile & Earnings Window (Clean & Spacious 8dp Grid)
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Single Unified Seller Store & Level Header Card (8dp padding)
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = DkkNavy),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(CircleShape)
                                                .background(DkkSlate)
                                                .border(1.5.dp, DkkGold, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Default.Storefront,
                                                contentDescription = null,
                                                tint = DkkGold,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }

                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Text(
                                                    text = sellerProfile?.businessName ?: user.name,
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Black,
                                                    fontSize = 14.sp
                                                )
                                                if (user.isProVip) {
                                                    ProVipBadge(small = true)
                                                }
                                            }
                                            Text(
                                                text = "${user.email} • ${user.phoneNumber}",
                                                color = Color(0xFF94A3B8),
                                                fontSize = 10.sp
                                            )
                                        }
                                    }

                                    // Biometric KYC badge
                                    if (sellerProfile?.faceScanVerified == true) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = EasypaisaGreen.copy(alpha = 0.2f),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, EasypaisaGreen)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                                            ) {
                                                Icon(Icons.Default.Verified, contentDescription = null, tint = EasypaisaGreen, modifier = Modifier.size(12.dp))
                                                Text("KYC OK", color = EasypaisaGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    } else {
                                        Button(
                                            onClick = onOpenFaceScan,
                                            colors = ButtonDefaults.buttonColors(containerColor = DkkGold),
                                            shape = RoundedCornerShape(6.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text("Verify KYC", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                // Level progress row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        SellerLevelBadge(level = currentLevel, soldCount = soldCount)
                                        Text("(${levelInfo.title})", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                    Text("$soldCount Sold", color = DkkGold, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }

                                val progress = if (levelInfo.maxSales > levelInfo.minSales) {
                                    ((soldCount - levelInfo.minSales).toFloat() / (levelInfo.maxSales - levelInfo.minSales).toFloat()).coerceIn(0f, 1f)
                                } else 1f

                                LinearProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(4.dp)
                                        .clip(RoundedCornerShape(2.dp)),
                                    color = DkkGold,
                                    trackColor = Color(0xFF0B132B)
                                )
                            }
                        }
                    }

                    // 🎙️ 5-Mic Live Lounge & Video Streaming Launch Card
                    if (onOpenAudioRoom != null) {
                        item {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF0F172A),
                                border = androidx.compose.foundation.BorderStroke(1.dp, DkkGold),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onOpenAudioRoom() }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = Color.Red
                                        ) {
                                            Text(
                                                "LIVE",
                                                color = Color.White,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Black,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }
                                        Column {
                                            Text(
                                                "🎙️ D.K.K. HNWI Audio Lounge (5-Mic)",
                                                color = DkkGold,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                "Speak on Stage • 1-Hour VIP Video Stream Looper",
                                                color = Color(0xFF94A3B8),
                                                fontSize = 10.sp
                                            )
                                        }
                                    }
                                    Icon(Icons.Default.Mic, contentDescription = null, tint = DkkGold, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }

                    // Compact Earnings Window: 3-Column Stats & Instant Payout
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(1.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Earnings Window",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 13.sp
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = EasypaisaGreen.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = "Easypaisa Instant Payout",
                                            color = EasypaisaGreen,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                // 3-Stat Compact Horizontal Row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    // Total Sales
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Column(modifier = Modifier.padding(6.dp)) {
                                            Text("Gross Sales", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(
                                                text = formatPkr(totalSalesGross),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }

                                    // Comm Cut
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFFFEF2F2),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Column(modifier = Modifier.padding(6.dp)) {
                                            Text("Comm. Cut", fontSize = 9.sp, color = Color(0xFFDC2626))
                                            Text(
                                                text = "- ${formatPkr(totalCommissionCut)}",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = Color(0xFFB91C1C)
                                            )
                                        }
                                    }

                                    // Net Balance
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFFE8F5E9),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, EasypaisaGreen),
                                        modifier = Modifier.weight(1.2f)
                                    ) {
                                        Column(modifier = Modifier.padding(6.dp)) {
                                            Text("Net Kamai", fontSize = 9.sp, color = Color(0xFF1B5E20), fontWeight = FontWeight.Bold)
                                            Text(
                                                text = formatPkr(netEarnings),
                                                fontWeight = FontWeight.Black,
                                                fontSize = 13.sp,
                                                color = DkkEmerald
                                            )
                                        }
                                    }
                                }

                                // Compact Tier Formula Line
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Tier Formula: ≤10k:10% • 10k-20k:8% • 20k-35k:7% • >35k:5%", fontSize = 9.sp, color = Color(0xFF64748B))
                                }
                            }
                        }
                    }

                    // Pure Hafte Ka "Baqi Rs" & Friday Clearance Status Card (Seller View)
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(18.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DkkEmerald.copy(alpha = 0.5f)),
                            elevation = CardDefaults.cardElevation(2.dp),
                            modifier = Modifier.fillMaxWidth().testTag("seller_friday_payout_status_card")
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
                                        Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = DkkGold, modifier = Modifier.size(24.dp))
                                        Column {
                                            Text(
                                                text = "Friday Payout Clearance",
                                                fontWeight = FontWeight.Black,
                                                fontSize = 15.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "جمعہ پے آؤٹ • ہفتہ وار باقی رقم کا حساب",
                                                fontSize = 11.sp,
                                                color = Color(0xFF64748B)
                                            )
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (baqiRsReady > 0) Color(0xFFECFDF5) else Color(0xFFF1F5F9),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, if (baqiRsReady > 0) DkkEmerald else Color(0xFFCBD5E1))
                                    ) {
                                        Text(
                                            text = if (baqiRsReady > 0) "🟢 Ready for Jumma" else "✓ In Review / Cleared",
                                            color = if (baqiRsReady > 0) DkkEmerald else Color(0xFF64748B),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Baqi Rs 3-Card Split
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Card 1: Baqi Rs Ready for this Friday
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color(0xFFECFDF5),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, DkkEmerald.copy(alpha = 0.5f)),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Text("Is Jumme Ki Baqi", fontSize = 10.sp, color = Color(0xFF047857), fontWeight = FontWeight.Bold)
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = formatPkr(baqiRsReady),
                                                fontWeight = FontWeight.Black,
                                                fontSize = 15.sp,
                                                color = DkkEmerald
                                            )
                                            Text("${readyForFridayOrders.size} orders ready", fontSize = 9.sp, color = Color(0xFF64748B))
                                        }
                                    }

                                    // Card 2: In 2-Day Return Review
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color(0xFFFFFBEB),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.4f)),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Text("2-Din Return Window", fontSize = 10.sp, color = Color(0xFFB45309), fontWeight = FontWeight.Bold)
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = formatPkr(baqiRsInReview),
                                                fontWeight = FontWeight.Black,
                                                fontSize = 15.sp,
                                                color = Color(0xFFD97706)
                                            )
                                            Text("${inReturnReviewOrders.size} orders waiting", fontSize = 9.sp, color = Color(0xFF64748B))
                                        }
                                    }

                                    // Card 3: Cleared on Past Fridays
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color(0xFFF8FAFC),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Text("Pichlay Jummay", fontSize = 10.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = formatPkr(totalClearedAmount),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = Color(0xFF334155)
                                            )
                                            Text("${clearedOrders.size} cleared", fontSize = 9.sp, color = Color(0xFF94A3B8))
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFFF8FAFC),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(
                                            text = "📌 Friday Clearance Nizaam (ادائیگی کا ضابطہ):",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = DkkNavy
                                        )
                                        Text(
                                            text = "• Har Friday ko platform owner clearance list se aapki baqi raqam aapke bank/Easypaisa account mein seedha bhejta hai.",
                                            fontSize = 10.sp,
                                            color = Color(0xFF475569)
                                        )
                                        Text(
                                            text = "• Delivery ke baad buyer ke paas 2 din ka return window hota hai. 2 din guzarne par raqam foran is Jumme ke clearance payout mein shamil ho jati hai.",
                                            fontSize = 10.sp,
                                            color = Color(0xFF475569)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Bank & Easypaisa Account Details for Weekly Friday Payouts
                    item {
                        var isEditingBank by remember {
                            mutableStateOf(
                                (sellerProfile?.bankAccountNumber.isNullOrBlank() && sellerProfile?.easypaisaNumber.isNullOrBlank())
                            )
                        }
                        var inputBankName by remember(sellerProfile) { mutableStateOf(sellerProfile?.bankName ?: "Meezan Bank") }
                        var inputAccountTitle by remember(sellerProfile) { mutableStateOf(sellerProfile?.bankAccountTitle ?: sellerProfile?.businessName ?: user.name) }
                        var inputAccountNumber by remember(sellerProfile) { mutableStateOf(sellerProfile?.bankAccountNumber ?: "") }
                        var inputIban by remember(sellerProfile) { mutableStateOf(sellerProfile?.bankIban ?: "") }
                        var inputEasypaisaNumber by remember(sellerProfile) { mutableStateOf(sellerProfile?.easypaisaNumber ?: user.phoneNumber) }
                        var inputEasypaisaTitle by remember(sellerProfile) { mutableStateOf(sellerProfile?.easypaisaTitle ?: user.name) }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = DkkNavy),
                            shape = RoundedCornerShape(18.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DkkGold.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth().testTag("seller_bank_details_card")
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
                                        Icon(Icons.Default.AccountBalance, contentDescription = null, tint = DkkGold, modifier = Modifier.size(24.dp))
                                        Column {
                                            Text(
                                                text = "Bank & Easypaisa Account",
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp
                                            )
                                            Text(
                                                text = "بینک اور ایزی پیسہ تفصیلات برائے ادائیگی",
                                                color = Color(0xFF94A3B8),
                                                fontSize = 11.sp
                                            )
                                        }
                                    }

                                    if (!isEditingBank) {
                                        IconButton(
                                            onClick = { isEditingBank = true },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(Icons.Default.Edit, contentDescription = "Edit Bank Details", tint = DkkGold, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Friday Payout & 2-Day Return Rule Notice Banner
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFF0F172A),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, DkkEmerald.copy(alpha = 0.5f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(Icons.Default.Schedule, contentDescription = null, tint = DkkGold, modifier = Modifier.size(16.dp))
                                            Text(
                                                text = "🗓️ Amount Friday ko send hogi",
                                                color = DkkGold,
                                                fontWeight = FontWeight.Black,
                                                fontSize = 12.sp
                                            )
                                        }
                                        Text(
                                            text = "Aapke farokht shuda items ki raqam har jummay (Friday) ko seedha aapke diye gaye bank ya Easypaisa account mein bheji jayegi.",
                                            color = Color(0xFFCBD5E1),
                                            fontSize = 11.sp,
                                            lineHeight = 15.sp
                                        )

                                        Spacer(modifier = Modifier.height(2.dp))

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(Icons.Default.LocalShipping, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(16.dp))
                                            Text(
                                                text = "📦 Delivery ke 2 din honge agar koi waps karna chahye",
                                                color = Color(0xFF38BDF8),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp
                                            )
                                        }
                                        Text(
                                            text = "Har delivery ke baad kharidar (buyer) ke paas kisi bhi nuqs ya kharabi par 2 din ka return window hoga. 2 din pure hote hi escrow se raqam release ho jayegi.",
                                            color = Color(0xFF94A3B8),
                                            fontSize = 10.sp,
                                            lineHeight = 14.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                if (isEditingBank) {
                                    // Editable form fields
                                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        OutlinedTextField(
                                            value = inputBankName,
                                            onValueChange = { inputBankName = it },
                                            label = { Text("Bank Name (e.g. Meezan, JS Bank, HBL)", fontSize = 11.sp) },
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth().testTag("seller_bank_name_input")
                                        )
                                        OutlinedTextField(
                                            value = inputAccountTitle,
                                            onValueChange = { inputAccountTitle = it },
                                            label = { Text("Bank Account Title (نام برائے بینک)", fontSize = 11.sp) },
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth().testTag("seller_account_title_input")
                                        )
                                        OutlinedTextField(
                                            value = inputAccountNumber,
                                            onValueChange = { inputAccountNumber = it },
                                            label = { Text("Account Number / IBAN (اکاؤنٹ نمبر)", fontSize = 11.sp) },
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth().testTag("seller_account_num_input")
                                        )

                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Easypaisa Account Option:",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = EasypaisaGreen
                                        )

                                        OutlinedTextField(
                                            value = inputEasypaisaNumber,
                                            onValueChange = { inputEasypaisaNumber = it },
                                            label = { Text("Easypaisa Mobile Number (03XXXXXXXXX)", fontSize = 11.sp) },
                                            keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone),
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth().testTag("seller_easypaisa_num_input")
                                        )
                                        OutlinedTextField(
                                            value = inputEasypaisaTitle,
                                            onValueChange = { inputEasypaisaTitle = it },
                                            label = { Text("Easypaisa Account Title (ایزی پیسہ ٹائٹل)", fontSize = 11.sp) },
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth().testTag("seller_easypaisa_title_input")
                                        )

                                        Button(
                                            onClick = {
                                                if (onUpdatePayoutDetails != null) {
                                                    onUpdatePayoutDetails(
                                                        inputBankName.trim(),
                                                        inputAccountTitle.trim(),
                                                        inputAccountNumber.trim(),
                                                        inputIban.trim(),
                                                        inputEasypaisaNumber.trim(),
                                                        inputEasypaisaTitle.trim()
                                                    )
                                                }
                                                isEditingBank = false
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = DkkGold),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.fillMaxWidth().testTag("save_bank_details_btn")
                                        ) {
                                            Text(
                                                text = "Save Payout Details (تفصیلات محفوظ کریں) ✓",
                                                color = DkkNavy,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                } else {
                                    // Saved Preview
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = Color(0xFF162038),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2E3D66)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                    Text("Bank:", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                                    Text(inputBankName.ifBlank { "Not set" }, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                                }
                                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                    Text("Account Title:", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                                    Text(inputAccountTitle.ifBlank { "Not set" }, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                                }
                                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                    Text("Account / IBAN:", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                                    Text(inputAccountNumber.ifBlank { "Not set" }, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DkkGold)
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                    Text("Easypaisa No:", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                                    Text(inputEasypaisaNumber.ifBlank { "Not set" }, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EasypaisaGreen)
                                                }
                                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                    Text("Easypaisa Title:", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                                    Text(inputEasypaisaTitle.ifBlank { "Not set" }, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                                }
                                            }
                                        }

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = DkkEmerald, modifier = Modifier.size(16.dp))
                                                Text("Payout Details Verified ✓", color = DkkEmerald, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }

                                            OutlinedButton(
                                                onClick = { isEditingBank = true },
                                                shape = RoundedCornerShape(8.dp),
                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = DkkGold),
                                                border = androidx.compose.foundation.BorderStroke(1.dp, DkkGold)
                                            ) {
                                                Text("Change", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // VIP Buy Card: Procedure & Benefits
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DkkGold.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = DkkGold, modifier = Modifier.size(24.dp))
                                        Text("DKK Pro VIP Status", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    }
                                    if (user.isProVip) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = DkkGold.copy(alpha = 0.2f),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, DkkGold)
                                        ) {
                                            Text("ACTIVE VIP ✓", color = DkkGold, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                        }
                                    } else {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color(0xFFFEF3C7)
                                        ) {
                                            Text("Rs 5,000 / One-time", color = Color(0xFF92400E), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "VIP perks: 15-second product videos (≤3MB), 1 high-res photo, daily 5 video uploads limit & top homepage feed boost.",
                                    color = Color(0xFFCBD5E1),
                                    fontSize = 11.sp,
                                    lineHeight = 16.sp
                                )

                                 Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { showVipInstructions = true },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = DkkGold),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, DkkGold),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("VIP Buy Karne Ka Tarika", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }

                                    if (onOpenSupportChat != null) {
                                        Button(
                                            onClick = onOpenSupportChat,
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = DkkEmerald),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(Icons.Default.Shield, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Admin Help Desk", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Quick Action: Post New Product
                    item {
                        Button(
                            onClick = onOpenPostProduct,
                            colors = ButtonDefaults.buttonColors(containerColor = DkkEmerald),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("seller_post_product_btn")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Post New Listing (Title, Price, Pro VIP)", fontWeight = FontWeight.Bold)
                        }
                    }

                    // Account Security & Log Out Card
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth().testTag("seller_account_security_card")
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(Icons.Default.PowerSettingsNew, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(20.dp))
                                        Text(
                                            text = "Account Security & Session",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFFF1F5F9)
                                    ) {
                                        Text(
                                            text = user.phoneNumber,
                                            fontSize = 10.sp,
                                            color = Color(0xFF64748B),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = "Aapka DKK seller account mehfooz hai. Kisi doosre account se login karne ya is portal se bahar jane ke liye neeche button se sign out karein.",
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B)
                                )

                                if (onLogout != null) {
                                    OutlinedButton(
                                        onClick = { showLogoutDialog = true },
                                        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFDC2626)),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(46.dp)
                                            .testTag("seller_card_logout_btn")
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Sign Out of Seller Portal (لاگ آؤٹ کریں)",
                                            fontWeight = FontWeight.Black,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            1 -> {
                // Section 2: Orders & OTP Delivery Verification
                if (sellerOrders.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                        Text("No customer orders yet. Post items to receive orders.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(sellerOrders) { order ->
                            var enteredOtp by remember { mutableStateOf("") }

                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                shape = RoundedCornerShape(16.dp),
                                elevation = CardDefaults.cardElevation(2.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("seller_order_card_${order.orderId}")
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = order.orderId,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color(order.deliveryStatus.colorHex).copy(alpha = 0.15f),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(order.deliveryStatus.colorHex))
                                        ) {
                                            Text(
                                                text = order.deliveryStatus.label,
                                                color = Color(order.deliveryStatus.colorHex),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(text = order.productTitle, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                    Text(text = "Buyer: ${order.buyerPhone}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text("Price: ${formatPkr(order.orderAmount)}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Text(
                                                text = "Tier Comm: -${formatPkr(order.commissionAmount)} (${order.commissionRatePercent.toInt()}%)",
                                                color = Color(0xFFDC2626),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text("Net Seller Payout", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(
                                                text = formatPkr(order.orderAmount - order.commissionAmount),
                                                fontWeight = FontWeight.Black,
                                                color = DkkEmerald,
                                                fontSize = 15.sp
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Order Status Actions: Pending -> Dispatch -> Deliver with OTP
                                    when (order.deliveryStatus) {
                                        DeliveryStatus.PENDING -> {
                                            Button(
                                                onClick = { onDispatchOrder(order.orderId) },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                                                shape = RoundedCornerShape(10.dp),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .testTag("dispatch_btn_${order.orderId}")
                                            ) {
                                                Icon(Icons.Default.LocalShipping, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("Mark Dispatched / Hand to Courier", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            }
                                        }
                                        DeliveryStatus.DISPATCHED -> {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(10.dp))
                                                    .padding(10.dp)
                                            ) {
                                                Text(
                                                    text = "Enter Buyer's 4-Digit Handover OTP:",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    OutlinedTextField(
                                                        value = enteredOtp,
                                                        onValueChange = { if (it.length <= 4) enteredOtp = it },
                                                        placeholder = { Text("OTP (e.g. ${order.otpCode})") },
                                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                        singleLine = true,
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .testTag("otp_input_${order.orderId}")
                                                    )

                                                    Button(
                                                        onClick = {
                                                            onDeliverOrderWithOtp(order.orderId, enteredOtp)
                                                        },
                                                        enabled = enteredOtp.length == 4,
                                                        colors = ButtonDefaults.buttonColors(containerColor = EasypaisaGreen),
                                                        shape = RoundedCornerShape(10.dp),
                                                        modifier = Modifier.testTag("verify_otp_btn_${order.orderId}")
                                                    ) {
                                                        Text("Deliver & Settle", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                                    }
                                                }
                                            }
                                        }
                                        DeliveryStatus.DELIVERED -> {
                                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = Color(0xFFE8F5E9),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(8.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                    ) {
                                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EasypaisaGreen, modifier = Modifier.size(16.dp))
                                                        Text(
                                                            text = "Delivered & Verified ✓ Escrow Locked",
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = Color(0xFF1B5E20)
                                                        )
                                                    }
                                                }

                                                // 2-day return policy + Friday Payout
                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = Color(0xFF0F172A),
                                                    border = androidx.compose.foundation.BorderStroke(1.dp, DkkGold.copy(alpha = 0.3f)),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                                        Text(
                                                            text = "🗓️ Amount Friday ko send hogi",
                                                            color = DkkGold,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 10.sp
                                                        )
                                                        Text(
                                                            text = "📦 Delivery ke 2 din honge agar buyer waps karna chahye. 2-day inspection window khatam hote hi jummay ko payment aapke account me bhej di jayegi.",
                                                            color = Color(0xFFCBD5E1),
                                                            fontSize = 9.sp,
                                                            lineHeight = 13.sp
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                        DeliveryStatus.RETURN_REQUESTED -> {
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = Color(0xFFFEF3C7),
                                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B)),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                                    Text(
                                                        text = "⚠️ Buyer Return Request (Under 2-Day Policy)",
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 11.sp,
                                                        color = Color(0xFF92400E)
                                                    )
                                                    Text(
                                                        text = "Reason: ${order.returnReason ?: "Kharabi / defect reported"}. Evidence submitted to receipts_vault/ for Owner Desk review.",
                                                        fontSize = 10.sp,
                                                        color = Color(0xFF78350F)
                                                    )
                                                }
                                            }
                                        }
                                        DeliveryStatus.RETURNED -> {
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = Color(0xFFFFEBEE),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text(
                                                    text = "↩️ Order Returned & Refunded (2-Day Return Rule Enforced)",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFFC62828),
                                                    modifier = Modifier.padding(8.dp)
                                                )
                                            }
                                        }
                                        else -> {}
                                    }
                                }
                            }
                        }
                    }
                }
            }
            2 -> {
                // Section 3: My Catalog Listings
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Button(
                            onClick = onOpenPostProduct,
                            colors = ButtonDefaults.buttonColors(containerColor = DkkEmerald),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("seller_add_listing_btn")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Post New Listing", fontWeight = FontWeight.Bold)
                        }
                    }

                    items(sellerProducts) { prod ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(14.dp),
                            elevation = CardDefaults.cardElevation(2.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                AsyncImage(
                                    model = resolveProductImageModel(prod.imageUrl),
                                    contentDescription = prod.title,
                                    modifier = Modifier
                                        .size(70.dp)
                                        .clip(RoundedCornerShape(10.dp)),
                                    contentScale = ContentScale.Crop
                                )

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(
                                            text = prod.title,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        if (prod.isProVip) {
                                            ProVipBadge(small = true)
                                        }
                                    }
                                    Text(
                                        text = formatPkr(prod.price),
                                        fontWeight = FontWeight.Black,
                                        color = DkkEmerald,
                                        fontSize = 14.sp
                                    )
                                    TierCommissionBadge(orderAmount = prod.price)
                                }
                            }
                        }
                    }
                }
            }
            3 -> {
                // Section 4: 🎙️ Live 10-Mic Audio Rooms Catalog (D.K.K. Live Audio Style)
                AudioRoomsCatalogScreen(
                    rooms = allAudioRooms,
                    onSelectRoom = { room -> onSelectRoom?.invoke(room) },
                    onCreateRoom = { title, videoUrl, flag, name -> onCreateRoom?.invoke(title, videoUrl, flag, name) },
                    isSeller = true
                )
            }
        }
    }
}
}
}

    if (showVipInstructions) {
        val clipboardManager = LocalClipboardManager.current
        AlertDialog(
            onDismissRequest = { showVipInstructions = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = DkkGold)
                    Text("VIP Buy Karne Ka Tarika", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "DKK Marketing Pro VIP Membership Hasil Karne Ke Asaan Marahil:",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Step 1
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("1. Fee Transfer (Rs 5,000 / One-time)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = DkkEmerald)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("DKK Official JS Bank Account:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("A/C: 0002775314 (JS Bank)", fontWeight = FontWeight.Black, fontSize = 12.sp)
                                IconButton(
                                    onClick = { clipboardManager.setText(AnnotatedString("0002775314")) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(16.dp))
                                }
                            }
                            Text("Title: Daro Khan DKK Marketing", fontSize = 10.sp, color = Color(0xFF64748B))
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Easypaisa: 03273856001 (Daro Khan)", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = DkkEmerald)
                                IconButton(
                                    onClick = { clipboardManager.setText(AnnotatedString("03273856001")) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy Easypaisa", modifier = Modifier.size(16.dp), tint = DkkEmerald)
                                }
                            }
                        }
                    }

                    // Step 2
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("2. Payment Slip / Screenshot (In-App)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = DkkEmerald)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Payment karne ke baad transaction slip ya TRX ID niche diye gaye 'Admin Help Desk' button se direct app mein upload karein.", fontSize = 11.sp)
                        }
                    }

                    // Step 3
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("3. Owner Desk Instant Approval", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = DkkEmerald)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Admin desk slip verify karke aapka account foran Pro VIP mein upgrade kar dega.", fontSize = 11.sp)
                        }
                    }

                    // VIP Benefits reminder
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFFEF3C7),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("⭐ VIP Media Upload Sahuliyat:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF92400E))
                            Text("• 15 Second tak ki video upload karein (Size max 3MB)", fontSize = 11.sp, color = Color(0xFF78350F))
                            Text("• Har listing ke sath 1 HD picture", fontSize = 11.sp, color = Color(0xFF78350F))
                            Text("• Din me rozana 5 videos upload kar sakte hain", fontSize = 11.sp, color = Color(0xFF78350F))
                            Text("• Marketplace feed par top-priority showcase", fontSize = 11.sp, color = Color(0xFF78350F))
                        }
                    }
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (onOpenSupportChat != null) {
                        OutlinedButton(
                            onClick = {
                                showVipInstructions = false
                                onOpenSupportChat()
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = DkkGold),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DkkGold),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Slip Send Karein", fontWeight = FontWeight.Bold)
                        }
                    }
                    Button(
                        onClick = { showVipInstructions = false },
                        colors = ButtonDefaults.buttonColors(containerColor = DkkEmerald),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Theek Hai", fontWeight = FontWeight.Bold)
                    }
                }
            }
        )
    }

    // Logout Confirmation Dialog
    if (showLogoutDialog && onLogout != null) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = null,
                        tint = Color(0xFFDC2626),
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Seller Portal se Log Out?",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                }
            },
            text = {
                Text(
                    text = "Kya aap waqai seller portal se sign out karna chahte hain? Aapka business profile aur orders mehfooz rahenge.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("btn_confirm_seller_logout")
                ) {
                    Text("Haan, Log Out Karein", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurface)
                }
            }
        )
    }
}

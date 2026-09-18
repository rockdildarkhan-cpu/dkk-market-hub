package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DeliveryStatus
import com.example.data.model.OrderEntity
import com.example.data.model.SellerProfileEntity
import com.example.data.model.UserEntity
import com.example.ui.components.SellerLevelBadge
import com.example.ui.components.formatPkr
import com.example.ui.theme.DkkEmerald
import com.example.ui.theme.DkkEmeraldLight
import com.example.ui.theme.DkkGold
import com.example.ui.theme.DkkNavy
import com.example.ui.theme.DkkSlate
import com.example.ui.theme.EasypaisaGreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Data representation for a seller's weekly Friday payout clearance record
 */
data class SellerFridayClearanceItem(
    val seller: SellerProfileEntity,
    val user: UserEntity?,
    val deliveredOrders: List<OrderEntity>,
    val readyOrders: List<OrderEntity>,
    val inReviewOrders: List<OrderEntity>,
    val alreadyClearedOrders: List<OrderEntity>,
    val baqiRsReady: Double,
    val baqiRsInReview: Double,
    val totalBaqiRs: Double,
    val clearedEarnings: Double
)

/**
 * 🗓️ Friday Payout Clearance System (جمعہ پے آؤٹ کلیئرنس ڈیسک)
 *
 * Provides:
 * 1. Pure Hafte Ka "Baqi Rs" Ka Hisab:
 *    - Total Baqi Rs ready for clearance this Friday (delivers past 2-day return policy)
 *    - Total locked in 2-day return window (future clearance)
 *    - DKK platform commission auto-retained
 *    - Total gross weekly order volume
 * 2. Har Jumme Ko Sab Sellers Ko Paise Bhejne Wali Clearance List:
 *    - Complete list of sellers with Bank details (Account Title, IBAN) & Easypaisa details
 *    - 1-Click Copy for Account Number / IBAN to easily paste into banking apps (Raast / IBFT)
 *    - Individual "Bhejen / Clear Friday Payout" action
 *    - Master "🚀 Clear All Friday Payouts (تمام سلرز کو بھیجیں)" with live confirmation dialog
 */
@Composable
fun FridayPayoutClearanceView(
    allSellers: List<SellerProfileEntity>,
    allUsers: List<UserEntity>,
    allOrders: List<OrderEntity>,
    onClearSellerFridayPayout: (sellerId: String, amount: Double) -> Unit,
    onClearAllFridayPayouts: (totalAmount: Double) -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    var copiedLabel by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(0) } // 0: All, 1: Ready to Pay (Baqi Rs > 0), 2: In 2-Day Review, 3: Cleared
    var showClearAllConfirmDialog by remember { mutableStateOf(false) }
    var sellerToClearIndividual by remember { mutableStateOf<SellerFridayClearanceItem?>(null) }

    val twoDaysMillis = 2L * 24 * 60 * 60 * 1000L
    val now = System.currentTimeMillis()

    // Aggregate clearance items per seller
    val clearanceItems = remember(allSellers, allUsers, allOrders) {
        val userMap = allUsers.associateBy { it.uid }

        allSellers.map { seller ->
            val sellerOrders = allOrders.filter { it.sellerId == seller.uid }
            val deliveredOrders = sellerOrders.filter {
                it.deliveryStatus == DeliveryStatus.DELIVERED && it.returnStatus != "ACCEPTED"
            }

            val pendingFridayOrders = deliveredOrders.filter { !it.isFridayCleared }
            val alreadyClearedOrders = deliveredOrders.filter { it.isFridayCleared }

            val readyOrders = pendingFridayOrders.filter {
                val delTime = it.deliveredAt ?: it.createdAt
                (now - delTime) >= twoDaysMillis
            }

            val inReviewOrders = pendingFridayOrders.filter {
                val delTime = it.deliveredAt ?: it.createdAt
                (now - delTime) < twoDaysMillis
            }

            val baqiRsReady = readyOrders.sumOf { it.orderAmount - it.commissionAmount }
            val baqiRsInReview = inReviewOrders.sumOf { it.orderAmount - it.commissionAmount }
            val totalBaqiRs = baqiRsReady + baqiRsInReview
            val clearedEarnings = alreadyClearedOrders.sumOf { it.orderAmount - it.commissionAmount }

            SellerFridayClearanceItem(
                seller = seller,
                user = userMap[seller.uid],
                deliveredOrders = deliveredOrders,
                readyOrders = readyOrders,
                inReviewOrders = inReviewOrders,
                alreadyClearedOrders = alreadyClearedOrders,
                baqiRsReady = baqiRsReady,
                baqiRsInReview = baqiRsInReview,
                totalBaqiRs = totalBaqiRs,
                clearedEarnings = clearedEarnings
            )
        }
    }

    // Platform-wide Weekly Aggregations
    val totalReadyBaqiRs = clearanceItems.sumOf { it.baqiRsReady }
    val totalInReviewBaqiRs = clearanceItems.sumOf { it.baqiRsInReview }
    val totalClearedRs = clearanceItems.sumOf { it.clearedEarnings }
    val eligibleSellersCount = clearanceItems.count { it.baqiRsReady > 0 }
    val totalDeliveredOrdersCount = clearanceItems.sumOf { it.deliveredOrders.size }
    val totalCommissionSecured = allOrders
        .filter { it.deliveryStatus == DeliveryStatus.DELIVERED }
        .sumOf { it.commissionAmount }

    // Filtered items
    val filteredItems = clearanceItems.filter { item ->
        val matchesSearch = item.seller.businessName.contains(searchQuery, ignoreCase = true) ||
                (item.user?.name?.contains(searchQuery, ignoreCase = true) == true) ||
                (item.user?.phoneNumber?.contains(searchQuery, ignoreCase = true) == true) ||
                item.seller.bankName.contains(searchQuery, ignoreCase = true) ||
                item.seller.easypaisaNumber.contains(searchQuery, ignoreCase = true)

        val matchesFilter = when (selectedFilter) {
            1 -> item.baqiRsReady > 0
            2 -> item.baqiRsInReview > 0 && item.baqiRsReady == 0.0
            3 -> item.alreadyClearedOrders.isNotEmpty() && item.baqiRsReady == 0.0
            else -> true
        }

        matchesSearch && matchesFilter
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("friday_payout_clearance_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Executive Weekly Clearance Header Banner
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DkkNavy),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, DkkGold.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
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
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(DkkSlate)
                                    .border(1.5.dp, DkkGold, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    tint = DkkGold,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Friday Payout Clearance",
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 18.sp
                                )
                                Text(
                                    text = "جمعہ پے آؤٹ ڈیسک • ہفتہ وار کلیئرنس لسٹ",
                                    color = DkkGold,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = EasypaisaGreen.copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, EasypaisaGreen)
                        ) {
                            Text(
                                text = "Every Friday Cycle",
                                color = EasypaisaGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Pure Hafte Ka "Baqi Rs" Ka Hisab Summary
                    Text(
                        text = "PURE HAFTE KA BAQI RS HISAB (WEEKLY ACCOUNTING)",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Card 1: Baqi Rs Ready for Friday
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF0F172A),
                            border = BorderStroke(1.5.dp, if (totalReadyBaqiRs > 0) DkkEmerald else Color(0xFF334155)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(Icons.Default.Payment, contentDescription = null, tint = DkkEmerald, modifier = Modifier.size(14.dp))
                                    Text("Baqi Rs Ready", color = DkkEmerald, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = formatPkr(totalReadyBaqiRs),
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 17.sp
                                )
                                Text(
                                    text = "$eligibleSellersCount Sellers to Pay",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 10.sp
                                )
                            }
                        }

                        // Card 2: In 2-Day Return Review
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF0F172A),
                            border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.5f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(Icons.Default.HourglassTop, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(14.dp))
                                    Text("2-Day Window", color = Color(0xFFF59E0B), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = formatPkr(totalInReviewBaqiRs),
                                    color = Color(0xFFFDE68A),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 17.sp
                                )
                                Text(
                                    text = "Locked Escrow",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Secondary Platform Stats
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "🏢 DKK Commission Kept: ${formatPkr(totalCommissionSecured)}",
                            color = DkkGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "✓ Cleared Past Fridays: ${formatPkr(totalClearedRs)}",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Master Action Button: Clear All Friday Payouts
                    Button(
                        onClick = { showClearAllConfirmDialog = true },
                        enabled = totalReadyBaqiRs > 0,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DkkEmerald,
                            disabledContainerColor = Color(0xFF334155)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("btn_clear_all_friday_payouts")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (totalReadyBaqiRs > 0) Color.White else Color(0xFF64748B),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (totalReadyBaqiRs > 0) {
                                "🚀 Clear All Friday Payouts (${formatPkr(totalReadyBaqiRs)})"
                            } else {
                                "Sab Sellers Is Jumme Ke Liye Cleared Hain"
                            },
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp,
                            color = if (totalReadyBaqiRs > 0) Color.White else Color(0xFF94A3B8)
                        )
                    }
                }
            }
        }

        // 2. Search & Filter Bar
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search seller by store, phone, or bank...", fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = DkkSlate)
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_friday_payout_seller")
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedFilter == 0,
                        onClick = { selectedFilter = 0 },
                        label = { Text("All (${clearanceItems.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    )
                    FilterChip(
                        selected = selectedFilter == 1,
                        onClick = { selectedFilter = 1 },
                        label = { Text("Ready ($eligibleSellersCount)", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    )
                    FilterChip(
                        selected = selectedFilter == 2,
                        onClick = { selectedFilter = 2 },
                        label = { Text("In 2-Day Review", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                    )
                    FilterChip(
                        selected = selectedFilter == 3,
                        onClick = { selectedFilter = 3 },
                        label = { Text("Cleared", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                    )
                }
            }
        }

        // 3. Seller Clearance List Items
        if (filteredItems.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Koi Seller Clearance List Mein Nahi Mila", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("Filters ya search badal kar check karein.", fontSize = 12.sp, color = Color(0xFF64748B))
                    }
                }
            }
        } else {
            items(filteredItems, key = { it.seller.uid }) { item ->
                SellerClearanceCard(
                    item = item,
                    onCopyText = { label, text ->
                        clipboardManager.setText(AnnotatedString(text))
                        copiedLabel = label
                    },
                    onClearPayout = {
                        sellerToClearIndividual = item
                    }
                )
            }
        }
    }

    // Confirmation Modal: Clear All Friday Payouts
    if (showClearAllConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearAllConfirmDialog = false },
            title = {
                Text(
                    text = "Confirm Mass Friday Clearance?",
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Kya aap tamam mutalliqah $eligibleSellersCount sellers ko kul ${formatPkr(totalReadyBaqiRs)} transfer aur clear karna chahte hain?",
                        fontSize = 13.sp
                    )
                    Surface(
                        color = Color(0xFFF1F5F9),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("• Total Sellers: $eligibleSellersCount", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("• Total Baqi Rs: ${formatPkr(totalReadyBaqiRs)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DkkEmerald)
                            Text("• Platform DKK Commission Kept: ${formatPkr(totalCommissionSecured)}", fontSize = 11.sp, color = Color(0xFF64748B))
                            Text("• Yeh orders 'Friday Cleared' mark ho jayenge.", fontSize = 11.sp, color = Color(0xFF64748B))
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onClearAllFridayPayouts(totalReadyBaqiRs)
                        showClearAllConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DkkEmerald),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("btn_confirm_clear_all_dialog")
                ) {
                    Text("Confirm & Clear All", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearAllConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Confirmation Modal: Individual Seller Friday Payout
    sellerToClearIndividual?.let { item ->
        AlertDialog(
            onDismissRequest = { sellerToClearIndividual = null },
            title = {
                Text(
                    text = "Clear Friday Payout for ${item.seller.businessName}?",
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Aap is seller ko is Jumme ka Baqi Rs (${formatPkr(item.baqiRsReady)}) clear kar rahe hain.",
                        fontSize = 13.sp
                    )
                    Surface(
                        color = Color(0xFFF1F5F9),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Store: ${item.seller.businessName}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            if (item.seller.bankAccountNumber.isNotBlank()) {
                                Text("Bank: ${item.seller.bankName} (${item.seller.bankAccountNumber})", fontSize = 11.sp)
                                Text("Title: ${item.seller.bankAccountTitle}", fontSize = 11.sp)
                            }
                            if (item.seller.easypaisaNumber.isNotBlank()) {
                                Text("Easypaisa: ${item.seller.easypaisaNumber} (${item.seller.easypaisaTitle})", fontSize = 11.sp)
                            }
                            Text("Clearance Amount: ${formatPkr(item.baqiRsReady)}", fontWeight = FontWeight.Black, fontSize = 13.sp, color = DkkEmerald)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onClearSellerFridayPayout(item.seller.uid, item.baqiRsReady)
                        sellerToClearIndividual = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DkkEmerald),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("btn_confirm_individual_clear_dialog")
                ) {
                    Text("Bhejen / Mark Cleared", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { sellerToClearIndividual = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

/**
 * Individual Seller Clearance Card with Bank, Easypaisa details and Order Breakdown
 */
@Composable
fun SellerClearanceCard(
    item: SellerFridayClearanceItem,
    onCopyText: (label: String, text: String) -> Unit,
    onClearPayout: () -> Unit
) {
    var expandedOrders by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        border = BorderStroke(
            width = if (item.baqiRsReady > 0) 1.5.dp else 1.dp,
            color = if (item.baqiRsReady > 0) DkkEmerald else MaterialTheme.colorScheme.outlineVariant
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("seller_clearance_card_${item.seller.uid}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Store Name, Seller Level, Status Badge
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
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(DkkSlate),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Storefront,
                            contentDescription = null,
                            tint = DkkGold,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = item.seller.businessName,
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            SellerLevelBadge(level = item.seller.sellerLevel, soldCount = item.seller.itemsSoldCount)
                        }
                        Text(
                            text = "${item.user?.name ?: "Seller"} • ${item.user?.phoneNumber ?: item.seller.easypaisaNumber}",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }

                // Status Badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when {
                        item.baqiRsReady > 0 -> Color(0xFFECFDF5)
                        item.baqiRsInReview > 0 -> Color(0xFFFFFBEB)
                        else -> Color(0xFFF1F5F9)
                    },
                    border = BorderStroke(
                        1.dp,
                        when {
                            item.baqiRsReady > 0 -> DkkEmerald
                            item.baqiRsInReview > 0 -> Color(0xFFF59E0B)
                            else -> Color(0xFFCBD5E1)
                        }
                    )
                ) {
                    Text(
                        text = when {
                            item.baqiRsReady > 0 -> "🟢 READY FOR FRIDAY"
                            item.baqiRsInReview > 0 -> "🟡 2-DAY REVIEW"
                            else -> "✓ ALL CLEARED"
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = when {
                            item.baqiRsReady > 0 -> Color(0xFF047857)
                            item.baqiRsInReview > 0 -> Color(0xFFB45309)
                            else -> Color(0xFF475569)
                        },
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Bank & Easypaisa Payment Destination Panel (With 1-Click Copy!)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "PAYMENT DESTINATION (بینک / ایزی پیسہ)",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B)
                    )

                    // Bank Info
                    if (item.seller.bankAccountNumber.isNotBlank() || item.seller.bankIban.isNotBlank()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.AccountBalance, contentDescription = null, tint = DkkNavy, modifier = Modifier.size(16.dp))
                                Column {
                                    Text(
                                        text = "${item.seller.bankName.ifBlank { "Bank" }} • ${item.seller.bankAccountTitle}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    val acc = item.seller.bankIban.ifBlank { item.seller.bankAccountNumber }
                                    Text(
                                        text = acc,
                                        fontSize = 11.sp,
                                        color = Color(0xFF334155),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            val copyText = item.seller.bankIban.ifBlank { item.seller.bankAccountNumber }
                            IconButton(
                                onClick = { onCopyText("Bank Account", copyText) },
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy Bank Info", tint = DkkEmerald, modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    // Easypaisa Info
                    if (item.seller.easypaisaNumber.isNotBlank()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.Phone, contentDescription = null, tint = EasypaisaGreen, modifier = Modifier.size(16.dp))
                                Column {
                                    Text(
                                        text = "Easypaisa: ${item.seller.easypaisaTitle.ifBlank { item.seller.businessName }}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = item.seller.easypaisaNumber,
                                        fontSize = 11.sp,
                                        color = EasypaisaGreen,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            IconButton(
                                onClick = { onCopyText("Easypaisa Number", item.seller.easypaisaNumber) },
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy Easypaisa", tint = EasypaisaGreen, modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    if (item.seller.bankAccountNumber.isBlank() && item.seller.easypaisaNumber.isBlank()) {
                        Text(
                            text = "⚠️ Seller ne abhi bank ya Easypaisa details add nahi kiye.",
                            color = Color(0xFFDC2626),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Payout Amounts Grid (Clean Horizontal Cards)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Baqi Rs to Pay (Ready)
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFECFDF5),
                    border = BorderStroke(1.dp, DkkEmerald.copy(alpha = 0.5f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Baqi Rs (Friday)", fontSize = 11.sp, color = Color(0xFF047857), fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = formatPkr(item.baqiRsReady),
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            color = DkkEmerald
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("${item.readyOrders.size} orders ready", fontSize = 10.sp, color = Color(0xFF64748B))
                    }
                }

                // In 2-Day Review
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFFFFBEB),
                    border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.4f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("2-Day Review", fontSize = 11.sp, color = Color(0xFFB45309), fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = formatPkr(item.baqiRsInReview),
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            color = Color(0xFFD97706)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("${item.inReviewOrders.size} orders waiting", fontSize = 10.sp, color = Color(0xFF64748B))
                    }
                }
            }

            if (item.clearedEarnings > 0 || item.alreadyClearedOrders.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFF8FAFC),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Past Cleared Fridays:", fontSize = 11.sp, color = Color(0xFF64748B), fontWeight = FontWeight.SemiBold)
                        Text(
                            text = "${formatPkr(item.clearedEarnings)} (${item.alreadyClearedOrders.size} orders)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = Color(0xFF334155)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action: Toggle Orders Breakdown & Individual Clearance Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // View Breakdown Button
                TextButton(
                    onClick = { expandedOrders = !expandedOrders },
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (expandedOrders) "Hide Orders" else "View Orders (${item.deliveredOrders.size})",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = DkkNavy
                    )
                    Icon(
                        imageVector = if (expandedOrders) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = DkkNavy
                    )
                }

                // Send / Clear Friday Payout Button
                Button(
                    onClick = onClearPayout,
                    enabled = item.baqiRsReady > 0,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DkkEmerald,
                        disabledContainerColor = Color(0xFFE2E8F0)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("btn_clear_payout_${item.seller.uid}")
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = if (item.baqiRsReady > 0) Color.White else Color(0xFF94A3B8),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (item.baqiRsReady > 0) "Clear Friday Payout" else "Cleared ✓",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = if (item.baqiRsReady > 0) Color.White else Color(0xFF64748B)
                    )
                }
            }

            // Expandable Breakdown List of Orders
            AnimatedVisibility(visible = expandedOrders) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "ORDER BREAKDOWN (حساب کی تفصیل)",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B)
                    )

                    if (item.deliveredOrders.isEmpty()) {
                        Text("No delivered orders found for this seller.", fontSize = 11.sp, color = Color(0xFF94A3B8))
                    } else {
                        item.deliveredOrders.forEach { order ->
                            val netSeller = order.orderAmount - order.commissionAmount
                            val isReady = !order.isFridayCleared && ((System.currentTimeMillis() - (order.deliveredAt ?: order.createdAt)) >= 2L * 24 * 60 * 60 * 1000L)
                            val isInReview = !order.isFridayCleared && !isReady

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFF8FAFC),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = order.productTitle,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 11.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "ID: ${order.orderId} • Buyer: ${order.buyerPhone}",
                                            fontSize = 10.sp,
                                            color = Color(0xFF64748B)
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "Net: ${formatPkr(netSeller)}",
                                            fontWeight = FontWeight.Black,
                                            fontSize = 11.sp,
                                            color = if (isReady) DkkEmerald else if (isInReview) Color(0xFFD97706) else Color(0xFF475569)
                                        )
                                        Text(
                                            text = when {
                                                order.isFridayCleared -> "✓ Cleared"
                                                isReady -> "🟢 Ready"
                                                else -> "🟡 2-Day Review"
                                            },
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = when {
                                                order.isFridayCleared -> Color(0xFF475569)
                                                isReady -> DkkEmerald
                                                else -> Color(0xFFD97706)
                                            }
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
}

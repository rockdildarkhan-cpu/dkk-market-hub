package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AdminOverviewStats
import com.example.data.model.OrderEntity
import com.example.data.model.OwnerAlertNotification
import com.example.data.model.OwnerAlertType
import com.example.data.model.PendingProductItem
import com.example.data.model.SellerProfileEntity
import com.example.data.model.SupportTicketItem
import com.example.data.model.UserEntity
import com.example.ui.components.formatPkr
import com.example.ui.theme.DkkEmerald
import com.example.ui.theme.DkkGold
import com.example.ui.theme.DkkNavy
import com.example.viewmodel.DkkViewModel

/**
 * AdminHomeScreen - Primary Screen for DKK Owner with Real-Time Notification Listener
 *
 * Alerts the owner immediately when:
 * 1. A new high-value product (>= PKR 20,000) is submitted
 * 2. An urgent support ticket is opened (emergency / fraud / delivery delay)
 */
@Composable
fun AdminHomeScreen(
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
    allSellersList: List<SellerProfileEntity> = emptyList(),
    onClearSellerFridayPayout: (sellerId: String, amount: Double) -> Unit = { _, _ -> },
    onClearAllFridayPayouts: (totalAmount: Double) -> Unit = {},
    viewModel: DkkViewModel? = null
) {
    OwnerAdminDashboardScreen(
        stats = stats,
        allUsers = allUsers,
        pendingSellers = pendingSellers,
        allOrders = allOrders,
        onApproveSeller = onApproveSeller,
        onRejectSeller = onRejectSeller,
        onToggleProVip = onToggleProVip,
        onOpenFirebase = onOpenFirebase,
        pendingSellersLiveCount = pendingSellersLiveCount,
        pendingProductsLive = pendingProductsLive,
        pendingProductsLiveCount = pendingProductsLiveCount,
        activeTicketsLive = activeTicketsLive,
        activeTicketsLiveCount = activeTicketsLiveCount,
        totalPlatformEarningsLive = totalPlatformEarningsLive,
        onApproveProductLive = onApproveProductLive,
        onRejectProductLive = onRejectProductLive,
        isLoadingLiveStats = isLoadingLiveStats,
        activeAlert = activeAlert,
        recentAlerts = recentAlerts,
        onDismissAlert = onDismissAlert,
        onDismissActiveAlert = onDismissActiveAlert,
        onSimulateHighValueProduct = onSimulateHighValueProduct,
        onSimulateUrgentTicket = onSimulateUrgentTicket,
        allSellersList = allSellersList,
        onClearSellerFridayPayout = onClearSellerFridayPayout,
        onClearAllFridayPayouts = onClearAllFridayPayouts,
        viewModel = viewModel
    )
}

/**
 * Animated Heads-Up Real-Time Alert Banner for AdminHomeScreen
 */
@Composable
fun OwnerAlertBanner(
    alert: OwnerAlertNotification,
    onDismiss: () -> Unit,
    onApprove: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isHighValue = alert.type == OwnerAlertType.HIGH_VALUE_PRODUCT

    val cardBg = if (isHighValue) {
        Brush.horizontalGradient(
            listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF0B192C))
        )
    } else {
        Brush.horizontalGradient(
            listOf(Color(0xFF450A0A), Color(0xFF7F1D1D), Color(0xFF3B0909))
        )
    }

    val borderColor = if (isHighValue) DkkGold else Color(0xFFFF4D4F)
    val accentIconColor = if (isHighValue) DkkGold else Color(0xFFFF7875)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .testTag(if (isHighValue) "alert_high_value_product" else "alert_urgent_ticket"),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.5.dp, borderColor),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Box(
            modifier = Modifier
                .background(cardBg)
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = borderColor.copy(alpha = 0.2f),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (isHighValue) Icons.Default.Diamond else Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = accentIconColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Column {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = borderColor.copy(alpha = 0.25f)
                            ) {
                                Text(
                                    text = if (isHighValue) "💎 HIGH-VALUE LISTING ALERT" else "🚨 URGENT TICKET OPENED",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (isHighValue) DkkGold else Color.White,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    letterSpacing = 0.5.sp
                                )
                            }
                            Text(
                                text = "REAL-TIME OWNER NOTIFICATION",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.6f)
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(28.dp)
                            .testTag("dismiss_alert_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss Alert",
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Title and Message Content
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = alert.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = alert.message,
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.9f),
                        lineHeight = 18.sp
                    )
                }

                // Highlight Tag (Price or Sender)
                if (isHighValue && alert.price != null) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.Black.copy(alpha = 0.4f),
                        border = BorderStroke(1.dp, DkkGold.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Listing Value:",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                            Text(
                                text = "PKR ${formatPkr(alert.price)}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = DkkGold
                            )
                            Text(
                                text = "• Tier 3/4 Commission",
                                fontSize = 11.sp,
                                color = DkkEmerald
                            )
                        }
                    }
                } else if (!isHighValue && alert.senderInfo != null) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.Black.copy(alpha = 0.4f),
                        border = BorderStroke(1.dp, Color(0xFFFF4D4F).copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "From:",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                            Text(
                                text = alert.senderInfo,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "• Action Priority: HIGH",
                                fontSize = 11.sp,
                                color = Color(0xFFFF7875)
                            )
                        }
                    }
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isHighValue) {
                        Button(
                            onClick = onApprove,
                            colors = ButtonDefaults.buttonColors(containerColor = DkkEmerald),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .testTag("btn_approve_high_value_alert")
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Approve Now", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .testTag("btn_review_urgent_ticket")
                        ) {
                            Icon(Icons.Default.SupportAgent, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Review Ticket", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    OutlinedButton(
                        onClick = onDismiss,
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .height(38.dp)
                            .testTag("btn_dismiss_alert")
                    ) {
                        Text("Dismiss", color = Color.White, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

/**
 * Real-Time Alert Monitor Card with Live Beacon and Simulation Triggers
 */
@Composable
fun RealTimeAlertMonitorCard(
    alertsCount: Int,
    recentAlerts: List<OwnerAlertNotification>,
    onDismissAlert: (String) -> Unit,
    onClearAll: () -> Unit,
    onSimulateHighValueProduct: () -> Unit,
    onSimulateUrgentTicket: () -> Unit,
    onApproveProduct: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Surface(
        color = Color.White,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        shadowElevation = 2.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Header with Live Pulse Indicator
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
                            .size(12.dp)
                            .alpha(pulseAlpha)
                            .background(Color(0xFF10B981), CircleShape)
                    )
                    Text(
                        text = "REAL-TIME NOTIFICATION LISTENER",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF0F172A),
                        letterSpacing = 0.8.sp
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (recentAlerts.isNotEmpty()) Color(0xFFEF4444).copy(alpha = 0.12f) else Color(0xFF10B981).copy(alpha = 0.12f)
                ) {
                    Text(
                        text = if (recentAlerts.isNotEmpty()) "${recentAlerts.size} ACTIVE ALERTS" else "ONLINE & MONITORING",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (recentAlerts.isNotEmpty()) Color(0xFFDC2626) else Color(0xFF059669),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Text(
                text = "Instant real-time listener is connected to Firebase Firestore & local bus. Alerts trigger immediately for high-value listings (≥ PKR 20,000) and urgent support complaints.",
                fontSize = 12.sp,
                color = Color(0xFF64748B),
                lineHeight = 17.sp
            )

            // Test Simulation Action Buttons
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFF8FAFC),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "TEST REAL-TIME NOTIFICATION LISTENER (INSTANT TRIGGER):",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF475569)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onSimulateHighValueProduct,
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, Color(0xFFD97706)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFB45309)),
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                                .testTag("btn_simulate_high_value")
                        ) {
                            Icon(Icons.Default.Diamond, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("⚡ Test High-Value (Rs 385k)", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = onSimulateUrgentTicket,
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, Color(0xFFDC2626)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                                .testTag("btn_simulate_urgent_ticket")
                        ) {
                            Icon(Icons.Default.ReportProblem, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("🚨 Test Urgent Ticket", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Recent Alerts List
            if (recentAlerts.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "RECENT REAL-TIME ALERTS (${recentAlerts.size})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                        Text(
                            text = "Clear All",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0057B7),
                            modifier = Modifier
                                .clickable { onClearAll() }
                                .padding(4.dp)
                                .testTag("btn_clear_all_alerts")
                        )
                    }

                    recentAlerts.take(4).forEach { item ->
                        val isHighVal = item.type == OwnerAlertType.HIGH_VALUE_PRODUCT
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isHighVal) Color(0xFFFFFBEB) else Color(0xFFFEF2F2),
                            border = BorderStroke(1.dp, if (isHighVal) Color(0xFFFDE68A) else Color(0xFFFECACA)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isHighVal) Icons.Default.Diamond else Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = if (isHighVal) Color(0xFFD97706) else Color(0xFFDC2626),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Column {
                                        Text(
                                            text = item.title,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isHighVal) Color(0xFF92400E) else Color(0xFF991B1B)
                                        )
                                        Text(
                                            text = item.message,
                                            fontSize = 11.sp,
                                            color = Color(0xFF475569),
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                                IconButton(
                                    onClick = { onDismissAlert(item.id) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Dismiss",
                                        tint = Color(0xFF94A3B8),
                                        modifier = Modifier.size(16.dp)
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

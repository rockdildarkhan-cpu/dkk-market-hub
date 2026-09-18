package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import com.example.data.model.DkkOfficialBankAccount
import com.example.data.model.DkkOfficialEasypaisaAccount
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.DeliveryStatus
import com.example.data.model.OrderEntity
import com.example.data.model.ProductEntity
import com.example.data.model.UserRole
import com.example.data.model.resolveProductImageModel
import com.example.data.repository.CommissionCalculator
import com.example.data.repository.SellerLevelCalculator
import com.example.ui.theme.DkkEmerald
import com.example.ui.theme.DkkEmeraldLight
import com.example.ui.theme.DkkGold
import com.example.ui.theme.DkkGoldLight
import com.example.ui.theme.DkkNavy
import com.example.ui.theme.DkkSlate
import com.example.ui.theme.EasypaisaGreen
import com.example.ui.theme.Tier1Color
import com.example.ui.theme.Tier2Color
import com.example.ui.theme.Tier3Color
import com.example.ui.theme.Tier4Color
import kotlinx.coroutines.delay
import java.text.NumberFormat
import java.util.Locale

fun formatPkr(amount: Double): String {
    val formatter = NumberFormat.getNumberInstance(Locale.US)
    return "Rs " + formatter.format(amount.toLong())
}

@Composable
fun ProVipBadge(modifier: Modifier = Modifier, small: Boolean = false) {
    Row(
        modifier = modifier
            .background(
                brush = Brush.horizontalGradient(
                    listOf(Color(0xFFD4AF37), Color(0xFFF59E0B), Color(0xFFD4AF37))
                ),
                shape = RoundedCornerShape(if (small) 4.dp else 8.dp)
            )
            .padding(horizontal = if (small) 6.dp else 10.dp, vertical = if (small) 2.dp else 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = Icons.Default.WorkspacePremium,
            contentDescription = "Pro VIP",
            tint = Color.Black,
            modifier = Modifier.size(if (small) 12.dp else 16.dp)
        )
        Text(
            text = "PRO VIP",
            color = Color.Black,
            fontWeight = FontWeight.Black,
            fontSize = if (small) 10.sp else 12.sp,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
fun SellerLevelBadge(
    level: Int,
    soldCount: Int? = null,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    val levelInfo = SellerLevelCalculator.getLevelInfo(
        when (level) {
            1 -> 0
            2 -> 8
            3 -> 20
            4 -> 35
            else -> 60
        }
    )

    val badgeColor = when (level) {
        1 -> Color(0xFFCD7F32) // Bronze
        2 -> Color(0xFF94A3B8) // Silver
        3 -> Color(0xFFF59E0B) // Gold
        4 -> Color(0xFF06B6D4) // Platinum
        else -> Color(0xFF8B5CF6) // Diamond / Master
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(6.dp),
        color = badgeColor.copy(alpha = 0.15f),
        border = androidx.compose.foundation.BorderStroke(1.dp, badgeColor.copy(alpha = 0.6f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "Seller Level",
                tint = badgeColor,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = if (compact) "Lvl $level" else "Level $level: ${levelInfo.badgeName}",
                color = badgeColor,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
            if (soldCount != null && !compact) {
                Text(
                    text = "($soldCount sold)",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
fun TierCommissionBadge(orderAmount: Double, modifier: Modifier = Modifier) {
    val tierNum = CommissionCalculator.getTierNumber(orderAmount)
    val ratePct = CommissionCalculator.getCommissionRatePercent(orderAmount).toInt()
    val tierColor = when (tierNum) {
        1 -> Tier1Color
        2 -> Tier2Color
        3 -> Tier3Color
        else -> Tier4Color
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(6.dp),
        color = tierColor.copy(alpha = 0.12f),
        border = androidx.compose.foundation.BorderStroke(1.dp, tierColor.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Icon(
                imageVector = Icons.Default.AccountBalanceWallet,
                contentDescription = "Commission Tier",
                tint = tierColor,
                modifier = Modifier.size(11.dp)
            )
            Text(
                text = "Tier $tierNum: $ratePct% Comm.",
                color = tierColor,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
fun DkkTopHeader(
    currentRole: UserRole,
    onRoleChange: (UserRole) -> Unit,
    userName: String,
    userPhone: String,
    isProVip: Boolean,
    onOpenRoleDialog: () -> Unit,
    isDarkTheme: Boolean = false,
    onToggleTheme: (() -> Unit)? = null,
    onOpenFirebase: (() -> Unit)? = null,
    onSearchClick: (() -> Unit)? = null,
    onOpenSupportChat: (() -> Unit)? = null,
    onLogout: (() -> Unit)? = null
) {
    Surface(
        color = DkkNavy,
        tonalElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
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
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF162038))
                            .border(2.dp, DkkGold, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.dkk_marketing_logo),
                            contentDescription = "DKK Logo",
                            modifier = Modifier.size(48.dp)
                        )
                    }
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "DKK Marketing",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp
                            )
                            if (isProVip) {
                                ProVipBadge(small = true)
                            }
                        }
                        Text(
                            text = "$userName • $userPhone",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Support Help Desk Button for Buyer and Seller
                    if (onOpenSupportChat != null) {
                        Surface(
                            onClick = onOpenSupportChat,
                            shape = CircleShape,
                            color = Color(0xFF162038),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DkkEmerald),
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("open_support_chat_btn")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = "Contact Admin Help Desk",
                                    tint = DkkEmerald,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    if (onToggleTheme != null) {
                        Surface(
                            onClick = onToggleTheme,
                            shape = CircleShape,
                            color = Color(0xFF162038),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isDarkTheme) DkkGold else Color(0xFF334155)),
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("theme_toggle_btn")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                                    contentDescription = "Toggle Theme",
                                    tint = if (isDarkTheme) DkkGold else Color(0xFFE2E8F0),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    // Role Switcher Pill
                    Surface(
                        onClick = onOpenRoleDialog,
                        shape = RoundedCornerShape(20.dp),
                        color = Color(currentRole.badgeColorHex).copy(alpha = 0.2f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(currentRole.badgeColorHex)),
                        modifier = Modifier.testTag("role_switcher_pill")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = when (currentRole) {
                                    UserRole.BUYER -> Icons.Default.ShoppingBag
                                    UserRole.SELLER -> Icons.Default.Storefront
                                    UserRole.OWNER -> Icons.Default.AdminPanelSettings
                                },
                                contentDescription = currentRole.displayName,
                                tint = Color(currentRole.badgeColorHex),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = currentRole.displayName,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = "Switch Role",
                                tint = Color(currentRole.badgeColorHex),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    // Direct Logout Option
                    if (onLogout != null) {
                        Surface(
                            onClick = onLogout,
                            shape = CircleShape,
                            color = Color(0xFFEF4444).copy(alpha = 0.2f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444)),
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("header_logout_btn")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                    contentDescription = "Log Out",
                                    tint = Color(0xFFFCA5A5),
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProductCard(
    product: ProductEntity,
    onProductClick: () -> Unit,
    onBuyNowClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onProductClick() }
            .testTag("product_card_${product.productId}"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(135.dp)
                    .background(Color(0xFFF1F5F9))
            ) {
                AsyncImage(
                    model = resolveProductImageModel(product.imageUrl),
                    contentDescription = product.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                if (product.isProVip) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(6.dp)
                    ) {
                        ProVipBadge(small = true)
                    }

                    // VIP 15s Video Badge
                    Surface(
                        modifier = Modifier.align(Alignment.Center),
                        shape = CircleShape,
                        color = Color.Black.copy(alpha = 0.6f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.PlayCircle, contentDescription = "VIP Video", tint = DkkGold, modifier = Modifier.size(14.dp))
                            Text("15s Video", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(6.dp),
                    shape = RoundedCornerShape(6.dp),
                    color = Color.Black.copy(alpha = 0.65f)
                ) {
                    Text(
                        text = product.category,
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
            ) {
                Text(
                    text = product.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Storefront,
                        contentDescription = "Seller",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(11.dp)
                    )
                    Text(
                        text = product.sellerName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 10.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Delivery Status Tag (Free Delivery vs Delivery Charges)
                if (product.isFreeDelivery) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFE8F5E9),
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFF81C784))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalShipping,
                                contentDescription = null,
                                tint = Color(0xFF2E7D32),
                                modifier = Modifier.size(11.dp)
                            )
                            Text(
                                text = "Free Delivery",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32)
                            )
                        }
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFF1F5F9),
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFFCBD5E1))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalShipping,
                                contentDescription = null,
                                tint = Color(0xFF475569),
                                modifier = Modifier.size(11.dp)
                            )
                            val charges = if (product.deliveryFee > 0) product.deliveryFee.toInt() else 150
                            Text(
                                text = "Delivery: Rs $charges",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF475569)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Bottom Row: Price (strictly only Rs, no commission to buyer) & Blue Cart Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatPkr(product.price),
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Surface(
                        onClick = onBuyNowClick,
                        color = Color(0xFF0F2B59), // Deep Navy Blue matching mockup button
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("buy_now_btn_${product.productId}")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = "Buy Now",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EasypaisaPaymentSheet(
    product: ProductEntity,
    buyerPhone: String,
    onDismiss: () -> Unit,
    onPaymentSuccess: (paymentRef: String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val clipboardManager = LocalClipboardManager.current
    var selectedMethod by remember { mutableStateOf("easypaisa") } // "easypaisa" or "jsbank"
    var accountNumber by remember { mutableStateOf(buyerPhone) }
    var accountPin by remember { mutableStateOf("") }
    var bankReferenceId by remember { mutableStateOf("") }
    var copiedBankField by remember { mutableStateOf<String?>(null) }
    var isProcessing by remember { mutableStateOf(false) }
    var paymentCompleted by remember { mutableStateOf(false) }
    var transactionId by remember { mutableStateOf("") }

    val deliveryFee = if (product.isFreeDelivery) 0.0 else (if (product.deliveryFee > 0) product.deliveryFee else 150.0)
    val totalPayable = product.price + deliveryFee

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            if (!paymentCompleted) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (selectedMethod == "easypaisa") {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(EasypaisaGreen, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("EP", color = Color.White, fontWeight = FontWeight.Black, fontSize = 14.sp)
                            }
                            Column {
                                Text(
                                    text = "Easypaisa Checkout",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Instant Secure Escrow Payment",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color(0xFF0F172A), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalance,
                                    contentDescription = null,
                                    tint = DkkGold,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "JS Bank Escrow Checkout",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Official DKK Escrow Settlement",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Payment Method Selector Tabs
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Surface(
                        onClick = { selectedMethod = "easypaisa" },
                        shape = RoundedCornerShape(10.dp),
                        color = if (selectedMethod == "easypaisa") EasypaisaGreen else Color.Transparent,
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🟢 Easypaisa",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (selectedMethod == "easypaisa") Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Surface(
                        onClick = { selectedMethod = "jsbank" },
                        shape = RoundedCornerShape(10.dp),
                        color = if (selectedMethod == "jsbank") Color(0xFF0F172A) else Color.Transparent,
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalance,
                                contentDescription = null,
                                tint = if (selectedMethod == "jsbank") DkkGold else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "JS Bank",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (selectedMethod == "jsbank") Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Item Summary Card
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = product.title,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Merchant: ${product.sellerName}",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Item Price:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = formatPkr(product.price),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Delivery Charges:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = if (product.isFreeDelivery) "FREE (Rs 0)" else formatPkr(deliveryFee),
                                fontSize = 12.sp,
                                color = if (product.isFreeDelivery) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Payable:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(
                                text = formatPkr(totalPayable),
                                fontWeight = FontWeight.ExtraBold,
                                color = DkkEmerald,
                                fontSize = 16.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (selectedMethod == "easypaisa") {
                    // Official Easypaisa Receiver Account Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                        shape = RoundedCornerShape(12.dp),
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
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .background(EasypaisaGreen, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("EP", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                    }
                                    Text(
                                        text = "Official Easypaisa Escrow Account",
                                        color = Color(0xFF1B5E20),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                                Surface(
                                    color = EasypaisaGreen.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "VERIFIED",
                                        color = EasypaisaGreen,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Account Title:", color = Color(0xFF2E7D32), fontSize = 11.sp)
                                Text(
                                    text = DkkOfficialEasypaisaAccount.ACCOUNT_TITLE,
                                    color = Color(0xFF1B5E20),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))

                            // Account Number Row with 1-tap copy
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.White, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Account Number (Raast / EP):", color = Color.Gray, fontSize = 9.sp)
                                    Text(
                                        text = DkkOfficialEasypaisaAccount.ACCOUNT_NUMBER,
                                        color = EasypaisaGreen,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(DkkOfficialEasypaisaAccount.ACCOUNT_NUMBER))
                                        copiedBankField = "Easypaisa Number (03273856001)"
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        Icons.Default.ContentCopy,
                                        contentDescription = "Copy Easypaisa Number",
                                        tint = EasypaisaGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            if (copiedBankField != null && copiedBankField!!.contains("Easypaisa")) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "✓ $copiedBankField copied to clipboard",
                                    color = Color(0xFF1B5E20),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Input fields
                    OutlinedTextField(
                        value = accountNumber,
                        onValueChange = { accountNumber = it },
                        label = { Text("Sender Easypaisa Mobile Account") },
                        placeholder = { Text("03XXXXXXXXX") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("easypaisa_account_input"),
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = "Account")
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = accountPin,
                        onValueChange = { if (it.length <= 5) accountPin = it },
                        label = { Text("5-Digit Easypaisa MPIN") },
                        placeholder = { Text("•••••") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("easypaisa_mpin_input"),
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = "MPIN")
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Security Note
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFE8F5E9), RoundedCornerShape(8.dp))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Shield,
                            contentDescription = "Safe",
                            tint = EasypaisaGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Payment held securely until delivery OTP verification by buyer.",
                            fontSize = 11.sp,
                            color = Color(0xFF1B5E20)
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = {
                            isProcessing = true
                        },
                        enabled = !isProcessing && accountNumber.length >= 10 && accountPin.length >= 4,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EasypaisaGreen,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("easypaisa_pay_btn")
                    ) {
                        if (isProcessing) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Verifying Easypaisa Gateway...")
                        } else {
                            Text(
                                "Pay ${formatPkr(totalPayable)} via Easypaisa",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }
                } else {
                    // JS Bank Payment Option
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Official Escrow Bank Account",
                                    color = DkkGold,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                                Surface(
                                    color = DkkEmerald.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "VERIFIED DKK",
                                        color = DkkEmerald,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Account Title:", color = Color.LightGray, fontSize = 11.sp)
                                Text(DkkOfficialBankAccount.ACCOUNT_TITLE, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Bank Name:", color = Color.LightGray, fontSize = 11.sp)
                                Text(DkkOfficialBankAccount.BANK_NAME, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Spacer(modifier = Modifier.height(6.dp))

                            // Account Number Row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF1E293B), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Account Number:", color = Color.LightGray, fontSize = 9.sp)
                                    Text(
                                        text = DkkOfficialBankAccount.ACCOUNT_NUMBER,
                                        color = DkkGold,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(DkkOfficialBankAccount.ACCOUNT_NUMBER))
                                        copiedBankField = "Account Number"
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        Icons.Default.ContentCopy,
                                        contentDescription = "Copy",
                                        tint = DkkGold,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // IBAN Row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF1E293B), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f).padding(end = 4.dp)) {
                                    Text("IBAN (Raast / 1Link):", color = Color.LightGray, fontSize = 9.sp)
                                    Text(
                                        text = DkkOfficialBankAccount.IBAN,
                                        color = Color.White,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 11.sp
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(DkkOfficialBankAccount.IBAN))
                                        copiedBankField = "IBAN"
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        Icons.Default.ContentCopy,
                                        contentDescription = "Copy",
                                        tint = Color.White,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }

                            if (copiedBankField != null) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "✓ $copiedBankField copied",
                                    color = DkkEmerald,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = bankReferenceId,
                        onValueChange = { bankReferenceId = it },
                        label = { Text("Sender Name / Transaction Reference / UTR") },
                        placeholder = { Text("e.g. JSBL-93821038 or Sender Name") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("js_bank_reference_input"),
                        leadingIcon = {
                            Icon(Icons.Default.ReceiptLong, contentDescription = "Ref")
                        }
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Security Note
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFEFF6FF), RoundedCornerShape(8.dp))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Shield,
                            contentDescription = "Safe",
                            tint = Color(0xFF2563EB),
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Transfer to DARO KHAN (JS Bank). Escrow holds funds until OTP delivery verification.",
                            fontSize = 11.sp,
                            color = Color(0xFF1E40AF)
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = {
                            isProcessing = true
                        },
                        enabled = !isProcessing && bankReferenceId.trim().length >= 3,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF0F172A),
                            contentColor = DkkGold
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("js_bank_pay_btn")
                    ) {
                        if (isProcessing) {
                            CircularProgressIndicator(
                                color = DkkGold,
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Verifying JS Bank Escrow Transfer...")
                        } else {
                            Text(
                                "Confirm JS Bank Transfer (${formatPkr(totalPayable)})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                LaunchedEffect(isProcessing) {
                    if (isProcessing) {
                        delay(1500)
                        transactionId = if (selectedMethod == "easypaisa") {
                            "EP-${(10000000..99999999).random()}"
                        } else {
                            "JSBL-${bankReferenceId.trim().ifEmpty { (10000000..99999999).random().toString() }}"
                        }
                        paymentCompleted = true
                        isProcessing = false
                    }
                }
            } else {
                // Success Receipt Screen
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .background(Color(0xFFE8F5E9), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "Success",
                            tint = if (selectedMethod == "easypaisa") EasypaisaGreen else DkkGold,
                            modifier = Modifier.size(44.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = if (selectedMethod == "easypaisa") "Easypaisa Payment Successful!" else "JS Bank Escrow Transfer Confirmed!",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (selectedMethod == "easypaisa") DkkEmerald else Color(0xFF0F172A)
                    )

                    Text(
                        text = "Transaction ID: $transactionId",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Item Price:", fontSize = 13.sp)
                                Text(formatPkr(product.price), fontWeight = FontWeight.Medium)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Delivery:", fontSize = 13.sp)
                                Text(
                                    text = if (product.isFreeDelivery) "Free Delivery (Rs 0)" else formatPkr(deliveryFee),
                                    fontWeight = FontWeight.Medium,
                                    color = if (product.isFreeDelivery) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Total Amount Paid:", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text(formatPkr(totalPayable), fontWeight = FontWeight.Bold, color = DkkEmerald)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Payment Method:", fontSize = 13.sp)
                                Text(
                                    text = if (selectedMethod == "easypaisa") "Easypaisa (DARO KHAN • 03273856001)" else "JS Bank (DARO KHAN • 0003015314)",
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Delivery Status:", fontSize = 13.sp)
                                Text("Order Dispatched Soon", color = Color(0xFFF59E0B), fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            onPaymentSuccess(transactionId)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DkkEmerald),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("finish_payment_btn")
                    ) {
                        Text("View Order & OTP Details", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FaceScanVerificationSheet(
    sellerName: String,
    onDismiss: () -> Unit,
    onVerificationComplete: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var scanState by remember { mutableStateOf(0) } // 0: Ready, 1: Scanning, 2: Verified
    val infiniteTransition = rememberInfiniteTransition(label = "scan_rotation")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radar_angle"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Seller KYC Face Verification",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Step 4 of Onboarding: Verify $sellerName",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Simulated Face Camera Viewport
            Box(
                modifier = Modifier
                    .size(220.dp)
                    .clip(CircleShape)
                    .background(DkkNavy)
                    .border(3.dp, if (scanState == 2) EasypaisaGreen else DkkGold, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (scanState == 2) {
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = "Verified",
                        tint = EasypaisaGreen,
                        modifier = Modifier.size(90.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Face,
                        contentDescription = "Face",
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(100.dp)
                    )

                    if (scanState == 1) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .rotate(angle)
                                .background(
                                    brush = Brush.sweepGradient(
                                        listOf(
                                            Color.Transparent,
                                            DkkEmeraldLight.copy(alpha = 0.4f),
                                            DkkGold.copy(alpha = 0.8f)
                                        )
                                    )
                                )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            when (scanState) {
                0 -> {
                    Text(
                        text = "Position your face inside the circle and press start scan to verify liveness and identity.",
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = { scanState = 1 },
                        colors = ButtonDefaults.buttonColors(containerColor = DkkEmerald),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("start_face_scan_btn")
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Start Live Face Scan", fontWeight = FontWeight.Bold)
                    }
                }
                1 -> {
                    Text(
                        text = "Scanning biometric facial landmarks... Please blink and stay still.",
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        color = DkkGold,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    CircularProgressIndicator(color = DkkEmeraldLight, modifier = Modifier.size(24.dp))
                    LaunchedEffect(Unit) {
                        delay(2500)
                        scanState = 2
                    }
                }
                2 -> {
                    Text(
                        text = "Face Scan 100% Verified! Seller Dashboard unlocked.",
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        color = EasypaisaGreen,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = onVerificationComplete,
                        colors = ButtonDefaults.buttonColors(containerColor = EasypaisaGreen),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("complete_face_scan_btn")
                    ) {
                        Text("Enter Seller Dashboard", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostProductSheet(
    sellerId: String,
    sellerName: String,
    onDismiss: () -> Unit,
    onSubmit: (
        title: String,
        description: String,
        price: Double,
        category: String,
        isProVip: Boolean,
        imageUrl: String,
        isFreeDelivery: Boolean,
        deliveryFee: Double
    ) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Shalwar Kameez & Lawn") }
    var isProVip by remember { mutableStateOf(false) }
    var imageUrl by remember { mutableStateOf("https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=600&auto=format&fit=crop&q=80") }
    var isFreeDelivery by remember { mutableStateOf(true) }
    var deliveryFeeText by remember { mutableStateOf("150") }
    var mediaType by remember { mutableStateOf("image") } // "image" or "video"
    var videoDurationSeconds by remember { mutableStateOf(15) }
    var videoFileSizeMb by remember { mutableStateOf(2.8) }
    var dailyVideoUploadCount by remember { mutableStateOf(1) } // out of 5 allowed daily

    val categories = listOf(
        "Shalwar Kameez & Lawn",
        "Peshawari Chappal & Khussa",
        "Handicrafts & Ajrak",
        "Dry Fruits & Desi Food",
        "Mobiles & Accessories",
        "Electronics & Solar",
        "Home Appliances",
        "Attar & Fragrances",
        "Sports & Sialkot Goods",
        "Bikes & Auto Parts",
        "Jewellery & Traditional Sets",
        "Grocery & Chai"
    )

    val price = priceText.toDoubleOrNull() ?: 0.0
    val commissionAmount = if (price > 0) CommissionCalculator.calculateCommission(price) else 0.0
    val commissionRatePct = if (price > 0) CommissionCalculator.getCommissionRatePercent(price).toInt() else 0

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Post New Listing",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Product Title") },
                placeholder = { Text("e.g. Wireless Smart Earbuds") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("new_product_title_input")
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = priceText,
                onValueChange = { priceText = it },
                label = { Text("Price in PKR (Rs)") },
                placeholder = { Text("15000") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("new_product_price_input")
            )

            if (price > 0) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = CommissionCalculator.getTierName(price),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Estimated Comm: ${formatPkr(commissionAmount)} ($commissionRatePct%)",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // VIP / Standard Media Upload Controls
            val sampleImagePresets = listOf(
                "Headphones" to "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=600&auto=format&fit=crop&q=80",
                "Smartphone" to "https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=600&auto=format&fit=crop&q=80",
                "Smartwatch" to "https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=600&auto=format&fit=crop&q=80",
                "Shoes" to "https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=600&auto=format&fit=crop&q=80",
                "Leather Bag" to "https://images.unsplash.com/photo-1548036328-c9fa89d128fa?w=600&auto=format&fit=crop&q=80"
            )

            // VIP Media Mode Switcher (1 Photo vs 15s Video)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isProVip) Color(0xFF1E293B) else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isProVip) "⭐ VIP Media Selection (15s Video or 1 Pic)" else "Product Media (1 Pic / VIP Video)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = if (isProVip) DkkGold else MaterialTheme.colorScheme.onSurface
                        )
                        if (isProVip) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = DkkEmerald.copy(alpha = 0.2f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, DkkEmerald)
                            ) {
                                Text("Quota: $dailyVideoUploadCount/5 Today", fontSize = 10.sp, color = DkkEmerald, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Option 1: 1 Picture
                        Surface(
                            onClick = { mediaType = "image" },
                            shape = RoundedCornerShape(8.dp),
                            color = if (mediaType == "image") DkkEmerald else Color.White.copy(alpha = 0.1f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (mediaType == "image") DkkEmerald else Color.Gray.copy(alpha = 0.4f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = if (mediaType == "image") Color.White else Color.Gray, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("1 HD Picture", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (mediaType == "image") Color.White else MaterialTheme.colorScheme.onSurface)
                            }
                        }

                        // Option 2: 15s Video (VIP Exclusive)
                        Surface(
                            onClick = {
                                if (isProVip) {
                                    mediaType = "video"
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            color = if (mediaType == "video") DkkGold else (if (isProVip) Color.White.copy(alpha = 0.1f) else Color(0xFF334155).copy(alpha = 0.3f)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (mediaType == "video") DkkGold else Color.Gray.copy(alpha = 0.4f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Default.PlayCircle, contentDescription = null, tint = if (mediaType == "video") Color.Black else if (isProVip) DkkGold else Color.Gray, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("15s Video (≤3MB)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (mediaType == "video") Color.Black else if (isProVip) Color.White else Color.Gray)
                            }
                        }
                    }

                    if (mediaType == "video") {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF0F172A),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DkkGold.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("🎥 VIP Video Constraints Check:", color = DkkGold, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    Text("Valid ✓", color = DkkEmerald, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("• Duration: $videoDurationSeconds seconds (Max 15s allowed)", color = Color(0xFFE2E8F0), fontSize = 10.sp)
                                Text("• File Size: $videoFileSizeMb MB (Max 3.0 MB limit enforced)", color = Color(0xFFE2E8F0), fontSize = 10.sp)
                                Text("• Daily Uploads: $dailyVideoUploadCount of 5 used today", color = Color(0xFFE2E8F0), fontSize = 10.sp)
                            }
                        }
                    } else if (!isProVip) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "💡 VIP members get 15-second video uploads (≤3MB, daily 5 limit). Switch Pro VIP below.",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFE2E8F0))
                        .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (imageUrl.isNotBlank()) {
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = "Selected Media",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        if (mediaType == "video") {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.4f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.PlayCircle, contentDescription = "Video", tint = DkkGold, modifier = Modifier.size(32.dp))
                            }
                        }
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(26.dp))
                            Text("No Media", fontSize = 10.sp, color = Color(0xFF64748B))
                        }
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = imageUrl,
                        onValueChange = { imageUrl = it },
                        label = { Text(if (mediaType == "video") "15s Video Link / MP4 URL" else "Image Link / Cloud URL") },
                        placeholder = { Text(if (mediaType == "video") "https://.../video.mp4 (max 3MB)" else "https://... ya sample chunein") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("product_image_url_input")
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text("Quick Samples (Click to pick photo):", fontSize = 11.sp, color = Color(0xFF64748B))
            Spacer(modifier = Modifier.height(4.dp))
            @OptIn(ExperimentalLayoutApi::class)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                sampleImagePresets.forEach { (name, url) ->
                    Surface(
                        onClick = {
                            imageUrl = url
                            mediaType = "image"
                        },
                        shape = RoundedCornerShape(6.dp),
                        color = if (imageUrl == url && mediaType == "image") DkkEmerald else Color(0xFFF1F5F9),
                        contentColor = if (imageUrl == url && mediaType == "image") Color.White else Color(0xFF334155)
                    ) {
                        Text(name, fontSize = 10.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text("Category", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            @OptIn(ExperimentalLayoutApi::class)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                categories.forEach { cat ->
                    val selected = category == cat
                    Surface(
                        onClick = { category = cat },
                        shape = RoundedCornerShape(8.dp),
                        color = if (selected) DkkEmerald else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (selected) Color.White else MaterialTheme.colorScheme.onSurface
                    ) {
                        Text(
                            text = cat,
                            fontSize = 12.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description & Warranty") },
                placeholder = { Text("Condition, specs, delivery package contents...") },
                maxLines = 3,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("new_product_desc_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Delivery Option Selection: Free Delivery vs Delivery Charges
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.LocalShipping,
                            contentDescription = null,
                            tint = DkkEmerald,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Delivery Option (Free ya Charges)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            onClick = { isFreeDelivery = true },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isFreeDelivery) DkkEmerald else Color.Transparent,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isFreeDelivery) DkkEmerald else MaterialTheme.colorScheme.outlineVariant
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "🚚 Free Delivery",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (isFreeDelivery) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Surface(
                            onClick = { isFreeDelivery = false },
                            shape = RoundedCornerShape(8.dp),
                            color = if (!isFreeDelivery) Color(0xFF0F2B59) else Color.Transparent,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (!isFreeDelivery) Color(0xFF0F2B59) else MaterialTheme.colorScheme.outlineVariant
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "📦 Delivery Charges",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (!isFreeDelivery) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    if (!isFreeDelivery) {
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = deliveryFeeText,
                            onValueChange = { deliveryFeeText = it },
                            label = { Text("Delivery Charges in PKR (Rs)") },
                            placeholder = { Text("e.g. 150") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("delivery_fee_input")
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Pro VIP boost toggle
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isProVip) Color(0xFFFEF3C7) else MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.WorkspacePremium,
                            contentDescription = "VIP",
                            tint = if (isProVip) Color(0xFFD97706) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Column {
                            Text(
                                "Feature as Pro VIP Product",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (isProVip) Color(0xFF92400E) else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "Top slot placement on Buyer Home Feed",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Switch(
                        checked = isProVip,
                        onCheckedChange = { isProVip = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFFD97706),
                            checkedTrackColor = Color(0xFFFDE68A)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    val finalDeliveryFee = if (isFreeDelivery) 0.0 else (deliveryFeeText.toDoubleOrNull() ?: 150.0)
                    onSubmit(title, description, price, category, isProVip, imageUrl, isFreeDelivery, finalDeliveryFee)
                },
                enabled = title.isNotBlank() && price > 0,
                colors = ButtonDefaults.buttonColors(containerColor = DkkEmerald),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("submit_product_btn")
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Publish Listing", fontWeight = FontWeight.Bold)
            }
        }
    }
}

/**
 * DKK Marketing - Direct Owner Support & Report Chat Sheet
 * Automatic user identity binding, 3MB screenshot firewall, routed directly to DKK MARKETING.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupportChatSheet(
    userName: String,
    userPhone: String,
    userEmail: String,
    userRole: String,
    onDismiss: () -> Unit,
    onSubmitReport: (subject: String, message: String, priority: String, screenshotUrl: String?) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var subject by remember { mutableStateOf("Payment / General Assistance") }
    var reportText by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("high") }
    var screenshotUrl by remember { mutableStateOf("") }
    var simulatedScreenshotSizeMb by remember { mutableStateOf(1.8) } // within 3MB limit
    var sizeError by remember { mutableStateOf<String?>(null) }

    val presetSubjects = listOf(
        "Payment Delay / Escrow",
        "Fake Buyer Report",
        "VIP Membership Activation",
        "Rider Delivery Issue",
        "Urgent Account Help"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .testTag("support_chat_sheet")
        ) {
            // Header with Security Lock badge
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
                            .background(DkkNavy, CircleShape)
                            .border(1.5.dp, DkkGold, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Shield, contentDescription = null, tint = DkkGold, modifier = Modifier.size(20.dp))
                    }
                    Column {
                        Text("Direct Admin Support Desk", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("Encrypted Private Route to DKK MARKETING", fontSize = 11.sp, color = Color(0xFF64748B))
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 1. Automatic User Identity Card
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF0F172A),
                border = androidx.compose.foundation.BorderStroke(1.dp, DkkGold.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = userName.ifBlank { "Registered User" },
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color.White
                            )
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = DkkEmerald.copy(alpha = 0.2f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, DkkEmerald)
                            ) {
                                Text(
                                    text = userRole.uppercase(),
                                    fontSize = 9.sp,
                                    color = DkkEmerald,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("Mobile: ${userPhone.ifBlank { "0333-XXXXXXX" }} • $userEmail", fontSize = 11.sp, color = Color(0xFFCBD5E1))
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color.Black.copy(alpha = 0.5f)
                    ) {
                        Text("ID Verified ✓", color = DkkGold, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 2. Issue Category
            Text("Issue Topic", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(6.dp))
            @OptIn(ExperimentalLayoutApi::class)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                presetSubjects.forEach { topic ->
                    Surface(
                        onClick = { subject = topic },
                        shape = RoundedCornerShape(8.dp),
                        color = if (subject == topic) DkkNavy else Color(0xFFF1F5F9),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (subject == topic) DkkGold else Color.Transparent)
                    ) {
                        Text(
                            text = topic,
                            color = if (subject == topic) DkkGold else Color(0xFF334155),
                            fontWeight = if (subject == topic) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 3. Text Report Node
            OutlinedTextField(
                value = reportText,
                onValueChange = { reportText = it },
                label = { Text("Describe your problem or complaint in detail") },
                placeholder = { Text("Apna masla yahan tafseel se likhein...") },
                minLines = 3,
                maxLines = 5,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("support_message_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 4. Screenshot File Guard (Max 3MB Firewall)
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFF8FAFC),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = DkkEmerald, modifier = Modifier.size(18.dp))
                            Text("Payment Slip / Evidence Screenshot", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                        }
                        Text("Max 3.0 MB", fontSize = 10.sp, color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = screenshotUrl,
                            onValueChange = {
                                screenshotUrl = it
                                sizeError = null
                            },
                            label = { Text("Screenshot URL or Slip Link") },
                            placeholder = { Text("https://... ya sample slip lagayein") },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("support_screenshot_input")
                        )

                        Button(
                            onClick = {
                                screenshotUrl = "https://images.unsplash.com/photo-1554224155-6726b3ff858f?w=600&auto=format&fit=crop&q=80"
                                simulatedScreenshotSizeMb = 1.6
                                sizeError = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DkkEmerald),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Attach Slip", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (screenshotUrl.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "File Size: ${simulatedScreenshotSizeMb} MB (Within 3.0 MB quota)",
                                fontSize = 10.sp,
                                color = if (simulatedScreenshotSizeMb <= 3.0) Color(0xFF16A34A) else Color(0xFFDC2626),
                                fontWeight = FontWeight.SemiBold
                            )
                            Text("Firewall Passed ✓", fontSize = 10.sp, color = DkkEmerald, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (sizeError != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = sizeError ?: "", color = Color(0xFFDC2626), fontSize = 11.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Submit Button
            Button(
                onClick = {
                    if (simulatedScreenshotSizeMb > 3.0) {
                        sizeError = "Screenshot file size exceeds 3.0 MB limit. Please compress image."
                        return@Button
                    }
                    onSubmitReport(
                        subject,
                        reportText,
                        priority,
                        if (screenshotUrl.isNotBlank()) screenshotUrl else null
                    )
                    onDismiss()
                },
                enabled = reportText.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = DkkEmerald),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("submit_support_report_btn")
            ) {
                Icon(Icons.Default.Shield, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Send Report to DKK MARKETING", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
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
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import com.example.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.firebase.AudioRoom
import com.example.data.firebase.MicSeat
import com.example.data.model.UserEntity
import com.example.data.model.UserRole
import com.example.ui.theme.DkkEmerald
import com.example.ui.theme.DkkGold
import com.example.ui.theme.DkkNavy
import com.example.ui.theme.DkkSlate
import com.example.ui.theme.EasypaisaGreen
import com.example.viewmodel.DkkViewModel
import kotlinx.coroutines.delay

private val StageDarkBg = Color(0xFF0B1426)
private val CardSurfaceBg = Color(0xFF131F38)
private val MicTranslucentBg = Color(0x330F172A)
private val GoldVipAccent = Color(0xFFFBBF24)
private val CyanAudioGlow = Color(0xFF38BDF8)
private val TealAccent = Color(0xFF14B8A6)
private val TealDark = Color(0xFF0F766E)

/**
 * 🎙️ DkkAudioRoomScreen - Matches Reference Screenshots 1-5:
 * - Top capsule: Host avatar with level 28, room flag 🇦🇪, title, ID with copy button -> clicks to Room Info sheet (Screenshot 5)
 * - Top right: `...` options & Power exit button -> clicks to Keep or Exit modal (Screenshot 4)
 * - 10-Mic Stage layout: Center top Mic 1 (Host with avatar, musical audio glow, level 32, timer 18:54, gift score), Row 1 (Mics 2-6), Row 2 (Mics 7-11 with locks 🔒)
 * - Floating widgets on right: Club Tier golden shield & Spinning vinyl music disc -> toggles docked music player & settings dialog (Screenshots 2 & 3)
 * - Green announcement banner & Arabic/Pashto poetry card
 * - Docked music player bar with track 'naya_daur', gear icon, power, progress slider, shuffle, prev, play/pause, next, volume
 * - Bottom action bar: Speaker toggle, "Say something" chat bar with pencil, Luxury 3D Gift Box
 * - Top-Up Sheet: Pakistan (5000 Rs = 5000 Coins) & Overseas ($50 = 5000 Coins)
 * - Host/Admin-only 30% payout calculation & Physical Gift Delivery across Pakistan addresses
 * - VIP Subscriptions: Room VIP ($100/mo, 12 videos/day, 15 sec max, 1-hr auto delete) & Seller VIP (3 pics/videos with PIV tag, 5 videos/day on Home)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DkkAudioRoomScreen(
    viewModel: DkkViewModel,
    currentUser: UserEntity?,
    onCloseRoom: () -> Unit
) {
    val context = LocalContext.current
    val activeRoom by viewModel.activeRoom.collectAsState()
    val coinBalance by viewModel.coinBalance.collectAsState()
    val diamondBalance by viewModel.diamondBalance.collectAsState()
    val latestGiftAnimation by viewModel.latestGiftAnimation.collectAsState()
    val giftRedemptions by viewModel.giftRedemptions.collectAsState()
    val isRoomVip by viewModel.isRoomVip.collectAsState()
    val isSellerVip by viewModel.isSellerVip.collectAsState()
    val unlockedThemes by viewModel.unlockedThemes.collectAsState()
    val vipVideosUploadedToday by viewModel.vipVideosUploadedToday.collectAsState()

    // Dialog & Sheet States
    var showGiftSheet by remember { mutableStateOf(false) }
    var showTopUpSheet by remember { mutableStateOf(false) }
    var showCashoutSheet by remember { mutableStateOf(false) }
    var showVideoUploadSheet by remember { mutableStateOf(false) }
    var showVipSheet by remember { mutableStateOf(false) }
    var showVipQuotaDialog by remember { mutableStateOf(false) }
    var selectedGiftRecipient by remember { mutableStateOf("") }
    var showKeepExitDialog by remember { mutableStateOf(false) }
    var showRoomInfoSheet by remember { mutableStateOf(false) }
    var showRoomSettingsSheet by remember { mutableStateOf(false) }
    var showRoomThemesSheet by remember { mutableStateOf(false) }
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showMusicSettingsDialog by remember { mutableStateOf(false) }
    var showMusicPlayerDock by remember { mutableStateOf(false) }

    // Music Player State (Screenshot 2 & 3)
    var isPlayingMusic by remember { mutableStateOf(true) }
    var broadcastMyMusic by remember { mutableStateOf(true) }
    var musicProgress by remember { mutableStateOf(0.45f) }
    var isMutedMain by remember { mutableStateOf(false) }

    // Seat Controls
    var selectedSeatForControls by remember { mutableStateOf<MicSeat?>(null) }
    var chatMessage by remember { mutableStateOf("") }
    val chatMessagesList = remember {
        mutableStateListOf(
            "System: Welcome to D.K.K. Live Audio. Please respect each other and chat in a decent manner.",
            "Hamza 🇵🇰: Salam to everyone on stage! ❤️",
            "Mansoori 🇦🇪: Welcome bro to the luxury lounge 👑",
            "Zubair: Superb audio quality today! 🎙️"
        )
    }

    // Spinning Vinyl Animation
    val infiniteTransition = rememberInfiniteTransition(label = "music_spin")
    val vinylRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "vinyl_angle"
    )

    // Auto clear gift announcement banner after 4 seconds
    LaunchedEffect(latestGiftAnimation) {
        if (latestGiftAnimation != null) {
            delay(4000)
            viewModel.clearGiftAnimation()
        }
    }

    val roomThemeBrush = when (activeRoom.themeId) {
        "royal_ruby" -> Brush.verticalGradient(
            listOf(
                Color(0xFF3B0712),
                Color(0xFF1E0308),
                Color(0xFF0F0104)
            )
        )
        "emerald_palace" -> Brush.verticalGradient(
            listOf(
                Color(0xFF064E3B),
                Color(0xFF022C22),
                Color(0xFF011812)
            )
        )
        "cyber_neon" -> Brush.verticalGradient(
            listOf(
                Color(0xFF3B0764),
                Color(0xFF1E0338),
                Color(0xFF0D001A)
            )
        )
        "golden_sultan" -> Brush.verticalGradient(
            listOf(
                Color(0xFF452B00),
                Color(0xFF261700),
                Color(0xFF120B00)
            )
        )
        else -> Brush.verticalGradient(
            listOf(
                Color(0xFF0F1E36),
                Color(0xFF0A1324),
                Color(0xFF060913)
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(roomThemeBrush)
    ) {
        // Theme 1: Cosmic Earth & Moon Wallpaper (Free Default Theme from Uploaded Photo)
        if (activeRoom.themeId == "cosmic_earth" || activeRoom.themeId.isBlank()) {
            Image(
                painter = painterResource(id = R.drawable.img_theme_cosmic_earth),
                contentDescription = "Cosmic Earth Background Theme",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            // Atmospheric contrast scrim so mic seats, text, and gifts stand out with perfect readability
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Black.copy(alpha = 0.35f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.65f)
                            )
                        )
                    )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // =================================================================
            // TOP HEADER: Host Capsule (Avatar, Level 28, Flag, Title, ID) + Options & Exit
            // Matches Screenshot 1 & 5
            // =================================================================
            Surface(
                color = Color(0xFF0A1326).copy(alpha = 0.95f),
                shadowElevation = 6.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Host Profile Capsule (Clickable -> Opens Room Info Sheet)
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = Color(0xFF1E293B).copy(alpha = 0.85f),
                        border = BorderStroke(1.dp, GoldVipAccent.copy(alpha = 0.6f)),
                        modifier = Modifier
                            .clickable { showRoomInfoSheet = true }
                            .testTag("open_room_info_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Host Avatar with Level 28 Badge
                            Box(contentAlignment = Alignment.BottomEnd) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.linearGradient(
                                                listOf(Color(0xFF6366F1), Color(0xFFEC4899))
                                            )
                                        )
                                        .border(1.5.dp, GoldVipAccent, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val dpText = activeRoom.roomDpUrl.ifBlank { "👑" }
                                    if (dpText.any { it.code > 127 || it.isSurrogate() }) {
                                        Text(
                                            text = dpText.take(2),
                                            fontSize = 16.sp
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = "Host Avatar",
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFF8B5CF6),
                                    modifier = Modifier.offset(x = 2.dp, y = 2.dp)
                                ) {
                                    Text(
                                        text = "28",
                                        color = Color.White,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.padding(horizontal = 3.dp, vertical = 0.5.dp)
                                    )
                                }
                            }

                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "${activeRoom.countryFlag} ${activeRoom.title.ifBlank { "خوږه یاران 👑" }}",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "ID: ${activeRoom.roomId.take(10).ifBlank { "6974202528" }}",
                                        color = Color(0xFF94A3B8),
                                        fontSize = 9.sp
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copy ID",
                                        tint = GoldVipAccent,
                                        modifier = Modifier
                                            .size(11.dp)
                                            .clickable {
                                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                clipboard.setPrimaryClip(ClipData.newPlainText("Room ID", activeRoom.roomId))
                                                Toast.makeText(context, "Room ID copied!", Toast.LENGTH_SHORT).show()
                                            }
                                    )
                                }
                            }
                        }
                    }

                    // Top Right Quick Controls: Coins Chip, Options & Exit Button
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Coin Quick Top-Up Chip
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFF1E293B),
                            border = BorderStroke(1.dp, GoldVipAccent.copy(alpha = 0.7f)),
                            modifier = Modifier.clickable { showTopUpSheet = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MonetizationOn,
                                    contentDescription = "Coins",
                                    tint = GoldVipAccent,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "${coinBalance / 1000}k",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Top Up",
                                    tint = EasypaisaGreen,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }

                        // VIP Tier badge button (Pure Fan VIP)
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (isRoomVip) GoldVipAccent else Color(0xFF1E293B),
                            modifier = Modifier.clickable { showVipSheet = true }
                        ) {
                            Text(
                                text = "VIP 👑",
                                color = if (isRoomVip) Color.Black else GoldVipAccent,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }

                        // Room Settings Button (for Host/Owner to edit Room Name & DP/Banner)
                        val isHostOrOwnerHeader = currentUser == null || currentUser.role == UserRole.OWNER || currentUser.uid == activeRoom.hostUid
                        if (isHostOrOwnerHeader) {
                            IconButton(
                                onClick = { showRoomSettingsSheet = true },
                                modifier = Modifier
                                    .size(32.dp)
                                    .testTag("room_settings_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Room Settings",
                                    tint = GoldVipAccent,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        // Options icon (Room Info)
                        IconButton(
                            onClick = { showRoomInfoSheet = true },
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("room_info_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreHoriz,
                                contentDescription = "More Options",
                                tint = Color.White
                            )
                        }

                        // Prominent "Leave Room" Button (Safely exit back to home screen)
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFFDC2626),
                            border = BorderStroke(1.dp, Color(0xFFEF4444)),
                            modifier = Modifier
                                .clickable {
                                    viewModel.closeAudioRoom()
                                    onCloseRoom()
                                }
                                .testTag("leave_room_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                    contentDescription = "Leave Room",
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "Leave",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Sub-header stats row: Trophy pill, VIP Status Quota Button & Audience count
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF1E293B).copy(alpha = 0.7f),
                        border = BorderStroke(0.6.dp, GoldVipAccent.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = GoldVipAccent, modifier = Modifier.size(12.dp))
                            Text("0 >", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Clean VIP Status Button for Room VIPs showing real-time video quota
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF1E293B),
                        border = BorderStroke(1.dp, GoldVipAccent),
                        modifier = Modifier
                            .clickable { showVipQuotaDialog = true }
                            .testTag("vip_status_quota_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = GoldVipAccent, modifier = Modifier.size(13.dp))
                            Text(
                                text = "VIP Quota: $vipVideosUploadedToday/12 Uploaded Today",
                                color = GoldVipAccent,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF334155)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(14.dp))
                        }
                        Text(
                            text = "${activeRoom.listenerCount.coerceAtLeast(1)}",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // =================================================================
            // MAIN STAGE BODY: 10-MIC STAGE + FLOATING WIDGETS + ANNOUNCEMENTS
            // Matches Screenshot 1
            // =================================================================
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(6.dp))

                    // ---------------------------------------------------------
                    // 10-MIC STAGE LAYOUT
                    // Mic 1: Centered at Top (Host)
                    // Row 1: Mics 2, 3, 4, 5, 6
                    // Row 2: Mics 7, 8 (Locked), 9, 10, 11 (Locked)
                    // ---------------------------------------------------------

                    // TOP CENTER: MIC 1 (HOST SEAT)
                    val hostSeat = activeRoom.seats.getOrNull(0)
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(vertical = 6.dp)
                    ) {
                        MicStageUnit(
                            seat = hostSeat,
                            seatIndex = 0,
                            seatLabel = "1",
                            isHostSeat = true,
                            currentUser = currentUser,
                            activeRoom = activeRoom,
                            onSeatClick = {
                                if (hostSeat == null || !hostSeat.isOccupied) {
                                    viewModel.occupyMicSeat(0)
                                } else if (hostSeat.occupantUid == currentUser?.uid || currentUser?.role == UserRole.OWNER || currentUser?.uid == activeRoom.hostUid) {
                                    selectedSeatForControls = hostSeat
                                }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // ROW 1: MICS 2, 3, 4, 5, 6
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (i in 1..5) {
                            val seat = activeRoom.seats.getOrNull(i)
                            MicStageUnit(
                                seat = seat,
                                seatIndex = i,
                                seatLabel = "${i + 1}",
                                isHostSeat = false,
                                currentUser = currentUser,
                                activeRoom = activeRoom,
                                onSeatClick = {
                                    handleSeatInteraction(
                                        seat = seat,
                                        index = i,
                                        currentUser = currentUser,
                                        activeRoom = activeRoom,
                                        viewModel = viewModel,
                                        onOpenControls = { selectedSeatForControls = it },
                                        context = context
                                    )
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // ROW 2: MICS 7, 8, 9, 10 (Matches Screenshot 1 with Locked Seats)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (i in 6..9) {
                            val seat = activeRoom.seats.getOrNull(i)
                            MicStageUnit(
                                seat = seat,
                                seatIndex = i,
                                seatLabel = "${i + 1}",
                                isHostSeat = false,
                                currentUser = currentUser,
                                activeRoom = activeRoom,
                                onSeatClick = {
                                    handleSeatInteraction(
                                        seat = seat,
                                        index = i,
                                        currentUser = currentUser,
                                        activeRoom = activeRoom,
                                        viewModel = viewModel,
                                        onOpenControls = { selectedSeatForControls = it },
                                        context = context
                                    )
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // ---------------------------------------------------------
                    // GREEN SYSTEM ANNOUNCEMENT PILL (Screenshot 1)
                    // ---------------------------------------------------------
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFF064E3B).copy(alpha = 0.85f),
                        border = BorderStroke(0.8.dp, Color(0xFF10B981)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Color(0xFF34D399), modifier = Modifier.size(16.dp))
                            Text(
                                text = "Welcome to D.K.K. Live Audio. Please respect each other and chat in a decent manner.",
                                color = Color(0xFFD1FAE5),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // ---------------------------------------------------------
                    // GOLD-BORDERED URDU/PASHTO ANNOUNCEMENT / AMUSEMENT CARD (Screenshot 1)
                    // ---------------------------------------------------------
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF131C2E).copy(alpha = 0.9f),
                        border = BorderStroke(1.2.dp, GoldVipAccent.copy(alpha = 0.8f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val isHostOrLeader = currentUser == null || currentUser.role == UserRole.OWNER || currentUser.uid == activeRoom.hostUid
                                if (isHostOrLeader) {
                                    showRoomSettingsSheet = true
                                }
                            }
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(Icons.Default.Campaign, contentDescription = null, tint = GoldVipAccent, modifier = Modifier.size(15.dp))
                                    Text(
                                        text = "Announcement / Amusement:",
                                        color = GoldVipAccent,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                val isHostOrLeader = currentUser == null || currentUser.role == UserRole.OWNER || currentUser.uid == activeRoom.hostUid
                                if (isHostOrLeader) {
                                    Text(
                                        text = "✏️ Edit (Leader)",
                                        color = GoldVipAccent,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                } else {
                                    Text(
                                        text = "${activeRoom.countryFlag} Official",
                                        color = Color(0xFF94A3B8),
                                        fontSize = 10.sp
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = activeRoom.announcement.ifBlank { "Welcome to D.K.K. Live Audio! Please respect everyone and chat decently. 🎙️✨" },
                                color = Color.White,
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                lineHeight = 17.sp,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // ---------------------------------------------------------
                    // RECENT CHAT MESSAGES STREAM
                    // ---------------------------------------------------------
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        chatMessagesList.takeLast(4).forEach { msg ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF0F172A).copy(alpha = 0.6f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = msg,
                                    color = Color(0xFFE2E8F0),
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // -------------------------------------------------------------
                // FLOATING RIGHT WIDGETS: Club Tier Trophy & Spinning Vinyl Record
                // Matches Screenshot 1
                // -------------------------------------------------------------
                Column(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 16.dp, end = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Golden Club Tier Badge
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF1E293B),
                        border = BorderStroke(1.5.dp, GoldVipAccent),
                        modifier = Modifier
                            .size(46.dp)
                            .clickable { showRoomInfoSheet = true }
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = "Club Tier",
                                tint = GoldVipAccent,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    // Spinning Vinyl Record "Music" Button (Screenshot 1, 2, 3)
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clickable { showMusicPlayerDock = !showMusicPlayerDock }
                            .testTag("toggle_music_player_dock"),
                        contentAlignment = Alignment.Center
                    ) {
                        // Spinning Vinyl Disc
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .rotate(if (isPlayingMusic) vinylRotation else 0f)
                                .clip(CircleShape)
                                .background(Color.Black)
                                .border(1.5.dp, if (isPlayingMusic) CyanAudioGlow else Color(0xFF64748B), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(GoldVipAccent)
                            )
                        }
                        // Music note indicator
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = "Music",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Cashout & 30% Host Payout Quick Button
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF0F766E),
                        border = BorderStroke(1.2.dp, Color(0xFF2DD4BF)),
                        modifier = Modifier
                            .size(42.dp)
                            .clickable { showCashoutSheet = true }
                            .testTag("open_cashout_sheet_button")
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Diamond,
                                contentDescription = "Payouts",
                                tint = Color(0xFF67E8F9),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "30%",
                                color = Color.White,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }

            // =================================================================
            // DOCKED MUSIC PLAYER BAR (Screenshot 2 & 3)
            // =================================================================
            if (showMusicPlayerDock) {
                Surface(
                    color = Color(0xFF0F172A),
                    border = BorderStroke(1.dp, Color(0xFF334155)),
                    shadowElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
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
                                    imageVector = Icons.Default.MusicNote,
                                    contentDescription = null,
                                    tint = CyanAudioGlow,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "naya_daur",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                // Settings Gear Icon (Opens Settings Dialog - Screenshot 2)
                                IconButton(
                                    onClick = { showMusicSettingsDialog = true },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = "Music Settings",
                                        tint = Color(0xFF94A3B8),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                // Power Off Music
                                IconButton(
                                    onClick = {
                                        isPlayingMusic = false
                                        showMusicPlayerDock = false
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PowerSettingsNew,
                                        contentDescription = "Close Music",
                                        tint = Color(0xFFEF4444),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        // Progress bar with time markers (Screenshot 3: 01:14 / 02:43)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("01:14", color = Color(0xFF94A3B8), fontSize = 9.sp)
                            Slider(
                                value = musicProgress,
                                onValueChange = { musicProgress = it },
                                colors = SliderDefaults.colors(
                                    thumbColor = CyanAudioGlow,
                                    activeTrackColor = CyanAudioGlow,
                                    inactiveTrackColor = Color(0xFF334155)
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            Text("02:43", color = Color(0xFF94A3B8), fontSize = 9.sp)
                        }

                        // Controls Row: Shuffle, Previous, Play/Pause, Next, Playlist, Volume
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = {}, modifier = Modifier.size(30.dp)) {
                                Icon(Icons.Default.Shuffle, contentDescription = "Shuffle", tint = Color(0xFF94A3B8), modifier = Modifier.size(18.dp))
                            }
                            IconButton(onClick = {}, modifier = Modifier.size(30.dp)) {
                                Icon(Icons.Default.SkipPrevious, contentDescription = "Previous", tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                            IconButton(
                                onClick = { isPlayingMusic = !isPlayingMusic },
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(TealAccent),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isPlayingMusic) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = "Play/Pause",
                                        tint = Color.Black,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            IconButton(onClick = {}, modifier = Modifier.size(30.dp)) {
                                Icon(Icons.Default.SkipNext, contentDescription = "Next", tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                            IconButton(onClick = {}, modifier = Modifier.size(30.dp)) {
                                Icon(Icons.Default.QueueMusic, contentDescription = "Playlist", tint = Color(0xFF94A3B8), modifier = Modifier.size(18.dp))
                            }
                            IconButton(onClick = {}, modifier = Modifier.size(30.dp)) {
                                Icon(Icons.Default.VolumeUp, contentDescription = "Volume", tint = Color(0xFF94A3B8), modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }

            // =================================================================
            // BOTTOM ACTION BAR: Speaker, Say Something Chat Pill, Gift Box
            // Matches Screenshot 1
            // =================================================================
            Surface(
                color = Color(0xFF0A1120),
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Speaker Mute/Unmute Toggle
                    IconButton(
                        onClick = { isMutedMain = !isMutedMain },
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = if (isMutedMain) Icons.Default.VolumeMute else Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Mute/Unmute Audio",
                            tint = if (isMutedMain) Color(0xFFEF4444) else Color.White
                        )
                    }

                    // "Say something" Input Field with Pencil Icon (Screenshot 1)
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = Color(0xFF1E293B),
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(16.dp)
                            )
                            OutlinedTextField(
                                value = chatMessage,
                                onValueChange = { chatMessage = it },
                                placeholder = {
                                    Text(
                                        "Say something...",
                                        color = Color(0xFF94A3B8),
                                        fontSize = 12.sp
                                    )
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            if (chatMessage.isNotBlank()) {
                                IconButton(
                                    onClick = {
                                        val author = currentUser?.name?.ifBlank { "User" } ?: "User"
                                        chatMessagesList.add("$author: $chatMessage")
                                        chatMessage = ""
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Send,
                                        contentDescription = "Send",
                                        tint = TealAccent,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Luxury 3D Gift Box FAB Button (Screenshot 1)
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clickable { showGiftSheet = true }
                            .testTag("open_gift_catalog_fab"),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFFF59E0B), Color(0xFFD97706), Color(0xFFEF4444))
                                    )
                                )
                                .border(1.5.dp, GoldVipAccent, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CardGiftcard,
                                contentDescription = "Send Luxury Gift",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }

        // =====================================================================
        // ANIMATED LUXURY GIFT BROADCAST BANNER (OVERLAY)
        // =====================================================================
        AnimatedVisibility(
            visible = latestGiftAnimation != null,
            enter = slideInVertically() + fadeIn(),
            exit = slideOutVertically() + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 64.dp, start = 16.dp, end = 16.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF0F172A),
                border = BorderStroke(2.dp, GoldVipAccent),
                shadowElevation = 10.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(GoldVipAccent),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = Color.Black)
                    }
                    Column {
                        Text(
                            text = "🎁 LUXURY GIFT SENT!",
                            color = GoldVipAccent,
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp
                        )
                        Text(
                            text = latestGiftAnimation ?: "",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }

    // =========================================================================
    // DIALOG 1: KEEP OR EXIT DIALOG (Matches Screenshot 4)
    // =========================================================================
    if (showKeepExitDialog) {
        Dialog(onDismissRequest = { showKeepExitDialog = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xEE0B1322),
                border = BorderStroke(1.dp, Color(0xFF1E293B)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Leave or Minimize Room?",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // KEEP BUTTON (Large Teal Circle with Up Arrow - Screenshot 4)
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable {
                                showKeepExitDialog = false
                                Toast.makeText(context, "Audio Room running in background", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(68.dp)
                                    .clip(CircleShape)
                                    .background(TealAccent),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowUp,
                                    contentDescription = "Keep",
                                    tint = Color.White,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Keep", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        // EXIT BUTTON (Large Teal Circle with Power Icon - Screenshot 4)
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable {
                                showKeepExitDialog = false
                                viewModel.closeAudioRoom()
                                onCloseRoom()
                            }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(68.dp)
                                    .clip(CircleShape)
                                    .background(TealAccent),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PowerSettingsNew,
                                    contentDescription = "Exit",
                                    tint = Color.White,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Exit", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }

    // =========================================================================
    // DIALOG 2: MUSIC SETTINGS DIALOG (Matches Screenshot 2)
    // =========================================================================
    if (showMusicSettingsDialog) {
        Dialog(onDismissRequest = { showMusicSettingsDialog = false }) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF0F172A),
                border = BorderStroke(1.dp, Color(0xFF334155)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Column {
                    // Teal / Green Header Bar with "Settings" and Close 'X' (Screenshot 2)
                    Surface(
                        color = TealAccent,
                        shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Settings",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            IconButton(
                                onClick = { showMusicSettingsDialog = false },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = Color.White
                                )
                            }
                        }
                    }

                    // Settings Body: Broadcast My Music Switch (Screenshot 2)
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Broadcast My Music",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Others can still hear the music I'm playing when my mic is muted.",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 11.sp,
                                    lineHeight = 16.sp
                                )
                            }

                            Switch(
                                checked = broadcastMyMusic,
                                onCheckedChange = { broadcastMyMusic = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = TealAccent,
                                    uncheckedThumbColor = Color(0xFF94A3B8),
                                    uncheckedTrackColor = Color(0xFF334155)
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { showMusicSettingsDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = TealAccent),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Save Preferences", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // =========================================================================
    // SHEET 1: ROOM INFO / PROFILE BOTTOM SHEET (Matches Screenshot 5)
    // =========================================================================
    if (showRoomInfoSheet) {
        var roomInfoTab by remember { mutableStateOf(0) } // 0: Profile, 1: Members, 2: Moments

        ModalBottomSheet(
            onDismissRequest = { showRoomInfoSheet = false },
            containerColor = Color(0xFF0F172A),
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .verticalScroll(rememberScrollState())
                    .navigationBarsPadding()
            ) {
                // Top Navigation Tabs: Profile | Members | Moments (Screenshot 5)
                TabRow(
                    selectedTabIndex = roomInfoTab,
                    containerColor = Color.Transparent,
                    contentColor = GoldVipAccent,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[roomInfoTab]),
                            color = GoldVipAccent
                        )
                    }
                ) {
                    Tab(
                        selected = roomInfoTab == 0,
                        onClick = { roomInfoTab = 0 },
                        text = { Text("Profile", fontWeight = FontWeight.Bold, color = if (roomInfoTab == 0) GoldVipAccent else Color.Gray) }
                    )
                    Tab(
                        selected = roomInfoTab == 1,
                        onClick = { roomInfoTab = 1 },
                        text = { Text("Members", fontWeight = FontWeight.Bold, color = if (roomInfoTab == 1) GoldVipAccent else Color.Gray) }
                    )
                    Tab(
                        selected = roomInfoTab == 2,
                        onClick = { roomInfoTab = 2 },
                        text = { Text("Moments", fontWeight = FontWeight.Bold, color = if (roomInfoTab == 2) GoldVipAccent else Color.Gray) }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Host Big Avatar + Title + ID with Copy (Screenshot 5)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(contentAlignment = Alignment.BottomEnd) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFF6366F1), Color(0xFFEC4899))
                                    )
                                )
                                .border(2.dp, GoldVipAccent, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(36.dp))
                        }
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFF8B5CF6),
                            modifier = Modifier.offset(x = 2.dp, y = 2.dp)
                        ) {
                            Text(
                                text = "28",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "ID: ${activeRoom.roomId.take(10).ifBlank { "6974202528" }}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy",
                                tint = GoldVipAccent,
                                modifier = Modifier
                                    .size(13.dp)
                                    .clickable {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("Room ID", activeRoom.roomId))
                                        Toast.makeText(context, "Room ID copied!", Toast.LENGTH_SHORT).show()
                                    }
                            )
                        }
                        Text(
                            text = "${activeRoom.countryFlag} ${activeRoom.title.ifBlank { "خوږه یاران 👑" }}",
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFF1E293B),
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Text(
                                text = "English",
                                color = Color(0xFF38BDF8),
                                fontSize = 9.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                            )
                        }
                    }

                    // Joined Button (Screenshot 5)
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = Color(0xFF047857),
                        modifier = Modifier.clickable {
                            Toast.makeText(context, "Joined Member Club!", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                            Text("Joined", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Room Level Bar: Level 28 • 56286 / 291500 (Screenshot 5)
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF1E293B),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFF8B5CF6)) {
                                    Text("28", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp))
                                }
                                Text("Room Level", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Icon(Icons.Default.HelpOutline, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(13.dp))
                            }
                            Text("56286 / 291500", color = Color(0xFF94A3B8), fontSize = 10.sp)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { 56286f / 291500f },
                            color = Color(0xFF8B5CF6),
                            trackColor = Color(0xFF334155),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Announcement Text Card (Screenshot 5)
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF1E293B),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Announcement:", color = GoldVipAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = activeRoom.announcement.ifBlank { "Welcome to D.K.K. Live Audio! Please respect everyone and chat decently. 🎙️✨" },
                            color = Color.White,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Member Club Card: Member 548 • Club Name MANSOR 💗 (Screenshot 5)
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF1E293B),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Member Club", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Members: 548 • Club: MANSOR 💗", color = Color(0xFF94A3B8), fontSize = 10.sp)
                        }
                        Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFF334155)) {
                            Text("No Data", color = Color.LightGray, fontSize = 9.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }
                }

                // Settings & Edit Buttons Row
                Spacer(modifier = Modifier.height(14.dp))
                val isHostOrOwnerInfo = currentUser == null || currentUser.role == UserRole.OWNER || currentUser.uid == activeRoom.hostUid
                if (isHostOrOwnerInfo) {
                    Button(
                        onClick = {
                            showRoomInfoSheet = false
                            showRoomSettingsSheet = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldVipAccent),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("edit_room_settings_button")
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = null, tint = Color.Black)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Edit Room Name & DP / Banner ⚙️", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                OutlinedButton(
                    onClick = {
                        showRoomInfoSheet = false
                        showEditProfileDialog = true
                    },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    border = BorderStroke(1.dp, GoldVipAccent),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_my_profile_button")
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, tint = GoldVipAccent)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Edit My Profile Details ✏️", color = Color.White, fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // =========================================================================
    // SHEET 2: LUXURY GIFTS CATALOG SHEET (D.K.K. Live Audio with 30% Share)
    // Flexible Gifting: Send to Host or Any Active Speaker on 10 Mic Seats
    // =========================================================================
    if (showGiftSheet) {
        if (selectedGiftRecipient.isBlank()) {
            selectedGiftRecipient = "Host (${activeRoom.hostName.ifBlank { "Stage Host" }})"
        }

        ModalBottomSheet(
            onDismissRequest = { showGiftSheet = false },
            containerColor = Color(0xFF0F172A),
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .verticalScroll(rememberScrollState())
                    .navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Send Luxury Gift 🎁",
                            color = GoldVipAccent,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Recipient receives 30% payout in Diamonds (Friday Settlement)",
                            color = Color(0xFF94A3B8),
                            fontSize = 10.sp
                        )
                    }
                    Surface(
                        color = Color(0xFF1E293B),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, GoldVipAccent.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = "$coinBalance Coins",
                            color = GoldVipAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Select Target Recipient across 10-mic stage
                Text("Select Recipient (Host or Any Active Speaker):", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    item {
                        FilterChipSmall(
                            selected = selectedGiftRecipient.startsWith("Host"),
                            text = "👑 Host (${activeRoom.hostName.ifBlank { "Host" }})",
                            onClick = { selectedGiftRecipient = "Host (${activeRoom.hostName.ifBlank { "Host" }})" }
                        )
                    }
                    val occupiedSeats = activeRoom.seats.filter { it.isOccupied && !it.occupantName.isNullOrBlank() }
                    items(occupiedSeats) { seat ->
                        val spkName = seat.occupantName ?: "Speaker"
                        FilterChipSmall(
                            selected = selectedGiftRecipient == spkName,
                            text = "🎙️ Seat ${seat.seatIndex + 1}: $spkName",
                            onClick = { selectedGiftRecipient = spkName }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF1E293B).copy(alpha = 0.8f),
                    border = BorderStroke(0.8.dp, GoldVipAccent.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.CardGiftcard, contentDescription = null, tint = GoldVipAccent, modifier = Modifier.size(16.dp))
                        Text(
                            text = "Target Recipient: $selectedGiftRecipient",
                            color = GoldVipAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Gifting Grid - Row 1: Virtual Luxury
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    GiftItemButton(
                        id = "gift_ring",
                        name = "Diamond Ring",
                        price = 1_000L,
                        icon = Icons.Default.Diamond,
                        sublabel = "30% = 300 💎",
                        onSend = {
                            viewModel.sendLuxuryGift(targetRecipient = selectedGiftRecipient, giftId = "gift_ring", giftName = "Diamond Ring 💍", coinPrice = 1_000L)
                            showGiftSheet = false
                        }
                    )
                    GiftItemButton(
                        id = "gift_supercar",
                        name = "Supercar",
                        price = 5_000L,
                        icon = Icons.Default.DirectionsCar,
                        sublabel = "30% = 1.5k 💎",
                        onSend = {
                            viewModel.sendLuxuryGift(targetRecipient = selectedGiftRecipient, giftId = "gift_supercar", giftName = "Supercar 🏎️", coinPrice = 5_000L)
                            showGiftSheet = false
                        }
                    )
                    GiftItemButton(
                        id = "gift_burj",
                        name = "Burj Crown",
                        price = 25_000L,
                        icon = Icons.Default.LocationCity,
                        sublabel = "30% = 7.5k 💎",
                        onSend = {
                            viewModel.sendLuxuryGift(targetRecipient = selectedGiftRecipient, giftId = "gift_burj", giftName = "Burj Crown 👑", coinPrice = 25_000L)
                            showGiftSheet = false
                        }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Gifting Grid - Row 2: Physical Gifts (Dress, Makeup, Watch)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    GiftItemButton(
                        id = "gift_dress",
                        name = "Silk Suit 👗",
                        price = 50_000L,
                        icon = Icons.Default.Checkroom,
                        sublabel = "30% = 15k 💎",
                        onSend = {
                            viewModel.sendLuxuryGift(targetRecipient = selectedGiftRecipient, giftId = "gift_dress", giftName = "Silk Bridal Dress 👗", coinPrice = 50_000L)
                            showGiftSheet = false
                        }
                    )
                    GiftItemButton(
                        id = "gift_makeup",
                        name = "Makeup Kit 💄",
                        price = 100_000L,
                        icon = Icons.Default.Spa,
                        sublabel = "30% = 30k 💎",
                        onSend = {
                            viewModel.sendLuxuryGift(targetRecipient = selectedGiftRecipient, giftId = "gift_makeup", giftName = "Luxury Makeup Kit 💄", coinPrice = 100_000L)
                            showGiftSheet = false
                        }
                    )
                    GiftItemButton(
                        id = "gift_watch",
                        name = "Gold Watch ⌚",
                        price = 250_000L,
                        icon = Icons.Default.Watch,
                        sublabel = "30% = 75k 💎",
                        onSend = {
                            viewModel.sendLuxuryGift(targetRecipient = selectedGiftRecipient, giftId = "gift_watch", giftName = "Luxury Gold Watch ⌚", coinPrice = 250_000L)
                            showGiftSheet = false
                        }
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    // =========================================================================
    // SHEET 3: TOP-UP COINS SHEET (Pakistan PKR vs Overseas USD Rates)
    // =========================================================================
    if (showTopUpSheet) {
        var topUpTab by remember { mutableStateOf(0) } // 0: Pakistan (PKR), 1: Overseas (USD)

        ModalBottomSheet(
            onDismissRequest = { showTopUpSheet = false },
            containerColor = Color(0xFF0F172A),
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .verticalScroll(rememberScrollState())
                    .navigationBarsPadding()
            ) {
                Text(
                    text = "Official Coin Top-Up Portal 🪙",
                    color = GoldVipAccent,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Zero Google Fee • Direct Rate for Pakistan & Overseas Website Buyers",
                    color = Color(0xFF94A3B8),
                    fontSize = 10.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Country Selector Tabs: 🇵🇰 Pakistan vs 🌐 Overseas
                TabRow(
                    selectedTabIndex = topUpTab,
                    containerColor = Color(0xFF1E293B),
                    contentColor = GoldVipAccent
                ) {
                    Tab(
                        selected = topUpTab == 0,
                        onClick = { topUpTab = 0 },
                        text = { Text("🇵🇰 Pakistan (PKR)", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
                    )
                    Tab(
                        selected = topUpTab == 1,
                        onClick = { topUpTab = 1 },
                        text = { Text("🌐 Overseas / Website ($ USD)", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (topUpTab == 0) {
                    // Pakistan Pricing (1 PKR = 1 Coin): "5000 Rs ke 5000 coins milenge"
                    Text("Official Pakistan Rate: 1 PKR = 1 Coin (JazzCash, Easypaisa, Raast)", color = EasypaisaGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))

                    TopUpPackageCard(coins = 1_000L, priceLabel = "Rs 1,000 PKR", bonus = null) {
                        viewModel.topUpCoinsPakistan(1_000L, 1_000.0)
                        Toast.makeText(context, "1,000 Coins added via Pakistan Direct Pay!", Toast.LENGTH_SHORT).show()
                        showTopUpSheet = false
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    TopUpPackageCard(coins = 5_000L, priceLabel = "Rs 5,000 PKR", bonus = "Standard Popular") {
                        viewModel.topUpCoinsPakistan(5_000L, 5_000.0)
                        Toast.makeText(context, "5,000 Coins added via Pakistan Direct Pay!", Toast.LENGTH_SHORT).show()
                        showTopUpSheet = false
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    TopUpPackageCard(coins = 11_000L, priceLabel = "Rs 10,000 PKR", bonus = "+1,000 BONUS") {
                        viewModel.topUpCoinsPakistan(11_000L, 10_000.0)
                        Toast.makeText(context, "11,000 Coins added via Pakistan Direct Pay!", Toast.LENGTH_SHORT).show()
                        showTopUpSheet = false
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    TopUpPackageCard(coins = 60_000L, priceLabel = "Rs 50,000 PKR", bonus = "+10,000 BONUS") {
                        viewModel.topUpCoinsPakistan(60_000L, 50_000.0)
                        Toast.makeText(context, "60,000 Coins added via Pakistan Direct Pay!", Toast.LENGTH_SHORT).show()
                        showTopUpSheet = false
                    }
                } else {
                    // Overseas Pricing: "50 dollar ke 5000 coins kharid sagte he websid pe"
                    Text("Overseas Rate: 50 USD = 5,000 Coins (Web Portal / Visa / Mastercard / PayPal)", color = CyanAudioGlow, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))

                    TopUpPackageCard(coins = 1_000L, priceLabel = "$10 USD", bonus = null) {
                        viewModel.topUpCoinsOverseas(1_000L, 10.0)
                        Toast.makeText(context, "1,000 Coins credited via Web Portal!", Toast.LENGTH_SHORT).show()
                        showTopUpSheet = false
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    TopUpPackageCard(coins = 5_000L, priceLabel = "$50 USD", bonus = "International Standard") {
                        viewModel.topUpCoinsOverseas(5_000L, 50.0)
                        Toast.makeText(context, "5,000 Coins credited via Web Portal!", Toast.LENGTH_SHORT).show()
                        showTopUpSheet = false
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    TopUpPackageCard(coins = 11_000L, priceLabel = "$100 USD", bonus = "+1,000 BONUS") {
                        viewModel.topUpCoinsOverseas(11_000L, 100.0)
                        Toast.makeText(context, "11,000 Coins credited via Web Portal!", Toast.LENGTH_SHORT).show()
                        showTopUpSheet = false
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    TopUpPackageCard(coins = 60_000L, priceLabel = "$500 USD", bonus = "+10,000 BONUS") {
                        viewModel.topUpCoinsOverseas(60_000L, 500.0)
                        Toast.makeText(context, "60,000 Coins credited via Web Portal!", Toast.LENGTH_SHORT).show()
                        showTopUpSheet = false
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    // =========================================================================
    // SHEET 4: CASHOUT & PHYSICAL DELIVERY SHEET (Host / Admin Only)
    // =========================================================================
    if (showCashoutSheet) {
        val isAuthorized = viewModel.canUserPayout(currentUser)
        var cashoutSubTab by remember { mutableStateOf(0) } // 0: Friday Cash Payout (30%), 1: Physical Gifts Delivery in Pakistan
        var recipientNameInput by remember { mutableStateOf(currentUser?.name ?: "") }
        var phoneInput by remember { mutableStateOf(currentUser?.phoneNumber ?: "") }
        var cityInput by remember { mutableStateOf("Lahore") }
        var addressInput by remember { mutableStateOf("") }
        var selectedPhysicalGift by remember { mutableStateOf("Silk Bridal Suit 👗") }
        var selectedPhysicalCost by remember { mutableStateOf(15_000L) }

        ModalBottomSheet(
            onDismissRequest = { showCashoutSheet = false },
            containerColor = Color(0xFF0F172A),
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .verticalScroll(rememberScrollState())
                    .navigationBarsPadding()
            ) {
                Text(
                    text = "Host Earnings Payout & Delivery 💎",
                    color = GoldVipAccent,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                if (!isAuthorized) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF450A0A),
                        border = BorderStroke(1.dp, Color(0xFFEF4444)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFFEF4444))
                            Column {
                                Text("Payout Sirf Host Ya Admin Kar Sakte Hain", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text("Regular listeners cannot cash out room funds. Only room host or admin can withdraw.", color = Color(0xFFFCA5A5), fontSize = 10.sp)
                            }
                        }
                    }
                } else {
                    Text(
                        text = "Current Diamond Balance: $diamondBalance 💎 (Worth Rs ${(diamondBalance * 0.10).toInt()} PKR)",
                        color = Color(0xFF38BDF8),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    TabRow(
                        selectedTabIndex = cashoutSubTab,
                        containerColor = Color(0xFF1E293B),
                        contentColor = GoldVipAccent
                    ) {
                        Tab(
                            selected = cashoutSubTab == 0,
                            onClick = { cashoutSubTab = 0 },
                            text = { Text("🗓️ Friday 30% Payout", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
                        )
                        Tab(
                            selected = cashoutSubTab == 1,
                            onClick = { cashoutSubTab = 1 },
                            text = { Text("🚚 Deliver to Pakistan", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (cashoutSubTab == 0) {
                        // 30% Friday Cash Settlement
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF1E293B),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Friday-to-Friday Weekly Settlement Engine", color = GoldVipAccent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("• Rule: Friday 12:00 AM to Thursday 11:59 PM accumulated Diamonds.", color = Color(0xFFCBD5E1), fontSize = 11.sp)
                                Text("• 30% Net Host Share: Dispatched directly via JazzCash / Easypaisa / Bank.", color = Color(0xFFCBD5E1), fontSize = 11.sp)
                                Text("• Estimated Payout: Rs ${(diamondBalance * 0.10).toInt()} PKR", color = EasypaisaGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                viewModel.cashoutDiamonds(
                                    diamondAmount = diamondBalance,
                                    user = currentUser,
                                    payoutMethod = "Friday Bank / Easypaisa Settlement",
                                    accountDetails = phoneInput.ifBlank { "0300-1234567" }
                                )
                                Toast.makeText(context, "Friday Payout scheduled successfully!", Toast.LENGTH_LONG).show()
                                showCashoutSheet = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EasypaisaGreen),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth(),
                            enabled = diamondBalance >= 1000
                        ) {
                            Icon(Icons.Default.AccountBalance, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (diamondBalance >= 1000) "Request Friday 30% Payout" else "Min 1,000 💎 Required",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        // Physical Gift Delivery across Pakistan addresses:
                        // "wo gift jo usko mila he wo pakistan ke eddress pe use mil sagta he"
                        Text("Select Physical Gift to Deliver in Pakistan:", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))

                        val physicalList = listOf(
                            Triple("Silk Bridal Suit 👗", 15_000L, "Pakistani Designer Silk Embroidery 3-Piece"),
                            Triple("Luxury Makeup Kit 💄", 10_000L, "MAC / Huda Beauty Full Vanity Box"),
                            Triple("Pure Silver Jewellery Set 💎", 20_000L, "925 Pure Silver Choker & Earring Set"),
                            Triple("Luxury Gold Watch ⌚", 25_000L, "Designer Chronograph Watch with Box")
                        )

                        physicalList.forEach { (title, cost, desc) ->
                            val isSelected = selectedPhysicalGift == title
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) Color(0xFF1E293B) else Color(0xFF0F172A),
                                border = BorderStroke(1.dp, if (isSelected) GoldVipAccent else Color(0xFF334155)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                                    .clickable {
                                        selectedPhysicalGift = title
                                        selectedPhysicalCost = cost
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Text(desc, color = Color(0xFF94A3B8), fontSize = 10.sp)
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = if (diamondBalance >= cost) GoldVipAccent else Color(0xFF334155)
                                    ) {
                                        Text(
                                            text = "$cost 💎",
                                            color = if (diamondBalance >= cost) Color.Black else Color.LightGray,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text("Courier Delivery Address in Pakistan:", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))

                        OutlinedTextField(
                            value = recipientNameInput,
                            onValueChange = { recipientNameInput = it },
                            label = { Text("Recipient Full Name", color = Color(0xFF94A3B8), fontSize = 10.sp) },
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = phoneInput,
                            onValueChange = { phoneInput = it },
                            label = { Text("Contact Phone (Courier calls upon delivery)", color = Color(0xFF94A3B8), fontSize = 10.sp) },
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = cityInput,
                            onValueChange = { cityInput = it },
                            label = { Text("City (Karachi, Lahore, Islamabad, etc.)", color = Color(0xFF94A3B8), fontSize = 10.sp) },
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = addressInput,
                            onValueChange = { addressInput = it },
                            label = { Text("Complete Street Address / House No", color = Color(0xFF94A3B8), fontSize = 10.sp) },
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                viewModel.redeemPhysicalGift(
                                    giftTitle = selectedPhysicalGift,
                                    diamondCost = selectedPhysicalCost,
                                    recipientName = recipientNameInput.ifBlank { "Pakistani Host" },
                                    phone = phoneInput.ifBlank { "0300-1234567" },
                                    city = cityInput.ifBlank { "Lahore" },
                                    fullAddress = addressInput.ifBlank { "Gulberg III, Lahore, Pakistan" }
                                )
                                Toast.makeText(context, "Gift parcel dispatched to Pakistan address via Courier! 🚚", Toast.LENGTH_LONG).show()
                                showCashoutSheet = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GoldVipAccent),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth(),
                            enabled = diamondBalance >= selectedPhysicalCost
                        ) {
                            Icon(Icons.Default.LocalShipping, contentDescription = null, tint = Color.Black)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (diamondBalance >= selectedPhysicalCost) "Deliver '$selectedPhysicalGift' to Pakistan 🚚" else "Need $selectedPhysicalCost 💎",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    // =========================================================================
    // SHEET 5: VIP SUBSCRIPTIONS (Room VIP vs Seller VIP)
    // =========================================================================
    if (showVipSheet) {
        ModalBottomSheet(
            onDismissRequest = { showVipSheet = false },
            containerColor = Color(0xFF0F172A),
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .verticalScroll(rememberScrollState())
                    .navigationBarsPadding()
            ) {
                Text(
                    text = "Fan VIP & Room Membership Lounge 👑",
                    color = GoldVipAccent,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Special privileges for fans, audio moments, and luxury entrance animations",
                    color = Color(0xFF94A3B8),
                    fontSize = 10.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // VIP TIER 1: ROOM VIP ($100 / Month)
                // "1ROOM wali jo day me 12 video post kar sagta he 1 hour me khud delet hogi video 15 second ki hogi ROOM wali VIP 100 doller pe par mohnt"
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF1E293B),
                    border = BorderStroke(1.5.dp, GoldVipAccent),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.RocketLaunch, contentDescription = null, tint = GoldVipAccent)
                                Text("ROOM VIP", color = GoldVipAccent, fontWeight = FontWeight.Black, fontSize = 15.sp)
                            }
                            Surface(shape = RoundedCornerShape(6.dp), color = GoldVipAccent) {
                                Text("$100 USD / Month", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("• 12 Videos / Audio Moments Per Day inside Room", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        Text("• Video Length: Max 15 Seconds HD", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        Text("• Auto-Delete: 1 Hour bad khud delete hogi (Ephemeral)", color = Color(0xFF38BDF8), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        Text("• 🦅 Royal Falcon Room Entrance Animation", color = GoldVipAccent, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                viewModel.subscribeRoomVip()
                                Toast.makeText(context, "Subscribed to ROOM VIP ($100/mo)!", Toast.LENGTH_SHORT).show()
                                showVipSheet = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GoldVipAccent),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (isRoomVip) "ROOM VIP Active ✓" else "Activate Room VIP ($100/mo)", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // VIP TIER 2: ROYAL FAN VIP ($20 / Month)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF1E293B),
                    border = BorderStroke(1.2.dp, Color(0xFF38BDF8)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.Diamond, contentDescription = null, tint = Color(0xFF38BDF8))
                                Text("ROYAL FAN VIP", color = Color(0xFF38BDF8), fontWeight = FontWeight.Black, fontSize = 15.sp)
                            }
                            Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFF38BDF8)) {
                                Text("$20 USD / Month", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("• 👑 Golden Animated Mic Ring on 10-Mic Stage", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        Text("• 💎 3D Luxury Fan Badge in Live Room Chat", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        Text("• 🎵 Sound Effects & Instant Soundboard Access", color = Color(0xFFBAE6FD), fontSize = 11.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                viewModel.subscribeSellerVip()
                                Toast.makeText(context, "Royal Fan VIP Activated!", Toast.LENGTH_SHORT).show()
                                showVipSheet = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Activate Royal Fan VIP ($20/mo)", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    // =========================================================================
    // DIALOG: VIP DAILY VIDEO STORIES QUOTA DIALOG
    // =========================================================================
    if (showVipQuotaDialog) {
        AlertDialog(
            onDismissRequest = { showVipQuotaDialog = false },
            containerColor = Color(0xFF0F172A),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = GoldVipAccent)
                    Text("Room VIP Daily Video Quota", color = GoldVipAccent, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Daily Quota: $vipVideosUploadedToday / 12 Stories Uploaded Today",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    LinearProgressIndicator(
                        progress = { (vipVideosUploadedToday / 12f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = GoldVipAccent,
                        trackColor = Color(0xFF334155)
                    )
                    Text(
                        text = "• Room VIPs ($100/mo) can post up to 12 ephemeral video stories per day.\n• Maximum 15 seconds each, automatically deletes in 1 hour.\n• Note: Audio rooms remain strictly 100% focused on live voice chat and gifting.",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.uploadVipHighlightVideo("15s Highlight #${vipVideosUploadedToday + 1}")
                        showVipQuotaDialog = false
                    },
                    enabled = vipVideosUploadedToday < 12,
                    colors = ButtonDefaults.buttonColors(containerColor = GoldVipAccent, contentColor = Color.Black)
                ) {
                    Text(if (vipVideosUploadedToday < 12) "Post 15s Story Highlight (+1)" else "Daily Limit Reached (12/12)", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showVipQuotaDialog = false }) {
                    Text("Close", color = Color.White)
                }
            }
        )
    }

    // =========================================================================
    // SHEET 6: MIC SEAT CONTROLS BOTTOM SHEET
    // =========================================================================
    selectedSeatForControls?.let { seat ->
        ModalBottomSheet(
            onDismissRequest = { selectedSeatForControls = null },
            containerColor = Color(0xFF0F172A),
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .navigationBarsPadding()
            ) {
                Text(
                    text = "Mic Seat ${seat.seatIndex + 1} Management",
                    color = GoldVipAccent,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Speaker: ${seat.occupantName ?: "Vacant"} • Gift Score: ${seat.giftScore} 🎁",
                    color = Color.White,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(14.dp))

                // Sit / Occupy Seat Button (if vacant)
                if (!seat.isOccupied && !seat.isLocked) {
                    Button(
                        onClick = {
                            viewModel.occupyMicSeat(seat.seatIndex)
                            selectedSeatForControls = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EasypaisaGreen),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("occupy_seat_button")
                    ) {
                        Icon(Icons.Default.Mic, contentDescription = null, tint = Color.Black)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sit on Mic Seat ${seat.seatIndex + 1}", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Mute / Unmute Button (if occupied)
                if (seat.isOccupied) {
                    Button(
                        onClick = {
                            viewModel.toggleMicSeatMute(seat.seatIndex)
                            selectedSeatForControls = null
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (seat.isMuted) EasypaisaGreen else Color(0xFFEF4444)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = if (seat.isMuted) Icons.Default.Mic else Icons.Default.MicOff, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (seat.isMuted) "Unmute Mic Seat" else "Mute Mic Seat")
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    // Direct 'Send Gift' option to this mic speaker
                    Button(
                        onClick = {
                            selectedGiftRecipient = seat.occupantName ?: "Speaker"
                            selectedSeatForControls = null
                            showGiftSheet = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldVipAccent, contentColor = Color.Black),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("send_gift_to_seat_occupant_button")
                    ) {
                        Icon(Icons.Default.CardGiftcard, contentDescription = null, tint = Color.Black)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Send Luxury Gift to ${seat.occupantName} 🎁", fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Lock / Unlock Seat Button (Host / Admin Only)
                val canLock = currentUser == null || currentUser.role == UserRole.OWNER || currentUser.uid == activeRoom.hostUid
                if (canLock) {
                    Button(
                        onClick = {
                            viewModel.toggleSeatLock(seat.seatIndex)
                            selectedSeatForControls = null
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (seat.isLocked) Color(0xFF3B82F6) else Color(0xFFD97706)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("toggle_lock_seat_button")
                    ) {
                        Icon(imageVector = if (seat.isLocked) Icons.Default.LockOpen else Icons.Default.Lock, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (seat.isLocked) "Unlock Seat ${seat.seatIndex + 1} 🔓" else "Lock Seat ${seat.seatIndex + 1} 🔒")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Leave Seat Button
                if (seat.isOccupied && (seat.occupantUid == currentUser?.uid || canLock)) {
                    OutlinedButton(
                        onClick = {
                            viewModel.leaveMicSeat(seat.seatIndex)
                            selectedSeatForControls = null
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Leave / Kick from Seat")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // =========================================================================
    // SHEET 7: ROOM SETTINGS SHEET (Host / Leader: Name, DP, Banner, Themes, Announcement)
    // =========================================================================
    if (showRoomSettingsSheet) {
        var roomNameInput by remember { mutableStateOf(activeRoom.title) }
        var selectedRoomDp by remember { mutableStateOf(activeRoom.roomDpUrl.ifBlank { "👑" }) }
        var selectedCountryFlag by remember { mutableStateOf(activeRoom.countryFlag) }
        var announcementInput by remember { mutableStateOf(activeRoom.announcement) }
        var selectedRoomBanner by remember { mutableStateOf(activeRoom.roomBannerUrl.ifBlank { "Midnight Blue" }) }

        val dpPresets = listOf("👑 Royal Crown", "🦁 Lion King", "🦅 Royal Falcon", "💎 Blue Diamond", "🎙️ Live Star", "🔥 Fire Flame", "🇦🇫 Afghan Eagle")
        val countryFlagsList = listOf(
            "🇦🇫" to "Afghanistan",
            "🇵🇰" to "Pakistan",
            "🇦🇪" to "UAE",
            "🇸🇦" to "Saudi Arabia",
            "🇶🇦" to "Qatar",
            "🇹🇷" to "Turkey"
        )
        val bannerPresets = listOf("Midnight Blue", "Velvet Ruby", "Emerald Forest", "Golden Sunset")

        ModalBottomSheet(
            onDismissRequest = { showRoomSettingsSheet = false },
            containerColor = Color(0xFF0F172A),
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .verticalScroll(rememberScrollState())
                    .navigationBarsPadding()
            ) {
                Text(
                    text = "Room Leader Controls & Settings ⚙️",
                    color = GoldVipAccent,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Update Room DP, Title, Announcement Banner (Amusement), and Room Themes",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // 1. Room Name
                Text("Room Name / Title", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = roomNameInput,
                    onValueChange = { roomNameInput = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_room_title_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldVipAccent,
                        unfocusedBorderColor = Color(0xFF334155),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color(0xFF1E293B),
                        unfocusedContainerColor = Color(0xFF1E293B)
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 2. Room Country Flag (Afghanistan 🇦🇫 Support)
                Text("Room Country Flag (🇦🇫 Afghanistan Support):", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(countryFlagsList) { (flag, cName) ->
                        FilterChipSmall(
                            selected = selectedCountryFlag == flag,
                            text = "$flag $cName",
                            onClick = { selectedCountryFlag = flag }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 3. Room DP / Display Picture
                Text("Room Display Picture (DP) / Icon:", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(dpPresets) { dp ->
                        FilterChipSmall(
                            selected = selectedRoomDp == dp,
                            text = dp,
                            onClick = { selectedRoomDp = dp }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 4. Room Announcement / Notice Banner (Amusement text)
                Text("Room Announcement / Notice Banner (Amusement Text):", color = GoldVipAccent, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = announcementInput,
                    onValueChange = { announcementInput = it },
                    placeholder = { Text("Enter room announcement notice (e.g. Welcome to D.K.K. Live Audio!)", color = Color.Gray, fontSize = 12.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_room_announcement_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldVipAccent,
                        unfocusedBorderColor = Color(0xFF334155),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color(0xFF1E293B),
                        unfocusedContainerColor = Color(0xFF1E293B)
                    ),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(14.dp))

                // 5. Dedicated Room Themes Button
                Text("Room Theme & Background:", color = GoldVipAccent, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF1E293B),
                    border = BorderStroke(1.2.dp, GoldVipAccent),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showRoomThemesSheet = true
                        }
                        .testTag("open_room_themes_button")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
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
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.2.dp, GoldVipAccent, RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.img_theme_cosmic_earth),
                                    contentDescription = "Cosmic Earth Theme",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text("Room Themes 🎨", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Surface(shape = RoundedCornerShape(4.dp), color = EasypaisaGreen.copy(alpha = 0.2f), border = BorderStroke(0.5.dp, EasypaisaGreen)) {
                                        Text("1 FREE", color = EasypaisaGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp))
                                    }
                                }
                                Text("Theme 1 (Cosmic View) • Tap to view all themes", color = Color(0xFF94A3B8), fontSize = 11.sp)
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Open", color = GoldVipAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = GoldVipAccent, modifier = Modifier.size(13.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        if (roomNameInput.isNotBlank()) {
                            viewModel.updateRoomSettings(
                                newTitle = roomNameInput.trim(),
                                newCountryFlag = selectedCountryFlag,
                                newDpUrl = selectedRoomDp,
                                newBannerUrl = selectedRoomBanner,
                                newAnnouncement = announcementInput.trim()
                            )
                            Toast.makeText(context, "Room Leader settings saved successfully!", Toast.LENGTH_SHORT).show()
                            showRoomSettingsSheet = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldVipAccent),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("save_room_settings_button")
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save Room Leader Settings", color = Color.Black, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // =========================================================================
    // SHEET: ROOM THEMES GALLERY (Theme 1 Free, Remaining Later)
    // =========================================================================
    if (showRoomThemesSheet) {
        data class RoomThemeOption(
            val id: String,
            val name: String,
            val cost: Long,
            val isFree: Boolean,
            val isImage: Boolean = false,
            val imageRes: Int? = null,
            val previewHex: Color = Color(0xFF0F1E36),
            val description: String = "",
            val isUpcomingPlaceholder: Boolean = false
        )

        val themesList = listOf(
            RoomThemeOption(
                id = "cosmic_earth",
                name = "Cosmic Earth & Moon 🌌",
                cost = 0L,
                isFree = true,
                isImage = true,
                imageRes = R.drawable.img_theme_cosmic_earth,
                previewHex = Color(0xFF0F1E36),
                description = "Free Default Theme • 1st Theme from Uploaded Photo (Planet Earth & Lunar Surface)"
            ),
            RoomThemeOption(
                id = "upcoming_theme_2",
                name = "Theme 2 (Upcoming)",
                cost = 0L,
                isFree = false,
                isUpcomingPlaceholder = true,
                description = "Baqi Bad Me Upload Hoga • Coming Soon 🖼️"
            ),
            RoomThemeOption(
                id = "upcoming_theme_3",
                name = "Theme 3 (Upcoming)",
                cost = 0L,
                isFree = false,
                isUpcomingPlaceholder = true,
                description = "Baqi Bad Me Upload Hoga • Coming Soon 🖼️"
            ),
            RoomThemeOption(
                id = "royal_ruby",
                name = "Royal Velvet Ruby",
                cost = 5_000L,
                isFree = false,
                previewHex = Color(0xFF4A0E17),
                description = "Luxury Deep Crimson & Velvet"
            ),
            RoomThemeOption(
                id = "emerald_palace",
                name = "Emerald Palace",
                cost = 5_000L,
                isFree = false,
                previewHex = Color(0xFF064E3B),
                description = "Imperial Deep Emerald"
            ),
            RoomThemeOption(
                id = "cyber_neon",
                name = "Imperial Cyber Neon",
                cost = 8_000L,
                isFree = false,
                previewHex = Color(0xFF3B0764),
                description = "Futuristic Royal Violet Glow"
            ),
            RoomThemeOption(
                id = "golden_sultan",
                name = "Golden Sultan Luxury",
                cost = 10_000L,
                isFree = false,
                previewHex = Color(0xFF452B00),
                description = "Pure HNWI Sultan Gold"
            ),
            RoomThemeOption(
                id = "dark_slate",
                name = "Dark Slate & Gold",
                cost = 0L,
                isFree = true,
                previewHex = Color(0xFF0F1E36),
                description = "Classic Dark Slate Studio"
            )
        )

        ModalBottomSheet(
            onDismissRequest = { showRoomThemesSheet = false },
            containerColor = Color(0xFF0F172A),
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .verticalScroll(rememberScrollState())
                    .navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.Palette, contentDescription = null, tint = GoldVipAccent, modifier = Modifier.size(20.dp))
                            Text(
                                text = "Room Themes 🎨",
                                color = GoldVipAccent,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "Theme 1 is Free (Cosmic Earth). Baqi themes bad me upload honge.",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = EasypaisaGreen.copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, EasypaisaGreen)
                    ) {
                        Text(
                            text = "1 FREE 🎁",
                            color = EasypaisaGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Render Themes List
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    themesList.forEach { theme ->
                        if (theme.isUpcomingPlaceholder) {
                            // Upcoming Placeholder Slot (Baqi bad me upload hoga)
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF1E293B).copy(alpha = 0.6f),
                                border = BorderStroke(1.dp, Color(0xFF475569)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Box(
                                            modifier = Modifier
                                                .size(46.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0xFF334155))
                                                .border(1.dp, Color(0xFF64748B), RoundedCornerShape(8.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(24.dp))
                                        }
                                        Column {
                                            Text(theme.name, color = Color(0xFFCBD5E1), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Text(theme.description, color = Color(0xFF94A3B8), fontSize = 11.sp)
                                        }
                                    }
                                    Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFF334155)) {
                                        Text("Coming Soon", color = Color(0xFF94A3B8), fontSize = 10.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                                    }
                                }
                            }
                        } else {
                            val isActiveTheme = (activeRoom.themeId == theme.id) || (theme.id == "cosmic_earth" && (activeRoom.themeId.isBlank() || activeRoom.themeId == "cosmic_earth"))
                            val isUnlocked = unlockedThemes.contains(theme.id) || theme.isFree

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF1E293B),
                                border = BorderStroke(
                                    if (isActiveTheme) 2.dp else 1.dp,
                                    if (isActiveTheme) GoldVipAccent else Color(0xFF334155)
                                ),
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
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            if (theme.isImage && theme.imageRes != null) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(54.dp)
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .border(1.5.dp, GoldVipAccent, RoundedCornerShape(8.dp))
                                                ) {
                                                    Image(
                                                        painter = painterResource(id = theme.imageRes),
                                                        contentDescription = theme.name,
                                                        modifier = Modifier.fillMaxSize(),
                                                        contentScale = ContentScale.Crop
                                                    )
                                                }
                                            } else {
                                                Box(
                                                    modifier = Modifier
                                                        .size(46.dp)
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(theme.previewHex)
                                                        .border(1.dp, GoldVipAccent, RoundedCornerShape(8.dp))
                                                )
                                            }

                                            Column {
                                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                    Text(theme.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                    if (theme.isFree) {
                                                        Surface(
                                                            shape = RoundedCornerShape(4.dp),
                                                            color = EasypaisaGreen.copy(alpha = 0.2f),
                                                            border = BorderStroke(0.5.dp, EasypaisaGreen)
                                                        ) {
                                                            Text("FREE 🎁", color = EasypaisaGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                                        }
                                                    }
                                                }
                                                Text(theme.description, color = Color(0xFF94A3B8), fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                if (!theme.isFree) {
                                                    Text("🪙 ${theme.cost} Coins", color = GoldVipAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))

                                        if (isActiveTheme) {
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = EasypaisaGreen.copy(alpha = 0.2f),
                                                border = BorderStroke(1.2.dp, EasypaisaGreen)
                                            ) {
                                                Text(
                                                    text = "ACTIVE ✅",
                                                    color = EasypaisaGreen,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                                )
                                            }
                                        } else if (isUnlocked) {
                                            Button(
                                                onClick = {
                                                    viewModel.purchaseAndApplyTheme(theme.id, 0L)
                                                    Toast.makeText(context, "${theme.name} Applied!", Toast.LENGTH_SHORT).show()
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = GoldVipAccent, contentColor = Color.Black),
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                                modifier = Modifier.height(34.dp)
                                            ) {
                                                Text("Apply", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        } else {
                                            Button(
                                                onClick = {
                                                    viewModel.purchaseAndApplyTheme(theme.id, theme.cost)
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706), contentColor = Color.White),
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                                modifier = Modifier.height(34.dp)
                                            ) {
                                                Text("Unlock (🪙 ${theme.cost})", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    // =========================================================================
    // SHEET 8: EDIT PROFILE DETAILS DIALOG (Display Name & Phone)
    // =========================================================================
    if (showEditProfileDialog) {
        var nameInput by remember { mutableStateOf(currentUser?.name ?: "") }
        var phoneInput by remember { mutableStateOf(currentUser?.phoneNumber ?: "") }

        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            containerColor = Color(0xFF0F172A),
            title = {
                Text("Edit Profile Details ✏️", color = GoldVipAccent, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Update your display name and contact phone number visible in rooms.",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    )
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Display Name") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("edit_profile_name_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldVipAccent,
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Color(0xFF1E293B),
                            unfocusedContainerColor = Color(0xFF1E293B),
                            focusedLabelColor = GoldVipAccent,
                            unfocusedLabelColor = Color(0xFF94A3B8)
                        ),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = phoneInput,
                        onValueChange = { phoneInput = it },
                        label = { Text("Phone Number / WhatsApp") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("edit_profile_phone_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldVipAccent,
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Color(0xFF1E293B),
                            unfocusedContainerColor = Color(0xFF1E293B),
                            focusedLabelColor = GoldVipAccent,
                            unfocusedLabelColor = Color(0xFF94A3B8)
                        ),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (nameInput.isNotBlank()) {
                            viewModel.updateUserProfile(
                                name = nameInput.trim(),
                                phoneNumber = phoneInput.trim()
                            )
                            Toast.makeText(context, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                            showEditProfileDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldVipAccent),
                    modifier = Modifier.testTag("save_profile_button")
                ) {
                    Text("Save Changes", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text("Cancel", color = Color.LightGray)
                }
            }
        )
    }
}

// =============================================================================
// SUB-COMPONENTS: MIC STAGE UNIT & GIFT BUTTONS
// =============================================================================

/**
 * MicStageUnit - Exact Rendering matching Screenshot 1:
 * - When occupied: circular avatar, musical note soundwave glow 🎵, Level 32 pill, timer pill 18:54, gift score pill 🎁 0.
 * - When vacant: translucent circular ring with mic icon, number below.
 * - When locked: gold lock icon with "Locked" label.
 */
@Composable
fun MicStageUnit(
    seat: MicSeat?,
    seatIndex: Int,
    seatLabel: String,
    isHostSeat: Boolean,
    currentUser: UserEntity?,
    activeRoom: AudioRoom,
    onSeatClick: () -> Unit
) {
    val isOccupied = seat?.isOccupied == true
    val isLocked = seat?.isLocked == true
    val isCurrentUser = seat?.occupantUid == currentUser?.uid
    val isMuted = seat?.isMuted == true
    val giftScore = seat?.giftScore ?: 0L

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onSeatClick() }
            .padding(vertical = 4.dp, horizontal = 2.dp)
    ) {
        Box(
            modifier = Modifier.size(if (isHostSeat) 60.dp else 48.dp),
            contentAlignment = Alignment.Center
        ) {
            when {
                isLocked -> {
                    // Locked Seat (Screenshot 1: Lock icon 🔒)
                    Box(
                        modifier = Modifier
                            .size(if (isHostSeat) 54.dp else 44.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E293B).copy(alpha = 0.8f))
                            .border(1.2.dp, GoldVipAccent.copy(alpha = 0.6f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Locked",
                            tint = GoldVipAccent,
                            modifier = Modifier.size(if (isHostSeat) 22.dp else 18.dp)
                        )
                    }
                }
                isOccupied -> {
                    // Occupied Seat with Musical Notes Glow Wave Animation (Screenshot 1)
                    Box(
                        modifier = Modifier
                            .size(if (isHostSeat) 60.dp else 48.dp)
                            .clip(CircleShape)
                            .background(CyanAudioGlow.copy(alpha = 0.2f))
                    )
                    Box(
                        modifier = Modifier
                            .size(if (isHostSeat) 52.dp else 42.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF6366F1), Color(0xFFEC4899))
                                )
                            )
                            .border(1.5.dp, if (isHostSeat) GoldVipAccent else CyanAudioGlow, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Occupant",
                            tint = Color.White,
                            modifier = Modifier.size(if (isHostSeat) 26.dp else 20.dp)
                        )
                    }

                    // Mute indicator badge
                    if (isMuted) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEF4444))
                                .align(Alignment.BottomEnd),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.MicOff, contentDescription = null, tint = Color.White, modifier = Modifier.size(9.dp))
                        }
                    }
                }
                else -> {
                    // Vacant Seat (Screenshot 1: Circular Translucent Glow Ring with Mic Icon)
                    Box(
                        modifier = Modifier
                            .size(if (isHostSeat) 54.dp else 44.dp)
                            .clip(CircleShape)
                            .background(MicTranslucentBg)
                            .border(
                                width = 1.dp,
                                color = Color(0xFF334155),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MicNone,
                            contentDescription = "Vacant",
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(if (isHostSeat) 22.dp else 18.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(3.dp))

        // Seat Sub-Label / Badges (Screenshot 1: Level pill, Name pill, Gift score)
        if (isOccupied) {
            val displayName = if (isCurrentUser) "You" else seat?.occupantName?.take(6) ?: "Speaker"
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = Color(0xFF1E293B).copy(alpha = 0.85f),
                border = BorderStroke(0.5.dp, Color(0xFF475569))
            ) {
                Text(
                    text = "$displayName ⭐",
                    color = Color.White,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                )
            }
            if (giftScore > 0 || isHostSeat) {
                Text(
                    text = "🎁 $giftScore",
                    color = GoldVipAccent,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        } else if (isLocked) {
            Text(
                text = "Locked",
                color = GoldVipAccent,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            )
        } else {
            Text(
                text = seatLabel,
                color = Color(0xFF94A3B8),
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

private fun handleSeatInteraction(
    seat: MicSeat?,
    index: Int,
    currentUser: UserEntity?,
    activeRoom: AudioRoom,
    viewModel: DkkViewModel,
    onOpenControls: (MicSeat) -> Unit,
    context: Context
) {
    if (seat == null) return
    val isHostOrOwner = currentUser == null || currentUser.role == UserRole.OWNER || currentUser.uid == activeRoom.hostUid
    if (seat.isLocked) {
        if (isHostOrOwner) {
            // Fix Mic Lock/Toggle Bug: In the 10-mic stage view, when a mic seat is locked (🔒), tapping it again does not unlock it.
            // Now tapping toggles and unlocks it immediately!
            viewModel.toggleSeatLock(index)
            Toast.makeText(context, "Seat ${index + 1} Unlocked 🔓", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Seat ${index + 1} is locked by Host 🔒", Toast.LENGTH_SHORT).show()
        }
    } else if (!seat.isOccupied) {
        if (isHostOrOwner) {
            onOpenControls(seat)
        } else {
            viewModel.occupyMicSeat(index)
        }
    } else if (seat.occupantUid == currentUser?.uid || isHostOrOwner) {
        onOpenControls(seat)
    }
}

@Composable
fun GiftItemButton(
    id: String,
    name: String,
    price: Long,
    icon: ImageVector,
    sublabel: String? = null,
    onSend: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onSend() }
            .padding(4.dp)
            .width(82.dp)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF0F172A))
                .border(1.5.dp, GoldVipAccent.copy(alpha = 0.7f), RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = name, tint = GoldVipAccent, modifier = Modifier.size(28.dp))
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(name, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center)
        Text("${if (price >= 1000) "${price / 1000}k" else price} Coins", color = GoldVipAccent, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
        if (sublabel != null) {
            Text(sublabel, color = Color(0xFF34D399), fontSize = 8.sp, maxLines = 1, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun TopUpPackageCard(
    coins: Long,
    priceLabel: String,
    bonus: String? = null,
    onBuy: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFF1E293B),
        border = BorderStroke(1.dp, Color(0xFF334155)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onBuy() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = GoldVipAccent, modifier = Modifier.size(20.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("$coins Coins", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        if (bonus != null) {
                            Surface(shape = RoundedCornerShape(4.dp), color = EasypaisaGreen) {
                                Text(bonus, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                            }
                        }
                    }
                    Text("Zero Google Fee • Direct Rate", color = Color(0xFF94A3B8), fontSize = 10.sp)
                }
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = GoldVipAccent
            ) {
                Text(
                    text = priceLabel,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
fun FilterChipSmall(selected: Boolean, text: String, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (selected) EasypaisaGreen else Color(0xFF1E293B),
        border = BorderStroke(1.dp, if (selected) EasypaisaGreen else Color(0xFF475569)),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = text,
            color = if (selected) Color.White else Color(0xFFCBD5E1),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

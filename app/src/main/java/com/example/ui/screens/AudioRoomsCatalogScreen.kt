package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.firebase.AudioRoom
import com.example.data.firebase.MicSeat
import com.example.ui.theme.DkkEmerald
import com.example.ui.theme.DkkGold
import com.example.ui.theme.DkkNavy
import com.example.ui.theme.DkkSlate
import com.example.ui.theme.EasypaisaGreen

private val DeepNavyHeader = Color(0xFF0F2B59)
private val LiveRed = Color(0xFFEF4444)
private val LoungeCardBg = Color(0xFF131C2E)
private val StageDarkBg = Color(0xFF0D1424)

data class CountryFilter(
    val code: String,
    val flag: String,
    val name: String,
    val label: String
)

val supportedCountryFilters = listOf(
    CountryFilter("ALL", "🌍", "All Countries", "All 🌍"),
    CountryFilter("AF", "🇦🇫", "Afghanistan", "Afghanistan 🇦🇫"),
    CountryFilter("PK", "🇵🇰", "Pakistan", "Pakistan 🇵🇰"),
    CountryFilter("AE", "🇦🇪", "UAE", "UAE 🇦🇪"),
    CountryFilter("SA", "🇸🇦", "Saudi Arabia", "Saudi Arabia 🇸🇦"),
    CountryFilter("QA", "🇶🇦", "Qatar", "Qatar 🇶🇦"),
    CountryFilter("KW", "🇰🇼", "Kuwait", "Kuwait 🇰🇼"),
    CountryFilter("OM", "🇴🇲", "Oman", "Oman 🇴🇲"),
    CountryFilter("BH", "🇧🇭", "Bahrain", "Bahrain 🇧🇭"),
    CountryFilter("GB", "🇬🇧", "United Kingdom", "UK 🇬🇧"),
    CountryFilter("US", "🇺🇸", "United States", "USA 🇺🇸"),
    CountryFilter("CA", "🇨🇦", "Canada", "Canada 🇨🇦"),
    CountryFilter("TR", "🇹🇷", "Turkey", "Turkey 🇹🇷")
)

/**
 * 🎙️ AudioRoomsCatalogScreen:
 * Social 10-Mic Voice & Gifting Lounges (D.K.K. Live Audio Style).
 * UAE and international country flag filtering, real-time voice, and luxury gifting.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioRoomsCatalogScreen(
    rooms: List<AudioRoom>,
    onSelectRoom: (AudioRoom) -> Unit,
    onCreateRoom: (title: String, videoStreamUrl: String?, countryFlag: String, countryName: String) -> Unit,
    isSeller: Boolean = false,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCountryCode by remember { mutableStateOf("ALL") }
    var showCreateSheet by remember { mutableStateOf(false) }

    val filteredRooms = rooms.filter { room ->
        val matchesSearch = searchQuery.isBlank() ||
                room.title.contains(searchQuery, ignoreCase = true) ||
                room.hostName.contains(searchQuery, ignoreCase = true) ||
                room.countryName.contains(searchQuery, ignoreCase = true)

        val matchesCountry = if (selectedCountryCode == "ALL") {
            true
        } else {
            val filter = supportedCountryFilters.find { it.code == selectedCountryCode }
            if (filter != null) {
                room.countryFlag == filter.flag ||
                room.countryName.equals(filter.name, ignoreCase = true) ||
                room.title.contains(filter.name, ignoreCase = true) ||
                room.title.contains(filter.flag)
            } else true
        }

        matchesSearch && matchesCountry
    }

    val totalSpeakersCount = rooms.sumOf { it.occupiedSeatsCount }
    val totalListenersCount = rooms.sumOf { it.listenerCount }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Luxury Top Banner
        Card(
            colors = CardDefaults.cardColors(containerColor = DeepNavyHeader),
            shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
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
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(DkkGold, Color(0xFFD97706)))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Microphone",
                                tint = Color.Black,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "D.K.K. Live Audio Lounges",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                LivePulseIndicator()
                            }
                            Text(
                                text = "5-Mic Voice Stages • Luxury Gifting & 30% Payout 🎁",
                                color = DkkGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Create Room Button
                    Button(
                        onClick = { showCreateSheet = true },
                        colors = ButtonDefaults.buttonColors(containerColor = DkkGold, contentColor = Color.Black),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("btn_create_audio_room")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Create Room",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "+ Host Room",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Live Stats Strip
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0A1F40), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.LiveTv, contentDescription = null, tint = LiveRed, modifier = Modifier.size(14.dp))
                        Text("${rooms.size} Live Rooms", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Text("•", color = Color.Gray, fontSize = 11.sp)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.Mic, contentDescription = null, tint = DkkGold, modifier = Modifier.size(14.dp))
                        Text("$totalSpeakersCount On Stage", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Text("•", color = Color.Gray, fontSize = 11.sp)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.Headphones, contentDescription = null, tint = EasypaisaGreen, modifier = Modifier.size(14.dp))
                        Text("$totalListenersCount Listening", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        // Search and Country Flag Strip
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Room, mulk ya host search karein...", fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.Gray) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("input_search_audio_rooms"),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = DkkEmerald,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                ),
                singleLine = true
            )

            // Country Flag Filter Row
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(supportedCountryFilters) { filter ->
                    val isSelected = selectedCountryCode == filter.code
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCountryCode = filter.code },
                        label = {
                            Text(
                                text = filter.label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = DeepNavyHeader,
                            selectedLabelColor = Color.White,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }
        }

        // Rooms List
        if (filteredRooms.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(
                        imageVector = Icons.Default.MicOff,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = "Koi active audio room nahi mila",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Aap '+ Host Room' par click kar ke naya room khol sakte hain!",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredRooms, key = { it.roomId }) { room ->
                    AudioRoomCard(
                        room = room,
                        onClick = { onSelectRoom(room) }
                    )
                }
            }
        }
    }

    // Modal Sheet: Create New Audio Room
    if (showCreateSheet) {
        CreateAudioRoomSheet(
            onDismiss = { showCreateSheet = false },
            onCreateRoom = { title, videoUrl, flag, name ->
                showCreateSheet = false
                onCreateRoom(title, videoUrl, flag, name)
            }
        )
    }
}

@Composable
fun AudioRoomsCatalogScreen(
    rooms: List<AudioRoom>,
    onSelectRoom: (AudioRoom) -> Unit,
    onCreateRoom: (title: String, videoStreamUrl: String?) -> Unit,
    isSeller: Boolean = false,
    modifier: Modifier = Modifier
) {
    AudioRoomsCatalogScreen(
        rooms = rooms,
        onSelectRoom = onSelectRoom,
        onCreateRoom = { title, videoUrl, flag, name -> onCreateRoom(title, videoUrl) },
        isSeller = isSeller,
        modifier = modifier
    )
}

/**
 * Individual Audio Room Card Component
 */
@Composable
fun AudioRoomCard(
    room: AudioRoom,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = LoungeCardBg),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("card_room_${room.roomId}")
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row: Live Tag, Title, and Video Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(LiveRed, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                            )
                            Text(
                                text = "LIVE",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    if (room.isVideoLoopActive) {
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF8B5CF6), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                Icon(Icons.Default.Videocam, contentDescription = null, tint = Color.White, modifier = Modifier.size(10.dp))
                                Text(
                                    text = "1-Hr Video Loop",
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Coins Gifted Badge
                if (room.totalCoinsGifted > 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        modifier = Modifier
                            .background(Color(0xFF261D10), RoundedCornerShape(6.dp))
                            .border(0.8.dp, DkkGold, RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Icon(Icons.Default.Diamond, contentDescription = null, tint = DkkGold, modifier = Modifier.size(11.dp))
                        Text(
                            text = "${room.totalCoinsGifted / 1000}k Coins",
                            color = DkkGold,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Room Title & Host Info
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Country Flag Badge
                    Surface(
                        color = Color(0xFF1E293B),
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(0.8.dp, DkkGold.copy(alpha = 0.6f))
                    ) {
                        Text(
                            text = "${room.countryFlag} ${room.countryName}",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Text(
                        text = room.title,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text("Host:", color = Color.LightGray, fontSize = 11.sp)
                    Text(
                        text = room.hostName,
                        color = DkkGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "VIP Host",
                        tint = DkkGold,
                        modifier = Modifier.size(12.dp)
                    )
                    Text("•", color = Color.Gray, fontSize = 10.sp)
                    Text(
                        text = "5-Mic Gifting Stage 🎁",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    )
                }
            }

            // 5-Mic Stage Visualizer Row
            StageSeatsPreview(seats = room.seats)

            // Bottom Action Strip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(StageDarkBg, RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.Headphones, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(13.dp))
                        Text(
                            text = "${room.listenerCount} Sun Rahe Hain",
                            color = Color.LightGray,
                            fontSize = 11.sp
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.Mic, contentDescription = null, tint = DkkEmerald, modifier = Modifier.size(13.dp))
                        Text(
                            text = "${room.occupiedSeatsCount}/5 Mics",
                            color = DkkEmerald,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Button(
                    onClick = onClick,
                    colors = ButtonDefaults.buttonColors(containerColor = DkkEmerald, contentColor = Color.White),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Text("Dakhil Hon 🎙️", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/**
 * 5-Mic Mini Preview Row showing stage seats
 */
@Composable
private fun StageSeatsPreview(seats: List<MicSeat>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(StageDarkBg, RoundedCornerShape(8.dp))
            .padding(vertical = 8.dp, horizontal = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until 5) {
            val seat = seats.getOrNull(i)
            val isOccupied = seat?.isOccupied == true
            val isHost = i == 0

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isHost && isOccupied -> Brush.linearGradient(listOf(DkkGold, Color(0xFFB45309)))
                                isOccupied -> Brush.linearGradient(listOf(DkkEmerald, Color(0xFF065F46)))
                                else -> Brush.linearGradient(listOf(Color(0xFF1E293B), Color(0xFF0F172A)))
                            }
                        )
                        .border(
                            width = if (isOccupied && seat?.isTalking == true) 2.dp else 1.dp,
                            color = when {
                                seat?.isTalking == true -> Color(0xFF10B981)
                                isHost -> DkkGold
                                isOccupied -> DkkEmerald
                                else -> Color(0xFF334155)
                            },
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isOccupied) {
                        Text(
                            text = (seat?.occupantName?.firstOrNull() ?: 'U').uppercase(),
                            color = if (isHost) Color.Black else Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Vacant",
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Text(
                    text = when {
                        isHost -> "Host 👑"
                        isOccupied -> seat?.occupantName?.take(6) ?: "Mic ${i + 1}"
                        else -> "Mic ${i + 1}"
                    },
                    color = if (isOccupied) Color.White else Color(0xFF64748B),
                    fontSize = 9.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * Pulsing Green LIVE dot indicator
 */
@Composable
private fun LivePulseIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .size(8.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(Color(0xFF10B981))
    )
}

/**
 * Bottom Sheet to Create a new 5-Mic Audio Room with Country Flag Selection
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateAudioRoomSheet(
    onDismiss: () -> Unit,
    onCreateRoom: (title: String, videoUrl: String?, countryFlag: String, countryName: String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var roomTitle by remember { mutableStateOf("") }
    var selectedRoomDp by remember { mutableStateOf("👑 Royal Crown") }
    var selectedCountry by remember { mutableStateOf(supportedCountryFilters.first { it.code == "AF" }) }

    val dpPresets = listOf("👑 Royal Crown", "🦁 Lion King", "🦅 Royal Falcon", "💎 Blue Diamond", "🎙️ Live Star", "🔥 Fire Flame", "🇦🇫 Afghan Eagle")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DeepNavyHeader
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "🎙️ Apna 10-Mic Audio Room Kholein",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Real-time voice, luxury gifting & 30% host payout",
                        color = DkkGold,
                        fontSize = 12.sp
                    )
                }
            }

            // Country Selector Row
            Text(
                text = "Room Ka Mulk (Country Flag) Chuniye:",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(supportedCountryFilters.filter { it.code != "ALL" }) { country ->
                    val isSelected = selectedCountry.code == country.code
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) DkkGold else Color(0xFF1E293B),
                        border = BorderStroke(1.dp, if (isSelected) DkkGold else Color(0xFF475569)),
                        modifier = Modifier.clickable { selectedCountry = country }
                    ) {
                        Text(
                            text = "${country.flag} ${country.name}",
                            color = if (isSelected) Color.Black else Color.White,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            OutlinedTextField(
                value = roomTitle,
                onValueChange = { roomTitle = it },
                label = { Text("Room Ka Naam / Title", color = Color.White) },
                placeholder = { Text("Maslan: ${selectedCountry.name} VIP Gifting Lounge ${selectedCountry.flag}", color = Color.Gray) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_new_room_title"),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = DkkGold,
                    unfocusedBorderColor = Color.LightGray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            // Room Profile Picture (DP) Selection
            Text(
                text = "Room Profile Picture (DP) Chuniye:",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(dpPresets) { dp ->
                    val isSelected = selectedRoomDp == dp
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) DkkGold else Color(0xFF1E293B),
                        border = BorderStroke(1.dp, if (isSelected) DkkGold else Color(0xFF475569)),
                        modifier = Modifier.clickable { selectedRoomDp = dp }
                    ) {
                        Text(
                            text = dp,
                            color = if (isSelected) Color.Black else Color.White,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Button(
                onClick = {
                    if (roomTitle.isNotBlank()) {
                        onCreateRoom(
                            roomTitle.trim(),
                            selectedRoomDp,
                            selectedCountry.flag,
                            selectedCountry.name
                        )
                    }
                },
                enabled = roomTitle.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = DkkGold, contentColor = Color.Black),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .testTag("btn_submit_create_room")
            ) {
                Icon(Icons.Default.Mic, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Stage Shuru Karein (${selectedCountry.flag} Seat 1 Host)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

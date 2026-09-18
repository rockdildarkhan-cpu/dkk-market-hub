package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AssignmentReturn
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.firebase.AudioRoom
import com.example.data.model.DeliveryStatus
import com.example.data.model.OrderEntity
import com.example.data.model.ProductEntity
import com.example.data.model.resolveProductImageModel
import com.example.data.security.ReceiptsVaultSecurityGuard
import com.example.ui.components.ProductCard
import com.example.ui.components.ProVipBadge
import com.example.ui.components.formatPkr
import com.example.ui.theme.DkkEmerald
import com.example.ui.theme.DkkGold
import com.example.ui.theme.DkkNavy
import com.example.ui.theme.EasypaisaGreen

private val DeepNavyHeader = Color(0xFF0F2B59)
private val DeliveryBannerBg = Color(0xFF0D254C)
private val OrangeDelivery = Color(0xFFF97316)

@Composable
fun BuyerDashboardScreen(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    allProducts: List<ProductEntity>,
    proVipProducts: List<ProductEntity>,
    buyerOrders: List<OrderEntity>,
    onProductSelected: (ProductEntity) -> Unit,
    onBuyNow: (ProductEntity) -> Unit,
    onOpenSupportChat: (() -> Unit)? = null,
    onOpenRoleDialog: (() -> Unit)? = null,
    onToggleTheme: (() -> Unit)? = null,
    isDarkTheme: Boolean = false,
    userName: String = "Buyer",
    userPhone: String = "",
    allAudioRooms: List<AudioRoom> = emptyList(),
    onSelectRoom: ((AudioRoom) -> Unit)? = null,
    onCreateRoom: ((title: String, videoStreamUrl: String?, countryFlag: String, countryName: String) -> Unit)? = null,
    onLogout: (() -> Unit)? = null,
    onRequestReturn: ((orderId: String, reason: String, proofScreenshotUrl: String, fileSizeBytes: Long) -> Unit)? = null
) {
    // 0: Home, 1: Categories, 2: Audio Rooms, 3: Orders, 4: Profile
    var currentNavTab by remember { mutableStateOf(0) }
    // Delivery Filter: "ALL", "FREE", "CHARGES"
    var deliveryFilter by remember { mutableStateOf("ALL") }

    val categories = listOf(
        "All",
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

    // Filter products based on category, search, and delivery option
    val displayedProducts = allProducts.filter { prod ->
        val matchesCategory = selectedCategory == "All" || prod.category.equals(selectedCategory, ignoreCase = true)
        val matchesSearch = searchQuery.isBlank() ||
                prod.title.contains(searchQuery, ignoreCase = true) ||
                prod.description.contains(searchQuery, ignoreCase = true) ||
                prod.category.contains(searchQuery, ignoreCase = true)
        val matchesDelivery = when (deliveryFilter) {
            "FREE" -> prod.isFreeDelivery
            "CHARGES" -> !prod.isFreeDelivery
            else -> true
        }
        matchesCategory && matchesSearch && matchesDelivery
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                // Tab 0: Home
                NavigationBarItem(
                    selected = currentNavTab == 0,
                    onClick = { currentNavTab = 0 },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home", fontSize = 11.sp, fontWeight = if (currentNavTab == 0) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = DeepNavyHeader,
                        indicatorColor = DeepNavyHeader,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.testTag("nav_item_home")
                )

                // Tab 1: Categories
                NavigationBarItem(
                    selected = currentNavTab == 1,
                    onClick = { currentNavTab = 1 },
                    icon = { Icon(Icons.Default.GridView, contentDescription = "Categories") },
                    label = { Text("Categories", fontSize = 11.sp, fontWeight = if (currentNavTab == 1) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = DeepNavyHeader,
                        indicatorColor = DeepNavyHeader,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.testTag("nav_item_categories")
                )

                // Tab 2: Audio Rooms (5-Mic Live Lounges)
                NavigationBarItem(
                    selected = currentNavTab == 2,
                    onClick = { currentNavTab = 2 },
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
                    label = { Text("Rooms 🎙️", fontSize = 11.sp, fontWeight = if (currentNavTab == 2) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = DeepNavyHeader,
                        indicatorColor = DeepNavyHeader,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.testTag("nav_item_audio_rooms")
                )

                // Tab 3: Orders
                NavigationBarItem(
                    selected = currentNavTab == 3,
                    onClick = { currentNavTab = 3 },
                    icon = {
                        if (buyerOrders.isNotEmpty()) {
                            BadgedBox(
                                badge = {
                                    Badge(
                                        containerColor = OrangeDelivery,
                                        contentColor = Color.White
                                    ) {
                                        Text("${buyerOrders.size}")
                                    }
                                }
                            ) {
                                Icon(Icons.Default.LocalShipping, contentDescription = "Orders")
                            }
                        } else {
                            Icon(Icons.Default.LocalShipping, contentDescription = "Orders")
                        }
                    },
                    label = { Text("Orders", fontSize = 11.sp, fontWeight = if (currentNavTab == 3) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = DeepNavyHeader,
                        indicatorColor = DeepNavyHeader,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.testTag("nav_item_orders")
                )

                // Tab 4: Profile
                NavigationBarItem(
                    selected = currentNavTab == 4,
                    onClick = { currentNavTab = 4 },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile", fontSize = 11.sp, fontWeight = if (currentNavTab == 4) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = DeepNavyHeader,
                        indicatorColor = DeepNavyHeader,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.testTag("nav_item_profile")
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
            when (currentNavTab) {
                0 -> {
                    // Home Screen Feed matching the user's reference mockup
                    BuyerHomeView(
                        searchQuery = searchQuery,
                        onSearchQueryChange = onSearchQueryChange,
                        selectedCategory = selectedCategory,
                        onCategorySelected = onCategorySelected,
                        categories = categories,
                        deliveryFilter = deliveryFilter,
                        onDeliveryFilterChange = { deliveryFilter = it },
                        allProducts = allProducts,
                        displayedProducts = displayedProducts,
                        proVipProducts = proVipProducts,
                        onProductSelected = onProductSelected,
                        onBuyNow = onBuyNow,
                        onOpenRoleDialog = onOpenRoleDialog,
                        onToggleTheme = onToggleTheme,
                        isDarkTheme = isDarkTheme,
                        onOpenSupportChat = onOpenSupportChat,
                        onLogout = onLogout
                    )
                }
                1 -> {
                    // Categories Browse View
                    BuyerCategoriesView(
                        categories = categories.filter { it != "All" },
                        allProducts = allProducts,
                        onCategoryClicked = { cat ->
                            onCategorySelected(cat)
                            currentNavTab = 0
                        }
                    )
                }
                2 -> {
                    // 🎙️ Live Audio Rooms Catalog View
                    AudioRoomsCatalogScreen(
                        rooms = allAudioRooms,
                        onSelectRoom = { room -> onSelectRoom?.invoke(room) },
                        onCreateRoom = { title, videoUrl, flag, name -> onCreateRoom?.invoke(title, videoUrl, flag, name) },
                        isSeller = false
                    )
                }
                3 -> {
                    // Orders & Tracking View
                    BuyerOrdersView(
                        buyerOrders = buyerOrders,
                        onOpenSupportChat = onOpenSupportChat,
                        onShopNow = { currentNavTab = 0 },
                        onRequestReturn = onRequestReturn
                    )
                }
                4 -> {
                    // Buyer Profile View
                    BuyerProfileView(
                        userName = userName,
                        userPhone = userPhone,
                        buyerOrdersCount = buyerOrders.size,
                        onOpenRoleDialog = onOpenRoleDialog,
                        onToggleTheme = onToggleTheme,
                        isDarkTheme = isDarkTheme,
                        onOpenSupportChat = onOpenSupportChat,
                        onLogout = onLogout
                    )
                }
            }
        }
    }
}

/**
 * Main Home screen layout matching the user's design image:
 * Deep navy top bar with DKK Marketing title, white pill search bar with search button,
 * Free delivery banner with orange delivery truck, delivery free / charges filter options,
 * category list, and 2-column product grid with zero commission displayed.
 */
@Composable
private fun BuyerHomeView(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    categories: List<String>,
    deliveryFilter: String,
    onDeliveryFilterChange: (String) -> Unit,
    allProducts: List<ProductEntity>,
    displayedProducts: List<ProductEntity>,
    proVipProducts: List<ProductEntity>,
    onProductSelected: (ProductEntity) -> Unit,
    onBuyNow: (ProductEntity) -> Unit,
    onOpenRoleDialog: (() -> Unit)?,
    onToggleTheme: (() -> Unit)?,
    isDarkTheme: Boolean,
    onOpenSupportChat: (() -> Unit)?,
    onLogout: (() -> Unit)? = null
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // 1. Deep Navy Header matching user reference image
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DeepNavyHeader)
                    .padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 18.dp)
            ) {
                // Top row: App title + Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "DKK Marketing",
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp,
                        color = Color.White
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Switch Role
                        if (onOpenRoleDialog != null) {
                            Surface(
                                onClick = onOpenRoleDialog,
                                shape = RoundedCornerShape(16.dp),
                                color = Color.White.copy(alpha = 0.15f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        Icons.Default.SwapHoriz,
                                        contentDescription = "Switch Role",
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        "Buyer",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        // Theme toggle
                        if (onToggleTheme != null) {
                            IconButton(onClick = onToggleTheme, modifier = Modifier.size(36.dp)) {
                                Icon(
                                    imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                                    contentDescription = "Toggle Theme",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        // Notifications bell with badge
                        Box(contentAlignment = Alignment.TopEnd) {
                            IconButton(
                                onClick = { /* View notifications */ },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    Icons.Default.Notifications,
                                    contentDescription = "Notifications",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .padding(top = 8.dp, end = 8.dp)
                                    .size(8.dp)
                                    .background(Color(0xFFEF4444), CircleShape)
                            )
                        }

                        // Logout button in top bar
                        if (onLogout != null) {
                            IconButton(
                                onClick = onLogout,
                                modifier = Modifier
                                    .size(36.dp)
                                    .testTag("buyer_top_logout_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                    contentDescription = "Log Out",
                                    tint = Color(0xFFFCA5A5),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Search Bar matching mockup: Rounded white pill with dark blue circular search button
                Surface(
                    shape = RoundedCornerShape(26.dp),
                    color = Color.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(start = 16.dp, end = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = onSearchQueryChange,
                            placeholder = {
                                Text(
                                    "Search for products...",
                                    fontSize = 13.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                focusedTextColor = Color(0xFF0F172A),
                                unfocusedTextColor = Color(0xFF0F172A)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("buyer_search_bar")
                        )

                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { onSearchQueryChange("") },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.Gray, modifier = Modifier.size(16.dp))
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                        }

                        // Circular dark navy button with search icon
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(DeepNavyHeader, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = "Search",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        // 2. Free Delivery Banner matching user mockup
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DeliveryBannerBg)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Orange rounded icon box with white delivery truck
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .background(OrangeDelivery, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.LocalShipping,
                            contentDescription = "Delivery Truck",
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Free Delivery",
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "ongoing in free delivery",
                            fontSize = 12.sp,
                            color = Color(0xFFBAE6FD)
                        )
                    }

                    // Special tag
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "PKR 0",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // 3. Delivery Options Filter: "ur delivery free he ya charges he ye option bi lgaw"
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Delivery Filter",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${displayedProducts.size} Items Found",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Option 1: All Items
                    Surface(
                        onClick = { onDeliveryFilterChange("ALL") },
                        shape = RoundedCornerShape(20.dp),
                        color = if (deliveryFilter == "ALL") DeepNavyHeader else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (deliveryFilter == "ALL") Color.White else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "All Items",
                            fontSize = 11.sp,
                            fontWeight = if (deliveryFilter == "ALL") FontWeight.Bold else FontWeight.Normal,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 7.dp)
                        )
                    }

                    // Option 2: Free Delivery
                    Surface(
                        onClick = { onDeliveryFilterChange("FREE") },
                        shape = RoundedCornerShape(20.dp),
                        color = if (deliveryFilter == "FREE") Color(0xFF166534) else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (deliveryFilter == "FREE") Color.White else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1.2f)
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 7.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🚚 Free Delivery",
                                fontSize = 11.sp,
                                fontWeight = if (deliveryFilter == "FREE") FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }

                    // Option 3: Delivery Charges
                    Surface(
                        onClick = { onDeliveryFilterChange("CHARGES") },
                        shape = RoundedCornerShape(20.dp),
                        color = if (deliveryFilter == "CHARGES") Color(0xFF1E293B) else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (deliveryFilter == "CHARGES") Color.White else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1.2f)
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 7.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "📦 Standard Fee",
                                fontSize = 11.sp,
                                fontWeight = if (deliveryFilter == "CHARGES") FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }

        // 4. Category Chips Row
        item {
            Spacer(modifier = Modifier.height(10.dp))
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { cat ->
                    val isSelected = selectedCategory == cat
                    Surface(
                        onClick = { onCategorySelected(cat) },
                        shape = RoundedCornerShape(18.dp),
                        color = if (isSelected) DeepNavyHeader else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.testTag("category_chip_$cat")
                    ) {
                        Text(
                            text = cat,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // 5. Featured Pro VIP Slot Carousel (Top priority)
        if (proVipProducts.isNotEmpty() && searchQuery.isBlank() && selectedCategory == "All" && deliveryFilter == "ALL") {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            ProVipBadge()
                            Text(
                                text = "Top Featured Slots",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        Text(
                            text = "Priority Listings",
                            fontSize = 11.sp,
                            color = DkkGold,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(proVipProducts) { vipProduct ->
                            Card(
                                modifier = Modifier
                                    .width(240.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { onProductSelected(vipProduct) }
                                    .testTag("vip_carousel_card_${vipProduct.productId}"),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(2.dp)
                            ) {
                                Column {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(120.dp)
                                    ) {
                                        AsyncImage(
                                            model = resolveProductImageModel(vipProduct.imageUrl),
                                            contentDescription = vipProduct.title,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.TopStart)
                                                .padding(6.dp)
                                        ) {
                                            ProVipBadge(small = true)
                                        }
                                        // Delivery badge in VIP
                                        Surface(
                                            modifier = Modifier
                                                .align(Alignment.BottomStart)
                                                .padding(6.dp),
                                            shape = RoundedCornerShape(4.dp),
                                            color = if (vipProduct.isFreeDelivery) Color(0xFF166534) else DeepNavyHeader
                                        ) {
                                            Text(
                                                text = if (vipProduct.isFreeDelivery) "Free Delivery" else "Delivery: Rs ${vipProduct.deliveryFee.toInt()}",
                                                color = Color.White,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(
                                            text = vipProduct.title,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = formatPkr(vipProduct.price),
                                                fontWeight = FontWeight.Black,
                                                color = DeepNavyHeader,
                                                fontSize = 14.sp
                                            )
                                            Button(
                                                onClick = { onBuyNow(vipProduct) },
                                                colors = ButtonDefaults.buttonColors(containerColor = DeepNavyHeader),
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                            ) {
                                                Text("Buy", fontSize = 11.sp, fontWeight = FontWeight.Bold)
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

        // 6. Section Title
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)
            ) {
                Text(
                    text = "Products (${displayedProducts.size})",
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        // 7. 2-Column Product Grid (Empty state handling)
        if (displayedProducts.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.ShoppingBag,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("No products match your filter", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Try selecting All Items or clear search", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }
        } else {
            // 2 items per row
            items(displayedProducts.chunked(2)) { pair ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    pair.forEach { prod ->
                        Box(modifier = Modifier.weight(1f)) {
                            ProductCard(
                                product = prod,
                                onProductClick = { onProductSelected(prod) },
                                onBuyNowClick = { onBuyNow(prod) }
                            )
                        }
                    }
                    if (pair.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

/**
 * Tab 1: Categories View
 */
@Composable
private fun BuyerCategoriesView(
    categories: List<String>,
    allProducts: List<ProductEntity>,
    onCategoryClicked: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Shop by Category",
            fontWeight = FontWeight.Black,
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Browse items filtered by specific department",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(categories) { cat ->
                val count = allProducts.count { it.category.equals(cat, ignoreCase = true) }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onCategoryClicked(cat) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(DeepNavyHeader.copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.GridView,
                                contentDescription = null,
                                tint = DeepNavyHeader,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(cat, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("$count products", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

/**
 * Tab 2: Orders & Tracking View with zero commission mentioned
 */
@Composable
private fun BuyerOrdersView(
    buyerOrders: List<OrderEntity>,
    onOpenSupportChat: (() -> Unit)?,
    onShopNow: () -> Unit,
    onRequestReturn: ((orderId: String, reason: String, proofScreenshotUrl: String, fileSizeBytes: Long) -> Unit)? = null
) {
    var returnTargetOrder by remember { mutableStateOf<OrderEntity?>(null) }
    if (buyerOrders.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.ShoppingBag,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "No Orders Placed Yet",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Browse items and checkout via Easypaisa or JS Bank to track orders here.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = onShopNow,
                    colors = ButtonDefaults.buttonColors(containerColor = DeepNavyHeader),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Start Shopping", fontWeight = FontWeight.Bold)
                }
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "My Orders (${buyerOrders.size})",
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Track your parcel and delivery confirmation OTP codes",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(buyerOrders) { order ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("buyer_order_card_${order.orderId}")
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
                                color = DeepNavyHeader
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(order.deliveryStatus.colorHex).copy(alpha = 0.15f),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    Color(order.deliveryStatus.colorHex)
                                )
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

                        Text(
                            text = order.productTitle,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Merchant: ${order.sellerName}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Paid via ${order.paymentMethod}", fontSize = 11.sp, color = EasypaisaGreen, fontWeight = FontWeight.Bold)
                                Text("Ref: ${order.paymentRef}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text(
                                text = formatPkr(order.orderAmount),
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp,
                                color = DeepNavyHeader
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Delivery OTP Security Box
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (order.otpVerified) Color(0xFFE8F5E9) else Color(0xFFFFFBEB),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (order.otpVerified) EasypaisaGreen else Color(0xFFF59E0B)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = if (order.otpVerified) "Delivery Verified with OTP" else "Your Delivery Handover OTP:",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (order.otpVerified) Color(0xFF1B5E20) else Color(0xFF92400E)
                                    )
                                    Text(
                                        text = if (order.otpVerified) "Payment released & order completed" else "Provide this code to rider when package arrives",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (order.otpVerified) EasypaisaGreen else DeepNavyHeader
                                ) {
                                    Text(
                                        text = if (order.otpVerified) "CONFIRMED" else order.otpCode,
                                        color = Color.White,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 14.sp,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }

                        // 📦 2-Day Return Guarantee Window Logic
                        val returnWindowMillis = 2 * 24 * 60 * 60 * 1000L // 48 hours = 2 days
                        val deliveredTime = order.deliveredAt ?: order.createdAt
                        val timeElapsed = (System.currentTimeMillis() - deliveredTime).coerceAtLeast(0)
                        val isWithin2Days = timeElapsed <= returnWindowMillis
                        val remainingHours = ((returnWindowMillis - timeElapsed) / (1000 * 60 * 60)).coerceAtLeast(0)

                        Spacer(modifier = Modifier.height(10.dp))

                        if (order.deliveryStatus == DeliveryStatus.RETURN_REQUESTED || order.returnStatus == "REQUESTED") {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFFEF3C7),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B)),
                                modifier = Modifier.fillMaxWidth().testTag("buyer_return_status_${order.orderId}")
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(16.dp))
                                        Text(
                                            text = "Wapsi Darkhwast Zere Ghor (Under Review)",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = Color(0xFF92400E)
                                        )
                                    }
                                    Text(
                                        text = "Kharabi ka saboot receipts_vault/ folder mein mahfooz hai. Owner Desk jaiza le raha hai.",
                                        fontSize = 10.sp,
                                        color = Color(0xFF78350F)
                                    )
                                    if (order.returnReason.isNotBlank()) {
                                        Text(
                                            text = "Wajah: ${order.returnReason}",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF92400E)
                                        )
                                    }
                                }
                            }
                        } else if (order.deliveryStatus == DeliveryStatus.RETURNED || order.returnStatus == "ACCEPTED") {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFF3E8FF),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF9333EA)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.AssignmentReturn, contentDescription = null, tint = Color(0xFF9333EA), modifier = Modifier.size(16.dp))
                                    Text(
                                        text = "Item Waps & Raqam Refund Ho Chuki Hai ✓",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = Color(0xFF6B21A8)
                                    )
                                }
                            }
                        } else if (order.deliveryStatus == DeliveryStatus.DELIVERED || order.otpVerified) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFF8FAFC),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1)),
                                modifier = Modifier.fillMaxWidth().testTag("buyer_return_policy_box_${order.orderId}")
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Icon(Icons.Default.LocalShipping, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(16.dp))
                                            Text(
                                                text = "📦 2 Din Ka Return Policy",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp,
                                                color = Color(0xFF0F172A)
                                            )
                                        }

                                        if (isWithin2Days) {
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = Color(0xFFE0F2FE)
                                            ) {
                                                Text(
                                                    text = "Baqi: ${remainingHours}h",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF0369A1),
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        } else {
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = Color(0xFFF1F5F9)
                                            ) {
                                                Text(
                                                    text = "Window Expired",
                                                    fontSize = 10.sp,
                                                    color = Color(0xFF64748B),
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }

                                    Text(
                                        text = if (isWithin2Days) {
                                            "Delivery ke 2 din honge agar koi waps karna chahye. Seller ko raqam jummay (Friday) ko muntaqil hoti hai."
                                        } else {
                                            "2 din ka return window mukammal ho gaya. Seller ko raqam Friday payout schedule ke mutabiq release ho chuki hai."
                                        },
                                        fontSize = 10.sp,
                                        color = Color(0xFF475569),
                                        lineHeight = 14.sp
                                    )

                                    if (isWithin2Days && onRequestReturn != null) {
                                        OutlinedButton(
                                            onClick = { returnTargetOrder = order },
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
                                            shape = RoundedCornerShape(8.dp),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF87171)),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("buyer_request_return_btn_${order.orderId}")
                                        ) {
                                            Icon(Icons.Default.AssignmentReturn, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Wapsi Ki Darkhwast (Return Item)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Direct Support Chat Assistance Card
            if (onOpenSupportChat != null) {
                item {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFF0F172A),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DkkGold.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenSupportChat() }
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .background(DeepNavyHeader, CircleShape)
                                        .border(1.dp, DkkGold, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Shield, contentDescription = null, tint = DkkGold, modifier = Modifier.size(22.dp))
                                }
                                Column {
                                    Text("Issue with an Order or Rider?", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                                    Text("Direct Encrypted Chat to Master Admin Desk", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                }
                            }
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Open Support", tint = DkkGold)
                        }
                    }
                }
            }
        }
    }

    if (returnTargetOrder != null) {
        val target = returnTargetOrder!!
        var returnReasonInput by remember { mutableStateOf("") }
        var proofScreenshotUrl by remember { mutableStateOf("https://picsum.photos/seed/return_${target.orderId}/800/800") }

        androidx.compose.ui.window.Dialog(
            onDismissRequest = { returnTargetOrder = null }
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = DkkNavy,
                border = androidx.compose.foundation.BorderStroke(1.dp, DkkGold),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Wapsi Darkhwast (2-Day Return)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                        IconButton(onClick = { returnTargetOrder = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }

                    Text(
                        text = "Item: ${target.productTitle} (Rs ${formatPkr(target.orderAmount)})",
                        fontSize = 12.sp,
                        color = DkkGold,
                        fontWeight = FontWeight.SemiBold
                    )

                    // 3.0 MB Security Firewall Notice
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF0F172A),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DkkEmerald.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "🛡️ 3.0 MB Security Firewall Active",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = DkkEmerald
                            )
                            Text(
                                text = "Kharabi ka screenshot receipts_vault/ folder mein mahfooz hoga. File size 3MB se kam hona lazmi hai taake system crash na kare.",
                                fontSize = 10.sp,
                                color = Color(0xFFCBD5E1)
                            )
                        }
                    }

                    OutlinedTextField(
                        value = returnReasonInput,
                        onValueChange = { returnReasonInput = it },
                        label = { Text("Kharabi / Nuqs Ki Wajah (Reason for Return)", fontSize = 11.sp) },
                        placeholder = { Text("e.g. Size kharab hai, toota hua piece aya", fontSize = 11.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("return_reason_input")
                    )

                    OutlinedTextField(
                        value = proofScreenshotUrl,
                        onValueChange = { proofScreenshotUrl = it },
                        label = { Text("Saboot / Defect Screenshot URL", fontSize = 11.sp) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("return_screenshot_url_input")
                    )

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF1E293B),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Proof File Size:", fontSize = 10.sp, color = Color(0xFF94A3B8))
                            Text("1.4 MB (Under 3.0 MB OK ✓)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = DkkEmerald)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { returnTargetOrder = null },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Mansookh (Cancel)", fontSize = 11.sp)
                        }

                        Button(
                            onClick = {
                                if (returnReasonInput.isNotBlank() && onRequestReturn != null) {
                                    onRequestReturn(
                                        target.orderId,
                                        returnReasonInput.trim(),
                                        proofScreenshotUrl.trim(),
                                        1_400_000L
                                    )
                                    returnTargetOrder = null
                                }
                            },
                            enabled = returnReasonInput.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("submit_return_request_btn")
                        ) {
                            Text("Submit Return ✓", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Tab 3: Buyer Profile View
 */
@Composable
private fun BuyerProfileView(
    userName: String,
    userPhone: String,
    buyerOrdersCount: Int,
    onOpenRoleDialog: (() -> Unit)?,
    onToggleTheme: (() -> Unit)?,
    isDarkTheme: Boolean,
    onOpenSupportChat: (() -> Unit)?,
    onLogout: (() -> Unit)? = null
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Buyer Profile",
                fontWeight = FontWeight.Black,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(6.dp))
        }

        // User Details Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .background(DeepNavyHeader, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = userName.take(1).uppercase(),
                            fontWeight = FontWeight.Black,
                            fontSize = 22.sp,
                            color = Color.White
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(userName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(
                            userPhone.ifEmpty { "Registered Buyer" },
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFE8F5E9)
                        ) {
                            Text(
                                "DKK Escrow Buyer Protected ✓",
                                fontSize = 10.sp,
                                color = Color(0xFF166534),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        // Quick Actions
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    if (onOpenRoleDialog != null) {
                        ProfileMenuRow(
                            icon = Icons.Default.SwapHoriz,
                            title = "Switch Persona / Role",
                            subtitle = "Switch between Buyer, Seller, Rider & Admin",
                            onClick = onOpenRoleDialog
                        )
                    }

                    if (onToggleTheme != null) {
                        ProfileMenuRow(
                            icon = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                            title = "App Theme",
                            subtitle = if (isDarkTheme) "Dark Mode Active (Tap to switch)" else "Light Mode Active (Tap to switch)",
                            onClick = onToggleTheme
                        )
                    }

                    if (onOpenSupportChat != null) {
                        ProfileMenuRow(
                            icon = Icons.Default.HeadsetMic,
                            title = "24/7 DKK Support Desk",
                            subtitle = "Direct live chat with platform manager",
                            onClick = onOpenSupportChat
                        )
                    }
                }
            }
        }

        // Dedicated Red Logout Card
        if (onLogout != null) {
            item {
                Card(
                    onClick = onLogout,
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFCA5A5)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("buyer_profile_logout_btn")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .background(Color(0xFFEF4444).copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                    contentDescription = "Log Out",
                                    tint = Color(0xFFDC2626),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Log Out (لاگ آؤٹ)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color(0xFFDC2626)
                                )
                                Text(
                                    text = "Apne account se log out ho kar login screen par jayein",
                                    fontSize = 11.sp,
                                    color = Color(0xFF991B1B)
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Color(0xFFDC2626),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Buyer Escrow Policy Notice
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        Icons.Default.Shield,
                        contentDescription = null,
                        tint = DeepNavyHeader,
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text("100% Escrow Guarantee", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = DeepNavyHeader)
                        Text(
                            "Your payment is held safely until you verify delivery with your unique OTP. Transparent and reliable across Pakistan.",
                            fontSize = 11.sp,
                            color = Color(0xFF475569)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileMenuRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .background(DeepNavyHeader.copy(alpha = 0.08f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = DeepNavyHeader, modifier = Modifier.size(18.dp))
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Text(subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Icon(
            Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp)
        )
    }
}

/**
 * Product detail sheet with ZERO commission details shown to buyer!
 * Only PKR (Rs) price and explicit Delivery Fee / Free Delivery details.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailSheet(
    product: ProductEntity,
    onDismiss: () -> Unit,
    onBuyNow: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val deliveryFee = if (product.isFreeDelivery) 0.0 else product.deliveryFee
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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(14.dp))
            ) {
                AsyncImage(
                    model = resolveProductImageModel(product.imageUrl),
                    contentDescription = product.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                if (product.isProVip) {
                    Box(modifier = Modifier.padding(10.dp).align(Alignment.TopStart)) {
                        ProVipBadge()
                    }

                    // VIP 15-second Video Preview Banner & Play overlay
                    Surface(
                        modifier = Modifier.align(Alignment.Center),
                        shape = RoundedCornerShape(20.dp),
                        color = Color.Black.copy(alpha = 0.65f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.PlayCircle, contentDescription = "Play 15s Video", tint = DkkGold, modifier = Modifier.size(24.dp))
                            Text("15s VIP Video (≤3MB)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = product.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Price & Delivery Badge (No commission shown!)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatPkr(product.price),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = DeepNavyHeader
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (product.isFreeDelivery) Color(0xFFE8F5E9) else Color(0xFFF1F5F9),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (product.isFreeDelivery) Color(0xFF2E7D32) else Color(0xFFCBD5E1)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.LocalShipping,
                            contentDescription = null,
                            tint = if (product.isFreeDelivery) Color(0xFF2E7D32) else DeepNavyHeader,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = if (product.isFreeDelivery) "Free Delivery" else "Delivery: ${formatPkr(deliveryFee)}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = if (product.isFreeDelivery) Color(0xFF2E7D32) else DeepNavyHeader
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Price Breakdown Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Item Price:", fontSize = 12.sp)
                        Text(formatPkr(product.price), fontWeight = FontWeight.Medium, fontSize = 12.sp)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Delivery Charges:", fontSize = 12.sp)
                        Text(
                            text = if (product.isFreeDelivery) "Free (Rs 0)" else formatPkr(deliveryFee),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = if (product.isFreeDelivery) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total Payable:", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text(formatPkr(totalPayable), fontWeight = FontWeight.Black, fontSize = 14.sp, color = DeepNavyHeader)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Merchant Info
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(10.dp))
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Storefront, contentDescription = null, tint = DeepNavyHeader)
                Column {
                    Text("Verified DKK Merchant", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(product.sellerName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text("Product Description", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = product.description,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Privacy & Address Boundary Banner
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Shield,
                        contentDescription = "Privacy Shield",
                        tint = DeepNavyHeader,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "DKK Privacy Protocol: Delivery secured strictly via dynamic OTP verification.",
                        fontSize = 11.sp,
                        color = Color(0xFF334155)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = onBuyNow,
                colors = ButtonDefaults.buttonColors(containerColor = EasypaisaGreen),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("detail_buy_easypaisa_btn")
            ) {
                Icon(Icons.Default.Payment, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Checkout via Easypaisa (${formatPkr(totalPayable)})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.OrderDao
import com.example.data.dao.ProductDao
import com.example.data.dao.SellerDao
import com.example.data.dao.UserDao
import com.example.data.model.DeliveryStatus
import com.example.data.model.OrderEntity
import com.example.data.model.ProductEntity
import com.example.data.model.SellerApprovalStatus
import com.example.data.model.SellerProfileEntity
import com.example.data.model.UserEntity
import com.example.data.model.UserRole
import com.example.data.repository.CommissionCalculator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Converters {
    @TypeConverter
    fun fromUserRole(role: UserRole): String = role.name

    @TypeConverter
    fun toUserRole(value: String): UserRole = try {
        UserRole.valueOf(value)
    } catch (e: Exception) {
        UserRole.BUYER
    }

    @TypeConverter
    fun fromDeliveryStatus(status: DeliveryStatus): String = status.name

    @TypeConverter
    fun toDeliveryStatus(value: String): DeliveryStatus = try {
        DeliveryStatus.valueOf(value)
    } catch (e: Exception) {
        DeliveryStatus.PENDING
    }

    @TypeConverter
    fun fromSellerApprovalStatus(status: SellerApprovalStatus): String = status.name

    @TypeConverter
    fun toSellerApprovalStatus(value: String): SellerApprovalStatus = try {
        SellerApprovalStatus.valueOf(value)
    } catch (e: Exception) {
        SellerApprovalStatus.APPROVED
    }
}

@Database(
    entities = [
        UserEntity::class,
        SellerProfileEntity::class,
        ProductEntity::class,
        OrderEntity::class
    ],
    version = 6,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class DkkDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun sellerDao(): SellerDao
    abstract fun productDao(): ProductDao
    abstract fun orderDao(): OrderDao

    companion object {
        @Volatile
        private var INSTANCE: DkkDatabase? = null

        fun getDatabase(context: Context): DkkDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DkkDatabase::class.java,
                    "dkk_marketing_database"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .addCallback(DatabaseCallback(context))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val context: Context
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                CoroutineScope(Dispatchers.IO).launch {
                    val database = getDatabase(context)
                    populateInitialData(database)
                }
            }
        }

        suspend fun populateInitialData(db: DkkDatabase) {
            val userDao = db.userDao()
            val sellerDao = db.sellerDao()
            val productDao = db.productDao()
            val orderDao = db.orderDao()

            // 1. Initial Users
            val ownerUser = UserEntity(
                uid = "OWNER_001",
                phoneNumber = "03330206001",
                email = "rockdildarkhan@gmail.com",
                name = "Daro Khan (DKK Owner)",
                profilePicture = "admin_avatar",
                role = UserRole.OWNER,
                isProVip = true
            )

            val seller1 = UserEntity(
                uid = "SELLER_001",
                phoneNumber = "03108219408",
                email = "aslam.universe@dkk.pk",
                name = "Muhammad Aslam (Universe Jewellery)",
                profilePicture = "seller_avatar_1",
                role = UserRole.SELLER,
                isProVip = true
            )

            val seller2 = UserEntity(
                uid = "SELLER_002",
                phoneNumber = "03335554433",
                email = "hassan.textiles@dkk.pk",
                name = "Hassan Raza (Raza Premium Garments)",
                profilePicture = "seller_avatar_2",
                role = UserRole.SELLER,
                isProVip = false
            )

            val seller3 = UserEntity(
                uid = "SELLER_003",
                phoneNumber = "03457788990",
                email = "bilal.gadgets@dkk.pk",
                name = "Bilal Khan (DKK Tech Hub)",
                profilePicture = "seller_avatar_3",
                role = UserRole.SELLER,
                isProVip = true
            )

            val sellerPending = UserEntity(
                uid = "SELLER_004",
                phoneNumber = "03124449911",
                email = "usman.jewels@dkk.pk",
                name = "Usman Ali (Crown Gold & Watches)",
                profilePicture = "seller_avatar_4",
                role = UserRole.SELLER,
                isProVip = false
            )

            val buyerUser = UserEntity(
                uid = "BUYER_001",
                phoneNumber = "03273856001",
                email = "buyer.ahmed@gmail.com",
                name = "Ahmed Nawaz",
                profilePicture = "buyer_avatar",
                role = UserRole.BUYER,
                isProVip = false
            )

            userDao.insertUser(ownerUser)
            userDao.insertUser(seller1)
            userDao.insertUser(seller2)
            userDao.insertUser(seller3)
            userDao.insertUser(sellerPending)
            userDao.insertUser(buyerUser)

            // 2. Initial Sellers
            sellerDao.insertSeller(
                SellerProfileEntity(
                    uid = "SELLER_001",
                    businessName = "Universe Jewellery",
                    faceScanVerified = true,
                    itemsSoldCount = 26,
                    sellerLevel = 3, // Gold
                    totalEarnings = 412000.0,
                    commissionPaid = 33800.0,
                    rating = 5.0,
                    approvalStatus = SellerApprovalStatus.APPROVED,
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

            sellerDao.insertSeller(
                SellerProfileEntity(
                    uid = "SELLER_002",
                    businessName = "Raza Premium Garments",
                    faceScanVerified = true,
                    itemsSoldCount = 8,
                    sellerLevel = 2, // Silver
                    totalEarnings = 96000.0,
                    commissionPaid = 8400.0,
                    rating = 4.7,
                    approvalStatus = SellerApprovalStatus.APPROVED,
                    bankName = "JS Bank",
                    bankAccountTitle = "Raza Premium Garments",
                    bankAccountNumber = "0003015314",
                    bankIban = "PK18JSBL9620000003015314",
                    easypaisaNumber = "03459876543",
                    easypaisaTitle = "Raza Garments",
                    payoutSchedule = "Every Friday (جمعہ کے روز)",
                    returnWindowDays = 2
                )
            )

            sellerDao.insertSeller(
                SellerProfileEntity(
                    uid = "SELLER_003",
                    businessName = "DKK Tech Hub",
                    faceScanVerified = true,
                    itemsSoldCount = 54,
                    sellerLevel = 5, // Master
                    totalEarnings = 1250000.0,
                    commissionPaid = 78000.0,
                    rating = 5.0,
                    approvalStatus = SellerApprovalStatus.APPROVED
                )
            )

            sellerDao.insertSeller(
                SellerProfileEntity(
                    uid = "SELLER_004",
                    businessName = "Crown Gold & Watches",
                    faceScanVerified = true,
                    itemsSoldCount = 0,
                    sellerLevel = 1,
                    totalEarnings = 0.0,
                    commissionPaid = 0.0,
                    rating = 5.0,
                    approvalStatus = SellerApprovalStatus.PENDING_APPROVAL
                )
            )

            // 3. Products across all Pakistani categories (With Free Delivery & Delivery Charges options)
            val p1 = ProductEntity(
                productId = "PROD_VIP_001",
                sellerId = "SELLER_002",
                sellerName = "Raza Premium Garments",
                title = "Men's Luxury Cotton Boski Kurta Shalwar Suit",
                description = "Original Faisalabad soft-wash premium cotton kurta shalwar with delicate thread embroidery.",
                price = 3499.0,
                category = "Shalwar Kameez & Lawn",
                imageUrl = "https://images.unsplash.com/photo-1593030761757-71fae45fa0e7?w=600&auto=format&fit=crop&q=80",
                isProVip = true,
                isFreeDelivery = true,
                deliveryFee = 0.0
            )

            val p2 = ProductEntity(
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

            val p3 = ProductEntity(
                productId = "PROD_VIP_003",
                sellerId = "SELLER_003",
                sellerName = "DKK Tech Hub",
                title = "Xiaomi Touch Control Roti Maker & Air Fryer 5L",
                description = "Non-stick automated chapati & roti press with rapid heat circulation. Easy household cooking.",
                price = 18500.0,
                category = "Home Appliances",
                imageUrl = "https://images.unsplash.com/photo-1550745165-9bc0b252726f?w=600&auto=format&fit=crop&q=80",
                isProVip = false,
                isFreeDelivery = true,
                deliveryFee = 0.0
            )

            val p4 = ProductEntity(
                productId = "PROD_004",
                sellerId = "SELLER_002",
                sellerName = "Khyber Leather Crafts",
                title = "Original Charsadda / Peshawari Double Sole Leather Chappal",
                description = "Handcrafted genuine cowhide leather with tyre sole stitching. Durable, classic Pashtun heritage.",
                price = 3800.0,
                category = "Peshawari Chappal & Khussa",
                imageUrl = "https://images.unsplash.com/photo-1607604276583-eef5d076aa5f?w=600&auto=format&fit=crop&q=80",
                isProVip = true,
                isFreeDelivery = true,
                deliveryFee = 0.0
            )

            val p5 = ProductEntity(
                productId = "PROD_005",
                sellerId = "SELLER_002",
                sellerName = "Sindh Heritage Store",
                title = "Traditional Sindhi Ajrak & Mirror Work Sindhi Topi Pack",
                description = "Authentic natural indigo dye block-printed Ajrak shawl with hand-embroidered mirror cap.",
                price = 2200.0,
                category = "Handicrafts & Ajrak",
                imageUrl = "https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=600&auto=format&fit=crop&q=80",
                isProVip = false,
                isFreeDelivery = true,
                deliveryFee = 0.0
            )

            val p6 = ProductEntity(
                productId = "PROD_006",
                sellerId = "SELLER_003",
                sellerName = "Quetta Dry Fruit Mart",
                title = "Quetta Premium Kaghan Walnut & Irani Pista Mix (1KG)",
                description = "Freshly harvested paper-shell akhrot, salted roasted pistachios, and chilgoza pack.",
                price = 3200.0,
                category = "Dry Fruits & Desi Food",
                imageUrl = "https://images.unsplash.com/photo-1546435770-a3e426bf472b?w=600&auto=format&fit=crop&q=80",
                isProVip = false,
                isFreeDelivery = true,
                deliveryFee = 0.0
            )

            val p7 = ProductEntity(
                productId = "PROD_007",
                sellerId = "SELLER_003",
                sellerName = "Sialkot Sports Factory",
                title = "Original Sialkot English Willow Hardball Bat (CA 15000 Edition)",
                description = "Grade 1 English willow, thick edges, massive sweet spot with fitted toe guard & cover.",
                price = 14500.0,
                category = "Sports & Sialkot Goods",
                imageUrl = "https://images.unsplash.com/photo-1593359677879-a4bb92f829d1?w=600&auto=format&fit=crop&q=80",
                isProVip = true,
                isFreeDelivery = false,
                deliveryFee = 250.0
            )

            val p8 = ProductEntity(
                productId = "PROD_008",
                sellerId = "SELLER_001",
                sellerName = "Al-Madina Electronics",
                title = "Apple iPhone 15 Pro Max 256GB (Official PTA Approved)",
                description = "Natural Titanium, 100% battery health, official physical dual sim with complete box.",
                price = 385000.0,
                category = "Mobiles & Accessories",
                imageUrl = "https://images.unsplash.com/photo-1695048133142-1a20484d2569?w=600&auto=format&fit=crop&q=80",
                isProVip = true,
                isFreeDelivery = false,
                deliveryFee = 250.0
            )

            val p9 = ProductEntity(
                productId = "PROD_009",
                sellerId = "SELLER_002",
                sellerName = "Haramain Attar House",
                title = "Original Janat-ul-Firdous & Pure Dehn Al-Oud 12ml Attar",
                description = "Alcohol-free non-alcoholic concentrated pure Arabian fragrance with rich woody notes.",
                price = 1850.0,
                category = "Attar & Fragrances",
                imageUrl = "https://images.unsplash.com/photo-1594035910387-fea47794261f?w=600&auto=format&fit=crop&q=80",
                isProVip = false,
                isFreeDelivery = true,
                deliveryFee = 0.0
            )

            val p10 = ProductEntity(
                productId = "PROD_010",
                sellerId = "SELLER_001",
                sellerName = "Rawalpindi Auto Spare Parts",
                title = "Honda CD 70 / 125 Complete LED Headlight & Indicator Set",
                description = "High focus projector white LED light with flasher indicators and metal bracket.",
                price = 2400.0,
                category = "Bikes & Auto Parts",
                imageUrl = "https://images.unsplash.com/photo-1558981403-c5f9899a28bc?w=600&auto=format&fit=crop&q=80",
                isProVip = false,
                isFreeDelivery = false,
                deliveryFee = 150.0
            )

            val p11 = ProductEntity(
                productId = "PROD_011",
                sellerId = "SELLER_004",
                sellerName = "Crown Gold & Watches",
                title = "Bridal Kundan 24K Gold Plated Necklace & Jhumka Set",
                description = "Royal Rajasthani Kundan bridal set with matching earrings and maang tikka in velvet box.",
                price = 6800.0,
                category = "Jewellery & Traditional Sets",
                imageUrl = "https://images.unsplash.com/photo-1535632066927-ab7c9ab60908?w=600&auto=format&fit=crop&q=80",
                isProVip = false,
                isFreeDelivery = true,
                deliveryFee = 0.0
            )

            val p12 = ProductEntity(
                productId = "PROD_012",
                sellerId = "SELLER_003",
                sellerName = "Punjab Pure Farms",
                title = "Original Desi Buffalo Ghee 1KG Jar (Farm Fresh)",
                description = "Traditional Bilona churned pure desi ghee, rich aroma & 100% organic guarantee.",
                price = 2900.0,
                category = "Grocery & Chai",
                imageUrl = "https://images.unsplash.com/photo-1589927986089-35812388d1f4?w=600&auto=format&fit=crop&q=80",
                isProVip = false,
                isFreeDelivery = true,
                deliveryFee = 0.0
            )

            productDao.insertProduct(p1)
            productDao.insertProduct(p2)
            productDao.insertProduct(p3)
            productDao.insertProduct(p4)
            productDao.insertProduct(p5)
            productDao.insertProduct(p6)
            productDao.insertProduct(p7)
            productDao.insertProduct(p8)
            productDao.insertProduct(p9)
            productDao.insertProduct(p10)
            productDao.insertProduct(p11)
            productDao.insertProduct(p12)

            // 4. Initial Orders showcasing each Commission Tier
            // Tier 1 order (≤ 10k: 10%)
            val order1Amt = 8500.0
            val order1Comm = CommissionCalculator.calculateCommission(order1Amt) // 850
            orderDao.insertOrder(
                OrderEntity(
                    orderId = "DKK-ORD-101",
                    buyerId = "BUYER_001",
                    buyerPhone = "03273856001",
                    sellerId = "SELLER_002",
                    sellerName = "Raza Premium Garments",
                    productId = "PROD_004",
                    productTitle = "Embroidered Pakistani Men's Kurta & Shalwar Suite",
                    orderAmount = order1Amt,
                    commissionAmount = order1Comm,
                    commissionRatePercent = 10.0,
                    paymentMethod = "Easypaisa",
                    paymentRef = "EP-94821034",
                    deliveryStatus = DeliveryStatus.DELIVERED,
                    otpCode = "7492",
                    otpVerified = true,
                    createdAt = System.currentTimeMillis() - 86400000 * 2
                )
            )

            // Tier 2 order (10,001 - 20,000: 8%)
            val order2Amt = 15000.0
            val order2Comm = CommissionCalculator.calculateCommission(order2Amt) // 1200
            orderDao.insertOrder(
                OrderEntity(
                    orderId = "DKK-ORD-102",
                    buyerId = "BUYER_001",
                    buyerPhone = "03273856001",
                    sellerId = "SELLER_001",
                    sellerName = "Muhammad Aslam (Universe Jewellery)",
                    productId = "PROD_008",
                    productTitle = "Xiaomi Smart Air Fryer Pro 4L OLED Display",
                    orderAmount = order2Amt,
                    commissionAmount = order2Comm,
                    commissionRatePercent = 8.0,
                    paymentMethod = "Easypaisa",
                    paymentRef = "EP-88231901",
                    deliveryStatus = DeliveryStatus.DELIVERED,
                    otpCode = "4819",
                    otpVerified = true,
                    createdAt = System.currentTimeMillis() - 86400000
                )
            )

            // Tier 3 order (20,001 - 35,000: 7%)
            val order3Amt = 28000.0
            val order3Comm = CommissionCalculator.calculateCommission(order3Amt) // 1960
            orderDao.insertOrder(
                OrderEntity(
                    orderId = "DKK-ORD-103",
                    buyerId = "BUYER_001",
                    buyerPhone = "03273856001",
                    sellerId = "SELLER_002",
                    sellerName = "Raza Premium Garments",
                    productId = "PROD_006",
                    productTitle = "Pure Kashmiri Pashmina Hand-Woven Shawl",
                    orderAmount = order3Amt,
                    commissionAmount = order3Comm,
                    commissionRatePercent = 7.0,
                    paymentMethod = "Easypaisa",
                    paymentRef = "EP-77441199",
                    deliveryStatus = DeliveryStatus.DISPATCHED,
                    otpCode = "9201",
                    otpVerified = false,
                    createdAt = System.currentTimeMillis() - 43200000
                )
            )

            // Tier 4 order (> 35,000: 5%)
            val order4Amt = 78000.0
            val order4Comm = CommissionCalculator.calculateCommission(order4Amt) // 3900
            orderDao.insertOrder(
                OrderEntity(
                    orderId = "DKK-ORD-104",
                    buyerId = "BUYER_001",
                    buyerPhone = "03273856001",
                    sellerId = "SELLER_003",
                    sellerName = "DKK Tech Hub",
                    productId = "PROD_VIP_003",
                    productTitle = "Sony WH-1000XM5 Wireless ANC Headphones",
                    orderAmount = order4Amt,
                    commissionAmount = order4Comm,
                    commissionRatePercent = 5.0,
                    paymentMethod = "Easypaisa",
                    paymentRef = "EP-66332211",
                    deliveryStatus = DeliveryStatus.PENDING,
                    otpCode = "3194",
                    otpVerified = false,
                    createdAt = System.currentTimeMillis() - 10800000
                )
            )
        }
    }
}

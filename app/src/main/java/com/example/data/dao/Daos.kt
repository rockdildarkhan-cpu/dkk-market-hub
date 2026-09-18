package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.OrderEntity
import com.example.data.model.ProductEntity
import com.example.data.model.SellerApprovalStatus
import com.example.data.model.SellerProfileEntity
import com.example.data.model.UserEntity
import com.example.data.model.UserRole
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE uid = :uid")
    fun getUserById(uid: String): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE uid = :uid")
    suspend fun getUserDirect(uid: String): UserEntity?

    @Query("SELECT * FROM users WHERE phoneNumber = :phone LIMIT 1")
    suspend fun getUserByPhone(phone: String): UserEntity?

    @Query("SELECT * FROM users ORDER BY createdAt DESC")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users")
    suspend fun getAllUsersDirect(): List<UserEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("UPDATE users SET isProVip = :isVip WHERE uid = :uid")
    suspend fun updateProVip(uid: String, isVip: Boolean)

    @Query("UPDATE users SET role = :role WHERE uid = :uid")
    suspend fun updateRole(uid: String, role: UserRole)

    @Query("SELECT COUNT(*) FROM users")
    fun getTotalUsersCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM users WHERE role = 'BUYER'")
    fun getBuyersCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM users WHERE role = 'SELLER'")
    fun getSellersCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM users WHERE isProVip = 1")
    fun getProVipCount(): Flow<Int>
}

@Dao
interface SellerDao {
    @Query("SELECT * FROM sellers WHERE uid = :uid")
    fun getSellerProfile(uid: String): Flow<SellerProfileEntity?>

    @Query("SELECT * FROM sellers WHERE uid = :uid")
    suspend fun getSellerProfileDirect(uid: String): SellerProfileEntity?

    @Query("SELECT * FROM sellers ORDER BY itemsSoldCount DESC")
    fun getAllSellers(): Flow<List<SellerProfileEntity>>

    @Query("SELECT * FROM sellers WHERE approvalStatus = 'PENDING_APPROVAL' OR approvalStatus = 'PENDING_VERIFICATION'")
    fun getPendingApprovalSellers(): Flow<List<SellerProfileEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSeller(seller: SellerProfileEntity)

    @Update
    suspend fun updateSeller(seller: SellerProfileEntity)

    @Query("UPDATE sellers SET faceScanVerified = :verified, approvalStatus = :status WHERE uid = :uid")
    suspend fun updateFaceScanStatus(uid: String, verified: Boolean, status: SellerApprovalStatus)

    @Query("UPDATE sellers SET approvalStatus = :status WHERE uid = :uid")
    suspend fun updateApprovalStatus(uid: String, status: SellerApprovalStatus)

    @Query("UPDATE sellers SET itemsSoldCount = itemsSoldCount + 1, sellerLevel = :newLevel, totalEarnings = totalEarnings + :netEarning, commissionPaid = commissionPaid + :commission WHERE uid = :uid")
    suspend fun recordSale(uid: String, newLevel: Int, netEarning: Double, commission: Double)

    @Query("UPDATE sellers SET bankName = :bankName, bankAccountTitle = :bankAccountTitle, bankAccountNumber = :bankAccountNumber, bankIban = :bankIban, easypaisaNumber = :easypaisaNumber, easypaisaTitle = :easypaisaTitle WHERE uid = :uid")
    suspend fun updateSellerPayoutDetails(
        uid: String,
        bankName: String,
        bankAccountTitle: String,
        bankAccountNumber: String,
        bankIban: String,
        easypaisaNumber: String,
        easypaisaTitle: String
    )
}

@Dao
interface ProductDao {
    @Query("SELECT * FROM products WHERE isAvailable = 1 ORDER BY isProVip DESC, createdAt DESC")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products")
    suspend fun getAllProductsDirect(): List<ProductEntity>

    @Query("SELECT * FROM products WHERE productId = :productId")
    fun getProductById(productId: String): Flow<ProductEntity?>

    @Query("SELECT * FROM products WHERE productId = :productId LIMIT 1")
    suspend fun getProductDirect(productId: String): ProductEntity?

    @Query("SELECT * FROM products WHERE sellerId = :sellerId ORDER BY createdAt DESC")
    fun getProductsBySeller(sellerId: String): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE isProVip = 1 AND isAvailable = 1 ORDER BY createdAt DESC")
    fun getProVipProducts(): Flow<List<ProductEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity)

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Query("DELETE FROM products WHERE productId = :productId")
    suspend fun deleteProduct(productId: String)
}

@Dao
interface OrderDao {
    @Query("SELECT * FROM orders ORDER BY createdAt DESC")
    fun getAllOrders(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders")
    suspend fun getAllOrdersDirect(): List<OrderEntity>

    @Query("SELECT * FROM orders WHERE buyerId = :buyerId ORDER BY createdAt DESC")
    fun getOrdersByBuyer(buyerId: String): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE sellerId = :sellerId ORDER BY createdAt DESC")
    fun getOrdersBySeller(sellerId: String): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE orderId = :orderId")
    suspend fun getOrderById(orderId: String): OrderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity)

    @Update
    suspend fun updateOrder(order: OrderEntity)

    @Query("UPDATE orders SET deliveryStatus = :status WHERE orderId = :orderId")
    suspend fun updateDeliveryStatus(orderId: String, status: String)

    @Query("UPDATE orders SET deliveryStatus = 'DELIVERED', otpVerified = 1, deliveredAt = :deliveredAt WHERE orderId = :orderId")
    suspend fun markOrderDeliveredWithTime(orderId: String, deliveredAt: Long = System.currentTimeMillis())

    @Query("UPDATE orders SET deliveryStatus = 'RETURN_REQUESTED', returnReason = :reason, returnProofUrl = :proofUrl, returnStatus = 'REQUESTED' WHERE orderId = :orderId")
    suspend fun requestOrderReturn(orderId: String, reason: String, proofUrl: String)

    @Query("UPDATE orders SET isFridayCleared = 1, payoutClearedAt = :clearedAt WHERE sellerId = :sellerId AND deliveryStatus = 'DELIVERED'")
    suspend fun clearFridayPayoutForSeller(sellerId: String, clearedAt: Long = System.currentTimeMillis())

    @Query("UPDATE orders SET isFridayCleared = 1, payoutClearedAt = :clearedAt WHERE deliveryStatus = 'DELIVERED'")
    suspend fun clearAllFridayPayouts(clearedAt: Long = System.currentTimeMillis())
}

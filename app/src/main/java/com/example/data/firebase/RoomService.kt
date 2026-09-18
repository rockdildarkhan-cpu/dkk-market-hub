package com.example.data.firebase

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Data model representing an individual seat in the 5-Mic luxury stage.
 */
data class MicSeat(
    val seatIndex: Int = 0,
    val occupantUid: String? = null,
    val occupantName: String? = null,
    val occupantAvatar: String? = null,
    val isMuted: Boolean = true,
    val isTalking: Boolean = false,
    val isLocked: Boolean = false,
    val giftScore: Long = 0L,
    val joinedAt: Long = 0L
) {
    val isOccupied: Boolean get() = !occupantUid.isNullOrBlank()

    fun toMap(): Map<String, Any?> = mapOf(
        "seatIndex" to seatIndex,
        "occupantUid" to occupantUid,
        "occupantName" to occupantName,
        "occupantAvatar" to occupantAvatar,
        "isMuted" to isMuted,
        "isTalking" to isTalking,
        "isLocked" to isLocked,
        "giftScore" to giftScore,
        "joinedAt" to joinedAt
    )

    companion object {
        fun fromMap(index: Int, map: Map<String, Any?>?): MicSeat {
            if (map == null) return MicSeat(seatIndex = index)
            return MicSeat(
                seatIndex = (map["seatIndex"] as? Number)?.toInt() ?: index,
                occupantUid = map["occupantUid"] as? String,
                occupantName = map["occupantName"] as? String,
                occupantAvatar = map["occupantAvatar"] as? String,
                isMuted = map["isMuted"] as? Boolean ?: true,
                isTalking = map["isTalking"] as? Boolean ?: false,
                isLocked = map["isLocked"] as? Boolean ?: (index == 7 || index == 9),
                giftScore = (map["giftScore"] as? Number)?.toLong() ?: 0L,
                joinedAt = (map["joinedAt"] as? Number)?.toLong() ?: 0L
            )
        }
    }
}

/**
 * Data model representing a live participant or listener in the audio room.
 */
data class RoomParticipant(
    val uid: String = "",
    val displayName: String = "",
    val avatarUrl: String = "",
    val isVip: Boolean = false,
    val country: String = "AE",
    val joinedAt: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "uid" to uid,
        "displayName" to displayName,
        "avatarUrl" to avatarUrl,
        "isVip" to isVip,
        "country" to country,
        "joinedAt" to joinedAt
    )

    companion object {
        fun fromMap(map: Map<String, Any?>): RoomParticipant {
            return RoomParticipant(
                uid = map["uid"] as? String ?: "",
                displayName = map["displayName"] as? String ?: "Guest",
                avatarUrl = map["avatarUrl"] as? String ?: "",
                isVip = map["isVip"] as? Boolean ?: false,
                country = map["country"] as? String ?: "AE",
                joinedAt = (map["joinedAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
            )
        }
    }
}

/**
 * Status of the audio room.
 */
enum class RoomStatus {
    LIVE,
    PAUSED,
    ENDED
}

/**
 * Complete state representation of a 5-Mic Social Audio Room.
 */
data class AudioRoom(
    val roomId: String = "",
    val title: String = "",
    val hostUid: String = "",
    val hostName: String = "",
    val hostAvatar: String = "",
    val countryFlag: String = "🇦🇪",
    val countryName: String = "UAE",
    val status: RoomStatus = RoomStatus.LIVE,
    val listenerCount: Int = 0,
    val seats: List<MicSeat> = (0 until 10).map { MicSeat(seatIndex = it, isLocked = (it == 7 || it == 9)) },
    val videoStreamUrl: String? = null,
    val isVideoLoopActive: Boolean = false,
    val totalCoinsGifted: Long = 0L,
    val roomDpUrl: String = "👑",
    val roomBannerUrl: String = "Midnight Blue",
    val announcement: String = "Welcome to D.K.K. Live Audio! Please respect everyone and chat decently. 🎙️✨",
    val themeId: String = "cosmic_earth",
    val createdAt: Long = System.currentTimeMillis()
) {
    val occupiedSeatsCount: Int get() = seats.count { it.isOccupied }
    val availableSeatsCount: Int get() = seats.count { !it.isOccupied }

    fun getSeat(index: Int): MicSeat? = seats.getOrNull(index)

    companion object {
        const val MAX_MIC_SEATS = 10

        @Suppress("UNCHECKED_CAST")
        fun fromDocument(id: String, data: Map<String, Any?>): AudioRoom {
            val rawSeats = data["seats"] as? Map<String, Map<String, Any?>> ?: emptyMap()
            val parsedSeats = (0 until MAX_MIC_SEATS).map { idx ->
                val seatData = rawSeats["$idx"] ?: rawSeats[idx.toString()]
                MicSeat.fromMap(idx, seatData)
            }

            val statusStr = (data["status"] as? String) ?: "LIVE"
            val roomStatus = try {
                RoomStatus.valueOf(statusStr.uppercase())
            } catch (_: Exception) {
                RoomStatus.LIVE
            }

            val videoStream = data["videoStream"] as? Map<String, Any?>
            val videoUrl = videoStream?.get("videoUrl") as? String ?: data["videoStreamUrl"] as? String
            val isVideoActive = videoStream?.get("isActive") as? Boolean ?: (data["isVideoLoopActive"] as? Boolean ?: false)

            return AudioRoom(
                roomId = id,
                title = data["title"] as? String ?: "HNWI Luxury Lounge",
                hostUid = data["hostUid"] as? String ?: "",
                hostName = data["hostName"] as? String ?: "Host",
                hostAvatar = data["hostAvatar"] as? String ?: "",
                countryFlag = data["countryFlag"] as? String ?: "🇦🇪",
                countryName = data["countryName"] as? String ?: "UAE",
                status = roomStatus,
                listenerCount = (data["listenerCount"] as? Number)?.toInt()?.coerceAtLeast(0) ?: 0,
                seats = parsedSeats,
                videoStreamUrl = videoUrl,
                isVideoLoopActive = isVideoActive,
                totalCoinsGifted = (data["totalCoinsGifted"] as? Number)?.toLong() ?: 0L,
                roomDpUrl = data["roomDpUrl"] as? String ?: "👑",
                roomBannerUrl = data["roomBannerUrl"] as? String ?: "Midnight Blue",
                announcement = data["announcement"] as? String ?: "Welcome to D.K.K. Live Audio! Please respect everyone and chat decently. 🎙️✨",
                themeId = data["themeId"] as? String ?: "cosmic_earth",
                createdAt = (data["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
            )
        }
    }
}

/**
 * RoomService: Firebase Firestore Manager for 5-Mic Live Audio Rooms,
 * atomic seat assignment, and real-time listener counts.
 */
class RoomService(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val tag = "RoomService"
    private val roomsCollection = firestore.collection("rooms")

    // =========================================================================
    // ROOM LIFECYCLE MANAGEMENT
    // =========================================================================

    /**
     * Creates a new 5-Mic Audio Room with the host seated on Seat 0.
     */
    suspend fun createRoom(
        roomId: String,
        title: String,
        host: RoomParticipant,
        videoStreamUrl: String? = null,
        isVideoLoopActive: Boolean = false
    ): Result<AudioRoom> = withContext(Dispatchers.IO) {
        try {
            val initialSeats = mutableMapOf<String, Any?>()
            // Seat 0: Host by default
            initialSeats["0"] = MicSeat(
                seatIndex = 0,
                occupantUid = host.uid,
                occupantName = host.displayName,
                occupantAvatar = host.avatarUrl,
                isMuted = false,
                joinedAt = System.currentTimeMillis()
            ).toMap()

            // Seats 1 to 4: Vacant
            for (i in 1 until AudioRoom.MAX_MIC_SEATS) {
                initialSeats[i.toString()] = MicSeat(seatIndex = i).toMap()
            }

            val roomData = hashMapOf(
                "roomId" to roomId,
                "title" to title,
                "hostUid" to host.uid,
                "hostName" to host.displayName,
                "hostAvatar" to host.avatarUrl,
                "status" to RoomStatus.LIVE.name,
                "listenerCount" to 1, // Host is also a listener
                "seats" to initialSeats,
                "totalCoinsGifted" to 0L,
                "roomDpUrl" to "👑",
                "roomBannerUrl" to "Midnight Blue",
                "announcement" to "Welcome to D.K.K. Live Audio! Please respect everyone and chat decently. 🎙️✨",
                "themeId" to "cosmic_earth",
                "createdAt" to System.currentTimeMillis(),
                "videoStream" to mapOf(
                    "isActive" to isVideoLoopActive,
                    "videoUrl" to videoStreamUrl
                )
            )

            val roomRef = roomsCollection.document(roomId)
            roomRef.set(roomData, SetOptions.merge()).awaitTask()

            // Also register host into the listeners subcollection
            roomRef.collection("listeners")
                .document(host.uid)
                .set(host.toMap(), SetOptions.merge())
                .awaitTask()

            val createdRoom = AudioRoom.fromDocument(roomId, roomData)
            Log.d(tag, "Room created successfully: $roomId with host: ${host.displayName}")
            Result.success(createdRoom)
        } catch (e: Exception) {
            Log.e(tag, "Failed to create room $roomId", e)
            Result.failure(e)
        }
    }

    /**
     * Closes an audio room and sets status to ENDED.
     */
    suspend fun endRoom(roomId: String, hostUid: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val roomRef = roomsCollection.document(roomId)
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(roomRef)
                if (!snapshot.exists()) {
                    throw IllegalStateException("Room does not exist: $roomId")
                }
                val actualHost = snapshot.getString("hostUid")
                if (actualHost != hostUid) {
                    throw SecurityException("Only the room host can end the room.")
                }

                transaction.update(
                    roomRef,
                    mapOf(
                        "status" to RoomStatus.ENDED.name,
                        "listenerCount" to 0,
                        "videoStream.isActive" to false
                    )
                )
            }.awaitTask()

            Log.d(tag, "Room ended: $roomId by host $hostUid")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(tag, "Failed to end room $roomId", e)
            Result.failure(e)
        }
    }

    // =========================================================================
    // 5-MIC SEAT OPERATIONS (ATOMIC TRANSACTIONS)
    // =========================================================================

    /**
     * Atomically occupies a mic seat (0 to 4).
     * Validates:
     * - Seat index within 0..4
     * - Seat is currently empty
     * - User is not already sitting on another seat
     */
    suspend fun occupySeat(
        roomId: String,
        seatIndex: Int,
        participant: RoomParticipant
    ): Result<Unit> = withContext(Dispatchers.IO) {
        if (seatIndex !in 0 until AudioRoom.MAX_MIC_SEATS) {
            return@withContext Result.failure(IllegalArgumentException("Seat index must be between 0 and 4."))
        }

        try {
            val roomRef = roomsCollection.document(roomId)
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(roomRef)
                if (!snapshot.exists()) throw IllegalStateException("Room $roomId does not exist.")

                val status = snapshot.getString("status")
                if (status != RoomStatus.LIVE.name) throw IllegalStateException("Room is not LIVE.")

                @Suppress("UNCHECKED_CAST")
                val seats = snapshot.get("seats") as? Map<String, Map<String, Any?>> ?: emptyMap()

                // Check if user is already in any seat
                for ((idx, seatData) in seats) {
                    val occupantUid = seatData["occupantUid"] as? String
                    if (occupantUid == participant.uid) {
                        throw IllegalStateException("User ${participant.displayName} is already on seat $idx.")
                    }
                }

                // Check target seat
                val targetSeatData = seats[seatIndex.toString()]
                val currentOccupant = targetSeatData?.get("occupantUid") as? String
                if (!currentOccupant.isNullOrBlank()) {
                    throw IllegalStateException("Seat $seatIndex is already occupied by $currentOccupant.")
                }

                // Assign seat
                val newSeat = MicSeat(
                    seatIndex = seatIndex,
                    occupantUid = participant.uid,
                    occupantName = participant.displayName,
                    occupantAvatar = participant.avatarUrl,
                    isMuted = true, // Default muted when taking mic
                    isTalking = false,
                    joinedAt = System.currentTimeMillis()
                )

                transaction.update(roomRef, "seats.$seatIndex", newSeat.toMap())
            }.awaitTask()

            Log.d(tag, "Seat $seatIndex occupied by ${participant.displayName} in room $roomId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(tag, "Error occupying seat $seatIndex in room $roomId", e)
            Result.failure(e)
        }
    }

    /**
     * Vacates a mic seat atomically.
     */
    suspend fun leaveSeat(
        roomId: String,
        seatIndex: Int,
        uid: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val roomRef = roomsCollection.document(roomId)
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(roomRef)
                if (!snapshot.exists()) return@runTransaction

                val currentOccupant = snapshot.getString("seats.$seatIndex.occupantUid")
                val hostUid = snapshot.getString("hostUid")

                // Either the occupant leaves or the host removes them
                if (currentOccupant == uid || hostUid == uid) {
                    val vacantSeat = MicSeat(seatIndex = seatIndex).toMap()
                    transaction.update(roomRef, "seats.$seatIndex", vacantSeat)
                } else {
                    throw SecurityException("Unauthorized to vacate seat $seatIndex.")
                }
            }.awaitTask()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(tag, "Failed to leave seat $seatIndex in room $roomId", e)
            Result.failure(e)
        }
    }

    /**
     * Mutes or unmutes a specific mic seat.
     */
    suspend fun toggleMute(
        roomId: String,
        seatIndex: Int,
        requesterUid: String,
        isMuted: Boolean
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val roomRef = roomsCollection.document(roomId)
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(roomRef)
                if (!snapshot.exists()) return@runTransaction

                val occupantUid = snapshot.getString("seats.$seatIndex.occupantUid")
                val hostUid = snapshot.getString("hostUid")

                if (occupantUid == requesterUid || hostUid == requesterUid) {
                    transaction.update(roomRef, "seats.$seatIndex.isMuted", isMuted)
                } else {
                    throw SecurityException("Cannot mute another speaker without host permissions.")
                }
            }.awaitTask()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Updates speaking state indicator (audio volume detection / waveform).
     */
    suspend fun setTalkingState(
        roomId: String,
        seatIndex: Int,
        uid: String,
        isTalking: Boolean
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val roomRef = roomsCollection.document(roomId)
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(roomRef)
                if (!snapshot.exists()) return@runTransaction
                val occupantUid = snapshot.getString("seats.$seatIndex.occupantUid")
                if (occupantUid == uid) {
                    transaction.update(roomRef, "seats.$seatIndex.isTalking", isTalking)
                }
            }.awaitTask()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // =========================================================================
    // PARTICIPANT & LISTENER STATE MANAGEMENT
    // =========================================================================

    /**
     * Registers a user as an active room listener and atomically increments listenerCount.
     */
    suspend fun joinRoomAsListener(
        roomId: String,
        participant: RoomParticipant
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val roomRef = roomsCollection.document(roomId)
            val listenerRef = roomRef.collection("listeners").document(participant.uid)

            firestore.runTransaction { transaction ->
                val listenerSnap = transaction.get(listenerRef)
                val isNewListener = !listenerSnap.exists()

                // Save or update participant entry
                transaction.set(listenerRef, participant.toMap(), SetOptions.merge())

                // Increment counter only if not already counted
                if (isNewListener) {
                    transaction.update(roomRef, "listenerCount", FieldValue.increment(1))
                }
            }.awaitTask()

            Log.d(tag, "User ${participant.displayName} joined room $roomId as listener")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(tag, "Failed to join room $roomId as listener", e)
            Result.failure(e)
        }
    }

    /**
     * Handles listener exit:
     * - Removes participant from listeners subcollection
     * - Atomically decrements listenerCount
     * - Vacates any occupied mic seat automatically
     */
    suspend fun leaveRoom(
        roomId: String,
        uid: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val roomRef = roomsCollection.document(roomId)
            val listenerRef = roomRef.collection("listeners").document(uid)

            firestore.runTransaction { transaction ->
                val roomSnap = transaction.get(roomRef)
                if (!roomSnap.exists()) return@runTransaction

                val listenerSnap = transaction.get(listenerRef)
                if (listenerSnap.exists()) {
                    transaction.delete(listenerRef)
                    val currentCount = roomSnap.getLong("listenerCount") ?: 1L
                    if (currentCount > 0) {
                        transaction.update(roomRef, "listenerCount", FieldValue.increment(-1))
                    }
                }

                // Check if user was sitting on any of the 5 seats
                @Suppress("UNCHECKED_CAST")
                val seats = roomSnap.get("seats") as? Map<String, Map<String, Any?>> ?: emptyMap()
                for ((seatIdx, seatData) in seats) {
                    if (seatData["occupantUid"] == uid) {
                        val vacant = MicSeat(seatIndex = seatIdx.toIntOrNull() ?: 0).toMap()
                        transaction.update(roomRef, "seats.$seatIdx", vacant)
                    }
                }
            }.awaitTask()

            Log.d(tag, "User $uid left room $roomId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(tag, "Failed to leave room $roomId", e)
            Result.failure(e)
        }
    }

    // =========================================================================
    // REAL-TIME FLOW OBSERVERS
    // =========================================================================

    /**
     * Observes real-time state of a specific audio room (5 seats, listener count, video status).
     */
    fun observeRoom(roomId: String): Flow<AudioRoom?> = callbackFlow {
        val docRef = roomsCollection.document(roomId)
        val registration: ListenerRegistration = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e(tag, "Error listening to room $roomId", error)
                trySend(null)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val data = snapshot.data
                if (data != null) {
                    val room = AudioRoom.fromDocument(snapshot.id, data)
                    trySend(room)
                } else {
                    trySend(null)
                }
            } else {
                trySend(null)
            }
        }

        awaitClose {
            registration.remove()
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Observes active listeners inside the room.
     */
    fun observeListeners(roomId: String): Flow<List<RoomParticipant>> = callbackFlow {
        val collectionRef = roomsCollection.document(roomId).collection("listeners")
        val registration: ListenerRegistration = collectionRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e(tag, "Error listening to listeners in room $roomId", error)
                trySend(emptyList())
                return@addSnapshotListener
            }

            val listeners = snapshot?.documents?.mapNotNull { doc ->
                val data = doc.data ?: return@mapNotNull null
                RoomParticipant.fromMap(data)
            } ?: emptyList()

            trySend(listeners)
        }

        awaitClose {
            registration.remove()
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Observes all active LIVE audio rooms for exploration / lobby feed.
     */
    fun observeLiveRooms(): Flow<List<AudioRoom>> = callbackFlow {
        val query = roomsCollection.whereEqualTo("status", RoomStatus.LIVE.name)
        val registration: ListenerRegistration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e(tag, "Error listening to live rooms", error)
                trySend(emptyList())
                return@addSnapshotListener
            }

            val rooms = snapshot?.documents?.mapNotNull { doc ->
                val data = doc.data ?: return@mapNotNull null
                AudioRoom.fromDocument(doc.id, data)
            } ?: emptyList()

            trySend(rooms)
        }

        awaitClose {
            registration.remove()
        }
    }.flowOn(Dispatchers.IO)
}

/**
 * Extension function to await Google Play Tasks in Kotlin Coroutines.
 */
suspend fun <T> Task<T>.awaitTask(): T = suspendCancellableCoroutine { cont ->
    addOnSuccessListener { result ->
        if (cont.isActive) cont.resume(result)
    }
    addOnFailureListener { exception ->
        if (cont.isActive) cont.resumeWithException(exception)
    }
    addOnCanceledListener {
        if (cont.isActive) cont.cancel()
    }
}

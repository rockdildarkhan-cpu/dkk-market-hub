package com.example

import com.example.data.firebase.AudioRoom
import com.example.data.firebase.MicSeat
import com.example.data.firebase.RoomParticipant
import com.example.data.firebase.RoomStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RoomServiceTest {

    @Test
    fun test5MicSeatInitializationAndOccupancy() {
        val vacantSeat = MicSeat(seatIndex = 1)
        assertFalse(vacantSeat.isOccupied)
        assertEquals(1, vacantSeat.seatIndex)

        val participant = RoomParticipant(
            uid = "usr_vip_01",
            displayName = "Sheikh Tariq",
            avatarUrl = "https://example.com/avatar.jpg",
            isVip = true,
            country = "AE"
        )

        val occupiedSeat = MicSeat(
            seatIndex = 1,
            occupantUid = participant.uid,
            occupantName = participant.displayName,
            occupantAvatar = participant.avatarUrl,
            isMuted = false,
            joinedAt = 100000L
        )

        assertTrue(occupiedSeat.isOccupied)
        assertEquals("usr_vip_01", occupiedSeat.occupantUid)
        assertEquals("Sheikh Tariq", occupiedSeat.occupantName)
        assertFalse(occupiedSeat.isMuted)
    }

    @Test
    fun testAudioRoomFromDocumentParsing() {
        val rawDoc = mapOf(
            "title" to "HNWI Luxury Investors Lounge",
            "hostUid" to "host_100",
            "hostName" to "Host Mansoor",
            "status" to "LIVE",
            "listenerCount" to 42,
            "seats" to mapOf(
                "0" to mapOf(
                    "seatIndex" to 0,
                    "occupantUid" to "host_100",
                    "occupantName" to "Host Mansoor",
                    "isMuted" to false
                ),
                "1" to mapOf(
                    "seatIndex" to 1,
                    "occupantUid" to "speaker_200",
                    "occupantName" to "Co-Host Dildar",
                    "isMuted" to true
                )
            ),
            "videoStream" to mapOf(
                "isActive" to true,
                "videoUrl" to "https://stream.cloudflare.com/hls/vip_loop.m3u8"
            ),
            "totalCoinsGifted" to 250000L
        )

        val room = AudioRoom.fromDocument("room_luxury_01", rawDoc)

        assertEquals("room_luxury_01", room.roomId)
        assertEquals("HNWI Luxury Investors Lounge", room.title)
        assertEquals(RoomStatus.LIVE, room.status)
        assertEquals(42, room.listenerCount)
        assertEquals(10, room.seats.size) // 10-Mic stage
        assertEquals(250000L, room.totalCoinsGifted)
        assertTrue(room.isVideoLoopActive)
        assertEquals("https://stream.cloudflare.com/hls/vip_loop.m3u8", room.videoStreamUrl)

        // Verify Seat 0 is Host
        val seat0 = room.getSeat(0)
        assertNotNull(seat0)
        assertTrue(seat0!!.isOccupied)
        assertEquals("host_100", seat0.occupantUid)

        // Verify Seat 1 is Co-Host
        val seat1 = room.getSeat(1)
        assertNotNull(seat1)
        assertTrue(seat1!!.isOccupied)
        assertEquals("speaker_200", seat1.occupantUid)

        // Seats 2..9 should be vacant
        val seat2 = room.getSeat(2)
        assertNotNull(seat2)
        assertFalse(seat2!!.isOccupied)

        assertEquals(2, room.occupiedSeatsCount)
        assertEquals(8, room.availableSeatsCount)
    }
}

import 'package:cloud_firestore/cloud_firestore.dart';

/// Status lifecycle for an Audio Room.
enum RoomStatus {
  live,
  paused,
  ended;

  String get value => name.toUpperCase();

  static RoomStatus fromString(String? status) {
    switch (status?.toUpperCase()) {
      case 'PAUSED':
        return RoomStatus.paused;
      case 'ENDED':
        return RoomStatus.ended;
      case 'LIVE':
      default:
        return RoomStatus.live;
    }
  }
}

/// Data model representing an individual seat in the 5-Mic luxury stage.
class MicSeat {
  final int seatIndex;
  final String? occupantUid;
  final String? occupantName;
  final String? occupantAvatar;
  final bool isMuted;
  final bool isTalking;
  final int joinedAt;

  const MicSeat({
    required this.seatIndex,
    this.occupantUid,
    this.occupantName,
    this.occupantAvatar,
    this.isMuted = true,
    this.isTalking = false,
    this.joinedAt = 0,
  });

  bool get isOccupied => occupantUid != null && occupantUid!.trim().isNotEmpty;

  Map<String, dynamic> toMap() => {
        'seatIndex': seatIndex,
        'occupantUid': occupantUid,
        'occupantName': occupantName,
        'occupantAvatar': occupantAvatar,
        'isMuted': isMuted,
        'isTalking': isTalking,
        'joinedAt': joinedAt,
      };

  factory MicSeat.fromMap(int index, Map<String, dynamic>? map) {
    if (map == null) return MicSeat(seatIndex: index);
    return MicSeat(
      seatIndex: (map['seatIndex'] as num?)?.toInt() ?? index,
      occupantUid: map['occupantUid'] as String?,
      occupantName: map['occupantName'] as String?,
      occupantAvatar: map['occupantAvatar'] as String?,
      isMuted: map['isMuted'] as bool? ?? true,
      isTalking: map['isTalking'] as bool? ?? false,
      joinedAt: (map['joinedAt'] as num?)?.toInt() ?? 0,
    );
  }

  MicSeat copyWith({
    int? seatIndex,
    String? occupantUid,
    String? occupantName,
    String? occupantAvatar,
    bool? isMuted,
    bool? isTalking,
    int? joinedAt,
  }) {
    return MicSeat(
      seatIndex: seatIndex ?? this.seatIndex,
      occupantUid: occupantUid ?? this.occupantUid,
      occupantName: occupantName ?? this.occupantName,
      occupantAvatar: occupantAvatar ?? this.occupantAvatar,
      isMuted: isMuted ?? this.isMuted,
      isTalking: isTalking ?? this.isTalking,
      joinedAt: joinedAt ?? this.joinedAt,
    );
  }
}

/// Data model representing a participant / listener in the room.
class RoomParticipant {
  final String uid;
  final String displayName;
  final String avatarUrl;
  final bool isVip;
  final String country;
  final int joinedAt;

  const RoomParticipant({
    required this.uid,
    required this.displayName,
    this.avatarUrl = '',
    this.isVip = false,
    this.country = 'AE',
    required this.joinedAt,
  });

  Map<String, dynamic> toMap() => {
        'uid': uid,
        'displayName': displayName,
        'avatarUrl': avatarUrl,
        'isVip': isVip,
        'country': country,
        'joinedAt': joinedAt,
      };

  factory RoomParticipant.fromMap(Map<String, dynamic> map) {
    return RoomParticipant(
      uid: map['uid'] as String? ?? '',
      displayName: map['displayName'] as String? ?? 'Guest',
      avatarUrl: map['avatarUrl'] as String? ?? '',
      isVip: map['isVip'] as bool? ?? false,
      country: map['country'] as String? ?? 'AE',
      joinedAt: (map['joinedAt'] as num?)?.toInt() ?? DateTime.now().millisecondsSinceEpoch,
    );
  }
}

/// Complete state representation of a 5-Mic Social Audio Room.
class AudioRoom {
  static const int maxMicSeats = 5;

  final String roomId;
  final String title;
  final String hostUid;
  final String hostName;
  final String hostAvatar;
  final RoomStatus status;
  final int listenerCount;
  final List<MicSeat> seats;
  final String? videoStreamUrl;
  final bool isVideoLoopActive;
  final int totalCoinsGifted;
  final int createdAt;

  const AudioRoom({
    required this.roomId,
    required this.title,
    required this.hostUid,
    required this.hostName,
    this.hostAvatar = '',
    this.status = RoomStatus.live,
    this.listenerCount = 0,
    required this.seats,
    this.videoStreamUrl,
    this.isVideoLoopActive = false,
    this.totalCoinsGifted = 0,
    required this.createdAt,
  });

  int get occupiedSeatsCount => seats.where((s) => s.isOccupied).length;
  int get availableSeatsCount => seats.where((s) => !s.isOccupied).length;

  MicSeat? getSeat(int index) {
    if (index < 0 || index >= seats.length) return null;
    return seats[index];
  }

  factory AudioRoom.fromFirestore(DocumentSnapshot<Map<String, dynamic>> doc) {
    final data = doc.data() ?? {};
    return AudioRoom.fromMap(doc.id, data);
  }

  factory AudioRoom.fromMap(String id, Map<String, dynamic> data) {
    final rawSeats = (data['seats'] as Map<String, dynamic>?) ?? {};
    final parsedSeats = List<MicSeat>.generate(maxMicSeats, (idx) {
      final seatData = rawSeats['$idx'] as Map<String, dynamic>?;
      return MicSeat.fromMap(idx, seatData);
    });

    final videoStream = data['videoStream'] as Map<String, dynamic>?;
    final videoUrl = videoStream?['videoUrl'] as String? ?? data['videoStreamUrl'] as String?;
    final isVideoActive = videoStream?['isActive'] as bool? ?? data['isVideoLoopActive'] as bool? ?? false;

    return AudioRoom(
      roomId: id,
      title: data['title'] as String? ?? 'HNWI Luxury Lounge',
      hostUid: data['hostUid'] as String? ?? '',
      hostName: data['hostName'] as String? ?? 'Host',
      hostAvatar: data['hostAvatar'] as String? ?? '',
      status: RoomStatus.fromString(data['status'] as String?),
      listenerCount: (data['listenerCount'] as num?)?.toInt() ?? 0,
      seats: parsedSeats,
      videoStreamUrl: videoUrl,
      isVideoLoopActive: isVideoActive,
      totalCoinsGifted: (data['totalCoinsGifted'] as num?)?.toInt() ?? 0,
      createdAt: (data['createdAt'] as num?)?.toInt() ?? DateTime.now().millisecondsSinceEpoch,
    );
  }

  Map<String, dynamic> toMap() {
    final Map<String, dynamic> seatsMap = {};
    for (final seat in seats) {
      seatsMap['${seat.seatIndex}'] = seat.toMap();
    }

    return {
      'roomId': roomId,
      'title': title,
      'hostUid': hostUid,
      'hostName': hostName,
      'hostAvatar': hostAvatar,
      'status': status.value,
      'listenerCount': listenerCount,
      'seats': seatsMap,
      'totalCoinsGifted': totalCoinsGifted,
      'createdAt': createdAt,
      'videoStream': {
        'isActive': isVideoLoopActive,
        'videoUrl': videoStreamUrl,
      },
    };
  }
}

/// Production Firestore Service for Room States and 5-Mic Audio Architecture.
///
/// Implements complete CRUD operations for audio rooms, atomic seat transactions
/// (`joinSeat`, `leaveSeat`, `updateMuteStatus`), real-time listener counters,
/// and live streaming observers.
class RoomService {
  final FirebaseFirestore _firestore;

  RoomService({FirebaseFirestore? firestore})
      : _firestore = firestore ?? FirebaseFirestore.instance;

  CollectionReference<Map<String, dynamic>> get _roomsCollection =>
      _firestore.collection('rooms');

  // ===========================================================================
  // 1. CREATE OPERATIONS
  // ===========================================================================

  /// Creates a new 5-Mic Audio Room.
  ///
  /// Initial state:
  /// - Seat 0: Automatically reserved for the room host.
  /// - Seats 1 to 4: Vacant and available for listeners to request/join.
  /// - Host is automatically added to the room's listeners subcollection.
  Future<AudioRoom> createRoom({
    required String roomId,
    required String title,
    required RoomParticipant host,
    String? videoStreamUrl,
    bool isVideoLoopActive = false,
  }) async {
    final now = DateTime.now().millisecondsSinceEpoch;

    // Build default 5 seats
    final Map<String, dynamic> initialSeats = {
      '0': MicSeat(
        seatIndex: 0,
        occupantUid: host.uid,
        occupantName: host.displayName,
        occupantAvatar: host.avatarUrl,
        isMuted: false,
        isTalking: false,
        joinedAt: now,
      ).toMap(),
    };

    for (int i = 1; i < AudioRoom.maxMicSeats; i++) {
      initialSeats['$i'] = MicSeat(seatIndex: i).toMap();
    }

    final roomData = <String, dynamic>{
      'roomId': roomId,
      'title': title,
      'hostUid': host.uid,
      'hostName': host.displayName,
      'hostAvatar': host.avatarUrl,
      'status': RoomStatus.live.value,
      'listenerCount': 1,
      'seats': initialSeats,
      'totalCoinsGifted': 0,
      'createdAt': now,
      'videoStream': {
        'isActive': isVideoLoopActive,
        'videoUrl': videoStreamUrl,
      },
    };

    final roomDocRef = _roomsCollection.doc(roomId);

    await roomDocRef.set(roomData, SetOptions(merge: true));

    // Also register host into the listeners subcollection
    await roomDocRef.collection('listeners').doc(host.uid).set(
          host.toMap(),
          SetOptions(merge: true),
        );

    return AudioRoom.fromMap(roomId, roomData);
  }

  // ===========================================================================
  // 2. READ OPERATIONS
  // ===========================================================================

  /// Fetches a single room snapshot by ID.
  Future<AudioRoom?> getRoom(String roomId) async {
    final snapshot = await _roomsCollection.doc(roomId).get();
    if (!snapshot.exists || snapshot.data() == null) {
      return null;
    }
    return AudioRoom.fromFirestore(snapshot);
  }

  /// Observes real-time state updates for a specific room.
  Stream<AudioRoom?> streamRoom(String roomId) {
    return _roomsCollection.doc(roomId).snapshots().map((snapshot) {
      if (!snapshot.exists || snapshot.data() == null) {
        return null;
      }
      return AudioRoom.fromFirestore(snapshot);
    });
  }

  /// Observes all currently LIVE rooms (useful for discovery feed).
  Stream<List<AudioRoom>> streamLiveRooms() {
    return _roomsCollection
        .where('status', isEqualTo: RoomStatus.live.value)
        .snapshots()
        .map((snapshot) {
      return snapshot.docs
          .map((doc) => AudioRoom.fromFirestore(doc))
          .toList();
    });
  }

  /// Observes the list of active listeners inside the room.
  Stream<List<RoomParticipant>> streamListeners(String roomId) {
    return _roomsCollection
        .doc(roomId)
        .collection('listeners')
        .snapshots()
        .map((snapshot) {
      return snapshot.docs
          .map((doc) => RoomParticipant.fromMap(doc.data()))
          .toList();
    });
  }

  // ===========================================================================
  // 3. 5-MIC AUDIO ARCHITECTURE (JOIN, LEAVE, TOGGLE MUTE TRANSACTIONS)
  // ===========================================================================

  /// Joins a specific mic seat (0 to 4) atomically.
  /// 
  /// Signature: joinSeat(roomId, seatIndex, userId, userName, [avatarUrl])
  ///
  /// Validations executed within transaction:
  /// 1. `seatIndex` is within range [0, 4].
  /// 2. Room exists and is in `LIVE` status.
  /// 3. User is not already seated on any other mic seat.
  /// 4. The requested seat is currently vacant.
  ///
  /// Upon success, user joins with `isMuted = true` by default.
  Future<void> joinSeat(
    String roomId,
    int seatIndex,
    String userId,
    String userName, {
    String avatarUrl = '',
  }) async {
    if (seatIndex < 0 || seatIndex >= AudioRoom.maxMicSeats) {
      throw ArgumentError(
        'Seat index $seatIndex is invalid. Must be between 0 and ${AudioRoom.maxMicSeats - 1}.',
      );
    }

    final roomDocRef = _roomsCollection.doc(roomId);

    await _firestore.runTransaction((transaction) async {
      final snapshot = await transaction.get(roomDocRef);
      if (!snapshot.exists) {
        throw StateError('Room $roomId does not exist.');
      }

      final data = snapshot.data()!;
      final status = data['status'] as String?;
      if (status != RoomStatus.live.value) {
        throw StateError('Room is not LIVE.');
      }

      final rawSeats = (data['seats'] as Map<String, dynamic>?) ?? {};

      // 1. Check if participant is already seated elsewhere
      for (final entry in rawSeats.entries) {
        final seatData = entry.value as Map<String, dynamic>?;
        final occupantUid = seatData?['occupantUid'] as String?;
        if (occupantUid == userId) {
          throw StateError(
            'User $userName ($userId) is already occupying seat ${entry.key}.',
          );
        }
      }

      // 2. Check if the target seat is already occupied
      final targetSeatData = rawSeats['$seatIndex'] as Map<String, dynamic>?;
      final currentOccupant = targetSeatData?['occupantUid'] as String?;
      if (currentOccupant != null && currentOccupant.trim().isNotEmpty) {
        throw StateError('Seat $seatIndex is already occupied.');
      }

      // 3. Assign the participant to the mic seat
      final newSeat = MicSeat(
        seatIndex: seatIndex,
        occupantUid: userId,
        occupantName: userName,
        occupantAvatar: avatarUrl,
        isMuted: true, // Default to muted when joining stage
        isTalking: false,
        joinedAt: DateTime.now().millisecondsSinceEpoch,
      );

      transaction.update(roomDocRef, {
        'seats.$seatIndex': newSeat.toMap(),
      });
    });
  }

  /// Vacates a mic seat atomically.
  ///
  /// Signature: leaveSeat(roomId, seatIndex, [requesterUid])
  ///
  /// If [requesterUid] is provided, validates that either the occupant is willingly
  /// leaving or the room host is removing/kicking them.
  Future<void> leaveSeat(
    String roomId,
    int seatIndex, [
    String? requesterUid,
  ]) async {
    if (seatIndex < 0 || seatIndex >= AudioRoom.maxMicSeats) {
      throw ArgumentError('Invalid seatIndex: $seatIndex');
    }

    final roomDocRef = _roomsCollection.doc(roomId);

    await _firestore.runTransaction((transaction) async {
      final snapshot = await transaction.get(roomDocRef);
      if (!snapshot.exists) return;

      final data = snapshot.data()!;
      final hostUid = data['hostUid'] as String?;
      final rawSeats = (data['seats'] as Map<String, dynamic>?) ?? {};
      final seatData = rawSeats['$seatIndex'] as Map<String, dynamic>?;
      final occupantUid = seatData?['occupantUid'] as String?;

      if (occupantUid == null || occupantUid.trim().isEmpty) {
        // Seat already empty
        return;
      }

      // Authorization check (if requester specified)
      if (requesterUid != null && requesterUid.isNotEmpty) {
        if (occupantUid != requesterUid && hostUid != requesterUid) {
          throw StateError('Unauthorized to vacate seat $seatIndex.');
        }
      }

      final vacantSeat = MicSeat(seatIndex: seatIndex).toMap();
      transaction.update(roomDocRef, {
        'seats.$seatIndex': vacantSeat,
      });
    });
  }

  /// Toggles or updates the mute status for a specific mic seat.
  ///
  /// Signature: toggleMute(roomId, seatIndex, isMuted, [requesterUid])
  ///
  /// Permission rules:
  /// - The speaker can mute/unmute themselves.
  /// - The room host can mute any speaker on the stage.
  Future<void> toggleMute(
    String roomId,
    int seatIndex,
    bool isMuted, [
    String? requesterUid,
  ]) async {
    if (seatIndex < 0 || seatIndex >= AudioRoom.maxMicSeats) {
      throw ArgumentError('Invalid seatIndex: $seatIndex');
    }

    final roomDocRef = _roomsCollection.doc(roomId);

    await _firestore.runTransaction((transaction) async {
      final snapshot = await transaction.get(roomDocRef);
      if (!snapshot.exists) return;

      final data = snapshot.data()!;
      final hostUid = data['hostUid'] as String?;
      final rawSeats = (data['seats'] as Map<String, dynamic>?) ?? {};
      final seatData = rawSeats['$seatIndex'] as Map<String, dynamic>?;
      final occupantUid = seatData?['occupantUid'] as String?;

      if (occupantUid == null || occupantUid.trim().isEmpty) {
        throw StateError('Seat $seatIndex has no occupant to mute/unmute.');
      }

      if (requesterUid != null && requesterUid.isNotEmpty) {
        if (occupantUid != requesterUid && hostUid != requesterUid) {
          throw StateError('Unauthorized to update mute status on seat $seatIndex.');
        }
      }

      transaction.update(roomDocRef, {
        'seats.$seatIndex.isMuted': isMuted,
        if (isMuted) 'seats.$seatIndex.isTalking': false,
      });
    });
  }

  /// Alias for toggleMute to support updateMuteStatus naming convention.
  Future<void> updateMuteStatus({
    required String roomId,
    required int seatIndex,
    required bool isMuted,
    String? requesterUid,
  }) async {
    return toggleMute(roomId, seatIndex, isMuted, requesterUid);
  }

  /// Updates real-time speaking/waveform indicator for a seat occupant.
  Future<void> updateTalkingStatus({
    required String roomId,
    required int seatIndex,
    required String uid,
    required bool isTalking,
  }) async {
    final roomDocRef = _roomsCollection.doc(roomId);

    await _firestore.runTransaction((transaction) async {
      final snapshot = await transaction.get(roomDocRef);
      if (!snapshot.exists) return;

      final rawSeats = (snapshot.data()?['seats'] as Map<String, dynamic>?) ?? {};
      final seatData = rawSeats['$seatIndex'] as Map<String, dynamic>?;
      final occupantUid = seatData?['occupantUid'] as String?;

      if (occupantUid == uid) {
        transaction.update(roomDocRef, {
          'seats.$seatIndex.isTalking': isTalking,
        });
      }
    });
  }

  // ===========================================================================
  // 4. PARTICIPANT & LISTENER LIFECYCLE
  // ===========================================================================

  /// Registers user as a listener and atomically increments `listenerCount`.
  Future<void> joinRoomAsListener({
    required String roomId,
    required RoomParticipant participant,
  }) async {
    final roomDocRef = _roomsCollection.doc(roomId);
    final listenerDocRef = roomDocRef.collection('listeners').doc(participant.uid);

    await _firestore.runTransaction((transaction) async {
      final listenerSnap = await transaction.get(listenerDocRef);
      final isNew = !listenerSnap.exists;

      transaction.set(listenerDocRef, participant.toMap(), SetOptions(merge: true));

      if (isNew) {
        transaction.update(roomDocRef, {
          'listenerCount': FieldValue.increment(1),
        });
      }
    });
  }

  /// Removes listener, decrements `listenerCount`, and vacates their mic seat if seated.
  Future<void> leaveRoom({
    required String roomId,
    required String uid,
  }) async {
    final roomDocRef = _roomsCollection.doc(roomId);
    final listenerDocRef = roomDocRef.collection('listeners').doc(uid);

    await _firestore.runTransaction((transaction) async {
      final roomSnap = await transaction.get(roomDocRef);
      if (!roomSnap.exists) return;

      final listenerSnap = await transaction.get(listenerDocRef);
      final Map<String, dynamic> updates = {};

      if (listenerSnap.exists) {
        transaction.delete(listenerDocRef);
        final currentCount = (roomSnap.data()?['listenerCount'] as num?)?.toInt() ?? 1;
        if (currentCount > 0) {
          updates['listenerCount'] = FieldValue.increment(-1);
        }
      }

      // Check if user is occupying any seat and vacate it
      final rawSeats = (roomSnap.data()?['seats'] as Map<String, dynamic>?) ?? {};
      for (final entry in rawSeats.entries) {
        final seatData = entry.value as Map<String, dynamic>?;
        if (seatData?['occupantUid'] == uid) {
          final seatIdx = int.tryParse(entry.key) ?? 0;
          updates['seats.$seatIdx'] = MicSeat(seatIndex: seatIdx).toMap();
        }
      }

      if (updates.isNotEmpty) {
        transaction.update(roomDocRef, updates);
      }
    });
  }

  // ===========================================================================
  // 5. UPDATE & DELETE OPERATIONS
  // ===========================================================================

  /// Updates room metadata such as title.
  Future<void> updateRoomTitle({
    required String roomId,
    required String hostUid,
    required String newTitle,
  }) async {
    final roomDocRef = _roomsCollection.doc(roomId);
    final snapshot = await roomDocRef.get();
    if (!snapshot.exists) throw StateError('Room does not exist.');
    if (snapshot.data()?['hostUid'] != hostUid) {
      throw StateError('Only the host can modify the room title.');
    }

    await roomDocRef.update({'title': newTitle});
  }

  /// Updates video stream status or URL (for VIP video looper).
  Future<void> updateVideoStream({
    required String roomId,
    required String hostUid,
    required String? videoUrl,
    required bool isActive,
  }) async {
    final roomDocRef = _roomsCollection.doc(roomId);
    final snapshot = await roomDocRef.get();
    if (!snapshot.exists) throw StateError('Room does not exist.');
    if (snapshot.data()?['hostUid'] != hostUid) {
      throw StateError('Only the host can update the video stream.');
    }

    await roomDocRef.update({
      'videoStream': {
        'isActive': isActive,
        'videoUrl': videoUrl,
      },
    });
  }

  /// Records luxury coin gifts sent to the room/host.
  Future<void> recordGift({
    required String roomId,
    required int coinAmount,
  }) async {
    await _roomsCollection.doc(roomId).update({
      'totalCoinsGifted': FieldValue.increment(coinAmount),
    });
  }

  /// Ends the audio room and marks status as `ENDED`.
  Future<void> endRoom({
    required String roomId,
    required String hostUid,
  }) async {
    final roomDocRef = _roomsCollection.doc(roomId);

    await _firestore.runTransaction((transaction) async {
      final snapshot = await transaction.get(roomDocRef);
      if (!snapshot.exists) throw StateError('Room does not exist.');

      final actualHost = snapshot.data()?['hostUid'] as String?;
      if (actualHost != hostUid) {
        throw StateError('Only the room host can end the room.');
      }

      transaction.update(roomDocRef, {
        'status': RoomStatus.ended.value,
        'listenerCount': 0,
        'videoStream.isActive': false,
      });
    });
  }

  /// Deletes a room document permanently.
  Future<void> deleteRoom({
    required String roomId,
    required String hostUid,
  }) async {
    final roomDocRef = _roomsCollection.doc(roomId);
    final snapshot = await roomDocRef.get();
    if (!snapshot.exists) return;

    if (snapshot.data()?['hostUid'] != hostUid) {
      throw StateError('Only the host can delete the room.');
    }

    await roomDocRef.delete();
  }
}

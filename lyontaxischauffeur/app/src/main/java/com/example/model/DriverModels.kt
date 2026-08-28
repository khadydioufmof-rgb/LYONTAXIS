package com.example.model

enum class DriverStatus {
    OFFLINE,
    ONLINE_WAITING,
    DISPATCH_OFFER,
    EN_ROUTE_PICKUP,
    ARRIVED_PICKUP,
    IN_TRIP,
    TRIP_COMPLETED
}

enum class VehicleCategory(val displayName: String, val badgeColorHex: Long) {
    ABER_X("AberX", 0xFF00D09C),
    ABER_CONFORT("Aber Confort", 0xFF3B82F6),
    ABER_BERLINE("Aber Berline", 0xFF8B5CF6),
    ABER_GREEN("Aber Green", 0xFF10B981),
    ABER_VAN("Aber Van", 0xFFF59E0B)
}

enum class RideQuickFilter(val label: String, val iconName: String) {
    ALL("Toutes", "all"),
    SHORT_DISTANCE("Courte distance (< 6 km)", "short"),
    AIRPORT("Vers aéroports / Gares", "airport"),
    SURGE_ONLY("Majorations (Surge)", "surge"),
    PREMIUM_ONLY("Berline / Van", "premium")
}

data class DriverProfile(
    val id: String = "drv_7849",
    val name: String = "Karim Benmansour",
    val phone: String = "+33 6 78 90 12 34",
    val email: String = "karim.benmansour@aber-driver.fr",
    val rating: Double = 4.95,
    val totalRides: Int = 1842,
    val acceptanceRate: Int = 98,
    val cancellationRate: Int = 1,
    val rankBadge: String = "Chauffeur Pro Élite",
    val vehicleModel: String = "Mercedes-Benz Classe E 220d",
    val vehiclePlate: String = "GC-742-LK",
    val vehicleColor: String = "Noir Obsidienne",
    val vehicleYear: Int = 2024,
    val vehicleInspectionStatus: String = "Conforme & Validé",
    val vehicleLastInspectionDate: String = "12/06/2026",
    val vehicleNextInspectionDate: String = "12/06/2027",
    val vehicleMileageKm: Int = 64250,
    val vehicleTechnicalNotes: String = "Pneus neufs Michelin CrossClimate 2, révision des 60k km effectuée.",
    val vtcLicenseNumber: String = "EVTC-075-23-0091",
    val insuranceExpiry: String = "15/11/2026",
    val balance: Double = 342.80,
    val todayEarnings: Double = 184.50,
    val weeklyEarnings: Double = 1289.50,
    val weeklyGoalEarnings: Double = 1500.00,
    val todayTripsCount: Int = 8,
    val todayOnlineHours: Double = 5.4,
    val isAutoAcceptEnabled: Boolean = false,
    val isCashAccepted: Boolean = true,
    val isHomeDestinationActive: Boolean = false,
    val homeAddress: String = "Boulogne-Billancourt",
    val activeQuickFilter: RideQuickFilter = RideQuickFilter.ALL
)

data class NavigationStep(
    val instruction: String,
    val subInstruction: String,
    val distanceRemainingMeters: Int,
    val iconType: String // "straight", "turn_right", "turn_left", "roundabout", "u_turn", "destination"
)

data class RideRequest(
    val id: String,
    val passengerName: String,
    val passengerRating: Double,
    val passengerTrips: Int,
    val passengerPhone: String,
    val category: VehicleCategory,
    val pickupAddress: String,
    val pickupDistanceKm: Double,
    val pickupDurationMin: Int,
    val dropoffAddress: String,
    val dropoffDistanceKm: Double,
    val dropoffDurationMin: Int,
    val estimatedFare: Double,
    val surgeMultiplier: Double = 1.0,
    val tipAmount: Double = 0.0,
    val paymentMethod: String = "LyonTaxis Pay (CB)",
    val specialNote: String = "",
    val navigationSteps: List<NavigationStep> = emptyList(),
    val pickupLat: Float = 0.35f,
    val pickupLng: Float = 0.45f,
    val dropoffLat: Float = 0.70f,
    val dropoffLng: Float = 0.65f
)

data class EarningsRecord(
    val id: String,
    val timestamp: String,
    val passengerName: String,
    val pickup: String,
    val dropoff: String,
    val baseFare: Double,
    val surgeBonus: Double,
    val tip: Double,
    val platformFee: Double,
    val netEarnings: Double,
    val category: VehicleCategory,
    val distanceKm: Double,
    val durationMin: Int,
    val paymentMethod: String
)

data class SurgeHotspot(
    val id: String,
    val name: String,
    val district: String,
    val multiplier: Double,
    val extraBonusEur: Double,
    val demandLevel: String, // "Très forte", "Forte", "Modérée"
    val estimatedWaitSec: Int,
    val relX: Float,
    val relY: Float
)

data class ChatMessage(
    val id: String,
    val senderIsDriver: Boolean,
    val text: String,
    val timeFormatted: String
)

data class PassengerConversation(
    val id: String,
    val passengerName: String,
    val passengerRating: Double = 4.95,
    val passengerPhone: String = "+33 6 12 34 56 78",
    val rideStatus: String = "Course en cours",
    val pickupAddress: String = "84 Avenue des Champs-Élysées",
    val dropoffAddress: String = "14 Place Vendôme",
    val isCurrentActiveTrip: Boolean = false,
    val unreadCount: Int = 0,
    val avatarInitials: String = "EM",
    val avatarColorHex: Long = 0xFF00D09C,
    val messages: List<ChatMessage> = emptyList()
) {
    val lastMessage: String
        get() = messages.lastOrNull()?.text ?: "Nouvelle conversation"

    val lastMessageTime: String
        get() = messages.lastOrNull()?.timeFormatted ?: ""
}

data class SupportFaq(
    val id: String,
    val question: String,
    val answer: String,
    val category: String
)

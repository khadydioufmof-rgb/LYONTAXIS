package com.example.model

enum class VehicleCategory(
  val displayName: String,
  val basePrice: Double,
  val timeMinutes: Int,
  val distanceDesc: String,
  val capacity: Int,
  val description: String
) {
  JUST_GO("Standard Lyon", 15.00, 2, "À proximité", 4, "Course quotidienne rapide et économique"),
  LIMOUSINE("Berline Prestige", 45.00, 5, "0.2 km", 4, "Expérience luxe & grand confort"),
  LUXURY("Taxi Confort", 30.00, 3, "0.4 km", 4, "Berlines haut de gamme spacieuses"),
  ELECTRIC_CAR("Taxi Électrique", 18.00, 2, "0.45 km", 4, "100% écologique zéro émission"),
  BIKE("Moto Taxi", 12.00, 3, "0.48 km", 1, "Rapide et fluide dans le trafic lyonnais"),
  TAXI_4_SEAT("Taxi 4 places", 20.00, 4, "0.5 km", 4, "Taxi officiel de Lyon avec compteur"),
  TAXI_7_SEAT("Van 7 places", 35.00, 4, "0.67 km", 7, "Idéal pour groupes et aéroport")
}

data class Driver(
  val id: String = "driver_1",
  val name: String = "Jean-Marc Dupont",
  val rating: Double = 4.9,
  val totalTrips: Int = 1420,
  val carModel: String = "Peugeot 508 Hybrid",
  val licensePlate: String = "EK-892-TL",
  val phoneNumber: String = "+33 6 12 34 56 78",
  val carColor: String = "Gris Métallisé",
  val avatarSeed: String = "JeanMarc"
)

data class LocationPoint(
  val id: String,
  val title: String,
  val address: String,
  val distanceKm: Double,
  val isFavorite: Boolean = false,
  val latitude: Double = 45.7578,
  val longitude: Double = 4.8320
)

enum class BookingStatus {
  PENDING,
  CONFIRMED,
  DRIVER_ARRIVING,
  IN_PROGRESS,
  COMPLETED,
  CANCELLED
}

data class RidePreferences(
  val babySeat: Boolean = false,
  val pmrAccess: Boolean = false,
  val petFriendly: Boolean = false,
  val extraLuggage: Boolean = false,
  val silentRide: Boolean = false,
  val climatePreference: String = "Tempéré (21°C)"
)

data class ScheduledRideItem(
  val id: String,
  val pickupLocation: LocationPoint,
  val dropoffLocation: LocationPoint,
  val intermediateStops: List<LocationPoint> = emptyList(),
  val vehicle: VehicleCategory,
  val scheduledDate: String, // e.g. "Demain (28 Août)"
  val scheduledTime: String, // e.g. "06:30"
  val estimatedFare: Double,
  val paymentMethodTitle: String = "Espèces LyonTaxis",
  val preferences: RidePreferences = RidePreferences(),
  val specialInstructions: String = "",
  val status: String = "Confirmée" // "Confirmée", "Rappel activé", "Annulée"
)

data class RideBooking(
  val id: String,
  val pickupLocation: LocationPoint,
  val dropoffLocation: LocationPoint,
  val intermediateStops: List<LocationPoint> = emptyList(),
  val vehicle: VehicleCategory,
  val driver: Driver,
  val fare: Double,
  val baseFare: Double = 3.50,
  val distanceFare: Double = 8.50,
  val timeFare: Double = 2.00,
  val stopFee: Double = 0.0,
  val serviceFee: Double = 1.00,
  val discount: Double = 0.0,
  val tip: Double = 0.0,
  val status: BookingStatus = BookingStatus.PENDING,
  val formattedTime: String = "Aujourd'hui, 17:03",
  val distanceKm: Double = 4.8,
  val durationMin: Int = 14,
  val paymentMethodTitle: String = "Espèces LyonTaxis",
  val preferences: RidePreferences = RidePreferences(),
  val isScheduled: Boolean = false,
  val scheduledDateTimeStr: String? = null
)

enum class MessageStatus {
  SENDING,
  SENT,
  DELIVERED,
  READ
}

enum class MessageType {
  TEXT,
  AUDIO,
  LOCATION,
  SYSTEM_STATUS
}

data class ChatMessage(
  val id: String,
  val isFromUser: Boolean,
  val text: String,
  val timestamp: String,
  val status: MessageStatus = MessageStatus.READ,
  val type: MessageType = MessageType.TEXT,
  val audioDurationSec: Int = 0,
  val locationTitle: String? = null
)

enum class PaymentType {
  CASH,
  VISA,
  PAYPAL,
  MASTERCARD
}

data class PaymentMethodItem(
  val id: String,
  val type: PaymentType,
  val title: String,
  val subtitle: String,
  val isDefault: Boolean = false,
  val isSelected: Boolean = false
)

enum class NotificationType {
  SYSTEM_CONFIRM,
  SYSTEM_CANCEL,
  SYSTEM_WALLET,
  PROMOTION
}

data class NotificationItem(
  val id: String,
  val type: NotificationType,
  val title: String,
  val description: String,
  val timeAgo: String,
  val isRead: Boolean = false
)

data class TripHistoryItem(
  val id: String,
  val pickupTitle: String,
  val dropoffTitle: String,
  val fare: Double,
  val date: String,
  val status: String, // "Confirmé", "Terminé", "Annulé"
  val distanceKm: Double = 4.8,
  val durationMin: Int = 14,
  val driverName: String = "Jean-Marc Dupont",
  val vehicleName: String = "Standard Lyon",
  val rating: Float = 5.0f,
  val tip: Double = 0.0,
  val paymentMethod: String = "Espèces LyonTaxis"
)

data class ContactFriend(
  val id: String,
  val name: String,
  val phone: String,
  val isInvited: Boolean = false
)

data class AuthState(
  val phoneOrEmail: String = "",
  val isAuthenticated: Boolean = false
)

data class UserProfile(
  val name: String = "Thomas Martin",
  val email: String = "thomas.martin@lyon-taxis.fr",
  val phoneNumber: String = "+33 6 45 88 12 90",
  val gender: String = "Homme",
  val birthday: String = "16 Avril 1990",
  val emergencyContact: String = "Sophie Martin (+33 6 98 76 54 32)",
  val homeAddress: String = "14 Rue de la République, 69002 Lyon",
  val memberLevel: String = "Membre Or",
  val cashBalance: Double = 150.00,
  val integralPoints: Int = 4500,
  val couponsCount: Int = 3,
  val referralCode: String = "LYON50",
  val avatarSeed: String = "Thomas"
)

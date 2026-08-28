package com.example.data

import com.example.model.*
import com.example.data.supabase.SupabaseAuthClient
import com.example.data.supabase.SupabaseSessionStore
import com.example.data.supabase.SupabaseDataClient
import com.example.data.supabase.SupabaseLocationDto
import com.example.data.supabase.SupabaseProfileDto
import com.example.data.supabase.SupabaseRideInsert
import com.example.data.supabase.SupabaseRideDto
import com.example.data.supabase.SupabaseNotificationDto
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.*

class AberRepository {

  companion object {
    val instance = AberRepository()
  }

  private val repositoryScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
  private val supabaseAuthClient = SupabaseAuthClient()
  private val supabaseDataClient = SupabaseDataClient()
  private var sessionStore: SupabaseSessionStore? = null
  private val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())

  private val _authState = MutableStateFlow(AuthState())
  val authState: StateFlow<AuthState> = _authState.asStateFlow()

  private val _userProfile = MutableStateFlow(UserProfile())
  val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

  private val _pickupLocation = MutableStateFlow(SampleData.defaultPickup)
  val pickupLocation: StateFlow<LocationPoint> = _pickupLocation.asStateFlow()

  private val _dropoffLocation = MutableStateFlow(SampleData.defaultDropoff)
  val dropoffLocation: StateFlow<LocationPoint> = _dropoffLocation.asStateFlow()

  private val _intermediateStops = MutableStateFlow<List<LocationPoint>>(emptyList())
  val intermediateStops: StateFlow<List<LocationPoint>> = _intermediateStops.asStateFlow()

  private val _popularLocations = MutableStateFlow(SampleData.popularLocations)
  val popularLocations: StateFlow<List<LocationPoint>> = _popularLocations.asStateFlow()

  private val _selectedVehicle = MutableStateFlow(VehicleCategory.LIMOUSINE)
  val selectedVehicle: StateFlow<VehicleCategory> = _selectedVehicle.asStateFlow()

  private val _activeBooking = MutableStateFlow<RideBooking?>(null)
  val activeBooking: StateFlow<RideBooking?> = _activeBooking.asStateFlow()

  private val _lastCompletedBooking = MutableStateFlow<RideBooking?>(null)
  val lastCompletedBooking: StateFlow<RideBooking?> = _lastCompletedBooking.asStateFlow()

  private val _appliedPromoCode = MutableStateFlow<String?>(null)
  val appliedPromoCode: StateFlow<String?> = _appliedPromoCode.asStateFlow()

  private val _discountAmount = MutableStateFlow(0.0)
  val discountAmount: StateFlow<Double> = _discountAmount.asStateFlow()

  private val _chatMessages = MutableStateFlow(SampleData.initialChatMessages)
  val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

  private val _isDriverTyping = MutableStateFlow(false)
  val isDriverTyping: StateFlow<Boolean> = _isDriverTyping.asStateFlow()

  private val _paymentMethods = MutableStateFlow(SampleData.paymentMethods)
  val paymentMethods: StateFlow<List<PaymentMethodItem>> = _paymentMethods.asStateFlow()

  private val _tripHistory = MutableStateFlow(SampleData.tripHistory)
  val tripHistory: StateFlow<List<TripHistoryItem>> = _tripHistory.asStateFlow()

  private val _scheduledRides = MutableStateFlow(SampleData.sampleScheduledRides)
  val scheduledRides: StateFlow<List<ScheduledRideItem>> = _scheduledRides.asStateFlow()

  private val _ridePreferences = MutableStateFlow(RidePreferences())
  val ridePreferences: StateFlow<RidePreferences> = _ridePreferences.asStateFlow()

  private val _notifications = MutableStateFlow(SampleData.notifications)
  val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

  private val _friends = MutableStateFlow(SampleData.contacts)
  val friends: StateFlow<List<ContactFriend>> = _friends.asStateFlow()

  private val _drivers = MutableStateFlow(SampleData.drivers)
  val drivers: StateFlow<List<Driver>> = _drivers.asStateFlow()

  private val _activeDriver = MutableStateFlow(SampleData.drivers.first())
  val activeDriver: StateFlow<Driver> = _activeDriver.asStateFlow()

  fun sendOtp(phoneOrEmail: String) {
    val identifier = phoneOrEmail.trim()
    if (identifier.isNotEmpty()) {
      _authState.value = AuthState(phoneOrEmail = identifier, isAuthenticated = false)
    }
  }

  fun configureSessionStore(store: SupabaseSessionStore) {
    sessionStore = store
    repositoryScope.launch { restoreSession() }
  }

  suspend fun requestOtp(phoneOrEmail: String): Result<Unit> {
    val identifier = phoneOrEmail.trim()
    if (identifier.isBlank()) return Result.failure(IllegalArgumentException("Identifier is required"))
    sendOtp(identifier)
    return supabaseAuthClient.requestOtp(identifier)
  }

  suspend fun verifyOtpWithSupabase(code: String): Result<Unit> {
    val current = _authState.value
    if (current.phoneOrEmail.isBlank() || !code.matches(Regex("\\d{4}"))) {
      return Result.failure(IllegalArgumentException("A four-digit OTP is required"))
    }
    return supabaseAuthClient.verifyOtp(current.phoneOrEmail, code).map { session ->
      sessionStore?.save(session, current.phoneOrEmail)
      _authState.value = current.copy(isAuthenticated = true)
    }
  }

  private suspend fun restoreSession() {
    val stored = sessionStore?.read() ?: return
    val accessToken = if (stored.expiresAt > System.currentTimeMillis() + 60_000L) {
      stored.accessToken
    } else {
      supabaseAuthClient.refreshSession(stored.refreshToken).getOrNull()?.also {
        sessionStore?.save(it, stored.identifier)
      }?.access_token ?: run {
        sessionStore?.clear()
        return
      }
    }
    _authState.value = AuthState(phoneOrEmail = stored.identifier, isAuthenticated = true)
    supabaseDataClient.getProfile(accessToken, stored.userId).getOrNull()?.let { profile ->
      if (profile != null) _userProfile.value = profile.toUserProfile()
    }
    supabaseDataClient.getRides(accessToken, stored.userId).getOrNull()?.let { rides ->
      _tripHistory.value = rides.map { it.toTripHistoryItem() }
    }
    supabaseDataClient.getNotifications(accessToken, stored.userId).getOrNull()?.let { notifications ->
      _notifications.value = notifications.map { it.toNotificationItem() }
    }
  }

  fun logout() {
    _authState.value = AuthState()
    sessionStore?.clear()
  }

  fun setPickupLocation(loc: LocationPoint) {
    _pickupLocation.value = loc
  }

  fun setDropoffLocation(loc: LocationPoint) {
    _dropoffLocation.value = loc
  }

  fun addIntermediateStop(stop: LocationPoint) {
    _intermediateStops.update { current ->
      if (current.size < 4 && current.none { it.id == stop.id }) {
        current + stop
      } else {
        current
      }
    }
  }

  fun removeIntermediateStop(stopId: String) {
    _intermediateStops.update { current ->
      current.filterNot { it.id == stopId }
    }
  }

  fun clearIntermediateStops() {
    _intermediateStops.value = emptyList()
  }

  fun setIntermediateStops(stops: List<LocationPoint>) {
    _intermediateStops.value = stops.take(4)
  }

  fun toggleFavorite(locationId: String) {
    _popularLocations.update { list ->
      list.map { if (it.id == locationId) it.copy(isFavorite = !it.isFavorite) else it }
    }
  }

  fun selectVehicle(category: VehicleCategory) {
    _selectedVehicle.value = category
  }

  fun selectDriver(driver: Driver) {
    _activeDriver.value = driver
  }

  fun applyPromoCode(code: String): Boolean {
    val cleanCode = code.trim().uppercase()
    return if (cleanCode == "LYON30" || cleanCode == "LYON" || cleanCode == "LYONTAXIS" || cleanCode == "BIENVENUE" || cleanCode == "ABER30") {
      _appliedPromoCode.value = cleanCode
      val discount = if (cleanCode == "LYON30" || cleanCode == "ABER30") 7.50 else 5.00
      _discountAmount.value = discount
      true
    } else {
      false
    }
  }

  fun clearPromo() {
    _appliedPromoCode.value = null
    _discountAmount.value = 0.0
  }

  fun createBooking(): RideBooking {
    val vehicle = _selectedVehicle.value
    val stops = _intermediateStops.value
    val numStops = stops.size
    val stopFeePerStop = 3.50
    val totalStopFee = numStops * stopFeePerStop
    val additionalDistanceKm = numStops * 2.4
    val additionalDurationMin = numStops * 5

    val base = vehicle.basePrice + totalStopFee + (additionalDistanceKm * 1.50)
    val discount = _discountAmount.value
    val finalFare = (base - discount).coerceAtLeast(5.0)
    val distance = 4.8 + additionalDistanceKm
    val duration = vehicle.timeMinutes.coerceAtLeast(12) + additionalDurationMin
    val baseFare = 3.50
    val distFare = (((base - totalStopFee) * 0.65) * 100).toInt() / 100.0
    val timeFare = (((base - totalStopFee) * 0.25) * 100).toInt() / 100.0
    val serviceFee = 1.00

    val selectedPayment = _paymentMethods.value.find { it.isSelected }?.title ?: "Espèces LyonTaxis"

    val booking = RideBooking(
      id = "LYON-${System.currentTimeMillis() % 10000}",
      pickupLocation = _pickupLocation.value,
      dropoffLocation = _dropoffLocation.value,
      intermediateStops = stops,
      vehicle = vehicle,
      driver = _activeDriver.value,
      fare = finalFare,
      baseFare = baseFare,
      distanceFare = distFare,
      timeFare = timeFare,
      stopFee = totalStopFee,
      serviceFee = serviceFee,
      discount = discount,
      status = BookingStatus.CONFIRMED,
      distanceKm = distance,
      durationMin = duration,
      paymentMethodTitle = selectedPayment,
      preferences = _ridePreferences.value
    )
    _activeBooking.value = booking
    syncRide(booking)
    return booking
  }

  private fun syncRide(booking: RideBooking) {
    val session = sessionStore?.read() ?: return
    repositoryScope.launch {
      supabaseDataClient.createRide(
        session.accessToken,
        SupabaseRideInsert(
          vehicle_category = booking.vehicle.toApiCategory(),
          pickup_latitude = booking.pickupLocation.latitude,
          pickup_longitude = booking.pickupLocation.longitude,
          dropoff_latitude = booking.dropoffLocation.latitude,
          dropoff_longitude = booking.dropoffLocation.longitude,
          pickup_address = booking.pickupLocation.address,
          dropoff_address = booking.dropoffLocation.address,
          passenger_count = booking.vehicle.capacity.coerceAtLeast(1),
          special_requests = listOfNotNull(
            if (booking.preferences.babySeat) "Siège bébé" else null,
            if (booking.preferences.pmrAccess) "Accès PMR" else null,
            if (booking.preferences.petFriendly) "Animal accepté" else null,
            if (booking.preferences.extraLuggage) "Bagages supplémentaires" else null,
            if (booking.preferences.silentRide) "Course silencieuse" else null,
          ).joinToString(", ").ifBlank { null }
        )
      )
    }
  }

  fun updateRidePreferences(preferences: RidePreferences) {
    _ridePreferences.value = preferences
  }

  fun scheduleRide(
    scheduledDate: String,
    scheduledTime: String,
    vehicle: VehicleCategory = _selectedVehicle.value,
    specialInstructions: String = ""
  ): ScheduledRideItem {
    val stops = _intermediateStops.value
    val numStops = stops.size
    val totalStopFee = numStops * 3.50
    val additionalDistanceKm = numStops * 2.4
    val base = vehicle.basePrice + totalStopFee + (additionalDistanceKm * 1.50)
    val discount = _discountAmount.value
    val finalFare = (base - discount).coerceAtLeast(5.0)
    val selectedPayment = _paymentMethods.value.find { it.isSelected }?.title ?: "Espèces LyonTaxis"

    val item = ScheduledRideItem(
      id = "sched_${System.currentTimeMillis()}",
      pickupLocation = _pickupLocation.value,
      dropoffLocation = _dropoffLocation.value,
      intermediateStops = stops,
      vehicle = vehicle,
      scheduledDate = scheduledDate,
      scheduledTime = scheduledTime,
      estimatedFare = finalFare,
      paymentMethodTitle = selectedPayment,
      preferences = _ridePreferences.value,
      specialInstructions = specialInstructions.trim(),
      status = "Confirmée"
    )

    _scheduledRides.update { listOf(item) + it }

    // Add notification
    val notif = NotificationItem(
      id = "n_sched_${System.currentTimeMillis()}",
      type = NotificationType.SYSTEM_CONFIRM,
      title = "Course programmée confirmée",
      description = "Votre taxi pour le $scheduledDate à $scheduledTime a été réservé avec succès.",
      timeAgo = "À l'instant",
      isRead = false
    )
    _notifications.update { listOf(notif) + it }

    return item
  }

  fun cancelScheduledRide(id: String) {
    _scheduledRides.update { list ->
      list.map { if (it.id == id) it.copy(status = "Annulée") else it }
    }
  }

  fun prepareRideCompletion(): RideBooking {
    val current = _activeBooking.value ?: createBooking()
    _lastCompletedBooking.value = current
    return current
  }

  fun cancelActiveBooking() {
    _activeBooking.value?.let { current ->
      val cancelledItem = TripHistoryItem(
        id = "th_c_${System.currentTimeMillis()}",
        pickupTitle = current.pickupLocation.title,
        dropoffTitle = current.dropoffLocation.title,
        fare = current.fare,
        date = "Aujourd'hui",
        status = "Annulé",
        distanceKm = current.distanceKm,
        durationMin = current.durationMin,
        driverName = current.driver.name,
        vehicleName = current.vehicle.displayName
      )
      _tripHistory.update { listOf(cancelledItem) + it }
    }
    _activeBooking.value = null
  }

  fun completeActiveBooking(
    rating: Float = 5.0f,
    review: String = "",
    tipAmount: Double = 0.0,
    compliments: List<String> = emptyList()
  ) {
    val current = _lastCompletedBooking.value ?: _activeBooking.value
    if (current != null) {
      val totalCost = current.fare + tipAmount
      _userProfile.update {
        it.copy(
          cashBalance = (it.cashBalance - totalCost).coerceAtLeast(0.0),
          integralPoints = it.integralPoints + (totalCost * 10).toInt()
        )
      }
      val historyItem = TripHistoryItem(
        id = "th_${System.currentTimeMillis()}",
        pickupTitle = current.pickupLocation.title,
        dropoffTitle = current.dropoffLocation.title,
        fare = totalCost,
        date = "Aujourd'hui",
        status = "Terminé",
        distanceKm = current.distanceKm,
        durationMin = current.durationMin,
        driverName = current.driver.name,
        vehicleName = current.vehicle.displayName,
        rating = rating,
        tip = tipAmount,
        paymentMethod = current.paymentMethodTitle
      )
      _tripHistory.update { listOf(historyItem) + it }
    }
    _activeBooking.value = null
  }

  fun sendChatMessage(text: String) {
    if (text.isBlank()) return
    val now = timeFormat.format(Date())
    val msgId = "msg_${System.currentTimeMillis()}"
    val userMsg = ChatMessage(
      id = msgId,
      isFromUser = true,
      text = text,
      timestamp = now,
      status = MessageStatus.SENT,
      type = MessageType.TEXT
    )
    _chatMessages.update { it + userMsg }

    triggerSimulatedDriverReply(text)
  }

  fun sendVoiceMessage(durationSec: Int = 4) {
    val now = timeFormat.format(Date())
    val userMsg = ChatMessage(
      id = "voice_${System.currentTimeMillis()}",
      isFromUser = true,
      text = "Message vocal ($durationSec sec)",
      timestamp = now,
      status = MessageStatus.SENT,
      type = MessageType.AUDIO,
      audioDurationSec = durationSec
    )
    _chatMessages.update { it + userMsg }

    triggerSimulatedDriverReply("message vocal")
  }

  fun sendLocationShare(customTitle: String? = null) {
    val now = timeFormat.format(Date())
    val locTitle = customTitle ?: _pickupLocation.value.title
    val userMsg = ChatMessage(
      id = "loc_${System.currentTimeMillis()}",
      isFromUser = true,
      text = "Point de prise en charge partagé : $locTitle",
      timestamp = now,
      status = MessageStatus.SENT,
      type = MessageType.LOCATION,
      locationTitle = locTitle
    )
    _chatMessages.update { it + userMsg }

    triggerSimulatedDriverReply("position: $locTitle")
  }

  private fun triggerSimulatedDriverReply(incomingPrompt: String) {
    repositoryScope.launch {
      delay(800)
      // Update last message status to DELIVERED / READ
      _chatMessages.update { list ->
        list.map { if (it.isFromUser && it.status == MessageStatus.SENT) it.copy(status = MessageStatus.READ) else it }
      }

      // Driver starts typing
      _isDriverTyping.value = true
      delay(1600)
      _isDriverTyping.value = false

      val lower = incomingPrompt.lowercase()
      val replyText = when {
        lower.contains("dehors") || lower.contains("attends") || lower.contains("entrée") || lower.contains("place") ->
          "Bien reçu ! Je vois l'entrée principale, je m'arrête avec les feux de détresse allumés."
        lower.contains("où") || lower.contains("combien") || lower.contains("temps") || lower.contains("arrive") ->
          "Je viens de tourner sur l'avenue ! Arrivée estimée dans moins d'une minute."
        lower.contains("veste") || lower.contains("manteau") || lower.contains("debout") || lower.contains("statue") ->
          "Parfait, je vous ai repéré ! Je me range immédiatement le long du trottoir."
        lower.contains("clim") || lower.contains("froid") || lower.contains("chaud") ->
          "La climatisation est en route à température idéale !"
        lower.contains("vocal") || lower.contains("audio") ->
          "Message 5/5 ! Je me positionne exactement au repère indiqué."
        lower.contains("position") || lower.contains("repère") || lower.contains("gps") ->
          "Coordonnées GPS bien reçues. Je navigue directement vers votre position."
        else ->
          "Bien compris ! Je fais route vers vous, à tout de suite."
      }

      val now = timeFormat.format(Date())
      val driverReply = ChatMessage(
        id = "reply_${System.currentTimeMillis()}",
        isFromUser = false,
        text = replyText,
        timestamp = now,
        status = MessageStatus.READ,
        type = MessageType.TEXT
      )
      _chatMessages.update { it + driverReply }
    }
  }

  fun selectPaymentMethod(id: String) {
    _paymentMethods.update { list ->
      list.map { it.copy(isSelected = it.id == id) }
    }
  }

  fun setDefaultPaymentMethod(id: String) {
    _paymentMethods.update { list ->
      list.map { it.copy(isDefault = it.id == id, isSelected = if (it.id == id) true else it.isSelected) }
    }
  }

  fun deletePaymentMethod(id: String) {
    _paymentMethods.update { list ->
      val filtered = list.filterNot { it.id == id }
      if (filtered.isNotEmpty() && filtered.none { it.isSelected }) {
        filtered.mapIndexed { idx, item -> if (idx == 0) item.copy(isSelected = true, isDefault = true) else item }
      } else {
        filtered
      }
    }
  }

  fun addPaymentMethod(type: PaymentType, title: String, subtitle: String, isDefault: Boolean = false) {
    val newItem = PaymentMethodItem(
      id = "pm_${System.currentTimeMillis()}",
      type = type,
      title = title,
      subtitle = subtitle,
      isDefault = isDefault,
      isSelected = true
    )
    _paymentMethods.update { list ->
      val updated = if (isDefault) {
        list.map { it.copy(isDefault = false, isSelected = false) }
      } else {
        list.map { it.copy(isSelected = false) }
      }
      updated + newItem
    }
  }

  fun clearNotifications() {
    _notifications.value = emptyList()
  }

  fun toggleFriendInvite(friendId: String) {
    _friends.update { list ->
      list.map { if (it.id == friendId) it.copy(isInvited = !it.isInvited) else it }
    }
  }

  fun updateUserProfile(
    name: String,
    email: String,
    phone: String,
    gender: String,
    birthday: String,
    emergencyContact: String? = null,
    homeAddress: String? = null,
    avatarSeed: String? = null
  ) {
    _userProfile.update { current ->
      current.copy(
        name = name,
        email = email,
        phoneNumber = phone,
        gender = gender,
        birthday = birthday,
        emergencyContact = emergencyContact ?: current.emergencyContact,
        homeAddress = homeAddress ?: current.homeAddress,
        avatarSeed = avatarSeed ?: current.avatarSeed
      )
    }
    val session = sessionStore?.read() ?: return
    val profile = _userProfile.value.toDto(session.userId)
    repositoryScope.launch { supabaseDataClient.updateProfile(session.accessToken, profile) }
  }

  fun updateAvatar(avatarSeed: String) {
    _userProfile.update { it.copy(avatarSeed = avatarSeed) }
  }

  private fun LocationPoint.toDto() = SupabaseLocationDto(id, title, address, distanceKm, latitude, longitude)

  private fun VehicleCategory.toApiCategory() = when (this) {
    VehicleCategory.JUST_GO, VehicleCategory.ELECTRIC_CAR, VehicleCategory.BIKE -> "Eco"
    VehicleCategory.LIMOUSINE, VehicleCategory.LUXURY -> "Premium"
    VehicleCategory.TAXI_4_SEAT -> "Sedan"
    VehicleCategory.TAXI_7_SEAT -> "Van"
  }

  private fun SupabaseProfileDto.toUserProfile() = UserProfile(
    name = name,
    email = email.orEmpty(),
    phoneNumber = phone_number.orEmpty(),
    gender = gender.orEmpty(),
    birthday = birthday.orEmpty(),
    emergencyContact = emergency_contact.orEmpty(),
    homeAddress = home_address.orEmpty(),
    memberLevel = member_level,
    cashBalance = cash_balance,
    integralPoints = integral_points,
    couponsCount = coupons_count,
    referralCode = referral_code.orEmpty(),
    avatarSeed = avatar_seed.orEmpty()
  )

  private fun UserProfile.toDto(userId: String) = SupabaseProfileDto(
    id = userId,
    name = name,
    email = email,
    phone_number = phoneNumber,
    gender = gender,
    birthday = birthday,
    emergency_contact = emergencyContact,
    home_address = homeAddress,
    member_level = memberLevel,
    cash_balance = cashBalance,
    integral_points = integralPoints,
    coupons_count = couponsCount,
    referral_code = referralCode,
    avatar_seed = avatarSeed
  )

  private fun SupabaseRideDto.toTripHistoryItem() = TripHistoryItem(
    id = id.toString(),
    pickupTitle = pickup_location.title.ifBlank { pickup_location.address },
    dropoffTitle = dropoff_location.title.ifBlank { dropoff_location.address },
    fare = fare,
    date = created_at,
    status = status,
    distanceKm = distance_km,
    durationMin = duration_min,
    driverName = driver?.name ?: "Chauffeur LyonTaxis",
    vehicleName = vehicle,
    paymentMethod = payment_method_title
  )

  private fun SupabaseNotificationDto.toNotificationItem() = NotificationItem(
    id = id.toString(),
    type = NotificationType.SYSTEM_CONFIRM,
    title = title,
    description = description,
    timeAgo = created_at,
    isRead = is_read
  )
}

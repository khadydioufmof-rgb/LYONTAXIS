package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.DriverRepository
import com.example.data.supabase.DriverReservation
import com.example.data.supabase.DriverSupabaseAuthClient
import com.example.location.DriverGpsLocation
import com.example.location.LocationTracker
import com.example.model.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class DriverTab {
    RADAR,
    EARNINGS,
    HOTSPOTS,
    CHAT,
    PROFILE
}

data class DriverUiState(
    val isAuthenticated: Boolean = true, // Default to true or authenticated on-device with easy logout/re-login switch
    val status: DriverStatus = DriverStatus.OFFLINE,
    val selectedTab: DriverTab = DriverTab.RADAR,
    val currentOffer: RideRequest? = null,
    val activeRide: RideRequest? = null,
    val offerSecondsLeft: Int = 15,
    val navigationStepIndex: Int = 0,
    val speedKmh: Int = 0,
    val waitTimerSeconds: Int = 0,
    val completedRecord: EarningsRecord? = null,
    val showPayoutSuccessDialog: Boolean = false,
    val lastPayoutAmount: Double = 0.0,
    val isNightMode: Boolean = false, // Default light theme
    val searchDurationSeconds: Int = 0,
    val gpsLocation: DriverGpsLocation = DriverGpsLocation(),
    val isGpsActive: Boolean = false,
    val isLocationPermissionGranted: Boolean = false,
    val selectedConversationId: String? = null
)

class DriverViewModel(
    private val repository: DriverRepository = DriverRepository(),
    private var locationTracker: LocationTracker? = null
) : ViewModel() {

    private val supabaseClient = DriverSupabaseAuthClient()
    private var supabaseAccessToken: String? = null

    private val _uiState = MutableStateFlow(DriverUiState())
    val uiState: StateFlow<DriverUiState> = _uiState.asStateFlow()

    val profile: StateFlow<DriverProfile> = repository.driverProfile
    val vehicleProfile: StateFlow<com.example.data.local.VehicleProfileEntity> = repository.vehicleProfile
    val earnings: StateFlow<List<EarningsRecord>> = repository.earningsHistory
    val hotspots: StateFlow<List<SurgeHotspot>> = repository.surgeHotspots
    val conversations: StateFlow<List<PassengerConversation>> = repository.conversations
    val chatMessages: StateFlow<List<ChatMessage>> = repository.chatMessages
    val faqs: List<SupportFaq> = repository.getFaqs()

    private var offerCountdownJob: Job? = null
    private var searchingJob: Job? = null
    private var tripSimulationJob: Job? = null
    private var waitTimerJob: Job? = null
    private var gpsTrackingJob: Job? = null

    init {
        // Automatically start GPS tracking if tracker is provided
        if (locationTracker != null) {
            startGpsTracking()
        }
    }

    fun setLocationTracker(tracker: LocationTracker) {
        this.locationTracker = tracker
        _uiState.update { it.copy(isLocationPermissionGranted = tracker.hasLocationPermission()) }
        if (tracker.hasLocationPermission()) {
            startGpsTracking()
        }
    }

    fun onLocationPermissionResult(isGranted: Boolean) {
        _uiState.update { it.copy(isLocationPermissionGranted = isGranted) }
        if (isGranted) {
            startGpsTracking()
        } else {
            stopGpsTracking()
        }
    }

    fun startGpsTracking() {
        val tracker = locationTracker ?: return
        if (!tracker.hasLocationPermission()) return

        gpsTrackingJob?.cancel()
        gpsTrackingJob = viewModelScope.launch {
            _uiState.update { it.copy(isGpsActive = true) }
            // Fetch initial single location if available
            tracker.getCurrentLocation()?.let { initialLoc ->
                _uiState.update { it.copy(gpsLocation = initialLoc) }
            }

            // Stream continuous location updates from FusedLocationProviderClient
            tracker.getLocationUpdates(intervalMs = 2000L).collect { location ->
                _uiState.update { current ->
                    current.copy(
                        gpsLocation = location,
                        isGpsActive = true,
                        // If not in a fixed wait/offline state, update speed with real GPS speed
                        speedKmh = if (current.status == DriverStatus.ARRIVED_PICKUP || current.status == DriverStatus.OFFLINE) {
                            0
                        } else if (location.speedKmh > 0f) {
                            location.speedKmh.toInt()
                        } else {
                            current.speedKmh
                        }
                    )
                }
            }
        }
    }

    fun stopGpsTracking() {
        gpsTrackingJob?.cancel()
        _uiState.update { it.copy(isGpsActive = false) }
    }

    fun updateManualGpsPosition(latitude: Double, longitude: Double, speedKmh: Float = 0f, bearing: Float = 0f) {
        _uiState.update {
            it.copy(
                gpsLocation = it.gpsLocation.copy(
                    latitude = latitude,
                    longitude = longitude,
                    speedKmh = speedKmh,
                    bearing = bearing,
                    hasGpsFix = true
                )
            )
        }
    }

    fun selectTab(tab: DriverTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun setQuickFilter(filter: RideQuickFilter) {
        repository.setQuickFilter(filter)
    }

    fun toggleOnlineStatus() {
        if (_uiState.value.status == DriverStatus.OFFLINE) {
            goOnline()
        } else {
            goOffline()
        }
    }

    fun goOnline() {
        _uiState.update {
            it.copy(
                status = DriverStatus.ONLINE_WAITING,
                searchDurationSeconds = 0
            )
        }
        startSearchingRoutine()
    }

    fun goOffline() {
        offerCountdownJob?.cancel()
        searchingJob?.cancel()
        tripSimulationJob?.cancel()
        waitTimerJob?.cancel()
        _uiState.update {
            it.copy(
                status = DriverStatus.OFFLINE,
                currentOffer = null,
                activeRide = null,
                searchDurationSeconds = 0
            )
        }
    }

    private fun startSearchingRoutine() {
        searchingJob?.cancel()
        searchingJob = viewModelScope.launch {
            // Count search time
            var seconds = 0
            while (_uiState.value.status == DriverStatus.ONLINE_WAITING) {
                delay(1000)
                seconds++
                _uiState.update { it.copy(searchDurationSeconds = seconds) }
                supabaseAccessToken?.let { token ->
                    val location = _uiState.value.gpsLocation
                    supabaseClient.getNearestReservation(token, location.latitude, location.longitude)
                        .getOrNull()?.let { reservation ->
                            dispatchNearestReservation(reservation)
                            return@launch
                        }
                }
                // Simulate incoming ride dispatch after 4 seconds of searching
                if (seconds >= 4 && _uiState.value.status == DriverStatus.ONLINE_WAITING) {
                    dispatchNewOffer()
                    break
                }
            }
        }
    }

    fun dispatchNewOffer() {
        val offer = repository.createSampleOffer()
        _uiState.update {
            it.copy(
                status = DriverStatus.DISPATCH_OFFER,
                currentOffer = offer,
                offerSecondsLeft = 15
            )
        }
        startOfferCountdown()
    }

    fun dispatchNearestReservation(reservation: DriverReservation) {
        val category = when (reservation.vehicle.lowercase()) {
            "sedan" -> VehicleCategory.ABER_CONFORT
            "premium" -> VehicleCategory.ABER_BERLINE
            "van" -> VehicleCategory.ABER_VAN
            else -> VehicleCategory.ABER_X
        }
        val offer = repository.createSampleOffer().copy(
            id = reservation.id,
            category = category,
            pickupAddress = reservation.pickupAddress,
            pickupDistanceKm = reservation.distanceKm,
            dropoffAddress = reservation.dropoffAddress,
            estimatedFare = reservation.fare,
            specialNote = "Réservation Supabase la plus proche"
        )
        _uiState.update {
            it.copy(status = DriverStatus.DISPATCH_OFFER, currentOffer = offer, offerSecondsLeft = 15)
        }
        startOfferCountdown()
    }

    private fun startOfferCountdown() {
        offerCountdownJob?.cancel()
        offerCountdownJob = viewModelScope.launch {
            for (i in 15 downTo 0) {
                _uiState.update { it.copy(offerSecondsLeft = i) }
                delay(1000)
                if (i == 0 && _uiState.value.status == DriverStatus.DISPATCH_OFFER) {
                    declineOffer()
                }
            }
        }
    }

    fun acceptOffer() {
        offerCountdownJob?.cancel()
        val offer = _uiState.value.currentOffer ?: return
        repository.addOrUpdateActiveTripConversation(offer)
        _uiState.update {
            it.copy(
                status = DriverStatus.EN_ROUTE_PICKUP,
                activeRide = offer,
                currentOffer = null,
                navigationStepIndex = 0,
                speedKmh = 48,
                selectedConversationId = offer.id
            )
        }
        startNavigationSimulation()
    }

    fun declineOffer() {
        offerCountdownJob?.cancel()
        _uiState.update {
            it.copy(
                status = DriverStatus.ONLINE_WAITING,
                currentOffer = null,
                offerSecondsLeft = 15,
                searchDurationSeconds = 0
            )
        }
        startSearchingRoutine()
    }

    private fun startNavigationSimulation() {
        tripSimulationJob?.cancel()
        tripSimulationJob = viewModelScope.launch {
            while (_uiState.value.status == DriverStatus.EN_ROUTE_PICKUP ||
                _uiState.value.status == DriverStatus.IN_TRIP) {
                delay(3000)
                _uiState.update { current ->
                    val nextIndex = (current.navigationStepIndex + 1).coerceAtMost(
                        (current.activeRide?.navigationSteps?.size ?: 1) - 1
                    )
                    val newSpeed = (38..54).random()
                    current.copy(
                        navigationStepIndex = nextIndex,
                        speedKmh = newSpeed
                    )
                }
            }
        }
    }

    fun arriveAtPickup() {
        tripSimulationJob?.cancel()
        _uiState.update {
            it.copy(
                status = DriverStatus.ARRIVED_PICKUP,
                waitTimerSeconds = 0,
                speedKmh = 0
            )
        }
        startWaitTimer()
    }

    private fun startWaitTimer() {
        waitTimerJob?.cancel()
        waitTimerJob = viewModelScope.launch {
            var waitTime = 0
            while (_uiState.value.status == DriverStatus.ARRIVED_PICKUP) {
                delay(1000)
                waitTime++
                _uiState.update { it.copy(waitTimerSeconds = waitTime) }
            }
        }
    }

    fun startTrip() {
        waitTimerJob?.cancel()
        _uiState.update {
            it.copy(
                status = DriverStatus.IN_TRIP,
                navigationStepIndex = 0,
                speedKmh = 45
            )
        }
        startNavigationSimulation()
    }

    fun completeTrip() {
        tripSimulationJob?.cancel()
        val ride = _uiState.value.activeRide ?: return
        val baseFare = ride.estimatedFare
        val surgeBonus = (baseFare * (ride.surgeMultiplier - 1.0)).coerceAtLeast(0.0)
        val platformFee = (baseFare + surgeBonus) * 0.15
        val net = (baseFare + surgeBonus + ride.tipAmount) - platformFee

        val record = EarningsRecord(
            id = "REC-${(1000..9999).random()}",
            timestamp = SimpleDateFormat("HH:mm", Locale.FRANCE).format(Date()).let { "Aujourd'hui, $it" },
            passengerName = ride.passengerName,
            pickup = ride.pickupAddress,
            dropoff = ride.dropoffAddress,
            baseFare = baseFare,
            surgeBonus = surgeBonus,
            tip = ride.tipAmount,
            platformFee = platformFee,
            netEarnings = net,
            category = ride.category,
            distanceKm = ride.dropoffDistanceKm,
            durationMin = ride.dropoffDurationMin,
            paymentMethod = ride.paymentMethod
        )

        repository.completeTrip(record)

        _uiState.update {
            it.copy(
                status = DriverStatus.TRIP_COMPLETED,
                completedRecord = record,
                speedKmh = 0
            )
        }
    }

    fun dismissTripSummary() {
        _uiState.update {
            it.copy(
                status = DriverStatus.ONLINE_WAITING,
                activeRide = null,
                completedRecord = null,
                navigationStepIndex = 0,
                searchDurationSeconds = 0
            )
        }
        startSearchingRoutine()
    }

    fun selectConversation(conversationId: String?) {
        _uiState.update { it.copy(selectedConversationId = conversationId) }
        if (conversationId != null) {
            repository.markConversationAsRead(conversationId)
        }
    }

    fun sendChatMessage(text: String, conversationId: String? = null) {
        if (text.isBlank()) return
        val targetId = conversationId ?: _uiState.value.selectedConversationId ?: "conv_1"
        val timeNow = SimpleDateFormat("HH:mm", Locale.FRANCE).format(Date())
        val msg = ChatMessage(
            id = "c_${System.currentTimeMillis()}",
            senderIsDriver = true,
            text = text.trim(),
            timeFormatted = timeNow
        )
        repository.addChatMessage(msg)
        repository.addConversationMessage(targetId, msg)

        // Simulate fast passenger automated reply
        viewModelScope.launch {
            delay(1800)
            val passengerReplies = listOf(
                "Merci, j'arrive tout de suite !",
                "Parfait, je vous vois arriver au loin.",
                "Entendu, je sors du hall de l'immeuble.",
                "D'accord, merci pour l'information !",
                "Très bien, je vous attends à l'angle.",
                "Bien reçu Karim, merci !"
            )
            val replyMsg = ChatMessage(
                id = "c_${System.currentTimeMillis() + 1}",
                senderIsDriver = false,
                text = passengerReplies.random(),
                timeFormatted = SimpleDateFormat("HH:mm", Locale.FRANCE).format(Date())
            )
            repository.addChatMessage(replyMsg)
            repository.addConversationMessage(targetId, replyMsg)
        }
    }

    fun performInstantPayout(amount: Double) {
        val success = repository.performInstantPayout(amount)
        if (success) {
            _uiState.update {
                it.copy(
                    showPayoutSuccessDialog = true,
                    lastPayoutAmount = amount
                )
            }
        }
    }

    fun dismissPayoutDialog() {
        _uiState.update { it.copy(showPayoutSuccessDialog = false) }
    }

    fun toggleCashAccepted() {
        repository.updateProfile { it.copy(isCashAccepted = !it.isCashAccepted) }
    }

    fun toggleAutoAccept() {
        repository.updateProfile { it.copy(isAutoAcceptEnabled = !it.isAutoAcceptEnabled) }
    }

    fun toggleHomeDestination() {
        repository.updateProfile { it.copy(isHomeDestinationActive = !it.isHomeDestinationActive) }
    }

    fun toggleNightMode() {
        _uiState.update { it.copy(isNightMode = !it.isNightMode) }
    }

    fun updateVehicleProfile(
        model: String,
        plate: String,
        color: String,
        category: String,
        inspectionStatus: String,
        lastInspectionDate: String,
        nextInspectionDate: String,
        mileageKm: Int,
        technicalNotes: String
    ) {
        viewModelScope.launch {
            val current = repository.vehicleProfile.value
            val updated = current.copy(
                model = model.trim(),
                plate = plate.trim().uppercase(),
                color = color.trim(),
                category = category.trim(),
                inspectionStatus = inspectionStatus.trim(),
                lastInspectionDate = lastInspectionDate.trim(),
                nextInspectionDate = nextInspectionDate.trim(),
                mileageKm = mileageKm,
                technicalNotes = technicalNotes.trim()
            )
            repository.saveVehicleProfile(updated)
        }
    }

    fun updateWeeklyGoal(newGoal: Double) {
        repository.setWeeklyGoal(newGoal)
    }

    fun authenticateDriver(
        email: String,
        accessToken: String? = null,
        name: String,
        phone: String,
        plate: String,
        model: String,
        vtcNumber: String
    ) {
        supabaseAccessToken = accessToken
        repository.updateProfile { current ->
            current.copy(
                email = email.trim(),
                name = name.trim(),
                phone = phone.trim(),
                vehiclePlate = plate.trim().uppercase(),
                vehicleModel = model.trim(),
                vtcLicenseNumber = vtcNumber.trim()
            )
        }
        _uiState.update { it.copy(isAuthenticated = true) }
    }

    fun logoutDriver() {
        goOffline()
        _uiState.update { it.copy(isAuthenticated = false) }
    }
}

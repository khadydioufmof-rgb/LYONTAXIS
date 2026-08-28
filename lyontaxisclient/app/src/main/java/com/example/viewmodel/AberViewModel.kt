package com.example.viewmodel

import androidx.lifecycle.ViewModel
import com.example.data.AberRepository
import com.example.model.*
import kotlinx.coroutines.flow.StateFlow

class AberViewModel(
  private val repository: AberRepository = AberRepository.instance
) : ViewModel() {

  val authState: StateFlow<AuthState> = repository.authState
  val userProfile: StateFlow<UserProfile> = repository.userProfile
  val pickupLocation: StateFlow<LocationPoint> = repository.pickupLocation
  val dropoffLocation: StateFlow<LocationPoint> = repository.dropoffLocation
  val intermediateStops: StateFlow<List<LocationPoint>> = repository.intermediateStops
  val popularLocations: StateFlow<List<LocationPoint>> = repository.popularLocations
  val selectedVehicle: StateFlow<VehicleCategory> = repository.selectedVehicle
  val activeBooking: StateFlow<RideBooking?> = repository.activeBooking
  val lastCompletedBooking: StateFlow<RideBooking?> = repository.lastCompletedBooking
  val drivers: StateFlow<List<Driver>> = repository.drivers
  val selectedDriver: StateFlow<Driver> = repository.activeDriver
  val appliedPromo: StateFlow<String?> = repository.appliedPromoCode
  val discountAmount: StateFlow<Double> = repository.discountAmount
  val chatMessages: StateFlow<List<ChatMessage>> = repository.chatMessages
  val isDriverTyping: StateFlow<Boolean> = repository.isDriverTyping
  val paymentMethods: StateFlow<List<PaymentMethodItem>> = repository.paymentMethods
  val tripHistory: StateFlow<List<TripHistoryItem>> = repository.tripHistory
  val scheduledRides: StateFlow<List<ScheduledRideItem>> = repository.scheduledRides
  val ridePreferences: StateFlow<RidePreferences> = repository.ridePreferences
  val notifications: StateFlow<List<NotificationItem>> = repository.notifications
  val contactFriends: StateFlow<List<ContactFriend>> = repository.friends

  fun sendOtp(phoneOrEmail: String) {
    repository.sendOtp(phoneOrEmail)
  }

  suspend fun requestOtp(phoneOrEmail: String): Result<Unit> {
    return repository.requestOtp(phoneOrEmail)
  }

  suspend fun verifyOtpWithSupabase(code: String): Result<Unit> {
    return repository.verifyOtpWithSupabase(code)
  }

  fun setPickupLocation(loc: LocationPoint) {
    repository.setPickupLocation(loc)
  }

  fun setDropoffLocation(loc: LocationPoint) {
    repository.setDropoffLocation(loc)
  }

  fun addIntermediateStop(stop: LocationPoint) {
    repository.addIntermediateStop(stop)
  }

  fun removeIntermediateStop(stopId: String) {
    repository.removeIntermediateStop(stopId)
  }

  fun clearIntermediateStops() {
    repository.clearIntermediateStops()
  }

  fun setIntermediateStops(stops: List<LocationPoint>) {
    repository.setIntermediateStops(stops)
  }

  fun selectVehicle(category: VehicleCategory) {
    repository.selectVehicle(category)
  }

  fun selectDriver(driver: Driver) {
    repository.selectDriver(driver)
  }

  fun applyPromoCode(code: String): Boolean {
    return repository.applyPromoCode(code)
  }

  fun removePromoCode() {
    repository.clearPromo()
  }

  fun confirmBooking(): RideBooking {
    return repository.createBooking()
  }

  fun prepareRideCompletion(): RideBooking {
    return repository.prepareRideCompletion()
  }

  fun completeActiveRide(
    rating: Float = 5.0f,
    review: String = "",
    tip: Double = 0.0,
    compliments: List<String> = emptyList()
  ) {
    repository.completeActiveBooking(rating, review, tip, compliments)
  }

  fun cancelActiveRide() {
    repository.cancelActiveBooking()
  }

  fun sendChatMessage(text: String) {
    repository.sendChatMessage(text)
  }

  fun sendVoiceMessage(durationSec: Int = 4) {
    repository.sendVoiceMessage(durationSec)
  }

  fun sendLocationShare(customTitle: String? = null) {
    repository.sendLocationShare(customTitle)
  }

  fun selectPaymentMethod(id: String) {
    repository.selectPaymentMethod(id)
  }

  fun setDefaultPaymentMethod(id: String) {
    repository.setDefaultPaymentMethod(id)
  }

  fun deletePaymentMethod(id: String) {
    repository.deletePaymentMethod(id)
  }

  fun addPaymentMethod(type: PaymentType, title: String, subtitle: String, isDefault: Boolean = false) {
    repository.addPaymentMethod(type, title, subtitle, isDefault)
  }

  fun toggleFavoriteLocation(id: String) {
    repository.toggleFavorite(id)
  }

  fun toggleInviteFriend(id: String) {
    repository.toggleFriendInvite(id)
  }

  fun updateProfile(
    name: String,
    email: String,
    phone: String,
    gender: String,
    birthday: String,
    emergencyContact: String? = null,
    homeAddress: String? = null,
    avatarSeed: String? = null
  ) {
    repository.updateUserProfile(name, email, phone, gender, birthday, emergencyContact, homeAddress, avatarSeed)
  }

  fun updateAvatar(avatarSeed: String) {
    repository.updateAvatar(avatarSeed)
  }

  fun submitTripRatingAndTip(rating: Float, comment: String, tip: Double) {
    repository.completeActiveBooking(rating, comment, tip)
  }

  fun clearNotifications() {
    repository.clearNotifications()
  }

  fun updateRidePreferences(preferences: RidePreferences) {
    repository.updateRidePreferences(preferences)
  }

  fun scheduleRide(
    date: String,
    time: String,
    vehicle: VehicleCategory = selectedVehicle.value,
    specialInstructions: String = ""
  ): ScheduledRideItem {
    repository.selectVehicle(vehicle)
    return repository.scheduleRide(date, time, vehicle, specialInstructions)
  }

  fun cancelScheduledRide(id: String) {
    repository.cancelScheduledRide(id)
  }

  fun logout() {
    repository.logout()
  }
}

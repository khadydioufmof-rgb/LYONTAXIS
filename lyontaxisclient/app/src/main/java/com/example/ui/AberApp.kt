package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.AuthState
import com.example.model.VehicleCategory
import com.example.ui.screens.*
import com.example.ui.theme.AberBackground
import com.example.viewmodel.AberViewModel
import kotlinx.coroutines.launch

sealed class Screen(val route: String) {
  data object Splash : Screen("splash")
  data object Onboarding : Screen("onboarding")
  data object GpsSetup : Screen("gps_setup")
  data object Auth : Screen("auth")
  data object OtpVerify : Screen("otp_verify")
  data object Home : Screen("home")
  data object ChooseDropoff : Screen("choose_dropoff")
  data object PickOnMap : Screen("pick_on_map")
  data object VehicleSelection : Screen("vehicle_selection")
  data object ScheduleRide : Screen("schedule_ride")
  data object SelectDriver : Screen("select_driver")
  data object ActiveTracking : Screen("active_tracking")
  data object DriverChat : Screen("driver_chat")
  data object PostRideSummary : Screen("post_ride_summary")
  data object TripRating : Screen("trip_rating")
  data object Tip : Screen("tip")
  data object MyAccount : Screen("account")
  data object MyWallet : Screen("wallet")
  data object PaymentMethods : Screen("payment_methods")
  data object History : Screen("history")
  data object Invoices : Screen("invoices")
  data object Notifications : Screen("notifications")
  data object InviteFriends : Screen("invite")
  data object Settings : Screen("settings")
}

@Composable
fun AberApp(
  viewModel: AberViewModel = viewModel(),
  modifier: Modifier = Modifier
) {
  val authState by viewModel.authState.collectAsStateWithLifecycle()
  val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
  val pickupLocation by viewModel.pickupLocation.collectAsStateWithLifecycle()
  val dropoffLocation by viewModel.dropoffLocation.collectAsStateWithLifecycle()
  val intermediateStops by viewModel.intermediateStops.collectAsStateWithLifecycle()
  val popularLocations by viewModel.popularLocations.collectAsStateWithLifecycle()
  val selectedVehicle by viewModel.selectedVehicle.collectAsStateWithLifecycle()
  val activeBooking by viewModel.activeBooking.collectAsStateWithLifecycle()
  val lastCompletedBooking by viewModel.lastCompletedBooking.collectAsStateWithLifecycle()
  val drivers by viewModel.drivers.collectAsStateWithLifecycle()
  val selectedDriver by viewModel.selectedDriver.collectAsStateWithLifecycle()
  val appliedPromo by viewModel.appliedPromo.collectAsStateWithLifecycle()
  val discountAmount by viewModel.discountAmount.collectAsStateWithLifecycle()
  val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
  val isDriverTyping by viewModel.isDriverTyping.collectAsStateWithLifecycle()
  val paymentMethods by viewModel.paymentMethods.collectAsStateWithLifecycle()
  val tripHistory by viewModel.tripHistory.collectAsStateWithLifecycle()
  val scheduledRides by viewModel.scheduledRides.collectAsStateWithLifecycle()
  val ridePreferences by viewModel.ridePreferences.collectAsStateWithLifecycle()
  val notifications by viewModel.notifications.collectAsStateWithLifecycle()
  val contactFriends by viewModel.contactFriends.collectAsStateWithLifecycle()

  var currentScreen by remember { mutableStateOf<Screen>(Screen.Splash) }
  var previousScreen by remember { mutableStateOf<Screen>(Screen.Home) }

  var showPromoDialog by remember { mutableStateOf(false) }
  var showBookingSuccessDialog by remember { mutableStateOf(false) }

  val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
  val coroutineScope = rememberCoroutineScope()

  val selectedPayment = remember(paymentMethods) {
    paymentMethods.firstOrNull { it.isSelected } ?: paymentMethods.firstOrNull()
  }

  fun navigateTo(screen: Screen) {
    previousScreen = currentScreen
    currentScreen = screen
  }

  fun navigateBack() {
    currentScreen = when (currentScreen) {
      Screen.ChooseDropoff, Screen.VehicleSelection -> Screen.Home
      Screen.ScheduleRide -> previousScreen
      Screen.PickOnMap -> Screen.ChooseDropoff
      Screen.SelectDriver -> Screen.VehicleSelection
      Screen.DriverChat -> Screen.ActiveTracking
      Screen.Invoices -> Screen.Home
      Screen.PostRideSummary -> Screen.Home
      Screen.TripRating -> Screen.ActiveTracking
      Screen.Tip -> Screen.Home
      Screen.MyAccount, Screen.MyWallet, Screen.History, Screen.Invoices, Screen.Notifications, Screen.InviteFriends, Screen.Settings -> Screen.Home
      Screen.PaymentMethods -> previousScreen
      else -> Screen.Home
    }
  }

  ModalNavigationDrawer(
    drawerState = drawerState,
    gesturesEnabled = currentScreen == Screen.Home,
    drawerContent = {
      NavigationDrawerContent(
        userProfile = userProfile,
        onNavigate = { route ->
          when (route) {
            "home" -> currentScreen = Screen.Home
            "account" -> currentScreen = Screen.MyAccount
            "wallet" -> currentScreen = Screen.MyWallet
            "history" -> currentScreen = Screen.History
            "invoices" -> currentScreen = Screen.Invoices
            "notifications" -> currentScreen = Screen.Notifications
            "invite" -> currentScreen = Screen.InviteFriends
            "settings" -> currentScreen = Screen.Settings
          }
        },
        onCloseDrawer = {
          coroutineScope.launch { drawerState.close() }
        },
        onLogout = {
          viewModel.logout()
          currentScreen = Screen.Auth
        }
      )
    }
  ) {
    Scaffold(
      containerColor = AberBackground,
      modifier = modifier.fillMaxSize()
    ) { innerPadding ->
      Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
        when (currentScreen) {
          Screen.Splash -> {
            SplashScreen(
              onFinishSplash = {
                currentScreen = Screen.Onboarding
              }
            )
          }

          Screen.Onboarding -> {
            OnboardingScreen(
              onComplete = {
                currentScreen = Screen.GpsSetup
              }
            )
          }

          Screen.GpsSetup -> {
            GpsSetupScreen(
              onUseCurrentLocation = { location ->
                viewModel.setPickupLocation(location)
                currentScreen = Screen.Auth
              },
              onSelectManually = {
                currentScreen = Screen.Auth
              }
            )
          }

          Screen.Auth -> {
            AuthScreen(
              onNavigateToOtp = { phoneOrEmail ->
                coroutineScope.launch {
                  if (viewModel.requestOtp(phoneOrEmail).isSuccess) {
                    currentScreen = Screen.OtpVerify
                  }
                }
              }
            )
          }

          Screen.OtpVerify -> {
            OtpVerifyScreen(
              phoneNumber = authState.phoneOrEmail.ifEmpty { "+84 905 07 00 17" },
              onVerifySuccess = { code ->
                coroutineScope.launch {
                  if (viewModel.verifyOtpWithSupabase(code).isSuccess) {
                    currentScreen = Screen.Home
                  }
                }
              },
              onBackClick = {
                currentScreen = Screen.Auth
              }
            )
          }

          Screen.Home -> {
            HomeScreen(
              pickupLocation = pickupLocation,
              dropoffLocation = dropoffLocation,
              selectedVehicle = selectedVehicle,
              popularLocations = popularLocations,
              onOpenDrawer = {
                coroutineScope.launch { drawerState.open() }
              },
              onChooseDropoffClick = {
                currentScreen = Screen.ChooseDropoff
              },
              onSelectVehicleClick = {
                currentScreen = Screen.VehicleSelection
              },
              onSelectQuickLocation = { quickTitle ->
                val match = popularLocations.firstOrNull { it.title == quickTitle }
                if (match != null) {
                  viewModel.setDropoffLocation(match)
                }
              },
              onSelectVehicle = { category ->
                viewModel.selectVehicle(category)
              },
              onUpdateDropoffLocation = { loc ->
                viewModel.setDropoffLocation(loc)
              },
              onConfirmRideWithEstimate = { category ->
                viewModel.selectVehicle(category)
                currentScreen = Screen.VehicleSelection
              },
              onScheduleClick = {
                navigateTo(Screen.ScheduleRide)
              }
            )
          }

          Screen.ChooseDropoff -> {
            ChooseDropoffScreen(
              pickupLocation = pickupLocation,
              dropoffLocation = dropoffLocation,
              popularLocations = popularLocations,
              onSelectLocation = { loc ->
                viewModel.setDropoffLocation(loc)
                currentScreen = Screen.VehicleSelection
              },
              onPickOnMapClick = {
                currentScreen = Screen.PickOnMap
              },
              onFavoriteToggle = { id ->
                viewModel.toggleFavoriteLocation(id)
              },
              onBackClick = {
                currentScreen = Screen.Home
              }
            )
          }

          Screen.PickOnMap -> {
            PickOnMapScreen(
              pickupLocation = pickupLocation,
              currentLocation = dropoffLocation,
              onApplyLocation = { loc ->
                viewModel.setDropoffLocation(loc)
                currentScreen = Screen.VehicleSelection
              },
              onBackClick = {
                currentScreen = Screen.ChooseDropoff
              }
            )
          }

          Screen.VehicleSelection -> {
            VehicleSelectionSheet(
              pickupLocation = pickupLocation,
              dropoffLocation = dropoffLocation,
              intermediateStops = intermediateStops,
              popularLocations = popularLocations,
              selectedVehicle = selectedVehicle,
              onSelectVehicle = { v ->
                viewModel.selectVehicle(v)
              },
              onAddIntermediateStop = { stop ->
                viewModel.addIntermediateStop(stop)
              },
              onRemoveIntermediateStop = { stopId ->
                viewModel.removeIntermediateStop(stopId)
              },
              onMoveStopUp = { idx ->
                if (idx > 0 && idx < intermediateStops.size) {
                  val mutable = intermediateStops.toMutableList()
                  val item = mutable.removeAt(idx)
                  mutable.add(idx - 1, item)
                  viewModel.setIntermediateStops(mutable)
                }
              },
              onMoveStopDown = { idx ->
                if (idx >= 0 && idx < intermediateStops.size - 1) {
                  val mutable = intermediateStops.toMutableList()
                  val item = mutable.removeAt(idx)
                  mutable.add(idx + 1, item)
                  viewModel.setIntermediateStops(mutable)
                }
              },
              selectedPayment = selectedPayment,
              appliedPromo = appliedPromo,
              discountAmount = discountAmount,
              ridePreferences = ridePreferences,
              onUpdateRidePreferences = { prefs ->
                viewModel.updateRidePreferences(prefs)
              },
              onRequestRide = {
                currentScreen = Screen.SelectDriver
              },
              onScheduleRide = { date, time ->
                viewModel.scheduleRide(date, time)
              },
              onOpenScheduleScreen = {
                navigateTo(Screen.ScheduleRide)
              },
              onOpenPromoDialog = {
                showPromoDialog = true
              },
              onOpenPaymentMethods = {
                navigateTo(Screen.PaymentMethods)
              },
              onBackClick = {
                currentScreen = Screen.Home
              }
            )
          }

          Screen.ScheduleRide -> {
            ScheduleRideScreen(
              pickupLocation = pickupLocation,
              dropoffLocation = dropoffLocation,
              intermediateStops = intermediateStops,
              selectedVehicle = selectedVehicle,
              selectedPayment = selectedPayment,
              ridePreferences = ridePreferences,
              discountAmount = discountAmount,
              onConfirmSchedule = { formattedDate, formattedTime, vehicle, notes ->
                viewModel.scheduleRide(
                  date = formattedDate,
                  time = formattedTime,
                  vehicle = vehicle,
                  specialInstructions = notes
                )
                currentScreen = Screen.History
              },
              onBackClick = {
                navigateBack()
              }
            )
          }

          Screen.SelectDriver -> {
            SelectDriverScreen(
              driver = selectedDriver,
              vehicle = selectedVehicle,
              pickup = pickupLocation,
              dropoff = dropoffLocation,
              discount = discountAmount,
              onConfirmDriver = {
                viewModel.confirmBooking()
                showBookingSuccessDialog = true
              },
              onChatClick = {
                navigateTo(Screen.DriverChat)
              },
              onCallClick = { },
              onBackClick = {
                currentScreen = Screen.VehicleSelection
              }
            )
          }

          Screen.ActiveTracking -> {
            val booking = activeBooking ?: viewModel.confirmBooking()
            ActiveRideTrackingScreen(
              booking = booking,
              messages = chatMessages,
              isDriverTyping = isDriverTyping,
              onSendMessage = { msg ->
                viewModel.sendChatMessage(msg)
              },
              onSendVoiceNote = {
                viewModel.sendVoiceMessage(3)
              },
              onShareLocation = {
                viewModel.sendLocationShare(booking.pickupLocation.title)
              },
              onOpenChat = {
                navigateTo(Screen.DriverChat)
              },
              onCallDriver = { },
              onCancelRequest = {
                viewModel.cancelActiveRide()
                currentScreen = Screen.Home
              },
              onCompleteRide = {
                viewModel.prepareRideCompletion()
                currentScreen = Screen.PostRideSummary
              },
              onMinimize = {
                currentScreen = Screen.Home
              }
            )
          }

          Screen.PostRideSummary -> {
            val booking = lastCompletedBooking ?: activeBooking ?: viewModel.confirmBooking()
            PostRideSummaryScreen(
              booking = booking,
              onSubmitRatingAndFinish = { rating, comment, tip, compliments ->
                viewModel.completeActiveRide(rating, comment, tip, compliments)
                currentScreen = Screen.Home
              },
              onBackToHome = {
                viewModel.completeActiveRide()
                currentScreen = Screen.Home
              }
            )
          }

          Screen.DriverChat -> {
            DriverChatScreen(
              driver = selectedDriver,
              messages = chatMessages,
              isDriverTyping = isDriverTyping,
              onSendMessage = { msg ->
                viewModel.sendChatMessage(msg)
              },
              onSendVoiceNote = {
                viewModel.sendVoiceMessage(3)
              },
              onShareLocation = {
                viewModel.sendLocationShare(pickupLocation.title)
              },
              onCallDriver = { },
              onBackClick = {
                navigateBack()
              }
            )
          }

          Screen.TripRating -> {
            TripRatingScreen(
              driver = selectedDriver,
              onSubmitRating = { rating, comment ->
                currentScreen = Screen.Tip
              },
              onBackClick = {
                currentScreen = Screen.Home
              }
            )
          }

          Screen.Tip -> {
            TipScreen(
              driver = selectedDriver,
              onCompleteTip = { tipAmount ->
                viewModel.submitTripRatingAndTip(5.0f, "Great driving!", tipAmount)
                currentScreen = Screen.Home
              },
              onSkip = {
                viewModel.submitTripRatingAndTip(5.0f, "Good ride", 0.0)
                currentScreen = Screen.Home
              }
            )
          }

          Screen.MyAccount -> {
            MyAccountScreen(
              userProfile = userProfile,
              paymentMethods = paymentMethods,
              onUpdateProfile = { name, email, phone, gender, birthday, emergencyContact, homeAddress, avatarSeed ->
                viewModel.updateProfile(name, email, phone, gender, birthday, emergencyContact, homeAddress, avatarSeed)
              },
              onSelectPaymentMethod = { id ->
                viewModel.selectPaymentMethod(id)
              },
              onSetDefaultPaymentMethod = { id ->
                viewModel.setDefaultPaymentMethod(id)
              },
              onDeletePaymentMethod = { id ->
                viewModel.deletePaymentMethod(id)
              },
              onAddPaymentMethod = { type, title, subtitle, isDefault ->
                viewModel.addPaymentMethod(type, title, subtitle, isDefault)
              },
              onBackClick = {
                currentScreen = Screen.Home
              }
            )
          }

          Screen.MyWallet -> {
            MyWalletScreen(
              userProfile = userProfile,
              onOpenPaymentMethods = {
                navigateTo(Screen.PaymentMethods)
              },
              onBackClick = {
                currentScreen = Screen.Home
              }
            )
          }

          Screen.PaymentMethods -> {
            PaymentMethodsScreen(
              paymentMethods = paymentMethods,
              onSelectPayment = { id ->
                viewModel.selectPaymentMethod(id)
              },
              onAddPaymentMethod = { type, title, subtitle ->
                viewModel.addPaymentMethod(type, title, subtitle)
              },
              onBackClick = {
                navigateBack()
              }
            )
          }

          Screen.History -> {
            TripHistoryScreen(
              historyItems = tripHistory,
              scheduledRides = scheduledRides,
              onCancelScheduledRide = { id ->
                viewModel.cancelScheduledRide(id)
              },
              onBackClick = {
                currentScreen = Screen.Home
              }
            )
          }

          Screen.Invoices -> {
            InvoicesScreen(
              userProfile = userProfile,
              tripHistory = tripHistory,
              onBackClick = {
                currentScreen = Screen.Home
              }
            )
          }

          Screen.Notifications -> {
            NotificationsScreen(
              notifications = notifications,
              onClearAll = {
                viewModel.clearNotifications()
              },
              onBackClick = {
                currentScreen = Screen.Home
              }
            )
          }

          Screen.InviteFriends -> {
            InviteFriendsScreen(
              inviteCode = userProfile.referralCode,
              friends = contactFriends,
              onToggleInvite = { id ->
                viewModel.toggleInviteFriend(id)
              },
              onBackClick = {
                currentScreen = Screen.Home
              }
            )
          }

          Screen.Settings -> {
            SettingsScreen(
              userProfile = userProfile,
              onOpenProfile = {
                currentScreen = Screen.MyAccount
              },
              onLogout = {
                viewModel.logout()
                currentScreen = Screen.Auth
              },
              onBackClick = {
                currentScreen = Screen.Home
              }
            )
          }
        }

        // Modals & Dialogs
        if (showPromoDialog) {
          PromoCodeDialog(
            onDismiss = { showPromoDialog = false },
            onApplyPromo = { code ->
              viewModel.applyPromoCode(code)
              showPromoDialog = false
            },
            appliedPromo = appliedPromo
          )
        }

        if (showBookingSuccessDialog) {
          BookingSuccessDialog(
            onDismiss = { showBookingSuccessDialog = false },
            onDone = {
              showBookingSuccessDialog = false
              currentScreen = Screen.ActiveTracking
            },
            onCancel = {
              showBookingSuccessDialog = false
              viewModel.cancelActiveRide()
              currentScreen = Screen.Home
            }
          )
        }
      }
    }
  }
}

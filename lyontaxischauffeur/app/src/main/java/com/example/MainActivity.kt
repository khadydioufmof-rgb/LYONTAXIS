package com.example

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.location.DefaultLocationTracker
import com.example.data.supabase.DriverSupabaseAuthClient
import com.example.model.DriverStatus
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.viewmodel.DriverTab
import com.example.viewmodel.DriverViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val viewModel: DriverViewModel = viewModel(
                factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                        val db = com.example.data.local.AppDatabase.getDatabase(context.applicationContext)
                        val repo = com.example.data.DriverRepository(db.vehicleDao())
                        @Suppress("UNCHECKED_CAST")
                        return DriverViewModel(repo) as T
                    }
                }
            )
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val profile by viewModel.profile.collectAsStateWithLifecycle()
            val vehicleEntity by viewModel.vehicleProfile.collectAsStateWithLifecycle()
            val earnings by viewModel.earnings.collectAsStateWithLifecycle()
            val hotspots by viewModel.hotspots.collectAsStateWithLifecycle()
            val conversations by viewModel.conversations.collectAsStateWithLifecycle()
            val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
            val supabaseAuthClient = remember { DriverSupabaseAuthClient() }

            val locationPermissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestMultiplePermissions()
            ) { permissions ->
                val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
                val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
                viewModel.onLocationPermissionResult(fineGranted || coarseGranted)
            }

            LaunchedEffect(Unit) {
                val tracker = DefaultLocationTracker(context.applicationContext)
                viewModel.setLocationTracker(tracker)
                if (!tracker.hasLocationPermission()) {
                    locationPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }
            }

            MyApplicationTheme(darkTheme = uiState.isNightMode) {
                if (!uiState.isAuthenticated) {
                    DriverAuthScreen(
                        onLoginSuccess = { email, password, name, phone, plate, model, vtc ->
                            supabaseAuthClient.signIn(email, password)
                                .map { session ->
                                    viewModel.authenticateDriver(
                                        email = email,
                                        accessToken = session.access_token,
                                        name = name,
                                        phone = phone,
                                        plate = plate,
                                        model = model,
                                        vtcNumber = vtc
                                    )
                                    Unit
                                }
                        }
                    )
                } else {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        bottomBar = {
                            // Only show bottom navigation when NOT in active driving navigation mode
                            val isNavigatingActiveTrip = uiState.activeRide != null && (
                                uiState.status == DriverStatus.EN_ROUTE_PICKUP ||
                                uiState.status == DriverStatus.ARRIVED_PICKUP ||
                                uiState.status == DriverStatus.IN_TRIP
                            )

                            if (!isNavigatingActiveTrip && uiState.status != DriverStatus.DISPATCH_OFFER) {
                                NavigationBar(
                                    modifier = Modifier
                                        .testTag("driver_bottom_bar")
                                        .windowInsetsPadding(WindowInsets.navigationBars),
                                    containerColor = if (uiState.isNightMode) Color(0xFF0F1622) else Color.White,
                                    tonalElevation = 8.dp
                                ) {
                                    NavigationBarItem(
                                        selected = uiState.selectedTab == DriverTab.RADAR,
                                        onClick = { viewModel.selectTab(DriverTab.RADAR) },
                                        icon = {
                                            Icon(
                                                imageVector = if (uiState.selectedTab == DriverTab.RADAR) Icons.Filled.NearMe else Icons.Outlined.NearMe,
                                                contentDescription = "Radar et Carte"
                                            )
                                        },
                                        label = { Text("Courses", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = Color(0xFF003829),
                                            selectedTextColor = AberMint,
                                            indicatorColor = AberMint,
                                            unselectedIconColor = if (uiState.isNightMode) TextSecondaryDark else TextSecondaryLight,
                                            unselectedTextColor = if (uiState.isNightMode) TextSecondaryDark else TextSecondaryLight
                                        )
                                    )

                                    NavigationBarItem(
                                        selected = uiState.selectedTab == DriverTab.EARNINGS,
                                        onClick = { viewModel.selectTab(DriverTab.EARNINGS) },
                                        icon = {
                                            Icon(
                                                imageVector = if (uiState.selectedTab == DriverTab.EARNINGS) Icons.Filled.AccountBalanceWallet else Icons.Outlined.AccountBalanceWallet,
                                                contentDescription = "Revenus et Portefeuille"
                                            )
                                        },
                                        label = { Text("Revenus", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = Color(0xFF003829),
                                            selectedTextColor = AberMint,
                                            indicatorColor = AberMint,
                                            unselectedIconColor = if (uiState.isNightMode) TextSecondaryDark else TextSecondaryLight,
                                            unselectedTextColor = if (uiState.isNightMode) TextSecondaryDark else TextSecondaryLight
                                        )
                                    )

                                    NavigationBarItem(
                                        selected = uiState.selectedTab == DriverTab.HOTSPOTS,
                                        onClick = { viewModel.selectTab(DriverTab.HOTSPOTS) },
                                        icon = {
                                            Icon(
                                                imageVector = if (uiState.selectedTab == DriverTab.HOTSPOTS) Icons.Filled.LocalFireDepartment else Icons.Outlined.LocalFireDepartment,
                                                contentDescription = "Zones et Demande"
                                            )
                                        },
                                        label = { Text("Zones", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = Color(0xFF003829),
                                            selectedTextColor = AberMint,
                                            indicatorColor = AberMint,
                                            unselectedIconColor = if (uiState.isNightMode) TextSecondaryDark else TextSecondaryLight,
                                            unselectedTextColor = if (uiState.isNightMode) TextSecondaryDark else TextSecondaryLight
                                        )
                                    )

                                    NavigationBarItem(
                                        selected = uiState.selectedTab == DriverTab.CHAT,
                                        onClick = { viewModel.selectTab(DriverTab.CHAT) },
                                        icon = {
                                            val unreadCount = conversations.sumOf { it.unreadCount }
                                            if (unreadCount > 0) {
                                                BadgedBox(
                                                    badge = {
                                                        Badge(
                                                            containerColor = AberMint,
                                                            contentColor = Color(0xFF003829)
                                                        ) {
                                                            Text("$unreadCount", fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                                        }
                                                    }
                                                ) {
                                                    Icon(
                                                        imageVector = if (uiState.selectedTab == DriverTab.CHAT) Icons.Filled.ChatBubble else Icons.Outlined.ChatBubbleOutline,
                                                        contentDescription = "Messages"
                                                    )
                                                }
                                            } else {
                                                Icon(
                                                    imageVector = if (uiState.selectedTab == DriverTab.CHAT) Icons.Filled.ChatBubble else Icons.Outlined.ChatBubbleOutline,
                                                    contentDescription = "Messages"
                                                )
                                            }
                                        },
                                        label = { Text("Messages", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = Color(0xFF003829),
                                            selectedTextColor = AberMint,
                                            indicatorColor = AberMint,
                                            unselectedIconColor = if (uiState.isNightMode) TextSecondaryDark else TextSecondaryLight,
                                            unselectedTextColor = if (uiState.isNightMode) TextSecondaryDark else TextSecondaryLight
                                        )
                                    )

                                    NavigationBarItem(
                                        selected = uiState.selectedTab == DriverTab.PROFILE,
                                        onClick = { viewModel.selectTab(DriverTab.PROFILE) },
                                        icon = {
                                            Icon(
                                                imageVector = if (uiState.selectedTab == DriverTab.PROFILE) Icons.Filled.Person else Icons.Outlined.PersonOutline,
                                                contentDescription = "Profil Chauffeur"
                                            )
                                        },
                                        label = { Text("Profil", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = Color(0xFF003829),
                                            selectedTextColor = AberMint,
                                            indicatorColor = AberMint,
                                            unselectedIconColor = if (uiState.isNightMode) TextSecondaryDark else TextSecondaryLight,
                                            unselectedTextColor = if (uiState.isNightMode) TextSecondaryDark else TextSecondaryLight
                                        )
                                    )
                                }
                            }
                        }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            when (uiState.selectedTab) {
                                DriverTab.RADAR -> {
                                    DriverHomeScreen(
                                        uiState = uiState,
                                        profile = profile,
                                        surgeHotspots = hotspots,
                                        onToggleOnline = { viewModel.toggleOnlineStatus() },
                                        onSimulateOffer = { viewModel.dispatchNewOffer() },
                                        onAcceptOffer = { viewModel.acceptOffer() },
                                        onDeclineOffer = { viewModel.declineOffer() },
                                        onArrivedAtPickup = { viewModel.arriveAtPickup() },
                                        onStartTrip = { viewModel.startTrip() },
                                        onCompleteTrip = { viewModel.completeTrip() },
                                        onDismissTripSummary = { viewModel.dismissTripSummary() },
                                        onOpenChat = { viewModel.selectTab(DriverTab.CHAT) },
                                        onToggleNightMode = { viewModel.toggleNightMode() },
                                        onSelectQuickFilter = { filter -> viewModel.setQuickFilter(filter) }
                                    )
                                }
                                DriverTab.EARNINGS -> {
                                    EarningsScreen(
                                        profile = profile,
                                        earningsList = earnings,
                                        showPayoutSuccessDialog = uiState.showPayoutSuccessDialog,
                                        lastPayoutAmount = uiState.lastPayoutAmount,
                                        onInstantPayout = { viewModel.performInstantPayout(it) },
                                        onDismissPayoutDialog = { viewModel.dismissPayoutDialog() },
                                        onUpdateWeeklyGoal = { viewModel.updateWeeklyGoal(it) }
                                    )
                                }
                                DriverTab.HOTSPOTS -> {
                                    SurgeMapScreen(
                                        hotspots = hotspots,
                                        onNavigateToZone = {
                                            viewModel.selectTab(DriverTab.RADAR)
                                            if (uiState.status == DriverStatus.OFFLINE) {
                                                viewModel.goOnline()
                                            }
                                        }
                                    )
                                }
                                DriverTab.CHAT -> {
                                    ChatSupportScreen(
                                        conversations = conversations,
                                        selectedConversationId = uiState.selectedConversationId,
                                        faqs = viewModel.faqs,
                                        onSelectConversation = { viewModel.selectConversation(it) },
                                        onSendMessage = { text, convId -> viewModel.sendChatMessage(text, convId) }
                                    )
                                }
                                DriverTab.PROFILE -> {
                                    DriverProfileScreen(
                                        profile = profile,
                                        vehicleEntity = vehicleEntity,
                                        onToggleCash = { viewModel.toggleCashAccepted() },
                                        onToggleAutoAccept = { viewModel.toggleAutoAccept() },
                                        onToggleHomeDestination = { viewModel.toggleHomeDestination() },
                                        onUpdateVehicleProfile = { model, plate, color, cat, status, lastDate, nextDate, km, notes ->
                                            viewModel.updateVehicleProfile(
                                                model = model,
                                                plate = plate,
                                                color = color,
                                                category = cat,
                                                inspectionStatus = status,
                                                lastInspectionDate = lastDate,
                                                nextInspectionDate = nextDate,
                                                mileageKm = km,
                                                technicalNotes = notes
                                            )
                                        },
                                        onLogout = { viewModel.logoutDriver() }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DriverProfile
import com.example.model.DriverStatus
import com.example.R
import com.example.model.SurgeHotspot
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.DriverUiState
import java.util.Locale

@Composable
fun DriverHomeScreen(
    uiState: DriverUiState,
    profile: DriverProfile,
    surgeHotspots: List<SurgeHotspot>,
    onToggleOnline: () -> Unit,
    onSimulateOffer: () -> Unit,
    onAcceptOffer: () -> Unit,
    onDeclineOffer: () -> Unit,
    onArrivedAtPickup: () -> Unit,
    onStartTrip: () -> Unit,
    onCompleteTrip: () -> Unit,
    onDismissTripSummary: () -> Unit,
    onOpenChat: () -> Unit,
    onToggleNightMode: () -> Unit,
    onSelectQuickFilter: (com.example.model.RideQuickFilter) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("driver_home_screen")
    ) {
        // MAP BACKGROUND
        DriverLeafletMap(
            status = uiState.status,
            activeRide = uiState.activeRide,
            modifier = Modifier.fillMaxSize()
        )

        // TOP DRIVER STATUS & HEADER CONTROLS
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Main Top Bar Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (uiState.isNightMode) Color(0xFF131A26) else Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (uiState.isNightMode) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.06f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    androidx.compose.foundation.Image(
                        painter = painterResource(R.drawable.lyontaxis_logo),
                        contentDescription = "Logo LyonTaxis Pro",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    // Driver Profile & Earnings Quick Overview
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(AberMint),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "KB",
                                color = Color(0xFF003829),
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp
                            )
                        }

                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = String.format("%.2f €", profile.todayEarnings),
                                    color = if (uiState.isNightMode) Color.White else Color.Black,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = "aujourd'hui",
                                    color = if (uiState.isNightMode) TextSecondaryDark else TextSecondaryLight,
                                    fontSize = 12.sp
                                )
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "${profile.todayTripsCount} courses",
                                    color = AberMint,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "• ★ ${profile.rating}",
                                    color = AberGold,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Night Mode Toggle & Notification badge
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = onToggleNightMode,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(
                                imageVector = if (uiState.isNightMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = "Changer le mode jour/nuit",
                                tint = if (uiState.isNightMode) AberGold else Color.DarkGray
                            )
                        }
                    }
                }
            }

            // Status Notice Ribbon
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = when (uiState.status) {
                    DriverStatus.OFFLINE -> Color(0xFF1E293B)
                    DriverStatus.ONLINE_WAITING -> AberMint.copy(alpha = 0.15f)
                    DriverStatus.DISPATCH_OFFER -> AberOrange.copy(alpha = 0.18f)
                    else -> AberMint.copy(alpha = 0.2f)
                },
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    when (uiState.status) {
                        DriverStatus.OFFLINE -> Color.White.copy(alpha = 0.1f)
                        DriverStatus.ONLINE_WAITING -> AberMint.copy(alpha = 0.5f)
                        DriverStatus.DISPATCH_OFFER -> AberOrange
                        else -> AberMint
                    }
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(
                                when (uiState.status) {
                                    DriverStatus.OFFLINE -> Color(0xFF94A3B8)
                                    DriverStatus.ONLINE_WAITING -> AberMint
                                    DriverStatus.DISPATCH_OFFER -> AberOrange
                                    else -> AberMint
                                }
                            )
                    )
                    Text(
                        text = when (uiState.status) {
                            DriverStatus.OFFLINE -> "Vous êtes hors ligne • Passez en ligne pour recevoir des courses"
                            DriverStatus.ONLINE_WAITING -> "En ligne • Recherche de passagers à proximité..."
                            DriverStatus.DISPATCH_OFFER -> "Nouvelle proposition de course entrante !"
                            DriverStatus.EN_ROUTE_PICKUP -> "En route vers le passager"
                            DriverStatus.ARRIVED_PICKUP -> "Arrivé au point de prise en charge"
                            DriverStatus.IN_TRIP -> "Course en cours vers la destination"
                            DriverStatus.TRIP_COMPLETED -> "Course clôturée avec succès"
                        },
                        color = if (uiState.isNightMode) Color.White else Color.Black,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                }
            }

            // Weekly Goal Progress Bar
            val weeklyGoal = profile.weeklyGoalEarnings
            val weeklyCurrent = profile.weeklyEarnings
            val weeklyProgress = (weeklyCurrent / weeklyGoal).toFloat().coerceIn(0f, 1f)
            val weeklyPct = ((weeklyCurrent / weeklyGoal) * 100).toInt()

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (uiState.isNightMode) Color(0xFF131A26) else Color(0xFFFFFFFF),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (uiState.isNightMode) AberMint.copy(alpha = 0.3f) else AberMint.copy(alpha = 0.4f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.TrackChanges,
                                contentDescription = null,
                                tint = AberMint,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Objectif semaine",
                                color = if (uiState.isNightMode) Color.White else Color.Black,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = "${String.format(Locale.FRANCE, "%.2f €", weeklyCurrent)} / ${String.format(Locale.FRANCE, "%.0f €", weeklyGoal)} ($weeklyPct%)",
                            color = AberMint,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    LinearProgressIndicator(
                        progress = { weeklyProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = if (weeklyCurrent >= weeklyGoal) AberGold else AberMint,
                        trackColor = if (uiState.isNightMode) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.08f)
                    )
                }
            }

            // Real-Time GPS Tracking Status Bar
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (uiState.isNightMode) Color(0xFF0F172A).copy(alpha = 0.9f) else Color(0xFFF1F5F9).copy(alpha = 0.95f),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (uiState.isGpsActive) AberMint.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.1f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.GpsFixed,
                            contentDescription = "GPS Fix",
                            tint = if (uiState.isGpsActive) AberMint else Color.Gray,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = if (uiState.isGpsActive) "GPS Fix HD (±${uiState.gpsLocation.accuracyMeters.toInt()}m)" else "GPS en attente",
                            color = if (uiState.isGpsActive) AberMint else TextMutedDark,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = String.format(
                            Locale.US,
                            "%.4f, %.4f • %d km/h",
                            uiState.gpsLocation.latitude,
                            uiState.gpsLocation.longitude,
                            uiState.speedKmh
                        ),
                        color = if (uiState.isNightMode) TextSecondaryDark else TextSecondaryLight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // BOTTOM MAIN CONTROLS (WHEN NO ACTIVE RIDE)
        if (uiState.status == DriverStatus.OFFLINE || uiState.status == DriverStatus.ONLINE_WAITING) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // QUICK RIDE FILTERS BAR
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("quick_filters_card"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (uiState.isNightMode) Color(0xFF131A26).copy(alpha = 0.95f) else Color.White.copy(alpha = 0.95f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (uiState.isNightMode) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.08f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FilterList,
                                    contentDescription = "Filtres rapides",
                                    tint = AberMint,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Préférences de courses",
                                    color = if (uiState.isNightMode) Color.White else Color.Black,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            if (profile.activeQuickFilter != com.example.model.RideQuickFilter.ALL) {
                                Surface(
                                    onClick = { onSelectQuickFilter(com.example.model.RideQuickFilter.ALL) },
                                    shape = RoundedCornerShape(6.dp),
                                    color = AberMint.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "Réinitialiser",
                                        color = AberMint,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        // Scrollable row of quick filter chips
                        androidx.compose.foundation.lazy.LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(com.example.model.RideQuickFilter.values().size) { index ->
                                val filter = com.example.model.RideQuickFilter.values()[index]
                                val isSelected = profile.activeQuickFilter == filter
                                Surface(
                                    onClick = { onSelectQuickFilter(filter) },
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) AberMint else (if (uiState.isNightMode) Color(0xFF1E293B) else Color(0xFFF1F5F9)),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isSelected) AberMint else (if (uiState.isNightMode) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.08f))
                                    ),
                                    modifier = Modifier.testTag("filter_chip_${filter.name.lowercase()}")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = when (filter) {
                                                com.example.model.RideQuickFilter.ALL -> Icons.Default.AllInclusive
                                                com.example.model.RideQuickFilter.SHORT_DISTANCE -> Icons.Default.NearMe
                                                com.example.model.RideQuickFilter.AIRPORT -> Icons.Default.FlightTakeoff
                                                com.example.model.RideQuickFilter.SURGE_ONLY -> Icons.Default.Bolt
                                                com.example.model.RideQuickFilter.PREMIUM_ONLY -> Icons.Default.Star
                                            },
                                            contentDescription = null,
                                            tint = if (isSelected) Color(0xFF003829) else (if (uiState.isNightMode) TextSecondaryDark else Color.DarkGray),
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = filter.label,
                                            color = if (isSelected) Color(0xFF003829) else (if (uiState.isNightMode) Color.White else Color.Black),
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Test Simulation Dispatch Trigger Button (Helper for testing)
                if (uiState.status == DriverStatus.ONLINE_WAITING) {
                    Surface(
                        onClick = onSimulateOffer,
                        shape = RoundedCornerShape(12.dp),
                        color = AberOrange.copy(alpha = 0.2f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AberOrange),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                tint = AberOrange,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Simuler une nouvelle course",
                                color = AberOrange,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Big Online / Offline Toggle Button
                Button(
                    onClick = onToggleOnline,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .testTag("toggle_online_button"),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (uiState.status == DriverStatus.OFFLINE) AberMint else AberRed,
                        contentColor = if (uiState.status == DriverStatus.OFFLINE) Color(0xFF003829) else Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = if (uiState.status == DriverStatus.OFFLINE) Icons.Default.PowerSettingsNew else Icons.Default.PowerOff,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = if (uiState.status == DriverStatus.OFFLINE) "PASSER EN LIGNE" else "PASSER HORS LIGNE",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }

        // INCOMING RIDE DISPATCH OVERLAY
        AnimatedVisibility(
            visible = uiState.status == DriverStatus.DISPATCH_OFFER && uiState.currentOffer != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            uiState.currentOffer?.let { offer ->
                RideDispatchSheet(
                    offer = offer,
                    secondsLeft = uiState.offerSecondsLeft,
                    onAccept = onAcceptOffer,
                    onDecline = onDeclineOffer
                )
            }
        }

        // ACTIVE NAVIGATION OVERLAY
        if (uiState.activeRide != null && (
            uiState.status == DriverStatus.EN_ROUTE_PICKUP ||
            uiState.status == DriverStatus.ARRIVED_PICKUP ||
            uiState.status == DriverStatus.IN_TRIP
        )) {
            ActiveNavigationOverlay(
                status = uiState.status,
                ride = uiState.activeRide,
                stepIndex = uiState.navigationStepIndex,
                speedKmh = uiState.speedKmh,
                waitTimerSeconds = uiState.waitTimerSeconds,
                onArrivedAtPickup = onArrivedAtPickup,
                onStartTrip = onStartTrip,
                onCompleteTrip = onCompleteTrip,
                onOpenChat = onOpenChat
            )
        }

        // TRIP COMPLETION SUMMARY MODAL
        if (uiState.status == DriverStatus.TRIP_COMPLETED && uiState.completedRecord != null) {
            TripCompletionDialog(
                record = uiState.completedRecord,
                onDismiss = onDismissTripSummary
            )
        }
    }
}

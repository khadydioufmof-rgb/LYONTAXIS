package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DriverStatus
import com.example.model.RideRequest
import com.example.ui.theme.*

@Composable
fun ActiveNavigationOverlay(
    status: DriverStatus,
    ride: RideRequest,
    stepIndex: Int,
    speedKmh: Int,
    waitTimerSeconds: Int,
    onArrivedAtPickup: () -> Unit,
    onStartTrip: () -> Unit,
    onCompleteTrip: () -> Unit,
    onOpenChat: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showCancelDialog by remember { mutableStateOf(false) }
    var showSosDialog by remember { mutableStateOf(false) }

    val currentStep = ride.navigationSteps.getOrNull(stepIndex) ?: ride.navigationSteps.firstOrNull()

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("active_navigation_overlay"),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // TOP GPS TURN-BY-TURN GUIDANCE BANNER
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Navigation Direction Icon Box
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(AberMint),
                    contentAlignment = Alignment.Center
                ) {
                    val navIcon = when (currentStep?.iconType) {
                        "turn_right" -> Icons.Default.TurnRight
                        "turn_left" -> Icons.Default.TurnLeft
                        "roundabout" -> Icons.Default.Cached
                        "destination" -> Icons.Default.Flag
                        else -> Icons.Default.Straight
                    }
                    Icon(
                        imageVector = navIcon,
                        contentDescription = null,
                        tint = Color(0xFF003829),
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Dans ${currentStep?.distanceRemainingMeters ?: 200} m",
                        color = AberMint,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = currentStep?.instruction ?: "Continuer tout droit",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        maxLines = 1
                    )
                    Text(
                        text = currentStep?.subInstruction ?: ride.pickupAddress,
                        color = TextSecondaryDark,
                        fontSize = 12.sp,
                        maxLines = 1
                    )
                }

                // Speed Limit Indicator
                Surface(
                    shape = CircleShape,
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(2.dp, AberRed),
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "$speedKmh",
                            color = Color.Black,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }

        // BOTTOM ACTION COCKPIT
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Waiting Timer Notice (When ARRIVED_PICKUP)
            AnimatedVisibility(visible = status == DriverStatus.ARRIVED_PICKUP) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF1E293B),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AberGold.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.HourglassBottom,
                                contentDescription = null,
                                tint = AberGold,
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(
                                    text = "Passager notifié de votre arrivée",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Frais d'attente après 2:00 min",
                                    color = TextSecondaryDark,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        val minutes = waitTimerSeconds / 60
                        val seconds = waitTimerSeconds % 60
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = AberGold.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = String.format("%02d:%02d", minutes, seconds),
                                color = AberGold,
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // Main Active Ride Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF141C28)),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Header: Status title + Fare estimate
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val stateLabel = when (status) {
                                DriverStatus.EN_ROUTE_PICKUP -> "Trajet vers prise en charge"
                                DriverStatus.ARRIVED_PICKUP -> "Au point de rencontre"
                                DriverStatus.IN_TRIP -> "Course en cours vers destination"
                                else -> "Navigation"
                            }
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(AberMint)
                            )
                            Text(
                                text = stateLabel,
                                color = AberMint,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = String.format("%.2f €", ride.estimatedFare),
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    // Passenger Details & Contact Shortcuts
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(AberMint, AberTeal))),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = ride.passengerName.take(1),
                                color = Color.Black,
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = ride.passengerName,
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = AberGold,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = "${ride.passengerRating}",
                                    color = TextPrimaryDark,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "• ${ride.category.displayName}",
                                    color = TextMutedDark,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        // Chat Button
                        IconButton(
                            onClick = onOpenChat,
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.08f))
                                .testTag("open_chat_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChatBubbleOutline,
                                contentDescription = "Messagerie passager",
                                tint = AberMint,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Call Button
                        IconButton(
                            onClick = { /* Call shortcut */ },
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.08f))
                                .testTag("call_passenger_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = "Appeler le passager",
                                tint = AberMint,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Active Target Address
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF1E2736)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = if (status == DriverStatus.IN_TRIP) Icons.Default.Place else Icons.Default.Navigation,
                                contentDescription = null,
                                tint = if (status == DriverStatus.IN_TRIP) AberGold else AberMint,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = if (status == DriverStatus.IN_TRIP) ride.dropoffAddress else ride.pickupAddress,
                                color = TextPrimaryDark,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Contextual Action Button
                    when (status) {
                        DriverStatus.EN_ROUTE_PICKUP -> {
                            Button(
                                onClick = onArrivedAtPickup,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp)
                                    .testTag("arrived_pickup_button"),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AberMint,
                                    contentColor = Color(0xFF003829)
                                )
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PinDrop,
                                        contentDescription = null
                                    )
                                    Text(
                                        text = "JE SUIS ARRIVÉ AU POINT",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }
                        DriverStatus.ARRIVED_PICKUP -> {
                            Button(
                                onClick = onStartTrip,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp)
                                    .testTag("start_trip_button"),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AberMint,
                                    contentColor = Color(0xFF003829)
                                )
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = null
                                    )
                                    Text(
                                        text = "DÉMARRER LA COURSE",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }
                        DriverStatus.IN_TRIP -> {
                            Button(
                                onClick = onCompleteTrip,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp)
                                    .testTag("complete_trip_button"),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF10B981),
                                    contentColor = Color.White
                                )
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null
                                    )
                                    Text(
                                        text = "TERMINER LA COURSE",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }
                        else -> {}
                    }

                    // Emergency SOS & Options Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { showSosDialog = true },
                            colors = ButtonDefaults.textButtonColors(contentColor = AberRed)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Bouton d'urgence SOS",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        TextButton(
                            onClick = { showCancelDialog = true },
                            colors = ButtonDefaults.textButtonColors(contentColor = TextMutedDark)
                        ) {
                            Text(
                                text = "Options / Annulation",
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }

    // SOS Emergency Alert Dialog
    if (showSosDialog) {
        AlertDialog(
            onDismissRequest = { showSosDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = AberRed,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "Assistance d'urgence 112",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            text = {
                Text(
                    text = "En cas de danger ou d'incident, un appel automatique vers les services de secours et la transmission de votre position GPS en temps réel sera effectuée.",
                    color = TextSecondaryDark
                )
            },
            confirmButton = {
                Button(
                    onClick = { showSosDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = AberRed)
                ) {
                    Text("Appeler le 112 (Urgences)")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSosDialog = false }) {
                    Text("Annuler", color = TextSecondaryDark)
                }
            },
            containerColor = Color(0xFF1E293B)
        )
    }

    // Cancel Ride Dialog
    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = {
                Text(
                    text = "Annuler la course en cours ?",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            text = {
                Text(
                    text = "Si vous annulez, votre taux d'acceptation pourrait être impacté sauf en cas de motif valable (panne, passager introuvable après 5 min).",
                    color = TextSecondaryDark
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showCancelDialog = false
                        onCompleteTrip()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AberRed)
                ) {
                    Text("Confirmer l'annulation")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) {
                    Text("Poursuivre la course", color = AberMint)
                }
            },
            containerColor = Color(0xFF1E293B)
        )
    }
}

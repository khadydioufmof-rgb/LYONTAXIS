package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.*
import com.example.ui.components.AberDarkButton
import com.example.ui.components.AberDriverCard
import com.example.ui.components.LeafletMap
import com.example.ui.components.AberPrimaryButton
import com.example.ui.theme.*
import kotlinx.coroutines.launch

enum class ActiveRideSheetMode {
  DRIVER_DETAILS,
  ROUTE_TRAFFIC_INSPECT,
  CHAT_EXPANDED
}

enum class TrafficSeverity {
  CLEAR,
  MODERATE,
  CONGESTED,
  OPTIMIZED
}

data class LiveTrafficEtaUpdate(
  val id: String,
  val etaMinutes: Int,
  val arrivalClock: String,
  val delayDeltaMinutes: Int, // +2, 0, -1
  val headline: String,
  val detail: String,
  val severity: TrafficSeverity,
  val speedAvgKmh: Int,
  val timestamp: String = "Just now"
)

data class RouteSegmentStep(
  val stepNumber: Int,
  val instruction: String,
  val streetName: String,
  val distance: String,
  val trafficLevel: String, // "fast", "moderate", "slow"
  val speedEstimate: String
)

@Composable
fun ActiveRideTrackingScreen(
  booking: RideBooking,
  messages: List<ChatMessage> = emptyList(),
  isDriverTyping: Boolean = false,
  onSendMessage: (String) -> Unit = {},
  onSendVoiceNote: () -> Unit = {},
  onShareLocation: () -> Unit = {},
  onOpenChat: () -> Unit = {},
  onCallDriver: () -> Unit = {},
  onCancelRequest: () -> Unit = {},
  onCompleteRide: () -> Unit = {},
  onMinimize: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  var sheetMode by remember { mutableStateOf(ActiveRideSheetMode.DRIVER_DETAILS) }
  var chatInputText by remember { mutableStateOf("") }
  var mapPanOffset by remember { mutableStateOf(Offset.Zero) }
  var mapZoomScale by remember { mutableFloatStateOf(1.0f) }
  var isTrafficVisible by remember { mutableStateOf(true) }

  // Dynamic Real-Time Live Traffic ETA State Engine
  val liveEtaUpdates = remember {
    listOf(
      LiveTrafficEtaUpdate(
        id = "eta_1",
        etaMinutes = 3,
        arrivalClock = "17:38",
        delayDeltaMinutes = 0,
        headline = "⚡ Itinéraire optimal actif",
        detail = "Axe Rue de la République dégagé. Chauffeur à 42 km/h.",
        severity = TrafficSeverity.OPTIMIZED,
        speedAvgKmh = 42
      ),
      LiveTrafficEtaUpdate(
        id = "eta_2",
        etaMinutes = 5,
        arrivalClock = "17:40",
        delayDeltaMinutes = 2,
        headline = "🚦 Ralentissement Pont Lafayette (+2 min)",
        detail = "Trafic plus dense vers les quais du Rhône. Itinéraire réajusté.",
        severity = TrafficSeverity.MODERATE,
        speedAvgKmh = 24
      ),
      LiveTrafficEtaUpdate(
        id = "eta_3",
        etaMinutes = 2,
        arrivalClock = "17:37",
        delayDeltaMinutes = -1,
        headline = "🟢 Trafic fluide en approche (-1 min)",
        detail = "Onde verte sur le Cours Lafayette. Gain estimé ~1 min.",
        severity = TrafficSeverity.CLEAR,
        speedAvgKmh = 54
      ),
      LiveTrafficEtaUpdate(
        id = "eta_4",
        etaMinutes = 1,
        arrivalClock = "17:36",
        delayDeltaMinutes = 0,
        headline = "🚗 Chauffeur à 300m • Préparez-vous",
        detail = "Le taxi s'engage dans votre rue de prise en charge.",
        severity = TrafficSeverity.OPTIMIZED,
        speedAvgKmh = 35
      )
    )
  }

  var etaUpdateIndex by remember { mutableIntStateOf(0) }
  val currentEtaUpdate = liveEtaUpdates[etaUpdateIndex % liveEtaUpdates.size]
  var isEtaSnackbarVisible by remember { mutableStateOf(true) }
  var isRecomputingLiveEta by remember { mutableStateOf(false) }

  // SOS Emergency & Location Sharing State
  val context = LocalContext.current
  var showSosEmergencyDialog by remember { mutableStateOf(false) }
  var isEmergencyAlertActive by remember { mutableStateOf(false) }
  var emergencyDispatchedNotification by remember { mutableStateOf<String?>(null) }

  // Periodic real-time traffic updates ticker (polling every 8.5 seconds)
  LaunchedEffect(Unit) {
    while (true) {
      kotlinx.coroutines.delay(8500)
      etaUpdateIndex = (etaUpdateIndex + 1) % liveEtaUpdates.size
      isEtaSnackbarVisible = true
    }
  }

  val coroutineScope = rememberCoroutineScope()
  val listState = rememberLazyListState()

  // Auto-scroll to bottom on incoming message or typing state change
  LaunchedEffect(messages.size, isDriverTyping) {
    if (messages.isNotEmpty()) {
      listState.animateScrollToItem(messages.size - 1 + if (isDriverTyping) 1 else 0)
    }
  }

  val lastDriverMessage = remember(messages) {
    messages.findLast { !it.isFromUser }
  }

  val routeSteps = remember {
    listOf(
      RouteSegmentStep(
        stepNumber = 1,
        instruction = "Remonter la Rue de la République",
        streetName = "Rue de la République",
        distance = "0.8 km",
        trafficLevel = "fast",
        speedEstimate = "45 km/h (Fluide)"
      ),
      RouteSegmentStep(
        stepNumber = 2,
        instruction = "Tourner à droite vers le Pont Lafayette",
        streetName = "Pont Lafayette",
        distance = "1.6 km",
        trafficLevel = "moderate",
        speedEstimate = "25 km/h (Ralentissement +2m)"
      ),
      RouteSegmentStep(
        stepNumber = 3,
        instruction = "Continuer sur le Cours Lafayette",
        streetName = "Cours Lafayette",
        distance = "1.8 km",
        trafficLevel = "fast",
        speedEstimate = "50 km/h (Fluide)"
      ),
      RouteSegmentStep(
        stepNumber = 4,
        instruction = "Rejoindre le Boulevard Vivier-Merle",
        streetName = "Boulevard Marius Vivier-Merle",
        distance = "0.6 km",
        trafficLevel = "slow",
        speedEstimate = "15 km/h (Feu tricolore +1m)"
      )
    )
  }

  Box(modifier = modifier.fillMaxSize()) {
    // 1. Live Vector Map with multi-touch Pinch to Zoom, Pan Gestures, and Dynamic Traffic Layer
    LeafletMap(
      pickup = booking.pickupLocation,
      dropoff = booking.dropoffLocation,
      modifier = Modifier.fillMaxSize(),
      interactive = true
    )

    // 2. Top Header Overlay with Close/Back, Status Pill & Quick Mode Switches
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .statusBarsPadding()
        .padding(horizontal = 16.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Surface(
        shape = CircleShape,
        color = Color.White,
        shadowElevation = 6.dp,
        modifier = Modifier.size(44.dp)
      ) {
        IconButton(
          onClick = onMinimize,
          modifier = Modifier.testTag("active_ride_close_button")
        ) {
          Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Réduire",
            tint = AberDark
          )
        }
      }

      Surface(
        shape = RoundedCornerShape(20.dp),
        color = LyonBluePrimary,
        shadowElevation = 6.dp
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          val infiniteTransition = rememberInfiniteTransition(label = "pulse")
          val pulseAlpha by infiniteTransition.animateFloat(
            initialValue = 0.4f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
              animation = tween(800, easing = LinearEasing),
              repeatMode = RepeatMode.Reverse
            ),
            label = "pulse_alpha"
          )
          Surface(
            shape = CircleShape,
            color = Color.White.copy(alpha = pulseAlpha),
            modifier = Modifier.size(8.dp)
          ) {}
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Le chauffeur arrive • 2 min",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
          )
        }
      }

      // Quick Chat Toggle Button with badge
      Surface(
        shape = CircleShape,
        color = if (sheetMode == ActiveRideSheetMode.CHAT_EXPANDED) AberDark else Color.White,
        shadowElevation = 6.dp,
        modifier = Modifier.size(44.dp)
      ) {
        Box(contentAlignment = Alignment.Center) {
          IconButton(
            onClick = {
              sheetMode = if (sheetMode == ActiveRideSheetMode.CHAT_EXPANDED) {
                ActiveRideSheetMode.DRIVER_DETAILS
              } else {
                ActiveRideSheetMode.CHAT_EXPANDED
              }
            },
            modifier = Modifier.testTag("active_ride_chat_toggle_button")
          ) {
            Icon(
              imageVector = if (sheetMode == ActiveRideSheetMode.CHAT_EXPANDED) Icons.Default.Map else Icons.Default.ChatBubble,
              contentDescription = if (sheetMode == ActiveRideSheetMode.CHAT_EXPANDED) "Afficher la carte" else "Ouvrir le chat",
              tint = if (sheetMode == ActiveRideSheetMode.CHAT_EXPANDED) Color.White else LyonBluePrimary,
              modifier = Modifier.size(20.dp)
            )
          }

          if (sheetMode != ActiveRideSheetMode.CHAT_EXPANDED && messages.isNotEmpty()) {
            Surface(
              shape = CircleShape,
              color = AberRed,
              modifier = Modifier
                .size(10.dp)
                .align(Alignment.TopEnd)
                .padding(top = 4.dp, end = 4.dp)
            ) {}
          }
        }
      }
    }

    // 3. Floating Mode Tabs Pill: "Driver Details" vs "Inspect Route & Traffic"
    if (sheetMode != ActiveRideSheetMode.CHAT_EXPANDED) {
      Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 8.dp,
        border = BorderStroke(1.dp, AberBorder),
        modifier = Modifier
          .align(Alignment.TopCenter)
          .statusBarsPadding()
          .padding(top = 64.dp)
      ) {
        Row(
          modifier = Modifier.padding(4.dp),
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          // Driver Overview Tab
          Surface(
            shape = RoundedCornerShape(20.dp),
            color = if (sheetMode == ActiveRideSheetMode.DRIVER_DETAILS) LyonBluePrimary else Color.Transparent,
            modifier = Modifier.clickable { sheetMode = ActiveRideSheetMode.DRIVER_DETAILS }
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = if (sheetMode == ActiveRideSheetMode.DRIVER_DETAILS) Color.White else AberDark,
                modifier = Modifier.size(15.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "Chauffeur",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (sheetMode == ActiveRideSheetMode.DRIVER_DETAILS) Color.White else AberDark
              )
            }
          }

          // Inspect Route & Traffic Tab
          Surface(
            shape = RoundedCornerShape(20.dp),
            color = if (sheetMode == ActiveRideSheetMode.ROUTE_TRAFFIC_INSPECT) LyonBluePrimary else Color.Transparent,
            modifier = Modifier.clickable { sheetMode = ActiveRideSheetMode.ROUTE_TRAFFIC_INSPECT }
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Default.Traffic,
                contentDescription = null,
                tint = if (sheetMode == ActiveRideSheetMode.ROUTE_TRAFFIC_INSPECT) Color.White else AberDark,
                modifier = Modifier.size(15.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "Itinéraire & Trafic",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (sheetMode == ActiveRideSheetMode.ROUTE_TRAFFIC_INSPECT) Color.White else AberDark
              )
            }
          }
        }
      }
    }

    // 4. Quick Camera Presets Bar when Inspecting Route
    if (sheetMode == ActiveRideSheetMode.ROUTE_TRAFFIC_INSPECT) {
      Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color.White.copy(alpha = 0.95f),
        shadowElevation = 6.dp,
        border = BorderStroke(1.dp, AberBorder),
        modifier = Modifier
          .align(Alignment.TopCenter)
          .statusBarsPadding()
          .padding(top = 118.dp, start = 16.dp, end = 16.dp)
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          CameraChip(
            label = "Aperçu trajet 🗺️",
            onClick = {
              mapPanOffset = Offset.Zero
              mapZoomScale = 1.0f
            }
          )
          CameraChip(
            label = "Suivre taxi 🚗",
            onClick = {
              mapPanOffset = Offset(20f, -40f)
              mapZoomScale = 1.8f
            }
          )
          CameraChip(
            label = "Destination 🏁",
            onClick = {
              mapPanOffset = Offset(-140f, 80f)
              mapZoomScale = 2.0f
            }
          )
        }
      }
    }

    // 5. Middle Floating Live Chat Alert (When in Driver mode & there's a recent message)
    AnimatedVisibility(
      visible = sheetMode == ActiveRideSheetMode.DRIVER_DETAILS && lastDriverMessage != null,
      enter = fadeIn() + slideInVertically(initialOffsetY = { -40 }),
      exit = fadeOut() + slideOutVertically(targetOffsetY = { -40 }),
      modifier = Modifier
        .fillMaxWidth()
        .statusBarsPadding()
        .padding(top = 118.dp, start = 16.dp, end = 16.dp)
        .align(Alignment.TopCenter)
    ) {
      Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Surface(
          shape = RoundedCornerShape(18.dp),
          color = Color.White,
          shadowElevation = 10.dp,
          border = BorderStroke(1.dp, LyonBluePrimary.copy(alpha = 0.3f)),
          modifier = Modifier
            .fillMaxWidth()
            .clickable { sheetMode = ActiveRideSheetMode.CHAT_EXPANDED }
            .testTag("floating_driver_message_banner")
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Surface(
              shape = CircleShape,
              color = LyonBlueLight,
              border = BorderStroke(1.dp, LyonBluePrimary),
              modifier = Modifier.size(36.dp)
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(
                  imageVector = Icons.Default.Person,
                  contentDescription = null,
                  tint = LyonBluePrimary,
                  modifier = Modifier.size(20.dp)
                )
              }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = booking.driver.name,
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                  color = AberDark
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = lastDriverMessage?.timestamp ?: "Maintenant",
                  fontSize = 10.sp,
                  color = AberGrayText
                )
              }
              Text(
                text = lastDriverMessage?.text ?: "",
                fontSize = 13.sp,
                color = AberDark,
                maxLines = 1,
                fontWeight = FontWeight.Medium
              )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Surface(
              shape = RoundedCornerShape(12.dp),
              color = LyonBlueLight,
              modifier = Modifier.padding(2.dp)
            ) {
              Text(
                text = "Répondre",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = LyonBluePrimary,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
              )
            }
          }
        }
      }
    }

    // 6. Dynamic Real-Time Live Traffic ETA Toast / Snackbar Notification
    if (sheetMode != ActiveRideSheetMode.CHAT_EXPANDED) {
      val topPadding = if (sheetMode == ActiveRideSheetMode.ROUTE_TRAFFIC_INSPECT) {
        168.dp
      } else if (lastDriverMessage != null) {
        188.dp
      } else {
        122.dp
      }

      AnimatedVisibility(
        visible = isEtaSnackbarVisible,
        enter = fadeIn(tween(350)) + slideInVertically(initialOffsetY = { -60 }, animationSpec = spring(stiffness = Spring.StiffnessMediumLow)),
        exit = fadeOut(tween(250)) + slideOutVertically(targetOffsetY = { -60 }),
        modifier = Modifier
          .fillMaxWidth()
          .statusBarsPadding()
          .padding(top = topPadding, start = 14.dp, end = 14.dp)
          .align(Alignment.TopCenter)
      ) {
        LiveTrafficEtaSnackbar(
          etaUpdate = currentEtaUpdate,
          isRecomputing = isRecomputingLiveEta,
          onDismiss = { isEtaSnackbarVisible = false },
          onInspectRoute = { sheetMode = ActiveRideSheetMode.ROUTE_TRAFFIC_INSPECT },
          onRecalculate = {
            if (!isRecomputingLiveEta) {
              coroutineScope.launch {
                isRecomputingLiveEta = true
                kotlinx.coroutines.delay(700)
                etaUpdateIndex = (etaUpdateIndex + 1) % liveEtaUpdates.size
                isRecomputingLiveEta = false
              }
            }
          }
        )
      }

      // Compact floating pill shown when user dismisses the full snackbar toast
      AnimatedVisibility(
        visible = !isEtaSnackbarVisible,
        enter = fadeIn(tween(200)) + scaleIn(initialScale = 0.8f),
        exit = fadeOut(tween(150)) + scaleOut(targetScale = 0.8f),
        modifier = Modifier
          .statusBarsPadding()
          .padding(top = topPadding, end = 16.dp)
          .align(Alignment.TopEnd)
      ) {
        LiveEtaFloatingPill(
          etaUpdate = currentEtaUpdate,
          onClick = { isEtaSnackbarVisible = true }
        )
      }
    }

    // 7. Dynamic Bottom Sheets (Driver Details, Route & Traffic Inspection, or Real-Time Chat)
    AnimatedContent(
      targetState = sheetMode,
      transitionSpec = {
        slideInVertically(
          initialOffsetY = { it },
          animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
        ) togetherWith slideOutVertically(
          targetOffsetY = { it },
          animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
        )
      },
      modifier = Modifier
        .fillMaxWidth()
        .align(Alignment.BottomCenter),
      label = "tracking_sheet_mode_transition"
    ) { mode ->
      when (mode) {
        ActiveRideSheetMode.DRIVER_DETAILS -> {
          // --- 1. Standard Driver & Ride Details View ---
          Surface(
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            color = Color.White,
            shadowElevation = 20.dp,
            modifier = Modifier
              .fillMaxWidth()
              .navigationBarsPadding()
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
            ) {
              // Drag handle hint
              Box(
                modifier = Modifier
                  .align(Alignment.CenterHorizontally)
                  .padding(bottom = 12.dp)
                  .size(width = 38.dp, height = 4.dp)
                  .clip(RoundedCornerShape(2.dp))
                  .background(AberBorder)
              )

              // Driver Profile Card
              AberDriverCard(
                driver = booking.driver,
                onChatClick = { sheetMode = ActiveRideSheetMode.CHAT_EXPANDED },
                onCallClick = onCallDriver,
                showActions = true
              )

              Spacer(modifier = Modifier.height(12.dp))

              // Route Overview with Inspect Shortcut Button
              Surface(
                shape = RoundedCornerShape(16.dp),
                color = AberGrayLight,
                border = BorderStroke(1.dp, AberBorder),
                modifier = Modifier.fillMaxWidth()
              ) {
                Column(modifier = Modifier.padding(14.dp)) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = CircleShape, color = LyonBluePrimary, modifier = Modifier.size(8.dp)) {}
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                      text = booking.pickupLocation.title,
                      fontSize = 13.sp,
                      fontWeight = FontWeight.Medium,
                      color = AberDark
                    )
                  }

                  if (booking.intermediateStops.isNotEmpty()) {
                    booking.intermediateStops.forEachIndexed { sIdx, stop ->
                      Box(
                        modifier = Modifier
                          .padding(start = 3.dp, top = 2.dp, bottom = 2.dp)
                          .size(width = 2.dp, height = 12.dp)
                          .background(AberAmber)
                      )
                      Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = CircleShape, color = AberAmber, modifier = Modifier.size(8.dp)) {}
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                          text = "Arrêt ${sIdx + 1}: ${stop.title}",
                          fontSize = 12.5.sp,
                          fontWeight = FontWeight.SemiBold,
                          color = AberDark
                        )
                      }
                    }
                  }

                  Box(
                    modifier = Modifier
                      .padding(start = 3.dp, top = 2.dp, bottom = 2.dp)
                      .size(width = 2.dp, height = 12.dp)
                      .background(AberBorder)
                  )

                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = AberRed, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                      text = booking.dropoffLocation.title,
                      fontSize = 13.sp,
                      fontWeight = FontWeight.Bold,
                      color = AberDark
                    )
                  }

                  HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = AberBorder)

                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Text(text = "DISTANCE : ${booking.distanceKm} km", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AberGrayText)
                    Text(text = "DURÉE : ${booking.durationMin} min", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AberGrayText)
                    Text(text = "PRIX : ${"%.2f".format(booking.fare)} €", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = LyonBluePrimary)
                  }
                }
              }

              Spacer(modifier = Modifier.height(14.dp))

              // Action Buttons
              AberPrimaryButton(
                text = "Terminer la course & Noter",
                onClick = onCompleteRide,
                icon = Icons.Default.CheckCircle,
                testTag = "simulate_complete_trip_button"
              )

              Spacer(modifier = Modifier.height(8.dp))

              AberDarkButton(
                text = "Annuler la demande",
                onClick = onCancelRequest,
                testTag = "cancel_request_button"
              )
            }
          }
        }

        ActiveRideSheetMode.ROUTE_TRAFFIC_INSPECT -> {
          // --- 2. Route & Traffic Conditions Inspector Panel ---
          Surface(
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            color = Color.White,
            shadowElevation = 22.dp,
            modifier = Modifier
              .fillMaxWidth()
              .fillMaxHeight(0.55f)
              .navigationBarsPadding()
              .testTag("route_traffic_inspector_panel")
          ) {
            Column(
              modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp, vertical = 14.dp)
            ) {
              // Drag handle
              Box(
                modifier = Modifier
                  .align(Alignment.CenterHorizontally)
                  .padding(bottom = 8.dp)
                  .size(width = 40.dp, height = 4.dp)
                  .clip(RoundedCornerShape(2.dp))
                  .background(AberBorder)
                  .clickable { sheetMode = ActiveRideSheetMode.DRIVER_DETAILS }
              )

              // Header: Title and Traffic Legend
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column {
                  Text(
                    text = "Itinéraire & Trafic en direct",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = AberDark
                  )
                  Text(
                    text = "Pincez ou touchez la carte pour inspecter",
                    fontSize = 11.sp,
                    color = AberGrayText
                  )
                }

                Surface(
                  shape = RoundedCornerShape(12.dp),
                  color = LyonBlueLight
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Icon(
                      imageVector = Icons.Default.Check,
                      contentDescription = null,
                      tint = LyonBluePrimary,
                      modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                      text = "Itinéraire le plus rapide",
                      fontSize = 11.sp,
                      fontWeight = FontWeight.Bold,
                      color = LyonBluePrimary
                    )
                  }
                }
              }

              Spacer(modifier = Modifier.height(10.dp))

              // Traffic Legend Bar
              Surface(
                shape = RoundedCornerShape(12.dp),
                color = AberGrayLight,
                modifier = Modifier.fillMaxWidth()
              ) {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                  horizontalArrangement = Arrangement.SpaceEvenly,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  LegendDot(color = Color(0xFF00C853), label = "Fluide (45+ km/h)")
                  LegendDot(color = Color(0xFFFF9100), label = "Modéré (25 km/h)")
                  LegendDot(color = Color(0xFFE53935), label = "Ralentissement (<15 km/h)")
                }
              }

              Spacer(modifier = Modifier.height(12.dp))

              // Step-by-Step Route Inspection List
              Text(
                text = "Points de passage & État du trafic",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = AberDark
              )

              Spacer(modifier = Modifier.height(6.dp))

              LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                items(routeSteps) { step ->
                  RouteStepCard(step = step)
                }
              }

              Spacer(modifier = Modifier.height(10.dp))

              AberPrimaryButton(
                text = "Retour aux infos chauffeur",
                onClick = { sheetMode = ActiveRideSheetMode.DRIVER_DETAILS },
                icon = Icons.Default.ArrowBack
              )
            }
          }
        }

        ActiveRideSheetMode.CHAT_EXPANDED -> {
          // --- 3. Full-Screen Messaging Interface ---
          Surface(
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            color = AberBackground,
            shadowElevation = 24.dp,
            modifier = Modifier
              .fillMaxWidth()
              .fillMaxHeight(0.82f)
              .navigationBarsPadding()
              .testTag("active_ride_expanded_chat_panel")
          ) {
            Column(modifier = Modifier.fillMaxSize()) {
              // Chat Header
              Surface(
                color = Color.White,
                shadowElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
              ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                  // Drag handle
                  Box(
                    modifier = Modifier
                      .align(Alignment.CenterHorizontally)
                      .padding(top = 8.dp, bottom = 4.dp)
                      .size(width = 40.dp, height = 4.dp)
                      .clip(RoundedCornerShape(2.dp))
                      .background(AberBorder)
                      .clickable { sheetMode = ActiveRideSheetMode.DRIVER_DETAILS }
                  )

                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    // Driver Avatar with Online Indicator
                    Box {
                      Surface(
                        shape = CircleShape,
                        color = LyonBlueLight,
                        border = BorderStroke(1.5.dp, LyonBluePrimary),
                        modifier = Modifier.size(44.dp)
                      ) {
                        Box(contentAlignment = Alignment.Center) {
                          Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = LyonBluePrimary,
                            modifier = Modifier.size(24.dp)
                          )
                        }
                      }
                      Surface(
                        shape = CircleShape,
                        color = LyonBluePrimary,
                        border = BorderStroke(1.5.dp, Color.White),
                        modifier = Modifier
                          .size(12.dp)
                          .align(Alignment.BottomEnd)
                      ) {}
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                      Text(
                        text = booking.driver.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = AberDark
                      )
                      Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isDriverTyping) {
                          Text(
                            text = "écrit...",
                            fontSize = 11.sp,
                            color = LyonBluePrimary,
                            fontWeight = FontWeight.Bold
                          )
                        } else {
                          Text(
                            text = "${booking.driver.carModel} • ${booking.driver.licensePlate}",
                            fontSize = 11.sp,
                            color = AberGrayText,
                            fontWeight = FontWeight.Medium
                          )
                        }
                      }
                    }

                    // Quick Action Call
                    IconButton(
                      onClick = onCallDriver,
                      modifier = Modifier.testTag("tracking_chat_call_button")
                    ) {
                      Surface(
                        shape = CircleShape,
                        color = LyonBlueLight,
                        modifier = Modifier.size(36.dp)
                      ) {
                        Box(contentAlignment = Alignment.Center) {
                          Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "Appeler chauffeur",
                            tint = LyonBluePrimary,
                            modifier = Modifier.size(18.dp)
                          )
                        }
                      }
                    }

                    // Minimize / Close Chat
                    IconButton(
                      onClick = { sheetMode = ActiveRideSheetMode.DRIVER_DETAILS },
                      modifier = Modifier.testTag("tracking_chat_minimize_button")
                    ) {
                      Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Réduire le chat",
                        tint = AberDark
                      )
                    }
                  }
                }
              }

              // Real-Time Message Thread
              LazyColumn(
                state = listState,
                modifier = Modifier
                  .weight(1f)
                  .padding(horizontal = 14.dp),
                contentPadding = PaddingValues(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
              ) {
                item {
                  Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                  ) {
                    Surface(
                      shape = RoundedCornerShape(12.dp),
                      color = AberGrayLight,
                      modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                      Text(
                        text = "Course en cours • Départ : ${booking.pickupLocation.title}",
                        fontSize = 11.sp,
                        color = AberGrayText,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                      )
                    }
                  }
                }

                items(messages, key = { it.id }) { message ->
                  RealTimeChatBubble(message = message)
                }

                // Live Typing Indicator
                if (isDriverTyping) {
                  item {
                    Row(
                      verticalAlignment = Alignment.CenterVertically,
                      modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                    ) {
                      Surface(
                        shape = CircleShape,
                        color = LyonBlueLight,
                        modifier = Modifier.size(24.dp)
                      ) {
                        Box(contentAlignment = Alignment.Center) {
                          Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = LyonBluePrimary,
                            modifier = Modifier.size(14.dp)
                          )
                        }
                      }
                      Spacer(modifier = Modifier.width(6.dp))
                      AnimatedTypingDotsBubble()
                    }
                  }
                }
              }

              // In-Chat Quick Action Chips
              LazyRow(
                modifier = Modifier
                  .fillMaxWidth()
                  .background(Color.White)
                  .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                item {
                  QuickReplyChip(text = "📍 Partager position") {
                    onShareLocation()
                  }
                }
                item {
                  QuickReplyChip(text = "👋 Je suis juste devant") {
                    onSendMessage("J'attends juste devant l'entrée")
                  }
                }
                item {
                  QuickReplyChip(text = "🚗 Je vous vois arriver") {
                    onSendMessage("Je vous vois arriver !")
                  }
                }
                item {
                  QuickReplyChip(text = "🧥 Manteau noir") {
                    onSendMessage("Je porte un manteau noir")
                  }
                }
                item {
                  QuickReplyChip(text = "❄️ Climatisation svp") {
                    onSendMessage("Pourriez-vous mettre la climatisation svp ?")
                  }
                }
                item {
                  QuickReplyChip(text = "⏳ Prenez votre temps") {
                    onSendMessage("Pas de problème, prenez votre temps !")
                  }
                }
              }

              HorizontalDivider(color = AberBorder)

              // Bottom Real-Time Input Dock
              Surface(
                color = Color.White,
                modifier = Modifier.fillMaxWidth()
              ) {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  IconButton(
                    onClick = onShareLocation,
                    modifier = Modifier.size(36.dp).testTag("chat_share_location_btn")
                  ) {
                    Icon(
                      imageVector = Icons.Default.LocationOn,
                      contentDescription = "Partager position",
                      tint = AberRed,
                      modifier = Modifier.size(20.dp)
                    )
                  }

                  IconButton(
                    onClick = onSendVoiceNote,
                    modifier = Modifier.size(36.dp).testTag("chat_voice_note_btn")
                  ) {
                    Icon(
                      imageVector = Icons.Default.Mic,
                      contentDescription = "Message vocal",
                      tint = LyonBluePrimary,
                      modifier = Modifier.size(20.dp)
                    )
                  }

                  OutlinedTextField(
                    value = chatInputText,
                    onValueChange = { chatInputText = it },
                    placeholder = {
                      Text(
                        "Message à ${booking.driver.name.split(" ").first()}...",
                        color = AberGrayText,
                        fontSize = 13.sp
                      )
                    },
                    modifier = Modifier
                      .weight(1f)
                      .testTag("tracking_chat_input"),
                    shape = RoundedCornerShape(22.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                      focusedBorderColor = LyonBluePrimary,
                      unfocusedBorderColor = AberBorder,
                      focusedContainerColor = AberGrayLight,
                      unfocusedContainerColor = AberGrayLight
                    ),
                    maxLines = 3,
                    textStyle = LocalTextStyle.current.copy(fontSize = 13.sp)
                  )

                  Spacer(modifier = Modifier.width(6.dp))

                  Surface(
                    shape = CircleShape,
                    color = if (chatInputText.isNotBlank()) LyonBluePrimary else AberGrayLight,
                    modifier = Modifier
                      .size(42.dp)
                      .clickable(enabled = chatInputText.isNotBlank()) {
                        onSendMessage(chatInputText)
                        chatInputText = ""
                      }
                      .testTag("tracking_chat_send_button")
                  ) {
                    Box(contentAlignment = Alignment.Center) {
                      Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Envoyer",
                        tint = if (chatInputText.isNotBlank()) Color.White else AberGrayText,
                        modifier = Modifier.size(18.dp)
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

    // 8. Floating 'SOS' Emergency Assistance Button
    if (sheetMode != ActiveRideSheetMode.CHAT_EXPANDED) {
      FloatingSosEmergencyButton(
        onClick = { showSosEmergencyDialog = true },
        isAlertActive = isEmergencyAlertActive,
        modifier = Modifier
          .align(Alignment.BottomEnd)
          .padding(
            end = 16.dp,
            bottom = if (sheetMode == ActiveRideSheetMode.DRIVER_DETAILS) 375.dp else 490.dp
          )
      )
    }

    // 9. Active Emergency Broadcast Alert Banner (Top of Screen)
    AnimatedVisibility(
      visible = isEmergencyAlertActive,
      enter = fadeIn(tween(300)) + slideInVertically(initialOffsetY = { -80 }),
      exit = fadeOut(tween(200)) + slideOutVertically(targetOffsetY = { -80 }),
      modifier = Modifier
        .fillMaxWidth()
        .statusBarsPadding()
        .padding(top = 10.dp, start = 12.dp, end = 12.dp)
        .align(Alignment.TopCenter)
    ) {
      EmergencyBroadcastBanner(
        message = emergencyDispatchedNotification ?: "🚨 SOS URGENCE ACTIF : Position GPS en direct & infos de course transmises aux services d'urgence (112) et à l'équipe LyonTaxis.",
        onManageClick = { showSosEmergencyDialog = true },
        onCancelSos = {
          isEmergencyAlertActive = false
          emergencyDispatchedNotification = null
        }
      )
    }

    // 10. Emergency SOS Assistance & Location Sharing Prompt Dialog
    if (showSosEmergencyDialog) {
      SosEmergencyDialog(
        booking = booking,
        isEmergencyAlertActive = isEmergencyAlertActive,
        onDismiss = { showSosEmergencyDialog = false },
        onShareLocationWithEmergency = {
          val shareText = "🚨 ALERTE SOS URGENCE - LYONTAXIS : Besoin d'assistance immédiate ! Position GPS : ${booking.pickupLocation.title} (Lyon, France - 45.7640° N, 4.8357° E). Chauffeur : ${booking.driver.name}, Véhicule : ${booking.vehicle.displayName} (Immatriculation : ${booking.driver.licensePlate}), Réf Course : #LYON-${booking.id.takeLast(4).ifEmpty { "9842" }}. Suivi GPS en direct : https://lyontaxis.fr/track/emergency-${booking.id.ifEmpty { "9842" }}"
          val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
          }
          val shareIntent = Intent.createChooser(sendIntent, "Partager la position d'urgence")
          try {
            context.startActivity(shareIntent)
          } catch (e: Exception) {
            // Handled gracefully in mock / preview environment
          }
          isEmergencyAlertActive = true
          emergencyDispatchedNotification = "✅ Position GPS et coordonnées du taxi partagées avec les secours (112) et contacts d'urgence."
          showSosEmergencyDialog = false
        },
        onCall911 = {
          val callIntent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:112")
          }
          try {
            context.startActivity(callIntent)
          } catch (e: Exception) {
            // Handled gracefully
          }
          isEmergencyAlertActive = true
          emergencyDispatchedNotification = "📞 Appel d'urgence 112 composé • Coordonnées GPS transmises."
          showSosEmergencyDialog = false
        },
        onSilentAlert = {
          isEmergencyAlertActive = true
          emergencyDispatchedNotification = "🛡️ Alerte silencieuse envoyée : Le centre de sécurité LyonTaxis 24/7 surveille la position du taxi."
          showSosEmergencyDialog = false
        },
        onCancelEmergencyAlert = {
          isEmergencyAlertActive = false
          emergencyDispatchedNotification = null
          showSosEmergencyDialog = false
        }
      )
    }
  }
}

@Composable
private fun CameraChip(
  label: String,
  onClick: () -> Unit
) {
  Surface(
    shape = RoundedCornerShape(14.dp),
    color = AberGrayLight,
    border = BorderStroke(1.dp, AberBorder),
    modifier = Modifier.clickable(onClick = onClick)
  ) {
    Text(
      text = label,
      fontSize = 11.5.sp,
      fontWeight = FontWeight.Bold,
      color = AberDark,
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
    )
  }
}

@Composable
private fun LegendDot(
  color: Color,
  label: String
) {
  Row(verticalAlignment = Alignment.CenterVertically) {
    Surface(shape = CircleShape, color = color, modifier = Modifier.size(8.dp)) {}
    Spacer(modifier = Modifier.width(4.dp))
    Text(text = label, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = AberDark)
  }
}

@Composable
private fun RouteStepCard(step: RouteSegmentStep) {
  Surface(
    shape = RoundedCornerShape(12.dp),
    color = AberGrayLight,
    border = BorderStroke(1.dp, AberBorder),
    modifier = Modifier.fillMaxWidth()
  ) {
    Row(
      modifier = Modifier.padding(10.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Surface(
        shape = CircleShape,
        color = when (step.trafficLevel) {
          "fast" -> Color(0xFF00C853)
          "moderate" -> Color(0xFFFF9100)
          else -> Color(0xFFE53935)
        },
        modifier = Modifier.size(24.dp)
      ) {
        Box(contentAlignment = Alignment.Center) {
          Text(
            text = "${step.stepNumber}",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
          )
        }
      }

      Spacer(modifier = Modifier.width(10.dp))

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = step.instruction,
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
          color = AberDark
        )
        Text(
          text = "${step.distance} • ${step.speedEstimate}",
          fontSize = 10.5.sp,
          color = when (step.trafficLevel) {
            "fast" -> AberTealPrimary
            "moderate" -> Color(0xFFE65100)
            else -> AberRed
          },
          fontWeight = FontWeight.SemiBold
        )
      }
    }
  }
}

@Composable
fun RealTimeChatBubble(message: ChatMessage) {
  val isUser = message.isFromUser

  Column(
    modifier = Modifier.fillMaxWidth(),
    horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
  ) {
    Surface(
      shape = RoundedCornerShape(
        topStart = 18.dp,
        topEnd = 18.dp,
        bottomStart = if (isUser) 18.dp else 4.dp,
        bottomEnd = if (isUser) 4.dp else 18.dp
      ),
      color = if (isUser) AberTealPrimary else Color.White,
      shadowElevation = 2.dp,
      border = if (isUser) null else BorderStroke(1.dp, AberBorder),
      modifier = Modifier.widthIn(max = 290.dp)
    ) {
      Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
        when (message.type) {
          MessageType.LOCATION -> {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Surface(
                shape = CircleShape,
                color = if (isUser) Color.White.copy(alpha = 0.25f) else AberTealLight,
                modifier = Modifier.size(32.dp)
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = if (isUser) Color.White else AberRed,
                    modifier = Modifier.size(18.dp)
                  )
                }
              }
              Spacer(modifier = Modifier.width(8.dp))
              Column {
                Text(
                  text = "Live Pickup Location",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (isUser) Color.White.copy(alpha = 0.8f) else AberTealPrimary
                )
                Text(
                  text = message.locationTitle ?: message.text,
                  fontSize = 13.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (isUser) Color.White else AberDark
                )
              }
            }
          }

          MessageType.AUDIO -> {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Surface(
                shape = CircleShape,
                color = if (isUser) Color.White.copy(alpha = 0.25f) else AberTealLight,
                modifier = Modifier.size(32.dp)
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = if (isUser) Color.White else AberTealPrimary,
                    modifier = Modifier.size(18.dp)
                  )
                }
              }
              Spacer(modifier = Modifier.width(8.dp))
              Column {
                Text(
                  text = "Voice Note (${message.audioDurationSec}s)",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (isUser) Color.White else AberDark
                )
                Text(
                  text = "▶ ılılllıılıl",
                  fontSize = 11.sp,
                  color = if (isUser) Color.White.copy(alpha = 0.8f) else AberGrayText
                )
              }
            }
          }

          else -> {
            Text(
              text = message.text,
              fontSize = 13.5.sp,
              color = if (isUser) Color.White else AberDark,
              lineHeight = 18.sp
            )
          }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
          modifier = Modifier.align(Alignment.End),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = message.timestamp,
            fontSize = 10.sp,
            color = if (isUser) Color.White.copy(alpha = 0.75f) else AberGrayText
          )
          if (isUser) {
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
              imageVector = if (message.status == MessageStatus.READ) Icons.Default.DoneAll else Icons.Default.Done,
              contentDescription = null,
              tint = if (message.status == MessageStatus.READ) Color.White else Color.White.copy(alpha = 0.6f),
              modifier = Modifier.size(12.dp)
            )
          }
        }
      }
    }
  }
}

@Composable
fun QuickReplyChip(text: String, onClick: () -> Unit) {
  Surface(
    shape = RoundedCornerShape(16.dp),
    color = AberGrayLight,
    border = BorderStroke(1.dp, AberBorder),
    modifier = Modifier.clickable(onClick = onClick)
  ) {
    Text(
      text = text,
      fontSize = 11.sp,
      fontWeight = FontWeight.SemiBold,
      color = AberDark,
      modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
    )
  }
}

@Composable
fun AnimatedTypingDotsBubble() {
  val infiniteTransition = rememberInfiniteTransition(label = "dots")
  val dot1Alpha by infiniteTransition.animateFloat(
    initialValue = 0.2f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(tween(600, 0, easing = LinearEasing), RepeatMode.Reverse),
    label = "dot1"
  )
  val dot2Alpha by infiniteTransition.animateFloat(
    initialValue = 0.2f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(tween(600, 200, easing = LinearEasing), RepeatMode.Reverse),
    label = "dot2"
  )
  val dot3Alpha by infiniteTransition.animateFloat(
    initialValue = 0.2f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(tween(600, 400, easing = LinearEasing), RepeatMode.Reverse),
    label = "dot3"
  )

  Surface(
    shape = RoundedCornerShape(14.dp),
    color = Color.White,
    border = BorderStroke(1.dp, AberBorder),
    modifier = Modifier.padding(4.dp)
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
      horizontalArrangement = Arrangement.spacedBy(4.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Surface(shape = CircleShape, color = LyonBluePrimary.copy(alpha = dot1Alpha), modifier = Modifier.size(6.dp)) {}
      Surface(shape = CircleShape, color = LyonBluePrimary.copy(alpha = dot2Alpha), modifier = Modifier.size(6.dp)) {}
      Surface(shape = CircleShape, color = LyonBluePrimary.copy(alpha = dot3Alpha), modifier = Modifier.size(6.dp)) {}
    }
  }
}

@Composable
fun LiveTrafficEtaSnackbar(
  etaUpdate: LiveTrafficEtaUpdate,
  isRecomputing: Boolean,
  onDismiss: () -> Unit,
  onInspectRoute: () -> Unit,
  onRecalculate: () -> Unit,
  modifier: Modifier = Modifier
) {
  val severityColor = when (etaUpdate.severity) {
    TrafficSeverity.CLEAR -> Color(0xFF00C853)
    TrafficSeverity.OPTIMIZED -> LyonBluePrimary
    TrafficSeverity.MODERATE -> Color(0xFFFF9100)
    TrafficSeverity.CONGESTED -> Color(0xFFE53935)
  }

  val infiniteTransition = rememberInfiniteTransition(label = "eta_pulse")
  val pulseAlpha by infiniteTransition.animateFloat(
    initialValue = 0.35f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(tween(750, easing = LinearEasing), RepeatMode.Reverse),
    label = "pulse_radar"
  )

  val rotationAngle by animateFloatAsState(
    targetValue = if (isRecomputing) 360f else 0f,
    animationSpec = if (isRecomputing) {
      infiniteRepeatable(tween(800, easing = LinearEasing), RepeatMode.Restart)
    } else {
      tween(300)
    },
    label = "spin_refresh"
  )

  Surface(
    shape = RoundedCornerShape(20.dp),
    color = Color.White,
    shadowElevation = 12.dp,
    border = BorderStroke(1.dp, severityColor.copy(alpha = 0.45f)),
    modifier = modifier
      .fillMaxWidth()
      .testTag("live_traffic_eta_snackbar")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp)
    ) {
      // 1. Top Bar: Live Status Dot, ETA & Clock, Badge & Dismiss Button
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Leading radar icon
        Surface(
          shape = CircleShape,
          color = severityColor.copy(alpha = 0.15f),
          border = BorderStroke(1.5.dp, severityColor.copy(alpha = pulseAlpha)),
          modifier = Modifier.size(36.dp)
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
              imageVector = when (etaUpdate.severity) {
                TrafficSeverity.CONGESTED -> Icons.Default.Warning
                TrafficSeverity.MODERATE -> Icons.Default.Traffic
                TrafficSeverity.CLEAR -> Icons.Default.Speed
                TrafficSeverity.OPTIMIZED -> Icons.Default.ElectricBolt
              },
              contentDescription = null,
              tint = severityColor,
              modifier = Modifier.size(20.dp)
            )
          }
        }

        Spacer(modifier = Modifier.width(10.dp))

        // ETA & Arrival Time
        Column(modifier = Modifier.weight(1f)) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Text(
              text = "Arrivée : ${etaUpdate.etaMinutes} min",
              fontSize = 15.sp,
              fontWeight = FontWeight.ExtraBold,
              color = AberDark
            )
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = AberGrayLight
            ) {
              Text(
                text = etaUpdate.arrivalClock,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = AberGrayText,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }
          }

          // Delay delta tag
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 1.dp)
          ) {
            val deltaText = when {
              etaUpdate.delayDeltaMinutes > 0 -> "+${etaUpdate.delayDeltaMinutes} min retard trafic"
              etaUpdate.delayDeltaMinutes < 0 -> "${etaUpdate.delayDeltaMinutes} min plus rapide"
              else -> "À l'heure (${etaUpdate.speedAvgKmh} km/h)"
            }
            Text(
              text = deltaText,
              fontSize = 11.sp,
              fontWeight = FontWeight.SemiBold,
              color = severityColor
            )
          }
        }

        // Action icons: Refresh / Recalculate + Close
        Row(verticalAlignment = Alignment.CenterVertically) {
          IconButton(
            onClick = onRecalculate,
            modifier = Modifier
              .size(32.dp)
              .testTag("recalculate_live_traffic_button")
          ) {
            Icon(
              imageVector = Icons.Default.Refresh,
              contentDescription = "Recalculer le trafic",
              tint = LyonBluePrimary,
              modifier = Modifier.size(18.dp)
            )
          }

          IconButton(
            onClick = onDismiss,
            modifier = Modifier
              .size(32.dp)
              .testTag("dismiss_live_traffic_snackbar")
          ) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Fermer",
              tint = AberGrayText,
              modifier = Modifier.size(18.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // 2. Traffic Headline & Real-Time Context
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = AberGrayLight,
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
          Text(
            text = if (isRecomputing) "Analyse en direct des routes lyonnaises..." else etaUpdate.headline,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = AberDark
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = if (isRecomputing) "Recalcul de l'itinéraire optimal..." else etaUpdate.detail,
            fontSize = 11.sp,
            color = AberGrayText,
            lineHeight = 15.sp
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // 3. Bottom Row: Quick Link to Inspect Route & Live Telemetry Badge
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.padding(start = 2.dp)
        ) {
          Surface(
            shape = CircleShape,
            color = severityColor,
            modifier = Modifier.size(6.dp)
          ) {}
          Spacer(modifier = Modifier.width(5.dp))
          Text(
            text = "Trafic en direct • LyonTaxis Info",
            fontSize = 10.sp,
            color = AberGrayText,
            fontWeight = FontWeight.Medium
          )
        }

        Surface(
          shape = RoundedCornerShape(12.dp),
          color = LyonBlueLight,
          modifier = Modifier
            .clickable(onClick = onInspectRoute)
            .testTag("snackbar_inspect_route_action")
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Voir l'itinéraire",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = LyonBluePrimary
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
              imageVector = Icons.Default.ArrowForward,
              contentDescription = null,
              tint = LyonBluePrimary,
              modifier = Modifier.size(12.dp)
            )
          }
        }
      }
    }
  }
}

@Composable
fun LiveEtaFloatingPill(
  etaUpdate: LiveTrafficEtaUpdate,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val severityColor = when (etaUpdate.severity) {
    TrafficSeverity.CLEAR -> Color(0xFF00C853)
    TrafficSeverity.OPTIMIZED -> LyonBluePrimary
    TrafficSeverity.MODERATE -> Color(0xFFFF9100)
    TrafficSeverity.CONGESTED -> Color(0xFFE53935)
  }

  val infiniteTransition = rememberInfiniteTransition(label = "mini_pulse")
  val pulseAlpha by infiniteTransition.animateFloat(
    initialValue = 0.4f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(tween(700, easing = LinearEasing), RepeatMode.Reverse),
    label = "mini_pulse_radar"
  )

  Surface(
    shape = RoundedCornerShape(20.dp),
    color = Color.White,
    shadowElevation = 8.dp,
    border = BorderStroke(1.dp, severityColor.copy(alpha = 0.5f)),
    modifier = modifier
      .clickable(onClick = onClick)
      .testTag("floating_live_eta_pill")
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Surface(
        shape = CircleShape,
        color = severityColor.copy(alpha = pulseAlpha),
        modifier = Modifier.size(8.dp)
      ) {}
      Spacer(modifier = Modifier.width(6.dp))
      Text(
        text = "⚡ Arrivée : ${etaUpdate.etaMinutes} min (${etaUpdate.arrivalClock})",
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = AberDark
      )
    }
  }
}

@Composable
fun FloatingSosEmergencyButton(
  onClick: () -> Unit,
  isAlertActive: Boolean,
  modifier: Modifier = Modifier
) {
  val infiniteTransition = rememberInfiniteTransition(label = "sos_pulse")
  val haloScale by infiniteTransition.animateFloat(
    initialValue = 1.0f,
    targetValue = 1.4f,
    animationSpec = infiniteRepeatable(tween(1400, easing = LinearEasing), RepeatMode.Restart),
    label = "sos_halo_scale"
  )
  val haloAlpha by infiniteTransition.animateFloat(
    initialValue = 0.65f,
    targetValue = 0.0f,
    animationSpec = infiniteRepeatable(tween(1400, easing = LinearEasing), RepeatMode.Restart),
    label = "sos_halo_alpha"
  )

  Box(
    modifier = modifier.wrapContentSize(),
    contentAlignment = Alignment.Center
  ) {
    // Pulsing radar halo around the SOS button
    Surface(
      shape = CircleShape,
      color = AberRed.copy(alpha = if (isAlertActive) haloAlpha else (haloAlpha * 0.5f)),
      modifier = Modifier
        .size(62.dp * haloScale)
    ) {}

    // Main Floating SOS Button
    Surface(
      shape = RoundedCornerShape(26.dp),
      color = if (isAlertActive) Color(0xFFB71C1C) else AberRed,
      shadowElevation = 10.dp,
      border = BorderStroke(2.dp, Color.White),
      modifier = Modifier
        .clickable(onClick = onClick)
        .testTag("floating_sos_emergency_button")
    ) {
      Row(
        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        Icon(
          imageVector = if (isAlertActive) Icons.Default.Warning else Icons.Default.Security,
          contentDescription = "Assistance SOS Urgence",
          tint = Color.White,
          modifier = Modifier.size(18.dp)
        )
        Text(
          text = if (isAlertActive) "SOS ACTIF" else "SOS",
          fontSize = 13.sp,
          fontWeight = FontWeight.ExtraBold,
          color = Color.White,
          letterSpacing = 1.sp
        )
      }
    }
  }
}

@Composable
fun EmergencyBroadcastBanner(
  message: String,
  onManageClick: () -> Unit,
  onCancelSos: () -> Unit,
  modifier: Modifier = Modifier
) {
  Surface(
    shape = RoundedCornerShape(16.dp),
    color = Color(0xFFB71C1C),
    shadowElevation = 12.dp,
    border = BorderStroke(1.5.dp, Color.White.copy(alpha = 0.8f)),
    modifier = modifier.testTag("emergency_broadcast_banner")
  ) {
    Column(modifier = Modifier.padding(12.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Surface(
          shape = CircleShape,
          color = Color.White,
          modifier = Modifier.size(28.dp)
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
              imageVector = Icons.Default.Warning,
              contentDescription = null,
              tint = AberRed,
              modifier = Modifier.size(16.dp)
            )
          }
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "ALERTE URGENCE SOS ACTIVE",
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
            letterSpacing = 0.5.sp
          )
          Text(
            text = message,
            fontSize = 11.sp,
            color = Color.White.copy(alpha = 0.9f),
            maxLines = 2
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = Color.White.copy(alpha = 0.2f),
          modifier = Modifier.clickable(onClick = onManageClick)
        ) {
          Text(
            text = "Options SOS",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
          )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Surface(
          shape = RoundedCornerShape(10.dp),
          color = Color.White,
          modifier = Modifier
            .clickable(onClick = onCancelSos)
            .testTag("cancel_sos_broadcast_btn")
        ) {
          Text(
            text = "Arrêter le SOS",
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFFB71C1C),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
          )
        }
      }
    }
  }
}

@Composable
fun SosEmergencyDialog(
  booking: RideBooking,
  isEmergencyAlertActive: Boolean,
  onDismiss: () -> Unit,
  onShareLocationWithEmergency: () -> Unit,
  onCall911: () -> Unit,
  onSilentAlert: () -> Unit,
  onCancelEmergencyAlert: () -> Unit
) {
  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Surface(
      shape = RoundedCornerShape(24.dp),
      color = Color.White,
      shadowElevation = 24.dp,
      modifier = Modifier
        .fillMaxWidth(0.92f)
        .wrapContentHeight()
        .testTag("sos_emergency_dialog")
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // Siren & Header
        Surface(
          shape = CircleShape,
          color = AberRed.copy(alpha = 0.12f),
          border = BorderStroke(2.dp, AberRed),
          modifier = Modifier.size(56.dp)
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
              imageVector = Icons.Default.Security,
              contentDescription = null,
              tint = AberRed,
              modifier = Modifier.size(30.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
          text = "Assistance Urgence SOS",
          fontSize = 19.sp,
          fontWeight = FontWeight.ExtraBold,
          color = AberDark
        )

        Text(
          text = "Partagez instantanément votre position GPS en direct et les informations du taxi avec les secours (112) et vos contacts de confiance.",
          fontSize = 12.5.sp,
          color = AberGrayText,
          lineHeight = 17.sp,
          modifier = Modifier.padding(top = 4.dp, bottom = 14.dp),
          textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        // Telemetry Preview Card
        Surface(
          shape = RoundedCornerShape(16.dp),
          color = AberGrayLight,
          border = BorderStroke(1.dp, AberBorder),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = AberRed,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "Position GPS en direct (Lyon)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = AberGrayText
              )
            }
            Text(
              text = "${booking.pickupLocation.title} (45.7640° N, 4.8357° E)",
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold,
              color = AberDark,
              modifier = Modifier.padding(vertical = 2.dp)
            )
            Text(
              text = "Signal GPS : Précision Haute (±2.5m)",
              fontSize = 10.5.sp,
              color = LyonBluePrimary,
              fontWeight = FontWeight.SemiBold
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = AberBorder)

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Column {
                Text(text = "Chauffeur & Taxi", fontSize = 10.5.sp, color = AberGrayText, fontWeight = FontWeight.SemiBold)
                Text(text = "${booking.driver.name} • ${booking.vehicle.displayName}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AberDark)
              }
              Column(horizontalAlignment = Alignment.End) {
                Text(text = "Réf. Course", fontSize = 10.5.sp, color = AberGrayText, fontWeight = FontWeight.SemiBold)
                Text(text = "#LYON-${booking.id.takeLast(4).ifEmpty { "9842" }}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = LyonBluePrimary)
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Action 1: Share Live GPS with 112 & Contacts (Primary Emergency Action)
        Button(
          onClick = onShareLocationWithEmergency,
          shape = RoundedCornerShape(16.dp),
          colors = ButtonDefaults.buttonColors(containerColor = AberRed),
          modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .testTag("share_emergency_location_button")
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
          ) {
            Icon(
              imageVector = Icons.Default.Share,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Partager la position avec les secours (112)",
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold,
              color = Color.White
            )
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Action 2: Call 112 Emergency Services
        OutlinedButton(
          onClick = onCall911,
          shape = RoundedCornerShape(16.dp),
          border = BorderStroke(1.5.dp, AberRed),
          colors = ButtonDefaults.outlinedButtonColors(contentColor = AberRed),
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .testTag("call_emergency_services_button")
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
          ) {
            Icon(
              imageVector = Icons.Default.Phone,
              contentDescription = null,
              tint = AberRed,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Appeler les secours d'urgence (112)",
              fontSize = 13.5.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Action 3: Silent Panic Alert to 24/7 LyonTaxis Safety Operations
        Surface(
          shape = RoundedCornerShape(14.dp),
          color = AberGrayLight,
          border = BorderStroke(1.dp, AberBorder),
          modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSilentAlert)
            .testTag("silent_panic_alert_button")
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
          ) {
            Icon(
              imageVector = Icons.Default.Lock,
              contentDescription = null,
              tint = AberDark,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Envoyer une alerte silencieuse LyonTaxis",
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = AberDark
            )
          }
        }

        if (isEmergencyAlertActive) {
          Spacer(modifier = Modifier.height(8.dp))

          TextButton(
            onClick = onCancelEmergencyAlert,
            modifier = Modifier.testTag("cancel_active_alert_button")
          ) {
            Text(
              text = "Annuler l'alerte d'urgence en cours",
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = AberRed
            )
          }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Dismiss button
        TextButton(
          onClick = onDismiss,
          modifier = Modifier.testTag("dismiss_emergency_dialog_button")
        ) {
          Text(
            text = "Fermer / Ce n'est pas une urgence",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = AberGrayText
          )
        }
      }
    }
  }
}


package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LocationPoint
import com.example.model.PaymentMethodItem
import com.example.model.RidePreferences
import com.example.model.VehicleCategory
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun VehicleSelectionSheet(
  pickupLocation: LocationPoint,
  dropoffLocation: LocationPoint,
  intermediateStops: List<LocationPoint> = emptyList(),
  popularLocations: List<LocationPoint> = emptyList(),
  selectedVehicle: VehicleCategory,
  onSelectVehicle: (VehicleCategory) -> Unit,
  onAddIntermediateStop: (LocationPoint) -> Unit,
  onRemoveIntermediateStop: (String) -> Unit,
  onMoveStopUp: ((Int) -> Unit)? = null,
  onMoveStopDown: ((Int) -> Unit)? = null,
  selectedPayment: PaymentMethodItem?,
  appliedPromo: String?,
  discountAmount: Double,
  ridePreferences: RidePreferences = RidePreferences(),
  onUpdateRidePreferences: (RidePreferences) -> Unit = {},
  onRequestRide: () -> Unit,
  onScheduleRide: (String, String) -> Unit = { _, _ -> },
  onOpenScheduleScreen: (() -> Unit)? = null,
  onOpenPromoDialog: () -> Unit,
  onOpenPaymentMethods: () -> Unit,
  onBackClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val vehicles = VehicleCategory.entries
  var showAddStopDialog by remember { mutableStateOf(false) }
  var showPreferencesSection by remember { mutableStateOf(false) }
  var showScheduleDialog by remember { mutableStateOf(false) }
  var scheduledConfirmationInfo by remember { mutableStateOf<Pair<String, String>?>(null) }

  val stopSurcharge = intermediateStops.size * 3.50
  val extraDistanceFare = intermediateStops.size * 3.60
  val totalExtraStopCost = stopSurcharge + extraDistanceFare

  val activePreferencesCount = listOf(
    ridePreferences.babySeat,
    ridePreferences.pmrAccess,
    ridePreferences.petFriendly,
    ridePreferences.extraLuggage,
    ridePreferences.silentRide
  ).count { it }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(AberBackground)
  ) {
    // Top Bar
    Surface(
      color = Color.White,
      shadowElevation = 4.dp,
      modifier = Modifier
        .fillMaxWidth()
        .statusBarsPadding()
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(
          onClick = onBackClick,
          modifier = Modifier.testTag("vehicle_sheet_back")
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            tint = AberDark
          )
        }

        Text(
          text = "Choisir le taxi & l'itinéraire",
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold,
          color = AberDark,
          modifier = Modifier.weight(1f)
        )

        // Stop count chip indicator
        if (intermediateStops.isNotEmpty()) {
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = AberAmber.copy(alpha = 0.15f),
            border = BorderStroke(1.dp, AberAmber)
          ) {
            Text(
              text = "${intermediateStops.size} Arrêt${if (intermediateStops.size > 1) "s" else ""}",
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = AberAmber,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
          }
        }
      }
    }

    // Scrollable Content: MultiStopRouteCard + Vehicles List + Preferences
    LazyColumn(
      modifier = Modifier
        .weight(1f)
        .padding(horizontal = 16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
      contentPadding = PaddingValues(vertical = 14.dp)
    ) {
      // 1. Interactive Multi-Stop Journey Route Component
      item {
        MultiStopRouteCard(
          pickupLocation = pickupLocation,
          dropoffLocation = dropoffLocation,
          intermediateStops = intermediateStops,
          onAddStopClick = { showAddStopDialog = true },
          onRemoveStop = onRemoveIntermediateStop,
          onMoveStopUp = onMoveStopUp,
          onMoveStopDown = onMoveStopDown
        )
      }

      // 2. Dynamic Price Breakdown Card (Expandable)
      item {
        DynamicPriceBreakdownCard(
          selectedVehicle = selectedVehicle,
          intermediateStopsCount = intermediateStops.size,
          discountAmount = discountAmount
        )
      }

      // 3. Options & Préférences de Course (Collapsible Card)
      item {
        Surface(
          shape = RoundedCornerShape(16.dp),
          color = Color.White,
          border = BorderStroke(1.dp, if (activePreferencesCount > 0) LyonBluePrimary.copy(alpha = 0.5f) else AberBorder),
          shadowElevation = 1.dp,
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clickable { showPreferencesSection = !showPreferencesSection },
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.Tune,
                  contentDescription = null,
                  tint = LyonBluePrimary,
                  modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "Options & Préférences de course",
                  fontSize = 14.sp,
                  fontWeight = FontWeight.Bold,
                  color = AberDark
                )
                if (activePreferencesCount > 0) {
                  Spacer(modifier = Modifier.width(6.dp))
                  Surface(
                    shape = CircleShape,
                    color = LyonBluePrimary,
                    modifier = Modifier.size(20.dp)
                  ) {
                    Box(contentAlignment = Alignment.Center) {
                      Text(
                        text = "$activePreferencesCount",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                      )
                    }
                  }
                }
              }

              Icon(
                imageVector = if (showPreferencesSection) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = AberGrayText
              )
            }

            AnimatedVisibility(
              visible = showPreferencesSection,
              enter = expandVertically(),
              exit = shrinkVertically()
            ) {
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(top = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Divider(color = AberBorder.copy(alpha = 0.5f))

                PreferenceToggleRow(
                  title = "Siège bébé / Rehausseur enfant",
                  subtitle = "Installé à l'arrière pour la sécurité des enfants",
                  icon = Icons.Default.ChildCare,
                  isChecked = ridePreferences.babySeat,
                  onCheckedChange = { onUpdateRidePreferences(ridePreferences.copy(babySeat = it)) }
                )

                PreferenceToggleRow(
                  title = "Véhicule adapté PMR / Fauteuil",
                  subtitle = "Accès facilité pour personnes à mobilité réduite",
                  icon = Icons.Default.Accessible,
                  isChecked = ridePreferences.pmrAccess,
                  onCheckedChange = { onUpdateRidePreferences(ridePreferences.copy(pmrAccess = it)) }
                )

                PreferenceToggleRow(
                  title = "Animaux de compagnie acceptés",
                  subtitle = "Chiens et chats bienvenus à bord",
                  icon = Icons.Default.Pets,
                  isChecked = ridePreferences.petFriendly,
                  onCheckedChange = { onUpdateRidePreferences(ridePreferences.copy(petFriendly = it)) }
                )

                PreferenceToggleRow(
                  title = "Grand coffre / Bagages volumineux",
                  subtitle = "Espace supplémentaire pour valises & sacs",
                  icon = Icons.Default.Luggage,
                  isChecked = ridePreferences.extraLuggage,
                  onCheckedChange = { onUpdateRidePreferences(ridePreferences.copy(extraLuggage = it)) }
                )

                PreferenceToggleRow(
                  title = "Trajet silencieux (Chauffeur discret)",
                  subtitle = "Idéal pour travailler, se reposer ou téléphoner",
                  icon = Icons.Default.VolumeOff,
                  isChecked = ridePreferences.silentRide,
                  onCheckedChange = { onUpdateRidePreferences(ridePreferences.copy(silentRide = it)) }
                )
              }
            }
          }
        }
      }

      // 4. Header for Vehicle Selection
      item {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(
            text = "Sélectionnez votre taxi",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = AberDark
          )

          Text(
            text = if (intermediateStops.isNotEmpty()) "Tarifs incluant ${intermediateStops.size} arrêt(s)" else "Prise en charge immédiate",
            fontSize = 11.5.sp,
            color = if (intermediateStops.isNotEmpty()) LyonBluePrimary else AberGrayText,
            fontWeight = if (intermediateStops.isNotEmpty()) FontWeight.SemiBold else FontWeight.Normal
          )
        }
      }

      // 5. Vehicle Category Cards with dynamically updated fares
      items(vehicles) { v ->
        val dynamicFare = v.basePrice + totalExtraStopCost
        AberVehicleCard(
          vehicle = v,
          isSelected = v == selectedVehicle,
          onSelect = { onSelectVehicle(v) },
          estimatedFare = dynamicFare,
          stopCount = intermediateStops.size
        )
      }
    }

    // Bottom Action Bar (Payment, Promo, Schedule & Request Buttons)
    Surface(
      shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
      color = Color.White,
      shadowElevation = 16.dp,
      modifier = Modifier
        .fillMaxWidth()
        .navigationBarsPadding()
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(18.dp)
      ) {
        // Quick Action Row (Payment + Promo)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Payment Method Selector Chip
          Surface(
            shape = RoundedCornerShape(14.dp),
            color = AberGrayLight,
            border = BorderStroke(1.dp, AberBorder),
            modifier = Modifier
              .clickable { onOpenPaymentMethods() }
              .testTag("vehicle_payment_chip")
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Default.Payment,
                contentDescription = null,
                tint = LyonBluePrimary,
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = selectedPayment?.title ?: "Espèces",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = AberDark
              )
            }
          }

          // Promo Code Chip
          Surface(
            shape = RoundedCornerShape(14.dp),
            color = if (appliedPromo != null) LyonBlueLight else AberGrayLight,
            border = BorderStroke(1.dp, if (appliedPromo != null) LyonBluePrimary else AberBorder),
            modifier = Modifier
              .clickable { onOpenPromoDialog() }
              .testTag("vehicle_promo_chip")
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Default.ConfirmationNumber,
                contentDescription = null,
                tint = if (appliedPromo != null) LyonBluePrimary else AberGrayText,
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = appliedPromo ?: "Code Promo",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (appliedPromo != null) LyonBluePrimary else AberDark
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Price Summary with Discount & Intermediate Stops
        val basePriceWithStops = selectedVehicle.basePrice + totalExtraStopCost
        val finalPrice = (basePriceWithStops - discountAmount).coerceAtLeast(5.0)

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "Estimation totale du trajet :",
              fontSize = 13.5.sp,
              color = AberGrayText
            )
            if (intermediateStops.isNotEmpty()) {
              Text(
                text = "Inclut +${"%.2f".format(totalExtraStopCost)} € pour ${intermediateStops.size} arrêt(s)",
                fontSize = 11.sp,
                color = AberAmber,
                fontWeight = FontWeight.SemiBold
              )
            }
          }
          Row(verticalAlignment = Alignment.CenterVertically) {
            if (discountAmount > 0) {
              Text(
                text = "${"%.2f".format(basePriceWithStops)} €",
                fontSize = 14.sp,
                color = AberGrayText,
                style = androidx.compose.ui.text.TextStyle(
                  textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                )
              )
              Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
              text = "${"%.2f".format(finalPrice)} €",
              fontSize = 22.sp,
              fontWeight = FontWeight.ExtraBold,
              color = LyonBluePrimary
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Action Buttons: Planifier vs Commander
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          // Schedule button
          OutlinedButton(
            onClick = {
              if (onOpenScheduleScreen != null) {
                onOpenScheduleScreen()
              } else {
                showScheduleDialog = true
              }
            },
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.5.dp, LyonBluePrimary),
            colors = ButtonDefaults.outlinedButtonColors(
              containerColor = LyonBlueLight.copy(alpha = 0.5f),
              contentColor = LyonBluePrimary
            ),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 14.dp),
            modifier = Modifier
              .weight(0.42f)
              .height(52.dp)
              .testTag("schedule_ride_button")
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.Center
            ) {
              Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "Planifier",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }

          // Immediate Request Button
          Button(
            onClick = onRequestRide,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = LyonBluePrimary,
              contentColor = Color.White
            ),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            modifier = Modifier
              .weight(0.58f)
              .height(52.dp)
              .testTag("request_ride_button")
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.Center
            ) {
              Icon(
                imageVector = Icons.Default.DirectionsCar,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "Commander",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }
      }
    }

    // Add Intermediate Stop Dialog
    if (showAddStopDialog) {
      AddIntermediateStopDialog(
        popularLocations = popularLocations,
        onSelectStop = { stop ->
          onAddIntermediateStop(stop)
          showAddStopDialog = false
        },
        onDismiss = { showAddStopDialog = false }
      )
    }

    // Schedule Ride Dialog
    if (showScheduleDialog) {
      ScheduleRideDialog(
        vehicle = selectedVehicle,
        pickupTitle = pickupLocation.title,
        dropoffTitle = dropoffLocation.title,
        estimatedFare = (selectedVehicle.basePrice + totalExtraStopCost - discountAmount).coerceAtLeast(5.0),
        onConfirmSchedule = { date, time ->
          onScheduleRide(date, time)
          showScheduleDialog = false
          scheduledConfirmationInfo = Pair(date, time)
        },
        onDismiss = { showScheduleDialog = false }
      )
    }

    // Schedule Confirmation Dialog
    if (scheduledConfirmationInfo != null) {
      val (date, time) = scheduledConfirmationInfo!!
      AlertDialog(
        onDismissRequest = { scheduledConfirmationInfo = null },
        icon = {
          Surface(
            shape = CircleShape,
            color = AberGreen.copy(alpha = 0.15f),
            modifier = Modifier.size(54.dp)
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = AberGreen,
                modifier = Modifier.size(32.dp)
              )
            }
          }
        },
        title = {
          Text(
            text = "Course programmée avec succès !",
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
          )
        },
        text = {
          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
              text = "Votre taxi ${selectedVehicle.displayName} est réservé pour le :",
              fontSize = 13.5.sp,
              color = AberDark
            )
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = LyonBlueLight,
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = Icons.Default.Event,
                  contentDescription = null,
                  tint = LyonBluePrimary,
                  modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "$date à $time",
                  fontSize = 15.sp,
                  fontWeight = FontWeight.ExtraBold,
                  color = LyonBluePrimary
                )
              }
            }
            Text(
              text = "Un rappel de notification vous sera envoyé 15 minutes avant l'arrivée du chauffeur.",
              fontSize = 12.sp,
              color = AberGrayText
            )
          }
        },
        confirmButton = {
          Button(
            onClick = { scheduledConfirmationInfo = null },
            colors = ButtonDefaults.buttonColors(containerColor = LyonBluePrimary)
          ) {
            Text("Compris")
          }
        }
      )
    }
  }
}

@Composable
private fun PreferenceToggleRow(
  title: String,
  subtitle: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  isChecked: Boolean,
  onCheckedChange: (Boolean) -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onCheckedChange(!isChecked) }
      .padding(vertical = 6.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Surface(
      shape = RoundedCornerShape(10.dp),
      color = if (isChecked) LyonBlueLight else AberGrayLight,
      modifier = Modifier.size(36.dp)
    ) {
      Box(contentAlignment = Alignment.Center) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = if (isChecked) LyonBluePrimary else AberGrayText,
          modifier = Modifier.size(18.dp)
        )
      }
    }

    Spacer(modifier = Modifier.width(10.dp))

    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = title,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        color = AberDark
      )
      Text(
        text = subtitle,
        fontSize = 11.sp,
        color = AberGrayText,
        lineHeight = 14.sp
      )
    }

    Switch(
      checked = isChecked,
      onCheckedChange = onCheckedChange,
      colors = SwitchDefaults.colors(
        checkedThumbColor = Color.White,
        checkedTrackColor = LyonBluePrimary,
        uncheckedThumbColor = AberGrayText,
        uncheckedTrackColor = AberBorder
      )
    )
  }
}

@Composable
fun ScheduleRideDialog(
  vehicle: VehicleCategory,
  pickupTitle: String,
  dropoffTitle: String,
  estimatedFare: Double,
  onConfirmSchedule: (date: String, time: String) -> Unit,
  onDismiss: () -> Unit
) {
  var selectedDayIndex by remember { mutableIntStateOf(0) }
  val days = listOf("Aujourd'hui", "Demain (28 Août)", "Vendredi (29 Août)", "Samedi (30 Août)", "Dimanche (31 Août)")

  var selectedHour by remember { mutableStateOf("07") }
  var selectedMinute by remember { mutableStateOf("30") }

  val hours = listOf("05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23")
  val minutes = listOf("00", "15", "30", "45")

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.Schedule,
          contentDescription = null,
          tint = LyonBluePrimary,
          modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Planifier une course",
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold
        )
      }
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        Text(
          text = "Choisissez l'horaire de prise en charge pour votre taxi ${vehicle.displayName} :",
          fontSize = 13.sp,
          color = AberDark
        )

        // Day Selector Chips
        Text("Date :", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AberGrayText)
        LazyRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          items(days.size) { idx ->
            val isSelected = selectedDayIndex == idx
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = if (isSelected) LyonBluePrimary else AberGrayLight,
              border = BorderStroke(1.dp, if (isSelected) LyonBluePrimary else AberBorder),
              modifier = Modifier.clickable { selectedDayIndex = idx }
            ) {
              Text(
                text = days[idx],
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else AberDark,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
              )
            }
          }
        }

        // Time Selector (Hours + Minutes)
        Text("Heure :", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AberGrayText)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          // Hour selector
          Column(modifier = Modifier.weight(1f)) {
            Text("Heure (HH)", fontSize = 11.sp, color = AberGrayText)
            Spacer(modifier = Modifier.height(4.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              items(hours) { h ->
                val isSelected = selectedHour == h
                Surface(
                  shape = RoundedCornerShape(10.dp),
                  color = if (isSelected) LyonBluePrimary else AberGrayLight,
                  modifier = Modifier.clickable { selectedHour = h }
                ) {
                  Text(
                    text = "${h}h",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) Color.White else AberDark,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                  )
                }
              }
            }
          }
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          // Minute selector
          Column(modifier = Modifier.fillMaxWidth()) {
            Text("Minutes (MM)", fontSize = 11.sp, color = AberGrayText)
            Spacer(modifier = Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              minutes.forEach { m ->
                val isSelected = selectedMinute == m
                Surface(
                  shape = RoundedCornerShape(10.dp),
                  color = if (isSelected) LyonBluePrimary else AberGrayLight,
                  border = BorderStroke(1.dp, if (isSelected) LyonBluePrimary else AberBorder),
                  modifier = Modifier
                    .weight(1f)
                    .clickable { selectedMinute = m }
                ) {
                  Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 8.dp)) {
                    Text(
                      text = ":$m",
                      fontSize = 13.sp,
                      fontWeight = FontWeight.Bold,
                      color = if (isSelected) Color.White else AberDark
                    )
                  }
                }
              }
            }
          }
        }

        // Summary Card
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = AberGrayLight,
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text("De : $pickupTitle", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = AberDark)
              Text("${"%.2f".format(estimatedFare)} €", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = LyonBluePrimary)
            }
            Text("Vers : $dropoffTitle", fontSize = 12.sp, color = AberGrayText)
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          onConfirmSchedule(days[selectedDayIndex], "${selectedHour}:${selectedMinute}")
        },
        colors = ButtonDefaults.buttonColors(containerColor = LyonBluePrimary)
      ) {
        Text("Confirmer la réservation")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Annuler", color = AberGrayText)
      }
    }
  )
}



package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LocationPoint
import com.example.model.VehicleCategory
import com.example.ui.components.AberLocationChip
import com.example.ui.components.LeafletMap
import com.example.ui.components.AberPrimaryButton
import com.example.ui.theme.*
import kotlin.math.roundToInt

@Composable
fun HomeScreen(
  pickupLocation: LocationPoint,
  dropoffLocation: LocationPoint,
  selectedVehicle: VehicleCategory = VehicleCategory.JUST_GO,
  popularLocations: List<LocationPoint> = emptyList(),
  onOpenDrawer: () -> Unit,
  onChooseDropoffClick: () -> Unit,
  onSelectVehicleClick: () -> Unit,
  onSelectQuickLocation: (String) -> Unit,
  onSelectVehicle: ((VehicleCategory) -> Unit)? = null,
  onUpdateDropoffLocation: ((LocationPoint) -> Unit)? = null,
  onConfirmRideWithEstimate: ((VehicleCategory) -> Unit)? = null,
  onScheduleClick: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  val focusManager = LocalFocusManager.current

  // State for destination input and price estimation
  var destinationSearchText by remember(dropoffLocation.title) { mutableStateOf(dropoffLocation.title) }
  var isSearchFocused by remember { mutableStateOf(false) }
  var currentSelectedCategory by remember(selectedVehicle) { mutableStateOf(selectedVehicle) }
  var estimatedDistanceKm by remember(dropoffLocation.distanceKm) {
    mutableFloatStateOf(if (dropoffLocation.distanceKm > 0) dropoffLocation.distanceKm.toFloat() else 4.8f)
  }
  var isPeakTrafficSurge by remember { mutableStateOf(false) }
  var showFareBreakdown by remember { mutableStateOf(false) }

  // Calculation formulas for live price estimation range
  val trafficMultiplier = if (isPeakTrafficSurge) 1.25f else 1.0f

  val vehicleBaseRate = when (currentSelectedCategory) {
    VehicleCategory.JUST_GO -> 3.50
    VehicleCategory.ELECTRIC_CAR -> 3.50
    VehicleCategory.BIKE -> 2.00
    VehicleCategory.TAXI_4_SEAT -> 4.00
    VehicleCategory.TAXI_7_SEAT -> 5.50
    VehicleCategory.LUXURY -> 7.50
    VehicleCategory.LIMOUSINE -> 12.00
  }

  val vehiclePerKmRate = when (currentSelectedCategory) {
    VehicleCategory.JUST_GO -> 1.65
    VehicleCategory.ELECTRIC_CAR -> 1.55
    VehicleCategory.BIKE -> 0.85
    VehicleCategory.TAXI_4_SEAT -> 1.85
    VehicleCategory.TAXI_7_SEAT -> 2.40
    VehicleCategory.LUXURY -> 2.90
    VehicleCategory.LIMOUSINE -> 4.20
  }

  val baseEstimatedFare = vehicleBaseRate + (estimatedDistanceKm * vehiclePerKmRate)
  val lowEstimate = (baseEstimatedFare * trafficMultiplier * 0.95).coerceAtLeast(vehicleBaseRate + 2.0)
  val highEstimate = (baseEstimatedFare * trafficMultiplier * 1.22).coerceAtLeast(lowEstimate + 2.5)
  val estimatedDurationMin = (estimatedDistanceKm * 2.6f * trafficMultiplier).roundToInt().coerceAtLeast(3)

  // Filtered location suggestions when typing
  val filteredSuggestions = remember(destinationSearchText, popularLocations) {
    if (destinationSearchText.isBlank()) {
      popularLocations.take(3)
    } else {
      popularLocations.filter {
        it.title.contains(destinationSearchText, ignoreCase = true) ||
          it.address.contains(destinationSearchText, ignoreCase = true)
      }
    }
  }

  Box(modifier = modifier.fillMaxSize()) {
    // 1. Vector Map Canvas with dynamic Chicago grid and moving taxis
    LeafletMap(
      pickup = pickupLocation,
      dropoff = dropoffLocation,
      modifier = Modifier.fillMaxSize(),
      interactive = true
    )

    // 2. Top Header Overlay with Avatar (Menu) button & App Badge
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .statusBarsPadding()
        .padding(horizontal = 16.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      // User Avatar / Menu Button
      Surface(
        shape = CircleShape,
        color = Color.White,
        shadowElevation = 6.dp,
        border = BorderStroke(2.dp, AberTealPrimary),
        modifier = Modifier
          .size(50.dp)
          .clickable { onOpenDrawer() }
          .testTag("home_menu_avatar_button")
      ) {
        Box(contentAlignment = Alignment.Center) {
          Icon(
            imageVector = Icons.Default.Menu,
            contentDescription = "Menu",
            tint = AberDark,
            modifier = Modifier.size(24.dp)
          )
        }
      }

      // App Title Pill
      Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 6.dp,
        modifier = Modifier.padding(vertical = 4.dp)
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Surface(
            shape = CircleShape,
            color = LyonBluePrimary,
            modifier = Modifier.size(10.dp)
          ) {}
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "LyonTaxis",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = AberDark
          )
        }
      }

      // Live Estimator Status Badge
      Surface(
        shape = RoundedCornerShape(20.dp),
        color = LyonBlueLight,
        border = BorderStroke(1.dp, LyonBluePrimary),
        modifier = Modifier.padding(vertical = 4.dp)
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Default.Calculate,
            contentDescription = null,
            tint = LyonBluePrimary,
            modifier = Modifier.size(14.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "Estimation en direct",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = LyonBluePrimary
          )
        }
      }
    }

    // 3. Bottom Comprehensive Ride Request & Price Estimation Card
    Surface(
      shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
      color = Color.White,
      shadowElevation = 20.dp,
      modifier = Modifier
        .fillMaxWidth()
        .align(Alignment.BottomCenter)
        .navigationBarsPadding()
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState())
          .padding(horizontal = 18.dp, vertical = 14.dp)
      ) {
        // Drag handle indicator
        Box(
          modifier = Modifier
            .align(Alignment.CenterHorizontally)
            .size(width = 40.dp, height = 4.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(AberBorder)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Pickup Location Row (Compact)
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clickable { onChooseDropoffClick() }
            .padding(vertical = 4.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Surface(
            shape = CircleShape,
            color = LyonBlueLight,
            border = BorderStroke(2.dp, LyonBluePrimary),
            modifier = Modifier.size(14.dp)
          ) {}

          Spacer(modifier = Modifier.width(10.dp))

          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "POINT DE DÉPART",
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              color = AberGrayText,
              letterSpacing = 0.5.sp
            )
            Text(
              text = pickupLocation.title,
              fontSize = 13.sp,
              fontWeight = FontWeight.SemiBold,
              color = AberDark
            )
          }

          TextButton(
            onClick = { onChooseDropoffClick() },
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
          ) {
            Text("Modifier", fontSize = 12.sp, color = LyonBluePrimary, fontWeight = FontWeight.Bold)
          }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // --- Interactive Destination Input Component ---
        OutlinedTextField(
          value = destinationSearchText,
          onValueChange = { newQuery ->
            destinationSearchText = newQuery
            isSearchFocused = true
            val matched = popularLocations.firstOrNull { it.title.equals(newQuery, ignoreCase = true) }
            if (matched != null) {
              onUpdateDropoffLocation?.invoke(matched)
              estimatedDistanceKm = matched.distanceKm.toFloat()
            }
          },
          label = { Text("Où souhaitez-vous aller ?", fontSize = 12.sp) },
          placeholder = { Text("Entrez une adresse ou un lieu à Lyon...", fontSize = 14.sp) },
          leadingIcon = {
            Icon(
              imageVector = Icons.Default.LocationOn,
              contentDescription = "Destination d'arrivée",
              tint = AberRed,
              modifier = Modifier.size(22.dp)
            )
          },
          trailingIcon = {
            if (destinationSearchText.isNotEmpty()) {
              IconButton(
                onClick = {
                  destinationSearchText = ""
                  isSearchFocused = true
                }
              ) {
                Icon(
                  imageVector = Icons.Default.Clear,
                  contentDescription = "Effacer la destination",
                  tint = AberGrayText,
                  modifier = Modifier.size(18.dp)
                )
              }
            } else {
              IconButton(onClick = onChooseDropoffClick) {
                Icon(
                  imageVector = Icons.Default.Search,
                  contentDescription = "Rechercher",
                  tint = LyonBluePrimary,
                  modifier = Modifier.size(20.dp)
                )
              }
            }
          },
          singleLine = true,
          shape = RoundedCornerShape(14.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = LyonBluePrimary,
            unfocusedBorderColor = AberBorder,
            focusedContainerColor = Color(0xFFF8FCFC),
            unfocusedContainerColor = AberGrayLight
          ),
          keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
          keyboardActions = KeyboardActions(
            onDone = {
              focusManager.clearFocus()
              isSearchFocused = false
            }
          ),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("home_destination_input")
        )

        // Dropdown suggestions when user enters text
        AnimatedVisibility(
          visible = isSearchFocused && filteredSuggestions.isNotEmpty(),
          enter = expandVertically() + fadeIn(),
          exit = shrinkVertically() + fadeOut()
        ) {
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color.White,
            border = BorderStroke(1.dp, AberBorder),
            shadowElevation = 6.dp,
            modifier = Modifier
              .fillMaxWidth()
              .padding(top = 4.dp, bottom = 6.dp)
          ) {
            Column(modifier = Modifier.padding(6.dp)) {
              filteredSuggestions.forEach { loc ->
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                      destinationSearchText = loc.title
                      onUpdateDropoffLocation?.invoke(loc)
                      estimatedDistanceKm = loc.distanceKm.toFloat()
                      isSearchFocused = false
                      focusManager.clearFocus()
                    }
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Icon(
                    imageVector = Icons.Default.Place,
                    contentDescription = null,
                    tint = LyonBluePrimary,
                    modifier = Modifier.size(18.dp)
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  Column(modifier = Modifier.weight(1f)) {
                    Text(
                      text = loc.title,
                      fontSize = 13.sp,
                      fontWeight = FontWeight.Bold,
                      color = AberDark
                    )
                    Text(
                      text = loc.address,
                      fontSize = 11.sp,
                      color = AberGrayText,
                      maxLines = 1
                    )
                  }
                  Text(
                    text = "${String.format("%.1f", loc.distanceKm)} km",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = LyonBluePrimary
                  )
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Quick Destination Chips (Scrollable) with Lyon destinations
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          AberLocationChip(
            text = "Gare Part-Dieu (2.4 km)",
            icon = Icons.Default.Train,
            onClick = {
              destinationSearchText = "Gare de Lyon-Part-Dieu"
              onSelectQuickLocation("Gare de Lyon-Part-Dieu")
              estimatedDistanceKm = 2.4f
            }
          )
          AberLocationChip(
            text = "Vieux Lyon (1.1 km)",
            icon = Icons.Default.Place,
            onClick = {
              destinationSearchText = "Vieux Lyon - Saint-Jean"
              onSelectQuickLocation("Vieux Lyon - Saint-Jean")
              estimatedDistanceKm = 1.1f
            }
          )
          AberLocationChip(
            text = "Fourvière (1.8 km)",
            icon = Icons.Default.Church,
            onClick = {
              destinationSearchText = "Basilique Notre-Dame de Fourvière"
              onSelectQuickLocation("Basilique Notre-Dame de Fourvière")
              estimatedDistanceKm = 1.8f
            }
          )
          AberLocationChip(
            text = "Parc Tête d'Or (3.2 km)",
            icon = Icons.Default.Park,
            onClick = {
              destinationSearchText = "Parc de la Tête d'Or"
              onSelectQuickLocation("Parc de la Tête d'Or")
              estimatedDistanceKm = 3.2f
            }
          )
          AberLocationChip(
            text = "Aéroport St-Exupéry (24 km)",
            icon = Icons.Default.Flight,
            onClick = {
              destinationSearchText = "Aéroport Lyon-Saint Exupéry"
              estimatedDistanceKm = 24.5f
            }
          )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // --- Vehicle Ride Type Switcher for Dynamic Price Estimation ---
        Text(
          text = "CHOISISSEZ VOTRE TAXI",
          fontSize = 10.sp,
          fontWeight = FontWeight.Bold,
          color = AberGrayText,
          letterSpacing = 0.5.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          VehicleCategory.entries.forEach { vehicle ->
            val isSelected = vehicle == currentSelectedCategory
            Surface(
              shape = RoundedCornerShape(14.dp),
              color = if (isSelected) LyonBlueLight else AberGrayLight,
              border = BorderStroke(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) LyonBluePrimary else AberBorder
              ),
              modifier = Modifier
                .clickable {
                  currentSelectedCategory = vehicle
                  onSelectVehicle?.invoke(vehicle)
                }
                .testTag("vehicle_estimate_chip_${vehicle.name}")
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = when (vehicle) {
                    VehicleCategory.BIKE -> Icons.Default.DirectionsBike
                    VehicleCategory.ELECTRIC_CAR -> Icons.Default.ElectricCar
                    VehicleCategory.LUXURY, VehicleCategory.LIMOUSINE -> Icons.Default.Star
                    else -> Icons.Default.DirectionsCar
                  },
                  contentDescription = vehicle.displayName,
                  tint = if (isSelected) LyonBluePrimary else AberDark,
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                  Text(
                    text = vehicle.displayName,
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) LyonBluePrimary else AberDark
                  )
                  val sampleCost = (vehicleBaseRate + (estimatedDistanceKm * vehiclePerKmRate) * trafficMultiplier)
                  Text(
                    text = "~${String.format("%.2f", sampleCost)} €",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) LyonBlueDark else AberGrayText
                  )
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // --- Live Price Estimation Range Display Card ---
        Surface(
          shape = RoundedCornerShape(16.dp),
          color = Color(0xFFF3F7FC),
          border = BorderStroke(1.5.dp, LyonBluePrimary.copy(alpha = 0.5f)),
          shadowElevation = 4.dp,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("price_estimate_card")
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                  shape = CircleShape,
                  color = LyonBluePrimary,
                  modifier = Modifier.size(24.dp)
                ) {
                  Box(contentAlignment = Alignment.Center) {
                    Icon(
                      imageVector = Icons.Default.Euro,
                      contentDescription = null,
                      tint = Color.White,
                      modifier = Modifier.size(16.dp)
                    )
                  }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                  Text(
                    text = "ESTIMATION DU TARIF",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = LyonBlueDark,
                    letterSpacing = 0.5.sp
                  )
                  Text(
                    text = "${currentSelectedCategory.displayName} • ${String.format("%.1f", estimatedDistanceKm)} km",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = AberGrayText
                  )
                }
              }

              // Dynamic calculated Price Range (High & Low bounds)
              Column(horizontalAlignment = Alignment.End) {
                Text(
                  text = "${String.format("%.2f", lowEstimate)} € – ${String.format("%.2f", highEstimate)} €",
                  fontSize = 17.sp,
                  fontWeight = FontWeight.ExtraBold,
                  color = AberDark,
                  modifier = Modifier.testTag("price_estimate_range_text")
                )
                Text(
                  text = "~$estimatedDurationMin min d'attente",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = LyonBluePrimary
                )
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Interactive Distance Slider
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(
                text = "Distance du trajet : ${String.format("%.1f", estimatedDistanceKm)} km",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = AberDark
              )
              TextButton(
                onClick = { isPeakTrafficSurge = !isPeakTrafficSurge },
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                modifier = Modifier.testTag("traffic_surge_chip")
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    imageVector = if (isPeakTrafficSurge) Icons.Default.Traffic else Icons.Default.FlashOn,
                    contentDescription = null,
                    tint = if (isPeakTrafficSurge) AberRed else LyonBluePrimary,
                    modifier = Modifier.size(13.dp)
                  )
                  Spacer(modifier = Modifier.width(3.dp))
                  Text(
                    text = if (isPeakTrafficSurge) "Heure de pointe (1.25x)" else "Trafic fluide",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isPeakTrafficSurge) AberRed else LyonBluePrimary
                  )
                }
              }
            }

            Slider(
              value = estimatedDistanceKm,
              onValueChange = { estimatedDistanceKm = it },
              valueRange = 1.0f..30.0f,
              steps = 28,
              colors = SliderDefaults.colors(
                thumbColor = LyonBluePrimary,
                activeTrackColor = LyonBluePrimary,
                inactiveTrackColor = AberBorder
              ),
              modifier = Modifier
                .fillMaxWidth()
                .testTag("distance_slider")
            )

            // Fare breakdown toggle
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clickable { showFareBreakdown = !showFareBreakdown }
                .padding(top = 2.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = if (showFareBreakdown) "Masquer les détails" else "Voir le détail du calcul",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = LyonBluePrimary
              )
              Icon(
                imageVector = if (showFareBreakdown) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = null,
                tint = LyonBluePrimary,
                modifier = Modifier.size(16.dp)
              )
            }

            AnimatedVisibility(
              visible = showFareBreakdown,
              enter = expandVertically() + fadeIn(),
              exit = shrinkVertically() + fadeOut()
            ) {
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(top = 8.dp)
                  .background(Color.White, RoundedCornerShape(10.dp))
                  .padding(10.dp)
              ) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Text("Prise en charge de base :", fontSize = 11.sp, color = AberGrayText)
                  Text("${String.format("%.2f", vehicleBaseRate)} €", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AberDark)
                }
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Text("Tarif kilométrique (${String.format("%.1f", estimatedDistanceKm)} km à ${String.format("%.2f", vehiclePerKmRate)} €/km) :", fontSize = 11.sp, color = AberGrayText)
                  Text("${String.format("%.2f", estimatedDistanceKm * vehiclePerKmRate)} €", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AberDark)
                }
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Text("Conditions de circulation :", fontSize = 11.sp, color = AberGrayText)
                  Text("${trafficMultiplier}x (${if (isPeakTrafficSurge) "Dense" else "Fluide"})", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isPeakTrafficSurge) AberRed else LyonBluePrimary)
                }
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Text("Frais de service & taxes :", fontSize = 11.sp, color = AberGrayText)
                  Text("1,00 €", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AberDark)
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Action Buttons: Confirm & Schedule
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          if (onScheduleClick != null) {
            OutlinedButton(
              onClick = {
                onSelectVehicle?.invoke(currentSelectedCategory)
                onScheduleClick()
              },
              shape = RoundedCornerShape(14.dp),
              border = BorderStroke(1.5.dp, LyonBluePrimary),
              colors = ButtonDefaults.outlinedButtonColors(
                contentColor = LyonBluePrimary
              ),
              modifier = Modifier
                .height(52.dp)
                .testTag("home_schedule_button")
            ) {
              Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = "Planifier",
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "Planifier",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
              )
            }
          }

          AberPrimaryButton(
            text = "Confirmer (${String.format("%.2f", lowEstimate)} €)",
            onClick = {
              onSelectVehicle?.invoke(currentSelectedCategory)
              if (onConfirmRideWithEstimate != null) {
                onConfirmRideWithEstimate(currentSelectedCategory)
              } else {
                onSelectVehicleClick()
              }
            },
            icon = Icons.Default.DirectionsCar,
            modifier = Modifier.weight(1f),
            testTag = "confirm_ride_estimate_button"
          )
        }
      }
    }
  }
}


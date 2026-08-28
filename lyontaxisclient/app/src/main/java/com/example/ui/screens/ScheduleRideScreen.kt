package com.example.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.animation.*
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LocationPoint
import com.example.model.PaymentMethodItem
import com.example.model.RidePreferences
import com.example.model.VehicleCategory
import com.example.ui.components.AberHeader
import com.example.ui.components.AberPrimaryButton
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun ScheduleRideScreen(
  pickupLocation: LocationPoint,
  dropoffLocation: LocationPoint,
  intermediateStops: List<LocationPoint> = emptyList(),
  selectedVehicle: VehicleCategory,
  selectedPayment: PaymentMethodItem?,
  ridePreferences: RidePreferences = RidePreferences(),
  discountAmount: Double = 0.0,
  onConfirmSchedule: (formattedDate: String, formattedTime: String, vehicle: VehicleCategory, notes: String) -> Unit,
  onBackClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val calendarNow = remember { Calendar.getInstance() }

  // State for chosen date & time (defaults to 1 hour ahead)
  val scheduledCal = remember {
    Calendar.getInstance().apply {
      add(Calendar.HOUR_OF_DAY, 1)
      set(Calendar.MINUTE, (get(Calendar.MINUTE) / 15 + 1) * 15 % 60)
      set(Calendar.SECOND, 0)
      set(Calendar.MILLISECOND, 0)
    }
  }

  var selectedYear by remember { mutableIntStateOf(scheduledCal.get(Calendar.YEAR)) }
  var selectedMonth by remember { mutableIntStateOf(scheduledCal.get(Calendar.MONTH)) }
  var selectedDay by remember { mutableIntStateOf(scheduledCal.get(Calendar.DAY_OF_MONTH)) }
  var selectedHour by remember { mutableIntStateOf(scheduledCal.get(Calendar.HOUR_OF_DAY)) }
  var selectedMinute by remember { mutableIntStateOf(scheduledCal.get(Calendar.MINUTE)) }

  var selectedCategory by remember { mutableStateOf(selectedVehicle) }
  var flightOrTrainNumber by remember { mutableStateOf("") }
  var specialInstructions by remember { mutableStateOf("") }
  var enableSmsReminder by remember { mutableStateOf(true) }
  var enableReturnBooking by remember { mutableStateOf(false) }

  // Quick day options
  val quickDays = remember {
    listOf(
      0 to "Aujourd'hui",
      1 to "Demain",
      2 to "Après-demain",
      7 to "+1 Semaine"
    )
  }
  var selectedQuickDayIndex by remember { mutableIntStateOf(0) }

  // Validation Logic: check if the selected date/time is in the past or strictly too close (less than 15 mins)
  val validationState = remember(selectedYear, selectedMonth, selectedDay, selectedHour, selectedMinute) {
    val checkCal = Calendar.getInstance().apply {
      set(Calendar.YEAR, selectedYear)
      set(Calendar.MONTH, selectedMonth)
      set(Calendar.DAY_OF_MONTH, selectedDay)
      set(Calendar.HOUR_OF_DAY, selectedHour)
      set(Calendar.MINUTE, selectedMinute)
      set(Calendar.SECOND, 0)
      set(Calendar.MILLISECOND, 0)
    }

    val currentCal = Calendar.getInstance()
    val diffMillis = checkCal.timeInMillis - currentCal.timeInMillis
    val diffMinutes = diffMillis / (1000 * 60)

    when {
      diffMillis <= 0 -> {
        ValidationResult(
          isValid = false,
          errorMessage = "La date et l'heure sélectionnées sont déjà passées. Veuillez choisir un horaire futur."
        )
      }
      diffMinutes < 15 -> {
        ValidationResult(
          isValid = false,
          errorMessage = "Veuillez réserver au moins 15 minutes à l'avance pour garantir la prise en charge d'un chauffeur."
        )
      }
      diffMinutes > 60 * 24 * 60 -> {
        ValidationResult(
          isValid = false,
          errorMessage = "La réservation anticipée est limitée à 60 jours à l'avance."
        )
      }
      else -> {
        ValidationResult(
          isValid = true,
          errorMessage = null,
          formattedDateTime = SimpleDateFormat("EEEE d MMMM yyyy 'à' HH'h'mm", Locale.FRANCE).format(checkCal.time),
          shortDate = SimpleDateFormat("EEE d MMM yyyy", Locale.FRANCE).format(checkCal.time),
          shortTime = String.format(Locale.FRANCE, "%02d:%02d", selectedHour, selectedMinute)
        )
      }
    }
  }

  // Cost calculation
  val totalExtraStopCost = intermediateStops.size * 3.50
  val estimatedFare = (selectedCategory.basePrice + totalExtraStopCost - discountAmount).coerceAtLeast(5.0)

  // Android Native Date Picker
  val datePickerDialog = remember {
    DatePickerDialog(
      context,
      { _, year, month, dayOfMonth ->
        selectedYear = year
        selectedMonth = month
        selectedDay = dayOfMonth
        selectedQuickDayIndex = -1 // custom
      },
      selectedYear,
      selectedMonth,
      selectedDay
    ).apply {
      datePicker.minDate = System.currentTimeMillis() - 1000
      datePicker.maxDate = System.currentTimeMillis() + (60L * 24 * 60 * 60 * 1000)
    }
  }

  // Android Native Time Picker
  val timePickerDialog = remember {
    TimePickerDialog(
      context,
      { _, hourOfDay, minute ->
        selectedHour = hourOfDay
        selectedMinute = minute
      },
      selectedHour,
      selectedMinute,
      true
    )
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(AberBackground)
  ) {
    // Header
    AberHeader(
      title = "Planifier une course",
      onBackClick = onBackClick
    )

    LazyColumn(
      modifier = Modifier
        .weight(1f)
        .padding(horizontal = 16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // 1. Hero banner: Planning info & advantage
      item {
        Spacer(modifier = Modifier.height(4.dp))
        Surface(
          shape = RoundedCornerShape(18.dp),
          color = LyonBlueLight,
          border = BorderStroke(1.dp, LyonBluePrimary.copy(alpha = 0.3f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Surface(
              shape = CircleShape,
              color = LyonBluePrimary,
              modifier = Modifier.size(44.dp)
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(
                  imageVector = Icons.Default.EventAvailable,
                  contentDescription = null,
                  tint = Color.White,
                  modifier = Modifier.size(24.dp)
                )
              }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Réservation garantie à Lyon",
                fontSize = 14.5.sp,
                fontWeight = FontWeight.Bold,
                color = LyonBluePrimary
              )
              Text(
                text = "Votre chauffeur arrive à l'heure exacte. Annulation sans frais jusqu'à 60 min avant le départ.",
                fontSize = 11.5.sp,
                color = AberDark.copy(alpha = 0.8f),
                lineHeight = 15.sp
              )
            }
          }
        }
      }

      // 2. Route Overview Card
      item {
        Surface(
          shape = RoundedCornerShape(18.dp),
          color = Color.White,
          shadowElevation = 2.dp,
          border = BorderStroke(1.dp, AberBorder),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
              text = "ITINÉRAIRE PRÉVU",
              fontSize = 11.sp,
              fontWeight = FontWeight.ExtraBold,
              color = AberGrayText,
              letterSpacing = 0.6.sp
            )

            // Departure
            Row(verticalAlignment = Alignment.CenterVertically) {
              Surface(
                shape = CircleShape,
                color = LyonBlueLight,
                border = BorderStroke(2.dp, LyonBluePrimary),
                modifier = Modifier.size(12.dp)
              ) {}
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = "Départ : ${pickupLocation.title}",
                  fontSize = 13.sp,
                  fontWeight = FontWeight.Bold,
                  color = AberDark
                )
                Text(
                  text = pickupLocation.address,
                  fontSize = 11.sp,
                  color = AberGrayText,
                  maxLines = 1
                )
              }
            }

            // Intermediate stops if any
            intermediateStops.forEachIndexed { index, stop ->
              Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                  shape = CircleShape,
                  color = AberAmber,
                  modifier = Modifier.size(8.dp)
                ) {}
                Spacer(modifier = Modifier.width(14.dp))
                Text(
                  text = "Arrêt ${index + 1} : ${stop.title}",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Medium,
                  color = AberDark
                )
              }
            }

            // Destination
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = AberRed,
                modifier = Modifier.size(14.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Column {
                Text(
                  text = "Arrivée : ${dropoffLocation.title}",
                  fontSize = 13.sp,
                  fontWeight = FontWeight.Bold,
                  color = AberDark
                )
                Text(
                  text = dropoffLocation.address,
                  fontSize = 11.sp,
                  color = AberGrayText,
                  maxLines = 1
                )
              }
            }
          }
        }
      }

      // 3. Date & Time Selection Section
      item {
        Surface(
          shape = RoundedCornerShape(18.dp),
          color = Color.White,
          shadowElevation = 2.dp,
          border = BorderStroke(
            width = if (!validationState.isValid) 1.5.dp else 1.dp,
            color = if (!validationState.isValid) AberRed else AberBorder
          ),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("schedule_date_time_card")
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.Schedule,
                  contentDescription = null,
                  tint = LyonBluePrimary,
                  modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "Date & Heure de prise en charge",
                  fontSize = 14.5.sp,
                  fontWeight = FontWeight.Bold,
                  color = AberDark
                )
              }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Quick date shortcut chips
            Text(
              text = "Raccourcis rapides :",
              fontSize = 11.sp,
              fontWeight = FontWeight.SemiBold,
              color = AberGrayText
            )
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              items(quickDays) { (daysToAdd, label) ->
                val isSelected = selectedQuickDayIndex == daysToAdd
                Surface(
                  shape = RoundedCornerShape(12.dp),
                  color = if (isSelected) LyonBluePrimary else AberGrayLight,
                  border = BorderStroke(1.dp, if (isSelected) LyonBluePrimary else AberBorder),
                  modifier = Modifier
                    .clickable {
                      selectedQuickDayIndex = daysToAdd
                      val targetCal = Calendar.getInstance().apply {
                        add(Calendar.DAY_OF_MONTH, daysToAdd)
                      }
                      selectedYear = targetCal.get(Calendar.YEAR)
                      selectedMonth = targetCal.get(Calendar.MONTH)
                      selectedDay = targetCal.get(Calendar.DAY_OF_MONTH)
                    }
                    .testTag("quick_day_$daysToAdd")
                ) {
                  Text(
                    text = label,
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) Color.White else AberDark,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Interactive Date & Time Pickers Grid
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              // Date Box (opens DatePickerDialog)
              Surface(
                shape = RoundedCornerShape(14.dp),
                color = AberGrayLight,
                border = BorderStroke(1.dp, AberBorder),
                modifier = Modifier
                  .weight(1f)
                  .clickable { datePickerDialog.show() }
                  .testTag("date_picker_button")
              ) {
                Row(
                  modifier = Modifier.padding(12.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = LyonBluePrimary,
                    modifier = Modifier.size(22.dp)
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  Column {
                    Text(
                      text = "DATE",
                      fontSize = 9.5.sp,
                      fontWeight = FontWeight.Bold,
                      color = AberGrayText,
                      letterSpacing = 0.5.sp
                    )
                    val dateStr = SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE).format(
                      Calendar.getInstance().apply {
                        set(selectedYear, selectedMonth, selectedDay)
                      }.time
                    )
                    Text(
                      text = dateStr,
                      fontSize = 14.sp,
                      fontWeight = FontWeight.ExtraBold,
                      color = AberDark
                    )
                  }
                }
              }

              // Time Box (opens TimePickerDialog)
              Surface(
                shape = RoundedCornerShape(14.dp),
                color = AberGrayLight,
                border = BorderStroke(1.dp, AberBorder),
                modifier = Modifier
                  .weight(1f)
                  .clickable { timePickerDialog.show() }
                  .testTag("time_picker_button")
              ) {
                Row(
                  modifier = Modifier.padding(12.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Icon(
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = null,
                    tint = LyonBluePrimary,
                    modifier = Modifier.size(22.dp)
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  Column {
                    Text(
                      text = "HEURE",
                      fontSize = 9.5.sp,
                      fontWeight = FontWeight.Bold,
                      color = AberGrayText,
                      letterSpacing = 0.5.sp
                    )
                    val timeStr = String.format(Locale.FRANCE, "%02d:%02d", selectedHour, selectedMinute)
                    Text(
                      text = timeStr,
                      fontSize = 14.sp,
                      fontWeight = FontWeight.ExtraBold,
                      color = AberDark
                    )
                  }
                }
              }
            }

            // Validation Feedback Banner
            Spacer(modifier = Modifier.height(12.dp))
            if (!validationState.isValid && validationState.errorMessage != null) {
              Surface(
                shape = RoundedCornerShape(12.dp),
                color = AberRedLight,
                border = BorderStroke(1.dp, AberRed.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
              ) {
                Row(
                  modifier = Modifier.padding(10.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Icon(
                    imageVector = Icons.Default.ErrorOutline,
                    contentDescription = null,
                    tint = AberRed,
                    modifier = Modifier.size(18.dp)
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                    text = validationState.errorMessage,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = AberRed,
                    lineHeight = 15.sp
                  )
                }
              }
            } else if (validationState.formattedDateTime != null) {
              Surface(
                shape = RoundedCornerShape(12.dp),
                color = LyonBlueLight,
                modifier = Modifier.fillMaxWidth()
              ) {
                Row(
                  modifier = Modifier.padding(10.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = AberGreen,
                    modifier = Modifier.size(18.dp)
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                    text = "Programmé pour le ${validationState.formattedDateTime}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = LyonBlueDark
                  )
                }
              }
            }
          }
        }
      }

      // 4. Vehicle Selection Category
      item {
        Column(modifier = Modifier.fillMaxWidth()) {
          Text(
            text = "CHOIX DU VÉHICULE",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = AberGrayText,
            letterSpacing = 0.6.sp
          )
          Spacer(modifier = Modifier.height(8.dp))

          LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            items(VehicleCategory.entries) { v ->
              val isSelected = v == selectedCategory
              val vPrice = (v.basePrice + totalExtraStopCost - discountAmount).coerceAtLeast(5.0)
              Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (isSelected) LyonBlueLight else Color.White,
                border = BorderStroke(
                  width = if (isSelected) 2.dp else 1.dp,
                  color = if (isSelected) LyonBluePrimary else AberBorder
                ),
                shadowElevation = if (isSelected) 3.dp else 1.dp,
                modifier = Modifier
                  .width(140.dp)
                  .clickable { selectedCategory = v }
                  .testTag("schedule_vehicle_${v.name}")
              ) {
                Column(modifier = Modifier.padding(12.dp)) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Icon(
                      imageVector = when (v) {
                        VehicleCategory.BIKE -> Icons.Default.DirectionsBike
                        VehicleCategory.ELECTRIC_CAR -> Icons.Default.ElectricCar
                        VehicleCategory.LUXURY, VehicleCategory.LIMOUSINE -> Icons.Default.Star
                        VehicleCategory.TAXI_7_SEAT -> Icons.Default.AirportShuttle
                        else -> Icons.Default.DirectionsCar
                      },
                      contentDescription = null,
                      tint = if (isSelected) LyonBluePrimary else AberDark,
                      modifier = Modifier.size(24.dp)
                    )

                    Surface(
                      shape = RoundedCornerShape(6.dp),
                      color = if (isSelected) LyonBluePrimary else AberGrayLight
                    ) {
                      Text(
                        text = "${v.capacity} pl.",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.White else AberGrayText,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                      )
                    }
                  }

                  Spacer(modifier = Modifier.height(8.dp))
                  Text(
                    text = v.displayName,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = AberDark,
                    maxLines = 1
                  )
                  Text(
                    text = "${"%.2f".format(vPrice)} €",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = LyonBluePrimary
                  )
                }
              }
            }
          }
        }
      }

      // 5. Flight / Train Number & Special Instructions (Optional)
      item {
        Surface(
          shape = RoundedCornerShape(18.dp),
          color = Color.White,
          shadowElevation = 2.dp,
          border = BorderStroke(1.dp, AberBorder),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
              text = "DÉTAILS COMPLÉMENTAIRES (OPTIONNEL)",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = AberGrayText,
              letterSpacing = 0.6.sp
            )

            // Flight / Train tracker field
            OutlinedTextField(
              value = flightOrTrainNumber,
              onValueChange = { flightOrTrainNumber = it },
              label = { Text("N° de vol ou de train (ex: AF7420, TGV 6612)", fontSize = 12.sp) },
              placeholder = { Text("Suivi d'horaire en cas de retard", fontSize = 12.sp) },
              leadingIcon = {
                Icon(
                  imageVector = Icons.Default.FlightTakeoff,
                  contentDescription = null,
                  tint = LyonBluePrimary,
                  modifier = Modifier.size(20.dp)
                )
              },
              singleLine = true,
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.fillMaxWidth()
            )

            // Driver instructions
            OutlinedTextField(
              value = specialInstructions,
              onValueChange = { specialInstructions = it },
              label = { Text("Consignes pour le chauffeur", fontSize = 12.sp) },
              placeholder = { Text("Ex: Hall 2, sonner à l'interphone...", fontSize = 12.sp) },
              leadingIcon = {
                Icon(
                  imageVector = Icons.Default.EditNote,
                  contentDescription = null,
                  tint = LyonBluePrimary,
                  modifier = Modifier.size(20.dp)
                )
              },
              maxLines = 2,
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.fillMaxWidth()
            )

            HorizontalDivider(color = AberBorder.copy(alpha = 0.6f))

            // SMS & Notification toggle
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
              ) {
                Icon(
                  imageVector = Icons.Default.NotificationsActive,
                  contentDescription = null,
                  tint = LyonBluePrimary,
                  modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                  Text("Rappel SMS & Notification", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = AberDark)
                  Text("Alerte 15 min avant la prise en charge", fontSize = 11.sp, color = AberGrayText)
                }
              }

              Switch(
                checked = enableSmsReminder,
                onCheckedChange = { enableSmsReminder = it },
                colors = SwitchDefaults.colors(
                  checkedThumbColor = Color.White,
                  checkedTrackColor = LyonBluePrimary
                )
              )
            }
          }
        }
      }

      // Bottom Spacer for scrolling comfortably above action bar
      item {
        Spacer(modifier = Modifier.height(10.dp))
      }
    }

    // Bottom Fixed Action Bar: Summary & Confirm Button
    Surface(
      shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
      color = Color.White,
      shadowElevation = 16.dp,
      border = BorderStroke(1.dp, AberBorder.copy(alpha = 0.5f)),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .navigationBarsPadding()
          .padding(horizontal = 20.dp, vertical = 16.dp)
      ) {
        // Price & Date recap
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "Montant estimé (${selectedCategory.displayName})",
              fontSize = 12.sp,
              color = AberGrayText
            )
            Text(
              text = "${"%.2f".format(estimatedFare)} €",
              fontSize = 22.sp,
              fontWeight = FontWeight.ExtraBold,
              color = LyonBluePrimary
            )
          }

          Surface(
            shape = RoundedCornerShape(10.dp),
            color = AberGrayLight
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Default.Payment,
                contentDescription = null,
                tint = AberDark,
                modifier = Modifier.size(14.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = selectedPayment?.title ?: "Espèces LyonTaxis",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = AberDark
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Confirm Button (disabled if invalid)
        Button(
          onClick = {
            if (validationState.isValid && validationState.shortDate != null && validationState.shortTime != null) {
              val combinedNotes = buildString {
                if (flightOrTrainNumber.isNotBlank()) append("N° de voyage : $flightOrTrainNumber. ")
                if (specialInstructions.isNotBlank()) append(specialInstructions)
              }
              onConfirmSchedule(validationState.shortDate, validationState.shortTime, selectedCategory, combinedNotes)
            } else {
              Toast.makeText(
                context,
                validationState.errorMessage ?: "Veuillez corriger la date et l'heure",
                Toast.LENGTH_LONG
              ).show()
            }
          },
          enabled = validationState.isValid,
          shape = RoundedCornerShape(14.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = LyonBluePrimary,
            disabledContainerColor = AberBorder
          ),
          modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .testTag("confirm_schedule_ride_button")
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
          ) {
            Icon(
              imageVector = Icons.Default.Check,
              contentDescription = null,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = if (validationState.isValid) "Confirmer la réservation pour le ${validationState.shortDate}" else "Sélectionnez un horaire valide",
              fontSize = 14.5.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }
    }
  }
}

private data class ValidationResult(
  val isValid: Boolean,
  val errorMessage: String? = null,
  val formattedDateTime: String? = null,
  val shortDate: String? = null,
  val shortTime: String? = null
)

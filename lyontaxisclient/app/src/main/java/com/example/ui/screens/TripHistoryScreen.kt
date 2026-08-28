package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ScheduledRideItem
import com.example.model.TripHistoryItem
import com.example.ui.components.AberHeader
import com.example.ui.theme.*

@Composable
fun TripHistoryScreen(
  historyItems: List<TripHistoryItem>,
  scheduledRides: List<ScheduledRideItem> = emptyList(),
  onCancelScheduledRide: (String) -> Unit = {},
  onNavigateToInvoices: (() -> Unit)? = null,
  onBackClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  var selectedTab by remember { mutableIntStateOf(0) } // 0: History, 1: Scheduled
  var selectedFilter by remember { mutableStateOf("Toutes les dates") }
  var showFilterDropdown by remember { mutableStateOf(false) }
  var selectedTripReceipt by remember { mutableStateOf<TripHistoryItem?>(null) }
  val context = LocalContext.current

  val filterOptions = listOf("Toutes les dates", "15 oct. 2024", "14 oct. 2024", "12 oct. 2024")

  val displayedHistory = remember(selectedFilter, historyItems) {
    if (selectedFilter == "Toutes les dates") historyItems
    else historyItems.filter { it.date == selectedFilter }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(AberBackground)
  ) {
    AberHeader(
      title = "Mes Courses",
      onBackClick = onBackClick
    )

    // Tab Navigation Bar (Historique vs Programmées)
    TabRow(
      selectedTabIndex = selectedTab,
      containerColor = Color.White,
      contentColor = LyonBluePrimary,
      indicator = { tabPositions ->
        TabRowDefaults.SecondaryIndicator(
          modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
          color = LyonBluePrimary
        )
      }
    ) {
      Tab(
        selected = selectedTab == 0,
        onClick = { selectedTab = 0 },
        text = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Historique (${historyItems.size})", fontWeight = FontWeight.Bold)
          }
        },
        modifier = Modifier.testTag("tab_history")
      )

      Tab(
        selected = selectedTab == 1,
        onClick = { selectedTab = 1 },
        text = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Programmées (${scheduledRides.size})", fontWeight = FontWeight.Bold)
          }
        },
        modifier = Modifier.testTag("tab_scheduled")
      )
    }

    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
      if (selectedTab == 0) {
        // Tab 0: Past Rides History
        if (onNavigateToInvoices != null) {
          Surface(
            shape = RoundedCornerShape(14.dp),
            color = LyonBlueLight,
            border = BorderStroke(1.dp, LyonBluePrimary.copy(alpha = 0.3f)),
            modifier = Modifier
              .fillMaxWidth()
              .clickable { onNavigateToInvoices() }
              .testTag("history_invoices_banner")
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
              ) {
                Surface(
                  shape = CircleShape,
                  color = LyonBluePrimary,
                  modifier = Modifier.size(32.dp)
                ) {
                  Box(contentAlignment = Alignment.Center) {
                    Icon(
                      imageVector = Icons.Default.ReceiptLong,
                      contentDescription = null,
                      tint = Color.White,
                      modifier = Modifier.size(18.dp)
                    )
                  }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                  Text(
                    text = "Factures & Notes de frais (PDF)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = LyonBlueDark
                  )
                  Text(
                    text = "Téléchargez vos justificatifs avec TVA 10% déductible",
                    fontSize = 11.sp,
                    color = AberDark.copy(alpha = 0.7f)
                  )
                }
              }
              Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = LyonBluePrimary
              )
            }
          }

          Spacer(modifier = Modifier.height(12.dp))
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Reçus & Justificatifs fiscaux",
            fontSize = 13.sp,
            color = AberGrayText,
            fontWeight = FontWeight.Medium
          )

          Box {
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = Color.White,
              border = BorderStroke(1.dp, AberBorder),
              modifier = Modifier
                .clickable { showFilterDropdown = true }
                .testTag("history_date_filter")
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = selectedFilter,
                  fontSize = 13.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = AberDark
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                  imageVector = Icons.Default.ArrowDropDown,
                  contentDescription = null,
                  tint = AberGrayText
                )
              }
            }

            DropdownMenu(
              expanded = showFilterDropdown,
              onDismissRequest = { showFilterDropdown = false }
            ) {
              filterOptions.forEach { opt ->
                DropdownMenuItem(
                  text = { Text(opt) },
                  onClick = {
                    selectedFilter = opt
                    showFilterDropdown = false
                  }
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (displayedHistory.isEmpty()) {
          Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
          ) {
            Text("Aucune course trouvée pour cette période.", color = AberGrayText, fontSize = 14.sp)
          }
        } else {
          LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
          ) {
            items(displayedHistory, key = { it.id }) { item ->
              HistoryCard(
                item = item,
                onClick = { selectedTripReceipt = item }
              )
            }
          }
        }
      } else {
        // Tab 1: Scheduled Rides
        if (scheduledRides.isEmpty()) {
          Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
          ) {
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Icon(
                imageVector = Icons.Outlined.EventAvailable,
                contentDescription = null,
                tint = AberGrayText,
                modifier = Modifier.size(54.dp)
              )
              Text("Aucune course programmée", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AberDark)
              Text(
                "Vous pouvez planifier un taxi à l'avance depuis l'écran de réservation.",
                color = AberGrayText,
                fontSize = 13.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
              )
            }
          }
        } else {
          LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
          ) {
            items(scheduledRides, key = { it.id }) { scheduled ->
              ScheduledRideCard(
                ride = scheduled,
                onCancel = {
                  onCancelScheduledRide(scheduled.id)
                  Toast.makeText(context, "Course du ${scheduled.scheduledDate} annulée", Toast.LENGTH_SHORT).show()
                },
                onSetReminder = {
                  Toast.makeText(context, "Rappel activé 15 min avant la prise en charge !", Toast.LENGTH_SHORT).show()
                }
              )
            }
          }
        }
      }
    }
  }

  // Professional Invoice & Receipt Dialog (Facture officielle avec TVA)
  if (selectedTripReceipt != null) {
    val trip = selectedTripReceipt!!
    val vatRate = 0.10 // 10% VAT in France for passenger transport
    val priceHt = trip.fare / (1 + vatRate)
    val vatAmount = trip.fare - priceHt

    AlertDialog(
      onDismissRequest = { selectedTripReceipt = null },
      title = {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Icon(
            imageVector = Icons.Default.ReceiptLong,
            contentDescription = null,
            tint = LyonBluePrimary,
            modifier = Modifier.size(24.dp)
          )
          Column {
            Text("Justificatif & Note de frais", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AberDark)
            Text("Facture N° FR-LYON-${trip.id.takeLast(6)}", fontSize = 11.5.sp, color = AberGrayText)
          }
        }
      },
      text = {
        Column(
          modifier = Modifier.fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          // Company Legal Header
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = AberGrayLight,
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
              Text("LyonTaxis SAS Métropole", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = AberDark)
              Text("SIRET : 849 203 112 00018 • TVA : FR 45 849203112", fontSize = 10.sp, color = AberGrayText)
              Text("Date d'émission : ${trip.date} • Règlement : ${trip.paymentMethod}", fontSize = 10.5.sp, color = AberDark)
            }
          }

          // Route Details
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color.White,
            border = BorderStroke(1.dp, AberBorder),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = CircleShape, color = LyonBluePrimary, modifier = Modifier.size(8.dp)) {}
                Spacer(modifier = Modifier.width(8.dp))
                Text(trip.pickupTitle, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = AberDark)
              }
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = AberRed, modifier = Modifier.size(10.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(trip.dropoffTitle, fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = AberDark)
              }
            }
          }

          // Trip metrics
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text("Chauffeur : ${trip.driverName}", fontSize = 11.5.sp, color = AberDark)
            Text("${trip.distanceKm} km • ${trip.durationMin} min", fontSize = 11.5.sp, fontWeight = FontWeight.Medium, color = AberGrayText)
          }

          HorizontalDivider(color = AberBorder)

          // Detailed Tax & Fare Breakdown (HT / TVA / TTC)
          Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text("Montant net hors taxes (HT) :", fontSize = 12.sp, color = AberGrayText)
              Text("${"%.2f".format(priceHt)} €", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = AberDark)
            }

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text("TVA Transport de personnes (10%) :", fontSize = 12.sp, color = AberGrayText)
              Text("${"%.2f".format(vatAmount)} €", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = AberDark)
            }

            if (trip.tip > 0) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Text("Pourboire chauffeur (0% TVA) :", fontSize = 12.sp, color = AberGrayText)
                Text("${"%.2f".format(trip.tip)} €", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = AberDark)
              }
            }

            HorizontalDivider(color = AberBorder.copy(alpha = 0.6f), modifier = Modifier.padding(vertical = 2.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text("Total payé TTC :", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AberDark)
              Text(
                "${"%.2f".format(trip.fare)} €",
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = LyonBluePrimary
              )
            }
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            Toast.makeText(context, "Facture téléchargée dans vos documents (PDF)", Toast.LENGTH_SHORT).show()
            selectedTripReceipt = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = LyonBluePrimary),
          shape = RoundedCornerShape(12.dp)
        ) {
          Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Télécharger PDF", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
        }
      },
      dismissButton = {
        OutlinedButton(
          onClick = {
            Toast.makeText(context, "Lien de la note de frais partagé !", Toast.LENGTH_SHORT).show()
          },
          shape = RoundedCornerShape(12.dp)
        ) {
          Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Partager", fontSize = 12.5.sp)
        }
      }
    )
  }
}

@Composable
private fun ScheduledRideCard(
  ride: ScheduledRideItem,
  onCancel: () -> Unit,
  onSetReminder: () -> Unit
) {
  val isCancelled = ride.status == "Annulée"

  Surface(
    shape = RoundedCornerShape(18.dp),
    color = Color.White,
    shadowElevation = 3.dp,
    border = BorderStroke(1.dp, if (isCancelled) AberBorder else LyonBluePrimary.copy(alpha = 0.4f)),
    modifier = Modifier
      .fillMaxWidth()
      .testTag("scheduled_card_${ride.id}")
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      // Date & Status Row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.Event,
            contentDescription = null,
            tint = if (isCancelled) AberGrayText else LyonBluePrimary,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "${ride.scheduledDate} à ${ride.scheduledTime}",
            fontSize = 14.sp,
            fontWeight = FontWeight.ExtraBold,
            color = if (isCancelled) AberGrayText else AberDark
          )
        }

        Surface(
          shape = RoundedCornerShape(10.dp),
          color = if (isCancelled) AberGrayLight else LyonBlueLight
        ) {
          Text(
            text = ride.status,
            color = if (isCancelled) AberGrayText else LyonBluePrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Pickup & Dropoff
      Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(shape = CircleShape, color = LyonBluePrimary, modifier = Modifier.size(8.dp)) {}
        Spacer(modifier = Modifier.width(10.dp))
        Text(
          text = ride.pickupLocation.title,
          fontSize = 13.5.sp,
          fontWeight = FontWeight.SemiBold,
          color = AberDark
        )
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
          text = ride.dropoffLocation.title,
          fontSize = 13.5.sp,
          fontWeight = FontWeight.Bold,
          color = AberDark
        )
      }

      // Preference Chips
      val activeOpts = buildList {
        if (ride.preferences.babySeat) add("Siège bébé")
        if (ride.preferences.pmrAccess) add("Accès PMR")
        if (ride.preferences.petFriendly) add("Animaux")
        if (ride.preferences.extraLuggage) add("Grand coffre")
        if (ride.preferences.silentRide) add("Silencieux")
      }

      if (activeOpts.isNotEmpty()) {
        Spacer(modifier = Modifier.height(8.dp))
        Row(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          activeOpts.forEach { opt ->
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = AberGrayLight
            ) {
              Text(
                text = "• $opt",
                fontSize = 11.sp,
                color = AberDark,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))
      HorizontalDivider(color = AberBorder.copy(alpha = 0.6f))
      Spacer(modifier = Modifier.height(10.dp))

      // Footer: Price & Actions
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(ride.vehicle.displayName, fontSize = 12.sp, color = AberGrayText)
          Text(
            text = "${"%.2f".format(ride.estimatedFare)} €",
            fontSize = 17.sp,
            fontWeight = FontWeight.ExtraBold,
            color = AberDark
          )
        }

        if (!isCancelled) {
          Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            IconButton(
              onClick = onSetReminder,
              modifier = Modifier.size(36.dp)
            ) {
              Icon(
                imageVector = Icons.Outlined.NotificationsActive,
                contentDescription = "Activer rappel",
                tint = LyonBluePrimary,
                modifier = Modifier.size(20.dp)
              )
            }

            TextButton(
              onClick = onCancel,
              colors = ButtonDefaults.textButtonColors(contentColor = AberRed)
            ) {
              Text("Annuler", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
          }
        }
      }
    }
  }
}

@Composable
private fun HistoryCard(
  item: TripHistoryItem,
  onClick: () -> Unit
) {
  val statusColor = when (item.status) {
    "Confirmé", "Confirm" -> LyonBluePrimary
    "Terminé", "Completed" -> LyonBluePrimary
    else -> AberGrayText
  }

  val statusBg = when (item.status) {
    "Confirmé", "Confirm" -> LyonBlueLight
    "Terminé", "Completed" -> LyonBlueLight
    else -> AberGrayLight
  }

  Surface(
    shape = RoundedCornerShape(18.dp),
    color = Color.White,
    shadowElevation = 3.dp,
    border = BorderStroke(1.dp, AberBorder),
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onClick() }
      .testTag("history_item_${item.id}")
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = item.date,
          fontSize = 12.sp,
          color = AberGrayText,
          fontWeight = FontWeight.Medium
        )

        // Status Badge
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = statusBg,
          modifier = Modifier.padding(2.dp)
        ) {
          Text(
            text = "${item.status} >",
            color = statusColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Pickup & Dropoff
      Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(shape = CircleShape, color = LyonBluePrimary, modifier = Modifier.size(8.dp)) {}
        Spacer(modifier = Modifier.width(10.dp))
        Text(
          text = item.pickupTitle,
          fontSize = 14.sp,
          fontWeight = FontWeight.SemiBold,
          color = AberDark
        )
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
          text = item.dropoffTitle,
          fontSize = 14.sp,
          fontWeight = FontWeight.Bold,
          color = AberDark
        )
      }

      Spacer(modifier = Modifier.height(12.dp))
      HorizontalDivider(color = AberBorder.copy(alpha = 0.6f))
      Spacer(modifier = Modifier.height(10.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Receipt, contentDescription = null, tint = LyonBluePrimary, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "Voir la facture",
            fontSize = 12.5.sp,
            color = LyonBluePrimary,
            fontWeight = FontWeight.SemiBold
          )
        }
        Text(
          text = "${"%.2f".format(item.fare)} €",
          fontSize = 17.sp,
          fontWeight = FontWeight.ExtraBold,
          color = AberDark
        )
      }
    }
  }
}


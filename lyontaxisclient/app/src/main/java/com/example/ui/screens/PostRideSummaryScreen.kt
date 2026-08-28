package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.RideBooking
import com.example.ui.components.AberHeader
import com.example.ui.components.LeafletMap
import com.example.ui.components.AberPrimaryButton
import com.example.ui.components.AberSecondaryButton
import com.example.ui.theme.*

@Composable
fun PostRideSummaryScreen(
  booking: RideBooking,
  onSubmitRatingAndFinish: (rating: Float, comment: String, tip: Double, compliments: List<String>) -> Unit,
  onBackToHome: () -> Unit,
  modifier: Modifier = Modifier
) {
  var rating by remember { mutableFloatStateOf(5.0f) }
  var reviewComment by remember { mutableStateOf("") }
  var selectedTip by remember { mutableDoubleStateOf(2.0) }
  var customTipInput by remember { mutableStateOf("") }
  var isCustomTipOpen by remember { mutableStateOf(false) }
  var showReceiptDownloadedSnackbar by remember { mutableStateOf(false) }

  val selectedCompliments = remember { mutableStateListOf<String>() }

  val availableCompliments = listOf(
    "🧼 Véhicule propre",
    "🛣️ Conduite souple",
    "💬 Courtois & Aimable",
    "⚡ Trajet rapide",
    "❄️ Climatisation agréable",
    "🎶 Bonne musique"
  )

  // Calculate live total cost
  val activeTip = if (isCustomTipOpen) (customTipInput.toDoubleOrNull() ?: 0.0) else selectedTip
  val totalCost = (booking.fare + activeTip).coerceAtLeast(0.0)

  val ratingSentiment = when {
    rating >= 5.0f -> "5.0 • Expérience exceptionnelle !"
    rating >= 4.0f -> "4.0 • Très bonne course !"
    rating >= 3.0f -> "3.0 • Trajet correct"
    rating >= 2.0f -> "2.0 • En dessous de la moyenne"
    else -> "1.0 • Mauvaise expérience"
  }

  Scaffold(
    snackbarHost = {
      if (showReceiptDownloadedSnackbar) {
        Snackbar(
          modifier = Modifier.padding(16.dp),
          action = {
            TextButton(onClick = { showReceiptDownloadedSnackbar = false }) {
              Text("OK", color = Color.White, fontWeight = FontWeight.Bold)
            }
          },
          containerColor = AberDark
        ) {
          Text("Reçu de course enregistré et envoyé par e-mail !", color = Color.White)
        }
      }
    },
    topBar = {
      AberHeader(
        title = "Récapitulatif de course",
        onBackClick = onBackToHome
      )
    },
    containerColor = AberBackground
  ) { innerPadding ->
    Column(
      modifier = modifier
        .fillMaxSize()
        .padding(innerPadding)
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 16.dp, vertical = 12.dp)
        .testTag("post_ride_summary_screen"),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // 1. Success Banner
      Surface(
        shape = RoundedCornerShape(20.dp),
        color = LyonBluePrimary,
        shadowElevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier.padding(16.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Surface(
            shape = CircleShape,
            color = Color.White.copy(alpha = 0.2f),
            modifier = Modifier.size(48.dp)
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(28.dp)
              )
            }
          }

          Spacer(modifier = Modifier.width(14.dp))

          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "Course terminée avec succès !",
              fontSize = 16.sp,
              fontWeight = FontWeight.Bold,
              color = Color.White
            )
            Text(
              text = "Réf : ${booking.id} • ${booking.formattedTime}",
              fontSize = 12.sp,
              color = Color.White.copy(alpha = 0.85f)
            )
          }
        }
      }

      // 2. Mini Map & Route Overview Card
      Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = BorderStroke(1.dp, AberBorder),
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = "Détails de l'itinéraire",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = AberDark
          )

          Spacer(modifier = Modifier.height(10.dp))

          // Mini Map Canvas
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(140.dp)
              .clip(RoundedCornerShape(14.dp))
          ) {
            LeafletMap(
              pickup = booking.pickupLocation,
              dropoff = booking.dropoffLocation,
              modifier = Modifier.fillMaxSize(),
              interactive = false
            )
          }

          Spacer(modifier = Modifier.height(16.dp))

          // Pickup Location Row
          Row(
            verticalAlignment = Alignment.Top,
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              modifier = Modifier.width(24.dp)
            ) {
              Surface(
                shape = CircleShape,
                color = LyonBluePrimary,
                modifier = Modifier.size(14.dp)
              ) {}
              Box(
                modifier = Modifier
                  .width(2.dp)
                  .height(36.dp)
                  .background(AberBorder)
              )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = booking.pickupLocation.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = AberDark
              )
              Text(
                text = booking.pickupLocation.address,
                fontSize = 12.sp,
                color = AberGrayText,
                lineHeight = 16.sp
              )
            }

            Text(
              text = "Départ 17:12",
              fontSize = 11.sp,
              fontWeight = FontWeight.SemiBold,
              color = LyonBluePrimary
            )
          }

          // Dropoff Location Row
          Row(
            verticalAlignment = Alignment.Top,
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              modifier = Modifier.width(24.dp)
            ) {
              Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = AberRed,
                modifier = Modifier.size(20.dp)
              )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = booking.dropoffLocation.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = AberDark
              )
              Text(
                text = booking.dropoffLocation.address,
                fontSize = 12.sp,
                color = AberGrayText,
                lineHeight = 16.sp
              )
            }

            Text(
              text = "Arrivée 17:26",
              fontSize = 11.sp,
              fontWeight = FontWeight.SemiBold,
              color = AberRed
            )
          }
        }
      }

      // 3. Trip Key Metrics (Distance, Duration, Vehicle, Eco Points)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        MetricTile(
          icon = Icons.Default.Navigation,
          label = "Distance",
          value = "${booking.distanceKm} km",
          modifier = Modifier.weight(1f)
        )
        MetricTile(
          icon = Icons.Default.Schedule,
          label = "Durée",
          value = "${booking.durationMin} min",
          modifier = Modifier.weight(1f)
        )
        MetricTile(
          icon = Icons.Default.DirectionsCar,
          label = "Véhicule",
          value = booking.vehicle.displayName,
          modifier = Modifier.weight(1f)
        )
      }

      // 4. Total Cost & Receipt Breakdown
      Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = BorderStroke(1.dp, AberBorder),
        shadowElevation = 2.dp,
        modifier = Modifier
          .fillMaxWidth()
          .testTag("post_ride_fare_breakdown_card")
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Détail du prix",
              fontSize = 15.sp,
              fontWeight = FontWeight.Bold,
              color = AberDark
            )

            Surface(
              shape = RoundedCornerShape(8.dp),
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
                  text = "Payé",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = LyonBluePrimary
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          CostLineItem(title = "Prise en charge de base", amount = "${"%.2f".format(booking.baseFare)} €")
          CostLineItem(title = "Tarif distance & temps", amount = "${"%.2f".format(booking.distanceFare + booking.timeFare)} €")
          CostLineItem(title = "Frais de service", amount = "${"%.2f".format(booking.serviceFee)} €")

          if (booking.discount > 0) {
            CostLineItem(
              title = "Code promo",
              amount = "-${"%.2f".format(booking.discount)} €",
              isHighlight = true
            )
          }

          if (activeTip > 0) {
            CostLineItem(
              title = "Pourboire chauffeur",
              amount = "+${"%.2f".format(activeTip)} €",
              isHighlight = false
            )
          }

          HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = AberBorder)

          // Total Cost Line
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "Total payé",
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = AberDark
              )
              Text(
                text = "Débité via ${booking.paymentMethodTitle}",
                fontSize = 12.sp,
                color = AberGrayText
              )
            }

            Text(
              text = "${"%.2f".format(totalCost)} €",
              fontSize = 22.sp,
              fontWeight = FontWeight.ExtraBold,
              color = LyonBluePrimary,
              modifier = Modifier.testTag("post_ride_total_cost_text")
            )
          }
        }
      }

      // 5. Driver Rating & Gratuity Card
      Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = BorderStroke(1.dp, AberBorder),
        shadowElevation = 2.dp,
        modifier = Modifier
          .fillMaxWidth()
          .testTag("post_ride_driver_rating_card")
      ) {
        Column(
          modifier = Modifier.padding(16.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Text(
            text = "Noter le chauffeur et l'expérience",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = AberDark,
            modifier = Modifier.fillMaxWidth()
          )

          Spacer(modifier = Modifier.height(14.dp))

          // Driver Profile Info
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Surface(
              shape = CircleShape,
              color = LyonBlueLight,
              border = BorderStroke(1.5.dp, LyonBluePrimary),
              modifier = Modifier.size(50.dp)
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(
                  imageVector = Icons.Default.Person,
                  contentDescription = null,
                  tint = LyonBluePrimary,
                  modifier = Modifier.size(30.dp)
                )
              }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = booking.driver.name,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = AberDark
              )
              Text(
                text = "${booking.driver.carModel} • ${booking.driver.licensePlate}",
                fontSize = 12.sp,
                color = AberGrayText
              )
            }

            Surface(
              shape = RoundedCornerShape(12.dp),
              color = AberYellow.copy(alpha = 0.15f)
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = Icons.Default.Star,
                  contentDescription = null,
                  tint = AberYellow,
                  modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "${booking.driver.rating}",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                  color = AberDark
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          // 5-Star Interactive Rating Row
          Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            for (i in 1..5) {
              val isFilled = i <= rating
              IconButton(
                onClick = { rating = i.toFloat() },
                modifier = Modifier
                  .size(44.dp)
                  .testTag("star_rate_$i")
              ) {
                Icon(
                  imageVector = if (isFilled) Icons.Default.Star else Icons.Outlined.StarBorder,
                  contentDescription = "$i étoile",
                  tint = if (isFilled) AberYellow else AberGrayText.copy(alpha = 0.5f),
                  modifier = Modifier.size(38.dp)
                )
              }
            }
          }

          Text(
            text = ratingSentiment,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = AberDark
          )

          Spacer(modifier = Modifier.height(16.dp))

          // Compliments Grid
          Text(
            text = "Laisser des compliments",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = AberDark,
            modifier = Modifier.fillMaxWidth()
          )

          Spacer(modifier = Modifier.height(8.dp))

          FlowRowLayout(
            items = availableCompliments,
            selectedItems = selectedCompliments,
            onToggle = { tag ->
              if (selectedCompliments.contains(tag)) {
                selectedCompliments.remove(tag)
              } else {
                selectedCompliments.add(tag)
              }
            }
          )

          Spacer(modifier = Modifier.height(16.dp))

          // Tip Selector Section
          Text(
            text = "Ajouter un pourboire",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = AberDark,
            modifier = Modifier.fillMaxWidth()
          )

          Spacer(modifier = Modifier.height(8.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            val tipPresets = listOf(0.0, 1.0, 2.0, 5.0)
            tipPresets.forEach { amt ->
              val isSelected = (!isCustomTipOpen && selectedTip == amt)
              val label = if (amt == 0.0) "Aucun" else "${amt.toInt()} €"
              Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) LyonBluePrimary else AberGrayLight,
                border = BorderStroke(1.dp, if (isSelected) LyonBluePrimary else AberBorder),
                modifier = Modifier
                  .weight(1f)
                  .height(42.dp)
                  .clickable {
                    selectedTip = amt
                    isCustomTipOpen = false
                  }
                  .testTag("tip_preset_${amt.toInt()}")
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Text(
                    text = label,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) Color.White else AberDark
                  )
                }
              }
            }

            // Custom Tip Option
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = if (isCustomTipOpen) LyonBluePrimary else AberGrayLight,
              border = BorderStroke(1.dp, if (isCustomTipOpen) LyonBluePrimary else AberBorder),
              modifier = Modifier
                .weight(1f)
                .height(42.dp)
                .clickable {
                  isCustomTipOpen = true
                }
                .testTag("tip_custom_button")
            ) {
              Box(contentAlignment = Alignment.Center) {
                Text(
                  text = "Autre",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (isCustomTipOpen) Color.White else AberDark
                )
              }
            }
          }

          if (isCustomTipOpen) {
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
              value = customTipInput,
              onValueChange = { customTipInput = it },
              placeholder = { Text("Entrer le montant (€)", fontSize = 12.sp) },
              singleLine = true,
              shape = RoundedCornerShape(12.dp),
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = LyonBluePrimary,
                unfocusedBorderColor = AberBorder
              ),
              modifier = Modifier
                .fillMaxWidth()
                .testTag("custom_tip_text_field")
            )
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Written Review Comment
          OutlinedTextField(
            value = reviewComment,
            onValueChange = { reviewComment = it },
            placeholder = {
              Text(
                "Laisser un message pour ${booking.driver.name.split(" ").first()} (facultatif)...",
                color = AberGrayText,
                fontSize = 12.sp
              )
            },
            modifier = Modifier
              .fillMaxWidth()
              .height(90.dp)
              .testTag("post_ride_review_input"),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = LyonBluePrimary,
              unfocusedBorderColor = AberBorder,
              focusedContainerColor = AberGrayLight,
              unfocusedContainerColor = AberGrayLight
            ),
            textStyle = LocalTextStyle.current.copy(fontSize = 12.sp)
          )
        }
      }

      // 6. Action Buttons
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        AberPrimaryButton(
          text = "Valider et terminer",
          onClick = {
            onSubmitRatingAndFinish(
              rating,
              reviewComment,
              activeTip,
              selectedCompliments.toList()
            )
          },
          icon = Icons.Default.Check,
          testTag = "post_ride_submit_done_button"
        )

        AberSecondaryButton(
          text = "Télécharger le reçu",
          onClick = {
            showReceiptDownloadedSnackbar = true
          },
          backgroundColor = LyonBluePrimary,
          testTag = "post_ride_download_receipt_button"
        )
      }

      Spacer(modifier = Modifier.height(20.dp))
    }
  }
}

@Composable
private fun MetricTile(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  label: String,
  value: String,
  modifier: Modifier = Modifier
) {
  Surface(
    shape = RoundedCornerShape(16.dp),
    color = Color.White,
    border = BorderStroke(1.dp, AberBorder),
    shadowElevation = 1.dp,
    modifier = modifier
  ) {
    Column(
      modifier = Modifier.padding(12.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = LyonBluePrimary,
        modifier = Modifier.size(20.dp)
      )
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = value,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        color = AberDark,
        textAlign = TextAlign.Center
      )
      Text(
        text = label,
        fontSize = 10.sp,
        color = AberGrayText,
        textAlign = TextAlign.Center
      )
    }
  }
}

@Composable
private fun CostLineItem(
  title: String,
  amount: String,
  isHighlight: Boolean = false
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 4.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(
      text = title,
      fontSize = 13.sp,
      color = if (isHighlight) LyonBluePrimary else AberDark,
      fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.Normal
    )
    Text(
      text = amount,
      fontSize = 13.sp,
      fontWeight = FontWeight.Bold,
      color = if (isHighlight) LyonBluePrimary else AberDark
    )
  }
}

@Composable
private fun FlowRowLayout(
  items: List<String>,
  selectedItems: List<String>,
  onToggle: (String) -> Unit
) {
  Column(
    modifier = Modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    val rows = items.chunked(2)
    rows.forEach { rowItems ->
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        rowItems.forEach { item ->
          val isSelected = selectedItems.contains(item)
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (isSelected) LyonBlueLight else AberGrayLight,
            border = BorderStroke(1.dp, if (isSelected) LyonBluePrimary else AberBorder),
            modifier = Modifier
              .weight(1f)
              .clickable { onToggle(item) }
          ) {
            Box(
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = item,
                fontSize = 11.5.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) LyonBluePrimary else AberDark
              )
            }
          }
        }
        if (rowItems.size == 1) {
          Spacer(modifier = Modifier.weight(1f))
        }
      }
    }
  }
}

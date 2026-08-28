package com.example.ui.components

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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.LocationPoint
import com.example.model.VehicleCategory
import com.example.ui.theme.*

val PRESET_INTERMEDIATE_STOPS = listOf(
  LocationPoint("stop_cafe", "Café des Fédérations", "8 Rue Major Martin, 69001 Lyon", 1.2),
  LocationPoint("stop_pharmacie", "Pharmacie Centrale Bellecour", "14 Rue Victor Hugo, 69002 Lyon", 1.5),
  LocationPoint("stop_monoprix", "Monoprix Cordeliers", "27 Rue de la République, 69002 Lyon", 1.8),
  LocationPoint("stop_bnp", "BNP Paribas Part-Dieu", "Place Charles Béraudier, 69003 Lyon", 2.1),
  LocationPoint("stop_paul", "Boulangerie Paul Bellecour", "Place Bellecour, 69002 Lyon", 0.9),
  LocationPoint("stop_pharmacie2", "Pharmacie de la Croix-Rousse", "1 Place de la Croix-Rousse, 69004 Lyon", 2.8),
  LocationPoint("stop_carrefour", "Carrefour Express Confluence", "54 Cours Charlemagne, 69002 Lyon", 2.3),
  LocationPoint("stop_ca", "Crédit Agricole Terreaux", "12 Place des Terreaux, 69001 Lyon", 1.4)
)

data class StopCategory(
  val name: String,
  val icon: ImageVector,
  val locations: List<LocationPoint>
)

val STOP_CATEGORIES = listOf(
  StopCategory("Toutes les suggestions", Icons.Default.Explore, PRESET_INTERMEDIATE_STOPS),
  StopCategory("Cafés & Boulangeries", Icons.Default.LocalCafe, PRESET_INTERMEDIATE_STOPS.filter { it.title.contains("Café") || it.title.contains("Paul") }),
  StopCategory("Pharmacies & Santé", Icons.Default.MedicalServices, PRESET_INTERMEDIATE_STOPS.filter { it.title.contains("Pharmacie") }),
  StopCategory("Supermarchés & Courses", Icons.Default.ShoppingCart, PRESET_INTERMEDIATE_STOPS.filter { it.title.contains("Monoprix") || it.title.contains("Carrefour") }),
  StopCategory("Banques & Distributeurs", Icons.Default.AccountBalance, PRESET_INTERMEDIATE_STOPS.filter { it.title.contains("BNP") || it.title.contains("Crédit") })
)

@Composable
fun MultiStopRouteCard(
  pickupLocation: LocationPoint,
  dropoffLocation: LocationPoint,
  intermediateStops: List<LocationPoint>,
  onAddStopClick: () -> Unit,
  onRemoveStop: (String) -> Unit,
  onMoveStopUp: ((Int) -> Unit)? = null,
  onMoveStopDown: ((Int) -> Unit)? = null,
  modifier: Modifier = Modifier,
  maxStops: Int = 4
) {
  Surface(
    shape = RoundedCornerShape(20.dp),
    color = Color.White,
    shadowElevation = 4.dp,
    border = BorderStroke(1.dp, AberBorder),
    modifier = modifier
      .fillMaxWidth()
      .testTag("multi_stop_route_card")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp)
    ) {
      // Header: Title & Stop Counter
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.AltRoute,
            contentDescription = null,
            tint = LyonBluePrimary,
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Itinéraire du trajet",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = AberDark
          )
        }

        if (intermediateStops.isNotEmpty()) {
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = LyonBlueLight,
            border = BorderStroke(1.dp, LyonBluePrimary)
          ) {
            Text(
              text = "${intermediateStops.size} arrêt${if (intermediateStops.size > 1) "s" else ""} ajouté${if (intermediateStops.size > 1) "s" else ""}",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = LyonBluePrimary,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Point 1: Pickup Location
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Surface(
          shape = CircleShape,
          color = LyonBlueLight,
          border = BorderStroke(2.dp, LyonBluePrimary),
          modifier = Modifier.size(20.dp)
        ) {
          Box(contentAlignment = Alignment.Center) {
            Text(
              text = "A",
              fontSize = 10.sp,
              fontWeight = FontWeight.ExtraBold,
              color = LyonBluePrimary
            )
          }
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "Point de départ",
            fontSize = 10.5.sp,
            fontWeight = FontWeight.SemiBold,
            color = AberGrayText
          )
          Text(
            text = pickupLocation.title,
            fontSize = 13.5.sp,
            fontWeight = FontWeight.Bold,
            color = AberDark,
            maxLines = 1
          )
        }
      }

      // Intermediate Stops List
      intermediateStops.forEachIndexed { index, stop ->
        // Connector Line
        Row(modifier = Modifier.padding(start = 9.dp)) {
          Box(
            modifier = Modifier
              .width(2.dp)
              .height(16.dp)
              .background(LyonBluePrimary.copy(alpha = 0.4f))
          )
        }

        // Stop Row
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = AberAmber.copy(alpha = 0.08f),
          border = BorderStroke(1.dp, AberAmber.copy(alpha = 0.4f)),
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .testTag("intermediate_stop_row_$index")
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Surface(
              shape = CircleShape,
              color = AberAmber,
              modifier = Modifier.size(20.dp)
            ) {
              Box(contentAlignment = Alignment.Center) {
                Text(
                  text = "${index + 1}",
                  fontSize = 10.sp,
                  fontWeight = FontWeight.ExtraBold,
                  color = Color.White
                )
              }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = "Arrêt ${index + 1}",
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  color = AberAmber
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "+3,50 € · +2,4 km",
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Medium,
                  color = AberGrayText
                )
              }
              Text(
                text = stop.title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = AberDark,
                maxLines = 1
              )
              Text(
                text = stop.address,
                fontSize = 11.sp,
                color = AberGrayText,
                maxLines = 1
              )
            }

            // Move Up/Down Controls if applicable
            if (intermediateStops.size > 1) {
              if (index > 0 && onMoveStopUp != null) {
                IconButton(
                  onClick = { onMoveStopUp(index) },
                  modifier = Modifier.size(26.dp)
                ) {
                  Icon(
                    imageVector = Icons.Default.KeyboardArrowUp,
                    contentDescription = "Monter",
                    tint = AberDark,
                    modifier = Modifier.size(16.dp)
                  )
                }
              }
              if (index < intermediateStops.size - 1 && onMoveStopDown != null) {
                IconButton(
                  onClick = { onMoveStopDown(index) },
                  modifier = Modifier.size(26.dp)
                ) {
                  Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Descendre",
                    tint = AberDark,
                    modifier = Modifier.size(16.dp)
                  )
                }
              }
            }

            // Remove Button
            IconButton(
              onClick = { onRemoveStop(stop.id) },
              modifier = Modifier
                .size(28.dp)
                .testTag("remove_stop_button_${stop.id}")
            ) {
              Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Supprimer l'arrêt",
                tint = AberRed,
                modifier = Modifier.size(16.dp)
              )
            }
          }
        }
      }

      // Connector Line to Destination
      Row(modifier = Modifier.padding(start = 9.dp)) {
        Box(
          modifier = Modifier
            .width(2.dp)
            .height(16.dp)
            .background(LyonBluePrimary.copy(alpha = 0.4f))
        )
      }

      // Destination Dropoff Point
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Surface(
          shape = CircleShape,
          color = AberRed.copy(alpha = 0.15f),
          border = BorderStroke(2.dp, AberRed),
          modifier = Modifier.size(20.dp)
        ) {
          Box(contentAlignment = Alignment.Center) {
            Text(
              text = "B",
              fontSize = 10.sp,
              fontWeight = FontWeight.ExtraBold,
              color = AberRed
            )
          }
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "Destination finale",
            fontSize = 10.5.sp,
            fontWeight = FontWeight.SemiBold,
            color = AberGrayText
          )
          Text(
            text = dropoffLocation.title,
            fontSize = 13.5.sp,
            fontWeight = FontWeight.Bold,
            color = AberDark,
            maxLines = 1
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Add Stop Action Button
      if (intermediateStops.size < maxStops) {
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = LyonBlueLight,
          border = BorderStroke(1.dp, LyonBluePrimary),
          modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onAddStopClick)
            .testTag("add_intermediate_stop_button")
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 9.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.AddCircleOutline,
              contentDescription = null,
              tint = LyonBluePrimary,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Ajouter un arrêt sur le trajet (+ 3,50 €)",
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold,
              color = LyonBluePrimary
            )
          }
        }
      }
    }
  }
}

@Composable
fun AddIntermediateStopDialog(
  popularLocations: List<LocationPoint>,
  onSelectStop: (LocationPoint) -> Unit,
  onDismiss: () -> Unit
) {
  var searchQuery by remember { mutableStateOf("") }
  var selectedCategoryIndex by remember { mutableIntStateOf(0) }
  var customStopTitle by remember { mutableStateOf("") }
  var customStopAddress by remember { mutableStateOf("") }
  var isCustomMode by remember { mutableStateOf(false) }

  val currentCategory = STOP_CATEGORIES.getOrElse(selectedCategoryIndex) { STOP_CATEGORIES.first() }

  val filteredLocations = remember(searchQuery, currentCategory, popularLocations) {
    val pool = if (selectedCategoryIndex == 0) (PRESET_INTERMEDIATE_STOPS + popularLocations).distinctBy { it.id } else currentCategory.locations
    if (searchQuery.isBlank()) pool
    else pool.filter {
      it.title.contains(searchQuery, ignoreCase = true) ||
        it.address.contains(searchQuery, ignoreCase = true)
    }
  }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Surface(
      shape = RoundedCornerShape(24.dp),
      color = Color.White,
      shadowElevation = 24.dp,
      modifier = Modifier
        .fillMaxWidth(0.94f)
        .fillMaxHeight(0.85f)
        .testTag("add_intermediate_stop_dialog")
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(18.dp)
      ) {
        // Dialog Title & Close
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "Ajouter un arrêt intermédiaire",
              fontSize = 18.sp,
              fontWeight = FontWeight.ExtraBold,
              color = AberDark
            )
            Text(
              text = "Faites un arrêt avant la destination finale (+ 3,50 €)",
              fontSize = 11.5.sp,
              color = AberGrayText
            )
          }

          IconButton(
            onClick = onDismiss,
            modifier = Modifier.testTag("dismiss_stop_dialog_button")
          ) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Fermer",
              tint = AberDark
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search Bar
        OutlinedTextField(
          value = searchQuery,
          onValueChange = { searchQuery = it },
          placeholder = { Text("Rechercher café, pharmacie, boutique, adresse...", fontSize = 13.sp, color = AberGrayText) },
          leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = null, tint = LyonBluePrimary)
          },
          trailingIcon = {
            if (searchQuery.isNotEmpty()) {
              IconButton(onClick = { searchQuery = "" }) {
                Icon(Icons.Default.Clear, contentDescription = "Effacer", tint = AberGrayText)
              }
            }
          },
          singleLine = true,
          shape = RoundedCornerShape(14.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = LyonBluePrimary,
            unfocusedBorderColor = AberBorder,
            focusedContainerColor = AberGrayLight,
            unfocusedContainerColor = AberGrayLight
          ),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("search_intermediate_stop_input")
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Category Filter Chips
        LazyRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          contentPadding = PaddingValues(vertical = 4.dp)
        ) {
          items(STOP_CATEGORIES.indices.toList()) { idx ->
            val cat = STOP_CATEGORIES[idx]
            val isSelected = selectedCategoryIndex == idx
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = if (isSelected) LyonBluePrimary else AberGrayLight,
              border = BorderStroke(1.dp, if (isSelected) LyonBluePrimary else AberBorder),
              modifier = Modifier
                .clickable {
                  selectedCategoryIndex = idx
                  isCustomMode = false
                }
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = cat.icon,
                  contentDescription = null,
                  tint = if (isSelected) Color.White else AberDark,
                  modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = cat.name,
                  fontSize = 12.sp,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                  color = if (isSelected) Color.White else AberDark
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Custom Address Input Toggle
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Lieux suggérés à Lyon",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = AberDark
          )

          TextButton(
            onClick = { isCustomMode = !isCustomMode },
            contentPadding = PaddingValues(0.dp)
          ) {
            Text(
              text = if (isCustomMode) "Suggestions" else "+ Adresse libre",
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = LyonBluePrimary
            )
          }
        }

        if (isCustomMode) {
          Surface(
            shape = RoundedCornerShape(14.dp),
            color = AberGrayLight,
            border = BorderStroke(1.dp, AberBorder),
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 6.dp)
          ) {
            Column(modifier = Modifier.padding(12.dp)) {
              Text(
                text = "Entrer un lieu ou une adresse personnalisée",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = AberDark
              )
              Spacer(modifier = Modifier.height(8.dp))
              OutlinedTextField(
                value = customStopTitle,
                onValueChange = { customStopTitle = it },
                placeholder = { Text("Ex: Salle de sport, Chez un ami", fontSize = 12.sp) },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
              )
              Spacer(modifier = Modifier.height(6.dp))
              OutlinedTextField(
                value = customStopAddress,
                onValueChange = { customStopAddress = it },
                placeholder = { Text("Ex: 15 Rue de la République, Lyon", fontSize = 12.sp) },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
              )
              Spacer(modifier = Modifier.height(10.dp))
              Button(
                onClick = {
                  if (customStopTitle.isNotBlank()) {
                    val newStop = LocationPoint(
                      id = "custom_stop_${System.currentTimeMillis()}",
                      title = customStopTitle.trim(),
                      address = customStopAddress.ifBlank { "Lyon, France" }.trim(),
                      distanceKm = 2.4
                    )
                    onSelectStop(newStop)
                  }
                },
                enabled = customStopTitle.isNotBlank(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LyonBluePrimary),
                modifier = Modifier.fillMaxWidth()
              ) {
                Text("Ajouter cet arrêt (+ 3,50 €)", fontWeight = FontWeight.Bold)
              }
            }
          }
        }

        // Suggestions List
        LazyColumn(
          modifier = Modifier.weight(1f),
          verticalArrangement = Arrangement.spacedBy(8.dp),
          contentPadding = PaddingValues(vertical = 4.dp)
        ) {
          items(filteredLocations) { loc ->
            Surface(
              shape = RoundedCornerShape(14.dp),
              color = AberGrayLight,
              border = BorderStroke(1.dp, AberBorder),
              modifier = Modifier
                .fillMaxWidth()
                .clickable { onSelectStop(loc) }
                .testTag("select_stop_${loc.id}")
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Surface(
                  shape = CircleShape,
                  color = LyonBlueLight,
                  modifier = Modifier.size(36.dp)
                ) {
                  Box(contentAlignment = Alignment.Center) {
                    Icon(
                      imageVector = when {
                        loc.title.contains("Café") || loc.title.contains("Paul") -> Icons.Default.LocalCafe
                        loc.title.contains("Pharmacie") -> Icons.Default.MedicalServices
                        loc.title.contains("Monoprix") || loc.title.contains("Carrefour") -> Icons.Default.ShoppingCart
                        loc.title.contains("BNP") || loc.title.contains("Crédit") -> Icons.Default.AccountBalance
                        else -> Icons.Default.Place
                      },
                      contentDescription = null,
                      tint = LyonBluePrimary,
                      modifier = Modifier.size(18.dp)
                    )
                  }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = loc.title,
                    fontSize = 13.5.sp,
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

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                  shape = RoundedCornerShape(8.dp),
                  color = LyonBluePrimary.copy(alpha = 0.12f)
                ) {
                  Text(
                    text = "+ 3,50 €",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = LyonBluePrimary,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
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

@Composable
fun DynamicPriceBreakdownCard(
  selectedVehicle: VehicleCategory,
  intermediateStopsCount: Int,
  discountAmount: Double,
  modifier: Modifier = Modifier
) {
  var isExpanded by remember { mutableStateOf(false) }

  val stopSurcharge = intermediateStopsCount * 3.50
  val extraDistanceKm = intermediateStopsCount * 2.4
  val extraDistanceFare = intermediateStopsCount * 3.60
  val totalBaseAndStops = selectedVehicle.basePrice + stopSurcharge + extraDistanceFare
  val finalTotalFare = (totalBaseAndStops - discountAmount).coerceAtLeast(5.0)

  Surface(
    shape = RoundedCornerShape(16.dp),
    color = AberGrayLight,
    border = BorderStroke(1.dp, AberBorder),
    modifier = modifier
      .fillMaxWidth()
      .testTag("dynamic_price_breakdown_card")
  ) {
    Column(modifier = Modifier.padding(12.dp)) {
      // Toggle Row
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clickable { isExpanded = !isExpanded },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.ReceiptLong,
            contentDescription = null,
            tint = LyonBluePrimary,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "Détail de l'estimation tarifaire",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = AberDark
          )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = "${"%.2f".format(finalTotalFare)} €",
            fontSize = 15.sp,
            fontWeight = FontWeight.ExtraBold,
            color = LyonBluePrimary
          )
          Icon(
            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
            contentDescription = null,
            tint = AberDark,
            modifier = Modifier.size(18.dp)
          )
        }
      }

      AnimatedVisibility(visible = isExpanded) {
        Column(modifier = Modifier.padding(top = 10.dp)) {
          HorizontalDivider(color = AberBorder)
          Spacer(modifier = Modifier.height(8.dp))

          // Base Rate
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(text = "Prise en charge ${selectedVehicle.displayName}", fontSize = 12.sp, color = AberGrayText)
            Text(text = "${"%.2f".format(selectedVehicle.basePrice)} €", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = AberDark)
          }

          // Intermediate Stops Surcharge
          if (intermediateStopsCount > 0) {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(
                text = "Arrêts intermédiaires ($intermediateStopsCount × 3,50 €)",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = AberAmber
              )
              Text(
                text = "+${"%.2f".format(stopSurcharge)} €",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = AberAmber
              )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(
                text = "Distance additionnelle (+${"%.1f".format(extraDistanceKm)} km)",
                fontSize = 12.sp,
                color = AberGrayText
              )
              Text(
                text = "+${"%.2f".format(extraDistanceFare)} €",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = AberDark
              )
            }
          }

          if (discountAmount > 0) {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(text = "Code promo déduit", fontSize = 12.sp, color = LyonBluePrimary, fontWeight = FontWeight.SemiBold)
              Text(text = "-${"%.2f".format(discountAmount)} €", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = LyonBluePrimary)
            }
          }

          Spacer(modifier = Modifier.height(8.dp))
          HorizontalDivider(color = AberBorder)
          Spacer(modifier = Modifier.height(6.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(text = "Tarif total estimé", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AberDark)
            Text(text = "${"%.2f".format(finalTotalFare)} €", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = LyonBluePrimary)
          }
        }
      }
    }
  }
}


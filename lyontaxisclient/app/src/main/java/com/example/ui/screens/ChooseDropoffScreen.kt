package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Map
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
import com.example.data.LyonGeocoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.ui.components.AberLocationRow
import com.example.ui.theme.*

@Composable
fun ChooseDropoffScreen(
  pickupLocation: LocationPoint,
  dropoffLocation: LocationPoint,
  popularLocations: List<LocationPoint>,
  onSelectLocation: (LocationPoint) -> Unit,
  onPickOnMapClick: () -> Unit,
  onFavoriteToggle: (String) -> Unit,
  onBackClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  var searchQuery by remember { mutableStateOf(dropoffLocation.title) }
  var remoteLocations by remember { mutableStateOf<List<LocationPoint>>(emptyList()) }
  val geocoder = remember { LyonGeocoder() }

  LaunchedEffect(searchQuery) {
    if (searchQuery.trim().length < 3) {
      remoteLocations = emptyList()
    } else {
      kotlinx.coroutines.delay(600)
      remoteLocations = runCatching {
        withContext(Dispatchers.IO) {
          geocoder.search(searchQuery).map { result ->
            LocationPoint(
              id = "nominatim_${result.place_id}",
              title = result.display_name.substringBefore(","),
              address = result.display_name,
              distanceKm = 0.0,
              latitude = result.lat.toDouble(),
              longitude = result.lon.toDouble()
            )
          }
        }
      }.getOrDefault(emptyList())
    }
  }

  val filteredLocations = remember(searchQuery, popularLocations) {
    if (searchQuery.isBlank()) popularLocations
    else popularLocations.filter {
      it.title.contains(searchQuery, ignoreCase = true) ||
        it.address.contains(searchQuery, ignoreCase = true)
    }
  }
  val displayedLocations = (remoteLocations + filteredLocations).distinctBy { it.id }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(Color.White)
  ) {
    // Top Search Header Card with Shadow
    Surface(
      color = Color.White,
      shadowElevation = 8.dp,
      modifier = Modifier
        .fillMaxWidth()
        .statusBarsPadding()
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 12.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          IconButton(
            onClick = onBackClick,
            modifier = Modifier.testTag("dropoff_back_button")
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back",
              tint = AberDark
            )
          }

          Text(
            text = "Choisir la destination",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = AberDark,
            modifier = Modifier.weight(1f)
          )

          // Pick on Map Shortcut Button
          IconButton(
            onClick = onPickOnMapClick,
            modifier = Modifier.testTag("pick_on_map_shortcut")
          ) {
            Icon(
              imageVector = Icons.Outlined.Map,
              contentDescription = "Carte",
              tint = LyonBluePrimary
            )
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Dual Search Box (Pickup & Drop-off)
        Surface(
          shape = RoundedCornerShape(16.dp),
          color = AberGrayLight,
          border = BorderStroke(1.dp, AberBorder),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            // Pickup
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.fillMaxWidth()
            ) {
              Surface(
                shape = CircleShape,
                color = LyonBlueLight,
                border = BorderStroke(2.dp, LyonBluePrimary),
                modifier = Modifier.size(14.dp)
              ) {}

              Spacer(modifier = Modifier.width(12.dp))

              Text(
                text = pickupLocation.title,
                fontSize = 14.sp,
                color = AberDark,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
              )
            }

            HorizontalDivider(
              modifier = Modifier.padding(start = 26.dp, top = 8.dp, bottom = 8.dp),
              color = AberBorder
            )

            // Drop-off with live search & clear 'X'
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.fillMaxWidth()
            ) {
              Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = AberRed,
                modifier = Modifier.size(18.dp)
              )

              Spacer(modifier = Modifier.width(8.dp))

              TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Où allez-vous ?", color = AberGrayText, fontSize = 14.sp) },
                singleLine = true,
                colors = TextFieldDefaults.colors(
                  focusedContainerColor = Color.Transparent,
                  unfocusedContainerColor = Color.Transparent,
                  focusedIndicatorColor = Color.Transparent,
                  unfocusedIndicatorColor = Color.Transparent
                ),
                modifier = Modifier
                  .weight(1f)
                  .testTag("destination_search_input")
              )

              if (searchQuery.isNotEmpty()) {
                IconButton(
                  onClick = { searchQuery = "" },
                  modifier = Modifier.size(24.dp)
                ) {
                  Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Effacer",
                    tint = AberGrayText,
                    modifier = Modifier.size(16.dp)
                  )
                }
              }
            }
          }
        }
      }
    }

    // Popular Locations List
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(top = 8.dp)
    ) {
      item {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = if (remoteLocations.isNotEmpty()) "ADRESSES À LYON" else "LIEUX POPULAIRES À LYON",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = AberGrayText,
            letterSpacing = 0.5.sp
          )

          TextButton(onClick = onPickOnMapClick) {
            Text(
              text = "Sur la carte",
              color = LyonBluePrimary,
              fontWeight = FontWeight.Bold,
              fontSize = 13.sp
            )
          }
        }
      }

      items(displayedLocations, key = { it.id }) { loc ->
        AberLocationRow(
          location = loc,
          onClick = { onSelectLocation(loc) },
          onFavoriteToggle = { onFavoriteToggle(loc.id) }
        )
        HorizontalDivider(
          modifier = Modifier.padding(start = 68.dp, end = 16.dp),
          color = AberBorder.copy(alpha = 0.7f),
          thickness = 0.8.dp
        )
      }
    }
  }
}

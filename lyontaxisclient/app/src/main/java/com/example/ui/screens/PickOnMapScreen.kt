package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LocationPoint
import com.example.ui.components.LeafletMap
import com.example.ui.components.AberPrimaryButton
import com.example.ui.theme.*

@Composable
fun PickOnMapScreen(
  pickupLocation: LocationPoint? = null,
  currentLocation: LocationPoint,
  onApplyLocation: (LocationPoint) -> Unit,
  onBackClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  var selectedLocation by remember { mutableStateOf(currentLocation) }
  var addressText by remember { mutableStateOf(currentLocation.title) }

  Box(modifier = modifier.fillMaxSize()) {
    // Interactive Full-bleed map with centered pin
    LeafletMap(
      pickup = pickupLocation,
      dropoff = selectedLocation,
      modifier = Modifier.fillMaxSize(),
      interactive = true,
      onMapTap = { latitude, longitude ->
        selectedLocation = selectedLocation.copy(
          title = "Point sélectionné",
          address = "${"%.5f".format(latitude)}, ${"%.5f".format(longitude)}",
          latitude = latitude,
          longitude = longitude
        )
        addressText = "Point sélectionné"
      }
    )

    // Top Search Bar
    Surface(
      shape = RoundedCornerShape(18.dp),
      color = Color.White,
      shadowElevation = 8.dp,
      border = BorderStroke(1.dp, AberBorder),
      modifier = Modifier
        .fillMaxWidth()
        .statusBarsPadding()
        .padding(horizontal = 16.dp, vertical = 12.dp)
        .align(Alignment.TopCenter)
    ) {
      Row(
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(
          onClick = onBackClick,
          modifier = Modifier.testTag("pick_on_map_back")
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            tint = AberDark
          )
        }

        Icon(
          imageVector = Icons.Default.LocationOn,
          contentDescription = null,
          tint = AberRed,
          modifier = Modifier.size(20.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        TextField(
          value = addressText,
          onValueChange = { addressText = it },
          placeholder = { Text("Préciser le repère sur la carte", color = AberGrayText) },
          singleLine = true,
          colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
          ),
          modifier = Modifier
            .weight(1f)
            .testTag("pick_on_map_input")
        )

        if (addressText.isNotEmpty()) {
          IconButton(onClick = { addressText = "" }) {
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

    // Bottom Action Card with Apply Button
    Surface(
      shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
      color = Color.White,
      shadowElevation = 16.dp,
      modifier = Modifier
        .fillMaxWidth()
        .align(Alignment.BottomCenter)
        .navigationBarsPadding()
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp)
      ) {
        Text(
          text = "Confirmer l'emplacement",
          fontSize = 17.sp,
          fontWeight = FontWeight.Bold,
          color = AberDark
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
          text = if (addressText.isBlank()) "Place Bellecour, 69002 Lyon" else addressText,
          fontSize = 14.sp,
          color = AberGrayText
        )

        Spacer(modifier = Modifier.height(18.dp))

        AberPrimaryButton(
          text = "Confirmer ce point",
          onClick = {
            val updated = selectedLocation.copy(
              title = if (addressText.isBlank()) "Place Bellecour, 69002 Lyon" else addressText,
              address = if (addressText.isBlank()) "Place Bellecour, 69002 Lyon" else addressText
            )
            onApplyLocation(updated)
          },
          testTag = "pick_on_map_apply_button"
        )
      }
    }
  }
}

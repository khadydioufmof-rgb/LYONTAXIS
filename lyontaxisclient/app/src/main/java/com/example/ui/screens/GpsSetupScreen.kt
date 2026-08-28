package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AberOutlinedButton
import com.example.ui.components.AberPrimaryButton
import com.example.ui.theme.*
import com.example.model.LocationPoint
import com.google.android.gms.location.LocationServices

@Composable
fun GpsSetupScreen(
  onUseCurrentLocation: (LocationPoint) -> Unit,
  onSelectManually: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
  val requestLocationPermission = rememberLauncherForActivityResult(
    ActivityResultContracts.RequestMultiplePermissions()
  ) { permissions ->
    val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
      permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    if (granted) {
      fusedLocationClient.lastLocation.addOnSuccessListener { location ->
        if (location != null) {
          onUseCurrentLocation(
            LocationPoint(
              id = "current_location",
              title = "Ma position actuelle",
              address = "Position GPS actuelle",
              distanceKm = 0.0,
              latitude = location.latitude,
              longitude = location.longitude
            )
          )
        }
      }
    }
  }

  fun useCurrentLocation() {
    val hasPermission = ContextCompat.checkSelfPermission(
      context,
      Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
      context,
      Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
    if (hasPermission) {
      fusedLocationClient.lastLocation.addOnSuccessListener { location ->
        if (location != null) {
          onUseCurrentLocation(
            LocationPoint("current_location", "Ma position actuelle", "Position GPS actuelle", 0.0, latitude = location.latitude, longitude = location.longitude)
          )
        }
      }
    } else {
      requestLocationPermission.launch(
        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
      )
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(Color.White)
      .statusBarsPadding()
      .navigationBarsPadding()
      .padding(horizontal = 28.dp, vertical = 24.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Spacer(modifier = Modifier.weight(0.2f))

    // Illustration Graphics
    Surface(
      shape = RoundedCornerShape(32.dp),
      color = LyonBlueLight,
      modifier = Modifier
        .size(220.dp)
        .shadow(6.dp, RoundedCornerShape(32.dp))
    ) {
      Box(contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
          drawCircle(
            color = LyonBluePrimary.copy(alpha = 0.2f),
            radius = size.width * 0.42f,
            center = Offset(size.width * 0.5f, size.height * 0.5f)
          )
        }

        Surface(
          shape = CircleShape,
          color = Color.White,
          shadowElevation = 6.dp,
          modifier = Modifier.size(80.dp)
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
              imageVector = Icons.Default.LocationOn,
              contentDescription = "GPS",
              tint = LyonBluePrimary,
              modifier = Modifier.size(44.dp)
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.weight(0.3f))

    Text(
      text = "Bienvenue sur LyonTaxis !",
      fontSize = 24.sp,
      fontWeight = FontWeight.Bold,
      color = AberDark,
      textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(12.dp))

    Text(
      text = "Activez votre position pour trouver rapidement des taxis disponibles dans la métropole de Lyon.",
      fontSize = 15.sp,
      color = AberGrayText,
      textAlign = TextAlign.Center,
      lineHeight = 22.sp,
      modifier = Modifier.padding(horizontal = 12.dp)
    )

    Spacer(modifier = Modifier.weight(0.5f))

    // Action 1: Use Current Location
    AberOutlinedButton(
      text = "Utiliser ma position actuelle",
      onClick = ::useCurrentLocation,
      borderColor = LyonBluePrimary,
      textColor = LyonBluePrimary,
      testTag = "use_current_location_button"
    )

    Spacer(modifier = Modifier.height(18.dp))

    // Action 2: Select it manually
    Text(
      text = "Choisir manuellement sur la carte",
      color = AberRed,
      fontSize = 15.sp,
      fontWeight = FontWeight.Bold,
      modifier = Modifier
        .clickable { onSelectManually() }
        .padding(8.dp)
        .testTag("select_manually_button")
    )

    Spacer(modifier = Modifier.height(24.dp))
  }
}

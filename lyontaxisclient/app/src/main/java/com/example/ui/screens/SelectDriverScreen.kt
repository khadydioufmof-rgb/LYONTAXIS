package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Driver
import com.example.model.LocationPoint
import com.example.model.VehicleCategory
import com.example.ui.components.AberDriverCard
import com.example.ui.components.AberHeader
import com.example.ui.components.AberPrimaryButton
import com.example.ui.theme.*

@Composable
fun SelectDriverScreen(
  driver: Driver,
  vehicle: VehicleCategory,
  pickup: LocationPoint,
  dropoff: LocationPoint,
  discount: Double,
  onConfirmDriver: () -> Unit,
  onChatClick: () -> Unit,
  onCallClick: () -> Unit,
  onBackClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val finalFare = (vehicle.basePrice - discount).coerceAtLeast(5.0)

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(AberBackground)
  ) {
    // Header
    AberHeader(
      title = "Sélectionner chauffeur",
      subtitle = "Chauffeurs disponibles à proximité",
      onBackClick = onBackClick
    )

    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
      // Driver Recommendations Row
      Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 3.dp,
        border = BorderStroke(1.dp, AberBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            // Overlapping Driver Avatars
            Box(modifier = Modifier.width(60.dp)) {
              Surface(
                shape = CircleShape,
                color = LyonBluePrimary,
                border = BorderStroke(2.dp, Color.White),
                modifier = Modifier.size(32.dp)
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                }
              }
              Surface(
                shape = CircleShape,
                color = LyonBlueLight,
                border = BorderStroke(2.dp, Color.White),
                modifier = Modifier
                  .padding(start = 18.dp)
                  .size(32.dp)
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Icon(Icons.Default.Person, contentDescription = null, tint = LyonBluePrimary, modifier = Modifier.size(18.dp))
                }
              }
              Surface(
                shape = CircleShape,
                color = AberYellow,
                border = BorderStroke(2.dp, Color.White),
                modifier = Modifier
                  .padding(start = 36.dp)
                  .size(32.dp)
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                }
              }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
              text = "25 Chauffeurs recommandés",
              fontSize = 13.sp,
              fontWeight = FontWeight.SemiBold,
              color = AberDark
            )
          }

          Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = LyonBluePrimary,
            modifier = Modifier.size(20.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Primary Driver Card (Gregory Smith)
      AberDriverCard(
        driver = driver,
        onChatClick = onChatClick,
        onCallClick = onCallClick,
        showActions = true
      )

      Spacer(modifier = Modifier.height(16.dp))

      // Trip Breakdown Card
      Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 3.dp,
        border = BorderStroke(1.dp, AberBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Text(
            text = "Détails de la course",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = AberDark
          )

          Spacer(modifier = Modifier.height(12.dp))

          // Route Points
          Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = CircleShape, color = LyonBluePrimary, modifier = Modifier.size(10.dp)) {}
            Spacer(modifier = Modifier.width(10.dp))
            Text(text = pickup.title, fontSize = 13.sp, color = AberDark, fontWeight = FontWeight.Medium)
          }

          Box(
            modifier = Modifier
              .padding(start = 4.dp, top = 2.dp, bottom = 2.dp)
              .size(width = 2.dp, height = 18.dp)
              .background(AberBorder)
          )

          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.LocationOn, contentDescription = null, tint = AberRed, modifier = Modifier.size(12.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = dropoff.title, fontSize = 13.sp, color = AberDark, fontWeight = FontWeight.Bold)
          }

          HorizontalDivider(modifier = Modifier.padding(vertical = 14.dp), color = AberBorder)

          // 3 Column Stats
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            TripStatItem(title = "DISTANCE", value = "3.8 km")
            TripStatItem(title = "DURÉE", value = "${vehicle.timeMinutes} min")
            TripStatItem(title = "PRIX", value = "${"%.2f".format(finalFare)} €", isPrice = true)
          }
        }
      }

      Spacer(modifier = Modifier.weight(1f))

      AberPrimaryButton(
        text = "Confirmer le chauffeur",
        onClick = onConfirmDriver,
        testTag = "confirm_driver_button"
      )

      Spacer(modifier = Modifier.height(12.dp))
    }
  }
}

@Composable
fun TripStatItem(
  title: String,
  value: String,
  isPrice: Boolean = false
) {
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Text(
      text = title,
      fontSize = 11.sp,
      fontWeight = FontWeight.Bold,
      color = AberGrayText,
      letterSpacing = 0.5.sp
    )
    Spacer(modifier = Modifier.height(4.dp))
    Text(
      text = value,
      fontSize = 16.sp,
      fontWeight = FontWeight.ExtraBold,
      color = if (isPrice) AberTealPrimary else AberDark
    )
  }
}

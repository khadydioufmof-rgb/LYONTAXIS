package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Driver
import com.example.model.LocationPoint
import com.example.model.VehicleCategory
import com.example.ui.theme.*

@Composable
fun AberVehicleCard(
  vehicle: VehicleCategory,
  isSelected: Boolean,
  onSelect: () -> Unit,
  modifier: Modifier = Modifier,
  estimatedFare: Double? = null,
  stopCount: Int = 0
) {
  val icon: ImageVector = when (vehicle) {
    VehicleCategory.JUST_GO -> Icons.Default.DirectionsCar
    VehicleCategory.LIMOUSINE -> Icons.Default.AirportShuttle
    VehicleCategory.LUXURY -> Icons.Default.DirectionsCarFilled
    VehicleCategory.ELECTRIC_CAR -> Icons.Default.ElectricCar
    VehicleCategory.BIKE -> Icons.Default.TwoWheeler
    VehicleCategory.TAXI_4_SEAT -> Icons.Default.LocalTaxi
    VehicleCategory.TAXI_7_SEAT -> Icons.Default.DirectionsBus
  }

  Surface(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(16.dp))
      .clickable { onSelect() }
      .testTag("vehicle_${vehicle.name}"),
    shape = RoundedCornerShape(16.dp),
    color = if (isSelected) AberTealLight else Color.White,
    border = BorderStroke(
      width = if (isSelected) 2.dp else 1.dp,
      color = if (isSelected) AberTealPrimary else AberBorder
    ),
    shadowElevation = if (isSelected) 4.dp else 1.dp
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 14.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Vehicle Icon
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) AberTealPrimary.copy(alpha = 0.15f) else AberGrayLight,
        modifier = Modifier.size(48.dp)
      ) {
        Box(contentAlignment = Alignment.Center) {
          Icon(
            imageVector = icon,
            contentDescription = vehicle.displayName,
            tint = if (isSelected) AberTealPrimary else AberDark,
            modifier = Modifier.size(28.dp)
          )
        }
      }

      Spacer(modifier = Modifier.width(14.dp))

      // Title & Subtitle
      Column(modifier = Modifier.weight(1f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = vehicle.displayName,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = AberDark
          )
          if (stopCount > 0) {
            Spacer(modifier = Modifier.width(6.dp))
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = AberTealPrimary.copy(alpha = 0.12f)
            ) {
              Text(
                text = "+$stopCount ${if (stopCount == 1) "stop" else "stops"}",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = AberTealPrimary,
                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
              )
            }
          }
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = "${vehicle.timeMinutes + (stopCount * 5)} min • ${vehicle.distanceDesc}",
          fontSize = 12.sp,
          color = AberGrayText
        )
      }

      // Price
      val priceToShow = estimatedFare ?: vehicle.basePrice
      Text(
        text = "$${"%.2f".format(priceToShow)}",
        fontSize = 17.sp,
        fontWeight = FontWeight.ExtraBold,
        color = AberDark
      )
    }
  }
}

@Composable
fun AberDriverCard(
  driver: Driver,
  modifier: Modifier = Modifier,
  onChatClick: (() -> Unit)? = null,
  onCallClick: (() -> Unit)? = null,
  showActions: Boolean = true
) {
  Surface(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(20.dp)),
    shape = RoundedCornerShape(20.dp),
    color = Color.White,
    shadowElevation = 6.dp,
    border = BorderStroke(1.dp, AberBorder)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Driver Avatar
      Surface(
        shape = CircleShape,
        color = AberTealLight,
        border = BorderStroke(2.dp, AberTealPrimary),
        modifier = Modifier.size(54.dp)
      ) {
        Box(contentAlignment = Alignment.Center) {
          Icon(
            imageVector = Icons.Default.Person,
            contentDescription = driver.name,
            tint = AberTealPrimary,
            modifier = Modifier.size(32.dp)
          )
        }
      }

      Spacer(modifier = Modifier.width(14.dp))

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = driver.name,
          fontSize = 16.sp,
          fontWeight = FontWeight.Bold,
          color = AberDark
        )
        Spacer(modifier = Modifier.height(3.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.Star,
            contentDescription = "Rating",
            tint = AberYellow,
            modifier = Modifier.size(15.dp)
          )
          Spacer(modifier = Modifier.width(3.dp))
          Text(
            text = "${driver.rating}",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = AberDark
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = driver.licensePlate,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = AberGrayText
          )
        }
      }

      if (showActions) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          if (onChatClick != null) {
            Surface(
              shape = CircleShape,
              color = AberBlueLight,
              modifier = Modifier
                .size(42.dp)
                .clickable { onChatClick() }
                .testTag("driver_chat_button")
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(
                  imageVector = Icons.Default.ChatBubble,
                  contentDescription = "Chat",
                  tint = AberBlue,
                  modifier = Modifier.size(20.dp)
                )
              }
            }
          }

          if (onCallClick != null) {
            Surface(
              shape = CircleShape,
              color = AberTealLight,
              modifier = Modifier
                .size(42.dp)
                .clickable { onCallClick() }
                .testTag("driver_call_button")
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(
                  imageVector = Icons.Default.Phone,
                  contentDescription = "Call",
                  tint = AberTealPrimary,
                  modifier = Modifier.size(20.dp)
                )
              }
            }
          }
        }
      }
    }
  }
}

@Composable
fun AberLocationRow(
  location: LocationPoint,
  onClick: () -> Unit,
  onFavoriteToggle: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .clickable { onClick() }
      .padding(horizontal = 16.dp, vertical = 12.dp)
      .testTag("location_item_${location.id}"),
    verticalAlignment = Alignment.CenterVertically
  ) {
    // Red Location Pin
    Surface(
      shape = CircleShape,
      color = AberRedLight,
      modifier = Modifier.size(38.dp)
    ) {
      Box(contentAlignment = Alignment.Center) {
        Icon(
          imageVector = Icons.Default.LocationOn,
          contentDescription = null,
          tint = AberRed,
          modifier = Modifier.size(20.dp)
        )
      }
    }

    Spacer(modifier = Modifier.width(14.dp))

    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = location.title,
        fontSize = 15.sp,
        fontWeight = FontWeight.SemiBold,
        color = AberDark
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = location.address,
        fontSize = 12.sp,
        color = AberGrayText,
        maxLines = 1
      )
    }

    if (onFavoriteToggle != null) {
      IconButton(onClick = onFavoriteToggle) {
        Icon(
          imageVector = if (location.isFavorite) Icons.Default.Star else Icons.Outlined.StarBorder,
          contentDescription = "Favorite",
          tint = if (location.isFavorite) AberYellow else AberGrayText
        )
      }
    }
  }
}

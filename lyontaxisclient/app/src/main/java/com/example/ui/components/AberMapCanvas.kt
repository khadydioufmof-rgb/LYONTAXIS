package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

data class TrafficIncident(
  val id: String,
  val title: String,
  val description: String,
  val delayMinutes: Int,
  val positionOffset: Offset,
  val iconType: String // "roadwork", "congestion", "signal", "fast"
)

@Composable
fun AberMapCanvas(
  modifier: Modifier = Modifier,
  showRoute: Boolean = false,
  showPickupPin: Boolean = true,
  showDropoffPin: Boolean = true,
  showCenterPinOnly: Boolean = false,
  showTrafficInitially: Boolean = true,
  interactive: Boolean = true,
  externalPanOffset: Offset? = null,
  externalZoomScale: Float? = null,
  onMapTap: ((Offset) -> Unit)? = null,
  onTrafficIncidentClick: ((TrafficIncident) -> Unit)? = null
) {
  var panOffset by remember { mutableStateOf(externalPanOffset ?: Offset.Zero) }
  var zoomScale by remember { mutableFloatStateOf(externalZoomScale ?: 1.0f) }
  var isTrafficEnabled by remember { mutableStateOf(showTrafficInitially) }
  var selectedIncident by remember { mutableStateOf<TrafficIncident?>(null) }
  var showZoomFeedback by remember { mutableStateOf(false) }

  // Sync with external state if provided
  LaunchedEffect(externalPanOffset) {
    if (externalPanOffset != null) {
      panOffset = externalPanOffset
    }
  }
  LaunchedEffect(externalZoomScale) {
    if (externalZoomScale != null) {
      zoomScale = externalZoomScale
    }
  }

  val infiniteTransition = rememberInfiniteTransition(label = "map_anim")
  val pulseRadius by infiniteTransition.animateFloat(
    initialValue = 12f,
    targetValue = 44f,
    animationSpec = infiniteRepeatable(
      animation = tween(1800, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "pulse_radius"
  )
  val pulseAlpha by infiniteTransition.animateFloat(
    initialValue = 0.85f,
    targetValue = 0.0f,
    animationSpec = infiniteRepeatable(
      animation = tween(1800, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "pulse_alpha"
  )

  val carProgress by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(6500, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "car_progress"
  )

  val trafficPulse by infiniteTransition.animateFloat(
    initialValue = 0.6f,
    targetValue = 1.0f,
    animationSpec = infiniteRepeatable(
      animation = tween(1200, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "traffic_pulse"
  )

  // Predefined realistic traffic incident points relative to city center
  val trafficIncidents = remember {
    listOf(
      TrafficIncident(
        id = "inc_1",
        title = "Moderate Slowdown",
        description = "Heavy traffic volume on 4th Ave (+3 min delay)",
        delayMinutes = 3,
        positionOffset = Offset(80f, 0f),
        iconType = "congestion"
      ),
      TrafficIncident(
        id = "inc_2",
        title = "Road Maintenance",
        description = "Right lane closed near Grand Ave junction",
        delayMinutes = 2,
        positionOffset = Offset(-180f, 120f),
        iconType = "roadwork"
      ),
      TrafficIncident(
        id = "inc_3",
        title = "Traffic Light Delay",
        description = "Signal timing adjustment on Broadway St",
        delayMinutes = 1,
        positionOffset = Offset(200f, -80f),
        iconType = "signal"
      )
    )
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(Color(0xFFF3F3EF))
      .then(
        if (interactive) {
          Modifier
            // Multi-touch Pinch to Zoom and Pan Gestures
            .pointerInput(Unit) {
              detectTransformGestures { centroid, pan, zoom, _ ->
                val newZoom = (zoomScale * zoom).coerceIn(0.5f, 4.0f)
                val zoomChange = newZoom / zoomScale
                // Pan adjustment with centroid zoom focal centering
                panOffset = Offset(
                  x = (panOffset.x + pan.x) * zoomChange + (1f - zoomChange) * (centroid.x - size.width / 2f),
                  y = (panOffset.y + pan.y) * zoomChange + (1f - zoomChange) * (centroid.y - size.height / 2f)
                )
                zoomScale = newZoom
              }
            }
            // Double Tap to Zoom In / Tap to dismiss tooltips or trigger callbacks
            .pointerInput(Unit) {
              detectTapGestures(
                onDoubleTap = { tapOffset ->
                  val targetZoom = (zoomScale * 1.5f).coerceAtMost(3.8f)
                  zoomScale = targetZoom
                  showZoomFeedback = true
                },
                onTap = { tapOffset ->
                  selectedIncident = null
                  onMapTap?.invoke(tapOffset)
                }
              )
            }
        } else Modifier
      )
  ) {
    Canvas(modifier = Modifier.fillMaxSize()) {
      val w = size.width
      val h = size.height
      val cx = w / 2f + panOffset.x
      val cy = h / 2f + panOffset.y

      // 1. Draw Parks & Green Zones
      val parkPath = Path().apply {
        addRoundRect(
          androidx.compose.ui.geometry.RoundRect(
            left = cx - 380f * zoomScale,
            top = cy - 260f * zoomScale,
            right = cx - 180f * zoomScale,
            bottom = cy - 70f * zoomScale,
            radiusX = 16f * zoomScale,
            radiusY = 16f * zoomScale
          )
        )
        addRoundRect(
          androidx.compose.ui.geometry.RoundRect(
            left = cx + 130f * zoomScale,
            top = cy + 170f * zoomScale,
            right = cx + 340f * zoomScale,
            bottom = cy + 350f * zoomScale,
            radiusX = 20f * zoomScale,
            radiusY = 20f * zoomScale
          )
        )
      }
      drawPath(parkPath, color = Color(0xFFD9F2E2))

      // 2. Draw River / Water curve
      val riverPath = Path().apply {
        moveTo(cx - 520f * zoomScale, cy + 420f * zoomScale)
        cubicTo(
          cx - 100f * zoomScale, cy + 200f * zoomScale,
          cx + 50f * zoomScale, cy - 100f * zoomScale,
          cx + 420f * zoomScale, cy - 460f * zoomScale
        )
      }
      drawPath(
        path = riverPath,
        color = Color(0xFFC7E2F8),
        style = Stroke(width = 48f * zoomScale, cap = StrokeCap.Round)
      )

      // 3. Draw City Grid Roads (Avenues & Streets)
      val streetColor = Color.White
      val streetBorderColor = Color(0xFFE2E2DC)

      val horizontalY = listOf(-320f, -220f, -120f, -20f, 80f, 180f, 280f, 380f)
      val verticalX = listOf(-380f, -260f, -140f, -20f, 100f, 220f, 340f, 460f)

      // Road background borders
      for (y in horizontalY) {
        val lineY = cy + y * zoomScale
        drawLine(
          color = streetBorderColor,
          start = Offset(0f, lineY),
          end = Offset(w, lineY),
          strokeWidth = 20f * zoomScale
        )
      }
      for (x in verticalX) {
        val lineX = cx + x * zoomScale
        drawLine(
          color = streetBorderColor,
          start = Offset(lineX, 0f),
          end = Offset(lineX, h),
          strokeWidth = 20f * zoomScale
        )
      }

      // Road fill
      for (y in horizontalY) {
        val lineY = cy + y * zoomScale
        drawLine(
          color = streetColor,
          start = Offset(0f, lineY),
          end = Offset(w, lineY),
          strokeWidth = 15f * zoomScale
        )
      }
      for (x in verticalX) {
        val lineX = cx + x * zoomScale
        drawLine(
          color = streetColor,
          start = Offset(lineX, 0f),
          end = Offset(lineX, h),
          strokeWidth = 15f * zoomScale
        )
      }

      // 4. Draw Main Expressway / Diagonal Boulevard
      val mainAvePath = Path().apply {
        moveTo(0f, cy + 190f * zoomScale)
        lineTo(cx - 20f * zoomScale, cy + 80f * zoomScale)
        lineTo(cx + 220f * zoomScale, cy - 120f * zoomScale)
        lineTo(w, cy - 270f * zoomScale)
      }
      drawPath(
        path = mainAvePath,
        color = Color(0xFFFFE0B2),
        style = Stroke(width = 24f * zoomScale, cap = StrokeCap.Round, join = StrokeJoin.Round)
      )

      // 5. Traffic Congestion Overlay on Grid Network (If Traffic Enabled)
      if (isTrafficEnabled) {
        // High Flow (Green) Street Segments
        drawLine(
          color = Color(0xFF4CAF50).copy(alpha = 0.75f),
          start = Offset(cx - 380f * zoomScale, cy - 20f * zoomScale),
          end = Offset(cx - 140f * zoomScale, cy - 20f * zoomScale),
          strokeWidth = 4f * zoomScale,
          cap = StrokeCap.Round
        )
        drawLine(
          color = Color(0xFF4CAF50).copy(alpha = 0.75f),
          start = Offset(cx + 100f * zoomScale, cy + 180f * zoomScale),
          end = Offset(cx + 340f * zoomScale, cy + 180f * zoomScale),
          strokeWidth = 4f * zoomScale,
          cap = StrokeCap.Round
        )

        // Moderate Traffic (Orange/Amber) Street Segments
        drawLine(
          color = Color(0xFFFF9800).copy(alpha = 0.85f * trafficPulse),
          start = Offset(cx + 100f * zoomScale, cy + 80f * zoomScale),
          end = Offset(cx + 100f * zoomScale, cy - 20f * zoomScale),
          strokeWidth = 5f * zoomScale,
          cap = StrokeCap.Round
        )
        drawLine(
          color = Color(0xFFFF9800).copy(alpha = 0.85f * trafficPulse),
          start = Offset(cx - 140f * zoomScale, cy + 80f * zoomScale),
          end = Offset(cx - 20f * zoomScale, cy + 80f * zoomScale),
          strokeWidth = 5f * zoomScale,
          cap = StrokeCap.Round
        )

        // Heavy Congestion (Red/Crimson) Near Center Intersections
        drawLine(
          color = Color(0xFFE53935).copy(alpha = 0.9f * trafficPulse),
          start = Offset(cx + 100f * zoomScale, cy - 20f * zoomScale),
          end = Offset(cx + 100f * zoomScale, cy - 120f * zoomScale),
          strokeWidth = 6f * zoomScale,
          cap = StrokeCap.Round
        )
      }

      // 6. Dynamic Active Route Line with Traffic Segmenting
      val pickupPt = Offset(cx - 20f * zoomScale, cy + 80f * zoomScale)
      val dropoffPt = Offset(cx + 220f * zoomScale, cy - 120f * zoomScale)
      val turn1Pt = Offset(cx + 100f * zoomScale, cy + 80f * zoomScale)
      val turn2Pt = Offset(cx + 100f * zoomScale, cy - 120f * zoomScale)

      if (showRoute) {
        val fullRoutePath = Path().apply {
          moveTo(pickupPt.x, pickupPt.y)
          lineTo(turn1Pt.x, turn1Pt.y)
          lineTo(turn2Pt.x, turn2Pt.y)
          lineTo(dropoffPt.x, dropoffPt.y)
        }

        // Route soft drop-shadow / background highlight
        drawPath(
          path = fullRoutePath,
          color = AberTealPrimary.copy(alpha = 0.22f),
          style = Stroke(width = 18f * zoomScale, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        if (isTrafficEnabled) {
          // Segment 1: Pickup to Turn 1 (Clear / Green Flow)
          drawLine(
            color = Color(0xFF00C853),
            start = pickupPt,
            end = turn1Pt,
            strokeWidth = 9f * zoomScale,
            cap = StrokeCap.Round
          )

          // Segment 2: Turn 1 to Turn 2 (Moderate Traffic / Amber Flow)
          drawLine(
            color = Color(0xFFFF9100),
            start = turn1Pt,
            end = turn2Pt,
            strokeWidth = 9f * zoomScale,
            cap = StrokeCap.Round
          )

          // Segment 3: Turn 2 to Dropoff (Smooth / Teal Flow)
          drawLine(
            color = AberTealPrimary,
            start = turn2Pt,
            end = dropoffPt,
            strokeWidth = 9f * zoomScale,
            cap = StrokeCap.Round
          )
        } else {
          // Standard Clean Blue/Teal Route
          drawPath(
            path = fullRoutePath,
            color = AberTealPrimary,
            style = Stroke(width = 9f * zoomScale, cap = StrokeCap.Round, join = StrokeJoin.Round)
          )
        }

        // Animated moving car along route
        val t = carProgress
        val currentCarPos = if (t < 0.33f) {
          val sub = t / 0.33f
          Offset(
            pickupPt.x + (turn1Pt.x - pickupPt.x) * sub,
            pickupPt.y
          )
        } else if (t < 0.66f) {
          val sub = (t - 0.33f) / 0.33f
          Offset(
            turn1Pt.x,
            turn1Pt.y + (turn2Pt.y - turn1Pt.y) * sub
          )
        } else {
          val sub = (t - 0.66f) / 0.34f
          Offset(
            turn2Pt.x + (dropoffPt.x - turn2Pt.x) * sub,
            turn2Pt.y
          )
        }

        // Car ripple radar
        drawCircle(
          color = AberTealPrimary.copy(alpha = 0.3f),
          radius = 16f * zoomScale,
          center = currentCarPos
        )
        // Car Body Marker
        drawCircle(
          color = AberDark,
          radius = 10f * zoomScale,
          center = currentCarPos
        )
        drawCircle(
          color = AberTealPrimary,
          radius = 7f * zoomScale,
          center = currentCarPos
        )
        drawCircle(
          color = Color.White,
          radius = 3.5f * zoomScale,
          center = currentCarPos
        )
      }

      // 7. Nearby Moving Aber Fleet Taxis
      drawTaxi(cx - 140f * zoomScale + (sin(carProgress * 6.28) * 30f).toFloat(), cy - 20f * zoomScale, zoomScale)
      drawTaxi(cx + 220f * zoomScale, cy + 180f * zoomScale - (carProgress * 100f) % 150f, zoomScale)
      drawTaxi(cx - 260f * zoomScale + (carProgress * 120f) % 200f, cy + 280f * zoomScale, zoomScale)
      drawTaxi(cx + 340f * zoomScale, cy - 220f * zoomScale + (cos(carProgress * 6.28) * 40f).toFloat(), zoomScale)

      // 8. Traffic Incident Badges (if traffic layer active)
      if (isTrafficEnabled && showRoute) {
        trafficIncidents.forEach { incident ->
          val pos = Offset(cx + incident.positionOffset.x * zoomScale, cy + incident.positionOffset.y * zoomScale)
          drawIncidentMarker(pos, incident.iconType, zoomScale)
        }
      }

      // 9. Pickup Location Pin (Pulsing radar)
      if (showPickupPin && !showCenterPinOnly) {
        drawCircle(
          color = AberTealPrimary.copy(alpha = pulseAlpha),
          radius = pulseRadius * zoomScale,
          center = pickupPt
        )
        drawCircle(
          color = Color.White,
          radius = 14f * zoomScale,
          center = pickupPt
        )
        drawCircle(
          color = AberTealPrimary,
          radius = 9f * zoomScale,
          center = pickupPt
        )
      }

      // 10. Dropoff Pin (Red Pin)
      if (showDropoffPin && !showCenterPinOnly) {
        drawDropoffMarker(dropoffPt, zoomScale)
      }
    }

    // Interactive Center Pin for "Pick on Map" screen
    if (showCenterPinOnly) {
      Box(
        modifier = Modifier.align(Alignment.Center),
        contentAlignment = Alignment.Center
      ) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier.offset(y = (-20).dp)
        ) {
          Surface(
            shape = CircleShape,
            color = AberRed,
            shadowElevation = 6.dp,
            modifier = Modifier.size(36.dp)
          ) {
            Box(contentAlignment = Alignment.Center) {
              Surface(
                shape = CircleShape,
                color = Color.White,
                modifier = Modifier.size(14.dp)
              ) {}
            }
          }
          // Pointer tip
          Canvas(modifier = Modifier.size(12.dp, 8.dp)) {
            val path = Path().apply {
              moveTo(0f, 0f)
              lineTo(size.width, 0f)
              lineTo(size.width / 2f, size.height)
              close()
            }
            drawPath(path, color = AberRed)
          }
          // Shadow dot
          Surface(
            shape = CircleShape,
            color = Color.Black.copy(alpha = 0.25f),
            modifier = Modifier.size(8.dp, 4.dp)
          ) {}
        }
      }
    }

    // Traffic Incident Tappable Overlays (Invisible click targets mapping to incidents)
    if (isTrafficEnabled && showRoute && interactive) {
      trafficIncidents.forEach { incident ->
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
          val cx = maxWidth.value / 2f + panOffset.x / 2.75f
          val cy = maxHeight.value / 2f + panOffset.y / 2.75f
          val posX = (cx + (incident.positionOffset.x * zoomScale) / 2.75f).dp
          val posY = (cy + (incident.positionOffset.y * zoomScale) / 2.75f).dp

          Box(
            modifier = Modifier
              .offset(x = posX - 18.dp, y = posY - 18.dp)
              .size(36.dp)
              .clip(CircleShape)
              .clickable {
                selectedIncident = incident
                onTrafficIncidentClick?.invoke(incident)
              }
          )
        }
      }
    }

    // Traffic Incident Info Popup Dialog / Tooltip
    AnimatedVisibility(
      visible = selectedIncident != null,
      enter = fadeIn() + scaleIn(),
      exit = fadeOut() + scaleOut(),
      modifier = Modifier
        .align(Alignment.TopCenter)
        .padding(top = 70.dp, start = 20.dp, end = 20.dp)
    ) {
      selectedIncident?.let { inc ->
        Surface(
          shape = RoundedCornerShape(16.dp),
          color = AberDark,
          shadowElevation = 10.dp,
          modifier = Modifier.fillMaxWidth(0.9f)
        ) {
          Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Surface(
              shape = CircleShape,
              color = when (inc.iconType) {
                "roadwork" -> AberYellow
                "congestion" -> AberRed
                else -> AberBlue
              },
              modifier = Modifier.size(36.dp)
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(
                  imageVector = when (inc.iconType) {
                    "roadwork" -> Icons.Default.Construction
                    "congestion" -> Icons.Default.Traffic
                    else -> Icons.Default.Warning
                  },
                  contentDescription = null,
                  tint = Color.White,
                  modifier = Modifier.size(20.dp)
                )
              }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = inc.title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
              Text(
                text = inc.description,
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.8f)
              )
            }

            IconButton(
              onClick = { selectedIncident = null },
              modifier = Modifier.size(28.dp)
            ) {
              Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = Color.White)
            }
          }
        }
      }
    }

    // Floating Map Inspection & Zoom Controls
    if (interactive) {
      Column(
        modifier = Modifier
          .align(Alignment.CenterEnd)
          .padding(end = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // 1. Traffic Layer Toggle Button
        Surface(
          shape = CircleShape,
          color = if (isTrafficEnabled) AberTealPrimary else Color.White,
          shadowElevation = 6.dp,
          border = BorderStroke(1.dp, if (isTrafficEnabled) AberTealPrimary else AberBorder),
          modifier = Modifier.size(46.dp)
        ) {
          IconButton(
            onClick = { isTrafficEnabled = !isTrafficEnabled },
            modifier = Modifier.testTag("map_traffic_layer_toggle")
          ) {
            Icon(
              imageVector = Icons.Default.Traffic,
              contentDescription = "Toggle Traffic Conditions",
              tint = if (isTrafficEnabled) Color.White else AberDark
            )
          }
        }

        // 2. Fit Route / Focus Center Button
        Surface(
          shape = CircleShape,
          color = Color.White,
          shadowElevation = 6.dp,
          border = BorderStroke(1.dp, AberBorder),
          modifier = Modifier.size(46.dp)
        ) {
          IconButton(
            onClick = {
              panOffset = Offset.Zero
              zoomScale = 1.0f
            },
            modifier = Modifier.testTag("map_fit_route_button")
          ) {
            Icon(
              imageVector = Icons.Default.CenterFocusStrong,
              contentDescription = "Fit Route and Center",
              tint = AberTealPrimary
            )
          }
        }

        // 3. Zoom Controls Card (+ / - / Zoom Level %)
        Surface(
          shape = RoundedCornerShape(14.dp),
          color = Color.White,
          border = BorderStroke(1.dp, AberBorder),
          shadowElevation = 6.dp
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            IconButton(
              onClick = { zoomScale = (zoomScale * 1.25f).coerceAtMost(4.0f) },
              modifier = Modifier
                .size(42.dp)
                .testTag("map_zoom_in_button")
            ) {
              Icon(imageVector = Icons.Default.Add, contentDescription = "Zoom In", tint = AberDark)
            }

            // Current Zoom Level Indicator
            Surface(
              color = AberGrayLight,
              modifier = Modifier
                .width(42.dp)
                .padding(vertical = 2.dp)
            ) {
              Text(
                text = "${(zoomScale * 100).toInt()}%",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = AberGrayText,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
              )
            }

            HorizontalDivider(color = AberBorder, thickness = 1.dp)

            IconButton(
              onClick = { zoomScale = (zoomScale * 0.8f).coerceAtLeast(0.5f) },
              modifier = Modifier
                .size(42.dp)
                .testTag("map_zoom_out_button")
            ) {
              Icon(imageVector = Icons.Default.Remove, contentDescription = "Zoom Out", tint = AberDark)
            }
          }
        }
      }
    }
  }
}

private fun DrawScope.drawIncidentMarker(pt: Offset, iconType: String, scale: Float) {
  val markerRadius = 11f * scale
  val color = when (iconType) {
    "roadwork" -> Color(0xFFFFB300) // Amber
    "congestion" -> Color(0xFFE53935) // Red
    "signal" -> Color(0xFFFB8C00) // Orange
    else -> Color(0xFF1E88E5)
  }

  // Soft shadow
  drawCircle(
    color = Color(0x40000000),
    radius = markerRadius + 2f * scale,
    center = Offset(pt.x + 1f, pt.y + 2f)
  )

  // Outer Border & Fill
  drawCircle(
    color = Color.White,
    radius = markerRadius + 1.5f * scale,
    center = pt
  )
  drawCircle(
    color = color,
    radius = markerRadius,
    center = pt
  )

  // Inner symbol (Exclamation / Dot / Tool)
  drawCircle(
    color = Color.White,
    radius = 3.5f * scale,
    center = pt
  )
}

private fun DrawScope.drawTaxi(cx: Float, cy: Float, scale: Float) {
  val carWidth = 26f * scale
  val carHeight = 14f * scale

  // Car Shadow
  drawRoundRect(
    color = Color(0x33000000),
    topLeft = Offset(cx - carWidth / 2f + 2f, cy - carHeight / 2f + 2f),
    size = Size(carWidth, carHeight),
    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f * scale, 4f * scale)
  )

  // Car Body
  drawRoundRect(
    color = AberTealPrimary,
    topLeft = Offset(cx - carWidth / 2f, cy - carHeight / 2f),
    size = Size(carWidth, carHeight),
    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f * scale, 4f * scale)
  )

  // Windshield & roof
  drawRoundRect(
    color = Color.White.copy(alpha = 0.85f),
    topLeft = Offset(cx - carWidth * 0.25f, cy - carHeight * 0.35f),
    size = Size(carWidth * 0.5f, carHeight * 0.7f),
    cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f * scale, 2f * scale)
  )
}

private fun DrawScope.drawDropoffMarker(pt: Offset, scale: Float) {
  val pinRadius = 14f * scale
  val shadowOffset = Offset(pt.x, pt.y + 12f * scale)

  // Ground shadow
  drawOval(
    color = Color(0x33000000),
    topLeft = Offset(shadowOffset.x - 8f * scale, shadowOffset.y - 3f * scale),
    size = Size(16f * scale, 6f * scale)
  )

  // Red Teardrop Body
  val pinPath = Path().apply {
    moveTo(pt.x, pt.y + 10f * scale)
    lineTo(pt.x - 10f * scale, pt.y - 6f * scale)
    arcTo(
      rect = androidx.compose.ui.geometry.Rect(
        left = pt.x - pinRadius,
        top = pt.y - pinRadius * 2,
        right = pt.x + pinRadius,
        bottom = pt.y
      ),
      startAngleDegrees = 180f,
      sweepAngleDegrees = 180f,
      forceMoveTo = false
    )
    lineTo(pt.x + 10f * scale, pt.y - 6f * scale)
    close()
  }

  drawPath(pinPath, color = AberRed)

  // Inner white dot
  drawCircle(
    color = Color.White,
    radius = 5f * scale,
    center = Offset(pt.x, pt.y - pinRadius)
  )
}

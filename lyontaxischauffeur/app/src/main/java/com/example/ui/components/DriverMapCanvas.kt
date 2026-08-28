package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DriverStatus
import com.example.model.RideRequest
import com.example.model.SurgeHotspot
import com.example.ui.theme.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalTextApi::class)
@Composable
fun DriverMapCanvas(
    status: DriverStatus,
    activeRide: RideRequest?,
    surgeHotspots: List<SurgeHotspot>,
    isNightMode: Boolean,
    modifier: Modifier = Modifier,
    onSurgeClick: (SurgeHotspot) -> Unit = {}
) {
    var showSurgeLayer by remember { mutableStateOf(true) }
    var zoomLevel by remember { mutableStateOf(1f) }

    // Radar pulsing animation for online search
    val infiniteTransition = rememberInfiniteTransition(label = "radar")
    val radarPulse by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse"
    )

    // Route dash flow animation
    val routeFlow by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 40f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "routeFlow"
    )

    val textMeasurer = rememberTextMeasurer()

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .testTag("driver_map_canvas")
        ) {
            val canvasW = size.width
            val canvasH = size.height

            // Background map canvas
            val bgColor = if (isNightMode) Color(0xFF0D131D) else Color(0xFFE9EEF4)
            val roadColor = if (isNightMode) Color(0xFF1B2433) else Color(0xFFFFFFFF)
            val roadOutline = if (isNightMode) Color(0xFF223044) else Color(0xFFD8E0EB)
            val riverColor = if (isNightMode) Color(0xFF092032) else Color(0xFFBFDBFE)
            val greenParkColor = if (isNightMode) Color(0xFF0F2620) else Color(0xFFD1FAE5)

            drawRect(bgColor)

            // Draw Parks / Green areas
            drawCircle(
                color = greenParkColor,
                radius = 120.dp.toPx(),
                center = Offset(canvasW * 0.2f, canvasH * 0.35f)
            )
            drawRoundRect(
                color = greenParkColor,
                topLeft = Offset(canvasW * 0.7f, canvasH * 0.15f),
                size = androidx.compose.ui.geometry.Size(180.dp.toPx(), 100.dp.toPx()),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(24.dp.toPx())
            )

            // Draw River Seine curve
            val riverPath = Path().apply {
                moveTo(-50f, canvasH * 0.55f)
                cubicTo(
                    canvasW * 0.3f, canvasH * 0.65f,
                    canvasW * 0.6f, canvasH * 0.40f,
                    canvasW + 50f, canvasH * 0.48f
                )
            }
            drawPath(
                path = riverPath,
                color = riverColor,
                style = Stroke(width = 42.dp.toPx(), cap = StrokeCap.Round)
            )

            // Draw Road Grid (City Streets)
            val roadSpacing = 70.dp.toPx()
            var x = 0f
            while (x < canvasW) {
                drawLine(
                    color = roadOutline,
                    start = Offset(x, 0f),
                    end = Offset(x, canvasH),
                    strokeWidth = 14f
                )
                drawLine(
                    color = roadColor,
                    start = Offset(x, 0f),
                    end = Offset(x, canvasH),
                    strokeWidth = 10f
                )
                x += roadSpacing
            }

            var y = 0f
            while (y < canvasH) {
                drawLine(
                    color = roadOutline,
                    start = Offset(0f, y),
                    end = Offset(canvasW, y),
                    strokeWidth = 14f
                )
                drawLine(
                    color = roadColor,
                    start = Offset(0f, y),
                    end = Offset(canvasW, y),
                    strokeWidth = 10f
                )
                y += roadSpacing
            }

            // Draw Major Boulevard Diagonal
            val diagPath = Path().apply {
                moveTo(canvasW * 0.1f, canvasH * 0.85f)
                lineTo(canvasW * 0.85f, canvasH * 0.15f)
            }
            drawPath(diagPath, roadOutline, style = Stroke(width = 24f, cap = StrokeCap.Round))
            drawPath(diagPath, roadColor, style = Stroke(width = 18f, cap = StrokeCap.Round))

            // Draw Surge Heatmaps if enabled
            if (showSurgeLayer) {
                surgeHotspots.forEach { hotspot ->
                    val zoneCenterX = canvasW * hotspot.relX
                    val zoneCenterY = canvasH * hotspot.relY
                    val surgeColor = when {
                        hotspot.multiplier >= 1.8 -> Color(0xFFEF4444).copy(alpha = 0.25f)
                        hotspot.multiplier >= 1.5 -> Color(0xFFF59E0B).copy(alpha = 0.25f)
                        else -> Color(0xFF10B981).copy(alpha = 0.20f)
                    }
                    val badgeColor = when {
                        hotspot.multiplier >= 1.8 -> Color(0xFFEF4444)
                        hotspot.multiplier >= 1.5 -> Color(0xFFF59E0B)
                        else -> Color(0xFF00D09C)
                    }

                    // Glow circle
                    drawCircle(
                        color = surgeColor,
                        radius = 55.dp.toPx(),
                        center = Offset(zoneCenterX, zoneCenterY)
                    )
                    drawCircle(
                        color = badgeColor.copy(alpha = 0.4f),
                        radius = 28.dp.toPx(),
                        center = Offset(zoneCenterX, zoneCenterY)
                    )

                    // Multiplier text pill
                    val text = "🔥 x${hotspot.multiplier}"
                    val textLayout = textMeasurer.measure(
                        text = text,
                        style = TextStyle(
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    val textW = textLayout.size.width.toFloat()
                    val textH = textLayout.size.height.toFloat()

                    drawRoundRect(
                        color = badgeColor,
                        topLeft = Offset(zoneCenterX - textW / 2 - 12f, zoneCenterY - textH / 2 - 8f),
                        size = androidx.compose.ui.geometry.Size(textW + 24f, textH + 16f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(14f)
                    )

                    drawText(
                        textMeasurer = textMeasurer,
                        text = text,
                        topLeft = Offset(zoneCenterX - textW / 2, zoneCenterY - textH / 2),
                        style = TextStyle(
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            // Driver center position
            val driverX = canvasW * 0.5f
            val driverY = canvasH * 0.55f

            // Draw Active Navigation Route if active ride exists
            if (activeRide != null && (status == DriverStatus.EN_ROUTE_PICKUP || status == DriverStatus.IN_TRIP || status == DriverStatus.ARRIVED_PICKUP)) {
                val targetX = if (status == DriverStatus.EN_ROUTE_PICKUP || status == DriverStatus.ARRIVED_PICKUP) {
                    canvasW * activeRide.pickupLng
                } else {
                    canvasW * activeRide.dropoffLng
                }
                val targetY = if (status == DriverStatus.EN_ROUTE_PICKUP || status == DriverStatus.ARRIVED_PICKUP) {
                    canvasH * activeRide.pickupLat
                } else {
                    canvasH * activeRide.dropoffLat
                }

                // Smooth polyline route
                val routePath = Path().apply {
                    moveTo(driverX, driverY)
                    cubicTo(
                        (driverX + targetX) / 2 + 60f, (driverY + targetY) / 2 - 40f,
                        (driverX + targetX) / 2 - 60f, (driverY + targetY) / 2 + 40f,
                        targetX, targetY
                    )
                }

                // Route Outer Shadow Glow
                drawPath(
                    path = routePath,
                    color = AberMint.copy(alpha = 0.35f),
                    style = Stroke(width = 18.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )

                // Route Core Line
                drawPath(
                    path = routePath,
                    color = AberMint,
                    style = Stroke(
                        width = 8.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(30f, 15f), routeFlow)
                    )
                )

                // Draw Target Pin (Pickup or Destination)
                drawCircle(
                    color = Color.Black.copy(alpha = 0.3f),
                    radius = 18.dp.toPx(),
                    center = Offset(targetX, targetY + 8.dp.toPx())
                )
                val pinColor = if (status == DriverStatus.EN_ROUTE_PICKUP || status == DriverStatus.ARRIVED_PICKUP) AberMint else AberGold
                drawCircle(
                    color = pinColor,
                    radius = 14.dp.toPx(),
                    center = Offset(targetX, targetY)
                )
                drawCircle(
                    color = Color.White,
                    radius = 6.dp.toPx(),
                    center = Offset(targetX, targetY)
                )
            }

            // Draw Radar Pulsing when in ONLINE_WAITING
            if (status == DriverStatus.ONLINE_WAITING) {
                val maxRadius = 140.dp.toPx()
                val currentRadius = maxRadius * radarPulse
                val alpha = (1f - radarPulse).coerceIn(0f, 1f)

                drawCircle(
                    color = AberMint.copy(alpha = alpha * 0.4f),
                    radius = currentRadius,
                    center = Offset(driverX, driverY),
                    style = Stroke(width = 3.dp.toPx())
                )
                drawCircle(
                    color = AberMint.copy(alpha = alpha * 0.15f),
                    radius = currentRadius * 0.6f,
                    center = Offset(driverX, driverY)
                )
            }

            // Draw Driver Vehicle Cursor
            drawCircle(
                color = Color.Black.copy(alpha = 0.35f),
                radius = 24.dp.toPx(),
                center = Offset(driverX, driverY + 4.dp.toPx())
            )
            drawCircle(
                color = if (status == DriverStatus.OFFLINE) Color(0xFF64748B) else AberMint,
                radius = 20.dp.toPx(),
                center = Offset(driverX, driverY)
            )
            drawCircle(
                color = if (isNightMode) Color(0xFF0F172A) else Color.White,
                radius = 15.dp.toPx(),
                center = Offset(driverX, driverY)
            )

            // Vehicle Heading Direction Arrow
            val headingPath = Path().apply {
                moveTo(driverX, driverY - 10.dp.toPx())
                lineTo(driverX - 6.dp.toPx(), driverY + 6.dp.toPx())
                lineTo(driverX, driverY + 3.dp.toPx())
                lineTo(driverX + 6.dp.toPx(), driverY + 6.dp.toPx())
                close()
            }
            drawPath(
                path = headingPath,
                color = if (status == DriverStatus.OFFLINE) Color(0xFF64748B) else AberMint
            )
        }

        // Map Float Controls (Right Side)
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Layer Toggle (Surge Heatmap)
            Surface(
                onClick = { showSurgeLayer = !showSurgeLayer },
                shape = CircleShape,
                color = if (showSurgeLayer) AberMint else (if (isNightMode) AberDarkCard else Color.White),
                shadowElevation = 4.dp,
                modifier = Modifier.size(46.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = "Afficher les zones de majoration",
                        tint = if (showSurgeLayer) Color.Black else (if (isNightMode) Color.White else Color.Black)
                    )
                }
            }

            // Recenter Location Button
            Surface(
                onClick = { /* Recenter map */ },
                shape = CircleShape,
                color = if (isNightMode) AberDarkCard else Color.White,
                shadowElevation = 4.dp,
                modifier = Modifier.size(46.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = "Recentrer ma position",
                        tint = AberMint
                    )
                }
            }

            // Traffic / Navigation Layer
            Surface(
                onClick = { /* Toggle traffic */ },
                shape = CircleShape,
                color = if (isNightMode) AberDarkCard else Color.White,
                shadowElevation = 4.dp,
                modifier = Modifier.size(46.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Traffic,
                        contentDescription = "Info trafic en temps réel",
                        tint = if (isNightMode) Color.White else Color.Black
                    )
                }
            }
        }
    }
}

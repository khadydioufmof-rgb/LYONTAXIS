package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import java.util.Locale

enum class DashboardTimeframe {
    DAILY_HOURLY,
    WEEKLY_DAILY
}

data class ChartDataPoint(
    val label: String,
    val value: Float,
    val trips: Int,
    val detail: String,
    val isHighlight: Boolean = false
)

@Composable
fun EarningsDashboardChart(
    todayTotal: Double = 184.50,
    weekTotal: Double = 1289.50,
    weeklyGoal: Double = 1500.00,
    modifier: Modifier = Modifier
) {
    var timeframe by remember { mutableStateOf(DashboardTimeframe.WEEKLY_DAILY) }
    var selectedIndex by remember { mutableStateOf(3) } // default to current day / active slot

    val weeklyData = remember {
        listOf(
            ChartDataPoint("Lun", 142.00f, 9, "9 courses • Majoration x1.2"),
            ChartDataPoint("Mar", 168.50f, 11, "11 courses • Majoration x1.3"),
            ChartDataPoint("Mer", 195.00f, 13, "13 courses • Majoration x1.5"),
            ChartDataPoint("Jeu", 184.50f, 12, "12 courses • Aujourd'hui (En cours)", isHighlight = true),
            ChartDataPoint("Ven", 245.00f, 15, "15 courses • Prévision soir"),
            ChartDataPoint("Sam", 260.00f, 16, "16 courses • Forte affluence"),
            ChartDataPoint("Dim", 110.00f, 7, "7 courses • Calme")
        )
    }

    val dailyHourlyData = remember {
        listOf(
            ChartDataPoint("07h", 18.50f, 1, "07:15 - Dépose Gare de Lyon"),
            ChartDataPoint("09h", 34.00f, 2, "08:30 & 09:15 - Trajets Business"),
            ChartDataPoint("11h", 22.00f, 1, "10:45 - Navette Roissy CDG"),
            ChartDataPoint("13h", 28.50f, 2, "12:10 & 13:00 - Déjeuners Opéra"),
            ChartDataPoint("15h", 16.00f, 1, "14:40 - Trajet intra-muros"),
            ChartDataPoint("17h", 42.50f, 3, "16:45 & 17:30 - Heure de pointe", isHighlight = true),
            ChartDataPoint("19h", 23.00f, 2, "18:40 - Trajet Soirée")
        )
    }

    val currentData = if (timeframe == DashboardTimeframe.WEEKLY_DAILY) weeklyData else dailyHourlyData
    val safeSelectedIndex = selectedIndex.coerceIn(0, currentData.lastIndex)
    val activePoint = currentData[safeSelectedIndex]

    val maxValue = (currentData.maxOfOrNull { it.value } ?: 100f) * 1.15f
    val totalPeriodAmount = if (timeframe == DashboardTimeframe.WEEKLY_DAILY) weekTotal else todayTotal

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("earnings_dashboard_chart_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131A26)),
        border = androidx.compose.foundation.BorderStroke(1.dp, AberMint.copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // HEADER & TIMEFRAME SELECTOR
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Tableau de Bord des Revenus",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (timeframe == DashboardTimeframe.WEEKLY_DAILY) "Semaine en cours (Lun - Dim)" else "Aujourd'hui par tranche horaire",
                        color = TextSecondaryDark,
                        fontSize = 11.sp
                    )
                }

                // Timeframe Pill Switcher
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF1C2636)
                ) {
                    Row(modifier = Modifier.padding(2.dp)) {
                        TimeframeButton(
                            text = "Jour",
                            isSelected = timeframe == DashboardTimeframe.DAILY_HOURLY,
                            onClick = {
                                timeframe = DashboardTimeframe.DAILY_HOURLY
                                selectedIndex = 5 // default to evening rush hour
                            }
                        )
                        TimeframeButton(
                            text = "Semaine",
                            isSelected = timeframe == DashboardTimeframe.WEEKLY_DAILY,
                            onClick = {
                                timeframe = DashboardTimeframe.WEEKLY_DAILY
                                selectedIndex = 3 // default to Thursday
                            }
                        )
                    }
                }
            }

            // SUMMARY KPI ROW
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF182232))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (timeframe == DashboardTimeframe.WEEKLY_DAILY) "Total Semaine" else "Total Journée",
                        color = TextMutedDark,
                        fontSize = 11.sp
                    )
                    Text(
                        text = String.format(Locale.FRANCE, "%.2f €", totalPeriodAmount),
                        color = AberMint,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black
                    )
                    if (timeframe == DashboardTimeframe.WEEKLY_DAILY) {
                        val pct = ((weekTotal / weeklyGoal) * 100).toInt()
                        Text(
                            text = "Objectif : $pct% ($weeklyGoal €)",
                            color = AberGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                VerticalDivider(
                    modifier = Modifier.height(32.dp),
                    color = Color.White.copy(alpha = 0.1f)
                )

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Moyenne / course",
                        color = TextMutedDark,
                        fontSize = 11.sp
                    )
                    val avgPerTrip = if (timeframe == DashboardTimeframe.WEEKLY_DAILY) 1289.50 / 84 else 184.50 / 12
                    Text(
                        text = String.format(Locale.FRANCE, "%.2f €", avgPerTrip),
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // INTERACTIVE TOOLTIP POPUP FOR SELECTED BAR
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF1E293B),
                border = androidx.compose.foundation.BorderStroke(1.dp, AberMint.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (activePoint.isHighlight) AberMint else AberGold)
                        )
                        Text(
                            text = "${activePoint.label} : ${String.format(Locale.FRANCE, "%.2f €", activePoint.value)}",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = activePoint.detail,
                        color = TextSecondaryDark,
                        fontSize = 11.sp,
                        maxLines = 1
                    )
                }
            }

            // NATIVE JETPACK COMPOSE BAR CHART CANVAS
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(currentData) {
                            detectTapGestures { offset ->
                                val slotWidth = size.width / currentData.size
                                val clickedIndex = (offset.x / slotWidth).toInt().coerceIn(0, currentData.lastIndex)
                                selectedIndex = clickedIndex
                            }
                        }
                ) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height
                    val barCount = currentData.size
                    val slotWidth = canvasWidth / barCount
                    val barWidth = slotWidth * 0.55f

                    // Draw subtle horizontal grid lines
                    val gridSteps = 3
                    for (i in 1..gridSteps) {
                        val y = canvasHeight * (i.toFloat() / (gridSteps + 1))
                        drawLine(
                            color = Color.White.copy(alpha = 0.05f),
                            start = Offset(0f, y),
                            end = Offset(canvasWidth, y),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    // Draw each bar with animated height & active selection outline
                    currentData.forEachIndexed { index, point ->
                        val isSelected = index == safeSelectedIndex
                        val fraction = (point.value / maxValue).coerceIn(0.08f, 1f)
                        val barHeight = canvasHeight * fraction * 0.85f
                        val left = (index * slotWidth) + (slotWidth - barWidth) / 2f
                        val top = canvasHeight - barHeight

                        val barBrush = if (isSelected) {
                            Brush.verticalGradient(
                                listOf(AberMint, AberTeal)
                            )
                        } else if (point.isHighlight) {
                            Brush.verticalGradient(
                                listOf(AberMint.copy(alpha = 0.8f), AberTeal.copy(alpha = 0.5f))
                            )
                        } else {
                            Brush.verticalGradient(
                                listOf(Color.White.copy(alpha = 0.25f), Color.White.copy(alpha = 0.08f))
                            )
                        }

                        // Draw Bar
                        drawRoundRect(
                            brush = barBrush,
                            topLeft = Offset(left, top),
                            size = Size(barWidth, barHeight),
                            cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                        )

                        // Selection Glow Ring on top of active bar
                        if (isSelected) {
                            drawRoundRect(
                                color = AberMint,
                                topLeft = Offset(left - 2.dp.toPx(), top - 2.dp.toPx()),
                                size = Size(barWidth + 4.dp.toPx(), barHeight + 4.dp.toPx()),
                                cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx()),
                                style = Stroke(width = 2.dp.toPx())
                            )
                        }
                    }
                }
            }

            // LABELS ROW BELOW BARS
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                currentData.forEachIndexed { index, point ->
                    val isSelected = index == safeSelectedIndex
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedIndex = index }
                    ) {
                        Text(
                            text = point.label,
                            color = if (isSelected) AberMint else if (point.isHighlight) TextPrimaryDark else TextMutedDark,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium
                        )
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 2.dp)
                                    .size(4.dp)
                                    .clip(CircleShape)
                                    .background(AberMint)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TimeframeButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) AberMint else Color.Transparent
    ) {
        Text(
            text = text,
            color = if (isSelected) Color(0xFF003829) else TextSecondaryDark,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.EarningsRecord
import com.example.ui.theme.*

@Composable
fun TripCompletionDialog(
    record: EarningsRecord,
    onDismiss: () -> Unit
) {
    var rating by remember { mutableStateOf(5) }
    var selectedBadges by remember { mutableStateOf(setOf("Passager ponctuel", "Très respectueux")) }

    val badges = listOf(
        "Passager ponctuel",
        "Très respectueux",
        "Communication fluide",
        "Agréable & courtois"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(vertical = 16.dp)
                .testTag("trip_completion_dialog"),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF131A26)),
            elevation = CardDefaults.cardElevation(defaultElevation = 24.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, AberMint.copy(alpha = 0.3f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Success Badge Icon
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(AberMint.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = AberMint,
                        modifier = Modifier.size(38.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Course terminée !",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "Gains nets crédités sur votre solde LyonTaxis",
                    color = TextSecondaryDark,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Net Earnings Box
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = Color(0xFF1C2534),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AberMint.copy(alpha = 0.25f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Net perçu",
                            color = TextSecondaryDark,
                            fontSize = 12.sp
                        )
                        Text(
                            text = String.format("+ %.2f €", record.netEarnings),
                            color = AberMint,
                            fontSize = 34.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Financial Breakdown List
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FareRow("Tarif de base", String.format("%.2f €", record.baseFare))
                    if (record.surgeBonus > 0) {
                        FareRow("Majoration forte demande", String.format("+ %.2f €", record.surgeBonus), highlightColor = AberOrange)
                    }
                    if (record.tip > 0) {
                        FareRow("Pourboire passager", String.format("+ %.2f €", record.tip), highlightColor = AberGold)
                    }
                    FareRow("Frais de service Aber (15%)", String.format("- %.2f €", record.platformFee), isMuted = true)
                }

                Divider(
                    modifier = Modifier.padding(vertical = 14.dp),
                    color = Color.White.copy(alpha = 0.1f)
                )

                // Rate Passenger Section
                Text(
                    text = "Notez votre passager (${record.passengerName})",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 5 Star Rating Row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    (1..5).forEach { starIndex ->
                        Icon(
                            imageVector = if (starIndex <= rating) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "Étoile $starIndex",
                            tint = if (starIndex <= rating) AberGold else TextMutedDark,
                            modifier = Modifier
                                .size(32.dp)
                                .clickable { rating = starIndex }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Compliment chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    badges.take(2).forEach { badge ->
                        val isSelected = selectedBadges.contains(badge)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedBadges = if (isSelected) selectedBadges - badge else selectedBadges + badge
                            },
                            label = { Text(badge, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AberMint.copy(alpha = 0.2f),
                                selectedLabelColor = AberMint
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Continue / Go Online button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("dismiss_trip_summary_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AberMint,
                        contentColor = Color(0xFF003829)
                    )
                ) {
                    Text(
                        text = "REPRENDRE LES COURSES",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}

@Composable
private fun FareRow(
    title: String,
    value: String,
    highlightColor: Color = Color.White,
    isMuted: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = if (isMuted) TextMutedDark else TextSecondaryDark,
            fontSize = 13.sp
        )
        Text(
            text = value,
            color = if (isMuted) TextMutedDark else highlightColor,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

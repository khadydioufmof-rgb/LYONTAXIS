package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DriverProfile
import com.example.model.EarningsRecord
import com.example.ui.components.EarningsDashboardChart
import com.example.ui.theme.*
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EarningsScreen(
    profile: DriverProfile,
    earningsList: List<EarningsRecord>,
    showPayoutSuccessDialog: Boolean,
    lastPayoutAmount: Double,
    onInstantPayout: (Double) -> Unit,
    onDismissPayoutDialog: () -> Unit,
    onUpdateWeeklyGoal: (Double) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedPeriod by remember { mutableStateOf("Aujourd'hui") }
    var showPayoutSheet by remember { mutableStateOf(false) }
    var showSetWeeklyGoalDialog by remember { mutableStateOf(false) }
    var showExportReportDialog by remember { mutableStateOf(false) }
    var selectedRecordForReceipt by remember { mutableStateOf<EarningsRecord?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Revenus & Portefeuille",
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp,
                        color = Color.White
                    )
                },
                actions = {
                    IconButton(
                        onClick = { showExportReportDialog = true },
                        modifier = Modifier.testTag("export_report_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FileDownload,
                            contentDescription = "Exporter les rapports de revenus",
                            tint = AberMint
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0B0F17)
                )
            )
        },
        containerColor = Color(0xFF0B0F17),
        modifier = modifier.testTag("earnings_screen")
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // PERIOD SELECTOR
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF131A26))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf("Aujourd'hui", "Cette semaine", "Ce mois").forEach { period ->
                        val isSelected = selectedPeriod == period
                        Surface(
                            onClick = { selectedPeriod = period },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) AberMint else Color.Transparent,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(vertical = 8.dp)
                            ) {
                                Text(
                                    text = period,
                                    color = if (isSelected) Color(0xFF003829) else TextSecondaryDark,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }

            // WALLET BALANCE & INSTANT PAYOUT CARD
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF16202E)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AberMint.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Solde disponible",
                                color = TextSecondaryDark,
                                fontSize = 13.sp
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = AberMint.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "Virement 60s",
                                    color = AberMint,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Text(
                            text = String.format("%.2f €", profile.balance),
                            color = Color.White,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Black
                        )

                        // Instant Payout Trigger Button
                        Button(
                            onClick = { showPayoutSheet = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("open_payout_sheet_button"),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AberMint,
                                contentColor = Color(0xFF003829)
                            )
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalanceWallet,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "VIREMENT INSTANTANÉ",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                }
            }

            // WEEKLY EARNINGS GOAL & PROGRESS DASHBOARD
            item {
                val weeklyGoal = profile.weeklyGoalEarnings
                val weeklyCurrent = profile.weeklyEarnings
                val progressFraction = (weeklyCurrent / weeklyGoal).toFloat().coerceIn(0f, 1f)
                val progressPercent = ((weeklyCurrent / weeklyGoal) * 100).toInt()
                val isGoalAchieved = weeklyCurrent >= weeklyGoal
                val remainingAmount = (weeklyGoal - weeklyCurrent).coerceAtLeast(0.0)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("weekly_goal_card"),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF131A26)),
                    border = BorderStroke(1.dp, AberMint.copy(alpha = 0.35f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Header with Title, Badge and Edit Action
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(if (isGoalAchieved) AberGold.copy(alpha = 0.2f) else AberMint.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isGoalAchieved) Icons.Default.EmojiEvents else Icons.Default.TrackChanges,
                                        contentDescription = null,
                                        tint = if (isGoalAchieved) AberGold else AberMint,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Column {
                                    Text(
                                        text = "Objectif Hebdomadaire",
                                        color = Color.White,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Tableau de bord de rentabilité",
                                        color = TextSecondaryDark,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            // Edit Goal Button
                            Surface(
                                onClick = { showSetWeeklyGoalDialog = true },
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFF1E293B),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                                modifier = Modifier.testTag("set_weekly_goal_button")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Tune,
                                        contentDescription = "Définir l'objectif",
                                        tint = AberMint,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "Modifier",
                                        color = AberMint,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // Progress Amounts & Percentage Display
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Column {
                                Text(
                                    text = "Revenus réalisés",
                                    color = TextMutedDark,
                                    fontSize = 11.sp
                                )
                                Row(
                                    verticalAlignment = Alignment.Bottom,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = String.format(Locale.FRANCE, "%.2f €", weeklyCurrent),
                                        color = Color.White,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                    Text(
                                        text = "/ ${String.format(Locale.FRANCE, "%.0f €", weeklyGoal)}",
                                        color = TextSecondaryDark,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(bottom = 2.dp)
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isGoalAchieved) AberGold.copy(alpha = 0.2f) else AberMint.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "$progressPercent%",
                                    color = if (isGoalAchieved) AberGold else AberMint,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        // Custom Styled Progress Bar with milestones
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            LinearProgressIndicator(
                                progress = { progressFraction },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(12.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .testTag("weekly_goal_progress_bar"),
                                color = if (isGoalAchieved) AberGold else AberMint,
                                trackColor = Color.White.copy(alpha = 0.08f)
                            )

                            // Milestone markers
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("0 €", color = TextMutedDark, fontSize = 9.sp)
                                Text("25%", color = if (progressFraction >= 0.25f) AberMint else TextMutedDark, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Text("50%", color = if (progressFraction >= 0.50f) AberMint else TextMutedDark, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Text("75%", color = if (progressFraction >= 0.75f) AberMint else TextMutedDark, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Text("🎯 100%", color = if (isGoalAchieved) AberGold else TextMutedDark, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Dynamic Guidance Banner
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF182230),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = if (isGoalAchieved) Icons.Default.CheckCircle else Icons.Default.Lightbulb,
                                    contentDescription = null,
                                    tint = if (isGoalAchieved) AberGold else AberMint,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = if (isGoalAchieved) {
                                        "Félicitations ! Objectif franchi avec un bonus de +${String.format(Locale.FRANCE, "%.2f €", weeklyCurrent - weeklyGoal)}."
                                    } else {
                                        "Reste ${String.format(Locale.FRANCE, "%.2f €", remainingAmount)} à générer pour compléter votre semaine."
                                    },
                                    color = TextSecondaryDark,
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }
                }
            }

            // DASHBOARD EARNINGS CHART (DAILY & WEEKLY)
            item {
                EarningsDashboardChart(
                    todayTotal = profile.todayEarnings,
                    weekTotal = profile.weeklyEarnings,
                    weeklyGoal = profile.weeklyGoalEarnings
                )
            }

            // KEY PERFORMANCE METRICS
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricBox(
                        title = "Courses",
                        value = "${profile.todayTripsCount}",
                        icon = Icons.Default.DirectionsCar,
                        modifier = Modifier.weight(1f)
                    )
                    MetricBox(
                        title = "Temps connecté",
                        value = "${profile.todayOnlineHours}h",
                        icon = Icons.Default.Schedule,
                        modifier = Modifier.weight(1f)
                    )
                    MetricBox(
                        title = "Acceptation",
                        value = "${profile.acceptanceRate}%",
                        icon = Icons.Default.ThumbUp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // TRANSACTIONS HEADER
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Historique des courses",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${earningsList.size} trajets enregistrés",
                            color = TextSecondaryDark,
                            fontSize = 11.sp
                        )
                    }

                    OutlinedButton(
                        onClick = { showExportReportDialog = true },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = AberMint
                        ),
                        border = BorderStroke(1.dp, AberMint.copy(alpha = 0.5f)),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("export_summary_report_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = AberMint
                            )
                            Text(
                                text = "Exporter récapitulatif",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // TRANSACTIONS LIST
            items(earningsList) { record ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedRecordForReceipt = record },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF131A26))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(AberMint.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.DirectionsCar,
                                contentDescription = null,
                                tint = AberMint,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = record.passengerName,
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "• ${record.category.displayName}",
                                    color = TextMutedDark,
                                    fontSize = 11.sp
                                )
                            }
                            Text(
                                text = record.timestamp,
                                color = TextSecondaryDark,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "${record.pickup.take(18)}... → ${record.dropoff.take(18)}...",
                                color = TextMutedDark,
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = String.format("+ %.2f €", record.netEarnings),
                                color = AberMint,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black
                            )
                            if (record.tip > 0) {
                                Text(
                                    text = "dont pourboire ${String.format("%.2f €", record.tip)}",
                                    color = AberGold,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // INSTANT PAYOUT MODAL SHEET
    if (showPayoutSheet) {
        var payoutAmount by remember { mutableStateOf(profile.balance.toString()) }
        AlertDialog(
            onDismissRequest = { showPayoutSheet = false },
            title = {
                Text(
                    text = "Virement instantané",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Transférez vos gains vers votre compte bancaire (IBAN FR76 **** 4892) en 60 secondes.",
                        color = TextSecondaryDark,
                        fontSize = 13.sp
                    )

                    OutlinedTextField(
                        value = payoutAmount,
                        onValueChange = { payoutAmount = it },
                        label = { Text("Montant (€)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AberMint,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = "Solde max : ${String.format("%.2f €", profile.balance)} (Frais de virement : 0,00€)",
                        color = AberMint,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = payoutAmount.toDoubleOrNull() ?: profile.balance
                        onInstantPayout(amt)
                        showPayoutSheet = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AberMint)
                ) {
                    Text("Valider le virement", color = Color(0xFF003829), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPayoutSheet = false }) {
                    Text("Annuler", color = TextSecondaryDark)
                }
            },
            containerColor = Color(0xFF16202E)
        )
    }

    // PAYOUT SUCCESS NOTIFICATION DIALOG
    if (showPayoutSuccessDialog) {
        AlertDialog(
            onDismissRequest = onDismissPayoutDialog,
            icon = {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = AberMint,
                    modifier = Modifier.size(44.dp)
                )
            },
            title = {
                Text("Virement effectué !", color = Color.White, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    text = "La somme de ${String.format("%.2f €", lastPayoutAmount)} a été envoyée instantanément vers votre compte bancaire.",
                    color = TextSecondaryDark
                )
            },
            confirmButton = {
                Button(
                    onClick = onDismissPayoutDialog,
                    colors = ButtonDefaults.buttonColors(containerColor = AberMint)
                ) {
                    Text("Compris", color = Color(0xFF003829))
                }
            },
            containerColor = Color(0xFF16202E)
        )
    }

    // RECEIPT DETAILS DIALOG
    selectedRecordForReceipt?.let { rec ->
        AlertDialog(
            onDismissRequest = { selectedRecordForReceipt = null },
            title = {
                Text("Détail de la course ${rec.id}", color = Color.White, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Passager : ${rec.passengerName}", color = TextSecondaryDark, fontSize = 13.sp)
                    Text("Prise en charge : ${rec.pickup}", color = TextSecondaryDark, fontSize = 13.sp)
                    Text("Destination : ${rec.dropoff}", color = TextSecondaryDark, fontSize = 13.sp)
                    Text("Distance : ${rec.distanceKm} km (${rec.durationMin} min)", color = TextSecondaryDark, fontSize = 13.sp)
                    Divider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 4.dp))
                    Text("Tarif de base : ${String.format("%.2f €", rec.baseFare)}", color = Color.White, fontSize = 13.sp)
                    if (rec.surgeBonus > 0) {
                        Text("Majoration : +${String.format("%.2f €", rec.surgeBonus)}", color = AberOrange, fontSize = 13.sp)
                    }
                    if (rec.tip > 0) {
                        Text("Pourboire : +${String.format("%.2f €", rec.tip)}", color = AberGold, fontSize = 13.sp)
                    }
                    Text("Net chauffeur : ${String.format("%.2f €", rec.netEarnings)}", color = AberMint, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            },
            confirmButton = {
                Button(
                    onClick = { selectedRecordForReceipt = null },
                    colors = ButtonDefaults.buttonColors(containerColor = AberMint)
                ) {
                    Text("Fermer", color = Color(0xFF003829))
                }
            },
            containerColor = Color(0xFF16202E)
        )
    }

    // SET / EDIT WEEKLY GOAL DIALOG
    if (showSetWeeklyGoalDialog) {
        SetWeeklyGoalDialog(
            currentGoal = profile.weeklyGoalEarnings,
            onDismiss = { showSetWeeklyGoalDialog = false },
            onConfirm = { newGoal ->
                onUpdateWeeklyGoal(newGoal)
                showSetWeeklyGoalDialog = false
            }
        )
    }

    // EXPORT REVENUE REPORT DIALOG
    if (showExportReportDialog) {
        ExportRevenueReportDialog(
            profile = profile,
            earningsList = earningsList,
            onDismiss = { showExportReportDialog = false }
        )
    }
}

@Composable
fun SetWeeklyGoalDialog(
    currentGoal: Double,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var goalText by remember { mutableStateOf(currentGoal.toInt().toString()) }
    val presetGoals = listOf(800, 1000, 1200, 1500, 1800, 2000, 2500)
    val parsedGoal = goalText.toDoubleOrNull() ?: currentGoal

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("set_weekly_goal_dialog"),
        icon = {
            Icon(
                imageVector = Icons.Default.TrackChanges,
                contentDescription = null,
                tint = AberMint,
                modifier = Modifier.size(36.dp)
            )
        },
        title = {
            Text(
                text = "Objectif Hebdomadaire",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Fixez un montant cible pour optimiser vos gains et suivre votre progression sur le tableau de bord.",
                    color = TextSecondaryDark,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )

                // Input Field
                OutlinedTextField(
                    value = goalText,
                    onValueChange = { input ->
                        if (input.all { it.isDigit() } && input.length <= 6) {
                            goalText = input
                        }
                    },
                    label = { Text("Objectif (€ / semaine)", color = TextSecondaryDark) },
                    trailingIcon = {
                        Text("€", color = AberMint, fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(end = 12.dp))
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AberMint,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("weekly_goal_input")
                )

                // Quick preset chips
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Suggestions rapides :",
                        color = TextMutedDark,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(presetGoals) { preset ->
                            val isSelected = goalText == preset.toString()
                            Surface(
                                onClick = { goalText = preset.toString() },
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) AberMint else Color(0xFF1E293B),
                                border = BorderStroke(1.dp, if (isSelected) AberMint else Color.White.copy(alpha = 0.1f))
                            ) {
                                Text(
                                    text = "$preset €",
                                    color = if (isSelected) Color(0xFF003829) else Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                // Daily breakdown projection
                if (parsedGoal > 0) {
                    val dailyAvg6Days = parsedGoal / 6.0
                    val dailyAvg5Days = parsedGoal / 5.0
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF1B2433),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Rythme estimé :",
                                color = AberMint,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "• Sur 6 jours de travail : ~${String.format(Locale.FRANCE, "%.0f € / jour", dailyAvg6Days)}",
                                color = TextSecondaryDark,
                                fontSize = 11.sp
                            )
                            Text(
                                text = "• Sur 5 jours de travail : ~${String.format(Locale.FRANCE, "%.0f € / jour", dailyAvg5Days)}",
                                color = TextSecondaryDark,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalGoal = goalText.toDoubleOrNull() ?: currentGoal
                    onConfirm(finalGoal)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = AberMint,
                    contentColor = Color(0xFF003829)
                ),
                modifier = Modifier.testTag("save_weekly_goal_button")
            ) {
                Text("Enregistrer l'objectif", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler", color = TextSecondaryDark)
            }
        },
        containerColor = Color(0xFF16202E)
    )
}

@Composable
private fun MetricBox(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131A26))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AberMint,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = value,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = title,
                color = TextSecondaryDark,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
fun ExportRevenueReportDialog(
    profile: DriverProfile,
    earningsList: List<EarningsRecord>,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var selectedReportType by remember { mutableStateOf("WEEKLY") } // "WEEKLY" or "MONTHLY"
    var isCopied by remember { mutableStateOf(false) }

    val reportText = remember(selectedReportType, profile, earningsList) {
        generateEarningsReportText(
            type = selectedReportType,
            profile = profile,
            records = earningsList
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("export_revenue_report_dialog"),
        icon = {
            Icon(
                imageVector = Icons.Default.Description,
                contentDescription = null,
                tint = AberMint,
                modifier = Modifier.size(36.dp)
            )
        },
        title = {
            Text(
                text = "Rapport Récapitulatif des Revenus",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Exportez votre synthèse d'activité et vos gains sous forme de fichier texte récapitulatif pour votre comptabilité.",
                    color = TextSecondaryDark,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )

                // Period Toggle (Weekly vs Monthly)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1E293B))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val isWeekly = selectedReportType == "WEEKLY"
                    Surface(
                        onClick = {
                            selectedReportType = "WEEKLY"
                            isCopied = false
                        },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isWeekly) AberMint else Color.Transparent,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            Text(
                                text = "📅 Hebdomadaire",
                                color = if (isWeekly) Color(0xFF003829) else TextSecondaryDark,
                                fontSize = 13.sp,
                                fontWeight = if (isWeekly) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }

                    val isMonthly = selectedReportType == "MONTHLY"
                    Surface(
                        onClick = {
                            selectedReportType = "MONTHLY"
                            isCopied = false
                        },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isMonthly) AberMint else Color.Transparent,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            Text(
                                text = "📊 Mensuel",
                                color = if (isMonthly) Color(0xFF003829) else TextSecondaryDark,
                                fontSize = 13.sp,
                                fontWeight = if (isMonthly) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                // Summary Highlights in Dialog
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF131A26),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (selectedReportType == "WEEKLY") "Total Semaine :" else "Total Estimé Mois :",
                                color = TextSecondaryDark,
                                fontSize = 12.sp
                            )
                            Text(
                                text = if (selectedReportType == "WEEKLY") {
                                    String.format(Locale.FRANCE, "%.2f €", profile.weeklyEarnings)
                                } else {
                                    String.format(Locale.FRANCE, "%.2f €", profile.weeklyEarnings * 4.2)
                                },
                                color = AberMint,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Courses enregistrées :",
                                color = TextSecondaryDark,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "${earningsList.size} courses",
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Format d'export :",
                                color = TextSecondaryDark,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "Fichier Texte (.txt)",
                                color = AberGold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                // Text Preview Box
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF0F1520),
                    border = BorderStroke(1.dp, AberMint.copy(alpha = 0.2f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(10.dp)
                    ) {
                        Text(
                            text = reportText,
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 13.sp,
                            maxLines = 7
                        )
                    }
                }

                // Copy to Clipboard Action
                OutlinedButton(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(reportText))
                        isCopied = true
                        Toast.makeText(context, "Rapport copié dans le presse-papier !", Toast.LENGTH_SHORT).show()
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (isCopied) AberMint else Color.White
                    ),
                    border = BorderStroke(1.dp, if (isCopied) AberMint else Color.White.copy(alpha = 0.2f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .testTag("copy_report_text_button")
                ) {
                    Icon(
                        imageVector = if (isCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (isCopied) AberMint else Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isCopied) "Texte copié !" else "Copier le texte récapitulatif",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    exportAndShareReportText(
                        context = context,
                        reportText = reportText,
                        type = selectedReportType
                    )
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = AberMint,
                    contentColor = Color(0xFF003829)
                ),
                modifier = Modifier.testTag("share_export_report_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Partager / Enregistrer .txt",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Fermer", color = TextSecondaryDark)
            }
        },
        containerColor = Color(0xFF16202E)
    )
}

/**
 * Generates structured, readable plain-text report for accounting & personal record keeping.
 */
fun generateEarningsReportText(
    type: String,
    profile: DriverProfile,
    records: List<EarningsRecord>
): String {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRANCE)
    val periodTitle = if (type == "WEEKLY") "RAPPORT HEBDOMADAIRE" else "RAPPORT MENSUEL"
    val totalRevenue = if (type == "WEEKLY") profile.weeklyEarnings else profile.weeklyEarnings * 4.2
    val goal = if (type == "WEEKLY") profile.weeklyGoalEarnings else profile.weeklyGoalEarnings * 4
    val completionPct = ((totalRevenue / goal) * 100).toInt()

    val totalTips = records.sumOf { it.tip }
    val totalSurge = records.sumOf { it.surgeBonus }
    val totalDistance = records.sumOf { it.distanceKm }

    val sb = StringBuilder()
    sb.appendLine("==================================================")
    sb.appendLine("   LYON TAXIS PRO - $periodTitle")
    sb.appendLine("==================================================")
    sb.appendLine("Date d'édition   : ${dateFormat.format(Date())}")
    sb.appendLine("Chauffeur        : ${profile.name} (ID: DRV-77002)")
    sb.appendLine("Véhicule         : ${profile.vehicleModel} [${profile.vehiclePlate}]")
    sb.appendLine("Note Chauffeur   : ${profile.rating} ★")
    sb.appendLine("--------------------------------------------------")
    sb.appendLine("SYNTHÈSE FINANCIÈRE & PERFORMANCE")
    sb.appendLine("--------------------------------------------------")
    sb.appendLine(String.format(Locale.FRANCE, "Total Gains Net : %.2f €", totalRevenue))
    sb.appendLine(String.format(Locale.FRANCE, "Objectif Fixé   : %.2f €", goal))
    sb.appendLine("Atteinte Cible  : $completionPct %")
    sb.appendLine(String.format(Locale.FRANCE, "Total Pourboires: %.2f €", totalTips))
    sb.appendLine(String.format(Locale.FRANCE, "Majoration Surge: %.2f €", totalSurge))
    sb.appendLine("Courses Réalisées: ${records.size} trajets")
    sb.appendLine(String.format(Locale.FRANCE, "Distance Totale : %.1f km", totalDistance))
    sb.appendLine("Taux Acceptation: ${profile.acceptanceRate} %")
    sb.appendLine("--------------------------------------------------")
    sb.appendLine("DÉTAIL DES TRAJETS RÉCENTS")
    sb.appendLine("--------------------------------------------------")

    if (records.isEmpty()) {
        sb.appendLine("Aucun trajet enregistré sur la période.")
    } else {
        records.forEachIndexed { index, rec ->
            sb.appendLine("${index + 1}. [${rec.timestamp}] ${rec.pickup} -> ${rec.dropoff}")
            sb.appendLine(
                String.format(
                    Locale.FRANCE,
                    "   Passager: %s | %.1f km (%d min) | Net: %.2f € (Base: %.2f € | Maj: %.2f € | Tip: %.2f €)",
                    rec.passengerName,
                    rec.distanceKm,
                    rec.durationMin,
                    rec.netEarnings,
                    rec.baseFare,
                    rec.surgeBonus,
                    rec.tip
                )
            )
        }
    }

    sb.appendLine("==================================================")
    sb.appendLine("Document récapitulatif généré via Aber Driver App")
    sb.appendLine("==================================================")

    return sb.toString()
}

/**
 * Writes the report to cache file and invokes Android Share Intent.
 */
private fun exportAndShareReportText(
    context: Context,
    reportText: String,
    type: String
) {
    try {
        val fileName = "Aber_Revenus_${type.lowercase()}_${System.currentTimeMillis()}.txt"
        val file = File(context.cacheDir, fileName)
        FileOutputStream(file).use { out ->
            out.write(reportText.toByteArray())
        }

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            this.type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Rapport de revenus LyonTaxis Pro ($type)")
            putExtra(Intent.EXTRA_TEXT, reportText)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        context.startActivity(Intent.createChooser(shareIntent, "Exporter le rapport de revenus"))
    } catch (e: Exception) {
        Toast.makeText(context, "Erreur lors de l'export du fichier texte : ${e.localizedMessage}", Toast.LENGTH_LONG).show()
    }
}

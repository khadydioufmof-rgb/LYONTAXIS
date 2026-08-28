package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.VehicleProfileEntity
import com.example.model.DriverProfile
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverProfileScreen(
    profile: DriverProfile,
    vehicleEntity: VehicleProfileEntity,
    onToggleCash: () -> Unit,
    onToggleAutoAccept: () -> Unit,
    onToggleHomeDestination: () -> Unit,
    onUpdateVehicleProfile: (
        model: String,
        plate: String,
        color: String,
        category: String,
        inspectionStatus: String,
        lastInspectionDate: String,
        nextInspectionDate: String,
        mileageKm: Int,
        technicalNotes: String
    ) -> Unit,
    onLogout: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showDocDialog by remember { mutableStateOf(false) }
    var showEditVehicleSheet by remember { mutableStateOf(false) }
    var showSuccessSnackbar by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Profil & Véhicule",
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp,
                        color = Color.White
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0B0F17)
                )
            )
        },
        snackbarHost = {
            if (showSuccessSnackbar) {
                Surface(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = AberMint,
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF003829)
                        )
                        Text(
                            text = "Profil véhicule et statut d'entretien enregistrés dans Room !",
                            color = Color(0xFF003829),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        },
        containerColor = Color(0xFF0B0F17),
        modifier = modifier.testTag("driver_profile_screen")
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // 1. DRIVER ID CARD
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF141C28)),
                    border = BorderStroke(1.dp, AberMint.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(Brush.linearGradient(listOf(AberMint, AberTeal))),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "KB",
                                    color = Color(0xFF003829),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 24.sp
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = profile.name,
                                        color = Color.White,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                    Icon(
                                        imageVector = Icons.Default.Verified,
                                        contentDescription = null,
                                        tint = AberMint,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = AberGold.copy(alpha = 0.15f),
                                    modifier = Modifier.padding(top = 2.dp)
                                ) {
                                    Text(
                                        text = profile.rankBadge,
                                        color = AberGold,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        // Stats Row (Rating, Trips, Accept Rate)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFF1C2534))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            DriverStatPill(
                                label = "Note",
                                value = "★ ${profile.rating}",
                                accentColor = AberGold
                            )
                            DriverStatPill(
                                label = "Courses",
                                value = "${profile.totalRides}",
                                accentColor = Color.White
                            )
                            DriverStatPill(
                                label = "Acceptation",
                                value = "${profile.acceptanceRate}%",
                                accentColor = AberMint
                            )
                            DriverStatPill(
                                label = "Annulation",
                                value = "${profile.cancellationRate}%",
                                accentColor = AberMint
                            )
                        }
                    }
                }
            }

            // 2. VEHICLE MANAGEMENT & MAINTENANCE (ROOM LOCAL STORAGE)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("vehicle_profile_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF131A26)),
                    border = BorderStroke(1.dp, AberMint.copy(alpha = 0.25f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Section Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DirectionsCar,
                                    contentDescription = null,
                                    tint = AberMint,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Véhicule & Entretien",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Inspection status badge
                            val isCompliant = vehicleEntity.inspectionStatus.contains("Conforme", ignoreCase = true) ||
                                    vehicleEntity.inspectionStatus.contains("Validé", ignoreCase = true)
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isCompliant) AberMint.copy(alpha = 0.18f) else AberGold.copy(alpha = 0.18f),
                                border = BorderStroke(
                                    1.dp,
                                    if (isCompliant) AberMint.copy(alpha = 0.5f) else AberGold.copy(alpha = 0.5f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(if (isCompliant) AberMint else AberGold)
                                    )
                                    Text(
                                        text = vehicleEntity.inspectionStatus,
                                        color = if (isCompliant) AberMint else AberGold,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // Vehicle Main Details
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xFF1E293B)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CarRental,
                                    contentDescription = null,
                                    tint = AberMint,
                                    modifier = Modifier.size(30.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = vehicleEntity.model,
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = "Couleur : ${vehicleEntity.color} • ${vehicleEntity.category}",
                                    color = TextSecondaryDark,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        // License plate display box (French / EU style)
                        LicensePlateDisplay(plate = vehicleEntity.plate)

                        // Maintenance & Technical Details Grid
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFF182230))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                DetailItem(
                                    icon = Icons.Default.Speed,
                                    label = "Kilométrage",
                                    value = "${"%,d".format(vehicleEntity.mileageKm).replace(',', ' ')} km"
                                )
                                DetailItem(
                                    icon = Icons.Default.Event,
                                    label = "Dernier contrôle",
                                    value = vehicleEntity.lastInspectionDate
                                )
                                DetailItem(
                                    icon = Icons.Default.Update,
                                    label = "Prochaine révision",
                                    value = vehicleEntity.nextInspectionDate
                                )
                            }

                            if (vehicleEntity.technicalNotes.isNotBlank()) {
                                HorizontalDivider(color = Color.White.copy(alpha = 0.06f))
                                Row(
                                    verticalAlignment = Alignment.Top,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Build,
                                        contentDescription = null,
                                        tint = TextMutedDark,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = vehicleEntity.technicalNotes,
                                        color = TextSecondaryDark,
                                        fontSize = 11.sp,
                                        lineHeight = 15.sp
                                    )
                                }
                            }
                        }

                        // Edit Button (Triggers Room Update Interface)
                        Button(
                            onClick = { showEditVehicleSheet = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("edit_vehicle_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = AberMint),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                tint = Color(0xFF003829),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Modifier le modèle & entretien (Room)",
                                color = Color(0xFF003829),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        // Document quick button
                        OutlinedButton(
                            onClick = { showDocDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Description,
                                contentDescription = null,
                                tint = TextSecondaryDark,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Consulter les pièces & assurances VTC", fontSize = 12.sp)
                        }
                    }
                }
            }

            // 3. DRIVER PREFERENCES & ROUTE FILTERS
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF131A26))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "Préférences de course",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )

                        // Cash rides toggle
                        PrefSwitchRow(
                            title = "Paiements en espèces",
                            subtitle = "Recevoir des courses payables en liquide",
                            checked = profile.isCashAccepted,
                            onCheckedChange = { onToggleCash() }
                        )

                        // Home Destination Filter
                        PrefSwitchRow(
                            title = "Filtre 'Retour Domicile'",
                            subtitle = "Ne recevoir que des trajets vers ${profile.homeAddress}",
                            checked = profile.isHomeDestinationActive,
                            onCheckedChange = { onToggleHomeDestination() }
                        )

                        // Auto-accept toggle
                        PrefSwitchRow(
                            title = "Acceptation automatique",
                            subtitle = "Accepter automatiquement toutes les courses entrantes",
                            checked = profile.isAutoAcceptEnabled,
                            onCheckedChange = { onToggleAutoAccept() }
                        )
                    }
                }
            }

            // 4. ACCOUNT & SESSION MANAGEMENT
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("driver_account_session_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF131A26))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Compte Partenaire & Accès",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AlternateEmail,
                                contentDescription = null,
                                tint = AberMint,
                                modifier = Modifier.size(16.dp)
                            )
                            Column {
                                Text("Email identifiant", color = TextMutedDark, fontSize = 11.sp)
                                Text(profile.email, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Badge,
                                contentDescription = null,
                                tint = AberMint,
                                modifier = Modifier.size(16.dp)
                            )
                            Column {
                                Text("Numéro de carte VTC", color = TextMutedDark, fontSize = 11.sp)
                                Text(profile.vtcLicenseNumber, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        OutlinedButton(
                            onClick = onLogout,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("logout_driver_button"),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, AberRed.copy(alpha = 0.6f)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = AberRed
                            )
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Logout,
                                    contentDescription = null,
                                    tint = AberRed,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Changer de compte / Se déconnecter",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // EDIT VEHICLE & MAINTENANCE MODAL SHEET / DIALOG
    if (showEditVehicleSheet) {
        EditVehicleDialog(
            currentEntity = vehicleEntity,
            onDismiss = { showEditVehicleSheet = false },
            onSave = { updatedModel, updatedPlate, updatedColor, updatedCat, updatedStatus, lastDate, nextDate, mileage, notes ->
                onUpdateVehicleProfile(
                    updatedModel,
                    updatedPlate,
                    updatedColor,
                    updatedCat,
                    updatedStatus,
                    lastDate,
                    nextDate,
                    mileage,
                    notes
                )
                showEditVehicleSheet = false
                showSuccessSnackbar = true
            }
        )
    }

    // VEHICLE DOCUMENTS DIALOG
    if (showDocDialog) {
        AlertDialog(
            onDismissRequest = { showDocDialog = false },
            title = {
                Text(
                    "Documents Chauffeur & Véhicule",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    DocStatusRow("Carte Professionnelle VTC", profile.vtcLicenseNumber, "Valide jusqu'en 2028")
                    DocStatusRow("Assurance RC Pro Circulation", vehicleEntity.insuranceCompany, "Expire le ${vehicleEntity.insuranceExpiry}")
                    DocStatusRow("Carte Grise & Contrôle Technique", vehicleEntity.plate, vehicleEntity.inspectionStatus)
                    DocStatusRow("Vignette Crit'Air", "Crit'Air 1 / Hybride", "Conforme ZFE")
                }
            },
            confirmButton = {
                Button(
                    onClick = { showDocDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = AberMint),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Fermer", color = Color(0xFF003829), fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color(0xFF16202E),
            shape = RoundedCornerShape(18.dp)
        )
    }
}

@Composable
private fun LicensePlateDisplay(plate: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFFF8FAFC),
        border = BorderStroke(1.5.dp, Color(0xFF334155)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // EU / France Blue Left Band
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = Color(0xFF003399)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "★",
                        color = AberGold,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "F",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            // Plate Text
            Text(
                text = plate.uppercase(),
                color = Color(0xFF0F172A),
                fontWeight = FontWeight.Black,
                fontSize = 18.sp,
                letterSpacing = 2.sp,
                fontFamily = FontFamily.Monospace
            )

            // Right Region Band (75 - Paris)
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = Color(0xFF003399)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "IDF",
                        color = Color.White,
                        fontSize = 7.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "75",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Column(horizontalAlignment = Alignment.Start) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AberMint,
                modifier = Modifier.size(12.dp)
            )
            Text(label, color = TextMutedDark, fontSize = 10.sp)
        }
        Text(
            text = value,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun EditVehicleDialog(
    currentEntity: VehicleProfileEntity,
    onDismiss: () -> Unit,
    onSave: (
        model: String,
        plate: String,
        color: String,
        category: String,
        inspectionStatus: String,
        lastInspectionDate: String,
        nextInspectionDate: String,
        mileageKm: Int,
        technicalNotes: String
    ) -> Unit
) {
    var modelText by remember { mutableStateOf(currentEntity.model) }
    var plateText by remember { mutableStateOf(currentEntity.plate) }
    var colorText by remember { mutableStateOf(currentEntity.color) }
    var categoryText by remember { mutableStateOf(currentEntity.category) }
    var inspectionStatusText by remember { mutableStateOf(currentEntity.inspectionStatus) }
    var lastInspectionDateText by remember { mutableStateOf(currentEntity.lastInspectionDate) }
    var nextInspectionDateText by remember { mutableStateOf(currentEntity.nextInspectionDate) }
    var mileageText by remember { mutableStateOf(currentEntity.mileageKm.toString()) }
    var technicalNotesText by remember { mutableStateOf(currentEntity.technicalNotes) }

    val popularModels = listOf(
        "Mercedes-Benz Classe E 220d",
        "Mercedes-Benz EQE 350",
        "Tesla Model 3 Grande Autonomie",
        "Tesla Model Y Propulsion",
        "Toyota Camry Hybride",
        "BMW Série 5 530e",
        "Lexus ES 300h"
    )

    val inspectionStatusOptions = listOf(
        "Conforme & Validé",
        "Révision des 60 000 km requise",
        "Contrôle technique proche",
        "En cours de maintenance",
        "Vidange & Pneus à contrôler"
    )

    val categoryOptions = listOf(
        "Aber Berline & Confort",
        "AberX & Green",
        "Aber Van (7 places)",
        "Aber Confort"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth().testTag("edit_vehicle_dialog"),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DirectionsCar,
                    contentDescription = null,
                    tint = AberMint
                )
                Text(
                    text = "Modifier le Véhicule & Entretien",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Room Storage Info Banner
                item {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF1E293B),
                        border = BorderStroke(1.dp, AberMint.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Storage,
                                contentDescription = null,
                                tint = AberMint,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Persistance locale active via Room Database (Table `vehicle_profile`)",
                                color = TextSecondaryDark,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                // Field 1: Model
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Modèle du véhicule", color = TextPrimaryDark, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        OutlinedTextField(
                            value = modelText,
                            onValueChange = { modelText = it },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("vehicle_model_input"),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF131A26),
                                unfocusedContainerColor = Color(0xFF131A26),
                                focusedBorderColor = AberMint,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        // Quick suggestion pills
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            items(popularModels) { model ->
                                Surface(
                                    onClick = { modelText = model },
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (modelText == model) AberMint.copy(alpha = 0.2f) else Color(0xFF1C2636),
                                    border = BorderStroke(1.dp, if (modelText == model) AberMint else Color.Transparent)
                                ) {
                                    Text(
                                        text = model,
                                        color = if (modelText == model) AberMint else TextSecondaryDark,
                                        fontSize = 10.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Field 2: License Plate & Color
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Immatriculation", color = TextPrimaryDark, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            OutlinedTextField(
                                value = plateText,
                                onValueChange = { plateText = it.uppercase() },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                                modifier = Modifier.fillMaxWidth().testTag("vehicle_plate_input"),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color(0xFF131A26),
                                    unfocusedContainerColor = Color(0xFF131A26),
                                    focusedBorderColor = AberMint,
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )
                        }

                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Couleur", color = TextPrimaryDark, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            OutlinedTextField(
                                value = colorText,
                                onValueChange = { colorText = it },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("vehicle_color_input"),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color(0xFF131A26),
                                    unfocusedContainerColor = Color(0xFF131A26),
                                    focusedBorderColor = AberMint,
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )
                        }
                    }
                }

                // Field 3: Inspection Status
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Statut d'entretien & Contrôle technique", color = TextPrimaryDark, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        OutlinedTextField(
                            value = inspectionStatusText,
                            onValueChange = { inspectionStatusText = it },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("vehicle_inspection_input"),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF131A26),
                                unfocusedContainerColor = Color(0xFF131A26),
                                focusedBorderColor = AberMint,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        // Quick status chips
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            items(inspectionStatusOptions) { status ->
                                val isSelected = inspectionStatusText == status
                                Surface(
                                    onClick = { inspectionStatusText = status },
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) AberMint.copy(alpha = 0.2f) else Color(0xFF1C2636),
                                    border = BorderStroke(1.dp, if (isSelected) AberMint else Color.Transparent)
                                ) {
                                    Text(
                                        text = status,
                                        color = if (isSelected) AberMint else TextSecondaryDark,
                                        fontSize = 10.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Field 4: Kilométrage & Dates
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Kilométrage (km)", color = TextPrimaryDark, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            OutlinedTextField(
                                value = mileageText,
                                onValueChange = { mileageText = it.filter { char -> char.isDigit() } },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth().testTag("vehicle_mileage_input"),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color(0xFF131A26),
                                    unfocusedContainerColor = Color(0xFF131A26),
                                    focusedBorderColor = AberMint,
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )
                        }

                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Prochaine révision", color = TextPrimaryDark, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            OutlinedTextField(
                                value = nextInspectionDateText,
                                onValueChange = { nextInspectionDateText = it },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("vehicle_next_inspection_input"),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color(0xFF131A26),
                                    unfocusedContainerColor = Color(0xFF131A26),
                                    focusedBorderColor = AberMint,
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )
                        }
                    }
                }

                // Field 5: Technical Notes
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Notes techniques & Carnet d'entretien", color = TextPrimaryDark, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        OutlinedTextField(
                            value = technicalNotesText,
                            onValueChange = { technicalNotesText = it },
                            minLines = 2,
                            maxLines = 3,
                            modifier = Modifier.fillMaxWidth().testTag("vehicle_notes_input"),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF131A26),
                                unfocusedContainerColor = Color(0xFF131A26),
                                focusedBorderColor = AberMint,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsedKm = mileageText.toIntOrNull() ?: currentEntity.mileageKm
                    onSave(
                        modelText,
                        plateText,
                        colorText,
                        categoryText,
                        inspectionStatusText,
                        lastInspectionDateText,
                        nextInspectionDateText,
                        parsedKm,
                        technicalNotesText
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = AberMint),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("save_vehicle_button")
            ) {
                Text("Enregistrer dans Room", color = Color(0xFF003829), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
            ) {
                Text("Annuler")
            }
        },
        containerColor = Color(0xFF16202E),
        shape = RoundedCornerShape(22.dp)
    )
}

@Composable
private fun DriverStatPill(
    label: String,
    value: String,
    accentColor: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = accentColor, fontSize = 15.sp, fontWeight = FontWeight.Black)
        Text(label, color = TextMutedDark, fontSize = 10.sp)
    }
}

@Composable
private fun PrefSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
            Text(title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = TextSecondaryDark, fontSize = 11.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color(0xFF003829),
                checkedTrackColor = AberMint,
                uncheckedTrackColor = Color.White.copy(alpha = 0.1f)
            )
        )
    }
}

@Composable
private fun DocStatusRow(
    docName: String,
    reference: String,
    status: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF1C2636))
            .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(docName, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(reference, color = TextSecondaryDark, fontSize = 11.sp)
        }
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = AberMint.copy(alpha = 0.15f)
        ) {
            Text(
                text = status,
                color = AberMint,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
    }
}

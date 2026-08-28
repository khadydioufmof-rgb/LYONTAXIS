package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import kotlinx.coroutines.launch
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DriverProfile
import com.example.R
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverAuthScreen(
    onLoginSuccess: suspend (email: String, password: String, name: String, phone: String, plate: String, model: String, vtcNumber: String) -> Result<Unit>,
    modifier: Modifier = Modifier
) {
    var authMode by remember { mutableStateOf(0) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var fullName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var vtcLicense by remember { mutableStateOf("") }
    var vehicleModel by remember { mutableStateOf("") }
    var vehiclePlate by remember { mutableStateOf("") }
    var agreedToTerms by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        containerColor = AberLightBg,
        modifier = modifier.testTag("driver_auth_screen")
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 32.dp, bottom = 48.dp)
        ) {
            // BRAND LOGO & HEADER
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(92.dp)
                            .clip(RoundedCornerShape(24.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.foundation.Image(
                            painter = painterResource(R.drawable.lyontaxis_logo),
                            contentDescription = "Logo LyonTaxis Pro",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Text(
                        text = "LYON TAXIS PRO",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPrimaryLight,
                        letterSpacing = 1.sp
                    )

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = AberGold.copy(alpha = 0.16f),
                        border = BorderStroke(1.dp, AberGold.copy(alpha = 0.45f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = null,
                                tint = AberGold,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Portail Officiel des Chauffeurs Partenaires",
                                color = AberGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // TAB SELECTOR (CONNEXION / INSCRIPTION)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, AberLightBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp)
                    ) {
                        Surface(
                            onClick = {
                                authMode = 0
                                errorMessage = null
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = if (authMode == 0) AberMintDark else Color.Transparent,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("auth_tab_login")
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Identification",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = if (authMode == 0) Color.White else TextSecondaryLight
                                )
                            }
                        }

                        Surface(
                            onClick = {
                                authMode = 1
                                errorMessage = null
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = if (authMode == 1) AberMintDark else Color.Transparent,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("auth_tab_register")
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Inscription",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = if (authMode == 1) Color.White else TextSecondaryLight
                                )
                            }
                        }
                    }
                }
            }

            // ERROR MESSAGE IF ANY
            if (errorMessage != null) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = AberRed.copy(alpha = 0.1f),
                        border = BorderStroke(1.dp, AberRed.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = AberRed,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = errorMessage ?: "",
                                color = AberRed,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // FORM FIELDS CARD
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, AberLightBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = if (authMode == 0) "Accédez à votre espace de conduite" else "Créez votre compte chauffeur partenaire",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryLight
                        )

                        if (authMode == 1) {
                            // Full Name
                            OutlinedTextField(
                                value = fullName,
                                onValueChange = { fullName = it },
                                label = { Text("Nom et Prénom") },
                                leadingIcon = {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = AberMintDark)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("auth_fullname_input"),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )

                            // Phone
                            OutlinedTextField(
                                value = phone,
                                onValueChange = { phone = it },
                                label = { Text("Téléphone mobile") },
                                leadingIcon = {
                                    Icon(Icons.Default.Phone, contentDescription = null, tint = AberMintDark)
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("auth_phone_input"),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        // Email
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Adresse email professionnelle") },
                            leadingIcon = {
                                Icon(Icons.Default.Email, contentDescription = null, tint = AberMintDark)
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_email_input"),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        // Password
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Mot de passe") },
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = AberMintDark)
                            },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "Afficher mot de passe"
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_password_input"),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        if (authMode == 1) {
                            Divider(modifier = Modifier.padding(vertical = 4.dp), color = AberLightBorder)

                            Text(
                                text = "Informations VTC & Véhicule",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = AberMintDark
                            )

                            // VTC Card Number
                            OutlinedTextField(
                                value = vtcLicense,
                                onValueChange = { vtcLicense = it },
                                label = { Text("Numéro Carte VTC (ex: EVTC-075...)") },
                                leadingIcon = {
                                    Icon(Icons.Default.Badge, contentDescription = null, tint = AberMintDark)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("auth_vtc_input"),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )

                            // Vehicle Model
                            OutlinedTextField(
                                value = vehicleModel,
                                onValueChange = { vehicleModel = it },
                                label = { Text("Modèle du véhicule (ex: Mercedes Classe E)") },
                                leadingIcon = {
                                    Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = AberMintDark)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("auth_model_input"),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )

                            // Vehicle Plate
                            OutlinedTextField(
                                value = vehiclePlate,
                                onValueChange = { vehiclePlate = it.uppercase() },
                                label = { Text("Immatriculation (ex: GC-742-LK)") },
                                leadingIcon = {
                                    Icon(Icons.Default.Pin, contentDescription = null, tint = AberMintDark)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("auth_plate_input"),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = agreedToTerms,
                                    onCheckedChange = { agreedToTerms = it },
                                    colors = CheckboxDefaults.colors(checkedColor = AberMintDark)
                                )
                                Text(
                                    text = "J'atteste disposer d'une carte VTC valide, d'une assurance RC Pro et d'un véhicule conforme.",
                                    fontSize = 11.sp,
                                    color = TextSecondaryLight,
                                    lineHeight = 14.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // SUBMIT BUTTON
                        Button(
                            onClick = {
                                if (email.isBlank() || password.isBlank()) {
                                    errorMessage = "Veuillez renseigner votre email et mot de passe."
                                    return@Button
                                }
                                if (authMode == 1 && (!agreedToTerms || fullName.isBlank() || vehiclePlate.isBlank())) {
                                    errorMessage = "Veuillez remplir toutes les informations obligatoires et accepter les conditions."
                                    return@Button
                                }

                                errorMessage = null
                                isLoading = true
                                coroutineScope.launch {
                                    val result = onLoginSuccess(
                                        email,
                                        password,
                                        if (authMode == 1) fullName else "Chauffeur LyonTaxis",
                                        if (authMode == 1) phone else "",
                                        if (authMode == 1) vehiclePlate else "",
                                        if (authMode == 1) vehicleModel else "",
                                        if (authMode == 1) vtcLicense else ""
                                    )
                                    isLoading = false
                                    result.exceptionOrNull()?.let { errorMessage = it.message ?: "Connexion Supabase impossible" }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("auth_submit_button"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AberMintDark,
                                contentColor = Color.White
                            ),
                            enabled = !isLoading,
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = if (isLoading) "Connexion en cours..." else if (authMode == 0) "Se connecter & Démarrer" else "Finaliser l'inscription chauffeur",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowForward,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SecurityBadge(Icons.Default.Security, "Paiements sécurisés")
                    SecurityBadge(Icons.Default.GpsFixed, "GPS actif")
                    SecurityBadge(Icons.Default.SupportAgent, "Assistance 24/7")
                }
            }
        }
    }
}

@Composable
private fun SecurityBadge(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = TextSecondaryLight,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = label,
            fontSize = 10.sp,
            color = TextSecondaryLight,
            fontWeight = FontWeight.Medium
        )
    }
}

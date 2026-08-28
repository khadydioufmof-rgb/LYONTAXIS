package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PaymentMethodItem
import com.example.model.PaymentType
import com.example.model.UserProfile
import com.example.ui.components.AberHeader
import com.example.ui.components.AberPrimaryButton
import com.example.ui.components.AberSecondaryButton
import com.example.ui.theme.*

@Composable
fun MyAccountScreen(
  userProfile: UserProfile,
  paymentMethods: List<PaymentMethodItem> = emptyList(),
  onUpdateProfile: (name: String, email: String, phone: String, gender: String, birthday: String, emergencyContact: String, homeAddress: String, avatarSeed: String) -> Unit,
  onSelectPaymentMethod: (String) -> Unit = {},
  onSetDefaultPaymentMethod: (String) -> Unit = {},
  onDeletePaymentMethod: (String) -> Unit = {},
  onAddPaymentMethod: (PaymentType, String, String, Boolean) -> Unit = { _, _, _, _ -> },
  onBackClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  // Navigation tabs within profile: 0 = Contact & Info, 1 = Moyens de Paiement, 2 = Sécurité & Adresses
  var selectedTab by remember { mutableIntStateOf(0) }

  // Dialog States
  var showEditContactDialog by remember { mutableStateOf(false) }
  var showAddPaymentDialog by remember { mutableStateOf(false) }
  var showAvatarPicker by remember { mutableStateOf(false) }
  var paymentToDelete by remember { mutableStateOf<PaymentMethodItem?>(null) }
  var showSuccessToast by remember { mutableStateOf(false) }
  var successMessage by remember { mutableStateOf("") }

  // Editable Form States
  var editName by remember(userProfile.name) { mutableStateOf(userProfile.name) }
  var editEmail by remember(userProfile.email) { mutableStateOf(userProfile.email) }
  var editPhone by remember(userProfile.phoneNumber) { mutableStateOf(userProfile.phoneNumber) }
  var editGender by remember(userProfile.gender) { mutableStateOf(userProfile.gender) }
  var editBirthday by remember(userProfile.birthday) { mutableStateOf(userProfile.birthday) }
  var editEmergencyContact by remember(userProfile.emergencyContact) { mutableStateOf(userProfile.emergencyContact) }
  var editHomeAddress by remember(userProfile.homeAddress) { mutableStateOf(userProfile.homeAddress) }
  var currentAvatarSeed by remember(userProfile.avatarSeed) { mutableStateOf(userProfile.avatarSeed) }

  val defaultPayment = remember(paymentMethods) {
    paymentMethods.firstOrNull { it.isDefault } ?: paymentMethods.firstOrNull { it.isSelected } ?: paymentMethods.firstOrNull()
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(AberBackground)
      .verticalScroll(rememberScrollState())
  ) {
    // Header with LyonTaxis styling & Passenger Avatar
    AberHeader(
      title = "Profil & Paramètres",
      subtitle = "Gérez vos coordonnées et paiements",
      onBackClick = onBackClick
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Box {
          Surface(
            shape = CircleShape,
            color = Color.White,
            border = BorderStroke(3.5.dp, Color.White),
            shadowElevation = 8.dp,
            modifier = Modifier
              .size(92.dp)
              .clickable { showAvatarPicker = true }
              .testTag("profile_avatar_image")
          ) {
            Box(contentAlignment = Alignment.Center) {
              val avatarIcon = when (currentAvatarSeed) {
                "Emma" -> Icons.Default.Face3
                "Alex" -> Icons.Default.Face4
                "Julie" -> Icons.Default.Face6
                "Lucas" -> Icons.Default.Face2
                "Marc" -> Icons.Default.Face5
                else -> Icons.Default.Person
              }
              Icon(
                imageVector = avatarIcon,
                contentDescription = userProfile.name,
                tint = LyonBluePrimary,
                modifier = Modifier.size(56.dp)
              )
            }
          }

          // Camera Badge
          Surface(
            shape = CircleShape,
            color = LyonBlueDark,
            shadowElevation = 4.dp,
            modifier = Modifier
              .align(Alignment.BottomEnd)
              .size(30.dp)
              .clickable { showAvatarPicker = true }
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = "Changer d'avatar",
                tint = Color.White,
                modifier = Modifier.size(16.dp)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
          text = userProfile.name,
          fontSize = 20.sp,
          fontWeight = FontWeight.Bold,
          color = Color.White
        )

        Spacer(modifier = Modifier.height(4.dp))

        // VIP Member Tier Pill
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = LyonYellow.copy(alpha = 0.25f),
          border = BorderStroke(1.dp, LyonYellow)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.Star,
              contentDescription = null,
              tint = LyonYellow,
              modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = userProfile.memberLevel,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = Color.White
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Quick Stats Row (Trips, Balance, Coupons)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceEvenly
        ) {
          ProfileStatItem(label = "Trajets", value = "42", icon = Icons.Default.DirectionsCar)
          ProfileStatItem(label = "Solde", value = "${userProfile.cashBalance.toInt()} €", icon = Icons.Default.AccountBalanceWallet)
          ProfileStatItem(label = "Coupons", value = "${userProfile.couponsCount}", icon = Icons.Default.ConfirmationNumber)
        }
      }
    }

    // Success Toast feedback
    AnimatedVisibility(visible = showSuccessToast) {
      Surface(
        color = Color(0xFF2E7D32),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 20.dp, vertical = 8.dp)
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
          Spacer(modifier = Modifier.width(10.dp))
          Text(text = successMessage, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Profile Section Segmented Tabs
    Surface(
      shape = RoundedCornerShape(16.dp),
      color = Color.White,
      shadowElevation = 2.dp,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp)
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(4.dp)
      ) {
        ProfileTabButton(
          title = "Coordonnées",
          icon = Icons.Default.Person,
          isSelected = selectedTab == 0,
          onClick = { selectedTab = 0 },
          modifier = Modifier.weight(1f),
          testTag = "profile_tab_contact"
        )
        ProfileTabButton(
          title = "Paiements",
          icon = Icons.Default.CreditCard,
          isSelected = selectedTab == 1,
          onClick = { selectedTab = 1 },
          modifier = Modifier.weight(1f),
          testTag = "profile_tab_payments"
        )
        ProfileTabButton(
          title = "Sécurité",
          icon = Icons.Default.Shield,
          isSelected = selectedTab == 2,
          onClick = { selectedTab = 2 },
          modifier = Modifier.weight(1f),
          testTag = "profile_tab_security"
        )
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // CONTENT BASED ON SELECTED TAB
    when (selectedTab) {
      0 -> {
        // TAB 1: COORDONNÉES & CONTACT INFORMATION
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
        ) {
          // Card Header with Edit Action
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "COORDONNÉES PERSONNELLES",
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = AberGrayText,
              letterSpacing = 0.5.sp
            )

            TextButton(
              onClick = {
                editName = userProfile.name
                editEmail = userProfile.email
                editPhone = userProfile.phoneNumber
                editGender = userProfile.gender
                editBirthday = userProfile.birthday
                editEmergencyContact = userProfile.emergencyContact
                editHomeAddress = userProfile.homeAddress
                showEditContactDialog = true
              },
              modifier = Modifier.testTag("edit_contact_button")
            ) {
              Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = null,
                tint = LyonBluePrimary,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "Modifier",
                color = LyonBluePrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }

          Spacer(modifier = Modifier.height(6.dp))

          Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            shadowElevation = 2.dp,
            border = BorderStroke(1.dp, AberBorder),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column {
              ProfileDetailRow(
                icon = Icons.Outlined.Person,
                label = "Nom complet",
                value = userProfile.name,
                onClick = { showEditContactDialog = true }
              )
              HorizontalDivider(color = AberBorder, modifier = Modifier.padding(horizontal = 16.dp))

              ProfileDetailRow(
                icon = Icons.Outlined.Email,
                label = "Adresse e-mail",
                value = userProfile.email,
                badge = "Vérifié",
                onClick = { showEditContactDialog = true }
              )
              HorizontalDivider(color = AberBorder, modifier = Modifier.padding(horizontal = 16.dp))

              ProfileDetailRow(
                icon = Icons.Outlined.Phone,
                label = "Numéro mobile",
                value = userProfile.phoneNumber,
                badge = "SMS actif",
                onClick = { showEditContactDialog = true }
              )
              HorizontalDivider(color = AberBorder, modifier = Modifier.padding(horizontal = 16.dp))

              ProfileDetailRow(
                icon = Icons.Outlined.Home,
                label = "Adresse habituelle",
                value = userProfile.homeAddress,
                onClick = { showEditContactDialog = true }
              )
              HorizontalDivider(color = AberBorder, modifier = Modifier.padding(horizontal = 16.dp))

              ProfileDetailRow(
                icon = Icons.Outlined.Wc,
                label = "Genre & Naissance",
                value = "${userProfile.gender} • ${userProfile.birthday}",
                onClick = { showEditContactDialog = true }
              )
            }
          }

          Spacer(modifier = Modifier.height(20.dp))

          // Primary Quick Edit Action Button
          AberPrimaryButton(
            text = "Modifier mes informations",
            icon = Icons.Default.Edit,
            onClick = {
              editName = userProfile.name
              editEmail = userProfile.email
              editPhone = userProfile.phoneNumber
              editGender = userProfile.gender
              editBirthday = userProfile.birthday
              editEmergencyContact = userProfile.emergencyContact
              editHomeAddress = userProfile.homeAddress
              showEditContactDialog = true
            },
            testTag = "open_edit_profile_button"
          )
        }
      }

      1 -> {
        // TAB 2: GESTION DES MOYENS DE PAIEMENT
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
        ) {
          // Default Payment Banner
          if (defaultPayment != null) {
            Text(
              text = "MODE DE PAIEMENT PAR DÉFAUT",
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = AberGrayText,
              letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
              shape = RoundedCornerShape(18.dp),
              color = LyonBlueDark,
              shadowElevation = 4.dp,
              modifier = Modifier.fillMaxWidth()
            ) {
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .background(
                    Brush.horizontalGradient(
                      colors = listOf(LyonBlueDark, LyonBluePrimary)
                    )
                  )
                  .padding(18.dp)
              ) {
                Column {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                      Surface(
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.2f),
                        modifier = Modifier.size(36.dp)
                      ) {
                        Box(contentAlignment = Alignment.Center) {
                          Icon(
                            imageVector = when (defaultPayment.type) {
                              PaymentType.VISA, PaymentType.MASTERCARD -> Icons.Default.CreditCard
                              PaymentType.PAYPAL -> Icons.Default.AccountBalance
                              PaymentType.CASH -> Icons.Default.Euro
                            },
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                          )
                        }
                      }
                      Spacer(modifier = Modifier.width(10.dp))
                      Column {
                        Text(
                          text = defaultPayment.title,
                          color = Color.White,
                          fontSize = 15.sp,
                          fontWeight = FontWeight.Bold
                        )
                        Text(
                          text = defaultPayment.subtitle,
                          color = Color.White.copy(alpha = 0.8f),
                          fontSize = 13.sp
                        )
                      }
                    }

                    Surface(
                      shape = RoundedCornerShape(10.dp),
                      color = LyonYellow,
                      modifier = Modifier.padding(4.dp)
                    ) {
                      Text(
                        text = "PAR DÉFAUT",
                        color = AberDark,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                      )
                    }
                  }

                  Spacer(modifier = Modifier.height(14.dp))

                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Text(
                      text = "Utilisé automatiquement pour vos courses LyonTaxis",
                      color = Color.White.copy(alpha = 0.85f),
                      fontSize = 11.sp
                    )
                    Icon(
                      imageVector = Icons.Default.CheckCircle,
                      contentDescription = null,
                      tint = Color.White,
                      modifier = Modifier.size(18.dp)
                    )
                  }
                }
              }
            }

            Spacer(modifier = Modifier.height(20.dp))
          }

          // List of all Registered Payment Methods
          Text(
            text = "TOUS VOS MOYENS DE PAIEMENT",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = AberGrayText,
            letterSpacing = 0.5.sp
          )

          Spacer(modifier = Modifier.height(8.dp))

          paymentMethods.forEach { method ->
            PaymentMethodCard(
              item = method,
              isDefault = method.isDefault || method.id == defaultPayment?.id,
              onSelectAsDefault = {
                onSetDefaultPaymentMethod(method.id)
                successMessage = "${method.title} défini comme moyen de paiement par défaut"
                showSuccessToast = true
              },
              onDelete = {
                paymentToDelete = method
              }
            )
            Spacer(modifier = Modifier.height(10.dp))
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Add New Payment Method CTA
          AberPrimaryButton(
            text = "Ajouter un moyen de paiement",
            icon = Icons.Default.AddCard,
            onClick = { showAddPaymentDialog = true },
            testTag = "add_payment_method_button"
          )

          Spacer(modifier = Modifier.height(12.dp))

          // Security Trust Guarantee
          Surface(
            shape = RoundedCornerShape(14.dp),
            color = LyonBlueLight,
            border = BorderStroke(1.dp, LyonBluePrimary.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier.padding(14.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = LyonBluePrimary,
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.width(10.dp))
              Text(
                text = "Paiements 100% sécurisés. Chiffrement bancaire 3D-Secure certifié pour la métropole de Lyon.",
                color = LyonBlueDark,
                fontSize = 12.sp,
                lineHeight = 16.sp
              )
            }
          }
        }
      }

      2 -> {
        // TAB 3: SÉCURITÉ & CONTACT D'URGENCE
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
        ) {
          Text(
            text = "SÉCURITÉ DU PASSAGER",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = AberGrayText,
            letterSpacing = 0.5.sp
          )

          Spacer(modifier = Modifier.height(8.dp))

          Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            shadowElevation = 2.dp,
            border = BorderStroke(1.dp, AberBorder),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(16.dp)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                  shape = CircleShape,
                  color = AberRedLight,
                  modifier = Modifier.size(40.dp)
                ) {
                  Box(contentAlignment = Alignment.Center) {
                    Icon(
                      imageVector = Icons.Default.Emergency,
                      contentDescription = null,
                      tint = AberRed,
                      modifier = Modifier.size(22.dp)
                    )
                  }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                  Text(
                    text = "Contact d'Urgence",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = AberDark
                  )
                  Text(
                    text = userProfile.emergencyContact,
                    fontSize = 13.sp,
                    color = AberGrayText
                  )
                }
              }

              Spacer(modifier = Modifier.height(14.dp))
              Text(
                text = "Ce contact reçoit automatiquement un SMS avec le lien de suivi en direct lors de l'activation du bouton SOS.",
                fontSize = 12.sp,
                color = AberGrayText,
                lineHeight = 16.sp
              )

              Spacer(modifier = Modifier.height(12.dp))

              AberSecondaryButton(
                text = "Modifier le contact d'urgence",
                icon = Icons.Default.Edit,
                onClick = {
                  editEmergencyContact = userProfile.emergencyContact
                  showEditContactDialog = true
                },
                testTag = "edit_emergency_button"
              )
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          // Trust and safety features
          Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            shadowElevation = 2.dp,
            border = BorderStroke(1.dp, AberBorder),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(16.dp)) {
              Text(
                text = "Garanties Chauffeurs LyonTaxis",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = AberDark
              )
              Spacer(modifier = Modifier.height(8.dp))
              SecurityFeatureItem(text = "Chauffeurs titulaires de la carte professionnelle de taxi Lyon")
              SecurityFeatureItem(text = "Véhicules contrôlés et désinfectés régulièrement")
              SecurityFeatureItem(text = "Assurance passager incluse sur tous les trajets")
            }
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(30.dp))
  }

  // -------------------------------------------------------------
  // MODALS & DIALOGS
  // -------------------------------------------------------------

  // 1. EDIT CONTACT INFORMATION DIALOG
  if (showEditContactDialog) {
    AlertDialog(
      onDismissRequest = { showEditContactDialog = false },
      title = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Edit, contentDescription = null, tint = LyonBluePrimary)
          Spacer(modifier = Modifier.width(8.dp))
          Text("Modifier mes coordonnées", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }
      },
      text = {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          OutlinedTextField(
            value = editName,
            onValueChange = { editName = it },
            label = { Text("Nom complet") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = LyonBluePrimary) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("edit_input_name"),
            shape = RoundedCornerShape(12.dp)
          )

          OutlinedTextField(
            value = editEmail,
            onValueChange = { editEmail = it },
            label = { Text("Adresse e-mail") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = LyonBluePrimary) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth().testTag("edit_input_email"),
            shape = RoundedCornerShape(12.dp)
          )

          OutlinedTextField(
            value = editPhone,
            onValueChange = { editPhone = it },
            label = { Text("Numéro de mobile") },
            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = LyonBluePrimary) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth().testTag("edit_input_phone"),
            shape = RoundedCornerShape(12.dp)
          )

          OutlinedTextField(
            value = editHomeAddress,
            onValueChange = { editHomeAddress = it },
            label = { Text("Adresse habituelle (Lyon)") },
            leadingIcon = { Icon(Icons.Default.Home, contentDescription = null, tint = LyonBluePrimary) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("edit_input_address"),
            shape = RoundedCornerShape(12.dp)
          )

          OutlinedTextField(
            value = editEmergencyContact,
            onValueChange = { editEmergencyContact = it },
            label = { Text("Contact d'urgence (Nom & Tél)") },
            leadingIcon = { Icon(Icons.Default.Emergency, contentDescription = null, tint = AberRed) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("edit_input_emergency"),
            shape = RoundedCornerShape(12.dp)
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            OutlinedTextField(
              value = editGender,
              onValueChange = { editGender = it },
              label = { Text("Genre") },
              singleLine = true,
              modifier = Modifier.weight(1f),
              shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
              value = editBirthday,
              onValueChange = { editBirthday = it },
              label = { Text("Naissance") },
              singleLine = true,
              modifier = Modifier.weight(1.3f),
              shape = RoundedCornerShape(12.dp)
            )
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (editName.isNotBlank() && editEmail.isNotBlank()) {
              onUpdateProfile(
                editName,
                editEmail,
                editPhone,
                editGender,
                editBirthday,
                editEmergencyContact,
                editHomeAddress,
                currentAvatarSeed
              )
              showEditContactDialog = false
              successMessage = "Coordonnées mises à jour avec succès !"
              showSuccessToast = true
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = LyonBluePrimary),
          modifier = Modifier.testTag("save_profile_button")
        ) {
          Text("Enregistrer", color = Color.White, fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(onClick = { showEditContactDialog = false }) {
          Text("Annuler", color = AberGrayText)
        }
      }
    )
  }

  // 2. ADD PAYMENT METHOD DIALOG
  if (showAddPaymentDialog) {
    var newCardType by remember { mutableStateOf(PaymentType.VISA) }
    var newCardNumber by remember { mutableStateOf("") }
    var newCardHolder by remember { mutableStateOf(userProfile.name) }
    var newCardExpiry by remember { mutableStateOf("12/28") }
    var newCardCvv by remember { mutableStateOf("882") }
    var setAsDefault by remember { mutableStateOf(true) }
    var cardError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
      onDismissRequest = { showAddPaymentDialog = false },
      title = {
        Text("Ajouter un moyen de paiement", fontWeight = FontWeight.Bold, fontSize = 18.sp)
      },
      text = {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          // Payment type selector
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            PaymentTypeSelectorChip(
              title = "Carte Visa",
              icon = Icons.Default.CreditCard,
              isSelected = newCardType == PaymentType.VISA,
              onClick = { newCardType = PaymentType.VISA },
              modifier = Modifier.weight(1f)
            )
            PaymentTypeSelectorChip(
              title = "Mastercard",
              icon = Icons.Default.CreditCard,
              isSelected = newCardType == PaymentType.MASTERCARD,
              onClick = { newCardType = PaymentType.MASTERCARD },
              modifier = Modifier.weight(1f)
            )
            PaymentTypeSelectorChip(
              title = "PayPal",
              icon = Icons.Default.AccountBalance,
              isSelected = newCardType == PaymentType.PAYPAL,
              onClick = { newCardType = PaymentType.PAYPAL },
              modifier = Modifier.weight(1f)
            )
          }

          // Live Card Visual Preview
          Surface(
            shape = RoundedCornerShape(16.dp),
            color = if (newCardType == PaymentType.MASTERCARD) Color(0xFF1E293B) else LyonBlueDark,
            shadowElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(
              modifier = Modifier
                .background(
                  Brush.linearGradient(
                    colors = if (newCardType == PaymentType.MASTERCARD) {
                      listOf(Color(0xFF1E293B), Color(0xFF0F172A))
                    } else {
                      listOf(LyonBlueDark, LyonBluePrimary)
                    }
                  )
                )
                .padding(16.dp)
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = when (newCardType) {
                    PaymentType.VISA -> "VISA LYON"
                    PaymentType.MASTERCARD -> "MASTERCARD"
                    PaymentType.PAYPAL -> "PAYPAL"
                    PaymentType.CASH -> "ESPÈCES"
                  },
                  color = Color.White,
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                  letterSpacing = 1.sp
                )
                Icon(
                  imageVector = Icons.Default.Contactless,
                  contentDescription = null,
                  tint = Color.White.copy(alpha = 0.8f),
                  modifier = Modifier.size(22.dp)
                )
              }

              Spacer(modifier = Modifier.height(18.dp))

              Text(
                text = if (newCardNumber.isBlank()) "**** **** **** 9012" else formatCardNumber(newCardNumber),
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
              )

              Spacer(modifier = Modifier.height(14.dp))

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Column {
                  Text(text = "TITULAIRE", color = Color.White.copy(alpha = 0.7f), fontSize = 9.sp)
                  Text(text = newCardHolder.uppercase(), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Column(horizontalAlignment = Alignment.End) {
                  Text(text = "EXPIRE FIN", color = Color.White.copy(alpha = 0.7f), fontSize = 9.sp)
                  Text(text = newCardExpiry, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
              }
            }
          }

          OutlinedTextField(
            value = newCardNumber,
            onValueChange = {
              if (it.length <= 19) {
                newCardNumber = it.filter { ch -> ch.isDigit() }
                cardError = null
              }
            },
            label = { Text("Numéro de carte (16 chiffres)") },
            placeholder = { Text("4970 1234 5678 9012") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("add_card_number_input"),
            shape = RoundedCornerShape(12.dp)
          )

          OutlinedTextField(
            value = newCardHolder,
            onValueChange = { newCardHolder = it },
            label = { Text("Nom sur la carte") },
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            OutlinedTextField(
              value = newCardExpiry,
              onValueChange = { if (it.length <= 5) newCardExpiry = it },
              label = { Text("Expiration (MM/AA)") },
              placeholder = { Text("08/29") },
              singleLine = true,
              modifier = Modifier.weight(1f),
              shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
              value = newCardCvv,
              onValueChange = { if (it.length <= 4) newCardCvv = it.filter { ch -> ch.isDigit() } },
              label = { Text("CVV / CVC") },
              placeholder = { Text("123") },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
              singleLine = true,
              modifier = Modifier.weight(1f),
              shape = RoundedCornerShape(12.dp)
            )
          }

          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { setAsDefault = !setAsDefault }
              .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Checkbox(
              checked = setAsDefault,
              onCheckedChange = { setAsDefault = it },
              colors = CheckboxDefaults.colors(checkedColor = LyonBluePrimary)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Définir comme moyen de paiement par défaut",
              fontSize = 13.sp,
              color = AberDark
            )
          }

          if (cardError != null) {
            Text(text = cardError!!, color = AberRed, fontSize = 12.sp)
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (newCardNumber.length < 4) {
              cardError = "Veuillez entrer un numéro de carte valide."
            } else {
              val title = when (newCardType) {
                PaymentType.VISA -> "Carte Bancaire Visa"
                PaymentType.MASTERCARD -> "Mastercard Lyon"
                PaymentType.PAYPAL -> "Compte PayPal"
                PaymentType.CASH -> "Espèces LyonTaxis"
              }
              val last4 = if (newCardNumber.length >= 4) newCardNumber.takeLast(4) else "4589"
              val subtitle = "**** **** **** $last4"

              onAddPaymentMethod(newCardType, title, subtitle, setAsDefault)
              showAddPaymentDialog = false
              successMessage = "$title ajouté avec succès !"
              showSuccessToast = true
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = LyonBluePrimary),
          modifier = Modifier.testTag("confirm_add_card_button")
        ) {
          Text("Ajouter", color = Color.White, fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(onClick = { showAddPaymentDialog = false }) {
          Text("Annuler", color = AberGrayText)
        }
      }
    )
  }

  // 3. DELETE PAYMENT CONFIRMATION DIALOG
  paymentToDelete?.let { method ->
    AlertDialog(
      onDismissRequest = { paymentToDelete = null },
      title = { Text("Supprimer ce moyen de paiement ?", fontWeight = FontWeight.Bold) },
      text = {
        Text("Êtes-vous sûr de vouloir supprimer ${method.title} (${method.subtitle}) de votre compte ?")
      },
      confirmButton = {
        Button(
          onClick = {
            onDeletePaymentMethod(method.id)
            paymentToDelete = null
            successMessage = "Moyen de paiement supprimé"
            showSuccessToast = true
          },
          colors = ButtonDefaults.buttonColors(containerColor = AberRed)
        ) {
          Text("Supprimer", color = Color.White)
        }
      },
      dismissButton = {
        TextButton(onClick = { paymentToDelete = null }) {
          Text("Conserver", color = AberGrayText)
        }
      }
    )
  }

  // 4. AVATAR PICKER DIALOG
  if (showAvatarPicker) {
    val avatarOptions = listOf(
      "Thomas" to Icons.Default.Person,
      "Emma" to Icons.Default.Face3,
      "Alex" to Icons.Default.Face4,
      "Julie" to Icons.Default.Face6,
      "Lucas" to Icons.Default.Face2,
      "Marc" to Icons.Default.Face5
    )

    AlertDialog(
      onDismissRequest = { showAvatarPicker = false },
      title = { Text("Choisir un avatar passager", fontWeight = FontWeight.Bold) },
      text = {
        Column {
          Text(
            text = "Sélectionnez votre style de photo de profil pour votre compte LyonTaxis :",
            fontSize = 13.sp,
            color = AberGrayText
          )
          Spacer(modifier = Modifier.height(16.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
          ) {
            avatarOptions.take(3).forEach { (seed, icon) ->
              AvatarOptionItem(
                name = seed,
                icon = icon,
                isSelected = currentAvatarSeed == seed,
                onSelect = {
                  currentAvatarSeed = seed
                  onUpdateProfile(
                    userProfile.name,
                    userProfile.email,
                    userProfile.phoneNumber,
                    userProfile.gender,
                    userProfile.birthday,
                    userProfile.emergencyContact,
                    userProfile.homeAddress,
                    seed
                  )
                  showAvatarPicker = false
                  successMessage = "Photo de profil mise à jour !"
                  showSuccessToast = true
                }
              )
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
          ) {
            avatarOptions.drop(3).forEach { (seed, icon) ->
              AvatarOptionItem(
                name = seed,
                icon = icon,
                isSelected = currentAvatarSeed == seed,
                onSelect = {
                  currentAvatarSeed = seed
                  onUpdateProfile(
                    userProfile.name,
                    userProfile.email,
                    userProfile.phoneNumber,
                    userProfile.gender,
                    userProfile.birthday,
                    userProfile.emergencyContact,
                    userProfile.homeAddress,
                    seed
                  )
                  showAvatarPicker = false
                  successMessage = "Photo de profil mise à jour !"
                  showSuccessToast = true
                }
              )
            }
          }
        }
      },
      confirmButton = {
        TextButton(onClick = { showAvatarPicker = false }) {
          Text("Fermer", color = LyonBluePrimary)
        }
      }
    )
  }
}

// -------------------------------------------------------------
// SUB-COMPONENTS
// -------------------------------------------------------------

@Composable
private fun ProfileStatItem(label: String, value: String, icon: ImageVector) {
  Surface(
    shape = RoundedCornerShape(14.dp),
    color = Color.White.copy(alpha = 0.15f),
    modifier = Modifier.padding(4.dp)
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
      Spacer(modifier = Modifier.width(6.dp))
      Column {
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Text(text = label, fontSize = 10.sp, color = Color.White.copy(alpha = 0.8f))
      }
    }
  }
}

@Composable
private fun ProfileTabButton(
  title: String,
  icon: ImageVector,
  isSelected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  testTag: String = ""
) {
  Surface(
    shape = RoundedCornerShape(12.dp),
    color = if (isSelected) LyonBluePrimary else Color.Transparent,
    modifier = modifier
      .clickable { onClick() }
      .testTag(testTag)
  ) {
    Row(
      modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
      horizontalArrangement = Arrangement.Center,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = if (isSelected) Color.White else AberGrayText,
        modifier = Modifier.size(16.dp)
      )
      Spacer(modifier = Modifier.width(4.dp))
      Text(
        text = title,
        fontSize = 12.sp,
        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
        color = if (isSelected) Color.White else AberGrayText
      )
    }
  }
}

@Composable
private fun ProfileDetailRow(
  icon: ImageVector,
  label: String,
  value: String,
  badge: String? = null,
  onClick: () -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onClick() }
      .padding(horizontal = 16.dp, vertical = 14.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Surface(
      shape = CircleShape,
      color = LyonBlueLight,
      modifier = Modifier.size(36.dp)
    ) {
      Box(contentAlignment = Alignment.Center) {
        Icon(imageVector = icon, contentDescription = null, tint = LyonBluePrimary, modifier = Modifier.size(18.dp))
      }
    }

    Spacer(modifier = Modifier.width(12.dp))

    Column(modifier = Modifier.weight(1f)) {
      Text(text = label, fontSize = 11.sp, color = AberGrayText, fontWeight = FontWeight.Medium)
      Text(text = value, fontSize = 14.sp, color = AberDark, fontWeight = FontWeight.SemiBold)
    }

    if (badge != null) {
      Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFFE8F5E9)
      ) {
        Text(
          text = badge,
          color = Color(0xFF2E7D32),
          fontSize = 10.sp,
          fontWeight = FontWeight.Bold,
          modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
      }
      Spacer(modifier = Modifier.width(6.dp))
    }

    Icon(
      imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
      contentDescription = "Modifier",
      tint = AberGrayText,
      modifier = Modifier.size(13.dp)
    )
  }
}

@Composable
private fun PaymentMethodCard(
  item: PaymentMethodItem,
  isDefault: Boolean,
  onSelectAsDefault: () -> Unit,
  onDelete: () -> Unit
) {
  val icon = when (item.type) {
    PaymentType.VISA, PaymentType.MASTERCARD -> Icons.Default.CreditCard
    PaymentType.PAYPAL -> Icons.Default.AccountBalance
    PaymentType.CASH -> Icons.Default.Euro
  }

  Surface(
    shape = RoundedCornerShape(16.dp),
    color = Color.White,
    shadowElevation = if (isDefault) 3.dp else 1.dp,
    border = BorderStroke(
      width = if (isDefault) 1.8.dp else 1.dp,
      color = if (isDefault) LyonBluePrimary else AberBorder
    ),
    modifier = Modifier.fillMaxWidth()
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 14.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Surface(
        shape = CircleShape,
        color = if (isDefault) LyonBlueLight else AberGrayLight,
        modifier = Modifier.size(42.dp)
      ) {
        Box(contentAlignment = Alignment.Center) {
          Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isDefault) LyonBluePrimary else AberDark,
            modifier = Modifier.size(22.dp)
          )
        }
      }

      Spacer(modifier = Modifier.width(12.dp))

      Column(modifier = Modifier.weight(1f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = item.title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = AberDark
          )
          if (isDefault) {
            Spacer(modifier = Modifier.width(6.dp))
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = LyonBlueLight
            ) {
              Text(
                text = "Défaut",
                color = LyonBluePrimary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
              )
            }
          }
        }
        Text(
          text = item.subtitle,
          fontSize = 13.sp,
          color = AberGrayText
        )
      }

      // Actions: Set as default or Delete
      if (!isDefault) {
        TextButton(
          onClick = onSelectAsDefault,
          contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
        ) {
          Text("Définir", fontSize = 12.sp, color = LyonBluePrimary, fontWeight = FontWeight.Bold)
        }
      }

      if (item.type != PaymentType.CASH) {
        IconButton(
          onClick = onDelete,
          modifier = Modifier.size(32.dp)
        ) {
          Icon(
            imageVector = Icons.Default.DeleteOutline,
            contentDescription = "Supprimer",
            tint = AberRed,
            modifier = Modifier.size(18.dp)
          )
        }
      }
    }
  }
}

@Composable
private fun PaymentTypeSelectorChip(
  title: String,
  icon: ImageVector,
  isSelected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Surface(
    shape = RoundedCornerShape(12.dp),
    color = if (isSelected) LyonBlueLight else AberGrayLight,
    border = BorderStroke(
      width = if (isSelected) 1.5.dp else 1.dp,
      color = if (isSelected) LyonBluePrimary else Color.Transparent
    ),
    modifier = modifier.clickable { onClick() }
  ) {
    Column(
      modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = if (isSelected) LyonBluePrimary else AberGrayText,
        modifier = Modifier.size(18.dp)
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = title,
        fontSize = 11.sp,
        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
        color = if (isSelected) LyonBluePrimary else AberDark,
        textAlign = TextAlign.Center
      )
    }
  }
}

@Composable
private fun AvatarOptionItem(
  name: String,
  icon: ImageVector,
  isSelected: Boolean,
  onSelect: () -> Unit
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier
      .clickable { onSelect() }
      .padding(6.dp)
  ) {
    Surface(
      shape = CircleShape,
      color = if (isSelected) LyonBlueLight else AberGrayLight,
      border = BorderStroke(
        width = if (isSelected) 2.5.dp else 1.dp,
        color = if (isSelected) LyonBluePrimary else AberBorder
      ),
      modifier = Modifier.size(52.dp)
    ) {
      Box(contentAlignment = Alignment.Center) {
        Icon(
          imageVector = icon,
          contentDescription = name,
          tint = if (isSelected) LyonBluePrimary else AberDark,
          modifier = Modifier.size(32.dp)
        )
      }
    }
    Spacer(modifier = Modifier.height(4.dp))
    Text(
      text = name,
      fontSize = 11.sp,
      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
      color = if (isSelected) LyonBluePrimary else AberDark
    )
  }
}

@Composable
private fun SecurityFeatureItem(text: String) {
  Row(
    modifier = Modifier.padding(vertical = 4.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Icon(
      imageVector = Icons.Default.Check,
      contentDescription = null,
      tint = Color(0xFF2E7D32),
      modifier = Modifier.size(16.dp)
    )
    Spacer(modifier = Modifier.width(8.dp))
    Text(text = text, fontSize = 12.sp, color = AberDark)
  }
}

private fun formatCardNumber(raw: String): String {
  val digits = raw.filter { it.isDigit() }
  return digits.chunked(4).joinToString(" ")
}

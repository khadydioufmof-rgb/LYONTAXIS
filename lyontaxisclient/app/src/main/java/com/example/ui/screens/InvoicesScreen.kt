package com.example.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.TripHistoryItem
import com.example.model.UserProfile
import com.example.ui.components.AberHeader
import com.example.ui.components.AberPrimaryButton
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

data class CompanyBillingInfo(
  val companyName: String = "Tech Lyon Solutions SAS",
  val siret: String = "912 345 678 00021",
  val vatNumber: String = "FR 78 912345678",
  val billingAddress: String = "22 Rue de la Villette, 69003 Lyon",
  val accountingEmail: String = "compta@techlyon.fr",
  val expenseCategory: String = "Déplacements professionnels & Rendez-vous clients"
)

@Composable
fun InvoicesScreen(
  userProfile: UserProfile,
  tripHistory: List<TripHistoryItem>,
  onBackClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()

  var companyInfo by remember { mutableStateOf(CompanyBillingInfo()) }
  var showEditCompanyDialog by remember { mutableStateOf(false) }
  var selectedInvoiceForPreview by remember { mutableStateOf<TripHistoryItem?>(null) }
  var downloadingInvoiceId by remember { mutableStateOf<String?>(null) }
  var isExportingAllReport by remember { mutableStateOf(false) }

  var searchQuery by remember { mutableStateOf("") }
  var selectedFilterTag by remember { mutableStateOf("Toutes") }

  val filterTags = listOf("Toutes", "Terminées", "Aéroport St-Exupéry", "Gares Lyon", "Ce mois-ci")

  // Filter completed and matching trips
  val filteredInvoices = remember(tripHistory, searchQuery, selectedFilterTag) {
    tripHistory.filter { trip ->
      val matchesSearch = searchQuery.isBlank() ||
          trip.pickupTitle.contains(searchQuery, ignoreCase = true) ||
          trip.dropoffTitle.contains(searchQuery, ignoreCase = true) ||
          trip.id.contains(searchQuery, ignoreCase = true) ||
          trip.driverName.contains(searchQuery, ignoreCase = true)

      val matchesTag = when (selectedFilterTag) {
        "Terminées" -> trip.status == "Terminé"
        "Aéroport St-Exupéry" -> trip.pickupTitle.contains("Aéroport", true) || trip.dropoffTitle.contains("Aéroport", true)
        "Gares Lyon" -> trip.pickupTitle.contains("Gare", true) || trip.dropoffTitle.contains("Gare", true)
        "Ce mois-ci" -> trip.date.contains("Oct", true) || trip.date.contains("Août", true)
        else -> true
      }
      matchesSearch && matchesTag
    }
  }

  // Financial aggregates
  val completedTrips = tripHistory.filter { it.status == "Terminé" }
  val totalTtc = completedTrips.sumOf { it.fare }
  val totalHt = completedTrips.sumOf { it.fare / 1.10 }
  val totalVat = totalTtc - totalHt

  fun simulateDownloadPdf(trip: TripHistoryItem) {
    coroutineScope.launch {
      downloadingInvoiceId = trip.id
      delay(900)
      downloadingInvoiceId = null
      val invoiceNum = "FR-LYON-${trip.id.takeLast(6).uppercase()}"
      Toast.makeText(
        context,
        "📄 Facture $invoiceNum.pdf enregistrée dans vos Téléchargements !",
        Toast.LENGTH_LONG
      ).show()
    }
  }

  fun exportAllReport() {
    coroutineScope.launch {
      isExportingAllReport = true
      delay(1200)
      isExportingAllReport = false
      Toast.makeText(
        context,
        "📊 Récapitulatif comptable TVA (Octobre 2024.pdf) généré et envoyé à ${companyInfo.accountingEmail}",
        Toast.LENGTH_LONG
      ).show()
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(AberBackground)
  ) {
    // Header
    AberHeader(
      title = "Factures & Notes de frais",
      subtitle = "Justificatifs officiels avec TVA déductible (10%)",
      onBackClick = onBackClick
    )

    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      // 1. Pro Company Billing Banner
      item {
        Spacer(modifier = Modifier.height(4.dp))
        Surface(
          shape = RoundedCornerShape(18.dp),
          color = Color.White,
          shadowElevation = 2.dp,
          border = BorderStroke(1.dp, AberBorder),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("company_billing_card")
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                  shape = RoundedCornerShape(10.dp),
                  color = LyonBlueLight,
                  modifier = Modifier.size(38.dp)
                ) {
                  Box(contentAlignment = Alignment.Center) {
                    Icon(
                      imageVector = Icons.Default.Business,
                      contentDescription = null,
                      tint = LyonBluePrimary,
                      modifier = Modifier.size(22.dp)
                    )
                  }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                  Text(
                    text = "PROFIL DE FACTURATION PRO",
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = AberGrayText,
                    letterSpacing = 0.5.sp
                  )
                  Text(
                    text = companyInfo.companyName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = AberDark
                  )
                }
              }

              TextButton(
                onClick = { showEditCompanyDialog = true },
                modifier = Modifier.testTag("edit_company_info_button")
              ) {
                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Modifier", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = LyonBluePrimary)
              }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = AberBorder.copy(alpha = 0.6f))
            Spacer(modifier = Modifier.height(8.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Column {
                Text("SIRET : ${companyInfo.siret}", fontSize = 11.sp, color = AberDark)
                Text("N° TVA : ${companyInfo.vatNumber}", fontSize = 11.sp, color = AberGrayText)
              }
              Column(horizontalAlignment = Alignment.End) {
                Text("Bénéficiaire : ${userProfile.name}", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = AberDark)
                Text(companyInfo.accountingEmail, fontSize = 11.sp, color = LyonBluePrimary)
              }
            }
          }
        }
      }

      // 2. Financial Metrics & Monthly Export Summary
      item {
        Surface(
          shape = RoundedCornerShape(18.dp),
          color = LyonBlueDark,
          shadowElevation = 4.dp,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("invoices_financial_summary")
        ) {
          Column(modifier = Modifier.padding(18.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text(
                  text = "TOTAL DÉPENSES PROFESSIONNELLES",
                  fontSize = 10.5.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color.White.copy(alpha = 0.8f),
                  letterSpacing = 0.5.sp
                )
                Text(
                  text = "${"%.2f".format(totalTtc)} € TTC",
                  fontSize = 24.sp,
                  fontWeight = FontWeight.ExtraBold,
                  color = Color.White
                )
              }

              Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White.copy(alpha = 0.15f)
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Icon(Icons.Default.Receipt, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = "${completedTrips.size} factures",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Breakdown Grid (HT + TVA 10%)
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White.copy(alpha = 0.12f),
                modifier = Modifier.weight(1f)
              ) {
                Column(modifier = Modifier.padding(10.dp)) {
                  Text("Total Hors Taxes (HT)", fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f))
                  Text("${"%.2f".format(totalHt)} €", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
              }

              Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White.copy(alpha = 0.12f),
                modifier = Modifier.weight(1f)
              ) {
                Column(modifier = Modifier.padding(10.dp)) {
                  Text("TVA déductible (10%)", fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f))
                  Text("${"%.2f".format(totalVat)} €", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = AberYellow)
                }
              }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Export All Button
            Button(
              onClick = { exportAllReport() },
              enabled = !isExportingAllReport,
              shape = RoundedCornerShape(12.dp),
              colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = LyonBlueDark
              ),
              modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .testTag("export_all_invoices_button")
            ) {
              if (isExportingAllReport) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = LyonBlueDark, strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Génération de l'archive PDF...", fontSize = 13.sp, fontWeight = FontWeight.Bold)
              } else {
                Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Exporter le relevé mensuel complet (PDF)", fontSize = 13.sp, fontWeight = FontWeight.Bold)
              }
            }
          }
        }
      }

      // 3. Search & Quick Filters
      item {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          // Search Field
          OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Rechercher par trajet, chauffeur ou N° facture...", fontSize = 13.sp) },
            leadingIcon = {
              Icon(Icons.Default.Search, contentDescription = null, tint = AberGrayText)
            },
            trailingIcon = {
              if (searchQuery.isNotEmpty()) {
                IconButton(onClick = { searchQuery = "" }) {
                  Icon(Icons.Default.Clear, contentDescription = "Effacer")
                }
              }
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
              unfocusedContainerColor = Color.White,
              focusedContainerColor = Color.White,
              unfocusedBorderColor = AberBorder,
              focusedBorderColor = LyonBluePrimary
            ),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("invoice_search_input")
          )

          // Filter tags
          LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(filterTags) { tag ->
              val isSelected = selectedFilterTag == tag
              FilterChip(
                selected = isSelected,
                onClick = { selectedFilterTag = tag },
                label = { Text(tag, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium) },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = LyonBluePrimary,
                  selectedLabelColor = Color.White,
                  containerColor = Color.White,
                  labelColor = AberDark
                ),
                border = BorderStroke(1.dp, if (isSelected) LyonBluePrimary else AberBorder),
                modifier = Modifier.testTag("filter_chip_$tag")
              )
            }
          }
        }
      }

      // 4. Invoices List
      if (filteredInvoices.isEmpty()) {
        item {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 40.dp),
            contentAlignment = Alignment.Center
          ) {
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Icon(Icons.Outlined.ReceiptLong, contentDescription = null, tint = AberGrayText, modifier = Modifier.size(48.dp))
              Text("Aucune facture correspondante", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = AberDark)
              Text("Essayez d'ajuster vos critères de recherche.", color = AberGrayText, fontSize = 12.5.sp)
            }
          }
        }
      } else {
        items(filteredInvoices, key = { it.id }) { trip ->
          val isDownloading = downloadingInvoiceId == trip.id
          InvoiceCard(
            trip = trip,
            isDownloading = isDownloading,
            onDownloadPdf = { simulateDownloadPdf(trip) },
            onPreviewInvoice = { selectedInvoiceForPreview = trip },
            onShare = {
              val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "Facture LyonTaxis FR-LYON-${trip.id.takeLast(6).uppercase()}")
                putExtra(
                  Intent.EXTRA_TEXT,
                  "Facture de taxi LyonTaxis SAS : Course du ${trip.date} de ${trip.pickupTitle} à ${trip.dropoffTitle} - Montant : ${String.format(Locale.FRANCE, "%.2f", trip.fare)} € TTC (TVA 10% incluse)."
                )
              }
              context.startActivity(Intent.createChooser(shareIntent, "Partager la facture"))
            }
          )
        }
      }

      item {
        Spacer(modifier = Modifier.height(20.dp))
      }
    }
  }

  // Dialog: Edit Company Billing Info
  if (showEditCompanyDialog) {
    EditCompanyDialog(
      currentInfo = companyInfo,
      onSave = { updated ->
        companyInfo = updated
        showEditCompanyDialog = false
        Toast.makeText(context, "Informations de facturation mises à jour", Toast.LENGTH_SHORT).show()
      },
      onDismiss = { showEditCompanyDialog = false }
    )
  }

  // Full Screen / Modal: Official PDF Invoice Viewer
  if (selectedInvoiceForPreview != null) {
    OfficialInvoicePdfDialog(
      trip = selectedInvoiceForPreview!!,
      companyInfo = companyInfo,
      userProfile = userProfile,
      onDownload = {
        simulateDownloadPdf(selectedInvoiceForPreview!!)
      },
      onDismiss = { selectedInvoiceForPreview = null }
    )
  }
}

@Composable
private fun InvoiceCard(
  trip: TripHistoryItem,
  isDownloading: Boolean,
  onDownloadPdf: () -> Unit,
  onPreviewInvoice: () -> Unit,
  onShare: () -> Unit
) {
  val vatRate = 0.10
  val priceHt = trip.fare / (1 + vatRate)
  val vatAmount = trip.fare - priceHt
  val invoiceNumber = "FR-LYON-${trip.id.takeLast(6).uppercase()}"

  Surface(
    shape = RoundedCornerShape(18.dp),
    color = Color.White,
    shadowElevation = 2.dp,
    border = BorderStroke(1.dp, AberBorder),
    modifier = Modifier
      .fillMaxWidth()
      .testTag("invoice_card_${trip.id}")
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      // Top row: Invoice ID + Date + Status
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = AberRedLight,
            modifier = Modifier.size(32.dp)
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                imageVector = Icons.Default.PictureAsPdf,
                contentDescription = null,
                tint = AberRed,
                modifier = Modifier.size(18.dp)
              )
            }
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = invoiceNumber,
              fontSize = 13.5.sp,
              fontWeight = FontWeight.ExtraBold,
              color = AberDark
            )
            Text(
              text = trip.date,
              fontSize = 11.5.sp,
              color = AberGrayText
            )
          }
        }

        Surface(
          shape = RoundedCornerShape(10.dp),
          color = if (trip.status == "Terminé") LyonBlueLight else AberGrayLight
        ) {
          Text(
            text = if (trip.status == "Terminé") "Acquittée ✓" else trip.status,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (trip.status == "Terminé") LyonBluePrimary else AberGrayText,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Route
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .background(AberGrayLight, RoundedCornerShape(12.dp))
          .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Surface(shape = CircleShape, color = LyonBluePrimary, modifier = Modifier.size(8.dp)) {}
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = trip.pickupTitle,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.SemiBold,
            color = AberDark,
            maxLines = 1
          )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.LocationOn, contentDescription = null, tint = AberRed, modifier = Modifier.size(10.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = trip.dropoffTitle,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.Bold,
            color = AberDark,
            maxLines = 1
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Pricing details: HT / TVA / TTC
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
      ) {
        Column {
          Text(
            text = "HT : ${"%.2f".format(priceHt)} € • TVA (10%) : ${"%.2f".format(vatAmount)} €",
            fontSize = 11.5.sp,
            color = AberGrayText
          )
          Text(
            text = "Règlement : ${trip.paymentMethod}",
            fontSize = 11.sp,
            color = AberDark.copy(alpha = 0.8f)
          )
        }

        Text(
          text = "${"%.2f".format(trip.fare)} € TTC",
          fontSize = 17.sp,
          fontWeight = FontWeight.ExtraBold,
          color = LyonBluePrimary
        )
      }

      Spacer(modifier = Modifier.height(14.dp))
      HorizontalDivider(color = AberBorder.copy(alpha = 0.6f))
      Spacer(modifier = Modifier.height(10.dp))

      // Action Buttons
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        // Preview button
        OutlinedButton(
          onClick = onPreviewInvoice,
          shape = RoundedCornerShape(10.dp),
          border = BorderStroke(1.dp, AberBorder),
          contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
          modifier = Modifier
            .weight(1f)
            .height(38.dp)
            .testTag("preview_invoice_${trip.id}")
        ) {
          Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(15.dp), tint = AberDark)
          Spacer(modifier = Modifier.width(6.dp))
          Text("Aperçu", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = AberDark)
        }

        // Share button
        OutlinedButton(
          onClick = onShare,
          shape = RoundedCornerShape(10.dp),
          border = BorderStroke(1.dp, AberBorder),
          contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
          modifier = Modifier
            .height(38.dp)
            .testTag("share_invoice_${trip.id}")
        ) {
          Icon(Icons.Default.Share, contentDescription = "Partager", modifier = Modifier.size(15.dp), tint = AberDark)
        }

        // Download PDF button
        Button(
          onClick = onDownloadPdf,
          enabled = !isDownloading,
          shape = RoundedCornerShape(10.dp),
          colors = ButtonDefaults.buttonColors(containerColor = LyonBluePrimary),
          contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
          modifier = Modifier
            .weight(1.3f)
            .height(38.dp)
            .testTag("download_pdf_${trip.id}")
        ) {
          if (isDownloading) {
            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Export...", fontSize = 12.sp, color = Color.White)
          } else {
            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(15.dp), tint = Color.White)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Télécharger PDF", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
          }
        }
      }
    }
  }
}

@Composable
private fun EditCompanyDialog(
  currentInfo: CompanyBillingInfo,
  onSave: (CompanyBillingInfo) -> Unit,
  onDismiss: () -> Unit
) {
  var name by remember { mutableStateOf(currentInfo.companyName) }
  var siret by remember { mutableStateOf(currentInfo.siret) }
  var vat by remember { mutableStateOf(currentInfo.vatNumber) }
  var address by remember { mutableStateOf(currentInfo.billingAddress) }
  var email by remember { mutableStateOf(currentInfo.accountingEmail) }
  var category by remember { mutableStateOf(currentInfo.expenseCategory) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Business, contentDescription = null, tint = LyonBluePrimary)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Coordonnées de l'entreprise", fontWeight = FontWeight.Bold, fontSize = 16.sp)
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Text(
          "Ces mentions figureront sur toutes les factures officielles et notes de frais générées.",
          fontSize = 11.5.sp,
          color = AberGrayText
        )

        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text("Raison sociale / Nom société", fontSize = 12.sp) },
          singleLine = true,
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
          value = siret,
          onValueChange = { siret = it },
          label = { Text("Numéro SIRET (14 chiffres)", fontSize = 12.sp) },
          singleLine = true,
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
          value = vat,
          onValueChange = { vat = it },
          label = { Text("N° TVA Intracommunautaire", fontSize = 12.sp) },
          singleLine = true,
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
          value = address,
          onValueChange = { address = it },
          label = { Text("Adresse du siège social", fontSize = 12.sp) },
          singleLine = true,
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
          value = email,
          onValueChange = { email = it },
          label = { Text("Email comptabilité (Envoi automatique)", fontSize = 12.sp) },
          singleLine = true,
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
          value = category,
          onValueChange = { category = it },
          label = { Text("Motif / Catégorie de note de frais", fontSize = 12.sp) },
          singleLine = true,
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.fillMaxWidth()
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          onSave(
            CompanyBillingInfo(
              companyName = name.ifBlank { "Entreprise" },
              siret = siret,
              vatNumber = vat,
              billingAddress = address,
              accountingEmail = email,
              expenseCategory = category
            )
          )
        },
        colors = ButtonDefaults.buttonColors(containerColor = LyonBluePrimary),
        shape = RoundedCornerShape(10.dp)
      ) {
        Text("Enregistrer", color = Color.White, fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Annuler", color = AberGrayText)
      }
    }
  )
}

@Composable
private fun OfficialInvoicePdfDialog(
  trip: TripHistoryItem,
  companyInfo: CompanyBillingInfo,
  userProfile: UserProfile,
  onDownload: () -> Unit,
  onDismiss: () -> Unit
) {
  val context = LocalContext.current
  val vatRate = 0.10
  val priceHt = trip.fare / (1 + vatRate)
  val vatAmount = trip.fare - priceHt
  val invoiceNumber = "FR-LYON-${trip.id.takeLast(6).uppercase()}"

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Surface(
      shape = RoundedCornerShape(20.dp),
      color = AberBackground,
      modifier = Modifier
        .fillMaxWidth(0.95f)
        .fillMaxHeight(0.92f)
        .padding(vertical = 12.dp)
        .testTag("official_invoice_pdf_dialog")
    ) {
      Column(modifier = Modifier.fillMaxSize()) {
        // Dialog Top Bar
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 12.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = AberRed, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Column {
              Text("Aperçu de la Facture PDF", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = AberDark)
              Text("Conforme Code Général des Impôts (Art. 279 g)", fontSize = 10.5.sp, color = AberGrayText)
            }
          }

          IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Fermer")
          }
        }

        HorizontalDivider(color = AberBorder)

        // Scrollable PDF Document Canvas (A4 Style White Paper)
        Column(
          modifier = Modifier
            .weight(1f)
            .verticalScroll(rememberScrollState())
            .padding(14.dp)
        ) {
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color.White,
            shadowElevation = 4.dp,
            border = BorderStroke(1.dp, AberBorder),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(18.dp)) {
              // 1. Header: Emitter & Paid Stamp
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
              ) {
                Column(modifier = Modifier.weight(1f)) {
                  Text("LyonTaxis SAS Métropole", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = LyonBlueDark)
                  Text("10 Rue de la République, 69002 Lyon", fontSize = 10.sp, color = AberGrayText)
                  Text("Capital : 500 000 € • RCS Lyon 849 203 112", fontSize = 9.5.sp, color = AberGrayText)
                  Text("SIRET : 849 203 112 00018 • Code NAF : 4932Z", fontSize = 9.5.sp, color = AberGrayText)
                  Text("N° TVA Intracommunautaire : FR 45 849203112", fontSize = 9.5.sp, fontWeight = FontWeight.SemiBold, color = AberDark)
                  Text("Licence Préfectorale Taxi Lyon : ADS-69-0428", fontSize = 9.5.sp, color = LyonBluePrimary)
                }

                // PAID STAMP BADGE
                Surface(
                  shape = RoundedCornerShape(8.dp),
                  color = AberGreenLight,
                  border = BorderStroke(1.5.dp, AberGreen),
                  modifier = Modifier.padding(start = 8.dp)
                ) {
                  Column(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                  ) {
                    Text("ACQUITTÉE", fontSize = 11.sp, fontWeight = FontWeight.Black, color = AberGreen)
                    Text(trip.date, fontSize = 9.sp, color = AberGreen)
                  }
                }
              }

              Spacer(modifier = Modifier.height(14.dp))
              HorizontalDivider(color = AberBorder, thickness = 1.dp)
              Spacer(modifier = Modifier.height(14.dp))

              // 2. Client & Invoice Reference Blocks
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                // Facturé à (Client Pro)
                Column(modifier = Modifier.weight(1f)) {
                  Text("CLIENT / NOTE DE FRAIS :", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = LyonBluePrimary)
                  Text(companyInfo.companyName, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AberDark)
                  Text("À l'attention de : ${userProfile.name}", fontSize = 10.5.sp, color = AberDark)
                  Text(companyInfo.billingAddress, fontSize = 10.sp, color = AberGrayText)
                  Text("SIRET : ${companyInfo.siret}", fontSize = 9.5.sp, color = AberGrayText)
                  Text("Motif : ${companyInfo.expenseCategory}", fontSize = 9.5.sp, color = AberDark)
                }

                // Facture details
                Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(0.9f)) {
                  Text("FACTURE N°", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = LyonBluePrimary)
                  Text(invoiceNumber, fontSize = 13.sp, fontWeight = FontWeight.Black, color = AberDark)
                  Spacer(modifier = Modifier.height(4.dp))
                  Text("Date de prestation : ${trip.date}", fontSize = 10.sp, color = AberDark)
                  Text("Mode de règlement : ${trip.paymentMethod}", fontSize = 10.sp, color = AberDark)
                  Text("Chauffeur agréé : ${trip.driverName}", fontSize = 10.sp, color = AberGrayText)
                }
              }

              Spacer(modifier = Modifier.height(16.dp))

              // 3. Trip & Service Description
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = AberGrayLight,
                modifier = Modifier.fillMaxWidth()
              ) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                  Text("DÉSIGNATION DE LA PRESTATION DE TRANSPORT", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = AberGrayText)
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("• Départ : ", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AberDark)
                    Text(trip.pickupTitle, fontSize = 11.sp, color = AberDark)
                  }
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("• Arrivée : ", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AberDark)
                    Text(trip.dropoffTitle, fontSize = 11.sp, color = AberDark)
                  }
                  Text(
                    "Distance réelle : ${trip.distanceKm} km • Durée : ${trip.durationMin} min • Véhicule : ${trip.vehicleName}",
                    fontSize = 10.sp,
                    color = AberGrayText
                  )
                }
              }

              Spacer(modifier = Modifier.height(14.dp))

              // 4. Line Items Table with VAT Detail
              Column(modifier = Modifier.fillMaxWidth()) {
                // Table Header
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .background(LyonBlueLight, RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Text("Description", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = LyonBlueDark, modifier = Modifier.weight(2f))
                  Text("TVA", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = LyonBlueDark, modifier = Modifier.weight(0.8f), textAlign = TextAlign.Center)
                  Text("Montant HT", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = LyonBlueDark, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Line 1: Course Taxi
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Text("Course de taxi urbain Métropole de Lyon", fontSize = 11.sp, color = AberDark, modifier = Modifier.weight(2f))
                  Text("10%", fontSize = 11.sp, color = AberDark, modifier = Modifier.weight(0.8f), textAlign = TextAlign.Center)
                  Text("${"%.2f".format(priceHt)} €", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = AberDark, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                }

                if (trip.tip > 0) {
                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                  ) {
                    Text("Pourboire chauffeur (Art. 261-7-1° CGI)", fontSize = 11.sp, color = AberDark, modifier = Modifier.weight(2f))
                    Text("0%", fontSize = 11.sp, color = AberDark, modifier = Modifier.weight(0.8f), textAlign = TextAlign.Center)
                    Text("${"%.2f".format(trip.tip)} €", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = AberDark, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                  }
                }

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = AberBorder)
                Spacer(modifier = Modifier.height(8.dp))

                // Totals Table Recap
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                  Column(modifier = Modifier.width(220.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                      Text("Total Net Hors Taxes (HT) :", fontSize = 11.sp, color = AberGrayText)
                      Text("${"%.2f".format(priceHt)} €", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = AberDark)
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                      Text("TVA Transport de pers. (10%) :", fontSize = 11.sp, color = AberGrayText)
                      Text("${"%.2f".format(vatAmount)} €", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AberDark)
                    }

                    if (trip.tip > 0) {
                      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Pourboire (Exonéré TVA) :", fontSize = 11.sp, color = AberGrayText)
                        Text("${"%.2f".format(trip.tip)} €", fontSize = 11.sp, color = AberDark)
                      }
                    }

                    HorizontalDivider(color = AberBorder, modifier = Modifier.padding(vertical = 2.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                      Text("TOTAL NET TTC PAYÉ :", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = LyonBlueDark)
                      Text(
                        "${"%.2f".format(trip.fare)} €",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = LyonBluePrimary
                      )
                    }
                  }
                }
              }

              Spacer(modifier = Modifier.height(16.dp))

              // 5. Legal notice for French tax accounting
              Surface(
                shape = RoundedCornerShape(6.dp),
                color = AberGrayLight,
                modifier = Modifier.fillMaxWidth()
              ) {
                Text(
                  text = "Ce document tient lieu de facture acquittée et de justificatif de note de frais conformément aux dispositions des articles 289 et suivants du Code Général des Impôts. TVA déductible par les assujettis pour le transport de salariés ou dirigeants dans les conditions de l'article 206 de l'annexe II au CGI.",
                  fontSize = 8.5.sp,
                  color = AberGrayText,
                  lineHeight = 11.sp,
                  modifier = Modifier.padding(8.dp)
                )
              }
            }
          }
        }

        // Bottom Dialog Action Buttons
        Surface(
          shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
          color = Color.White,
          shadowElevation = 8.dp,
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            OutlinedButton(
              onClick = {
                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                  type = "text/plain"
                  putExtra(Intent.EXTRA_EMAIL, arrayOf(companyInfo.accountingEmail))
                  putExtra(Intent.EXTRA_SUBJECT, "Facture de taxi LyonTaxis - $invoiceNumber")
                  putExtra(
                    Intent.EXTRA_TEXT,
                    "Veuillez trouver ci-joint le justificatif pour la course de taxi du ${trip.date} d'un montant de ${String.format(Locale.FRANCE, "%.2f", trip.fare)} € TTC (TVA 10% : ${String.format(Locale.FRANCE, "%.2f", vatAmount)} €)."
                  )
                }
                context.startActivity(Intent.createChooser(sendIntent, "Envoyer à la comptabilité"))
              },
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier
                .weight(1f)
                .height(48.dp)
            ) {
              Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Envoyer compta", fontSize = 12.5.sp)
            }

            Button(
              onClick = {
                onDownload()
                onDismiss()
              },
              shape = RoundedCornerShape(12.dp),
              colors = ButtonDefaults.buttonColors(containerColor = LyonBluePrimary),
              modifier = Modifier
                .weight(1.2f)
                .height(48.dp)
                .testTag("dialog_download_pdf_button")
            ) {
              Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Télécharger le PDF", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }
  }
}

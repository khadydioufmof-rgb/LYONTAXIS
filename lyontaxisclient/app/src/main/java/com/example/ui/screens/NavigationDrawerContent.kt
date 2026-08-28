package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.UserProfile
import com.example.ui.theme.*

@Composable
fun NavigationDrawerContent(
  userProfile: UserProfile,
  onNavigate: (String) -> Unit,
  onCloseDrawer: () -> Unit,
  onLogout: () -> Unit,
  modifier: Modifier = Modifier
) {
  ModalDrawerSheet(
    modifier = modifier
      .width(310.dp)
      .fillMaxHeight(),
    drawerContainerColor = Color.White,
    drawerShape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp)
  ) {
    Column(modifier = Modifier.fillMaxSize()) {
      // Top Curved Profile Header with LyonTaxis Logo & Profile
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(bottomEnd = 32.dp))
          .background(
            Brush.verticalGradient(
              colors = listOf(LyonBlueDark, LyonBluePrimary)
            )
          )
          .statusBarsPadding()
          .padding(horizontal = 24.dp, vertical = 20.dp)
      ) {
        Column {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            // Avatar
            Surface(
              shape = CircleShape,
              color = Color.White,
              border = BorderStroke(2.5.dp, Color.White),
              shadowElevation = 6.dp,
              modifier = Modifier
                .size(62.dp)
                .clickable {
                  onNavigate("account")
                  onCloseDrawer()
                }
                .testTag("drawer_profile_avatar")
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(
                  imageVector = Icons.Default.Person,
                  contentDescription = userProfile.name,
                  tint = LyonBluePrimary,
                  modifier = Modifier.size(38.dp)
                )
              }
            }

            // LyonTaxis Logo thumbnail
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = AberDark,
              modifier = Modifier.size(44.dp)
            ) {
              Image(
                painter = painterResource(id = R.drawable.lyontaxis_logo_1787822271774),
                contentDescription = "LyonTaxis",
                modifier = Modifier
                  .fillMaxSize()
                  .clip(RoundedCornerShape(12.dp))
              )
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          Text(
            text = userProfile.name,
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
          )

          Spacer(modifier = Modifier.height(4.dp))

          // Cash Balance Pill
          Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White.copy(alpha = 0.25f),
            modifier = Modifier.clickable {
              onNavigate("wallet")
              onCloseDrawer()
            }
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "Solde: ${userProfile.cashBalance.toInt()} €",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
              Spacer(modifier = Modifier.width(4.dp))
              Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(11.dp)
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Drawer Menu Navigation Items in French
      Column(
        modifier = Modifier
          .weight(1f)
          .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        DrawerMenuItem(
          icon = Icons.Outlined.Home,
          title = "Accueil",
          onClick = {
            onNavigate("home")
            onCloseDrawer()
          },
          tag = "drawer_item_home"
        )

        DrawerMenuItem(
          icon = Icons.Outlined.Person,
          title = "Mon Profil & Paiements",
          onClick = {
            onNavigate("account")
            onCloseDrawer()
          },
          tag = "drawer_item_account"
        )

        DrawerMenuItem(
          icon = Icons.Outlined.AccountBalanceWallet,
          title = "Mon Portefeuille",
          onClick = {
            onNavigate("wallet")
            onCloseDrawer()
          },
          tag = "drawer_item_wallet"
        )

        DrawerMenuItem(
          icon = Icons.Outlined.History,
          title = "Historique des courses",
          onClick = {
            onNavigate("history")
            onCloseDrawer()
          },
          tag = "drawer_item_history"
        )

        DrawerMenuItem(
          icon = Icons.Outlined.ReceiptLong,
          title = "Factures & Notes de frais",
          badgeText = "PDF",
          onClick = {
            onNavigate("invoices")
            onCloseDrawer()
          },
          tag = "drawer_item_invoices"
        )

        DrawerMenuItem(
          icon = Icons.Outlined.Notifications,
          title = "Notifications",
          badgeCount = 2,
          onClick = {
            onNavigate("notifications")
            onCloseDrawer()
          },
          tag = "drawer_item_notifications"
        )

        DrawerMenuItem(
          icon = Icons.Outlined.CardGiftcard,
          title = "Parrainer des amis",
          onClick = {
            onNavigate("invite")
            onCloseDrawer()
          },
          tag = "drawer_item_invite"
        )

        DrawerMenuItem(
          icon = Icons.Outlined.Settings,
          title = "Paramètres",
          onClick = {
            onNavigate("settings")
            onCloseDrawer()
          },
          tag = "drawer_item_settings"
        )
      }

      HorizontalDivider(color = AberBorder, modifier = Modifier.padding(horizontal = 16.dp))

      // Logout
      DrawerMenuItem(
        icon = Icons.AutoMirrored.Filled.Logout,
        title = "Déconnexion",
        onClick = {
          onLogout()
          onCloseDrawer()
        },
        tint = AberRed,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        tag = "drawer_item_logout"
      )

      Spacer(modifier = Modifier.navigationBarsPadding())
    }
  }
}

@Composable
private fun DrawerMenuItem(
  icon: ImageVector,
  title: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  badgeCount: Int? = null,
  badgeText: String? = null,
  tint: Color = AberDark,
  tag: String
) {
  Surface(
    shape = RoundedCornerShape(14.dp),
    color = Color.Transparent,
    modifier = modifier
      .fillMaxWidth()
      .clickable { onClick() }
      .testTag(tag)
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(
        imageVector = icon,
        contentDescription = title,
        tint = tint,
        modifier = Modifier.size(22.dp)
      )

      Spacer(modifier = Modifier.width(16.dp))

      Text(
        text = title,
        fontSize = 15.sp,
        fontWeight = FontWeight.SemiBold,
        color = tint,
        modifier = Modifier.weight(1f)
      )

      if (badgeText != null) {
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = LyonBlueLight
        ) {
          Text(
            text = badgeText,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = LyonBluePrimary,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
          )
        }
      } else if (badgeCount != null && badgeCount > 0) {
        Surface(
          shape = CircleShape,
          color = LyonBluePrimary,
          modifier = Modifier.size(22.dp)
        ) {
          Box(contentAlignment = Alignment.Center) {
            Text(
              text = "$badgeCount",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = Color.White
            )
          }
        }
      }
    }
  }
}


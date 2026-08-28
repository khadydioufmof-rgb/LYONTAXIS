package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
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
import com.example.model.ChatMessage
import com.example.model.Driver
import com.example.ui.theme.*

@Composable
fun DriverChatScreen(
  driver: Driver,
  messages: List<ChatMessage>,
  isDriverTyping: Boolean = false,
  onSendMessage: (String) -> Unit,
  onSendVoiceNote: () -> Unit = {},
  onShareLocation: () -> Unit = {},
  onCallDriver: () -> Unit,
  onBackClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  var inputText by remember { mutableStateOf("") }
  val listState = rememberLazyListState()

  LaunchedEffect(messages.size, isDriverTyping) {
    if (messages.isNotEmpty()) {
      listState.animateScrollToItem(messages.size - 1 + if (isDriverTyping) 1 else 0)
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(AberBackground)
  ) {
    // Top Chat Header
    Surface(
      color = Color.White,
      shadowElevation = 4.dp,
      modifier = Modifier
        .fillMaxWidth()
        .statusBarsPadding()
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(
          onClick = onBackClick,
          modifier = Modifier.testTag("chat_back_button")
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            tint = AberDark
          )
        }

        // Driver Avatar with Online badge
        Box {
          Surface(
            shape = CircleShape,
            color = LyonBlueLight,
            border = BorderStroke(1.5.dp, LyonBluePrimary),
            modifier = Modifier.size(42.dp)
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = LyonBluePrimary,
                modifier = Modifier.size(24.dp)
              )
            }
          }
          Surface(
            shape = CircleShape,
            color = LyonBluePrimary,
            border = BorderStroke(1.5.dp, Color.White),
            modifier = Modifier
              .size(10.dp)
              .align(Alignment.BottomEnd)
          ) {}
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = driver.name,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = AberDark
          )
          if (isDriverTyping) {
            Text(
              text = "écrit...",
              fontSize = 12.sp,
              color = LyonBluePrimary,
              fontWeight = FontWeight.Bold
            )
          } else {
            Text(
              text = "${driver.carModel} • ${driver.licensePlate}",
              fontSize = 12.sp,
              color = LyonBluePrimary,
              fontWeight = FontWeight.Medium
            )
          }
        }

        IconButton(
          onClick = onCallDriver,
          modifier = Modifier.testTag("chat_call_button")
        ) {
          Icon(
            imageVector = Icons.Default.Call,
            contentDescription = "Appeler",
            tint = LyonBluePrimary
          )
        }
      }
    }

    // Message Thread
    LazyColumn(
      state = listState,
      modifier = Modifier
        .weight(1f)
        .padding(horizontal = 16.dp),
      contentPadding = PaddingValues(vertical = 16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      item {
        Box(
          modifier = Modifier.fillMaxWidth(),
          contentAlignment = Alignment.Center
        ) {
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color.White,
            border = BorderStroke(1.dp, AberBorder),
            modifier = Modifier.padding(vertical = 4.dp)
          ) {
            Text(
              text = "Chauffeur assigné • Course en cours",
              fontSize = 11.sp,
              color = AberGrayText,
              fontWeight = FontWeight.SemiBold,
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
          }
        }
      }

      items(messages, key = { it.id }) { msg ->
        RealTimeChatBubble(message = msg)
      }

      if (isDriverTyping) {
        item {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 4.dp, top = 2.dp)
          ) {
            Surface(
              shape = CircleShape,
              color = LyonBlueLight,
              modifier = Modifier.size(24.dp)
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(
                  Icons.Default.Person,
                  contentDescription = null,
                  tint = LyonBluePrimary,
                  modifier = Modifier.size(14.dp)
                )
              }
            }
            Spacer(modifier = Modifier.width(6.dp))
            AnimatedTypingDotsBubble()
          }
        }
      }
    }

    // Quick Reply Chips
    LazyRow(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 6.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      item {
        Surface(
          shape = RoundedCornerShape(14.dp),
          color = Color.White,
          border = BorderStroke(1.dp, AberBorder),
          modifier = Modifier.clickable { onShareLocation() }
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(Icons.Default.LocationOn, contentDescription = null, tint = AberRed, modifier = Modifier.size(13.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = "Partager position", fontSize = 12.sp, color = AberDark, fontWeight = FontWeight.Medium)
          }
        }
      }

      item {
        Surface(
          shape = RoundedCornerShape(14.dp),
          color = Color.White,
          border = BorderStroke(1.dp, AberBorder),
          modifier = Modifier.clickable { onSendMessage("Je suis au point de rendez-vous") }
        ) {
          Text(
            text = "Je suis au point de RDV 📍",
            fontSize = 12.sp,
            color = AberDark,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
          )
        }
      }

      item {
        Surface(
          shape = RoundedCornerShape(14.dp),
          color = Color.White,
          border = BorderStroke(1.dp, AberBorder),
          modifier = Modifier.clickable { onSendMessage("J'arrive dans 2 minutes") }
        ) {
          Text(
            text = "J'arrive dans 2 min ⏱️",
            fontSize = 12.sp,
            color = AberDark,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
          )
        }
      }

      item {
        Surface(
          shape = RoundedCornerShape(14.dp),
          color = Color.White,
          border = BorderStroke(1.dp, AberBorder),
          modifier = Modifier.clickable { onSendMessage("J'attends à l'extérieur devant l'entrée") }
        ) {
          Text(
            text = "J'attends dehors 🚪",
            fontSize = 12.sp,
            color = AberDark,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
          )
        }
      }

      item {
        Surface(
          shape = RoundedCornerShape(14.dp),
          color = Color.White,
          border = BorderStroke(1.dp, AberBorder),
          modifier = Modifier.clickable { onSendMessage("J'ai 2 valises à mettre dans le coffre") }
        ) {
          Text(
            text = "J'ai des valises 🧳",
            fontSize = 12.sp,
            color = AberDark,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
          )
        }
      }

      item {
        Surface(
          shape = RoundedCornerShape(14.dp),
          color = Color.White,
          border = BorderStroke(1.dp, AberBorder),
          modifier = Modifier.clickable { onSendMessage("Où êtes-vous actuellement ?") }
        ) {
          Text(
            text = "Où êtes-vous ?",
            fontSize = 12.sp,
            color = AberDark,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
          )
        }
      }
    }

    // Bottom Input Bar
    Surface(
      color = Color.White,
      shadowElevation = 8.dp,
      modifier = Modifier
        .fillMaxWidth()
        .navigationBarsPadding()
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(
          onClick = onShareLocation,
          modifier = Modifier.size(36.dp)
        ) {
          Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = "Partager position",
            tint = AberRed,
            modifier = Modifier.size(20.dp)
          )
        }

        IconButton(
          onClick = onSendVoiceNote,
          modifier = Modifier.size(36.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Mic,
            contentDescription = "Message vocal",
            tint = LyonBluePrimary,
            modifier = Modifier.size(20.dp)
          )
        }

        OutlinedTextField(
          value = inputText,
          onValueChange = { inputText = it },
          placeholder = { Text("Écrire un message...", color = AberGrayText, fontSize = 13.sp) },
          modifier = Modifier
            .weight(1f)
            .testTag("chat_message_input"),
          shape = RoundedCornerShape(24.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = LyonBluePrimary,
            unfocusedBorderColor = AberBorder,
            focusedContainerColor = AberGrayLight,
            unfocusedContainerColor = AberGrayLight
          ),
          maxLines = 3,
          textStyle = LocalTextStyle.current.copy(fontSize = 13.sp)
        )

        Spacer(modifier = Modifier.width(6.dp))

        Surface(
          shape = CircleShape,
          color = if (inputText.isNotBlank()) LyonBluePrimary else AberGrayLight,
          modifier = Modifier
            .size(42.dp)
            .clickable(enabled = inputText.isNotBlank()) {
              onSendMessage(inputText)
              inputText = ""
            }
            .testTag("chat_send_button")
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.Send,
              contentDescription = "Envoyer",
              tint = if (inputText.isNotBlank()) Color.White else AberGrayText,
              modifier = Modifier.size(18.dp)
            )
          }
        }
      }
    }
  }
}

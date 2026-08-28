package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.example.model.PassengerConversation
import com.example.model.SupportFaq
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatSupportScreen(
    conversations: List<PassengerConversation>,
    selectedConversationId: String?,
    faqs: List<SupportFaq>,
    onSelectConversation: (String?) -> Unit,
    onSendMessage: (String, String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedSection by remember { mutableStateOf(0) } // 0 = Passagers, 1 = Assistance
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("Tous") } // "Tous", "En cours", "Terminées"
    var showCallPassengerDialog by remember { mutableStateOf<PassengerConversation?>(null) }

    val activeConversation = conversations.find { it.id == selectedConversationId }
    val totalUnread = conversations.sumOf { it.unreadCount }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (activeConversation != null && selectedSection == 0) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color(activeConversation.avatarColorHex)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = activeConversation.avatarInitials,
                                    color = Color(0xFF003829),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.sp
                                )
                            }
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = activeConversation.passengerName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = Color.White
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = null,
                                            tint = AberGold,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Text(
                                            text = String.format("%.2f", activeConversation.passengerRating),
                                            color = AberGold,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Text(
                                    text = activeConversation.rideStatus,
                                    color = if (activeConversation.isCurrentActiveTrip) AberMint else TextSecondaryDark,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    } else {
                        Text(
                            text = if (selectedSection == 0) "Messages Passagers" else "Centre d'Assistance",
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                            color = Color.White
                        )
                    }
                },
                navigationIcon = {
                    if (activeConversation != null && selectedSection == 0) {
                        IconButton(
                            onClick = { onSelectConversation(null) },
                            modifier = Modifier.testTag("back_to_conversations_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Retour aux conversations",
                                tint = Color.White
                            )
                        }
                    }
                },
                actions = {
                    if (activeConversation != null && selectedSection == 0) {
                        IconButton(
                            onClick = { showCallPassengerDialog = activeConversation },
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(AberMint.copy(alpha = 0.15f))
                                .testTag("call_passenger_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = "Appeler le passager",
                                tint = AberMint,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0B0F17))
            )
        },
        containerColor = Color(0xFF0B0F17),
        modifier = modifier.testTag("chat_support_screen")
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // TAB ROW (Only visible when no conversation is actively opened)
            if (activeConversation == null) {
                TabRow(
                    selectedTabIndex = selectedSection,
                    containerColor = Color(0xFF131A26),
                    contentColor = AberMint
                ) {
                    Tab(
                        selected = selectedSection == 0,
                        onClick = { selectedSection = 0 },
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Conversations",
                                    fontWeight = if (selectedSection == 0) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedSection == 0) AberMint else TextSecondaryDark
                                )
                                if (totalUnread > 0) {
                                    Box(
                                        modifier = Modifier
                                            .size(18.dp)
                                            .clip(CircleShape)
                                            .background(AberMint),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "$totalUnread",
                                            color = Color(0xFF003829),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                }
                            }
                        }
                    )
                    Tab(
                        selected = selectedSection == 1,
                        onClick = { selectedSection = 1 },
                        text = {
                            Text(
                                text = "Aide & FAQ Chauffeur",
                                fontWeight = if (selectedSection == 1) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedSection == 1) AberMint else TextSecondaryDark
                            )
                        }
                    )
                }
            }

            if (selectedSection == 0) {
                if (activeConversation != null) {
                    // ACTIVE SINGLE CONVERSATION CHAT VIEW
                    PassengerChatView(
                        conversation = activeConversation,
                        onSendMessage = { text ->
                            onSendMessage(text, activeConversation.id)
                        }
                    )
                } else {
                    // ACTIVE CONVERSATIONS LIST
                    ConversationsListView(
                        conversations = conversations,
                        searchQuery = searchQuery,
                        onSearchChange = { searchQuery = it },
                        selectedFilter = selectedFilter,
                        onFilterChange = { selectedFilter = it },
                        onSelectConversation = onSelectConversation
                    )
                }
            } else {
                // SUPPORT & HELP DESK VIEW
                SupportDeskView(faqs = faqs)
            }
        }
    }

    // CALL PASSENGER CONFIRMATION POPUP DIALOG
    showCallPassengerDialog?.let { conv ->
        AlertDialog(
            onDismissRequest = { showCallPassengerDialog = null },
            icon = {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(AberMint.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = null,
                        tint = AberMint,
                        modifier = Modifier.size(26.dp)
                    )
                }
            },
            title = {
                Text(
                    text = "Appeler ${conv.passengerName} ?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.White
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Numéro masqué sécurisé LyonTaxis :",
                        color = TextSecondaryDark,
                        fontSize = 12.sp
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF1E293B),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = conv.passengerPhone,
                            color = AberMint,
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                    Text(
                        text = "Votre numéro personnel reste totalement confidentiel.",
                        color = TextMutedDark,
                        fontSize = 11.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showCallPassengerDialog = null },
                    colors = ButtonDefaults.buttonColors(containerColor = AberMint),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Lancer l'appel", color = Color(0xFF003829), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showCallPassengerDialog = null },
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Text("Fermer")
                }
            },
            containerColor = Color(0xFF16202E),
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
private fun ConversationsListView(
    conversations: List<PassengerConversation>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    selectedFilter: String,
    onFilterChange: (String) -> Unit,
    onSelectConversation: (String) -> Unit
) {
    val filteredConversations = remember(conversations, searchQuery, selectedFilter) {
        conversations.filter { conv ->
            val matchQuery = searchQuery.isBlank() ||
                    conv.passengerName.contains(searchQuery, ignoreCase = true) ||
                    conv.pickupAddress.contains(searchQuery, ignoreCase = true) ||
                    conv.lastMessage.contains(searchQuery, ignoreCase = true)

            val matchFilter = when (selectedFilter) {
                "En cours" -> conv.isCurrentActiveTrip
                "Terminées" -> !conv.isCurrentActiveTrip
                else -> true
            }

            matchQuery && matchFilter
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // SEARCH BAR
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            placeholder = { Text("Rechercher un passager ou une adresse...", color = TextMutedDark, fontSize = 13.sp) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = TextMutedDark,
                    modifier = Modifier.size(18.dp)
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Effacer",
                            tint = TextMutedDark,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("search_conversations_input"),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF131A26),
                unfocusedContainerColor = Color(0xFF131A26),
                focusedBorderColor = AberMint,
                unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )

        // FILTER PILLS
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Tous", "En cours", "Terminées").forEach { filter ->
                val isSelected = selectedFilter == filter
                Surface(
                    onClick = { onFilterChange(filter) },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) AberMint else Color(0xFF131A26),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSelected) AberMint else Color.White.copy(alpha = 0.1f)
                    )
                ) {
                    Text(
                        text = filter,
                        color = if (isSelected) Color(0xFF003829) else TextSecondaryDark,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // CONVERSATIONS LIST
        if (filteredConversations.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 48.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ChatBubbleOutline,
                        contentDescription = null,
                        tint = TextMutedDark,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = "Aucune conversation trouvée",
                        color = TextSecondaryDark,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("conversations_list"),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredConversations, key = { it.id }) { conv ->
                    ConversationItemCard(
                        conversation = conv,
                        onClick = { onSelectConversation(conv.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ConversationItemCard(
    conversation: PassengerConversation,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("conversation_item_${conversation.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (conversation.isCurrentActiveTrip) Color(0xFF162232) else Color(0xFF131A26)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (conversation.isCurrentActiveTrip) AberMint.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.08f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Passenger Avatar
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(Color(conversation.avatarColorHex)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = conversation.avatarInitials,
                    color = Color(0xFF003829),
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Main Info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = conversation.passengerName,
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (conversation.isCurrentActiveTrip) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = AberMint.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "EN COURS",
                                    color = AberMint,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Text(
                        text = conversation.lastMessageTime,
                        color = if (conversation.unreadCount > 0) AberMint else TextMutedDark,
                        fontSize = 11.sp,
                        fontWeight = if (conversation.unreadCount > 0) FontWeight.Bold else FontWeight.Normal
                    )
                }

                // Route preview
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = null,
                        tint = TextMutedDark,
                        modifier = Modifier.size(11.dp)
                    )
                    Text(
                        text = "${conversation.pickupAddress} → ${conversation.dropoffAddress}",
                        color = TextMutedDark,
                        fontSize = 11.sp,
                        maxLines = 1
                    )
                }

                // Last Message snippet + Unread Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = conversation.lastMessage,
                        color = if (conversation.unreadCount > 0) TextPrimaryDark else TextSecondaryDark,
                        fontSize = 13.sp,
                        fontWeight = if (conversation.unreadCount > 0) FontWeight.Bold else FontWeight.Normal,
                        maxLines = 1,
                        modifier = Modifier.weight(1f)
                    )

                    if (conversation.unreadCount > 0) {
                        Box(
                            modifier = Modifier
                                .padding(start = 6.dp)
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(AberMint),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${conversation.unreadCount}",
                                color = Color(0xFF003829),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PassengerChatView(
    conversation: PassengerConversation,
    onSendMessage: (String) -> Unit
) {
    var inputMessage by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val driverQuickReplies = listOf(
        "📍 Je suis au point de rendez-vous",
        "⏱️ J'arrive dans 2 minutes",
        "🚨 Feux de détresse allumés",
        "⚠️ Léger ralentissement sur la route",
        "👋 Où vous trouvez-vous exactement ?"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // TOP ROUTE CONTEXT CARD
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFF131A26),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(AberMint)
                    )
                    Text(
                        text = conversation.pickupAddress,
                        color = TextPrimaryDark,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(AberGold)
                    )
                    Text(
                        text = conversation.dropoffAddress,
                        color = TextSecondaryDark,
                        fontSize = 12.sp,
                        maxLines = 1
                    )
                }
            }
        }

        // CHAT MESSAGES SCROLLABLE LIST
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(conversation.messages, key = { it.id }) { msg ->
                ChatBubble(msg)
            }
        }

        // QUICK REPLIES HORIZONTAL ROW
        Text(
            text = "Réponses rapides chauffeur :",
            color = TextMutedDark,
            fontSize = 11.sp,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(driverQuickReplies) { reply ->
                Surface(
                    onClick = {
                        onSendMessage(reply)
                        coroutineScope.launch {
                            listState.animateScrollToItem((conversation.messages.size).coerceAtLeast(0))
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF1E2736),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AberMint.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = reply,
                        color = TextPrimaryDark,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // MESSAGE INPUT FIELD
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = inputMessage,
                onValueChange = { inputMessage = it },
                placeholder = { Text("Écrire à ${conversation.passengerName}...", color = TextMutedDark, fontSize = 13.sp) },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_input_field"),
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF131A26),
                    unfocusedContainerColor = Color(0xFF131A26),
                    focusedBorderColor = AberMint,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            IconButton(
                onClick = {
                    if (inputMessage.isNotBlank()) {
                        onSendMessage(inputMessage)
                        inputMessage = ""
                        coroutineScope.launch {
                            listState.animateScrollToItem((conversation.messages.size).coerceAtLeast(0))
                        }
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(AberMint)
                    .testTag("send_chat_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Envoyer le message",
                    tint = Color(0xFF003829),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun SupportDeskView(faqs: List<SupportFaq>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Priority Support Hotline Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF16202E)),
                border = androidx.compose.foundation.BorderStroke(1.dp, AberMint.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(AberMint.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.HeadsetMic,
                            contentDescription = null,
                            tint = AberMint,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Ligne Chauffeur 24/7",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Assistance prioritaire pour vos courses",
                            color = TextSecondaryDark,
                            fontSize = 12.sp
                        )
                    }

                    Button(
                        onClick = { /* Call hotline */ },
                        colors = ButtonDefaults.buttonColors(containerColor = AberMint),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Appeler", color = Color(0xFF003829), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // FAQ Section
        item {
            Text(
                text = "Questions fréquentes",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        items(faqs) { faq ->
            var expanded by remember { mutableStateOf(false) }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF131A26))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = faq.question,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = AberMint
                        )
                    }

                    AnimatedVisibility(visible = expanded) {
                        Text(
                            text = faq.answer,
                            color = TextSecondaryDark,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatBubble(msg: ChatMessage) {
    val isDriver = msg.senderIsDriver
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isDriver) Alignment.End else Alignment.Start
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isDriver) 16.dp else 4.dp,
                bottomEnd = if (isDriver) 4.dp else 16.dp
            ),
            color = if (isDriver) AberMint else Color(0xFF1E293B),
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Text(
                text = msg.text,
                color = if (isDriver) Color(0xFF003829) else Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(12.dp)
            )
        }
        Text(
            text = msg.timeFormatted,
            color = TextMutedDark,
            fontSize = 10.sp,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        )
    }
}

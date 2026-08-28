package com.example.data

import com.example.data.local.VehicleDao
import com.example.data.local.VehicleProfileEntity
import com.example.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DriverRepository(
    private val vehicleDao: VehicleDao? = null
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _vehicleProfile = MutableStateFlow(
        VehicleProfileEntity(
            id = 1,
            model = "Mercedes-Benz Classe E 220d",
            plate = "GC-742-LK",
            color = "Noir Obsidienne",
            category = "LyonTaxis Berline & Confort",
            inspectionStatus = "Conforme & Validé",
            lastInspectionDate = "12/06/2026",
            nextInspectionDate = "12/06/2027",
            mileageKm = 64250,
            technicalNotes = "Pneus neufs Michelin CrossClimate 2, révision des 60k km effectuée en concession.",
            insuranceCompany = "AXA Entreprise Pro VTC",
            insuranceExpiry = "15/11/2026",
            isHybridOrElectric = true
        )
    )
    val vehicleProfile: StateFlow<VehicleProfileEntity> = _vehicleProfile.asStateFlow()

    private val _driverProfile = MutableStateFlow(
        DriverProfile()
    )
    val driverProfile: StateFlow<DriverProfile> = _driverProfile.asStateFlow()

    init {
        vehicleDao?.let { dao ->
            scope.launch {
                dao.getVehicleProfile().collect { entity ->
                    if (entity != null) {
                        _vehicleProfile.value = entity
                        syncDriverProfileWithVehicle(entity)
                    } else {
                        // Seed initial entity into Room
                        val initial = _vehicleProfile.value
                        dao.insertOrUpdate(initial)
                    }
                }
            }
        }
    }

    private fun syncDriverProfileWithVehicle(vehicle: VehicleProfileEntity) {
        _driverProfile.update { current ->
            current.copy(
                vehicleModel = vehicle.model,
                vehiclePlate = vehicle.plate,
                vehicleColor = vehicle.color,
                vehicleInspectionStatus = vehicle.inspectionStatus,
                vehicleLastInspectionDate = vehicle.lastInspectionDate,
                vehicleNextInspectionDate = vehicle.nextInspectionDate,
                vehicleMileageKm = vehicle.mileageKm,
                vehicleTechnicalNotes = vehicle.technicalNotes
            )
        }
    }

    suspend fun saveVehicleProfile(updated: VehicleProfileEntity) {
        _vehicleProfile.value = updated
        syncDriverProfileWithVehicle(updated)
        vehicleDao?.insertOrUpdate(updated)
    }

    private val _earningsHistory = MutableStateFlow(getInitialEarnings())
    val earningsHistory: StateFlow<List<EarningsRecord>> = _earningsHistory.asStateFlow()

    private val _surgeHotspots = MutableStateFlow(getInitialSurgeZones())
    val surgeHotspots: StateFlow<List<SurgeHotspot>> = _surgeHotspots.asStateFlow()

    private val _conversations = MutableStateFlow(getInitialConversations())
    val conversations: StateFlow<List<PassengerConversation>> = _conversations.asStateFlow()

    private val _chatMessages = MutableStateFlow(getInitialChat())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    fun updateProfile(transform: (DriverProfile) -> DriverProfile) {
        _driverProfile.update(transform)
    }

    fun setWeeklyGoal(goal: Double) {
        _driverProfile.update { current ->
            current.copy(weeklyGoalEarnings = goal.coerceAtLeast(100.0))
        }
    }

    fun completeTrip(record: EarningsRecord) {
        _earningsHistory.update { listOf(record) + it }
        _driverProfile.update { current ->
            current.copy(
                balance = current.balance + record.netEarnings,
                todayEarnings = current.todayEarnings + record.netEarnings,
                todayTripsCount = current.todayTripsCount + 1,
                totalRides = current.totalRides + 1
            )
        }
    }

    fun performInstantPayout(amount: Double): Boolean {
        if (_driverProfile.value.balance < amount) return false
        _driverProfile.update { current ->
            current.copy(balance = current.balance - amount)
        }
        return true
    }

    fun addChatMessage(message: ChatMessage) {
        _chatMessages.update { it + message }
    }

    fun addConversationMessage(conversationId: String, message: ChatMessage) {
        _conversations.update { list ->
            list.map { conv ->
                if (conv.id == conversationId) {
                    conv.copy(
                        messages = conv.messages + message,
                        unreadCount = if (!message.senderIsDriver) conv.unreadCount + 1 else conv.unreadCount
                    )
                } else conv
            }
        }
    }

    fun markConversationAsRead(conversationId: String) {
        _conversations.update { list ->
            list.map { conv ->
                if (conv.id == conversationId) {
                    conv.copy(unreadCount = 0)
                } else conv
            }
        }
    }

    fun addOrUpdateActiveTripConversation(ride: RideRequest) {
        _conversations.update { list ->
            val existing = list.find { it.id == ride.id }
            if (existing != null) {
                list.map { if (it.id == ride.id) it.copy(isCurrentActiveTrip = true, rideStatus = "Course en cours") else it.copy(isCurrentActiveTrip = false) }
            } else {
                val newConv = PassengerConversation(
                    id = ride.id,
                    passengerName = ride.passengerName,
                    passengerRating = ride.passengerRating,
                    passengerPhone = ride.passengerPhone,
                    rideStatus = "Course en cours",
                    pickupAddress = ride.pickupAddress,
                    dropoffAddress = ride.dropoffAddress,
                    isCurrentActiveTrip = true,
                    unreadCount = 1,
                    avatarInitials = ride.passengerName.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString("").ifEmpty { "PA" },
                    avatarColorHex = 0xFF00D09C,
                    messages = listOf(
                        ChatMessage("m_init_${ride.id}", false, "Bonjour, je vous attends au point de prise en charge !", "À l'instant")
                    )
                )
                listOf(newConv) + list.map { it.copy(isCurrentActiveTrip = false) }
            }
        }
    }

    fun clearChat() {
        _chatMessages.value = emptyList()
    }

    fun setQuickFilter(filter: RideQuickFilter) {
        _driverProfile.update { it.copy(activeQuickFilter = filter) }
    }

    fun createSampleOffer(preferredFilter: RideQuickFilter = _driverProfile.value.activeQuickFilter): RideRequest {
        val allSamples = listOf(
            // 1. Short distance
            RideRequest(
                id = "ABR-${(1000..9999).random()}",
                passengerName = "Élodie Martin",
                passengerRating = 4.96,
                passengerTrips = 142,
                passengerPhone = "+33 6 12 34 56 78",
                category = VehicleCategory.ABER_BERLINE,
                pickupAddress = "84 Avenue des Champs-Élysées, 75008 Paris",
                pickupDistanceKm = 0.8,
                pickupDurationMin = 3,
                dropoffAddress = "14 Place Vendôme, 75001 Paris",
                dropoffDistanceKm = 3.6,
                dropoffDurationMin = 14,
                estimatedFare = 28.50,
                surgeMultiplier = 1.4,
                tipAmount = 4.00,
                paymentMethod = "LyonTaxis Pay (CB)",
                specialNote = "Passager avec une valise cabine.",
                navigationSteps = listOf(
                    NavigationStep("Tourner à droite", "Rue de Berri", 250, "turn_right"),
                    NavigationStep("Continuer tout droit", "Boulevard Haussmann", 900, "straight"),
                    NavigationStep("Prendre à gauche", "Rue de la Paix", 600, "turn_left"),
                    NavigationStep("Vous êtes arrivé à destination", "14 Place Vendôme", 50, "destination")
                ),
                pickupLat = 0.32f,
                pickupLng = 0.38f,
                dropoffLat = 0.65f,
                dropoffLng = 0.58f
            ),
            // 2. Short distance City trip
            RideRequest(
                id = "ABR-${(1000..9999).random()}",
                passengerName = "Julien Mercier",
                passengerRating = 4.92,
                passengerTrips = 89,
                passengerPhone = "+33 6 11 22 33 44",
                category = VehicleCategory.ABER_X,
                pickupAddress = "12 Rue de Rivoli, 75004 Paris",
                pickupDistanceKm = 0.5,
                pickupDurationMin = 2,
                dropoffAddress = "24 Boulevard Saint-Germain, 75005 Paris",
                dropoffDistanceKm = 2.4,
                dropoffDurationMin = 9,
                estimatedFare = 14.50,
                surgeMultiplier = 1.0,
                tipAmount = 2.00,
                paymentMethod = "LyonTaxis Pay",
                specialNote = "Sortie Métro Saint-Paul.",
                navigationSteps = listOf(
                    NavigationStep("Continuer tout droit", "Pont de Sully", 400, "straight"),
                    NavigationStep("Tourner à droite", "Bd Saint-Germain", 500, "turn_right"),
                    NavigationStep("Arrivé à destination", "24 Bd Saint-Germain", 30, "destination")
                ),
                pickupLat = 0.45f,
                pickupLng = 0.55f,
                dropoffLat = 0.52f,
                dropoffLng = 0.58f
            ),
            // 3. Station / Airport Trip (Gare Montparnasse)
            RideRequest(
                id = "ABR-${(1000..9999).random()}",
                passengerName = "Alexandre Dupont",
                passengerRating = 4.88,
                passengerTrips = 67,
                passengerPhone = "+33 6 98 76 54 32",
                category = VehicleCategory.ABER_CONFORT,
                pickupAddress = "Gare Montparnasse - Dépose Minute, 75015 Paris",
                pickupDistanceKm = 1.4,
                pickupDurationMin = 5,
                dropoffAddress = "28 Rue Saint-Dominique, 75007 Paris",
                dropoffDistanceKm = 4.2,
                dropoffDurationMin = 16,
                estimatedFare = 22.00,
                surgeMultiplier = 1.2,
                tipAmount = 2.50,
                paymentMethod = "Apple Pay",
                specialNote = "Sortie principale Place Raoul Dautry.",
                navigationSteps = listOf(
                    NavigationStep("Prendre la sortie", "Bd de Vaugirard", 300, "straight"),
                    NavigationStep("Au rond-point, 2ème sortie", "Avenue de Saxe", 850, "roundabout"),
                    NavigationStep("Tourner à gauche", "Rue Saint-Dominique", 450, "turn_left"),
                    NavigationStep("Destination sur la droite", "28 Rue Saint-Dominique", 30, "destination")
                ),
                pickupLat = 0.72f,
                pickupLng = 0.35f,
                dropoffLat = 0.40f,
                dropoffLng = 0.45f
            ),
            // 4. Airport Trip (Orly -> Paris)
            RideRequest(
                id = "ABR-${(1000..9999).random()}",
                passengerName = "Marc & Sophie Laurent",
                passengerRating = 5.00,
                passengerTrips = 310,
                passengerPhone = "+33 6 45 67 89 01",
                category = VehicleCategory.ABER_VAN,
                pickupAddress = "Aéroport Orly Ouest - Terminal 1",
                pickupDistanceKm = 3.2,
                pickupDurationMin = 8,
                dropoffAddress = "Hôtel Le Meurice, Rue de Rivoli, Paris",
                dropoffDistanceKm = 18.5,
                dropoffDurationMin = 32,
                estimatedFare = 64.00,
                surgeMultiplier = 1.6,
                tipAmount = 8.00,
                paymentMethod = "LyonTaxis Business",
                specialNote = "3 passagers + 4 valises.",
                navigationSteps = listOf(
                    NavigationStep("Rejoindre l'autoroute A6a", "Direction Paris Porte d'Italie", 4500, "straight"),
                    NavigationStep("Prendre la sortie", "Périphérique Intérieur", 1200, "turn_right"),
                    NavigationStep("Continuer sur Quai de Bercy", "Voie Georges Pompidou", 3800, "straight"),
                    NavigationStep("Arrivée à l'hôtel", "228 Rue de Rivoli", 80, "destination")
                ),
                pickupLat = 0.85f,
                pickupLng = 0.75f,
                dropoffLat = 0.48f,
                dropoffLng = 0.52f
            ),
            // 5. Airport Trip (Paris -> CDG Roissy)
            RideRequest(
                id = "ABR-${(1000..9999).random()}",
                passengerName = "Sarah Jenkins",
                passengerRating = 4.98,
                passengerTrips = 215,
                passengerPhone = "+33 7 88 99 00 11",
                category = VehicleCategory.ABER_BERLINE,
                pickupAddress = "15 Place Vendôme, 75001 Paris",
                pickupDistanceKm = 1.1,
                pickupDurationMin = 4,
                dropoffAddress = "Aéroport CDG Paris - Terminal 2E Départs",
                dropoffDistanceKm = 29.4,
                dropoffDurationMin = 42,
                estimatedFare = 78.00,
                surgeMultiplier = 1.75,
                tipAmount = 10.00,
                paymentMethod = "LyonTaxis Business Card",
                specialNote = "Vol international départ dans 2h30, 2 bagages.",
                navigationSteps = listOf(
                    NavigationStep("Prendre Rue de la Paix", "Direction Opéra", 500, "straight"),
                    NavigationStep("Rejoindre l'autoroute A1", "Direction Lille / Aéroport CDG", 16000, "straight"),
                    NavigationStep("Prendre la sortie Aérogares 2", "Suivre Terminal 2E", 1400, "turn_right"),
                    NavigationStep("Dépose-minute T2E", "Porte 4", 100, "destination")
                ),
                pickupLat = 0.38f,
                pickupLng = 0.42f,
                dropoffLat = 0.15f,
                dropoffLng = 0.82f
            ),
            // 6. High Surge Only Trip
            RideRequest(
                id = "ABR-${(1000..9999).random()}",
                passengerName = "Lucas Moreau",
                passengerRating = 4.91,
                passengerTrips = 73,
                passengerPhone = "+33 6 33 44 55 66",
                category = VehicleCategory.ABER_GREEN,
                pickupAddress = "Accor Arena - Bercy, 75012 Paris",
                pickupDistanceKm = 1.0,
                pickupDurationMin = 3,
                dropoffAddress = "55 Avenue Kléber, 75116 Paris",
                dropoffDistanceKm = 8.6,
                dropoffDurationMin = 24,
                estimatedFare = 39.50,
                surgeMultiplier = 1.85,
                tipAmount = 5.00,
                paymentMethod = "LyonTaxis Pay",
                specialNote = "Sortie concert Bercy, forte affluence.",
                navigationSteps = listOf(
                    NavigationStep("Rejoindre le Quai de Bercy", "Direction Centre", 800, "straight"),
                    NavigationStep("Prendre la Voie Mazas", "Direction Concorde", 2400, "straight"),
                    NavigationStep("Rejoindre Avenue Kléber", "Direction Trocadéro", 1100, "turn_left"),
                    NavigationStep("Arrivé à destination", "55 Avenue Kléber", 40, "destination")
                ),
                pickupLat = 0.78f,
                pickupLng = 0.72f,
                dropoffLat = 0.36f,
                dropoffLng = 0.30f
            )
        )

        val filtered = when (preferredFilter) {
            RideQuickFilter.ALL -> allSamples
            RideQuickFilter.SHORT_DISTANCE -> allSamples.filter { it.dropoffDistanceKm < 6.0 }
            RideQuickFilter.AIRPORT -> allSamples.filter {
                it.pickupAddress.contains("Aéroport", ignoreCase = true) ||
                it.dropoffAddress.contains("Aéroport", ignoreCase = true) ||
                it.pickupAddress.contains("Gare", ignoreCase = true) ||
                it.dropoffAddress.contains("Gare", ignoreCase = true)
            }
            RideQuickFilter.SURGE_ONLY -> allSamples.filter { it.surgeMultiplier > 1.3 }
            RideQuickFilter.PREMIUM_ONLY -> allSamples.filter {
                it.category == VehicleCategory.ABER_BERLINE || it.category == VehicleCategory.ABER_VAN
            }
        }

        return (if (filtered.isNotEmpty()) filtered else allSamples).random()
    }

    private fun getInitialEarnings(): List<EarningsRecord> = listOf(
        EarningsRecord(
            id = "REC-9481",
            timestamp = "Aujourd'hui, 14:15",
            passengerName = "Thomas L.",
            pickup = "Avenue Montaigne, 75008",
            dropoff = "Gare de Lyon, 75012",
            baseFare = 21.00,
            surgeBonus = 4.50,
            tip = 3.00,
            platformFee = 4.20,
            netEarnings = 24.30,
            category = VehicleCategory.ABER_BERLINE,
            distanceKm = 6.4,
            durationMin = 22,
                paymentMethod = "LyonTaxis Pay"
        ),
        EarningsRecord(
            id = "REC-9480",
            timestamp = "Aujourd'hui, 12:40",
            passengerName = "Claire B.",
            pickup = "Boulevard Saint-Germain, 75006",
            dropoff = "Rue de Rivoli, 75004",
            baseFare = 13.50,
            surgeBonus = 2.00,
            tip = 2.00,
            platformFee = 2.70,
            netEarnings = 14.80,
            category = VehicleCategory.ABER_CONFORT,
            distanceKm = 2.8,
            durationMin = 11,
            paymentMethod = "Apple Pay"
        ),
        EarningsRecord(
            id = "REC-9479",
            timestamp = "Aujourd'hui, 10:20",
            passengerName = "David R.",
            pickup = "Tour Eiffel - Port de la Bourdonnais",
            dropoff = "Opéra Garnier, 75009",
            baseFare = 18.00,
            surgeBonus = 3.60,
            tip = 4.00,
            platformFee = 3.60,
            netEarnings = 22.00,
            category = VehicleCategory.ABER_X,
            distanceKm = 4.7,
            durationMin = 18,
            paymentMethod = "Carte Bancaire"
        ),
        EarningsRecord(
            id = "REC-9478",
            timestamp = "Aujourd'hui, 08:35",
            passengerName = "Sophie M.",
            pickup = "Neuilly-sur-Seine",
            dropoff = "La Défense Grande Arche",
            baseFare = 15.00,
            surgeBonus = 5.00,
            tip = 0.00,
            platformFee = 3.00,
            netEarnings = 17.00,
            category = VehicleCategory.ABER_GREEN,
            distanceKm = 3.2,
            durationMin = 14,
                paymentMethod = "LyonTaxis Pay"
        ),
        EarningsRecord(
            id = "REC-9475",
            timestamp = "Hier, 22:15",
            passengerName = "Julien K.",
            pickup = "Châtelet Les Halles",
            dropoff = "Aéroport Roissy CDG 2E",
            baseFare = 45.00,
            surgeBonus = 12.50,
            tip = 10.00,
            platformFee = 9.00,
            netEarnings = 58.50,
            category = VehicleCategory.ABER_BERLINE,
            distanceKm = 29.0,
            durationMin = 38,
            paymentMethod = "Aber Business"
        )
    )

    private fun getInitialSurgeZones(): List<SurgeHotspot> = listOf(
        SurgeHotspot("z1", "Champs-Élysées & Triangle d'Or", "Paris 8e", 1.8, 6.50, "Très forte", 45, 0.35f, 0.40f),
        SurgeHotspot("z2", "Opéra - Saint-Lazare", "Paris 9e", 1.6, 5.00, "Forte", 60, 0.55f, 0.32f),
        SurgeHotspot("z3", "Gare de Lyon & Bercy", "Paris 12e", 1.9, 7.20, "Très forte", 30, 0.78f, 0.68f),
        SurgeHotspot("z4", "La Défense Esplanade", "Hauts-de-Seine", 1.5, 4.00, "Forte", 90, 0.18f, 0.22f),
        SurgeHotspot("z5", "Châtelet & Marais", "Paris 4e", 1.7, 5.80, "Très forte", 50, 0.58f, 0.52f),
        SurgeHotspot("z6", "Montparnasse Gare", "Paris 15e", 1.4, 3.50, "Modérée", 120, 0.42f, 0.72f)
    )

    private fun getInitialChat(): List<ChatMessage> = listOf(
        ChatMessage("c1", false, "Bonjour Karim ! Je vous attends devant le numéro 84.", "14:02"),
        ChatMessage("c2", true, "Bonjour Élodie, j'arrive dans 2 minutes, je suis sur l'avenue.", "14:03"),
        ChatMessage("c3", false, "Parfait, je suis en manteau beige avec une petite valise.", "14:04")
    )

    private fun getInitialConversations(): List<PassengerConversation> = listOf(
        PassengerConversation(
            id = "conv_1",
            passengerName = "Élodie Martin",
            passengerRating = 4.96,
            passengerPhone = "+33 6 12 34 56 78",
            rideStatus = "Course en cours",
            pickupAddress = "84 Avenue des Champs-Élysées, Paris 8e",
            dropoffAddress = "14 Place Vendôme, Paris 1er",
            isCurrentActiveTrip = true,
            unreadCount = 1,
            avatarInitials = "EM",
            avatarColorHex = 0xFF00D09C,
            messages = listOf(
                ChatMessage("c1", false, "Bonjour Karim ! Je vous attends devant le numéro 84.", "14:02"),
                ChatMessage("c2", true, "Bonjour Élodie, j'arrive dans 2 minutes, je suis sur l'avenue.", "14:03"),
                ChatMessage("c3", false, "Parfait, je suis en manteau beige avec une petite valise.", "14:04")
            )
        ),
        PassengerConversation(
            id = "conv_2",
            passengerName = "Alexandre Dupont",
            passengerRating = 4.88,
            passengerPhone = "+33 6 98 76 54 32",
            rideStatus = "Terminée il y a 25 min",
            pickupAddress = "Gare Montparnasse, Paris 15e",
            dropoffAddress = "28 Rue Saint-Dominique, Paris 7e",
            isCurrentActiveTrip = false,
            unreadCount = 0,
            avatarInitials = "AD",
            avatarColorHex = 0xFF3B82F6,
            messages = listOf(
                ChatMessage("c2_1", true, "Bonjour Alexandre, je suis garé sur le dépose-minute.", "13:20"),
                ChatMessage("c2_2", false, "Super, j'arrive avec mon chariot !", "13:22"),
                ChatMessage("c2_3", false, "Merci beaucoup pour la conduite et la musique !", "13:45")
            )
        ),
        PassengerConversation(
            id = "conv_3",
            passengerName = "Sophie Marceau",
            passengerRating = 4.92,
            passengerPhone = "+33 6 45 23 89 00",
            rideStatus = "Terminée ce matin",
            pickupAddress = "15 Rue de Rivoli, Paris 4e",
            dropoffAddress = "Tour Eiffel - Quai Branly, Paris 7e",
            isCurrentActiveTrip = false,
            unreadCount = 0,
            avatarInitials = "SM",
            avatarColorHex = 0xFF8B5CF6,
            messages = listOf(
                ChatMessage("c3_1", false, "Bonjour, avez-vous de la place pour 2 sacs ?", "09:12"),
                ChatMessage("c3_2", true, "Bonjour Sophie, oui le coffre est totalement libre.", "09:13"),
                ChatMessage("c3_3", false, "Parfait merci !", "09:14")
            )
        ),
        PassengerConversation(
            id = "conv_4",
            passengerName = "Julien & Sarah",
            passengerRating = 5.00,
            passengerPhone = "+33 6 77 88 99 11",
            rideStatus = "Réservation 19:30",
            pickupAddress = "Hôtel Ritz, 15 Place Vendôme",
            dropoffAddress = "Aéroport Roissy CDG 2E",
            isCurrentActiveTrip = false,
            unreadCount = 0,
            avatarInitials = "JS",
            avatarColorHex = 0xFFF59E0B,
            messages = listOf(
                ChatMessage("c4_1", false, "Bonjour Karim, nous aurons un vol à 22h, à tout à l'heure !", "Hier 18:00"),
                ChatMessage("c4_2", true, "Bonjour, bien noté ! Je serai là à 19h25 précises.", "Hier 18:05")
            )
        )
    )

    fun getFaqs(): List<SupportFaq> = listOf(
        SupportFaq("f1", "Comment fonctionne le virement instantané ?", "Vous pouvez transférer vos gains disponibles à tout moment sur votre compte bancaire ou carte Visa/Mastercard en moins de 60 secondes.", "Paiement"),
        SupportFaq("f2", "Que faire en cas d'annulation passager après 2 minutes ?", "Si un passager annule après 2 minutes ou ne se présente pas après 5 minutes d'attente au point de prise en charge, des frais d'annulation de 6,00€ vous sont automatiquement crédités.", "Courses"),
        SupportFaq("f3", "Comment activer le filtre 'Retour vers mon domicile' ?", "Allez dans l'onglet Profil > Préférences de trajet et activez le commutateur. Vous ne recevrez que des courses qui se dirigent vers votre ville d'habitation.", "Navigation"),
        SupportFaq("f4", "Comment déclarer un objet trouvé dans mon véhicule ?", "Cliquez sur l'historique des courses, sélectionnez le trajet concerné, puis cliquez sur 'Signaler un objet oublié' pour contacter le passager en toute sécurité.", "Assistance")
    )
}

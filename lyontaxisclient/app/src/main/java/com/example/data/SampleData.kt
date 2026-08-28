package com.example.data

import com.example.model.*

object SampleData {
  val defaultPickup = LocationPoint(
    id = "loc_pickup_0",
    title = "Place Bellecour",
    address = "Place Bellecour, 69002 Lyon, France",
    distanceKm = 0.0,
    isFavorite = true,
    latitude = 45.7578,
    longitude = 4.8320
  )

  val defaultDropoff = LocationPoint(
    id = "loc_dropoff_0",
    title = "Gare de Lyon-Part-Dieu",
    address = "5 Place Charles Béraudier, 69003 Lyon",
    distanceKm = 2.4,
    isFavorite = false,
    latitude = 45.7606,
    longitude = 4.8594
  )

  val popularLocations = listOf(
    LocationPoint("p1", "Place Bellecour", "Place Bellecour, 69002 Lyon", 0.0, true, 45.7578, 4.8320),
    LocationPoint("p2", "Gare de Lyon-Part-Dieu", "5 Place Charles Béraudier, 69003 Lyon", 2.4, true, 45.7606, 4.8594),
    LocationPoint("p3", "Vieux Lyon - Saint-Jean", "Place Saint-Jean, 69005 Lyon", 1.1, true, 45.7608, 4.8271),
    LocationPoint("p4", "Aéroport Lyon-Saint Exupéry", "69125 Colombier-Saugnieu, Lyon", 24.5, true, 45.7219, 5.0789),
    LocationPoint("p5", "Parc de la Tête d'Or", "Boulevard des Belges, 69006 Lyon", 3.2, true, 45.7772, 4.8549),
    LocationPoint("p6", "Basilique Notre-Dame de Fourvière", "8 Place de Fourvière, 69005 Lyon", 1.8, true, 45.7623, 4.8226),
    LocationPoint("p7", "Centre Commercial Confluence", "112 Cours Charlemagne, 69002 Lyon", 2.9, false, 45.7431, 4.8184),
    LocationPoint("p8", "Hôtel de Ville - Place des Terreaux", "Place des Terreaux, 69001 Lyon", 1.5, false, 45.7675, 4.8335)
  )

  val drivers = listOf(
    Driver(
      id = "driver_1",
      name = "Jean-Marc Dupont",
      rating = 4.9,
      totalTrips = 1420,
      carModel = "Peugeot 508 Hybrid",
      licensePlate = "EK-892-TL",
      phoneNumber = "+33 6 12 34 56 78",
      carColor = "Gris Métallisé",
      avatarSeed = "JeanMarc"
    ),
    Driver(
      id = "driver_2",
      name = "Nicolas Bernard",
      rating = 4.8,
      totalTrips = 980,
      carModel = "Renault Talisman",
      licensePlate = "FJ-481-BT",
      phoneNumber = "+33 6 98 76 54 32",
      carColor = "Noir Intense",
      avatarSeed = "Nicolas"
    ),
    Driver(
      id = "driver_3",
      name = "Sophie Laurent",
      rating = 5.0,
      totalTrips = 2150,
      carModel = "Tesla Model Y",
      licensePlate = "GB-903-EV",
      phoneNumber = "+33 6 45 67 89 10",
      carColor = "Blanc Nacré",
      avatarSeed = "Sophie"
    )
  )

  val initialChatMessages = listOf(
    ChatMessage("c1", true, "Bonjour, êtes-vous à proximité de la place ?", "17:03"),
    ChatMessage("c2", false, "Bonjour ! J'arrive dans environ 2 minutes.", "17:04"),
    ChatMessage("c3", true, "Parfait, je vous attends devant la statue équestre.", "17:06"),
    ChatMessage("c4", false, "Bien reçu, feux de détresse allumés sur le bas-côté.", "17:08")
  )

  val paymentMethods = listOf(
    PaymentMethodItem("pm_cash", PaymentType.CASH, "Espèces LyonTaxis", "Paiement direct au chauffeur", isDefault = true, isSelected = true),
    PaymentMethodItem("pm_visa", PaymentType.VISA, "Carte Visa", "**** **** **** 5967", isDefault = false, isSelected = false),
    PaymentMethodItem("pm_paypal", PaymentType.PAYPAL, "PayPal", "thomas.martin@lyon.fr", isDefault = false, isSelected = false),
    PaymentMethodItem("pm_mc", PaymentType.MASTERCARD, "MasterCard", "**** **** **** 3461", isDefault = false, isSelected = false)
  )

  val sampleScheduledRides = listOf(
    ScheduledRideItem(
      id = "sched_1",
      pickupLocation = LocationPoint("p1", "Place Bellecour", "Place Bellecour, 69002 Lyon", 0.0, true, 45.7578, 4.8320),
      dropoffLocation = LocationPoint("p4", "Aéroport Lyon-Saint Exupéry", "69125 Colombier-Saugnieu, Lyon", 24.5, true, 45.7219, 5.0789),
      vehicle = VehicleCategory.ELECTRIC_CAR,
      scheduledDate = "Demain (28 Août)",
      scheduledTime = "06:30",
      estimatedFare = 52.00,
      paymentMethodTitle = "Carte Visa",
      preferences = RidePreferences(extraLuggage = true, silentRide = true)
    ),
    ScheduledRideItem(
      id = "sched_2",
      pickupLocation = LocationPoint("p3", "Vieux Lyon - Saint-Jean", "Place Saint-Jean, 69005 Lyon", 1.1, true, 45.7608, 4.8271),
      dropoffLocation = LocationPoint("p2", "Gare de Lyon-Part-Dieu", "5 Place Charles Béraudier, 69003 Lyon", 2.4, true, 45.7606, 4.8594),
      vehicle = VehicleCategory.JUST_GO,
      scheduledDate = "Vendredi (29 Août)",
      scheduledTime = "14:15",
      estimatedFare = 18.00,
      paymentMethodTitle = "Espèces LyonTaxis",
      preferences = RidePreferences(babySeat = false)
    )
  )

  val tripHistory = listOf(
    TripHistoryItem("th1", "Place Bellecour", "Gare de Lyon-Part-Dieu", 18.50, "15 Oct 2024", "Terminé"),
    TripHistoryItem("th2", "Vieux Lyon - Saint-Jean", "Parc de la Tête d'Or", 14.00, "14 Oct 2024", "Terminé"),
    TripHistoryItem("th3", "Basilique Fourvière", "Place des Terreaux", 12.00, "12 Oct 2024", "Annulé"),
    TripHistoryItem("th4", "Confluence", "Aéroport Lyon-Saint Exupéry", 55.00, "10 Oct 2024", "Terminé"),
    TripHistoryItem("th5", "Gare Part-Dieu", "Place Bellecour", 15.00, "08 Oct 2024", "Terminé")
  )

  val notifications = listOf(
    NotificationItem("n1", NotificationType.SYSTEM_CONFIRM, "Système", "Votre réservation de taxi #LYON-1234 a été confirmée.", "À l'instant", false),
    NotificationItem("n2", NotificationType.PROMOTION, "Offre LyonTaxis", "Parrainez vos proches : recevez 10 € sur vos prochaines courses !", "Il y a 2h", false),
    NotificationItem("n3", NotificationType.PROMOTION, "Tarif Nuit", "Bénéficiez de 15% de réduction ce week-end à Lyon !", "Hier", false),
    NotificationItem("n4", NotificationType.SYSTEM_CANCEL, "Système", "Votre course #LYON-1205 a été annulée sans frais.", "Il y a 3 jours", true),
    NotificationItem("n5", NotificationType.SYSTEM_WALLET, "Portefeuille", "Paiement de 18,50 € validé avec succès. Merci !", "Il y a 5 jours", true)
  )

  val contacts = listOf(
    ContactFriend("cf1", "Camille Morel", "+33 6 11 22 33 44", true),
    ContactFriend("cf2", "Julien Giraud", "+33 6 55 66 77 88", false),
    ContactFriend("cf3", "Claire Bonnet", "+33 6 99 88 77 66", false),
    ContactFriend("cf4", "Antoine Roux", "+33 6 44 33 22 11", true),
    ContactFriend("cf5", "Élodie Blanc", "+33 6 77 88 99 00", false)
  )
}


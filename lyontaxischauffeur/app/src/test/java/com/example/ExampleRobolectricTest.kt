package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.DriverRepository
import com.example.model.DriverStatus
import com.example.viewmodel.DriverViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun readStringFromContext() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("LyonTaxis Pro Chauffeur", appName)
    }

    @Test
    fun driverViewModelStateTransitions() {
        val repo = DriverRepository()
        val viewModel = DriverViewModel(repo)

        assertEquals(DriverStatus.OFFLINE, viewModel.uiState.value.status)

        viewModel.goOnline()
        assertEquals(DriverStatus.ONLINE_WAITING, viewModel.uiState.value.status)

        viewModel.dispatchNewOffer()
        assertEquals(DriverStatus.DISPATCH_OFFER, viewModel.uiState.value.status)
        assertNotNull(viewModel.uiState.value.currentOffer)

        viewModel.acceptOffer()
        assertEquals(DriverStatus.EN_ROUTE_PICKUP, viewModel.uiState.value.status)
        assertNotNull(viewModel.uiState.value.activeRide)

        viewModel.arriveAtPickup()
        assertEquals(DriverStatus.ARRIVED_PICKUP, viewModel.uiState.value.status)

        viewModel.startTrip()
        assertEquals(DriverStatus.IN_TRIP, viewModel.uiState.value.status)

        viewModel.completeTrip()
        assertEquals(DriverStatus.TRIP_COMPLETED, viewModel.uiState.value.status)
        assertNotNull(viewModel.uiState.value.completedRecord)
    }

    @Test
    fun instantPayoutDeduction() {
        val repo = DriverRepository()
        val viewModel = DriverViewModel(repo)
        val initialBalance = viewModel.profile.value.balance

        viewModel.performInstantPayout(100.0)
        assertEquals(initialBalance - 100.0, viewModel.profile.value.balance, 0.01)
        assertTrue(viewModel.uiState.value.showPayoutSuccessDialog)
    }

    @Test
    fun gpsLocationTrackingUpdates() {
        val repo = DriverRepository()
        val viewModel = DriverViewModel(repo)

        viewModel.updateManualGpsPosition(
            latitude = 48.8738,
            longitude = 2.2950,
            speedKmh = 52.0f,
            bearing = 180.0f
        )

        val loc = viewModel.uiState.value.gpsLocation
        assertEquals(48.8738, loc.latitude, 0.0001)
        assertEquals(2.2950, loc.longitude, 0.0001)
        assertEquals(52.0f, loc.speedKmh, 0.1f)
        assertEquals(180.0f, loc.bearing, 0.1f)
        assertTrue(loc.hasGpsFix)
    }

    @Test
    fun earningsProfileValues() {
        val repo = DriverRepository()
        val viewModel = DriverViewModel(repo)

        val profile = viewModel.profile.value
        assertTrue(profile.todayEarnings > 0.0)
        assertTrue(profile.weeklyEarnings > 0.0)
        assertTrue(profile.weeklyEarnings >= profile.todayEarnings)
    }

    @Test
    fun passengerConversationsAndMessaging() {
        val repo = DriverRepository()
        val viewModel = DriverViewModel(repo)

        val initialList = viewModel.conversations.value
        assertTrue(initialList.isNotEmpty())

        val firstConv = initialList.first()
        viewModel.selectConversation(firstConv.id)
        assertEquals(firstConv.id, viewModel.uiState.value.selectedConversationId)

        val initialMsgCount = firstConv.messages.size
        viewModel.sendChatMessage("Je suis au point de rendez-vous", firstConv.id)

        val updatedConv = viewModel.conversations.value.first { it.id == firstConv.id }
        assertTrue(updatedConv.messages.size > initialMsgCount)
        assertEquals("Je suis au point de rendez-vous", updatedConv.messages.last().text)
    }

    @Test
    fun roomVehicleProfilePersistence() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = androidx.room.Room.inMemoryDatabaseBuilder(context, com.example.data.local.AppDatabase::class.java).build()
        val dao = db.vehicleDao()
        val repo = DriverRepository(dao)
        val viewModel = DriverViewModel(repo)

        viewModel.updateVehicleProfile(
            model = "Tesla Model 3 Grande Autonomie",
            plate = "AB-123-CD",
            color = "Blanc Nacré",
            category = "Aber Green",
            inspectionStatus = "Conforme & Validé",
            lastInspectionDate = "15/08/2026",
            nextInspectionDate = "15/08/2027",
            mileageKm = 38500,
            technicalNotes = "Pneus neufs, contrôle des batteries 100% OK"
        )

        assertEquals("Tesla Model 3 Grande Autonomie", viewModel.vehicleProfile.value.model)
        assertEquals("AB-123-CD", viewModel.vehicleProfile.value.plate)
        assertEquals("Conforme & Validé", viewModel.vehicleProfile.value.inspectionStatus)
        assertEquals(38500, viewModel.vehicleProfile.value.mileageKm)
        assertEquals("Tesla Model 3 Grande Autonomie", viewModel.profile.value.vehicleModel)
        assertEquals("AB-123-CD", viewModel.profile.value.vehiclePlate)

        db.close()
    }

    @Test
    fun weeklyEarningsGoalUpdateAndProgress() {
        val repo = DriverRepository(null)
        val viewModel = DriverViewModel(repo)

        assertEquals(1500.0, viewModel.profile.value.weeklyGoalEarnings, 0.01)

        viewModel.updateWeeklyGoal(2000.0)
        assertEquals(2000.0, viewModel.profile.value.weeklyGoalEarnings, 0.01)

        val weeklyEarnings = viewModel.profile.value.weeklyEarnings
        val progress = weeklyEarnings / viewModel.profile.value.weeklyGoalEarnings
        assertTrue(progress > 0.5) // ~1289.50 / 2000 = ~64%
    }

    @Test
    fun earningsReportTextGenerationWeeklyAndMonthly() {
        val profile = com.example.model.DriverProfile(
            name = "Alexandre Martin",
            vehicleModel = "Mercedes-Benz Classe E",
            vehiclePlate = "FX-892-AZ",
            rating = 4.96,
            weeklyEarnings = 1289.50,
            weeklyGoalEarnings = 1500.00
        )
        val records = listOf(
            com.example.model.EarningsRecord(
                id = "TRIP-801",
                timestamp = "14:20",
                passengerName = "Éric L.",
                pickup = "Aéroport CDG T2",
                dropoff = "Hôtel Le Bristol",
                baseFare = 45.0,
                surgeBonus = 12.5,
                tip = 5.0,
                platformFee = 9.0,
                netEarnings = 62.50,
                category = com.example.model.VehicleCategory.ABER_BERLINE,
                distanceKm = 28.4,
                durationMin = 38,
                paymentMethod = "Aber Business"
            )
        )

        val weeklyReport = com.example.ui.screens.generateEarningsReportText("WEEKLY", profile, records)
        assertTrue(weeklyReport.contains("RAPPORT HEBDOMADAIRE"))
        assertTrue(weeklyReport.contains("Alexandre Martin"))
        assertTrue(weeklyReport.contains("Mercedes-Benz Classe E"))
        assertTrue(weeklyReport.contains("Aéroport CDG T2"))
        assertTrue(weeklyReport.contains("1 289,50 €") || weeklyReport.contains("1289.50 €") || weeklyReport.contains("1289,50 €"))

        val monthlyReport = com.example.ui.screens.generateEarningsReportText("MONTHLY", profile, records)
        assertTrue(monthlyReport.contains("RAPPORT MENSUEL"))
        assertTrue(monthlyReport.contains("Hôtel Le Bristol"))
    }

    @Test
    fun testRideQuickFilters() {
        val repo = DriverRepository()
        
        // 1. Test short distance filter
        repo.setQuickFilter(com.example.model.RideQuickFilter.SHORT_DISTANCE)
        val shortOffer = repo.createSampleOffer(com.example.model.RideQuickFilter.SHORT_DISTANCE)
        assertTrue(shortOffer.dropoffDistanceKm < 6.0)

        // 2. Test airport / station filter
        repo.setQuickFilter(com.example.model.RideQuickFilter.AIRPORT)
        val airportOffer = repo.createSampleOffer(com.example.model.RideQuickFilter.AIRPORT)
        val isAirportOrGare = airportOffer.pickupAddress.contains("Aéroport", ignoreCase = true) ||
                airportOffer.dropoffAddress.contains("Aéroport", ignoreCase = true) ||
                airportOffer.pickupAddress.contains("Gare", ignoreCase = true) ||
                airportOffer.dropoffAddress.contains("Gare", ignoreCase = true)
        assertTrue(isAirportOrGare)

        // 3. Test surge filter
        repo.setQuickFilter(com.example.model.RideQuickFilter.SURGE_ONLY)
        val surgeOffer = repo.createSampleOffer(com.example.model.RideQuickFilter.SURGE_ONLY)
        assertTrue(surgeOffer.surgeMultiplier > 1.3)
    }
}

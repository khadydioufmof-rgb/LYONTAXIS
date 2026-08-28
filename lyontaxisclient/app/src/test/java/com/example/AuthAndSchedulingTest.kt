package com.example

import com.example.data.AberRepository
import com.example.model.VehicleCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlinx.coroutines.runBlocking

class AuthAndSchedulingTest {

  private lateinit var repository: AberRepository

  @Before
  fun setup() {
    repository = AberRepository.instance
    repository.logout()
    repository.clearIntermediateStops()
  }

  @Test
  fun otpRequestRequiresAnIdentifier() = runBlocking {
    assertFalse(repository.requestOtp("").isSuccess)
    repository.sendOtp("+33612345678")
  }

  @Test
  fun scheduledRideKeepsSelectedVehicleAndInstructions() {
    val scheduled = repository.scheduleRide(
      scheduledDate = "Demain",
      scheduledTime = "08:30",
      vehicle = VehicleCategory.TAXI_7_SEAT,
      specialInstructions = "Hall 2, sonner a l'interphone"
    )

    assertEquals(VehicleCategory.TAXI_7_SEAT, scheduled.vehicle)
    assertEquals("Hall 2, sonner a l'interphone", scheduled.specialInstructions)
    assertEquals(scheduled, repository.scheduledRides.value.first())
  }
}

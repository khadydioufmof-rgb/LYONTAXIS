package com.example

import com.example.data.AberRepository
import com.example.model.LocationPoint
import com.example.model.VehicleCategory
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class IntermediateStopsTest {

  private lateinit var repository: AberRepository

  @Before
  fun setup() {
    repository = AberRepository.instance
    repository.clearIntermediateStops()
    repository.clearPromo()
  }

  @Test
  fun testAddAndRemoveIntermediateStops() {
    val stop1 = LocationPoint("stop_1", "Starbucks Reserve", "330 N Wabash Ave", 1.2)
    val stop2 = LocationPoint("stop_2", "CVS Pharmacy", "205 N Michigan Ave", 1.8)

    repository.addIntermediateStop(stop1)
    assertEquals(1, repository.intermediateStops.value.size)
    assertEquals("Starbucks Reserve", repository.intermediateStops.value.first().title)

    repository.addIntermediateStop(stop2)
    assertEquals(2, repository.intermediateStops.value.size)

    repository.removeIntermediateStop("stop_1")
    assertEquals(1, repository.intermediateStops.value.size)
    assertEquals("CVS Pharmacy", repository.intermediateStops.value.first().title)

    repository.clearIntermediateStops()
    assertEquals(0, repository.intermediateStops.value.size)
  }

  @Test
  fun testDynamicPriceCalculationWithIntermediateStops() {
    repository.selectVehicle(VehicleCategory.JUST_GO)
    val stop1 = LocationPoint("stop_1", "Whole Foods Market", "255 E Grand Ave", 2.3)
    val stop2 = LocationPoint("stop_2", "Chase Bank", "10 S Dearborn St", 1.5)

    // Base fare with 0 stops
    val booking0 = repository.createBooking()
    val baseFare = VehicleCategory.JUST_GO.basePrice
    assertEquals(baseFare, booking0.fare, 0.01)
    assertEquals(0, booking0.intermediateStops.size)
    assertEquals(0.0, booking0.stopFee, 0.01)

    // Add 2 intermediate stops
    repository.addIntermediateStop(stop1)
    repository.addIntermediateStop(stop2)

    val bookingWithStops = repository.createBooking()
    assertEquals(2, bookingWithStops.intermediateStops.size)
    // 2 stops: 2 * $3.50 ($7.00) + detour distance fare (2 * 2.4km * $1.50 = $7.20)
    assertTrue(bookingWithStops.fare > baseFare)
    assertEquals(7.00, bookingWithStops.stopFee, 0.01)
    assertTrue(bookingWithStops.distanceKm > booking0.distanceKm)
  }

  @Test
  fun testMaxStopsLimit() {
    for (i in 1..6) {
      repository.addIntermediateStop(LocationPoint("stop_$i", "Stop $i", "Address $i", 1.0))
    }
    // Max 4 stops should be enforced
    assertEquals(4, repository.intermediateStops.value.size)
  }
}

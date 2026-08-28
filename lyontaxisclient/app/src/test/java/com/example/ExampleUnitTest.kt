package com.example

import com.example.data.AberRepository
import com.example.model.PaymentType
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testUpdateUserProfileContactInfo() {
    val repo = AberRepository.instance
    repo.updateUserProfile(
      name = "Claire Dupont",
      email = "claire.dupont@lyon.fr",
      phone = "+33 6 77 88 99 00",
      gender = "Femme",
      birthday = "22 Juillet 1994",
      emergencyContact = "Jean Dupont (+33 6 11 22 33 44)",
      homeAddress = "25 Rue Mercière, 69002 Lyon",
      avatarSeed = "Emma"
    )

    val profile = repo.userProfile.value
    assertEquals("Claire Dupont", profile.name)
    assertEquals("claire.dupont@lyon.fr", profile.email)
    assertEquals("+33 6 77 88 99 00", profile.phoneNumber)
    assertEquals("Femme", profile.gender)
    assertEquals("Jean Dupont (+33 6 11 22 33 44)", profile.emergencyContact)
    assertEquals("25 Rue Mercière, 69002 Lyon", profile.homeAddress)
    assertEquals("Emma", profile.avatarSeed)
  }

  @Test
  fun testManagePaymentMethods() {
    val repo = AberRepository.instance
    
    // Add new payment method as default
    repo.addPaymentMethod(
      type = PaymentType.MASTERCARD,
      title = "Mastercard Gold",
      subtitle = "**** **** **** 7744",
      isDefault = true
    )

    val methods = repo.paymentMethods.value
    val addedMethod = methods.find { it.title == "Mastercard Gold" }
    assertNotNull(addedMethod)
    assertTrue(addedMethod!!.isDefault)

    // Set another as default
    val visaMethod = methods.firstOrNull { it.type == PaymentType.VISA }
    if (visaMethod != null) {
      repo.setDefaultPaymentMethod(visaMethod.id)
      val updatedVisa = repo.paymentMethods.value.find { it.id == visaMethod.id }
      assertTrue(updatedVisa!!.isDefault)
    }

    // Delete added method
    repo.deletePaymentMethod(addedMethod.id)
    assertNull(repo.paymentMethods.value.find { it.id == addedMethod.id })
  }
}


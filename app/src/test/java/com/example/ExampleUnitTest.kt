package com.example

import com.example.model.CardBrand
import com.example.security.PaymentSecurityManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testLuhnCardValidation() {
        // Standard Visa test card
        assertTrue(PaymentSecurityManager.validateCardNumber("4242 4242 4242 4242"))
        // Invalid card number (fails checksum)
        assertFalse(PaymentSecurityManager.validateCardNumber("4242 4242 4242 4243"))
        // Short card number
        assertFalse(PaymentSecurityManager.validateCardNumber("1234"))
    }

    @Test
    fun testCardBrandDetection() {
        assertEquals(CardBrand.VISA, PaymentSecurityManager.detectCardBrand("4242424242424242"))
        assertEquals(CardBrand.MASTERCARD, PaymentSecurityManager.detectCardBrand("5500000000000004"))
        assertEquals(CardBrand.AMEX, PaymentSecurityManager.detectCardBrand("378282246310005"))
        assertEquals(CardBrand.DISCOVER, PaymentSecurityManager.detectCardBrand("6011000000000000"))
    }

    @Test
    fun testExpiryValidation() {
        assertTrue(PaymentSecurityManager.validateExpiry("12/30"))
        assertFalse(PaymentSecurityManager.validateExpiry("15/30")) // Invalid month
        assertFalse(PaymentSecurityManager.validateExpiry("01/20")) // Past year
    }

    @Test
    fun testSecureTokenGeneration() {
        val token = PaymentSecurityManager.generateSecureToken("TEST_PAYLOAD")
        assertTrue(token.startsWith("tok_pci_e2ee_"))
        assertEquals(29, token.length)
    }
}

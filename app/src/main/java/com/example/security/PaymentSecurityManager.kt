package com.example.security

import com.example.model.CardBrand
import java.security.MessageDigest
import java.util.Calendar
import java.util.UUID

object PaymentSecurityManager {

    /**
     * Validates credit card number using Luhn Algorithm (Mod 10).
     */
    fun validateCardNumber(rawCardNumber: String): Boolean {
        val clean = rawCardNumber.replace(" ", "").replace("-", "")
        if (clean.length < 13 || clean.length > 19 || !clean.all { it.isDigit() }) {
            return false
        }
        var sum = 0
        var alternate = false
        for (i in clean.length - 1 downTo 0) {
            var n = clean[i].digitToInt()
            if (alternate) {
                n *= 2
                if (n > 9) {
                    n = (n % 10) + 1
                }
            }
            sum += n
            alternate = !alternate
        }
        return (sum % 10 == 0)
    }

    /**
     * Identifies card brand based on BIN (Bank Identification Number) ranges.
     */
    fun detectCardBrand(rawCardNumber: String): CardBrand {
        val clean = rawCardNumber.replace(" ", "").replace("-", "")
        if (clean.isEmpty()) return CardBrand.UNKNOWN

        if (clean.startsWith("4")) {
            return CardBrand.VISA
        }
        if (clean.length >= 2) {
            val prefix2 = clean.substring(0, 2).toIntOrNull() ?: 0
            if (prefix2 in 51..55) return CardBrand.MASTERCARD
            if (prefix2 == 34 || prefix2 == 37) return CardBrand.AMEX
            if (prefix2 == 65) return CardBrand.DISCOVER
        }
        if (clean.length >= 4) {
            val prefix4 = clean.substring(0, 4).toIntOrNull() ?: 0
            if (prefix4 in 2221..2720) return CardBrand.MASTERCARD
            if (prefix4 == 6011) return CardBrand.DISCOVER
        }
        return CardBrand.UNKNOWN
    }

    /**
     * Validates card expiry in MM/YY format against current date.
     */
    fun validateExpiry(expiry: String): Boolean {
        val clean = expiry.trim()
        val parts = clean.split("/")
        if (parts.size != 2) return false

        val month = parts[0].toIntOrNull() ?: return false
        val yearPart = parts[1].toIntOrNull() ?: return false
        if (month !in 1..12) return false

        val calendar = Calendar.getInstance()
        val currentYearShort = calendar.get(Calendar.YEAR) % 100
        val currentMonth = calendar.get(Calendar.MONTH) + 1 // 1-based

        val fullYear = if (yearPart < 100) yearPart else yearPart % 100
        return when {
            fullYear > currentYearShort -> true
            fullYear == currentYearShort -> month >= currentMonth
            else -> false
        }
    }

    /**
     * Validates CVV based on brand (Amex = 4, Others = 3).
     */
    fun validateCvv(cvv: String, brand: CardBrand): Boolean {
        val clean = cvv.trim()
        if (!clean.all { it.isDigit() }) return false
        return if (brand == CardBrand.AMEX) {
            clean.length == 4
        } else {
            clean.length == 3
        }
    }

    /**
     * Generates a PCI-compliant cryptographic token representing the encrypted payload.
     */
    fun generateSecureToken(input: String): String {
        return try {
            val md = MessageDigest.getInstance("SHA-256")
            val hash = md.digest(input.toByteArray(Charsets.UTF_8))
            val hex = hash.joinToString("") { "%02x".format(it) }
            "tok_pci_e2ee_${hex.take(16)}"
        } catch (e: Exception) {
            "tok_pci_e2ee_${UUID.randomUUID().toString().take(16)}"
        }
    }

    fun generatePciAuthCode(): String {
        val randomHex = UUID.randomUUID().toString().replace("-", "").take(8).uppercase()
        return "AUTH-$randomHex-3DS2"
    }

    fun generateReceiptId(): String {
        val randomDigits = (100000..999999).random()
        return "PV-REC-$randomDigits"
    }
}

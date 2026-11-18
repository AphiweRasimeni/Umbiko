package com.umbiko.app.auth

import com.umbiko.app.ui.screens.auth.AuthValidation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Unit tests for client-side registration validation (FR-01). Run in CI by GitHub Actions. */
class AuthValidationTest {

    private fun validate(
        name: String = "Thato Mokoena",
        email: String = "thato@example.com",
        mobile: String = "0821234567",
        area: String = "area-1",
        password: String = "Passw0rd!",
    ) = AuthValidation.validateRegistration(name, email, mobile, area, password)

    @Test fun validRegistration_returnsNull() = assertNull(validate())

    @Test fun internationalMobileFormat_isAccepted() = assertNull(validate(mobile = "+27821234567"))

    @Test fun shortName_isRejected() = assertEquals("Enter your full name", validate(name = "T"))

    @Test fun badEmail_isRejected() = assertEquals("Enter a valid email address", validate(email = "not-an-email"))

    @Test fun badMobile_isRejected() =
        assertEquals("Enter a valid South African mobile number", validate(mobile = "12345"))

    @Test fun missingArea_isRejected() = assertEquals("Choose your area", validate(area = ""))

    @Test fun weakPasswords_areRejected() {
        val message = "Password needs 8+ characters, an uppercase letter, a digit and a symbol"
        assertEquals(message, validate(password = "Short1!"))        // too short
        assertEquals(message, validate(password = "password1!"))     // no uppercase
        assertEquals(message, validate(password = "Password!!"))     // no digit
        assertEquals(message, validate(password = "Password12"))     // no symbol
    }
}

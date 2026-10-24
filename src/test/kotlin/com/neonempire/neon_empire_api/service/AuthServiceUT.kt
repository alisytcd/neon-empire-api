package com.neonempire.neon_empire_api.service

import com.neonempire.neon_empire_api.repository.UserRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.security.crypto.password.PasswordEncoder

class AuthServiceUT {

    private val userRepository : UserRepository = mock()
    private val passwordEncoder : PasswordEncoder = mock()
    private val jwtService : JWTService = mock()

    private val authService = AuthService(userRepository, passwordEncoder, jwtService)

    @Test
    fun `authenticateUser returns null when user is not found`() {

        val userEmail = "test@123.com"
        val password = "abcd123"

        `when`(userRepository.findByEmail(userEmail)).thenReturn(null)

        val authResult = authService.authenticateUser(userEmail,password)

        assertEquals(null,authResult)
    }

    @Test
    fun `authenticateUser returns null when password doesn't match database`(){

        val userEmail = "test@123.com"
        val password = "abcd123"
        val encodedPassword = "abcd123"

        `when`(passwordEncoder.matches(password,encodedPassword)).thenReturn(false)

        val authResult = authService.authenticateUser(userEmail, password)

        assertEquals(null,authResult)
    }
}
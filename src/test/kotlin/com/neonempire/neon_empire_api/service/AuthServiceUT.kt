package com.neonempire.neon_empire_api.service

import com.neonempire.neon_empire_api.model.User
import com.neonempire.neon_empire_api.repository.UserRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.kotlin.verifyNoInteractions
import org.springframework.security.crypto.password.PasswordEncoder
import java.util.UUID

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

        verifyNoInteractions(passwordEncoder)

        verifyNoInteractions(jwtService)

        assertEquals(null,authResult)
    }

    @Test
    fun `authenticateUser returns null when user credentials are invalid`(){

        val userEmail = "test@123.com"

        val password = "abcd123"
        //encodedPassword isnt' a real Bcrypt Hash but since we're just testing it should be fine - testing hashes etc isn't the purpose of this test
        val encodedPassword = "abcd1234"

        val userGuid = UUID.randomUUID()

        val user = User(userGuid,userEmail,encodedPassword)

        `when`(userRepository.findByEmail(userEmail)).thenReturn(user)

        `when`(passwordEncoder.matches(password,encodedPassword)).thenReturn(false)

        val authResult = authService.authenticateUser(userEmail, password)

        //verifying that the passwordEncoder.matches method was called once
        verify(passwordEncoder.matches(password,encodedPassword))

        //we also want to ensure that the jwtService was never called to ensure our guarding logic works
        verifyNoInteractions(jwtService)

        assertEquals(null,authResult)
    }

    @Test
    fun `authenticateUser returns token when user credentials are valid`(){

        val userEmail = "test@123.com"

        val password = "abcd1234"
        //encodedPassword isnt' a real Bcrypt Hash but since we're just testing it should be fine - testing hashes etc isn't the purpose of this test
        val encodedPassword = "abcd1234"

        val userGuid = UUID.randomUUID()

        val user = User(userGuid,userEmail,encodedPassword)

        val jwtToken = "MyJwtTokenValue"

        `when`(userRepository.findByEmail(userEmail)).thenReturn(user)

        `when`(passwordEncoder.matches(password,encodedPassword)).thenReturn(true)

        `when`(jwtService.generateToken(userGuid)).thenReturn(jwtToken)

        val authResult = authService.authenticateUser(userEmail, password)

        verify(userRepository.findByEmail(userEmail))

        verify(passwordEncoder.matches(password,encodedPassword))

        verify(jwtService.generateToken(userGuid))

        assertEquals(jwtToken,authResult)

    }
}
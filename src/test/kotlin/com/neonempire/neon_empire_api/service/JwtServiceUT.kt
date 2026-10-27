package com.neonempire.neon_empire_api.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify
import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.JwtEncoderParameters
import java.util.UUID
import java.time.Duration
import java.time.Instant
import org.junit.jupiter.api.Assertions.assertTrue

class JwtServiceUT
{
    private val jwtEncoder : JwtEncoder = mock()
    private val jwtIssuer = "neon-empire-api"
    private val jwtService = JWTService(jwtEncoder, jwtIssuer)

    @Test
    fun `generateToken successfully creates a correct jwt token`(){

        val userID = UUID.randomUUID()

        val mockedToken : Jwt = mock()

        val expectedToken = "someTokenValue"

        `when`(jwtEncoder.encode(any())).thenReturn(mockedToken)

        `when`(mockedToken.tokenValue).thenReturn(expectedToken)

        val actualToken = jwtService.generateToken(userID)

        val paramCaptor = argumentCaptor<JwtEncoderParameters>()

        verify(jwtEncoder).encode(paramCaptor.capture())


        val params = paramCaptor.firstValue

        val jwsHeader = params.jwsHeader
        val claims= params.claims

        assertEquals(MacAlgorithm.HS256, jwsHeader?.algorithm)
        assertEquals(userID.toString(),claims.subject)
        assertEquals(jwtIssuer,claims.getClaimAsString("iss"))


        // Verify issuedAt is approximately the current time
        val issuedAt = claims.issuedAt

        assertTrue(
            Duration.between(issuedAt, Instant.now()).abs().seconds < 5
        )

        // Verify token expires exactly 15 minutes after issuance
        val expiresAt = claims.expiresAt
        assertEquals(
            900L,
            Duration.between(issuedAt, expiresAt).seconds
        )


        assertEquals(expectedToken,actualToken)

    }

}
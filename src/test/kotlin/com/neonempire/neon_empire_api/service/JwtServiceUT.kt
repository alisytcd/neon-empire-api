package com.neonempire.neon_empire_api.service

import com.neonempire.neon_empire_api.model.User
import com.nimbusds.jose.jwk.source.ImmutableSecret
import com.nimbusds.jose.proc.SecurityContext
import org.junit.jupiter.api.Assertions.assertEquals
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
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.springframework.security.oauth2.jwt.JwtValidators
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder
import java.security.SecureRandom
import java.time.temporal.Temporal
import javax.crypto.spec.SecretKeySpec

class JwtServiceUT
{   //mocking dependencies
    private val jwtEncoder : JwtEncoder = mock()
    private val jwtIssuer = "neon-empire-api"

    //creating the actual service with mocked dependencies
    private val jwtService = JWTService(jwtEncoder, jwtIssuer)

    @ParameterizedTest
    @EnumSource(User.Role::class)
    fun `generateToken successfully creates a jwt token with correct claims and header`(userRole : User.Role){

        val userID = UUID.randomUUID()

        val mockedToken : Jwt = mock()

        val expectedToken = "someTokenValue"

        //arranging for the mocks to return the expected values
        `when`(jwtEncoder.encode(any())).thenReturn(mockedToken)

        //ensuring that the returned token value from the generateToken's returned string value
        //is the expected token we've defined above
        `when`(mockedToken.tokenValue).thenReturn(expectedToken)

        val actualToken = jwtService.generateToken(userID,userRole)

        //using argument captor to capture the parameters that the internal functions such as encode() in
        // jwt service's generateToken method ran with - to ensure it actually encoded the token with the correct values

        val paramCaptor = argumentCaptor<JwtEncoderParameters>()

        verify(jwtEncoder).encode(paramCaptor.capture())


        val params = paramCaptor.firstValue

        val jwsHeader = params.jwsHeader
        val claims= params.claims

        assertEquals(MacAlgorithm.HS256, jwsHeader?.algorithm)
        assertEquals(userID.toString(),claims.subject)
        assertEquals(jwtIssuer,claims.getClaimAsString("iss"))
        assertEquals(userRole.name,claims.getClaimAsString("role"))


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

    @ParameterizedTest
    @EnumSource(User.Role::class)
    fun `generateToken successfully signs a JWT token`(userRole: User.Role){

        //creating byteArray
        val keyBytes = ByteArray(32)
        //populating it with random bytes
        SecureRandom().nextBytes(keyBytes)

        //creating encoder
        val secretKeySpec = SecretKeySpec(keyBytes,"HmacSHA256")
        val secret = ImmutableSecret<SecurityContext>(secretKeySpec)

        val encoder = NimbusJwtEncoder(secret)

        //creating decoder
        val decoder =  NimbusJwtDecoder.withSecretKey(secretKeySpec).macAlgorithm(MacAlgorithm.HS256).build()
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(jwtIssuer))

        //creating actual JWT service with encoder we created above
        val localJwtService = JWTService(encoder,jwtIssuer)

        val userId = UUID.randomUUID()

        val token = localJwtService.generateToken(userId,userRole)

        //validating the generated jwt token from above using our decoder
        val validatedJwt = decoder.decode(token)

        //ensuring that the claims and headers were successfully encoded and decoded and verifying via assertions
        val claims = validatedJwt.claims
        val headers = validatedJwt.headers


        assertEquals("HS256", headers["alg"])
        assertEquals(userId.toString(), claims["sub"])
        assertEquals(jwtIssuer, claims["iss"])
        assertEquals(userRole.name,claims["role"])



        val issuedAt = validatedJwt.issuedAt
        val expiresAt = validatedJwt.expiresAt

        assertNotNull(issuedAt)
        assertNotNull(expiresAt)

        // Verify issuedAt is approximately the current time
        assertTrue(
            Duration.between(issuedAt, Instant.now()).abs().seconds < 5
        )

        // Verify token expires exactly 15 minutes after issuance
        assertEquals(
            900L,
            Duration.between(issuedAt, expiresAt as Temporal?).seconds
        )
    }

}
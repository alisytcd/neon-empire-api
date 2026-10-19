package com.neonempire.neon_empire_api.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.JwsHeader
import org.springframework.security.oauth2.jwt.JwtClaimsSet
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.JwtEncoderParameters
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID

@Service
class JWTService (private val jwtEncoder : JwtEncoder,
          @Value("\${JWT_ISSUER") private val jwtIssuer : String) {

    fun generateToken(userId : UUID) : String {

        val claimsBuilder = JwtClaimsSet.builder()

        claimsBuilder.subject(userId.toString())

        claimsBuilder.issuer(jwtIssuer)

        val now = Instant.now()

        claimsBuilder.issuedAt(now)

        claimsBuilder.expiresAt(now.plusSeconds(15 * 60))

        val claimsSet = claimsBuilder.build()

        val jwsBuilder = JwsHeader.with(MacAlgorithm.HS256)

        val jwsHeader = jwsBuilder.build()

        val parameters = JwtEncoderParameters.from(jwsHeader,claimsSet)

        return jwtEncoder.encode(parameters).tokenValue
    }
}
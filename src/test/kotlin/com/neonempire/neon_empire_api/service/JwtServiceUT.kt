package com.neonempire.neon_empire_api.service

import org.mockito.Mockito.mock
import org.springframework.security.oauth2.jwt.JwtEncoder

class JwtServiceUT
{
    private val jwtEncoder : JwtEncoder = mock()
    private val jwtIssuer = "neon-empire-api"


}
package com.neonempire.neon_empire_api.service

import com.neonempire.neon_empire_api.repository.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthService (
    private val userRepository : UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JWTService
    )
{
    fun authenticateUser(userEmail: String, password: String): String? {

        val user = userRepository.findByEmail(userEmail) ?: return null

        if(passwordEncoder.matches(password, user.password)){
            return jwtService.generateToken(user.id!!)
        }

        return null
    }
}
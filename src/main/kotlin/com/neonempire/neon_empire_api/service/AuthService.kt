package com.neonempire.neon_empire_api.service

import com.neonempire.neon_empire_api.repository.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthService (
    private val userRepository : UserRepository,
    private val passwordEncoder: PasswordEncoder
    )
{
    fun authenticateUser(userEmail: String, password: String): Boolean {

        val user = userRepository.findByEmail(userEmail) ?: return false

        return passwordEncoder.matches(password, user.password)

    }
}
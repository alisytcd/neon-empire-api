package com.neonempire.neon_empire_api.repository

import com.neonempire.neon_empire_api.model.User
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<User, Long> {

    fun findByEmail(email : String) : User?
}
package com.neonempire.neon_empire_api.repository

import com.neonempire.neon_empire_api.model.User
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface UserRepository : JpaRepository<User, UUID> {

    fun findByEmail(email : String) : User?
}
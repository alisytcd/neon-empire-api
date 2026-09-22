package com.neonempire.neon_empire_api.model

import java.time.LocalDate
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id

@Entity
data class Show (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    var venue: String,
    var city: String,
    var date: LocalDate
    )
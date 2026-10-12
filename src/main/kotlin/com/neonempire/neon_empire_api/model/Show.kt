package com.neonempire.neon_empire_api.model

import jakarta.persistence.Column
import java.time.LocalDate
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name="shows")
class Show (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    var venue: String,

    @Column(nullable = false)
    var city: String,

    @Column(nullable = false)
    var date: LocalDate
    )
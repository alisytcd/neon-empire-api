package com.neonempire.neon_empire_api.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.LocalDate

data class CreateShowRequest (

    @field:NotBlank
    val venue: String,

    @field:NotBlank
    val city: String,

    @field:NotNull
    val date: LocalDate?

)

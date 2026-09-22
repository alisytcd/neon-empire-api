package com.neonempire.neon_empire_api.dto

import jakarta.validation.constraints.FutureOrPresent
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.LocalDate

data class UpdateShowRequestDTO (

    @field:NotBlank(message = "The 'venue' provided in the input is invalid.")
    val venue: String,

    @field:NotBlank(message = "The 'city' provided in the input is invalid.")
    val city: String,

    @field:NotNull(message = "The 'date' is required..")
    @field:FutureOrPresent(message = "The 'date' provided in the input must be a present of future date.")
    val date: LocalDate?

)
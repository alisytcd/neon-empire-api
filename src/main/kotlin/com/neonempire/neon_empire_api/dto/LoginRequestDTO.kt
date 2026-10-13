package com.neonempire.neon_empire_api.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class LoginRequestDTO (
    @field:NotBlank(message = "Please enter an email.")
    @field:Email(message = "Please enter a valid email.")
    val email: String,

    @field:NotBlank(message = "Please enter a password.")
    val password: String
)



package com.neonempire.neon_empire_api.controller

import com.neonempire.neon_empire_api.dto.LoginRequestDTO
import com.neonempire.neon_empire_api.dto.LoginResponseDTO
import com.neonempire.neon_empire_api.service.AuthService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class AuthController (private val authService : AuthService){


    @PostMapping("/auth/login")
        fun userLogin(@Valid @RequestBody loginRequest : LoginRequestDTO) : ResponseEntity<LoginResponseDTO> {

            //createJWT token -> populate responseDTO and return it, otherwise return 401

            val token = authService.authenticateUser(loginRequest.email,loginRequest.password)

            if(token!=null){
                val responseDTO = LoginResponseDTO(token,"Bearer")
                return ResponseEntity.ok(responseDTO)

            }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

    }

}

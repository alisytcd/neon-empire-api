package com.neonempire.neon_empire_api.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException ( exception: MethodArgumentNotValidException): ResponseEntity<Map<String, Any>> {

        val fieldErrors = exception.bindingResult.fieldErrors

        val errors = fieldErrors.associate {
            it.field to (it.defaultMessage ?: "Invalid value")
        }

        val response = mapOf(
            "status" to 400,
            "error" to "Validation failed",
            "fields" to errors
        )

        val responseBody = ResponseEntity(response, HttpStatus.BAD_REQUEST)

        return responseBody
    }
}
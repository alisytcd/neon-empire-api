package com.neonempire.neon_empire_api.integration

import com.neonempire.neon_empire_api.model.User
import com.neonempire.neon_empire_api.repository.UserRepository
import org.hamcrest.Matchers.isEmptyOrNullString
import org.hamcrest.Matchers.not
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import tools.jackson.databind.ObjectMapper
import kotlin.test.assertEquals


@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")

class AuthenticationIT {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var passwordEncoder: PasswordEncoder

    @Autowired
    lateinit var jwtDecoder : JwtDecoder

    @BeforeEach
    fun cleanDatabase() {
        userRepository.deleteAll()
    }


    @Test
    fun `successful user login returns a 200 OK and valid JWT token`(){

        val userEmail = "user123@test.com"
        val userPassword = "abcd123!"

        val encodedPassword = passwordEncoder.encode(userPassword)

        val requestLoginJson = """
            {
                "email" : "user123@test.com",
                "password" : "abcd123!"
            }
        """.trimIndent()

        val user = userRepository.save(User(null,userEmail,encodedPassword!!))

        val userGuid = user.id.toString();


        val response = mockMvc.perform(post("/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestLoginJson))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.tokenType").value("Bearer"))
            .andExpect(jsonPath("$.accessToken").value(not(isEmptyOrNullString())))
            .andReturn()

        val responseAsString = response.response.contentAsString
        val objectMapper = ObjectMapper()
        val jsonResponse = objectMapper.readTree(responseAsString)
        val accessToken = jsonResponse["accessToken"].asText()
        val decodedToken = jwtDecoder.decode(accessToken)


        assertEquals(userGuid, decodedToken.subject)

    }

    @Test
    fun `unsuccessful login returns a 401 UNAUTHORIZED response`() {

        val userEmail = "user123@test.com"
        val userPassword = "abcd123!"

        val encodedPassword = passwordEncoder.encode(userPassword)

        val requestLoginJson = """
            {
                "email" : "user123@test.com",
                "password" : "abcd12"
            }
        """.trimIndent()

        userRepository.save(User(null,userEmail,encodedPassword!!))

        mockMvc.perform(post("/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestLoginJson))
            .andExpect(status().isUnauthorized)
            .andExpect(content().string(""))

    }


}
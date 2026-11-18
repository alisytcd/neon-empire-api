package com.neonempire.neon_empire_api.integration

import com.jayway.jsonpath.JsonPath
import com.neonempire.neon_empire_api.model.User
import com.neonempire.neon_empire_api.repository.UserRepository
import org.hamcrest.Matchers.isEmptyOrNullString
import org.hamcrest.Matchers.not
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
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


    @ParameterizedTest
    @EnumSource(User.Role::class)
    fun `successful user login returns a 200 OK and valid JWT token with appropriate role`(userRole: User.Role){

        val userEmail = "user123@test.com"
        val userPassword = "abcd123!"

        val encodedPassword = passwordEncoder.encode(userPassword)

        val requestLoginJson = """
            {
                "email" : "user123@test.com",
                "password" : "abcd123!"
            }
        """.trimIndent()

        val user = userRepository.save(User(null,userEmail,encodedPassword!!,userRole))

        val userGuid = user.id.toString();


        val response = mockMvc.perform(post("/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestLoginJson))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.tokenType").value("Bearer"))
            .andExpect(jsonPath("$.accessToken").value(not(isEmptyOrNullString())))
            .andReturn()

        val accessToken = JsonPath.read<String>(response.response.contentAsString,"$.accessToken")
        val decodedToken = jwtDecoder.decode(accessToken)

        assertEquals(userGuid, decodedToken.subject)
        assertEquals(userRole.name,decodedToken.claims["role"])

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

    @Test
    fun `unsuccessful login with email that doesn't exist in db returns 401 UNAUTHORIZED response`() {

        val requestLoginJson = """
            {
                "email" : "user123456@test.com",
                "password" : "abcd12"
            }
        """.trimIndent()

        mockMvc.perform(post("/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestLoginJson))
            .andExpect(status().isUnauthorized)
            .andExpect(content().string(""))

    }

    @Test
    fun `unsuccessful login with empty email returns error message and 400 BAD REQUEST`() {

        val requestLoginJson = """
            {
                "email" : "",
                "password" : "abcd12"
            }
        """.trimIndent()

        mockMvc.perform(post("/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestLoginJson))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.fields.email").value("Please enter an email."))

    }

    @Test
    fun `unsuccessful login with invalid email format returns error message and 400 BAD REQUEST`() {

        val requestLoginJson = """
            {
                "email" : "testuser222",
                "password" : "abcd12"
            }
        """.trimIndent()

        mockMvc.perform(post("/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestLoginJson))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.fields.email").value("Please enter a valid email."))

    }

    @Test
    fun `unsuccessful login with empty password returns error message and 400 BAD REQUEST`() {

        val requestLoginJson = """
            {
                "email" : "testuser222@email.com",
                "password" : ""
            }
        """.trimIndent()

        mockMvc.perform(post("/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestLoginJson))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.fields.password").value("Please enter a password."))

    }

}
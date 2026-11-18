package com.neonempire.neon_empire_api.integration

import com.neonempire.neon_empire_api.model.Show
import com.neonempire.neon_empire_api.model.User
import com.neonempire.neon_empire_api.repository.ShowRepository
import com.neonempire.neon_empire_api.service.JWTService
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import java.time.LocalDate
import java.util.UUID
import kotlin.test.assertEquals


@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ShowIntegrationIT {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var showRepository: ShowRepository

    @Autowired
    lateinit var jwtService : JWTService

    @BeforeEach
    fun cleanDatabase() {
        showRepository.deleteAll()
    }

    private fun generateToken(userRole : User.Role) : String {
        val uuid = UUID.randomUUID()
        //create a jwt token with user or admin permissions
        return jwtService.generateToken(uuid, userRole)
    }

    @Test
    fun `GET all shows returns 200 and an empty list when no shows exist in DB`(){
        mockMvc.perform(get("/content/shows"))
            .andExpect(status().isOk)
            .andExpect(content().json("[]"))
    }

    @Test
    fun `GET all shows returns 200 and all shows that exist in DB`(){

        showRepository.saveAll(
            listOf(
                Show(
                    venue = "The Academy",
                    city = "Dublin",
                    date = LocalDate.of(2030, 1, 1)
                ),
                Show(
                    venue = "Fred Zeppelins",
                    city = "Cork",
                    date = LocalDate.of(2030, 3, 1)
                )
            )
        )

        mockMvc.perform(get("/content/shows"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].venue").value("The Academy"))
            .andExpect(jsonPath("$[0].city").value("Dublin"))
            .andExpect(jsonPath("$[0].date").value("2030-01-01"))
            .andExpect(jsonPath("$[1].venue").value("Fred Zeppelins"))
            .andExpect(jsonPath("$[1].city").value("Cork"))
            .andExpect(jsonPath("$[1].date").value("2030-03-01"))

    }

    @Test
    fun `user with admin role can POST show which returns a 200 status and successfully creates a show that persists in the DB`(){

        //create a jwt token with admin permissions
        val jwtToken = generateToken(User.Role.ADMIN)

        val requestShowJson = """
            {
                "venue" : "The Academy",
                "city" : "Dublin",
                "date" : "2030-03-01"
            }
        """.trimIndent()

        mockMvc.perform(post("/content/shows").header("Authorization", "Bearer $jwtToken")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestShowJson))
            .andExpect(status().isOk)
            .andExpect(jsonPath("venue").value("The Academy"))
            .andExpect(jsonPath("city").value("Dublin"))
            .andExpect(jsonPath("date").value("2030-03-01"))

        val showsInDatabase = showRepository.findAll()

        assertEquals(1,showsInDatabase.size)
        assertNotNull(showsInDatabase[0].id)
        assertEquals("The Academy",showsInDatabase[0].venue)
        assertEquals("Dublin", showsInDatabase[0].city)
        assertEquals(LocalDate.of(2030, 3, 1),showsInDatabase[0].date)
    }


    @Test
    fun `user with USER role can't make successful requests to POST show endpoint and receives a 403 status`(){

        val jwtToken = generateToken(User.Role.USER)

        val requestShowJson = """
            {
                "venue" : "The Academy",
                "city" : "Dublin",
                "date" : "2030-03-01"
            }
        """.trimIndent()

        mockMvc.perform(post("/content/shows").header("Authorization", "Bearer $jwtToken")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestShowJson))
            .andExpect(status().isForbidden)

        //making sure nothing was saved in db
        assertEquals(0,showRepository.count())
    }

    @Test
    fun `POST show with invalid parameters produces a 400 Bad Request and nothing persists in DB`(){

        val jwtToken = generateToken( User.Role.ADMIN)

        val requestShowJson = """
            {
                "venue" : "",
                "city" : "Dublin",
                "date" : "2030-03-01"
            }
        """.trimIndent()

        mockMvc.perform(post("/content/shows").header("Authorization", "Bearer $jwtToken")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestShowJson))
            .andExpect(status().isBadRequest)


        assertEquals(0,showRepository.count())
    }

    @Test
    fun `a user with admin role calls PUT show successfully which returns a 200 and modifies the show and persists the change in the DB`(){

        val jwtToken = generateToken(User.Role.ADMIN)

        val show = showRepository.save(
                Show(
                venue = "The Academy",
                city = "Dublin",
                date = LocalDate.of(2030, 1, 1)
        ))

        val savedShowId = show.id!!

        val requestShowJson = """
            {
                "venue" : "The Olympia",
                "city" : "Dublin",
                "date" : "2030-03-01"
            }
        """.trimIndent()

        mockMvc.perform(put("/content/shows/${savedShowId}").header("Authorization", "Bearer $jwtToken")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestShowJson))
            .andExpect( status().isOk)
            .andExpect(jsonPath("venue").value("The Olympia"))
            .andExpect(jsonPath("date").value("2030-03-01"))

        val modifiedShow = showRepository.findAll()[0]

        assertEquals("The Olympia", modifiedShow.venue)
        assertEquals(LocalDate.of(2030, 3, 1),modifiedShow.date)

    }

    @Test
    fun `a user with user role can't successfully make requests to the PUT show endpoint and receives a 403`(){

        val jwtToken = generateToken(User.Role.USER)

        val show = showRepository.save(
            Show(
                venue = "The Academy",
                city = "Dublin",
                date = LocalDate.of(2030, 1, 1)
            ))

        val savedShowId = show.id!!

        val requestShowJson = """
            {
                "venue" : "The Olympia",
                "city" : "Dublin",
                "date" : "2030-03-01"
            }
        """.trimIndent()

        mockMvc.perform(put("/content/shows/${savedShowId}").header("Authorization", "Bearer $jwtToken")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestShowJson))
            .andExpect( status().isForbidden)

        val savedShow = showRepository.findById(savedShowId).orElseThrow()

        assertEquals(savedShowId,savedShow.id)
        assertEquals("The Academy", savedShow.venue)
        assertEquals("Dublin",savedShow.city)
        assertEquals(LocalDate.of(2030, 1, 1),savedShow.date)

    }

    @Test
    fun `PUT show with invalid parameters returns 400 and leaves existing show unchanged`(){

        val jwtToken = generateToken(User.Role.ADMIN)

        val show = showRepository.save(
            Show(
                venue = "The Academy",
                city = "Dublin",
                date = LocalDate.of(2030, 1, 1)
            ))

        val savedShowId = show.id!!

        val requestShowJson = """
            {
                "venue" : "",
                "city" : "Dublin",
                "date" : "2030-03-01"
            }
        """.trimIndent()

        mockMvc.perform(put("/content/shows/${savedShowId}").header("Authorization", "Bearer $jwtToken")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestShowJson))
            .andExpect( status().isBadRequest)

        val savedShow = showRepository.findById(savedShowId).orElseThrow()

        assertEquals(savedShowId,savedShow.id)
        assertEquals("The Academy", savedShow.venue)
        assertEquals("Dublin",savedShow.city)
        assertEquals(LocalDate.of(2030, 1, 1),savedShow.date)

    }

    @Test
    fun `PUT show with an id that doesn't exist in the DB produces a 404 and nothing persists in the DB`(){

        val jwtToken = generateToken(User.Role.ADMIN)

        val requestShowJson = """
            {
                "venue" : "The Olympia",
                "city" : "Dublin",
                "date" : "2030-03-01"
            }
        """.trimIndent()

        mockMvc.perform(put("/content/shows/9999").header("Authorization", "Bearer $jwtToken")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestShowJson))
            .andExpect( status().isNotFound)

        assertEquals(0,showRepository.count())
    }

    @Test
    fun `a user with admin role calls DELETE show endpoint successfully and deletes a show with a 204 response and show is deleted in DB`(){

        val jwtToken = generateToken(User.Role.ADMIN)

        val show = showRepository.save(
            Show(
                venue = "The Academy",
                city = "Dublin",
                date = LocalDate.of(2030, 1, 1)
            ))

        val savedShowId = show.id!!

        mockMvc.perform(delete("/content/shows/${savedShowId}")
            .header("Authorization", "Bearer $jwtToken"))
            .andExpect( status().isNoContent)

        assertEquals(0,showRepository.count())
        assertFalse(showRepository.existsById(savedShowId))

    }

    @Test
    fun `a user with user role can't successfully call DELETE show endpoint and received 403 response with the show still persisting in db`(){

        val jwtToken = generateToken(User.Role.USER)

        val show = showRepository.save(
            Show(
                venue = "The Academy",
                city = "Dublin",
                date = LocalDate.of(2030, 1, 1)
            ))

        val savedShowId = show.id!!

        mockMvc.perform(delete("/content/shows/${savedShowId}")
            .header("Authorization", "Bearer $jwtToken"))
            .andExpect( status().isForbidden)

        assertEquals(1,showRepository.count())
        assertTrue(showRepository.existsById(savedShowId))

    }

    @Test
    fun `DELETE show returns 404 when request is sent to delete a non-existent show`(){
        
        val jwtToken = generateToken(User.Role.ADMIN)

        val show = showRepository.save(
            Show(
                venue = "The Academy",
                city = "Dublin",
                date = LocalDate.of(2030, 1, 1)
            ))

        mockMvc.perform(delete("/content/shows/9999")
            .header("Authorization", "Bearer $jwtToken"))
            .andExpect( status().isNotFound)

        assertEquals(1, showRepository.count())
        assertTrue(showRepository.existsById(show.id!!))

    }

    @Test
    fun `unauthenticated user cannot POST show and receives 401`() {

        val requestShowJson = """
        {
            "venue": "The Academy",
            "city": "Dublin",
            "date": "2030-03-01"
        }
    """.trimIndent()

        mockMvc.perform(
            post("/content/shows")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestShowJson)
        )
            .andExpect(status().isUnauthorized)

        assertEquals(0, showRepository.count())
    }

    @Test
    fun `unauthenticated user cannot PUT show and receives 401 with existing show unchanged`() {

        val show = showRepository.save(
            Show(
                venue = "The Academy",
                city = "Dublin",
                date = LocalDate.of(2030, 1, 1)
            )
        )

        val savedShowId = show.id!!

        val requestShowJson = """
        {
            "venue": "The Olympia",
            "city": "Dublin",
            "date": "2030-03-01"
        }
    """.trimIndent()

        mockMvc.perform(
            put("/content/shows/$savedShowId")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestShowJson)
        )
            .andExpect(status().isUnauthorized)

        val savedShow = showRepository.findById(savedShowId).orElseThrow()

        assertEquals(savedShowId, savedShow.id)
        assertEquals("The Academy", savedShow.venue)
        assertEquals("Dublin", savedShow.city)
        assertEquals(LocalDate.of(2030, 1, 1), savedShow.date)
    }

    @Test
    fun `unauthenticated user cannot DELETE show and receives 401 with show still persisted`() {

        val show = showRepository.save(
            Show(
                venue = "The Academy",
                city = "Dublin",
                date = LocalDate.of(2030, 1, 1)
            )
        )

        val savedShowId = show.id!!

        mockMvc.perform(
            delete("/content/shows/$savedShowId")
        )
            .andExpect(status().isUnauthorized)

        assertEquals(1, showRepository.count())
        assertTrue(showRepository.existsById(savedShowId))
    }

    //TODO: ADD TESTS FOR EXPIRED JWT

}
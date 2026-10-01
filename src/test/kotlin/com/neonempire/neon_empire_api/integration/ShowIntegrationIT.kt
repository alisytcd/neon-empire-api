package com.neonempire.neon_empire_api.integration

import com.neonempire.neon_empire_api.model.Show
import com.neonempire.neon_empire_api.repository.ShowRepository
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
import kotlin.test.assertEquals


@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ShowIntegrationIT {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var showRepository: ShowRepository

    @BeforeEach
    fun cleanDatabase() {
        showRepository.deleteAll()
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
    fun `POST show successfully creates a show and persists it in the DB`(){

        val requestShowJson = """
            {
                "venue" : "The Academy",
                "city" : "Dublin",
                "date" : "2030-03-01"
            }
        """.trimIndent()

        mockMvc.perform(post("/content/shows")
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
    fun `POST show with invalid parameters produces a 400 Bad Request and nothing persists in DB`(){

        val requestShowJson = """
            {
                "venue" : "",
                "city" : "Dublin",
                "date" : "2030-03-01"
            }
        """.trimIndent()

        mockMvc.perform(post("/content/shows")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestShowJson))
            .andExpect(status().isBadRequest)


        assertEquals(0,showRepository.count())
    }

    @Test
    fun `PUT show successfully modifies show and persists the change in the DB`(){

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

        mockMvc.perform(put("/content/shows/${savedShowId}")
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
    fun `PUT show with invalid parameters produces a 400 Bad Request and nothing persists in the DB`(){

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

        mockMvc.perform(put("/content/shows/${savedShowId}")
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

        val requestShowJson = """
            {
                "venue" : "The Olympia",
                "city" : "Dublin",
                "date" : "2030-03-01"
            }
        """.trimIndent()

        mockMvc.perform(put("/content/shows/9999")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestShowJson))
            .andExpect( status().isNotFound)

        assertEquals(0,showRepository.count())
    }

    @Test
    fun `DELETE show successfully deletes a show with a 204 response and show is deleted in DB`(){

        val show = showRepository.save(
            Show(
                venue = "The Academy",
                city = "Dublin",
                date = LocalDate.of(2030, 1, 1)
            ))

        val savedShowId = show.id!!

        mockMvc.perform(delete("/content/shows/${savedShowId}"))
            .andExpect( status().isNoContent)

        assertEquals(0,showRepository.count())
        assertFalse(showRepository.existsById(savedShowId))

    }

    @Test
    fun `DELETE show returns 404 when request is sent to delete a non-existent show`(){

        val show = showRepository.save(
            Show(
                venue = "The Academy",
                city = "Dublin",
                date = LocalDate.of(2030, 1, 1)
            ))

        mockMvc.perform(delete("/content/shows/9999"))
            .andExpect( status().isNotFound)

        assertEquals(1, showRepository.count())
        assertTrue(showRepository.existsById(show.id!!))

    }

}
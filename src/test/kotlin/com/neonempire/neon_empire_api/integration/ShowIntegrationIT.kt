package com.neonempire.neon_empire_api.integration

import com.neonempire.neon_empire_api.model.Show
import com.neonempire.neon_empire_api.repository.ShowRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
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
}
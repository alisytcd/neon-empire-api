package com.neonempire.neon_empire_api.service

import com.neonempire.neon_empire_api.dto.CreateShowRequestDTO
import com.neonempire.neon_empire_api.model.Show
import com.neonempire.neon_empire_api.repository.ShowRepository
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import java.time.LocalDate
import kotlin.test.assertEquals
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify
import kotlin.test.assertNull

class ShowServiceTest {

    private val showRepository : ShowRepository = mock()
    private val service = ShowService(showRepository)

    @Test
    fun getAllShowsReturnsAllShowsInRepository(){
        val expectedShowsResult = listOf(
                Show (
                        id = 1L,
                        venue = "The Academy",
                        city = "Dublin",
                        date = LocalDate.of(2030, 1, 1)
                    ),
                Show (
                        id = 2L,
                        venue = "Whelans",
                        city = "Dublin",
                        date = LocalDate.of(2030, 2, 1)
                ),
                Show (
                        id = 3L,
                        venue = "Fred Zepplins",
                        city = "Cork",
                        date = LocalDate.of(2030, 3, 1)
                )
            )

        `when`(showRepository.findAll()).thenReturn(expectedShowsResult)

        val actualResult = service.getAllShows()

        assertEquals(expectedShowsResult, actualResult)


    }

    @Test
    fun saveShowSuccessfullySaves(){

        val expectedResult = Show (
            id = 1L,
            venue = "The Academy",
            city = "Dublin",
            date = LocalDate.of(2030, 1, 1)
        )

        `when`(showRepository.save(any<Show>())).thenReturn(expectedResult)

        val showRequest = CreateShowRequestDTO("The Academy","Dublin",LocalDate.of(2030, 1, 1))

        val actualResult = service.saveShow(showRequest)

        val showCaptor = argumentCaptor<Show>()

        verify(showRepository).save(showCaptor.capture())

        val capturedShow = showCaptor.firstValue

        assertNull(capturedShow.id)
        assertEquals("The Academy", capturedShow.venue)
        assertEquals("Dublin", capturedShow.city)
        assertEquals(LocalDate.of(2030, 1, 1), capturedShow.date)


        assertEquals(expectedResult, actualResult)

    }
}
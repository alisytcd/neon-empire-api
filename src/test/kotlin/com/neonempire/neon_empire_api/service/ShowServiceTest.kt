package com.neonempire.neon_empire_api.service

import com.neonempire.neon_empire_api.dto.CreateShowRequestDTO
import com.neonempire.neon_empire_api.dto.UpdateShowRequestDTO
import com.neonempire.neon_empire_api.exception.ShowNotFoundException
import com.neonempire.neon_empire_api.model.Show
import com.neonempire.neon_empire_api.repository.ShowRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import java.time.LocalDate
import kotlin.test.assertEquals
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify
import java.util.Optional
import kotlin.test.assertNull
import kotlin.test.assertTrue

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
    fun getAllShowsReturnsEmptyListWhenNoShowsExist() {

        `when`(showRepository.findAll()).thenReturn(emptyList())

        val actualResult = service.getAllShows()

        assertTrue(actualResult.isEmpty())
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

    @Test
    fun updateShowSuccessfullyUpdatesExistingShow(){

        val expectedResult = Show (
            id = 1L,
            venue = "The Academy",
            city = "Dublin",
            date = LocalDate.of(2030, 1, 3)
        )

        val priorShow = Show (
            id = 1L,
            venue = "The Olympia",
            city = "Dublin",
            date = LocalDate.of(2030, 1, 1)
        )

        val updateShowRequestDTO = UpdateShowRequestDTO("The Academy", "Dublin", LocalDate.of(2030, 1, 3))

        `when`(showRepository.findById(1L)).thenReturn(Optional.of(priorShow))

        `when`(showRepository.save(any<Show>())).thenReturn(expectedResult)

        val actualResult = service.updateShow(priorShow.id!!, updateShowRequestDTO)

        val showCaptor = argumentCaptor<Show>()

        verify(showRepository).save(showCaptor.capture())

        val capturedShow = showCaptor.firstValue

        assertEquals(1L, capturedShow.id)
        assertEquals("The Academy", capturedShow.venue)
        assertEquals("Dublin", capturedShow.city)
        assertEquals(LocalDate.of(2030, 1, 3), capturedShow.date)

        assertEquals(expectedResult, actualResult)


    }

    @Test
    fun updateShowThrowsExceptionWhenShowIdNotFound(){

        val showId = 999L

        `when`(showRepository.findById(showId)).thenReturn(Optional.empty())

        val updateShowRequestDTO = UpdateShowRequestDTO("Central Park", "New York", LocalDate.of(2030, 1, 3))

        val exception = assertThrows<ShowNotFoundException>{

            service.updateShow(showId,updateShowRequestDTO)

        }

        assertEquals("Show with $showId doesn't exist in the database." , exception.message)

        verify(showRepository, never()).save(any<Show>())

    }

    @Test
    fun deleteShowSuccessfullyDeletesExistingShow(){

        val priorShow = Show (
            id = 1L,
            venue = "The Olympia",
            city = "Dublin",
            date = LocalDate.of(2030, 1, 1)
        )

        `when`(showRepository.findById(1L)).thenReturn(Optional.of(priorShow))

        service.deleteShow(1L)

        verify(showRepository).deleteById(1L)

    }

    @Test
    fun deleteShowThrowsExceptionWhenShowIdNotFound(){

        val showId = 999L

        `when`(showRepository.findById(showId)).thenReturn(Optional.empty())

        val exception = assertThrows<ShowNotFoundException>{

            service.deleteShow(showId)

        }

        assertEquals("Show with $showId doesn't exist in the database." , exception.message)

        verify(showRepository, never()).deleteById(showId)

    }
}
package com.neonempire.neon_empire_api.service

import com.neonempire.neon_empire_api.dto.CreateShowRequestDTO
import com.neonempire.neon_empire_api.dto.UpdateShowRequestDTO
import com.neonempire.neon_empire_api.exception.ShowNotFoundException
import com.neonempire.neon_empire_api.model.Show
import com.neonempire.neon_empire_api.repository.ShowRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class ShowService (
    private val showRepository: ShowRepository
){

    fun getAllShows(): List<Show> {
        return showRepository.findAll();
    }

    fun saveShow(showRequest : CreateShowRequestDTO) : Show {

        val show = Show(
            venue = showRequest.venue,
            city = showRequest.city,
            date = showRequest.date!!
        )
        return showRepository.save(show)
    }

    fun updateShow(showId : Long, updateShowRequest : UpdateShowRequestDTO) : Show {

        val show = showRepository.findById(showId)
            .orElseThrow{ShowNotFoundException("Show with $showId doesn't exist in the database.")}

        show.venue = updateShowRequest.venue
        show.city = updateShowRequest.city
        show.date = updateShowRequest.date!!

        showRepository.save(show)

        return show
    }
    fun deleteShow(showId : Long) {

        showRepository.findById(showId)
            .orElseThrow{ShowNotFoundException("Show with $showId doesn't exist in the database.")}

        showRepository.deleteById(showId)

    }

}
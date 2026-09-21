package com.neonempire.neon_empire_api.service

import com.neonempire.neon_empire_api.model.Show
import com.neonempire.neon_empire_api.repository.ShowRepository
import org.springframework.stereotype.Service

@Service
class ShowService (
    private val showRepository: ShowRepository
){

    fun getAllShows(): List<Show> {
        return showRepository.findAll();
    }

    fun saveShow(show : Show) : Show {
        return showRepository.save(show)
    }

}
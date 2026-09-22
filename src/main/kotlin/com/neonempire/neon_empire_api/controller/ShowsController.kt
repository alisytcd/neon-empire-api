package com.neonempire.neon_empire_api.controller

import com.neonempire.neon_empire_api.dto.CreateShowRequestDTO
import com.neonempire.neon_empire_api.dto.UpdateShowRequestDTO
import com.neonempire.neon_empire_api.model.Show
import com.neonempire.neon_empire_api.service.ShowService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping

@RestController
class ShowsController (private val showService : ShowService) {


    @GetMapping("/api/shows")
    fun getShows() : List<Show> {
        return showService.getAllShows()
    }

    @PostMapping("/api/shows")
    fun saveShow (
        @Valid @RequestBody showRequest: CreateShowRequestDTO
    ) : Show {
        return showService.saveShow(showRequest)
    }

    @PutMapping("/api/shows/{showId}")
    fun updateShow (
        @PathVariable showId : Long ,
        @Valid @RequestBody updateShowRequest : UpdateShowRequestDTO
    ) : Show {
        return showService.updateShow(showId,updateShowRequest)
    }



}
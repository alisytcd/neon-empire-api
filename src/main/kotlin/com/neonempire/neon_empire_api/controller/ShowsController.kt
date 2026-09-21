package com.neonempire.neon_empire_api.controller

import com.neonempire.neon_empire_api.model.Show
import com.neonempire.neon_empire_api.service.ShowService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class ShowsController (private val showService : ShowService) {


    @GetMapping("/api/shows")
    fun getShows() : List<Show> {
        return showService.getAllShows()
    }

    @PostMapping("/api/shows")
    fun saveShow(@RequestBody show: Show) : Show {
        return showService.saveShow(show)
    }
}
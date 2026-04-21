package br.com.taskstreamai.controller

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
class SilenceFaviconController {

    @GetMapping("/favicon.ico")
    @ResponseStatus(HttpStatus.NO_CONTENT) // Returns 204 No Content
    fun silence() {
        // This stops the NoResourceFoundException because a handler NOW exists.
    }
}
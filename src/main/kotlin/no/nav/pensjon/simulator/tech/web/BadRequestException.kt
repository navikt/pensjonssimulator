package no.nav.pensjon.simulator.tech.web

class BadRequestException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)

package no.nav.pensjon.simulator.alderspensjon.api.samhandler.acl.v3

data class BadRequestReasonV3(
    val feil: String,
    val kode: String
)

data class InternalServerErrorReasonV3(
    val feil: String
)

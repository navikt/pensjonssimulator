package no.nav.pensjon.simulator.hybrid.api.samhandler.acl.v3

import java.time.LocalDateTime

data class ForretningsmessigUnntakReasonV3(
    val feilkilde: String,
    val feilaarsak: String,
    val feilmelding: String,
    val tidspunkt: LocalDateTime
)

data class InternalServerErrorReasonV3(
    val feilkilde: String,
    val feilmelding: String,
    val tidspunkt: LocalDateTime
)

package no.nav.pensjon.simulator.tech.sporing

import no.nav.pensjon.simulator.generelt.organisasjon.Organisasjonsnummer
import no.nav.pensjon.simulator.person.Pid
import java.time.LocalDateTime

data class Sporing(
    val pid: Pid,
    val mottaker: Organisasjonsnummer,
    val tema: String,
    val behandlingGrunnlag: String,
    val uthentingTidspunkt: LocalDateTime,
    val leverteData: String
)

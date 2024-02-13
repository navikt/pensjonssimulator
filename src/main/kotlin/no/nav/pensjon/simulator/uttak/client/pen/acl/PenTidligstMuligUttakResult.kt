package no.nav.pensjon.simulator.uttak.client.pen.acl

import java.time.LocalDate

data class PenTidligstMuligUttakResult(
    val uttakDato: LocalDate // dato på format 'yyyy-MM-dd'
)

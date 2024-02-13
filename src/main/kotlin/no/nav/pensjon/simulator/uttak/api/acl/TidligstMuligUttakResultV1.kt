package no.nav.pensjon.simulator.uttak.api.acl

import java.time.LocalDate

data class TidligstMuligUttakResultV1(
    val tidligstMuligeUttakstidspunktListe: List<TidligstMuligUttakV1>,
    val feil: TidligstMuligUttakFeilV1? = null
)

data class TidligstMuligUttakV1(
    val uttaksgrad: Int,
    val tidligstMuligeUttaksdato: LocalDate
)

data class TidligstMuligUttakFeilV1(
    val type: TidligstMuligUttakFeilTypeV1,
    val beskrivelse: String
)

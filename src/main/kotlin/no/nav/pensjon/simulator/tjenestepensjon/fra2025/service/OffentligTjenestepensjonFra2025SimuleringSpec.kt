package no.nav.pensjon.simulator.tjenestepensjon.fra2025.service

import no.nav.pensjon.simulator.person.Pid
import java.time.LocalDate

data class OffentligTjenestepensjonFra2025SimuleringSpec(
    val pid: Pid,
    val foedselsdato: LocalDate,
    val uttaksdato: LocalDate,
    val sisteInntekt: Int,
    val utlandAntallAar: Int, // antall år etter minstealder for trygdetid
    val afpErForespurt: Boolean,
    val epsHarPensjon: Boolean,
    val epsHarInntektOver2G: Boolean,
    val fremtidigeInntekter: List<TjenestepensjonInntektSpec>?, //TODO make non-nullable
    val gjelderApoteker: Boolean
)

data class TjenestepensjonInntektSpec(
    val fom: LocalDate,
    val aarligInntekt: Int
)

package no.nav.pensjon.simulator.hybrid

import no.nav.pensjon.simulator.uttak.Uttaksgrad

data class AlderspensjonOgPrivatAfpResult(
    val suksess: Boolean,
    val alderspensjonsperiodeListe: List<Alderspensjonsperiode>,
    val privatAfpPeriodeListe: List<PrivatAfpPeriode>,
    val harNaavaerendeUttak: Boolean,
    val harTidligereUttak: Boolean,
    val harLoependePrivatAfp: Boolean,
    val problem: Problem? = null
)

data class Alderspensjonsperiode(
    val alderAar: Int,
    val beloep: Int,
    val fom: String,
    val uttaksperiodeListe: List<Uttaksperiode>
)

data class PrivatAfpPeriode(
    val alderAar: Int,
    val beloep: Int
)

data class Uttaksperiode(
    val startmaaned: Int,
    val uttaksgrad: Uttaksgrad
)

data class Problem(
    val type: ProblemType,
    val beskrivelse: String
)

enum class ProblemType {
    UGYLDIG_UTTAKSDATO,
    UGYLDIG_UTTAKSGRAD,
    UGYLDIG_SIVILSTATUS,
    UGYLDIG_INNTEKT,
    UGYLDIG_ANTALL_AAR,
    UGYLDIG_PERSONIDENT,
    PERSON_IKKE_FUNNET,
    PERSON_FOR_HOEY_ALDER,
    UTILSTREKKELIG_OPPTJENING,
    UTILSTREKKELIG_TRYGDETID,
    ANNEN_KLIENTFEIL,
    SERVERFEIL
}

package no.nav.pensjon.simulator.core.out

// no.nav.domain.pensjon.kjerne.simulering.Garantipensjonsniva
data class OutputGarantipensjonNivaa(
    val beloep: Double?,
    val satsType: String?,
    val sats: Double?,
    val anvendtTrygdetid: Int?
)

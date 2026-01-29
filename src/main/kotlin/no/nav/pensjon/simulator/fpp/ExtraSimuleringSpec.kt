package no.nav.pensjon.simulator.fpp

data class ExtraSimuleringSpec(
    val beregnInstitusjonsopphold: Boolean,
    val beregnForsoergingstillegg: Boolean,
    val epsMottarPensjon: Boolean
)

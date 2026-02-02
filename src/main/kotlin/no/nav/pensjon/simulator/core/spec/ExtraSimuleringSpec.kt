package no.nav.pensjon.simulator.core.spec

data class ExtraSimuleringSpec(
    val beregnInstitusjonsopphold: Boolean,
    val beregnForsoergingstillegg: Boolean,
    val epsMottarPensjon: Boolean
)
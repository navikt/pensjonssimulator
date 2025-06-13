package no.nav.pensjon.simulator.core.domain.regler.grunnlag

// 2025-06-13
data class NormertPensjonsalderGrunnlag(
    val ovreAr: Int,
    val ovreMnd: Int,
    val normertAr: Int,
    val normertMnd: Int,
    val nedreAr: Int,
    val nedreMnd: Int,
    val erPrognose: Boolean
)

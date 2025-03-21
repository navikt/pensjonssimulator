package no.nav.pensjon.simulator.core.domain.regler.grunnlag

// 2025-02-21
data class NormertPensjonsalderGrunnlag(
    val ovreAr: Int,
    val ovreMnd: Int,
    val normertAr: Int,
    val normertMnd: Int,
    val nedreAr: Int,
    val nedreMnd: Int,
    val erPrognose: Boolean
) {
    fun copy() =
        NormertPensjonsalderGrunnlag(
            ovreAr = ovreAr,
            ovreMnd = ovreMnd,
            normertAr = normertAr,
            normertMnd = normertMnd,
            nedreAr = nedreAr,
            nedreMnd = nedreMnd,
            erPrognose = erPrognose
        )
}

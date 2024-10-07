package no.nav.pensjon.simulator.core.domain.regler.beregning2011

data class AfpOffentligLivsvarigGrunnlag(
    val sistRegulertG: Int,
    val bruttoPerAr: Double
) {
    // SIMDOM-ADD
    // TODO: Check sistRegulertG vs sistRegulert
    fun toVilkarsprovDto() = AfpOffentligLivsvarigGrunnlag(sistRegulertG, bruttoPerAr)
    //fun toVilkarsprovDto() = AfpOffentligLivsvarig(sistRegulert, bruttoPerAr)

    // SIMDOM-ADD
    //Kan sendes uten kovertering til foreldreklassen, naar pensjon-regler publserer et nytt API med stotte til AfpOffentligLivsvarig : AfpLivsvarig
    fun toAfpLivsvarig() =
        AfpLivsvarig().apply {
            this.bruttoPerAr = bruttoPerAr
        }
}

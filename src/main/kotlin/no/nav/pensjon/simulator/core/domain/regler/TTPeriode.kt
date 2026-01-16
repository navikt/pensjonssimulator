package no.nav.pensjon.simulator.core.domain.regler

import no.nav.pensjon.simulator.core.domain.regler.enum.GrunnlagkildeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.LandkodeEnum
import java.util.*

// Copied from pensjon-regler-api 2026-01-16
class TTPeriode {
    /**
     * Fra-og-med dato for perioden.
     */
    var fom: Date? = null

    /**
     * Til-og-med dato for perioden.
     */
    var tom: Date? = null

    /**
     * Skal bruker ha poeng for hele året i fom-datoen
     */
    var poengIInnAr = false

    /**
     * Skal bruker ha poeng for hele året i tom-datoen
     */
    var poengIUtAr = false

    /**
     * Hvilket land perioden er opptjent i.
     */
    var landEnum: LandkodeEnum? = null

    /**
     * Om det skal regnes pro rata. Gjelder ved utenlandssaker.
     */
    var ikkeProRata = false

    /**
     * Angir om trygdetidsperioden brukes somm grunnlag på kravet.
     */
    var bruk: Boolean? = null // NB: Made nullable here, since nullable in Trygdetidsgrunnlag in PEN

    /**
     * Kilden til trygdetidsperioden.
     */
    var grunnlagKildeEnum: GrunnlagkildeEnum? = null
}

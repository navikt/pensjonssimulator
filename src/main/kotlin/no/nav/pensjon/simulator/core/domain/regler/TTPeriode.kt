package no.nav.pensjon.simulator.core.domain.regler

import com.fasterxml.jackson.annotation.JsonIgnore
import no.nav.pensjon.simulator.core.domain.regler.enum.GrunnlagkildeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.LandkodeEnum
import no.nav.pensjon.simulator.core.util.DateNoonExtension.noon
import java.util.*

// Checked 2025-02-28
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
    var poengIInnAr: Boolean = false

    /**
     * Skal bruker ha poeng for hele året i tom-datoen
     */
    var poengIUtAr: Boolean = false

    /**
     * Hvilket land perioden er opptjent i.
     */
    var landEnum: LandkodeEnum? = null

    /**
     * Om det skal regnes pro rata. Gjelder ved utenlandssaker.
     */
    var ikkeProRata: Boolean = false

    /**
     * Angir om trygdetidsperioden brukes somm grunnlag på kravet.
     */
    var bruk: Boolean? = null // SIMDOM-EDIT true -> null, since nullable in Trygdetidsgrunnlag in PEN

    /**
     * Kilden til trygdetidsperioden.
     */
    var grunnlagKildeEnum: GrunnlagkildeEnum? = null

    // SIMDOM-ADD:
    @JsonIgnore
    var rawFom: Date? = null
    @JsonIgnore
    var rawTom: Date? = null

    fun finishInit() {
        rawFom = fom
        rawTom = tom
        fom = rawFom?.noon()
        tom = rawTom?.noon()
    }
    // end SIMDOM-ADD

    constructor()

    constructor(source: TTPeriode) : this() {
        fom = source.fom?.clone() as? Date
        rawFom = source.rawFom?.clone() as? Date
        tom = source.tom?.clone() as? Date
        rawTom = source.rawTom?.clone() as? Date
        poengIInnAr = source.poengIInnAr
        poengIUtAr = source.poengIUtAr
        landEnum = source.landEnum
        ikkeProRata = source.ikkeProRata
        bruk = source.bruk
        grunnlagKildeEnum = source.grunnlagKildeEnum
    }

    override fun toString(): String {
        return "TTPeriode(fom=$fom, tom=$tom, poengIInnAr=$poengIInnAr, poengIUtAr=$poengIUtAr, land=$landEnum, ikkeProRata=$ikkeProRata, bruk=$bruk, grunnlagKilde=$grunnlagKildeEnum)"
    }
}

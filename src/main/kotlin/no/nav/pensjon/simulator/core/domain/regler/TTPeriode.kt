package no.nav.pensjon.simulator.core.domain.regler

import com.fasterxml.jackson.annotation.JsonIgnore
import no.nav.pensjon.simulator.core.domain.regler.kode.GrunnlagKildeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.LandCti
import no.nav.pensjon.simulator.core.domain.regler.util.DateCompareUtil
import no.nav.pensjon.simulator.core.util.DateNoonExtension.noon
import java.io.Serializable
import java.util.*

class TTPeriode(
    var fom: Date? = null,
    var tom: Date? = null,
    var changed: Boolean? = null,

    /**
     * Skal bruker ha poeng for hele året i fom-datoen
     */
    var poengIInnAr: Boolean = false,

    /**
     * Skal bruker ha poeng for hele året i tom-datoen
     */
    var poengIUtAr: Boolean = false,

    /**
     * Hvilket land perioden er opptjent i.
     */
    var land: LandCti? = null,

    /**
     * Om det skal regnes pro rata. Gjelder ved utenlandssaker.
     */
    var ikkeProRata: Boolean = false,

    /**
     * Angir om trygdetidsperioden brukes somm grunnlag på kravet.
     */
    var bruk: Boolean = true,
    var grunnlagKilde: GrunnlagKildeCti? = null
) : Comparable<TTPeriode>, Serializable {

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

    constructor(source: TTPeriode) : this() {
        source.fom?.let { this.fom = it.clone() as Date }
        source.rawFom?.let { this.rawFom = it.clone() as Date }
        source.tom?.let { this.tom = it.clone() as Date }
        source.rawTom?.let { this.rawTom = it.clone() as Date }
        this.poengIInnAr = source.poengIInnAr
        this.poengIUtAr = source.poengIUtAr
        source.land?.let { this.land = LandCti(it) }
        this.ikkeProRata = source.ikkeProRata
        this.bruk = source.bruk
        source.grunnlagKilde?.let { this.grunnlagKilde = GrunnlagKildeCti(it) }
    }

    override fun compareTo(other: TTPeriode): Int {
        return DateCompareUtil.compareTo(fom, other.fom)
    }

    override fun toString(): String {
        return "TTPeriode(fom=$fom, tom=$tom, poengIInnAr=$poengIInnAr, poengIUtAr=$poengIUtAr, land=$land, ikkeProRata=$ikkeProRata, bruk=$bruk, grunnlagKilde=$grunnlagKilde)"
    }
}

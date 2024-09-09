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

    constructor(tTPeriode: TTPeriode) : this() {
        if (tTPeriode.fom != null) {
            this.fom = tTPeriode.fom!!.clone() as Date
        }
        if (tTPeriode.tom != null) {
            this.tom = tTPeriode.tom!!.clone() as Date
        }
        this.poengIInnAr = tTPeriode.poengIInnAr
        this.poengIUtAr = tTPeriode.poengIUtAr
        if (tTPeriode.land != null) {
            this.land = LandCti(tTPeriode.land)
        }
        this.ikkeProRata = tTPeriode.ikkeProRata
        this.bruk = tTPeriode.bruk
        if (tTPeriode.grunnlagKilde != null) {
            this.grunnlagKilde = GrunnlagKildeCti(tTPeriode.grunnlagKilde)
        }
    }

    override fun compareTo(other: TTPeriode): Int {
        return DateCompareUtil.compareTo(fom, other.fom)
    }

    override fun toString(): String {
        return "TTPeriode(fom=$fom, tom=$tom, poengIInnAr=$poengIInnAr, poengIUtAr=$poengIUtAr, land=$land, ikkeProRata=$ikkeProRata, bruk=$bruk, grunnlagKilde=$grunnlagKilde)"
    }
}

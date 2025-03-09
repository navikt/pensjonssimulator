package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import com.fasterxml.jackson.annotation.JsonIgnore
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.JustertGarantipensjonsniva
import no.nav.pensjon.simulator.core.domain.regler.enum.BeholdningtypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.GarantiPensjonsnivaSatsEnum
import no.nav.pensjon.simulator.core.domain.regler.kode.BeholdningsTypeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.GarantipenNivaCti

// Checked 2025-02-28
class Garantipensjonsbeholdning() : Beholdning() {
    var justertGarantipensjonsniva: JustertGarantipensjonsniva? = null
    var pensjonsbeholdning = 0.0
    var delingstall67 = 0.0

    /**
     * Satstype brukt i garantipensjonsnivå.
     */
    var satsType: GarantipenNivaCti? = null
    var satsTypeEnum: GarantiPensjonsnivaSatsEnum? = null

    /**
     * Garantipensjonsnivå sats
     */
    var sats = 0.0

    /**
     * Garantipensjonsnivå justert for trygdetid
     */
    var garPN_tt_anv = 0.0

    /**
     * Garantipensjonsnivå fremskrevet.
     */
    var garPN_justert = 0.0

    override var beholdningsType: BeholdningsTypeCti = BeholdningsTypeCti("GAR_PEN_B")
    override var beholdningsTypeEnum: BeholdningtypeEnum = BeholdningtypeEnum.GAR_PEN_B

    // SIMDOM-ADD
    @JsonIgnore private var unclearedPensjonsbeholdning: Double? = null

    val internPensjonsbeholdning: Double
        @JsonIgnore get() = unclearedPensjonsbeholdning ?: pensjonsbeholdning

    // Garantipensjonsbeholdning in kjerne/PEN does not contain pensjonsbeholdning, hence need to set it to zero:
    fun clearPensjonsbeholdning() {
        unclearedPensjonsbeholdning = pensjonsbeholdning
        pensjonsbeholdning = 0.0
    }
}

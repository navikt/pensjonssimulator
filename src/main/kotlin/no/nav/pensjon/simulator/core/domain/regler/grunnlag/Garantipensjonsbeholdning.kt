package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import com.fasterxml.jackson.annotation.JsonIgnore
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.JustertGarantipensjonsniva
import no.nav.pensjon.simulator.core.domain.regler.enum.BeholdningtypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.GarantiPensjonsnivaSatsEnum

// 2025-04-06
class Garantipensjonsbeholdning() : Beholdning() {
    var justertGarantipensjonsniva: JustertGarantipensjonsniva? = null
    var pensjonsbeholdning = 0.0
    @Deprecated("Avvikles.", replaceWith = ReplaceWith("delingstallVedNormertPensjonsalder"))
    var delingstall67 = 0.0
    var delingstallVedNormertPensjonsalder = 0.0

    /**
     * Satstype brukt i garantipensjonsnivå.
     */
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

    override var beholdningsTypeEnum: BeholdningtypeEnum = BeholdningtypeEnum.GAR_PEN_B

    //--- Extra:
    @JsonIgnore private var unclearedPensjonsbeholdning: Double? = null

    val internPensjonsbeholdning: Double
        @JsonIgnore get() = unclearedPensjonsbeholdning ?: pensjonsbeholdning

    // Garantipensjonsbeholdning in kjerne/PEN does not contain pensjonsbeholdning, hence need to set it to zero:
    fun clearPensjonsbeholdning() {
        unclearedPensjonsbeholdning = pensjonsbeholdning
        pensjonsbeholdning = 0.0
    }
    // end extra ---
}

package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import no.nav.pensjon.simulator.core.domain.regler.beregning2011.JustertGarantipensjonsniva
import no.nav.pensjon.simulator.core.domain.regler.enum.BeholdningtypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.GarantiPensjonsnivaSatsEnum

// Copied from pensjon-regler-api v2.4.3 2026-09-04
class Garantipensjonsbeholdning() : Beholdning() {
    var justertGarantipensjonsniva: JustertGarantipensjonsniva? = null
    var pensjonsbeholdning = 0.0
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
     * Har ektefelle inntekt over 2G ved virk.
     * Bidrar til å avgjøre [satsTypeEnum]
     */
    var ektefelleInntektOver2G: Boolean = false

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
    // Garantipensjonsbeholdning in kjerne/PEN does not contain pensjonsbeholdning, hence need to set it to zero:
    fun clearPensjonsbeholdning() {
        pensjonsbeholdning = 0.0
    }
    // end extra ---
}
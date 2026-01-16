package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import no.nav.pensjon.simulator.core.domain.regler.enum.AFPtypeEnum
import java.util.Date

// Copied from pensjon-regler-api 2026-01-16
class AfpHistorikk {
    /**
     * Fremtidig pensjonspoeng
     */
    var afpFpp = 0.0
    var virkFom: Date? = null
    var virkTom: Date? = null
    var afpPensjonsgrad = 0
    var afpOrdningEnum: AFPtypeEnum? = null
}

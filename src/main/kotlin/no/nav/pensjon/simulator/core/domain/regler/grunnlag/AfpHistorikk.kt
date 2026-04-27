package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import no.nav.pensjon.simulator.core.domain.regler.enum.AFPtypeEnum
import java.time.LocalDate

// 2026-04-23
class AfpHistorikk {
    /**
     * Fremtidig pensjonspoeng
     */
    var afpFpp = 0.0
    var virkFomLd: LocalDate? = null
    var virkTomLd: LocalDate? = null
    var afpPensjonsgrad = 0
    var afpOrdningEnum: AFPtypeEnum? = null
}

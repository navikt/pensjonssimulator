package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import no.nav.pensjon.simulator.core.domain.regler.enum.AFPtypeEnum
import java.util.Date

// Checked 2025-02-28
class AfpHistorikk {
    /**
     * Fremtidig pensjonspoeng
     */
    var afpFpp = 0.0
    var virkFom: Date? = null
    var virkTom: Date? = null
    var afpPensjonsgrad = 0
    var afpOrdningEnum: AFPtypeEnum? = null
/*
    constructor()

    constructor(source: AfpHistorikk) : this() {
        afpFpp = source.afpFpp
        virkFom = source.virkFom?.clone() as? Date
        virkTom = source.virkTom?.clone() as? Date
        afpPensjonsgrad = source.afpPensjonsgrad
        source.afpOrdning?.let { afpOrdning = AfpOrdningTypeCti(it) }
        afpOrdning = source.afpOrdning?.let(::AfpOrdningTypeCti)
            ?: source.afpOrdningEnum?.let { AfpOrdningTypeCti(it.name) }
        afpOrdningEnum = source.afpOrdningEnum ?: source.afpOrdning?.let { AFPtypeEnum.valueOf(it.kode) }
    }*/
}

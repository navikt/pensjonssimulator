package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import no.nav.pensjon.simulator.core.domain.regler.enum.ProRataBeregningTypeEnum

// Checked 2025-02-28
class EosEkstra {
    var proRataBeregningTypeEnum: ProRataBeregningTypeEnum? = null
    var redusertAntFppAr: Int? = null
    var spt_eos: Double? = null
    var spt_pa_f92_eos: Int? = null
    var spt_pa_e91_eos: Int? = null
    var vilkar3_17Aok: Boolean? = null

    constructor()

    constructor(source: EosEkstra) : this() {
        proRataBeregningTypeEnum = source.proRataBeregningTypeEnum
        redusertAntFppAr = source.redusertAntFppAr
        spt_eos = source.spt_eos
        spt_pa_f92_eos = source.spt_pa_f92_eos
        spt_pa_e91_eos = source.spt_pa_e91_eos
        vilkar3_17Aok = source.vilkar3_17Aok
    }
}

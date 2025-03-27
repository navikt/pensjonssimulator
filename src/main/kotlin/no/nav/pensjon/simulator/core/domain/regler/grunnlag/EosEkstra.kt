package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import no.nav.pensjon.simulator.core.domain.regler.enum.ProRataBeregningTypeEnum

// 2025-03-23
class EosEkstra {
    var proRataBeregningTypeEnum: ProRataBeregningTypeEnum? = null
    var redusertAntFppAr: Int? = null
    var spt_eos: Double? = null
    var spt_pa_f92_eos: Int? = null
    var spt_pa_e91_eos: Int? = null
    var vilkar3_17Aok: Boolean? = null
}

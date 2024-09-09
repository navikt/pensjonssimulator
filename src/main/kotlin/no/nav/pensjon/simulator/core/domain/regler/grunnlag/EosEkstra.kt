package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import no.nav.pensjon.simulator.core.domain.regler.kode.ProRataBeregningTypeCti
import java.io.Serializable

class EosEkstra(

    var proRataBeregningType: ProRataBeregningTypeCti? = null,

    var redusertAntFppAr: Int = 0,

    var spt_eos: Double = 0.0,

    var spt_pa_f92_eos: Int = 0,

    var spt_pa_e91_eos: Int = 0,

    var vilkar3_17Aok: Boolean = false
) : Serializable {
    constructor(eosEkstra: EosEkstra) : this() {
        if (eosEkstra.proRataBeregningType != null) {
            proRataBeregningType = ProRataBeregningTypeCti(eosEkstra.proRataBeregningType)
        }
        this.redusertAntFppAr = eosEkstra.redusertAntFppAr
        this.spt_eos = eosEkstra.spt_eos
        this.spt_pa_f92_eos = eosEkstra.spt_pa_f92_eos
        this.spt_pa_e91_eos = eosEkstra.spt_pa_e91_eos
        this.vilkar3_17Aok = eosEkstra.vilkar3_17Aok
    }
}

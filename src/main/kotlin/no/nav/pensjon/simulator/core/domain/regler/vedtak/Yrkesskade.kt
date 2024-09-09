package no.nav.pensjon.simulator.core.domain.regler.vedtak

import no.nav.pensjon.simulator.core.domain.regler.kode.VilkarOppfyltUTCti

class Yrkesskade : AbstraktVilkar {
    constructor() : super()
    constructor(yrkesskade: Yrkesskade?) : super(yrkesskade!!)
    constructor(resultat: VilkarOppfyltUTCti?) : super(resultat)

    override fun dypKopi(abstraktVilkar: AbstraktVilkar): AbstraktVilkar? {
        var fm: Yrkesskade? = null
        if (abstraktVilkar.javaClass == Yrkesskade::class.java) {
            fm = Yrkesskade(abstraktVilkar as Yrkesskade?)
        }
        return fm
    }
}

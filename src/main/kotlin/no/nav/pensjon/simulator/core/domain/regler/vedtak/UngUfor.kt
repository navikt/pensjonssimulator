package no.nav.pensjon.simulator.core.domain.regler.vedtak

import no.nav.pensjon.simulator.core.domain.regler.kode.VilkarOppfyltUTCti

class UngUfor : AbstraktVilkar {
    constructor() : super()
    constructor(resultat: VilkarOppfyltUTCti?) : super(resultat)
    constructor(ungUfor: UngUfor) : super(ungUfor)

    override fun dypKopi(abstraktVilkar: AbstraktVilkar): AbstraktVilkar? {
        var uu: UngUfor? = null
        if (abstraktVilkar.javaClass == UngUfor::class.java) {
            uu = UngUfor(abstraktVilkar as UngUfor)
        }
        return uu
    }

}

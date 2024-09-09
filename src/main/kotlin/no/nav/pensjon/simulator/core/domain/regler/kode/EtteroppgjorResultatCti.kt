package no.nav.pensjon.simulator.core.domain.regler.kode

import java.io.Serializable

class EtteroppgjorResultatCti : TypeCti, Serializable {
    constructor() : super("")
    constructor(etteroppgjorResultatCti: EtteroppgjorResultatCti?) : super(etteroppgjorResultatCti!!)
    constructor(kode: String) : super(kode)
}

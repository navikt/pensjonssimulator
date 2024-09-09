package no.nav.pensjon.simulator.core.domain.regler.kode

import java.io.Serializable

/**
 * Kodeverk for IfuType. Angir om bruker kvalifiserer til Minste-IFU sats som ung ufør, enslig eller gift
 */
class MinimumIfuTypeCti : TypeCti, Serializable {
    constructor() : super("")
    constructor(kode: String) : super(kode)
    constructor(typeCti: TypeCti?) : super(typeCti!!)
}

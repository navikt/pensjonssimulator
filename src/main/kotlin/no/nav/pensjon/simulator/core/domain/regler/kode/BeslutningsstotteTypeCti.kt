package no.nav.pensjon.simulator.core.domain.regler.kode

import java.io.Serializable

/**
 * Beskriver type besluttningsstøtte som skal eksekveres
 */
class BeslutningsstotteTypeCti : TypeCti, Serializable {
    constructor() : super("")
    constructor(kode: String) : super(kode)
    constructor(beslutningsstotteTypeCti: BeslutningsstotteTypeCti?) : super(beslutningsstotteTypeCti!!)
}

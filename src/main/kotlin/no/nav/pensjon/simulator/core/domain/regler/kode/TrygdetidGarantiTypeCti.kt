package no.nav.pensjon.simulator.core.domain.regler.kode

import java.io.Serializable

/**
 * Kodeverk for Trygdetid garantiType.
 */
class TrygdetidGarantiTypeCti : TypeCti, Serializable {
    constructor() : super("")
    constructor(kode: String) : super(kode)
    constructor(typeCti: TypeCti?) : super(typeCti!!)
}

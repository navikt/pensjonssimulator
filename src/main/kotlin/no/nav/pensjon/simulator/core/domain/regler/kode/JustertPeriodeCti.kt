package no.nav.pensjon.simulator.core.domain.regler.kode

import java.io.Serializable

/**
 * Tilsvarer kodeverktype K_JUST_PERIODE.
 */
class JustertPeriodeCti : TypeCti, Serializable {
    constructor() : super("")
    constructor(kode: String) : super(kode)
    constructor(justertPeriodeCti: JustertPeriodeCti?) : super(justertPeriodeCti!!)
}

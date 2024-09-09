package no.nav.pensjon.simulator.core.domain.regler.kode

import java.io.Serializable

class LandCti : TypeCti, Serializable {
    constructor() : super("")
    constructor(kode: String) : super(kode)
    constructor(landCti: LandCti?) : super(landCti!!)

    override var kode: String
        get() = when (super.kode) {
            "???" -> "P_UKJENT"
            "349" -> "P_SPANSKE_OMR_AFRIKA"
            "546" -> "P_SIKKIM"
            "556" -> "P_YEMEN"
            "669" -> "P_PANAMAKANALSONEN"
            else -> {
                super.kode
            }
        }
        set(value) {
            when (value) {
                "P_UKJENT" -> super.kode = "???"
                "P_SPANSKE_OMR_AFRIKA" -> super.kode = "349"
                "P_SIKKIM" -> super.kode = "546"
                "P_YEMEN" -> super.kode = "556"
                "P_PANAMAKANALSONEN" -> super.kode = "669"
                else -> {
                    super.kode = value
                }
            }
        }
}

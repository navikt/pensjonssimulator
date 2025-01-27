package no.nav.pensjon.simulator.core.trygd

import no.nav.pensjon.simulator.core.domain.regler.enum.LandkodeEnum
import java.util.*

// PEN: no.nav.service.pensjon.simulering.support.command.LandMedRettTilOpptjeningAvTrygdetidCode
enum class TrygdetidOpptjeningRettLand(val land: LandkodeEnum, val kravOmArbeid: Boolean) {
    // Nederland + nordisk konvensjon
    NLD(land = LandkodeEnum.NLD, kravOmArbeid = false),
    SWE(land = LandkodeEnum.SWE, kravOmArbeid = false),
    DNK(land = LandkodeEnum.DNK, kravOmArbeid = false),
    FIN(land = LandkodeEnum.FIN, kravOmArbeid = false),
    FRO(land = LandkodeEnum.FRO, kravOmArbeid = false),
    GRL(land = LandkodeEnum.GRL, kravOmArbeid = false),
    ISL(land = LandkodeEnum.ISL, kravOmArbeid = false),

    // EØS-land
    BEL(land = LandkodeEnum.BEL, kravOmArbeid = true),
    BGR(land = LandkodeEnum.BGR, kravOmArbeid = true),
    EST(land = LandkodeEnum.EST, kravOmArbeid = true),
    FRA(land = LandkodeEnum.FRA, kravOmArbeid = true),
    GRC(land = LandkodeEnum.GRC, kravOmArbeid = true),
    IRL(land = LandkodeEnum.IRL, kravOmArbeid = true),
    ITA(land = LandkodeEnum.ITA, kravOmArbeid = true),
    CYP(land = LandkodeEnum.CYP, kravOmArbeid = true),
    HRV(land = LandkodeEnum.HRV, kravOmArbeid = true),
    LVA(land = LandkodeEnum.LVA, kravOmArbeid = true),
    LTU(land = LandkodeEnum.LTU, kravOmArbeid = true),
    LUX(land = LandkodeEnum.LUX, kravOmArbeid = true),
    MLT(land = LandkodeEnum.MLT, kravOmArbeid = true),
    POL(land = LandkodeEnum.POL, kravOmArbeid = true),
    PRT(land = LandkodeEnum.PRT, kravOmArbeid = true),
    ROU(land = LandkodeEnum.ROU, kravOmArbeid = true),
    SVK(land = LandkodeEnum.SVK, kravOmArbeid = true),
    SVN(land = LandkodeEnum.SVN, kravOmArbeid = true),
    ESP(land = LandkodeEnum.ESP, kravOmArbeid = true),
    GBR(land = LandkodeEnum.GBR, kravOmArbeid = true),
    CZE(land = LandkodeEnum.CZE, kravOmArbeid = true),
    DEU(land = LandkodeEnum.DEU, kravOmArbeid = true),
    HUN(land = LandkodeEnum.HUN, kravOmArbeid = true),
    AUT(land = LandkodeEnum.AUT, kravOmArbeid = true),

    // Øvrige
    USA(land = LandkodeEnum.USA, kravOmArbeid = true),
    CHL(land = LandkodeEnum.CHL, kravOmArbeid = true),
    ISR(land = LandkodeEnum.ISR, kravOmArbeid = true),
    AUS(land = LandkodeEnum.AUS, kravOmArbeid = true),
    CAN(land = LandkodeEnum.CAN, kravOmArbeid = true),
    CHE(land = LandkodeEnum.CHE, kravOmArbeid = true),
    IND(land = LandkodeEnum.IND, kravOmArbeid = true);

    companion object {
        fun rettTilOpptjeningAvTrygdetid(land: LandkodeEnum?, harArbeidet: Boolean): Boolean {
            if (mapper == null) {
                initMapper()
            }

            return mapper!!.containsKey(land) && (harArbeidet || !mapper!![land]?.kravOmArbeid!!)
        }

        private var mapper: MutableMap<LandkodeEnum, TrygdetidOpptjeningRettLand>? = null

        private fun initMapper() {
            mapper = EnumMap(LandkodeEnum::class.java)

            @OptIn(ExperimentalStdlibApi::class)
            for (entry in entries) {
                mapper!![entry.land] = entry
            }
        }
    }
}

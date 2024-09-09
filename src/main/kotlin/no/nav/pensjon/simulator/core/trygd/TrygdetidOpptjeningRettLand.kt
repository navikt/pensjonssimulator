package no.nav.pensjon.simulator.core.trygd

import no.nav.pensjon.simulator.core.domain.Land
import java.util.*

// no.nav.service.pensjon.simulering.support.command.LandMedRettTilOpptjeningAvTrygdetidCode
enum class TrygdetidOpptjeningRettLand(val land: Land, val kravOmArbeid: Boolean) {
    /* TODO
    // Nederland + nordisk konvensjon
    NLD(Land.NLD, false),
    SWE(Land.SWE, false),
    DNK(Land.DNK, false),
    FIN(Land.FIN, false),
    FRO(Land.FRO, false),
    GRL(Land.GRL, false),
    ISL(Land.ISL, false),

    // EØS-land
    BEL(Land.BEL, true),
    BGR(Land.BGR, true),
    EST(Land.EST, true),
    FRA(Land.FRA, true),
    GRC(Land.GRC, true),
    IRL(Land.IRL, true),
    ITA(Land.ITA, true),
    CYP(Land.CYP, true),
    HRV(Land.HRV, true),
    LVA(Land.LVA, true),
    LTU(Land.LTU, true),
    LUX(Land.LUX, true),
    MLT(Land.MLT, true),
    POL(Land.POL, true),
    PRT(Land.PRT, true),
    ROU(Land.ROU, true),
    SVK(Land.SVK, true),
    SVN(Land.SVN, true),
    ESP(Land.ESP, true),
    GBR(Land.GBR, true),
    CZE(Land.CZE, true),
    DEU(Land.DEU, true),
    HUN(Land.HUN, true),
    AUT(Land.AUT, true),

    // Øvrige
    USA(Land.USA, true),
    CHL(Land.CHL, true),
    ISR(Land.ISR, true),
    AUS(Land.AUS, true),
    CAN(Land.CAN, true),
    CHE(Land.CHE, true),
    IND(Land.IND, true)
    */
    NOR(Land.NOR, true);

    companion object {
        fun rettTilOpptjeningAvTrygdetid(land: Land?, harArbeidet: Boolean): Boolean {
            if (mapper == null) {
                initMapper()
            }

            return mapper!!.containsKey(land) && (harArbeidet || !mapper!![land]?.kravOmArbeid!!)
        }

        private var mapper: MutableMap<Land, TrygdetidOpptjeningRettLand>? = null

        private fun initMapper() {
            mapper = EnumMap(Land::class.java)

            for (entry in TrygdetidOpptjeningRettLand.entries) {
                mapper!![entry.land] = entry
            }
        }
    }
}

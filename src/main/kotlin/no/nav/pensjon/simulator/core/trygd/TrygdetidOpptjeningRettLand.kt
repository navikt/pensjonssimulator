package no.nav.pensjon.simulator.core.trygd

import no.nav.pensjon.simulator.core.domain.regler.enum.LandkodeEnum
import java.util.*

// PEN: no.nav.service.pensjon.simulering.support.command.LandMedRettTilOpptjeningAvTrygdetidCode
enum class TrygdetidOpptjeningRettLand(val land: LandkodeEnum, val kravOmArbeid: Boolean = true) {
    // Nederland + nordisk konvensjon
    ALA(land = LandkodeEnum.ALA, kravOmArbeid = false), // Åland
    DNK(land = LandkodeEnum.DNK, kravOmArbeid = false), // Danmark
    FIN(land = LandkodeEnum.FIN, kravOmArbeid = false), // Finland
    FRO(land = LandkodeEnum.FRO, kravOmArbeid = false), // Færøyene
    GRL(land = LandkodeEnum.GRL, kravOmArbeid = false), // Grønland
    ISL(land = LandkodeEnum.ISL, kravOmArbeid = false), // Island
    NLD(land = LandkodeEnum.NLD, kravOmArbeid = false), // Nederland
    SWE(land = LandkodeEnum.SWE, kravOmArbeid = false), // Sverige

    // EØS-land
    AUT(land = LandkodeEnum.AUT), // Østerrike
    BEL(land = LandkodeEnum.BEL), // Belgia
    BGR(land = LandkodeEnum.BGR), // Bulgaria
    CYP(land = LandkodeEnum.CYP), // Kypros
    CZE(land = LandkodeEnum.CZE), // Tsjekkia
    DEU(land = LandkodeEnum.DEU), // Tyskland
    ESP(land = LandkodeEnum.ESP), // Spania
    EST(land = LandkodeEnum.EST), // Estland
    FRA(land = LandkodeEnum.FRA), // Frankrike
    GRC(land = LandkodeEnum.GRC), // Hellas
    IRL(land = LandkodeEnum.IRL), // Irland
    ITA(land = LandkodeEnum.ITA), // Italia
    HRV(land = LandkodeEnum.HRV), // Kroatia
    HUN(land = LandkodeEnum.HUN), // Ungarn
    LTU(land = LandkodeEnum.LTU), // Litauen
    LVA(land = LandkodeEnum.LVA), // Latvia
    LUX(land = LandkodeEnum.LUX), // Luxembourg
    MLT(land = LandkodeEnum.MLT), // Malta
    POL(land = LandkodeEnum.POL), // Polen
    PRT(land = LandkodeEnum.PRT), // Portugal
    ROU(land = LandkodeEnum.ROU), // Romania
    SVK(land = LandkodeEnum.SVK), // Slovakia
    SVN(land = LandkodeEnum.SVN), // Slovenia

    // Øvrige
    AUS(land = LandkodeEnum.AUS), // Australia
    CAN(land = LandkodeEnum.CAN), // Canada
    CHE(land = LandkodeEnum.CHE), // Sveits
    CHL(land = LandkodeEnum.CHL), // Chile
    GBR(land = LandkodeEnum.GBR), // Storbritannia
    IND(land = LandkodeEnum.IND), // India
    ISR(land = LandkodeEnum.ISR), // Israel
    QEB(land = LandkodeEnum.QEB), // Quebec
    USA(land = LandkodeEnum.USA); // USA

    companion object {
        private val opptjeningsrettVedLand: Map<LandkodeEnum, TrygdetidOpptjeningRettLand> = map()

        fun rettTilOpptjeningAvTrygdetid(land: LandkodeEnum?, harArbeidet: Boolean): Boolean =
            opptjeningsrettVedLand.containsKey(land)
                    && (harArbeidet || opptjeningsrettVedLand[land]!!.kravOmArbeid.not())

        private fun map(): Map<LandkodeEnum, TrygdetidOpptjeningRettLand> {
            val rettVedLand: MutableMap<LandkodeEnum, TrygdetidOpptjeningRettLand> = EnumMap(LandkodeEnum::class.java)

            @OptIn(ExperimentalStdlibApi::class)
            for (opptjeningsrettLand in entries) {
                rettVedLand[opptjeningsrettLand.land] = opptjeningsrettLand
            }

            return rettVedLand
        }
    }
}

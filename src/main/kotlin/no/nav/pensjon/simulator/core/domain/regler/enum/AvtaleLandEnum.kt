package no.nav.pensjon.simulator.core.domain.regler.enum

import java.util.EnumMap

// Aligned with pensjon-regler-api 2026-01-21
// Includes logic from LandMedRettTilOpptjeningAvTrygdetidCode in PEN
/**
 * Ref.
 * - Nordisk konvensjon om trygd - lovdata.no/dokument/TRAKTAT/traktat/2012-06-12-18
 * - Navs rettskilder: Trygdeavtalerhttps - lovdata.no/nav/andre-rettskilder/Trygdeavtaler
 * NB:
 * Åland er ikke inkludert (Finland må brukes isteden), ref. FAGSYSTEM-318027.
 */
enum class AvtaleLandEnum(val land: LandkodeEnum, val opptjeningsrett: Boolean = true, val kravOmArbeid: Boolean = true) {
    AUS(land = LandkodeEnum.AUS), // Australia
    BEL(land = LandkodeEnum.BEL), // Belgia
    BGR(land = LandkodeEnum.BGR), // Bulgaria
    CAN(land = LandkodeEnum.CAN), // Canada
    CHL(land = LandkodeEnum.CHL), // Chile
    DNK(land = LandkodeEnum.DNK, kravOmArbeid = false), // Danmark
    CZE(land = LandkodeEnum.CZE), // Tsjekkia
    EST(land = LandkodeEnum.EST), // Estland
    FIN(land = LandkodeEnum.FIN, kravOmArbeid = false), // Finland
    FRA(land = LandkodeEnum.FRA), // Frankrike
    GUF(land = LandkodeEnum.GUF, opptjeningsrett = false), // Fransk Guyana
    GLP(land = LandkodeEnum.GLP, opptjeningsrett = false), // Guadeloupe
    MTQ(land = LandkodeEnum.MTQ, opptjeningsrett = false), // Martinique
    REU(land = LandkodeEnum.REU, opptjeningsrett = false), // Réunion
    FRO(land = LandkodeEnum.FRO, kravOmArbeid = false), // Færøyene
    GRL(land = LandkodeEnum.GRL, kravOmArbeid = false), // Grønland
    GRC(land = LandkodeEnum.GRC), // Hellas
    IRL(land = LandkodeEnum.IRL), // Irland
    ISL(land = LandkodeEnum.ISL, kravOmArbeid = false), // Island
    ITA(land = LandkodeEnum.ITA), // Italia
    CYP(land = LandkodeEnum.CYP), // Kypros
    LVA(land = LandkodeEnum.LVA), // Latvia
    LIE(land = LandkodeEnum.LIE, opptjeningsrett = false),
    LTU(land = LandkodeEnum.LTU), // Litauen
    LUX(land = LandkodeEnum.LUX), // Luxembourg
    MLT(land = LandkodeEnum.MLT), // Malta
    NLD(land = LandkodeEnum.NLD, kravOmArbeid = false), // Nederland
    NOR(land = LandkodeEnum.NOR, kravOmArbeid = false), // Norge
    POL(land = LandkodeEnum.POL), // Polen
    PRT(land = LandkodeEnum.PRT), // Portugal
    QEB(land = LandkodeEnum.QEB), // Quebec
    ROU(land = LandkodeEnum.ROU), // Romania
    SVK(land = LandkodeEnum.SVK), // Slovakia
    SVN(land = LandkodeEnum.SVN), // Slovenia
    ESP(land = LandkodeEnum.ESP), // Spania
    GBR(land = LandkodeEnum.GBR), // Storbritannia
    GGY(land = LandkodeEnum.GGY, opptjeningsrett = false), // Guernsey
    IMN(land = LandkodeEnum.IMN, opptjeningsrett = false), // Man
    JEY(land = LandkodeEnum.JEY, opptjeningsrett = false), // Jersey
    CHE(land = LandkodeEnum.CHE), // Sveits
    SWE(land = LandkodeEnum.SWE, kravOmArbeid = false), // Sverige
    DEU(land = LandkodeEnum.DEU), // Tyskland
    HUN(land = LandkodeEnum.HUN), // Ungarn
    USA(land = LandkodeEnum.USA), // USA
    AUT(land = LandkodeEnum.AUT), // Østerrike
    ISR(land = LandkodeEnum.ISR), // Israel
    IND(land = LandkodeEnum.IND), // India
    KOR(land = LandkodeEnum.KOR, opptjeningsrett = false), // Sør-Korea
    YUG(land = LandkodeEnum.YUG, opptjeningsrett = false), // Jugoslavia (foreldet)
    BIH(land = LandkodeEnum.BIH, opptjeningsrett = false), // Bosnia-Hercegovina
    HRV(land = LandkodeEnum.HRV), // Kroatia
    SRB(land = LandkodeEnum.SRB, opptjeningsrett = false), // Serbia
    TUR(land = LandkodeEnum.TUR, opptjeningsrett = false); // Tyrkia

    companion object {
        private val opptjeningsrettVedLand: Map<LandkodeEnum, AvtaleLandEnum> = map()

        fun rettTilOpptjeningAvTrygdetid(land: LandkodeEnum?, harArbeidet: Boolean): Boolean =
            opptjeningsrettVedLand.containsKey(land)
                    && (harArbeidet || opptjeningsrettVedLand[land]!!.kravOmArbeid.not())

        private fun map(): Map<LandkodeEnum, AvtaleLandEnum> {
            val rettVedLand: MutableMap<LandkodeEnum, AvtaleLandEnum> = EnumMap(LandkodeEnum::class.java)

            @OptIn(ExperimentalStdlibApi::class)
            for (opptjeningsrettLand in entries.filter { it.opptjeningsrett }) {
                rettVedLand[opptjeningsrettLand.land] = opptjeningsrettLand
            }

            return rettVedLand
        }
    }
}

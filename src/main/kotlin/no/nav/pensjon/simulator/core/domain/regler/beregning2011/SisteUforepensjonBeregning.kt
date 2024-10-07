package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import no.nav.pensjon.simulator.core.domain.regler.Trygdetid
import no.nav.pensjon.simulator.core.domain.regler.beregning.Ektefelletillegg
import no.nav.pensjon.simulator.core.domain.regler.beregning.UforeEkstra
import no.nav.pensjon.simulator.core.domain.regler.enum.MinstepensjonstypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.ResultatKildeEnum
import no.nav.pensjon.simulator.core.domain.regler.trygdetid.AnvendtTrygdetid

class SisteUforepensjonBeregning : SisteBeregning() {

    var et: Ektefelletillegg? = null
    var tt: Trygdetid? = null
    var uforeEkstra: UforeEkstra? = null

    /**
     * Konverteringsgrunnlaget for brukers rettigheter
     */
    var konverteringsgrunnlagOrdinert: Konverteringsgrunnlag? = null

    /**
     * Betinget, Konverteringsgrunnlag hvis gjenlevenderetten vant
     */
    var konverteringsgrunnlagGJT: Konverteringsgrunnlag? = null

    /**
     * Betinget: Hvis Uføregraden > 0, yrkesskadegrad < Uføregrad.
     */
    var konverteringsgrunnlagUforedel: Konverteringsgrunnlag? = null

    /**
     * Betinget: Hvis Uføregraden > 0, yrkesskadegrad < Uføregrad.
     */
    var konverteringsgrunnlagYrkesskade: Konverteringsgrunnlag? = null

    /**
     * Betinget: Hvis det fantes en folketrygdberegning(ikke nødvendigvis vinnende) i UP
     */
    var anvendtTrygdetidFolketrygd: AnvendtTrygdetid? = null

    /**
     * Hvorvidt utbetalt uførepensjonen per 31.12.2014 ble definert som minstepensjon.
     */
    var minstepensjontypeEnum: MinstepensjonstypeEnum? = null

    /**
     * Hvorvidt utbetalt uførepensjonen per 31.12.2014 ble manuelt overstyrt eller ikke.
     */
    var resultatKildeEnum: ResultatKildeEnum? = null

    /**
     * Netto særtillegg i utbetalt uførepensjonen per 31.12.2014.
     */
    var sertilleggNetto = 0
}

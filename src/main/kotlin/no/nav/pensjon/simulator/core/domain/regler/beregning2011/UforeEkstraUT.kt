package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import no.nav.pensjon.simulator.core.domain.regler.beregning.BeregningUforeperiode
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Uforeperiode

/**
 * Ekstra informasjon til beregnet uføretrygd. Brukes for at PREG skal beregne en uførehistorikk for uføretrygd.
 *
 * @author Swiddy de Louw (Capgemini) - PK-10228
 */
class UforeEkstraUT {
    /**
     * Beregnede uføreperioder for uføretrygd.
     * Dette er uføreperioder som beregnes av PREG, og ligger ved beregningen for en uføretrygd.
     */
    var beregnetUforeperiodeListe: MutableList<BeregningUforeperiode> = mutableListOf()

    val uforeperiodeListe: MutableList<BeregningUforeperiode>
        get() = beregnetUforeperiodeListe

    constructor() : super() {
        beregnetUforeperiodeListe = ArrayList()
    }

    constructor(ue: UforeEkstraUT) {
        beregnetUforeperiodeListe = ArrayList()
        for (bu in ue.beregnetUforeperiodeListe) {
            beregnetUforeperiodeListe.add(BeregningUforeperiode(bu))
        }
    }

    /**
     * Metoden gjør det enkelt i regelmotor å adde en hel array til beregnetUforeperiodeListe.
     */
    fun addToBeregnetUforeperiodeListe(upListe: MutableList<Uforeperiode>) {
        for (up in upListe) {
            beregnetUforeperiodeListe.add(BeregningUforeperiode(up))
        }
    }
}

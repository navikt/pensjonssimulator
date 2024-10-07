package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import no.nav.pensjon.simulator.core.domain.regler.beregning2011.BeregnetUtbetalingsperiode
import java.util.*

class InntektKontrollGrunnlag {

    /**
     * Faktiske inntekter mottatt av bruker hittil i år
     */
    var faktiskeInntekterBrukerListe: List<FaktiskInntektsgrunnlag> = mutableListOf()

    /**
     * Faktiske inntekter mottatt av EPS hittil i år
     */
    var faktiskeInntekterEPSListe: List<FaktiskInntektsgrunnlag> = mutableListOf()

    /**
     * Forventede inntekter for bruker fra Inntektskomponenten
     */
    var forventetInntektBrukerListe: List<Inntektsgrunnlag> = mutableListOf()

    /**
     * Forventede inntekter for EPS fra Inntektskomponenten
     */
    var forventetInntektEPSListe: List<Inntektsgrunnlag> = mutableListOf()

    /**
     * En liste med hva som er beregnet utbetalt av ytelser hittil i år for søker.
     * Listen inneholder et sett med kontinuerlige ikke-overlappende perioder
     */
    var beregnetUtbetalingsperiodeBrukerListe: List<BeregnetUtbetalingsperiode> = mutableListOf()

    /**
     * En liste med hva som er beregnet utbetalt av ytelser hittil i år for EPS.
     * Listen inneholder et sett med kontinuerlige ikke-overlappende perioder
     */
    var beregnetUtbetalingsperiodeEPSListe: List<BeregnetUtbetalingsperiode> = mutableListOf()

    /**
     * Hvilken måned som kontrolleres
     */
    var kontrolldato: Date? = null
}

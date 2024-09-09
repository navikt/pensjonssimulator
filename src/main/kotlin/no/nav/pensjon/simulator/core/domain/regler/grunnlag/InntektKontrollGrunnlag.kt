package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import no.nav.pensjon.simulator.core.domain.regler.beregning2011.BeregnetUtbetalingsperiode
import java.util.*

class InntektKontrollGrunnlag(

    /**
     * Inneholder faktiske inntekter mottatt av bruker hittil i år.
     */
    var faktiskeInntekterBrukerListe: MutableList<FaktiskInntektsgrunnlag> = mutableListOf(),

    /**
     * Inneholder faktiske inntekter mottatt av EPS hittil i år.
     */
    var faktiskeInntekterEPSListe: MutableList<FaktiskInntektsgrunnlag> = mutableListOf(),

    /**
     * Inneholder forventede inntekter for bruker fra Inntektskomponenten.
     */
    var forventetInntektBrukerListe: MutableList<Inntektsgrunnlag> = mutableListOf(),

    /**
     * Inneholder forventede inntekter for EPS fra Inntektskomponenten.
     */
    var forventetInntektEPSListe: MutableList<Inntektsgrunnlag> = mutableListOf(),

    /**
     * En liste med hva som er beregnet utbetalt av ytelser hittil i år for søker.
     * Listen inneholder et sett med kontinuerlige ikke-overlappende perioder
     */
    var beregnetUtbetalingsperiodeBrukerListe: MutableList<BeregnetUtbetalingsperiode> = mutableListOf(),

    /**
     * En liste med hva som er beregnet utbetalt av ytelser hittil i år for EPS.
     * Listen inneholder et sett med kontinuerlige ikke-overlappende perioder
     */
    var beregnetUtbetalingsperiodeEPSListe: MutableList<BeregnetUtbetalingsperiode> = mutableListOf(),

    /**
     * Angir hvilken måned som kontrolleres.
     */
    var kontrolldato: Date? = null
) {

    constructor(inntektKontrollGrunnlag: InntektKontrollGrunnlag) : this() {
        if (inntektKontrollGrunnlag.kontrolldato != null) {
            this.kontrolldato = Date(inntektKontrollGrunnlag.kontrolldato!!.time)
        }

        this.faktiskeInntekterBrukerListe = inntektKontrollGrunnlag.faktiskeInntekterBrukerListe.map { FaktiskInntektsgrunnlag(it) }.toMutableList()
        this.faktiskeInntekterEPSListe = inntektKontrollGrunnlag.faktiskeInntekterEPSListe.map { FaktiskInntektsgrunnlag(it) }.toMutableList()
        this.forventetInntektBrukerListe = inntektKontrollGrunnlag.forventetInntektBrukerListe.map { Inntektsgrunnlag(it) }.toMutableList()
        this.forventetInntektEPSListe = inntektKontrollGrunnlag.forventetInntektEPSListe.map { Inntektsgrunnlag(it) }.toMutableList()
        this.beregnetUtbetalingsperiodeBrukerListe = inntektKontrollGrunnlag.beregnetUtbetalingsperiodeBrukerListe.map { BeregnetUtbetalingsperiode(it) }.toMutableList()
        this.beregnetUtbetalingsperiodeEPSListe = inntektKontrollGrunnlag.beregnetUtbetalingsperiodeEPSListe.map { BeregnetUtbetalingsperiode(it) }.toMutableList()
    }
}

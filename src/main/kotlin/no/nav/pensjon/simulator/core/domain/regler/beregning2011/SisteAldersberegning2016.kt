package no.nav.pensjon.simulator.core.domain.regler.beregning2011

/**
 * Siste aldersberegning 2016 som arver SisteAldersberegning2011. Har en pub2025 i tilleg til pub2011.
 */

/**
 * @author Geir Anders Nilsen
 * @author Magnus Bakken (Accenture), PK-20716
 */
class SisteAldersberegning2016 : SisteAldersberegning2011 {

    /**
     * Pensjon under utbetaling fra beregningsresultatet for AP2011
     */
    var pensjonUnderUtbetaling2011: PensjonUnderUtbetaling? = null

    /**
     * Pensjon under utbetaling fra beregningsresultatet for AP2011, regnet uten gjenlevenderett
     * Feltet brukes ved revurdering av AP2016 når bruker har gjenlevendetillegg.
     */
    var pensjonUnderUtbetaling2011UtenGJR: PensjonUnderUtbetaling? = null

    /**
     * Pensjon under utbetaling fra beregningsresultatet for AP2025
     */
    var pensjonUnderUtbetaling2025: PensjonUnderUtbetaling? = null

    /**
     * Restpensjon regnet uten gjenlevenderettighet.
     * Feltet brukes ved revurdering av AP2016 når bruker har gjenlevendetillegg.
     */
    var restpensjonUtenGJR: Basispensjon? = null

    /**
     * Basispensjon regnet uten gjenlevenderettighet.
     * Feltet brukes ved revurdering av AP2016 når bruker har gjenlevendetillegg.
     */
    var basispensjonUtenGJR: Basispensjon? = null

    constructor() : super()

    constructor(sb: SisteAldersberegning2016) : super(sb) {

        if (sb.pensjonUnderUtbetaling2025 != null) {
            pensjonUnderUtbetaling2025 = PensjonUnderUtbetaling(sb.pensjonUnderUtbetaling2025!!)
        }
        if (sb.pensjonUnderUtbetaling2011 != null) {
            pensjonUnderUtbetaling2011 = PensjonUnderUtbetaling(sb.pensjonUnderUtbetaling2011!!)
        }
        if (sb.pensjonUnderUtbetaling2011UtenGJR != null) {
            pensjonUnderUtbetaling2011UtenGJR = PensjonUnderUtbetaling(sb.pensjonUnderUtbetaling2011UtenGJR!!)
        }
        if (sb.basispensjonUtenGJR != null) {
            basispensjonUtenGJR = Basispensjon(sb.basispensjonUtenGJR!!)
        }
        if (sb.restpensjonUtenGJR != null) {
            restpensjonUtenGJR = Basispensjon(sb.restpensjonUtenGJR!!)
        }
    }

    constructor(
            pensjonUnderUtbetaling2011: PensjonUnderUtbetaling? = null,
            pensjonUnderUtbetaling2011UtenGJR: PensjonUnderUtbetaling? = null,
            pensjonUnderUtbetaling2025: PensjonUnderUtbetaling? = null,
            restpensjonUtenGJR: Basispensjon? = null,
            basispensjonUtenGJR: Basispensjon? = null
    ) {
        this.pensjonUnderUtbetaling2011 = pensjonUnderUtbetaling2011
        this.pensjonUnderUtbetaling2011UtenGJR = pensjonUnderUtbetaling2011UtenGJR
        this.pensjonUnderUtbetaling2025 = pensjonUnderUtbetaling2025
        this.restpensjonUtenGJR = restpensjonUtenGJR
        this.basispensjonUtenGJR = basispensjonUtenGJR
    }
}

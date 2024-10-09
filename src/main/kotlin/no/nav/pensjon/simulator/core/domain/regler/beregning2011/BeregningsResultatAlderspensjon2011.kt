package no.nav.pensjon.simulator.core.domain.regler.beregning2011

class BeregningsResultatAlderspensjon2011 : AbstraktBeregningsResultat {
    /**
     * Informasjon om pensjon under utbetaling, regnet uten gjenlevenderettighet.
     * Kommer ikke til utbetaling, da denne kun er regnet ut som del av beregningen av gjenlevendetillegget på AP2016.
     */
    var pensjonUnderUtbetalingUtenGJR: PensjonUnderUtbetaling? = null
    var beregningsInformasjonKapittel19: BeregningsInformasjon? = null
    var beregningsInformasjonAvdod: BeregningsInformasjon? = null
    var beregningKapittel19: AldersberegningKapittel19? = null

    constructor() : super()

    constructor(source: BeregningsResultatAlderspensjon2011) : super(source) {

        if (source.pensjonUnderUtbetalingUtenGJR != null) {
            pensjonUnderUtbetalingUtenGJR = PensjonUnderUtbetaling(source.pensjonUnderUtbetalingUtenGJR!!)
        }

        if (source.beregningsInformasjonKapittel19 != null) {
            beregningsInformasjonKapittel19 = BeregningsInformasjon(source.beregningsInformasjonKapittel19!!)
        }

        if (source.beregningsInformasjonAvdod != null) {
            beregningsInformasjonAvdod = BeregningsInformasjon(source.beregningsInformasjonAvdod!!)
        }

        if (source.beregningKapittel19 != null) {
            beregningKapittel19 = AldersberegningKapittel19(source.beregningKapittel19!!)
        }
    }
}

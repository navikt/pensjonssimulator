package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import com.fasterxml.jackson.annotation.JsonIgnore
import no.nav.pensjon.simulator.core.domain.regler.Merknad
import no.nav.pensjon.simulator.core.domain.regler.kode.BeregningArsakCti
import no.nav.pensjon.simulator.core.domain.regler.kode.BorMedTypeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.SivilstandTypeCti
import java.io.Serializable
import java.util.*

class BeregningsResultatAlderspensjon2011 : Serializable, AbstraktBeregningsResultat {
    /**
     * Informasjon om pensjon under utbetaling, regnet uten gjenlevenderettighet.
     * Kommer ikke til utbetaling, da denne kun er regnet ut som del av beregningen av gjenlevendetillegget på AP2016.
     */
    var pensjonUnderUtbetalingUtenGJR: PensjonUnderUtbetaling? = null

    var beregningsInformasjonKapittel19: BeregningsInformasjon? = null

    var beregningsInformasjonAvdod: BeregningsInformasjon? = null

    var beregningKapittel19: AldersberegningKapittel19? = null

    @JsonIgnore
    var nullstilt: Boolean = false

    constructor() : super()

    constructor(br: BeregningsResultatAlderspensjon2011) : super(br) {

        if (br.pensjonUnderUtbetalingUtenGJR != null) {
            pensjonUnderUtbetalingUtenGJR = PensjonUnderUtbetaling(br.pensjonUnderUtbetalingUtenGJR!!)
        }
        if (br.beregningsInformasjonKapittel19 != null) {
            beregningsInformasjonKapittel19 = BeregningsInformasjon(br.beregningsInformasjonKapittel19!!)
        }
        if (br.beregningsInformasjonAvdod != null) {
            beregningsInformasjonAvdod = BeregningsInformasjon(br.beregningsInformasjonAvdod!!)
        }
        if (br.beregningKapittel19 != null) {
            beregningKapittel19 = AldersberegningKapittel19(br.beregningKapittel19!!)
        }
        nullstilt = br.nullstilt
    }

    constructor(
        pensjonUnderUtbetalingUtenGJR: PensjonUnderUtbetaling? = null,
        beregningsInformasjonKapittel19: BeregningsInformasjon? = null,
        beregningsInformasjonAvdod: BeregningsInformasjon? = null,
        beregningKapittel19: AldersberegningKapittel19? = null,
        nullstilt: Boolean = false,
        /** super AbstraktBeregningsResultat */
            virkFom: Date? = null,
        pensjonUnderUtbetaling: PensjonUnderUtbetaling? = null,
        uttaksgrad: Int = 0,
        brukersSivilstand: SivilstandTypeCti? = null,
        benyttetSivilstand: BorMedTypeCti? = null,
        beregningArsak: BeregningArsakCti? = null,
        lonnsvekstInformasjon: LonnsvekstInformasjon? = null,
        merknadListe: MutableList<Merknad> = mutableListOf(),
        gjennomsnittligUttaksgradSisteAr: Double = 0.0
    ) : super(
            virkFom = virkFom,
            pensjonUnderUtbetaling = pensjonUnderUtbetaling,
            uttaksgrad = uttaksgrad,
            brukersSivilstand = brukersSivilstand,
            benyttetSivilstand = benyttetSivilstand,
            beregningArsak = beregningArsak,
            lonnsvekstInformasjon = lonnsvekstInformasjon,
            merknadListe = merknadListe,
            gjennomsnittligUttaksgradSisteAr = gjennomsnittligUttaksgradSisteAr
    ) {
        this.pensjonUnderUtbetalingUtenGJR = pensjonUnderUtbetalingUtenGJR
        this.beregningsInformasjonKapittel19 = beregningsInformasjonKapittel19
        this.beregningsInformasjonAvdod = beregningsInformasjonAvdod
        this.beregningKapittel19 = beregningKapittel19
        this.nullstilt = nullstilt
    }
}

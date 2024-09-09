package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import com.fasterxml.jackson.annotation.JsonIgnore
import no.nav.pensjon.simulator.core.domain.regler.Merknad
import no.nav.pensjon.simulator.core.domain.regler.kode.BeregningArsakCti
import no.nav.pensjon.simulator.core.domain.regler.kode.BorMedTypeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.SivilstandTypeCti
import java.io.Serializable
import java.util.*

class BeregningsResultatAlderspensjon2025 : Serializable, AbstraktBeregningsResultat {

    var beregningKapittel20: AldersberegningKapittel20? = null
    var beregningsInformasjonKapittel20: BeregningsInformasjon? = null

    constructor() : super()

    constructor(br: BeregningsResultatAlderspensjon2025) : super(br) {
        if (br.beregningKapittel20 != null) {
            beregningKapittel20 = AldersberegningKapittel20(br.beregningKapittel20!!)
        }
        if (br.beregningsInformasjonKapittel20 != null) {
            beregningsInformasjonKapittel20 = br.beregningsInformasjonKapittel20
        }
    }

    constructor(
        beregningKapittel20: AldersberegningKapittel20? = null,
        beregningsInformasjonKapittel20: BeregningsInformasjon? = null,
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
        this.beregningKapittel20 = beregningKapittel20
        this.beregningsInformasjonKapittel20 = beregningsInformasjonKapittel20
    }
}

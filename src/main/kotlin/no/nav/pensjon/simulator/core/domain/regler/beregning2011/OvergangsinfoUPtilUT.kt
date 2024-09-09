package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import no.nav.pensjon.simulator.core.domain.regler.kode.MinstepensjonTypeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.ResultatKildeCti
import no.nav.pensjon.simulator.core.domain.regler.trygdetid.AnvendtTrygdetid
import java.io.Serializable

/**
 * Informasjon mht overgang fra UP til UT
 *
 * @author Swiddy de Louw - Capgemini- PK-8712
 * @author Swiddy de Louw - Capgemini- PK-7113
 */

class OvergangsinfoUPtilUT : Serializable {
    /**
     * Konvertert beregningsgrunnlag for Ordinær beregning
     */
    var konvertertBeregningsgrunnlagOrdiner: BeregningsgrunnlagKonvertert? = null

    /**
     * Konvertert beregningsgrunnlag for Yrkesskade beregning
     */
    var konvertertBeregningsgrunnlagYrkesskade: BeregningsgrunnlagKonvertert? = null

    /**
     * Uføretrygd ektefelletilleg
     */
    var ektefelletilleggUT: EktefelletilleggUT? = null

    /**
     * Inntektsgrense for Friintektsdato
     */
    var inntektsgrenseorFriinntektsdato: Int = 0

    /**
     * Konvertert beregningsgrunnlag for gjenlevendetillegg hvis fastsatt
     */
    var konvertertBeregningsgrunnlagGJT: BeregningsgrunnlagKonvertert? = null

    /**
     * Anvendt trygdetid fra konvertert uførepensjon
     */
    var anvendtTrygdetidUP: AnvendtTrygdetid? = null

    /**
     * Anvendt trygdetid fra hjemmeberegningen til konvertert uførepensjon
     */
    var anvendtTrygdetidUPHjemme: AnvendtTrygdetid? = null

    /**
     * Egen anvendt trygdetid fra UP
     */
    var anvendtTrygdetidUP_egen: AnvendtTrygdetid? = null

    /**
     * Hvorvidt utbetalt uførepensjonen per 31.12.2014 ble definert som minstepensjon.
     */
    var minstepensjonType: MinstepensjonTypeCti? = null

    /**
     * Hvorvidt utbetalt uførepensjonen per 31.12.2014 ble manuelt overstyrt eller ikke.
     */
    var resultatKilde: ResultatKildeCti? = null

    /**
     * Netto særtillegg i utbetalt uførepensjonen per 31.12.2014.
     */
    var sertilleggNetto: Int = 0

    constructor(konvertertBeregningsgrunnlagOrdiner: BeregningsgrunnlagKonvertert, konvertertBeregningsgrunnlagYrkesskade: BeregningsgrunnlagKonvertert,
                ektefelletilleggUT: EktefelletilleggUT, inntektsgrenseorFriinntektsdato: Int, konvertertBeregningsgrunnlagGJT: BeregningsgrunnlagKonvertert,
                anvendtTrygdetidUP: AnvendtTrygdetid, anvendtTrygdetidUPHjemme: AnvendtTrygdetid, minstepensjonType: MinstepensjonTypeCti, resultatKilde: ResultatKildeCti,
                sertilleggNetto: Int) : this() {
        this.konvertertBeregningsgrunnlagOrdiner = konvertertBeregningsgrunnlagOrdiner
        this.konvertertBeregningsgrunnlagYrkesskade = konvertertBeregningsgrunnlagYrkesskade
        this.ektefelletilleggUT = ektefelletilleggUT
        this.inntektsgrenseorFriinntektsdato = inntektsgrenseorFriinntektsdato
        this.konvertertBeregningsgrunnlagGJT = konvertertBeregningsgrunnlagGJT
        this.anvendtTrygdetidUP = anvendtTrygdetidUP
        this.anvendtTrygdetidUPHjemme = anvendtTrygdetidUPHjemme
        this.minstepensjonType = minstepensjonType
        this.resultatKilde = resultatKilde
        this.sertilleggNetto = sertilleggNetto
    }

    constructor()

    constructor(o: OvergangsinfoUPtilUT) : this() {
        if (o.ektefelletilleggUT != null) {
            ektefelletilleggUT = EktefelletilleggUT(o.ektefelletilleggUT!!)
        }
        inntektsgrenseorFriinntektsdato = o.inntektsgrenseorFriinntektsdato

        if (o.konvertertBeregningsgrunnlagOrdiner != null) {
            konvertertBeregningsgrunnlagOrdiner = BeregningsgrunnlagKonvertert(o.konvertertBeregningsgrunnlagOrdiner!!)
        }
        if (o.konvertertBeregningsgrunnlagYrkesskade != null) {
            konvertertBeregningsgrunnlagYrkesskade = BeregningsgrunnlagKonvertert(o.konvertertBeregningsgrunnlagYrkesskade!!)
        }
        if (o.konvertertBeregningsgrunnlagGJT != null) {
            konvertertBeregningsgrunnlagGJT = BeregningsgrunnlagKonvertert(o.konvertertBeregningsgrunnlagGJT!!)
        }
        if (o.anvendtTrygdetidUP != null) {
            anvendtTrygdetidUP = AnvendtTrygdetid(o.anvendtTrygdetidUP!!)
        }
        if (o.anvendtTrygdetidUPHjemme != null) {
            anvendtTrygdetidUPHjemme = AnvendtTrygdetid(o.anvendtTrygdetidUPHjemme!!)
        }

        if (o.anvendtTrygdetidUP_egen != null) {
            anvendtTrygdetidUP_egen = AnvendtTrygdetid(o.anvendtTrygdetidUP_egen!!)
        }

        if (o.minstepensjonType != null) {
            minstepensjonType = MinstepensjonTypeCti(o.minstepensjonType)
        }

        if (o.resultatKilde != null) {
            resultatKilde = ResultatKildeCti(o.resultatKilde)
        }
    }

    constructor(
            konvertertBeregningsgrunnlagOrdiner: BeregningsgrunnlagKonvertert? = null,
            konvertertBeregningsgrunnlagYrkesskade: BeregningsgrunnlagKonvertert? = null,
            ektefelletilleggUT: EktefelletilleggUT? = null,
            inntektsgrenseorFriinntektsdato: Int = 0,
            konvertertBeregningsgrunnlagGJT: BeregningsgrunnlagKonvertert? = null,
            anvendtTrygdetidUP: AnvendtTrygdetid? = null,
            anvendtTrygdetidUPHjemme: AnvendtTrygdetid? = null,
            anvendtTrygdetidUP_egen: AnvendtTrygdetid? = null,
            minstepensjonType: MinstepensjonTypeCti? = null,
            resultatKilde: ResultatKildeCti? = null,
            sertilleggNetto: Int = 0
    ) {
        this.konvertertBeregningsgrunnlagOrdiner = konvertertBeregningsgrunnlagOrdiner
        this.konvertertBeregningsgrunnlagYrkesskade = konvertertBeregningsgrunnlagYrkesskade
        this.ektefelletilleggUT = ektefelletilleggUT
        this.inntektsgrenseorFriinntektsdato = inntektsgrenseorFriinntektsdato
        this.konvertertBeregningsgrunnlagGJT = konvertertBeregningsgrunnlagGJT
        this.anvendtTrygdetidUP = anvendtTrygdetidUP
        this.anvendtTrygdetidUPHjemme = anvendtTrygdetidUPHjemme
        this.anvendtTrygdetidUP_egen = anvendtTrygdetidUP_egen
        this.minstepensjonType = minstepensjonType
        this.resultatKilde = resultatKilde
        this.sertilleggNetto = sertilleggNetto
    }

}

package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import no.nav.pensjon.simulator.core.domain.regler.Merknad
import no.nav.pensjon.simulator.core.domain.regler.enum.Beregningsarsak
import no.nav.pensjon.simulator.core.domain.regler.enum.BorMedTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.SivilstandEnum
import java.util.*

// 2025-03-10
@JsonSubTypes(
    JsonSubTypes.Type(value = BeregningsresultatUforetrygd::class),
    JsonSubTypes.Type(value = BeregningsResultatAlderspensjon2016::class),
    JsonSubTypes.Type(value = BeregningsResultatAlderspensjon2025::class),
    JsonSubTypes.Type(value = BeregningsResultatAfpPrivat::class),
    JsonSubTypes.Type(value = BeregningsResultatAlderspensjon2011::class)
)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
abstract class AbstraktBeregningsResultat {
    var virkFom: Date? = null
    var pensjonUnderUtbetaling: PensjonUnderUtbetaling? = null
    var uttaksgrad = 0

    /**
     * Snittet av uttaksgradene i perioden fra (virk bakover i tid til 1 mai) og til (virk fremover i tid til 1 mai).
     */
    var brukersSivilstandEnum: SivilstandEnum? = null
    var benyttetSivilstandEnum: BorMedTypeEnum? = null
    var beregningArsakEnum: Beregningsarsak? = null
    var lonnsvekstInformasjon: LonnsvekstInformasjon? = null
    var merknadListe: List<Merknad> = mutableListOf()
    var gjennomsnittligUttaksgradSisteAr = 0.0

    //--- Extra:

    // from PEN no.nav.domain.pensjon.kjerne.beregning2011.BeregningHoveddel
    var kravId: Long? = null
    var virkTom: Date? = null
    var beregningsinformasjon: SpecialBeregningInformasjon? = null
    var epsMottarPensjon: Boolean = false // deprecated; use beregningInformasjon
    var epsPaavirkerBeregning: Boolean = false // deprecated
    var harGjenlevenderett: Boolean = false // deprecated

    fun epsPaavirkerBeregningen(): Boolean =
        beregningsinformasjon?.let {
            it.epsMottarPensjon || it.epsHarInntektOver2G
        } ?: epsPaavirkerBeregning

    protected constructor() : super()

    protected constructor(source: AbstraktBeregningsResultat) : super() {
        virkFom = source.virkFom?.clone() as? Date
        virkTom = source.virkTom?.clone() as? Date
        merknadListe = source.merknadListe.map(::Merknad).toMutableList()
        pensjonUnderUtbetaling = source.pensjonUnderUtbetaling?.let(::PensjonUnderUtbetaling)
        brukersSivilstandEnum = source.brukersSivilstandEnum
        benyttetSivilstandEnum = source.benyttetSivilstandEnum
        beregningArsakEnum = source.beregningArsakEnum
        lonnsvekstInformasjon = source.lonnsvekstInformasjon?.let(::LonnsvekstInformasjon)
        uttaksgrad = source.uttaksgrad
        gjennomsnittligUttaksgradSisteAr = source.gjennomsnittligUttaksgradSisteAr
        kravId = source.kravId
        beregningsinformasjon = source.beregningsinformasjon?.copy()
        epsMottarPensjon = source.epsMottarPensjon // deprecated
        epsPaavirkerBeregning = source.epsPaavirkerBeregning // deprecated
        harGjenlevenderett = source.harGjenlevenderett // deprecated
    }
    // end extra
}

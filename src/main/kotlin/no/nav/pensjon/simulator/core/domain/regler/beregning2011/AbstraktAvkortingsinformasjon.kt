package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import no.nav.pensjon.simulator.core.domain.regler.enum.InntektsavkortingTypeEnum

/**
 * Inneholder felles felt for avkorting av både UT og BT. Enkelte felt fra tidligere klasse Avkortningsinformasjon.
 */
// 2025-09-03
@JsonSubTypes(
    JsonSubTypes.Type(value = AvkortingsinformasjonUT::class),
    JsonSubTypes.Type(value = AvkortingsinformasjonBT::class),
)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
abstract class AbstraktAvkortingsinformasjon {
    /**
     * Antall måneder før virk.
     */
    var antallMndFor = 0

    /**
     * Antall måneder fom virk.
     */
    var antallMndEtter = 0

    /**
     * Inntekt over dette taket gir ingen ytelse.
     */
    var inntektstak = 0

    /**
     * Beregnet avkortingsbeløp før justering for tidligere fradrag per år.
     */
    var avkortingsbelopPerAr = 0

    /**
     * Angir den netto ytelse som gjenstår å utbetale for året
     */
    var restTilUtbetaling = 0

    /**
     * Angir om inntektsavkorting er gjort med hensyn til etteroppgjør, evt ved revurdering.
     */
    var inntektsavkortingTypeEnum: InntektsavkortingTypeEnum? = null
/*
    constructor() {}

    constructor(source: AbstraktAvkortingsinformasjon) {
        antallMndFor = source.antallMndFor
        antallMndEtter = source.antallMndEtter
        inntektstak = source.inntektstak
        avkortingsbelopPerAr = source.avkortingsbelopPerAr
        restTilUtbetaling = source.restTilUtbetaling
        inntektsavkortingTypeEnum = source.inntektsavkortingTypeEnum
    }*/
}

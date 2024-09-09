package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import no.nav.pensjon.simulator.core.domain.regler.kode.InntektsavkortingTypeCti

/**
 * Inneholder felles felt for avkorting av både UT og BT. Enkelte felt fra tidligere klasse Avkortningsinformasjon.
 */
@JsonSubTypes(
    JsonSubTypes.Type(value = AvkortingsinformasjonUT::class),
    JsonSubTypes.Type(value = AvkortingsinformasjonBT::class),
)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
abstract class AbstraktAvkortingsinformasjon {

    /**
     * Antall måneder før virk.
     */
    var antallMndFor: Int = 0

    /**
     * Antall måneder fom virk.
     */
    var antallMndEtter: Int = 0

    /**
     * Inntekt over dette taket gir ingen ytelse.
     */
    var inntektstak: Int = 0

    /**
     * Beregnet avkortingsbeløp før justering for tidligere fradrag per år.
     */
    var avkortingsbelopPerAr: Int = 0

    /**
     * Angir den netto ytelse som gjenstår å utbetale for året
     */
    var restTilUtbetaling: Int = 0

    /**
     * Angir om inntektsavkorting er gjort med hensyn til etteroppgjør, evt ved revurdering.
     */
    var inntektsavkortingType: InntektsavkortingTypeCti? = null

    constructor() {}

    constructor(abstraktAvkortingsinformasjon: AbstraktAvkortingsinformasjon) {
        antallMndFor = abstraktAvkortingsinformasjon.antallMndFor
        antallMndEtter = abstraktAvkortingsinformasjon.antallMndEtter
        inntektstak = abstraktAvkortingsinformasjon.inntektstak
        avkortingsbelopPerAr = abstraktAvkortingsinformasjon.avkortingsbelopPerAr
        restTilUtbetaling = abstraktAvkortingsinformasjon.restTilUtbetaling
        inntektsavkortingType = abstraktAvkortingsinformasjon.inntektsavkortingType
        if (abstraktAvkortingsinformasjon.inntektsavkortingType != null) {
            inntektsavkortingType = InntektsavkortingTypeCti(abstraktAvkortingsinformasjon.inntektsavkortingType)
        }
    }

    constructor(
            antallMndFor: Int = 0,
            antallMndEtter: Int = 0,
            inntektstak: Int = 0,
            avkortingsbelopPerAr: Int = 0,
            restTilUtbetaling: Int = 0,
            inntektsavkortingType: InntektsavkortingTypeCti? = null
    ) {
        this.antallMndFor = antallMndFor
        this.antallMndEtter = antallMndEtter
        this.inntektstak = inntektstak
        this.avkortingsbelopPerAr = avkortingsbelopPerAr
        this.restTilUtbetaling = restTilUtbetaling
        this.inntektsavkortingType = inntektsavkortingType
    }
}

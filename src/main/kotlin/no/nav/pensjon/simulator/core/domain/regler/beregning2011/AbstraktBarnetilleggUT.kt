package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import no.nav.pensjon.simulator.core.domain.reglerextend.beregning2011.copy

@JsonSubTypes(
    JsonSubTypes.Type(value = BarnetilleggFellesbarnUT::class),
    JsonSubTypes.Type(value = BarnetilleggSerkullsbarnUT::class),
)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
abstract class AbstraktBarnetilleggUT : AbstraktBarnetillegg, UforetrygdYtelseskomponent {

    /**
     * Detaljer rundt avkortning av netto barnetillegg.
     */
    var avkortingsinformasjon: AvkortingsinformasjonBT? = null

    /**
     * Inntektsavkortningsbeløp per år, før justering med differansebeløp
     */
    var avkortningsbelopPerAr = 0

    /**
     * Inntekt som fører til at barnetillegget ikke blir utbetalt
     */
    var inntektstak = 0

    /**
     * Akkumulert netto hittil i året eksklusiv måned for beregningsperiodens fomDato.
     */
    var nettoAkk = 0

    /**
     * Gjenstående beløp brukeren har rett på for året som beregningsperioden starter, og inkluderer måneden det beregnes fra.
     */
    var nettoRestAr = 0

    /**
     * Uttrykker avviket mellom ytelseskomponenten basert på løpende inntektsavkorting og ytelseskomponenten basert på lignet inntekt.
     */
    var periodisertAvvikEtteroppgjor = 0.0

    /**
     * Detaljer rundt reduksjon av brutto barnetillegg.
     */
    var reduksjonsinformasjon: Reduksjonsinformasjon? = null

    /**
     * årsbeløp for delytelsen fra tidligere vedtak (fra tilsvarende beregningsperiode)
     */
    override var tidligereBelopAr = 0

    /**
     * Brukers uføretrygd før justering
     */
    var brukersUforetrygdForJustering = 0

    constructor()

    constructor(source: AbstraktBarnetilleggUT) : super(source) {
        avkortingsinformasjon = source.avkortingsinformasjon?.copy()
        avkortningsbelopPerAr = source.avkortningsbelopPerAr
        inntektstak = source.inntektstak
        nettoAkk = source.nettoAkk
        nettoRestAr = source.nettoRestAr
        periodisertAvvikEtteroppgjor = source.periodisertAvvikEtteroppgjor
        reduksjonsinformasjon = source.reduksjonsinformasjon?.let(::Reduksjonsinformasjon)
        tidligereBelopAr = source.tidligereBelopAr
        brukersUforetrygdForJustering = source.brukersUforetrygdForJustering
    }
}

package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import no.nav.pensjon.simulator.core.domain.regler.Merknad
import no.nav.pensjon.simulator.core.domain.regler.kode.AvkortingsArsakCti
import no.nav.pensjon.simulator.core.domain.regler.kode.FormelKodeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.SakTypeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.YtelsekomponentTypeCti

@JsonSubTypes(
    JsonSubTypes.Type(value = BarnetilleggFellesbarnUT::class),
    JsonSubTypes.Type(value = BarnetilleggSerkullsbarnUT::class),
)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
abstract class AbstraktBarnetilleggUT : AbstraktBarnetillegg, UforetrygdYtelseskomponent {

    /**
     * Inntektsavkortningsbeløp per år, før justering med differansebeløp
     */
    var avkortningsbelopPerAr: Int = 0

    /**
     * Akkumulert netto hittil i året eksklusiv måned for beregningsperiodens fomDato.
     */
    var nettoAkk: Int = 0

    /**
     * Gjenstående beløp brukeren har rett på for året som beregningsperioden starter, og inkluderer måneden det beregnes fra.
     */
    var nettoRestAr: Int = 0

    /**
     * Detaljer rundt avkortning av netto barnetillegg.
     */
    var avkortingsinformasjon: AvkortingsinformasjonBT? = null

    /**
     * Inntekt som fører til at barnetillegget ikke blir utbetalt
     */
    var inntektstak: Int = 0

    /**
     * Uttrykker avviket mellom ytelseskomponenten basert på løpende inntektsavkorting og ytelseskomponenten basert på lignet inntekt.
     */
    var periodisertAvvikEtteroppgjor: Double = 0.0

    /**
     * Detaljer rundt reduksjon av brutto barnetillegg.
     */
    var reduksjonsinformasjon: Reduksjonsinformasjon? = null

    /**
     * Årsbeløp for delytelsen fra tidligere vedtak (fra tilsvarende beregningsperiode)
     */
    override var tidligereBelopAr: Int = 0

    /**
     * Brukers uføretrygd før justering
     */
    var brukersUforetrygdForJustering: Int = 0

    constructor(ab: AbstraktBarnetilleggUT) : super(ab) {
        inntektstak = ab.inntektstak
        nettoAkk = ab.nettoAkk
        nettoRestAr = ab.nettoRestAr
        reduksjonsinformasjon = ab.reduksjonsinformasjon
        avkortingsinformasjon = ab.avkortingsinformasjon
        periodisertAvvikEtteroppgjor = ab.periodisertAvvikEtteroppgjor
        tidligereBelopAr = ab.tidligereBelopAr
        brukersUforetrygdForJustering = ab.brukersUforetrygdForJustering
    }

    constructor(
            /** AbstraktBarnetilleggUT */
            avkortningsbelopPerAr: Int = 0,
            nettoAkk: Int = 0,
            nettoRestAr: Int = 0,
            avkortingsinformasjon: AvkortingsinformasjonBT? = null,
            inntektstak: Int = 0,
            periodisertAvvikEtteroppgjor: Double = 0.0,
            reduksjonsinformasjon: Reduksjonsinformasjon? = null,
            tidligereBelopAr: Int = 0,
            brukersUforetrygdForJustering: Int = 0,
            /** super AbstraktBarnetillegg */
            antallBarn: Int = 0,
            avkortet: Boolean = false,
            btDiff_eos: Int = 0,
            fribelop: Int = 0,
            mpnSatsFT: Double = 0.0,
            proratanevner: Int = 0,
            proratateller: Int = 0,
            samletInntektAvkort: Int = 0,
            tt_anv: Int = 0,
            avkortingsArsakList: MutableList<AvkortingsArsakCti> = mutableListOf(),
            /** super ytelseskomponent */
            brutto: Int = 0,
            netto: Int = 0,
            fradrag: Int = 0,
            bruttoPerAr: Double = 0.0,
            nettoPerAr: Double = 0.0,
            fradragPerAr: Double = 0.0,
            ytelsekomponentType: YtelsekomponentTypeCti,
            merknadListe: MutableList<Merknad> = mutableListOf(),
            fradragsTransaksjon: Boolean = false,
            opphort: Boolean = false,
            sakType: SakTypeCti? = null,
            formelKode: FormelKodeCti,
            reguleringsInformasjon: ReguleringsInformasjon? = null
    ) : super(
            /** super abstraktBarnetillegg */
            antallBarn = antallBarn,
            avkortet = avkortet,
            btDiff_eos = btDiff_eos,
            fribelop = fribelop,
            mpnSatsFT = mpnSatsFT,
            proratanevner = proratanevner,
            proratateller = proratateller,
            samletInntektAvkort = samletInntektAvkort,
            tt_anv = tt_anv,
            avkortingsArsakList = avkortingsArsakList,
            /** super ytelseskomponent */
            brutto = brutto,
            netto = netto,
            fradrag = fradrag,
            bruttoPerAr = bruttoPerAr,
            nettoPerAr = nettoPerAr,
            fradragPerAr = fradragPerAr,
            ytelsekomponentType = ytelsekomponentType,
            merknadListe = merknadListe,
            fradragsTransaksjon = fradragsTransaksjon,
            opphort = opphort,
            sakType = sakType,
            formelKode = formelKode,
            reguleringsInformasjon = reguleringsInformasjon
    ) {
        this.avkortningsbelopPerAr = avkortningsbelopPerAr
        this.nettoAkk = nettoAkk
        this.nettoRestAr = nettoRestAr
        this.avkortingsinformasjon = avkortingsinformasjon
        this.inntektstak = inntektstak
        this.periodisertAvvikEtteroppgjor = periodisertAvvikEtteroppgjor
        this.reduksjonsinformasjon = reduksjonsinformasjon
        this.tidligereBelopAr = tidligereBelopAr
        this.brukersUforetrygdForJustering = brukersUforetrygdForJustering
    }
}

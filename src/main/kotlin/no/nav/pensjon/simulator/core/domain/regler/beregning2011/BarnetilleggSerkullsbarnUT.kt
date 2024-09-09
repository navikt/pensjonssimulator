package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import no.nav.pensjon.simulator.core.domain.regler.Merknad
import no.nav.pensjon.simulator.core.domain.regler.kode.AvkortingsArsakCti
import no.nav.pensjon.simulator.core.domain.regler.kode.FormelKodeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.SakTypeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.YtelsekomponentTypeCti

/**
 * @author Ingleiv Johansen (Accenture) - PK-7250
 * @author Lars Hartvigsen (Decisive) - PK-20946
 */
class BarnetilleggSerkullsbarnUT : AbstraktBarnetilleggUT {

    /**
     * Brukers gjenlevendetillegg før justering.
     */
    var brukersGjenlevendetilleggForJustering: Int = 0

    constructor() : super(
            ytelsekomponentType = YtelsekomponentTypeCti("UT_TSB"),
            formelKode = FormelKodeCti("BTx")
    )

    constructor(barnetilleggFellesbarnUT: BarnetilleggSerkullsbarnUT) : super(barnetilleggFellesbarnUT) {
        brukersGjenlevendetilleggForJustering = barnetilleggFellesbarnUT.brukersGjenlevendetilleggForJustering
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
            ytelsekomponentType: YtelsekomponentTypeCti = YtelsekomponentTypeCti("UT_TSB"),
            merknadListe: MutableList<Merknad> = mutableListOf(),
            fradragsTransaksjon: Boolean = false,
            opphort: Boolean = false,
            sakType: SakTypeCti? = null,
            formelKode: FormelKodeCti = FormelKodeCti("BTx"),
            reguleringsInformasjon: ReguleringsInformasjon? = null
    ) : super(
            /** super AbstraktBarnetilleggUT */
            avkortningsbelopPerAr = avkortningsbelopPerAr,
            nettoAkk = nettoAkk,
            nettoRestAr = nettoRestAr,
            avkortingsinformasjon = avkortingsinformasjon,
            inntektstak = inntektstak,
            periodisertAvvikEtteroppgjor = periodisertAvvikEtteroppgjor,
            reduksjonsinformasjon = reduksjonsinformasjon,
            tidligereBelopAr = tidligereBelopAr,
            brukersUforetrygdForJustering = brukersUforetrygdForJustering,
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
    )
}

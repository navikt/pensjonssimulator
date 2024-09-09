package no.nav.pensjon.simulator.core.domain.regler.beregning

import no.nav.pensjon.simulator.core.domain.regler.Merknad
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.AbstraktBarnetillegg
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.ReguleringsInformasjon
import no.nav.pensjon.simulator.core.domain.regler.kode.AvkortingsArsakCti
import no.nav.pensjon.simulator.core.domain.regler.kode.FormelKodeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.SakTypeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.YtelsekomponentTypeCti

class BarnetilleggSerkullsbarn : AbstraktBarnetillegg {

    constructor() : super(
            ytelsekomponentType = YtelsekomponentTypeCti("TSB"),
            formelKode = FormelKodeCti("BTx")
    )

    constructor(barnetilleggSerkullsbarn: BarnetilleggSerkullsbarn) : super(barnetilleggSerkullsbarn)

    constructor(antallBarn: Int, fribelop: Int, samletInntektAvkort: Int, btDiff_eos: Int) : this() {
        this.antallBarn = antallBarn
        this.fribelop = fribelop
        this.samletInntektAvkort = samletInntektAvkort
        this.btDiff_eos = btDiff_eos
    }

    constructor(
        /** super AbstractBarnetillegg */
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
        ytelsekomponentType: YtelsekomponentTypeCti = YtelsekomponentTypeCti("TSB"),
        merknadListe: MutableList<Merknad> = mutableListOf(),
        fradragsTransaksjon: Boolean = false,
        opphort: Boolean = false,
        sakType: SakTypeCti? = null,
        formelKode: FormelKodeCti = FormelKodeCti("BTx"),
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
    )
}

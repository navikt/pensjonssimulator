package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import no.nav.pensjon.simulator.core.domain.regler.Merknad
import no.nav.pensjon.simulator.core.domain.regler.beregning.Ytelseskomponent
import no.nav.pensjon.simulator.core.domain.regler.kode.FormelKodeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.SakTypeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.YtelsekomponentTypeCti

class AfpKompensasjonstillegg : Ytelseskomponent {
    var referansebelop: Int = 0
    var reduksjonsbelop: Int = 0
    var forholdstallKompensasjonstillegg: Double = 0.0

    constructor(ytelseskomponent: Ytelseskomponent, referansebelop: Int, reduksjonsbelop: Int, forholdstallKompensasjonstillegg: Double) : super(ytelseskomponent) {
        this.referansebelop = referansebelop
        this.reduksjonsbelop = reduksjonsbelop
        this.forholdstallKompensasjonstillegg = forholdstallKompensasjonstillegg
    }

    constructor() : super(ytelsekomponentType = YtelsekomponentTypeCti("AFP_KOMP_TILLEGG"))

    constructor(aAfpKompensasjonstillegg: AfpKompensasjonstillegg) : super(aAfpKompensasjonstillegg) {
        referansebelop = aAfpKompensasjonstillegg.referansebelop
        reduksjonsbelop = aAfpKompensasjonstillegg.reduksjonsbelop
        forholdstallKompensasjonstillegg = aAfpKompensasjonstillegg.forholdstallKompensasjonstillegg
    }

    constructor(
            referansebelop: Int = 0,
            reduksjonsbelop: Int = 0,
            forholdstallKompensasjonstillegg: Double = 0.0,
            /** super Ytelseskomponent*/
            brutto: Int = 0,
            netto: Int = 0,
            fradrag: Int = 0,
            bruttoPerAr: Double = 0.0,
            nettoPerAr: Double = 0.0,
            fradragPerAr: Double = 0.0,
            ytelsekomponentType: YtelsekomponentTypeCti = YtelsekomponentTypeCti("AFP_KOMP_TILLEGG"),
            merknadListe: MutableList<Merknad> = mutableListOf(),
            fradragsTransaksjon: Boolean = false,
            opphort: Boolean = false,
            sakType: SakTypeCti? = null,
            formelKode: FormelKodeCti? = null,
            reguleringsInformasjon: ReguleringsInformasjon? = null
    ) : super(
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
        this.referansebelop = referansebelop
        this.reduksjonsbelop = reduksjonsbelop
        this.forholdstallKompensasjonstillegg = forholdstallKompensasjonstillegg
    }
}

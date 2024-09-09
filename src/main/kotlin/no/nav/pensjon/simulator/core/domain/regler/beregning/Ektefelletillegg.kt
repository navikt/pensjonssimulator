package no.nav.pensjon.simulator.core.domain.regler.beregning

import no.nav.pensjon.simulator.core.domain.regler.Merknad
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.ReguleringsInformasjon
import no.nav.pensjon.simulator.core.domain.regler.kode.AvkortingsArsakCti
import no.nav.pensjon.simulator.core.domain.regler.kode.FormelKodeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.SakTypeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.YtelsekomponentTypeCti

class Ektefelletillegg : Ytelseskomponent {

    var fribelop: Int = 0

    /**
     * Summen av inntektene som kan bli lagt til grunn ved avkorting, selv når det ikke fører til avkorting.
     */
    var samletInntektAvkort: Int = 0

    /**
     * Angir om tillegget er avkortet.
     */
    var avkortet: Boolean = false

    /**
     * Årsaken(e) til avkorting. Satt dersom avkortet er true.
     */
    var arsaksList: MutableList<AvkortingsArsakCti> = mutableListOf()

    /**
     * Angir minste pensjonsnivåsats for ektefelletillegget
     */
    var mpnSatsFT: Double = 0.0

    /**
     * Den anvendte trygdetiden i beregningen av tillegget. Kan være forskjellig fra Beregningen.tt_anv
     */
    var tt_anv: Int = 0

    /**
     * Nedtrappingsgrad brukt ved utfasing av forsørgingstillegg fom 2023.
     */
    var forsorgingstilleggNiva: Int = 100

    /**
     * Telleren i proratabrøken for EØS-avtaleberegnet tillegg
     */
    var proratateller: Int = 0

    /**
     * Telleren i proratabrøken for EØS-avtaleberegnet tillegg
     */
    var proratanevner: Int = 0

    /**
     * Flagg som forteller om ektefelletillegget er skattefritt.
     * Ektefelletillegg som utbetales til AFP og alderspensjonister skal utbetales skattefritt for de
     * som mottar ektefelletillegg pr 31. desember 2010. Fritaket gjelder ikke for de som mister
     * ektefelletillegget for ett eller flere inntektsår etter desember 2010, men senere før det tilbake.
     * Skattefritaket skal ikke gjelde alderspensjonister som tilstås ektefelletillegg med virkning tidligst
     * 1. januar 2011
     */
    var skattefritak: Boolean = false

    constructor(ektefelletillegg: Ektefelletillegg) : super(ektefelletillegg) {
        fribelop = ektefelletillegg.fribelop
        samletInntektAvkort = ektefelletillegg.samletInntektAvkort
        avkortet = ektefelletillegg.avkortet
        mpnSatsFT = ektefelletillegg.mpnSatsFT
        tt_anv = ektefelletillegg.tt_anv
        forsorgingstilleggNiva = ektefelletillegg.forsorgingstilleggNiva
        proratanevner = ektefelletillegg.proratanevner
        proratateller = ektefelletillegg.proratateller
        skattefritak = ektefelletillegg.skattefritak
        arsaksList = mutableListOf()
        for (arsak in ektefelletillegg.arsaksList) {
            arsaksList.add(AvkortingsArsakCti(arsak.kode))
        }
    }

    constructor(fribelop: Int, samletInntektAvkort: Int) : this() {
        this.fribelop = fribelop
        this.samletInntektAvkort = samletInntektAvkort
    }

    constructor() : super(
            ytelsekomponentType = YtelsekomponentTypeCti("ET"),
            formelKode = FormelKodeCti("ETx")
    )

    constructor(
        fribelop: Int = 0,
        samletInntektAvkort: Int = 0,
        avkortet: Boolean = false,
        arsaksList: MutableList<AvkortingsArsakCti> = mutableListOf(),
        mpnSatsFT: Double = 0.0,
        tt_anv: Int = 0,
        forsorgingstilleggNiva: Int = 100,
        proratateller: Int = 0,
        proratanevner: Int = 0,
        skattefritak: Boolean = false,
        /** super Ytelseskomponent*/
            brutto: Int = 0,
        netto: Int = 0,
        fradrag: Int = 0,
        bruttoPerAr: Double = 0.0,
        nettoPerAr: Double = 0.0,
        fradragPerAr: Double = 0.0,
        ytelsekomponentType: YtelsekomponentTypeCti = YtelsekomponentTypeCti("ET"),
        merknadListe: MutableList<Merknad> = mutableListOf(),
        fradragsTransaksjon: Boolean = false,
        opphort: Boolean = false,
        sakType: SakTypeCti? = null,
        formelKode: FormelKodeCti = FormelKodeCti("ETx"),
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
        this.fribelop = fribelop
        this.samletInntektAvkort = samletInntektAvkort
        this.avkortet = avkortet
        this.arsaksList = arsaksList
        this.mpnSatsFT = mpnSatsFT
        this.tt_anv = tt_anv
        this.forsorgingstilleggNiva = forsorgingstilleggNiva
        this.proratateller = proratateller
        this.proratanevner = proratanevner
        this.skattefritak = skattefritak
    }
}

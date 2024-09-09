package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import no.nav.pensjon.simulator.core.domain.regler.Merknad
import no.nav.pensjon.simulator.core.domain.regler.beregning.BarnetilleggFellesbarn
import no.nav.pensjon.simulator.core.domain.regler.beregning.BarnetilleggSerkullsbarn
import no.nav.pensjon.simulator.core.domain.regler.beregning.Ytelseskomponent
import no.nav.pensjon.simulator.core.domain.regler.kode.AvkortingsArsakCti
import no.nav.pensjon.simulator.core.domain.regler.kode.FormelKodeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.SakTypeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.YtelsekomponentTypeCti

@JsonSubTypes(
    JsonSubTypes.Type(value = BarnetilleggSerkullsbarn::class),
    JsonSubTypes.Type(value = BarnetilleggFellesbarn::class),
    JsonSubTypes.Type(value = AbstraktBarnetilleggUT::class),
)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
abstract class AbstraktBarnetillegg : Ytelseskomponent {
    /**
     * Antall barn i kullet.
     */
    var antallBarn: Int = 0

    /**
     * Angir om tillegget er avkortet.
     */
    var avkortet: Boolean = false

    /**
     * Differansetillegg ved barnetillegg. Anvendes dersom primært land for BT er et annet EØS land
     */
    var btDiff_eos: Int = 0

    /**
     * Anvendt fribeløp.
     */
    var fribelop: Int = 0

    /**
     * Angir minste pensjonsnivåsats for ektefelletillegget
     */
    var mpnSatsFT: Double = 0.0

    /**
     * Nevneren i proratabrøken for EØS-avtaleberegnet tillegg
     */
    var proratanevner: Int = 0

    /**
     * Telleren i proratabrøken for EØS-avtaleberegnet tillegg
     */
    var proratateller: Int = 0

    /**
     * Summen av inntektene som kan bli lagt til grunn ved avkorting, selv når det ikke fører til avkorting.
     */
    var samletInntektAvkort: Int = 0

    /**
     * Den anvendte trygdetiden i beregningen av tillegget. Kan være forskjellig fra tt_anv.
     */
    var tt_anv: Int = 0

    /**
     * Nedtrappingsgrad brukt ved utfasing av forsørgingstillegg fom 2023.
     */
    var forsorgingstilleggNiva: Int = 100

    /**
     * Årsaken(e) til avkorting. Satt dersom avkortet er true.
     */
    var avkortingsArsakList: MutableList<AvkortingsArsakCti> = mutableListOf()

    protected constructor(ab: AbstraktBarnetillegg) : super(ab) {
        antallBarn = ab.antallBarn
        avkortet = ab.avkortet
        btDiff_eos = ab.btDiff_eos
        fribelop = ab.fribelop
        mpnSatsFT = ab.mpnSatsFT
        proratanevner = ab.proratanevner
        proratateller = ab.proratateller
        samletInntektAvkort = ab.samletInntektAvkort
        tt_anv = ab.tt_anv
        forsorgingstilleggNiva = ab.forsorgingstilleggNiva
        for (arsak in ab.avkortingsArsakList) {
            avkortingsArsakList.add(AvkortingsArsakCti(arsak.kode))
        }
    }

    constructor(
        antallBarn: Int = 0,
        avkortet: Boolean = false,
        btDiff_eos: Int = 0,
        fribelop: Int = 0,
        mpnSatsFT: Double = 0.0,
        proratanevner: Int = 0,
        proratateller: Int = 0,
        samletInntektAvkort: Int = 0,
        tt_anv: Int = 0,
        forsorgingstilleggNiva: Int = 100,
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
        formelKode: FormelKodeCti? = null,
        reguleringsInformasjon: ReguleringsInformasjon? = null) : super(
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
        this.antallBarn = antallBarn
        this.avkortet = avkortet
        this.btDiff_eos = btDiff_eos
        this.fribelop = fribelop
        this.mpnSatsFT = mpnSatsFT
        this.proratanevner = proratanevner
        this.proratateller = proratateller
        this.samletInntektAvkort = samletInntektAvkort
        this.tt_anv = tt_anv
        this.forsorgingstilleggNiva = forsorgingstilleggNiva
        this.avkortingsArsakList = avkortingsArsakList
    }

}

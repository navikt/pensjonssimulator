package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import no.nav.pensjon.simulator.core.domain.regler.beregning.BarnetilleggFellesbarn
import no.nav.pensjon.simulator.core.domain.regler.beregning.BarnetilleggSerkullsbarn
import no.nav.pensjon.simulator.core.domain.regler.beregning.Ytelseskomponent
import no.nav.pensjon.simulator.core.domain.regler.enum.AvkortningsArsakEnum

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
    var antallBarn = 0

    /**
     * Angir om tillegget er avkortet.
     */
    var avkortet = false

    /**
     * Differansetillegg ved barnetillegg. Anvendes dersom primårt land for BT er et annet EØS land
     */
    var btDiff_eos = 0

    /**
     * Anvendt fribeløp.
     */
    var fribelop = 0

    /**
     * Angir minste pensjonsnivåsats for ektefelletillegget
     */
    var mpnSatsFT = 0.0

    /**
     * Nevneren i prorata-brøken for EØS-avtaleberegnet tillegg
     */
    var proratanevner = 0

    /**
     * Telleren i prorata-brøken for EØS-avtaleberegnet tillegg
     */
    var proratateller = 0

    /**
     * Summen av inntektene som kan bli lagt til grunn ved avkorting, selv når det ikke fører til avkorting.
     */
    var samletInntektAvkort = 0

    /**
     * Den anvendte trygdetiden i beregningen av tillegget. Kan være forskjellig fra tt_anv.
     */
    var tt_anv = 0

    /**
     * Nedtrappingsgrad brukt ved utfasing av forsørgingstillegg fom 2023.
     */
    var forsorgingstilleggNiva = 100

    /**
     * Årsaken(e) til avkorting. Satt dersom avkortet er true.
     */
    var avkortingsArsakListEnum: MutableList<AvkortningsArsakEnum> = mutableListOf()

    constructor()

    constructor(source: AbstraktBarnetillegg) : super(source) {
        antallBarn = source.antallBarn
        avkortet = source.avkortet
        btDiff_eos = source.btDiff_eos
        fribelop = source.fribelop
        mpnSatsFT = source.mpnSatsFT
        proratanevner = source.proratanevner
        proratateller = source.proratateller
        samletInntektAvkort = source.samletInntektAvkort
        tt_anv = source.tt_anv
        forsorgingstilleggNiva = source.forsorgingstilleggNiva

        for (arsak in source.avkortingsArsakListEnum) {
            avkortingsArsakListEnum.add(arsak)
        }
    }
}

package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import no.nav.pensjon.simulator.core.domain.regler.beregning.BarnetilleggFellesbarn
import no.nav.pensjon.simulator.core.domain.regler.beregning.BarnetilleggSerkullsbarn
import no.nav.pensjon.simulator.core.domain.regler.beregning.Ytelseskomponent
import no.nav.pensjon.simulator.core.domain.regler.enum.AvkortningsArsakEnum

// 2025-03-20
@JsonSubTypes(
    JsonSubTypes.Type(value = BarnetilleggSerkullsbarn::class),
    JsonSubTypes.Type(value = BarnetilleggFellesbarn::class),
    JsonSubTypes.Type(value = AbstraktBarnetilleggUT::class),
)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
abstract class AbstraktBarnetillegg : Ytelseskomponent() {
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
     * Nevneren i proratabrøken for EØS-avtaleberegnet tillegg
     */
    var proratanevner = 0

    /**
     * Telleren i proratabrøken for EØS-avtaleberegnet tillegg
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
     * Nedtrappingsgrad brukt ved utfasing av forsærgingstillegg fom 2023.
     */
    var forsorgingstilleggNiva = 100

    /**
     * årsaken(e) til avkorting. Satt dersom avkortet er true.
     */
    var avkortingsArsakListEnum: MutableList<AvkortningsArsakEnum> = mutableListOf()
}

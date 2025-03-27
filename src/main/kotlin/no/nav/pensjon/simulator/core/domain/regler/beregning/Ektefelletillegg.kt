package no.nav.pensjon.simulator.core.domain.regler.beregning

import no.nav.pensjon.simulator.core.domain.regler.enum.AvkortningsArsakEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.FormelKodeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.YtelseskomponentTypeEnum

// 2025-03-19
class Ektefelletillegg : Ytelseskomponent {
    /**
     * Fribeløpet
     */
    var fribelop = 0

    /**
     * Summen av inntektene som kan bli lagt til grunn ved avkorting, selv når det ikke fører til avkorting.
     */
    var samletInntektAvkort = 0

    /**
     * Angir om tillegget er avkortet.
     */
    var avkortet = false

    /**
     * Årsaken(e) til avkorting. Satt dersom avkortet er true.
     */
    var arsaksListEnum: MutableList<AvkortningsArsakEnum> = mutableListOf()

    /**
     * Angir minste pensjonsnivåsats for ektefelletillegget
     */
    var mpnSatsFT = 0.0

    /**
     * Den anvendte trygdetiden i beregningen av tillegget. Kan være forskjellig fra Beregningen.tt_anv
     */
    var tt_anv = 0

    /**
     * Nedtrappingsgrad brukt ved utfasing av forsørgingstillegg fom 2023.
     */
    var forsorgingstilleggNiva = 100

    /**
     * Telleren i prorata-brøken for EØS-avtaleberegnet tillegg
     */
    var proratateller = 0

    /**
     * Telleren i prorata-brøken for EØS-avtaleberegnet tillegg
     */
    var proratanevner = 0

    /**
     * Flagg som forteller om ektefelletillegget er skattefritt.
     * Ektefelletillegg som utbetales til AFP og alderspensjonister skal utbetales skattefritt for de
     * som mottar ektefelletillegg pr 31. desember 2010. Fritaket gjelder ikke for de som mister
     * ektefelletillegget for ett eller flere inntektsår etter desember 2010, men senere før det tilbake.
     * Skattefritaket skal ikke gjelde alderspensjonister som tilstøs ektefelletillegg med virkning tidligst
     * 1. januar 2011
     */
    var skattefritak = false

    override var ytelsekomponentTypeEnum: YtelseskomponentTypeEnum = YtelseskomponentTypeEnum.ET

    constructor() {
        formelKodeEnum = FormelKodeEnum.ETx
    }

    constructor(source: Ektefelletillegg) : super(source) {
        fribelop = source.fribelop
        samletInntektAvkort = source.samletInntektAvkort
        avkortet = source.avkortet
        mpnSatsFT = source.mpnSatsFT
        tt_anv = source.tt_anv
        forsorgingstilleggNiva = source.forsorgingstilleggNiva
        proratanevner = source.proratanevner
        proratateller = source.proratateller
        skattefritak = source.skattefritak

        for (arsak in source.arsaksListEnum) {
            arsaksListEnum.add(arsak)
        }
    }
}

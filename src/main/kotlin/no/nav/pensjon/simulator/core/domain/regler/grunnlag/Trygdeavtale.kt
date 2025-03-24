package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import no.nav.pensjon.simulator.core.domain.regler.enum.AvtaleDatoEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.AvtaleKritEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.AvtaletypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.LandkodeEnum
import java.util.*

// Checked 2025-02-28
class Trygdeavtale {
    /**
     * Hvilket land personen bor i. Se /2/, arkfane K_AVTALELAND_T
     */
    var bostedslandEnum: LandkodeEnum? = null

    /**
     * Hva slags type avtale som er inngått. Se /2/, arkfane K_AVTALE_T.
     */
    var avtaleTypeEnum: AvtaletypeEnum? = null

    /**
     * Felt for å registrere når avtalen tro i kraft, hvis avtaletypen tilsier at
     * landet har flere mulige avtaler med Norge. Se /2/, arkfane K_AVTALE_DATO.
     */
    var avtaledatoEnum: AvtaleDatoEnum? = null

    /**
     * Felt for å registrere kriterier som er oppfylt for å omfattes av trygdeavtalen.
     * Se /2/, arkfane K_AVTALE_KRIT_T.
     */
    var avtaleKriterieEnum: AvtaleKritEnum? = null

    /**
     * Angir om personen omfattes av avtalens personkrets
     */
    var omfattesavAvtalensPersonkrets = false

    /**
     * Dato for kravdato i avtale
     */
    var kravDatoIAvtaleland: Date? = null

    constructor()

    constructor(source: Trygdeavtale) : this() {
        bostedslandEnum = source.bostedslandEnum
        avtaleTypeEnum = source.avtaleTypeEnum
        avtaledatoEnum = source.avtaledatoEnum
        avtaleKriterieEnum = source.avtaleKriterieEnum
        omfattesavAvtalensPersonkrets = source.omfattesavAvtalensPersonkrets
        kravDatoIAvtaleland = source.kravDatoIAvtaleland?.clone() as? Date
    }
}

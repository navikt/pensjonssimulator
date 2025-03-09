package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import no.nav.pensjon.simulator.core.domain.regler.enum.AvtaleDatoEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.AvtaleKritEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.AvtaletypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.LandkodeEnum
import no.nav.pensjon.simulator.core.domain.regler.kode.AvtaleDatoCti
import no.nav.pensjon.simulator.core.domain.regler.kode.AvtaleKritCti
import no.nav.pensjon.simulator.core.domain.regler.kode.AvtaleTypeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.LandCti
import java.util.*

// Checked 2025-02-28
class Trygdeavtale {
    /**
     * Hvilket land personen bor i. Se /2/, arkfane K_AVTALELAND_T
     */
    var bostedsland: LandCti? = null
    var bostedslandEnum: LandkodeEnum? = null

    /**
     * Hva slags type avtale som er inngått. Se /2/, arkfane K_AVTALE_T.
     */
    var avtaleType: AvtaleTypeCti? = null
    var avtaleTypeEnum: AvtaletypeEnum? = null

    /**
     * Felt for å registrere når avtalen tro i kraft, hvis avtaletypen tilsier at
     * landet har flere mulige avtaler med Norge. Se /2/, arkfane K_AVTALE_DATO.
     */
    var avtaledato: AvtaleDatoCti? = null
    var avtaledatoEnum: AvtaleDatoEnum? = null

    /**
     * Felt for å registrere kriterier som er oppfylt for å omfattes av trygdeavtalen.
     * Se /2/, arkfane K_AVTALE_KRIT_T.
     */
    var avtaleKriterie: AvtaleKritCti? = null
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
        bostedsland = source.bostedsland?.let(::LandCti)
        bostedslandEnum = source.bostedslandEnum
        avtaleType = source.avtaleType?.let(::AvtaleTypeCti)
        avtaleTypeEnum = source.avtaleTypeEnum
        avtaledato = source.avtaledato?.let(::AvtaleDatoCti)
        avtaledatoEnum = source.avtaledatoEnum
        avtaleKriterie = source.avtaleKriterie?.let(::AvtaleKritCti)
        avtaleKriterieEnum = source.avtaleKriterieEnum
        omfattesavAvtalensPersonkrets = source.omfattesavAvtalensPersonkrets
        kravDatoIAvtaleland = source.kravDatoIAvtaleland?.clone() as? Date
    }

    override fun toString(): String {
        val TAB = "    "

        val retValue = StringBuilder()

        retValue.append("Trygdeavtale ( ").append(super.toString()).append(TAB).append("bostedsland = ")
            .append(bostedsland).append(TAB).append("avtaleType = ").append(avtaleType)
            .append(TAB).append("avtaledato = ").append(avtaledato).append(TAB).append("avtaleKriterie = ")
            .append(avtaleKriterie).append(TAB)
            .append("omfattesavAvtalensPersonkrets = ").append(omfattesavAvtalensPersonkrets).append(TAB)
            .append("kravDatoIAvtaleland = ").append(kravDatoIAvtaleland)
            .append(TAB).append(" )")

        return retValue.toString()
    }
}

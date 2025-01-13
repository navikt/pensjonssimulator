package no.nav.pensjon.simulator.sak.client.pen.acl

import no.nav.pensjon.simulator.core.util.toNorwegianLocalDate
import no.nav.pensjon.simulator.core.virkning.FoersteVirkningDato
import no.nav.pensjon.simulator.core.virkning.FoersteVirkningDatoCombo

object PenPersonVirkningDatoResultMapper {

    /**
     * NB: The 'person' and 'foersteVirkningDatoGrunnlagListe' fields are just assigned by reference
     * from the DTO (not mapped to other objects);
     * it is assumed that PEN returns regler-compatible response body
     */
    fun fromDto(source: PenPersonVirkningDatoResult) =
        FoersteVirkningDatoCombo(
            person = source.person,
            foersteVirkningDatoListe = source.forsteVirkningsdatoListe.map(::foersteVirkningDato),
            foersteVirkningDatoGrunnlagListe = source.forsteVirkningsdatoGrunnlagListe
        )

    private fun foersteVirkningDato(source: PenFoersteVirkningDato) =
        FoersteVirkningDato(
            sakType = source.sakType,
            kravlinjeType = source.kravlinjeType,
            virkningDato = source.virkningsdato?.toNorwegianLocalDate(),
            annenPerson = source.annenPerson
        )
}

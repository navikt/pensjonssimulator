package no.nav.pensjon.simulator.beholdning.client.pen.acl

import no.nav.pensjon.simulator.beholdning.BeholdningerMedGrunnlagSpec
import no.nav.pensjon.simulator.beholdning.BeholdningerMedGrunnlagPersonSpec

/**
 * Maps a 'beholdninger med grunnlag' specification object from domain variant to DTO variant.
 * The DTO (data transfer object) is used in calls to PEN.
 */
object PenBeholdningerMedGrunnlagSpecMapper {

    fun toDto(source: BeholdningerMedGrunnlagSpec) =
        PenBeholdningerMedGrunnlagSpec(
            pid = source.pid.value,
            hentPensjonspoeng = source.hentPensjonspoeng,
            hentGrunnlagForOpptjeninger = source.hentGrunnlagForOpptjeninger,
            hentBeholdninger = source.hentBeholdninger,
            harUfoeretrygdKravlinje = source.harUfoeretrygdKravlinje,
            regelverkType = source.regelverkType,
            sakType = source.sakType,
            personSpecListe = source.personSpecListe.map(::personligBeholdningSpec),
            soekerSpec = source.soekerSpec.let(::personligBeholdningSpec)
        )

    private fun personligBeholdningSpec(source: BeholdningerMedGrunnlagPersonSpec) =
        PenBeholdningerMedGrunnlagPersonSpec(
            pid = source.pid.value,
            sisteGyldigeOpptjeningAar = source.sisteGyldigeOpptjeningAar,
            isGrunnlagRolleSoeker = source.isGrunnlagRolleSoeker
        )
}

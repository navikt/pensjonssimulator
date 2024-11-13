package no.nav.pensjon.simulator.beholdning.client.pen.acl

import no.nav.pensjon.simulator.core.domain.regler.enum.RegelverkTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.SakTypeEnum

/**
 * Corresponds with BeholdningerMedGrunnlagSpec in PEN (API provider).
 * Any changes to this class must be aligned with PEN.
 */
data class PenBeholdningerMedGrunnlagSpec(
    val pid: String,
    val hentPensjonspoeng: Boolean,
    val hentGrunnlagForOpptjeninger: Boolean,
    val hentBeholdninger: Boolean,
    val harUfoeretrygdKravlinje: Boolean,
    val regelverkType: RegelverkTypeEnum?,
    val sakType: SakTypeEnum?,
    val personSpecListe: List<PenBeholdningerMedGrunnlagPersonSpec>,
    val soekerSpec: PenBeholdningerMedGrunnlagPersonSpec
)

/**
 * Corresponds with BeholdningerMedGrunnlagPersonSpec in PEN (API provider).
 * Any changes to this class must be aligned with PEN.
 */
data class PenBeholdningerMedGrunnlagPersonSpec(
    val pid: String,
    val sisteGyldigeOpptjeningAar: Int,
    val isGrunnlagRolleSoeker: Boolean
)

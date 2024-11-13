package no.nav.pensjon.simulator.beholdning

import no.nav.pensjon.simulator.core.domain.regler.enum.RegelverkTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.SakTypeEnum
import no.nav.pensjon.simulator.person.Pid

/**
 * Specifies input to the 'beholdninger med grunnlag' service.
 */
data class BeholdningerMedGrunnlagSpec(
    val pid: Pid,
    val hentPensjonspoeng: Boolean,
    val hentGrunnlagForOpptjeninger: Boolean,
    val hentBeholdninger: Boolean,
    val harUfoeretrygdKravlinje: Boolean,
    val regelverkType: RegelverkTypeEnum?,
    val sakType: SakTypeEnum?,
    val personSpecListe: List<BeholdningerMedGrunnlagPersonSpec>,
    val soekerSpec: BeholdningerMedGrunnlagPersonSpec
)

data class BeholdningerMedGrunnlagPersonSpec(
    val pid: Pid,
    val sisteGyldigeOpptjeningAar: Int,
    val isGrunnlagRolleSoeker: Boolean
)

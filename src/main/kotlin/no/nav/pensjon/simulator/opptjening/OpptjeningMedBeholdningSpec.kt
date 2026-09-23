package no.nav.pensjon.simulator.opptjening

import no.nav.pensjon.simulator.core.domain.regler.enum.RegelverkTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.SakTypeEnum
import no.nav.pensjon.simulator.person.Pid

/**
 * Spesifiserer hva opptjeningstjenesten skal utføre.
 */
data class OpptjeningMedBeholdningSpec(
    val pid: Pid,
    val hentPensjonspoeng: Boolean,
    val hentGrunnlagForOpptjeninger: Boolean,
    val hentBeholdninger: Boolean,
    val harUfoeretrygdKravlinje: Boolean,
    val regelverkType: RegelverkTypeEnum?,
    val sakType: SakTypeEnum?,
    val personSpecListe: List<OpptjeningMedBeholdningPersonSpec>,
    val soekerSpec: OpptjeningMedBeholdningPersonSpec
) {
    fun medBeholdninger() =
        copy(hentBeholdninger = true)
}

data class OpptjeningMedBeholdningPersonSpec(
    val pid: Pid,
    val sisteGyldigeOpptjeningAar: Int,
    val isGrunnlagRolleSoeker: Boolean
)
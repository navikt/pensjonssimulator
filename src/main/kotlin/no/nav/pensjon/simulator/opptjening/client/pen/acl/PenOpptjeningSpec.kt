package no.nav.pensjon.simulator.opptjening.client.pen.acl

import no.nav.pensjon.simulator.core.domain.regler.enum.RegelverkTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.SakTypeEnum
import no.nav.pensjon.simulator.opptjening.OpptjeningMedBeholdningPersonSpec
import no.nav.pensjon.simulator.opptjening.OpptjeningMedBeholdningSpec

/**
 * Corresponds with BeholdningerMedGrunnlagSpec in PEN (API provider).
 * Any changes to this class must be aligned with PEN.
 */
data class PenOpptjeningSpec(
    val pid: String,
    val hentPensjonspoeng: Boolean,
    val hentOpptjeningGrunnlag: Boolean,
    val hentBeholdninger: Boolean,
    val harUfoeretrygdKravlinje: Boolean,
    val regelverkType: RegelverkTypeEnum?,
    val sakType: SakTypeEnum?,
    val personSpecListe: List<PenOpptjeningPersonSpec>,
    val soekerSpec: PenOpptjeningPersonSpec
) {
    companion object {
        fun fromInternalValue(source: OpptjeningMedBeholdningSpec) =
            PenOpptjeningSpec(
                pid = source.pid.value,
                hentPensjonspoeng = source.hentPensjonspoeng,
                hentOpptjeningGrunnlag = source.hentGrunnlagForOpptjeninger,
                hentBeholdninger = source.hentBeholdninger,
                harUfoeretrygdKravlinje = source.harUfoeretrygdKravlinje,
                regelverkType = source.regelverkType,
                sakType = source.sakType,
                personSpecListe = source.personSpecListe.map(PenOpptjeningPersonSpec::fromInternalValue),
                soekerSpec = source.soekerSpec.let(PenOpptjeningPersonSpec::fromInternalValue)
            )
    }
}

/**
 * Corresponds with BeholdningerMedGrunnlagPersonSpec in PEN (API provider).
 * Any changes to this class must be aligned with PEN.
 */
data class PenOpptjeningPersonSpec(
    val pid: String,
    val sisteGyldigeOpptjeningAar: Int,
    val isGrunnlagRolleSoeker: Boolean
) {
    companion object {
        fun fromInternalValue(source: OpptjeningMedBeholdningPersonSpec) =
            PenOpptjeningPersonSpec(
                pid = source.pid.value,
                sisteGyldigeOpptjeningAar = source.sisteGyldigeOpptjeningAar,
                isGrunnlagRolleSoeker = source.isGrunnlagRolleSoeker
            )
    }
}
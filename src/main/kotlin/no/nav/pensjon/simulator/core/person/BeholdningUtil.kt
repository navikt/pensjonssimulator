package no.nav.pensjon.simulator.core.person

import no.nav.pensjon.simulator.core.domain.regler.enum.GrunnlagsrolleEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.SakTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.PersonDetalj
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Persongrunnlag
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.opptjening.OpptjeningMedBeholdningPersonSpec
import no.nav.pensjon.simulator.opptjening.OpptjeningMedBeholdningSpec
import no.nav.pensjon.simulator.person.Pid

object BeholdningUtil {

    fun opptjeningSpec(pid: Pid, persongrunnlag: Persongrunnlag, kravhode: Kravhode) =
        OpptjeningMedBeholdningSpec(
            pid,
            hentPensjonspoeng = true,
            hentGrunnlagForOpptjeninger = true,
            hentBeholdninger = true,
            harUfoeretrygdKravlinje = kravhode.isUforetrygd(),
            regelverkType = kravhode.regelverkTypeEnum,
            sakType = kravhode.sakType?.let { SakTypeEnum.valueOf(it.name) },
            personSpecListe = listOf(personligOpptjeningSpec(pid, persongrunnlag)),
            soekerSpec = personligOpptjeningSpec(pid, persongrunnlag) //TODO redundant value?
        )

    private fun personligOpptjeningSpec(pid: Pid, grunnlag: Persongrunnlag) =
        OpptjeningMedBeholdningPersonSpec(
            pid,
            sisteGyldigeOpptjeningAar = grunnlag.sisteGyldigeOpptjeningsAr,
            isGrunnlagRolleSoeker = soekerIBruk(grunnlag) != null
        )

    private fun soekerIBruk(grunnlag: Persongrunnlag): PersonDetalj? =
        grunnlag.findPersonDetaljIPersongrunnlag(
            rolle = GrunnlagsrolleEnum.SOKER,
            checkBruk = true
        )
}
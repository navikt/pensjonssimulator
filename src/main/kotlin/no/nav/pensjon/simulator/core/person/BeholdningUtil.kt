package no.nav.pensjon.simulator.core.person

import no.nav.pensjon.simulator.beholdning.BeholdningerMedGrunnlagPersonSpec
import no.nav.pensjon.simulator.beholdning.BeholdningerMedGrunnlagSpec
import no.nav.pensjon.simulator.core.domain.regler.enum.GrunnlagsrolleEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.SakTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.PersonDetalj
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Persongrunnlag
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.person.Pid

object BeholdningUtil {

    fun beholdningSpec(pid: Pid, persongrunnlag: Persongrunnlag, kravhode: Kravhode) =
        BeholdningerMedGrunnlagSpec(
            pid,
            hentPensjonspoeng = true,
            hentGrunnlagForOpptjeninger = true,
            hentBeholdninger = false,
            harUfoeretrygdKravlinje = kravhode.isUforetrygd(),
            regelverkType = kravhode.regelverkTypeEnum,
            sakType = kravhode.sakType?.let { SakTypeEnum.valueOf(it.name) },
            personSpecListe = listOf(persongrunnlag.let(::personligBeholdningSpec)),
            soekerSpec = persongrunnlag.let(::personligBeholdningSpec) //TODO redundant value?
        )

    private fun personligBeholdningSpec(grunnlag: Persongrunnlag) =
        BeholdningerMedGrunnlagPersonSpec(
            pid = grunnlag.penPerson?.pid!!,
            sisteGyldigeOpptjeningAar = grunnlag.sisteGyldigeOpptjeningsAr,
            isGrunnlagRolleSoeker = soekerIBruk(grunnlag) != null
        )

    private fun soekerIBruk(grunnlag: Persongrunnlag): PersonDetalj? =
        grunnlag.findPersonDetaljIPersongrunnlag(
            grunnlagsrolle = GrunnlagsrolleEnum.SOKER,
            checkBruk = true
        )
}

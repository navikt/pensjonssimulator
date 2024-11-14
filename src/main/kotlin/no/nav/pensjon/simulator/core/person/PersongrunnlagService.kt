package no.nav.pensjon.simulator.core.person

import no.nav.pensjon.simulator.beholdning.BeholdningerMedGrunnlagPersonSpec
import no.nav.pensjon.simulator.beholdning.BeholdningerMedGrunnlagService
import no.nav.pensjon.simulator.beholdning.BeholdningerMedGrunnlagSpec
import no.nav.pensjon.simulator.core.domain.regler.PenPerson
import no.nav.pensjon.simulator.core.domain.regler.enum.GrunnlagsrolleEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.SakTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Pensjonsbeholdning
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Persongrunnlag
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.person.Pid
import org.springframework.stereotype.Service

@Service
class PersongrunnlagService(
    private val beholdningService: BeholdningerMedGrunnlagService,
    private val persongrunnlagMapper: PersongrunnlagMapper
) {
    // OpprettKravHodeHelper.opprettPersongrunnlagForBruker
    fun addSoekerGrunnlagToKravhode(
        spec: SimuleringSpec,
        kravhode: Kravhode,
        person: PenPerson
    ): Kravhode {
        val persongrunnlag = persongrunnlagMapper.mapToPersongrunnlag(person, spec)
        kravhode.persongrunnlagListe.add(persongrunnlag)

        addBeholdningerMedGrunnlagToPersongrunnlag(
            persongrunnlag,
            kravhode,
            pid = person.pid!!,
            hentBeholdninger = false
        )

        return kravhode
    }

    // OpprettKravHodeHelper.oppdaterGrunnlagMedBeholdninger
    private fun addBeholdningerMedGrunnlagToPersongrunnlag(
        persongrunnlag: Persongrunnlag,
        kravhode: Kravhode,
        pid: Pid,
        hentBeholdninger: Boolean
    ) {
        with(beholdningService.getBeholdningerMedGrunnlag(beholdningSpec(pid, kravhode))) {
            persongrunnlag.opptjeningsgrunnlagListe = opptjeningGrunnlagListe.toMutableList()
            persongrunnlag.omsorgsgrunnlagListe = omsorgGrunnlagListe.toMutableList()
            persongrunnlag.inntektsgrunnlagListe = inntektGrunnlagListe.toMutableList()
            persongrunnlag.dagpengegrunnlagListe = dagpengerGrunnlagListe.toMutableList()
            persongrunnlag.forstegangstjenestegrunnlag = foerstegangstjeneste

            if (hentBeholdninger) {
                beholdningListe.filterIsInstance<Pensjonsbeholdning>().forEach(persongrunnlag.beholdninger::add)
            }
        }
    }

    private fun beholdningSpec(pid: Pid, kravhode: Kravhode) =
        BeholdningerMedGrunnlagSpec(
            pid,
            hentPensjonspoeng = true,
            hentGrunnlagForOpptjeninger = true,
            hentBeholdninger = false,
            harUfoeretrygdKravlinje = kravhode.isUforetrygd(),
            regelverkType = kravhode.regelverkTypeEnum,
            sakType = kravhode.sakType?.let { SakTypeEnum.valueOf(it.name) },
            personSpecListe = kravhode.persongrunnlagListe.map(::personligBeholdningSpec),
            soekerSpec = kravhode.hentPersongrunnlagForSoker().let(::personligBeholdningSpec)
        )

    private fun personligBeholdningSpec(persongrunnlag: Persongrunnlag) =
        BeholdningerMedGrunnlagPersonSpec(
            pid = persongrunnlag.penPerson?.pid!!,
            sisteGyldigeOpptjeningAar = persongrunnlag.sisteGyldigeOpptjeningsAr,

            isGrunnlagRolleSoeker = persongrunnlag.findPersonDetaljIPersongrunnlag(
                grunnlagsrolle = GrunnlagsrolleEnum.SOKER,
                checkBruk = true
            ) != null
        )
}

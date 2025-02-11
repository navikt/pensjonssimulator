package no.nav.pensjon.simulator.core.person

import no.nav.pensjon.simulator.beholdning.BeholdningerMedGrunnlagService
import no.nav.pensjon.simulator.core.domain.regler.PenPerson
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Pensjonsbeholdning
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Persongrunnlag
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.person.BeholdningUtil.beholdningSpec
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.person.Pid
import org.springframework.stereotype.Service

@Service
class PersongrunnlagService(
    private val beholdningService: BeholdningerMedGrunnlagService,
    private val persongrunnlagMapper: PersongrunnlagMapper
) {
    // OpprettKravHodeHelper.opprettPersongrunnlagForBruker
    fun getPersongrunnlagForSoeker(spec: SimuleringSpec, kravhode: Kravhode, person: PenPerson): Persongrunnlag {
        val persongrunnlag = persongrunnlagMapper.mapToPersongrunnlag(person, spec)

        addBeholdningerMedGrunnlagToPersongrunnlag(
            persongrunnlag,
            kravhode,
            pid = person.pid!!,
            hentBeholdninger = false
        )

        return persongrunnlag
    }

    // OpprettKravHodeHelper.oppdaterGrunnlagMedBeholdninger
    fun addBeholdningerMedGrunnlagToPersongrunnlag(
        persongrunnlag: Persongrunnlag,
        kravhode: Kravhode,
        pid: Pid,
        hentBeholdninger: Boolean
    ) {
        with(beholdningService.getBeholdningerMedGrunnlag(beholdningSpec(pid, persongrunnlag, kravhode))) {
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
}

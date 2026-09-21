package no.nav.pensjon.simulator.core.person

import no.nav.pensjon.simulator.core.domain.regler.PenPerson
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Persongrunnlag
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.person.BeholdningUtil.opptjeningSpec
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.opptjening.OpptjeningMedBeholdningService
import no.nav.pensjon.simulator.opptjening.Pensjonsbeholdning
import no.nav.pensjon.simulator.person.Pid
import org.springframework.stereotype.Service
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Pensjonsbeholdning as ReglerPensjonsbeholdning

@Service
class PersongrunnlagService(
    private val opptjeningService: OpptjeningMedBeholdningService,
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
        val spec = opptjeningSpec(pid, persongrunnlag, kravhode)

        with(opptjeningService.pensjonsopptjening(spec)) {
            persongrunnlag.opptjeningsgrunnlagListe = opptjeningGrunnlagListe.toMutableList()
            persongrunnlag.omsorgsgrunnlagListe = omsorgGrunnlagListe.toMutableList()
            persongrunnlag.inntektsgrunnlagListe = inntektGrunnlagListe.toMutableList()
            persongrunnlag.dagpengegrunnlagListe = dagpengerGrunnlagListe.toMutableList()
            persongrunnlag.forstegangstjenestegrunnlag = forstegangstjeneste

            if (hentBeholdninger) {
                beholdningListe.map(::reglerFormat).forEach(persongrunnlag.beholdninger::add)
            }
        }
    }

    private fun reglerFormat(source: Pensjonsbeholdning) =
        ReglerPensjonsbeholdning().apply {
            ar = source.aar
            fomLd = source.fom
            tomLd = source.tom
            totalbelop = source.totalbeloep
            opptjening = source.opptjening
            lonnsvekstInformasjon = source.loennsvekstinformasjon
            reguleringsInformasjon = source.reguleringsinformasjon
            formelKodeEnum = source.formelkode
            merknadListe = source.merknadListe.toMutableList()
        }
}
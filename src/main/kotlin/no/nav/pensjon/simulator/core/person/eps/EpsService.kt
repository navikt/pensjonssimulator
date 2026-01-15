package no.nav.pensjon.simulator.core.person.eps

import no.nav.pensjon.simulator.core.domain.Avdoed
import no.nav.pensjon.simulator.core.domain.regler.enum.GrunnlagkildeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.InntekttypeEnum
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Inntektsgrunnlag
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Persongrunnlag
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.inntekt.OpptjeningUpdater
import no.nav.pensjon.simulator.core.krav.Inntekt
import no.nav.pensjon.simulator.core.person.PersongrunnlagMapper
import no.nav.pensjon.simulator.core.person.PersongrunnlagService
import no.nav.pensjon.simulator.core.person.eps.EpsUtil.erEps
import no.nav.pensjon.simulator.core.person.eps.EpsUtil.gjelderGjenlevenderett
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.core.util.toNorwegianDateAtNoon
import no.nav.pensjon.simulator.core.util.toNorwegianLocalDate
import no.nav.pensjon.simulator.generelt.GenerelleDataHolder
import no.nav.pensjon.simulator.person.GeneralPersonService
import no.nav.pensjon.simulator.person.PersonService
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.tech.time.DateUtil.foersteDag
import no.nav.pensjon.simulator.tech.time.Time
import no.nav.pensjon.simulator.validity.BadSpecException
import org.springframework.stereotype.Service
import java.time.LocalDate

/**
 * Functionality related to 'ektefelle/partner/samboer' (EPS).
 */
@Service
class EpsService(
    private val pensjonPersonService: PersonService,
    private val generalPersonService: GeneralPersonService,
    private val persongrunnlagService: PersongrunnlagService,
    private val persongrunnlagMapper: PersongrunnlagMapper,
    private val opptjeningUpdater: OpptjeningUpdater,
    private val generelleDataHolder: GenerelleDataHolder,
    private val time: Time
) {
    // OpprettKravHodeHelper.opprettPersongrunnlagForEPS
    fun addPersongrunnlagForEpsToKravhode(spec: SimuleringSpec, kravhode: Kravhode, grunnbeloep: Int) {
        if (gjelderGjenlevenderett(spec.type)) {
            kravhode.persongrunnlagListe.add(gjenlevenderettPersongrunnlag(spec.avdoed, spec.pid, kravhode))
        } else if (erEps(spec.sivilstatus)) {
            kravhode.persongrunnlagListe.add(persongrunnlagBasedOnSivilstatus(spec, grunnbeloep))
        }
    }

    // OpprettKravHodeHelper.createPersongrunnlagBasedOnSivilstatus
    private fun persongrunnlagBasedOnSivilstatus(spec: SimuleringSpec, grunnbeloep: Int): Persongrunnlag {
        val grunnlag = persongrunnlagMapper.mapToEpsPersongrunnlag(
            sivilstatus = spec.sivilstatus,
            foedselsdato = foedselsdato(spec)
        )

        if (spec.epsHarInntektOver2G) {
            val today: LocalDate = time.today()

            val foersteUttakDato: LocalDate =
                spec.foersteUttakDato?.let { if (it.isBefore(today)) it else today } ?: today

            grunnlag.inntektsgrunnlagListe.add(
                epsInntektGrunnlag(grunnbeloep, foersteUttakAar = foersteUttakDato.year)
            )
        }

        return grunnlag
    }

    // OpprettKravHodeHelper.createPersongrunnlagInCaseOfGjenlevenderett
    private fun gjenlevenderettPersongrunnlag(avdoed: Avdoed?, soekerPid: Pid?, kravhode: Kravhode): Persongrunnlag {
        // Del 1
        val avdoedPerson = avdoed?.pid?.let(pensjonPersonService::person)
            ?: throw BadSpecException("Gjenlevenderett: Avdød person med PID ${avdoed?.pid} ikke funnet")

        val persongrunnlag: Persongrunnlag =
            persongrunnlagMapper.avdoedPersongrunnlag(avdoed, avdoedPerson, soekerPid).apply {
                sisteGyldigeOpptjeningsAr = generelleDataHolder.getSisteGyldigeOpptjeningsaar()
            }

        // Del 2
        persongrunnlagService.addBeholdningerMedGrunnlagToPersongrunnlag(
            persongrunnlag,
            kravhode,
            pid = avdoed.pid,
            hentBeholdninger = true
        )

        filterAndUpdateInntektsgrunnlaglistOnPersongrunnlag(persongrunnlag)

        // Del 3.1
        val inntekt = Inntekt(
            inntektAar = time.today().year - 1,
            beloep = avdoed.inntektFoerDoed.toLong(),
            inntektType = null
        )

        val epsInntektListe: MutableList<Inntekt> = mutableListOf(inntekt)

        // Del 3.2
        persongrunnlag.opptjeningsgrunnlagListe =
            opptjeningUpdater.oppdaterOpptjeningsgrunnlagFraInntekter(
                originalGrunnlagListe = persongrunnlag.opptjeningsgrunnlagListe,
                inntektListe = epsInntektListe,
                foedselsdato = persongrunnlag.fodselsdato?.toNorwegianLocalDate()
            )

        return persongrunnlag
    }

    // OpprettKravHodeHelper.filterAndUpdateInntektsgrunnlaglistOnPersongrunnlag
    private fun filterAndUpdateInntektsgrunnlaglistOnPersongrunnlag(persongrunnlag: Persongrunnlag) {
        persongrunnlag.inntektsgrunnlagListe = persongrunnlag.inntektsgrunnlagListe.filter {
            it.bruk == true && InntekttypeEnum.FPI != it.inntektTypeEnum // FPI: Forventet pensjongivende inntekt
        }.toMutableList()
    }

    private fun foedselsdato(spec: SimuleringSpec): LocalDate =
        spec.pid?.let(generalPersonService::foedselsdato) ?: foersteDag(spec.foedselAar)
    // NB: Not using spec.foedselDato here (for unknown reasons)

    companion object {
        const val EPS_GRUNNBELOEP_MULTIPLIER = 3 // greater than 2 (due to 2G income limit for EPS)

        // OpprettKravHodeHelper.createInntektsgrunnlagForBrukerOrEps (special EPS variant)
        private fun epsInntektGrunnlag(grunnbeloep: Int, foersteUttakAar: Int) =
            Inntektsgrunnlag().apply {
                belop = EPS_GRUNNBELOEP_MULTIPLIER * grunnbeloep
                fom = foersteDag(foersteUttakAar).toNorwegianDateAtNoon() // noon: ref. GrunnlagToReglerMapper.mapToInntektsgrunnlag in PEN
                tom = null
                grunnlagKildeEnum = GrunnlagkildeEnum.BRUKER
                inntektTypeEnum = InntekttypeEnum.FPI
                bruk = true
            }
    }
}

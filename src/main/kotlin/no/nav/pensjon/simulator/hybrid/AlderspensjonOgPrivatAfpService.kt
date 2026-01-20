package no.nav.pensjon.simulator.hybrid

import no.nav.pensjon.simulator.alderspensjon.spec.SimuleringSpecValidator.validate
import no.nav.pensjon.simulator.core.SimulatorCore
import no.nav.pensjon.simulator.core.exception.*
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.tech.time.Time
import no.nav.pensjon.simulator.tech.validation.InvalidEnumValueException
import no.nav.pensjon.simulator.tech.web.BadRequestException
import no.nav.pensjon.simulator.tech.web.EgressException
import no.nav.pensjon.simulator.validity.BadSpecException
import no.nav.pensjon.simulator.validity.Problem
import no.nav.pensjon.simulator.validity.ProblemType
import no.nav.pensjon.simulator.ytelse.YtelseService
import org.springframework.stereotype.Service
import java.time.format.DateTimeParseException

/**
 * Simuler alderspensjon og privat AFP.
 * Dette er en "hybrid" tjeneste, siden to pensjonstyper er involvert.
 */
@Service
class AlderspensjonOgPrivatAfpService(
    private val simulatorCore: SimulatorCore,
    private val ytelseService: YtelseService,
    private val resultPreparer: AlderspensjonOgPrivatAfpResultPreparer,
    private val time: Time
) {
    fun simuler(spec: SimuleringSpec): AlderspensjonOgPrivatAfpResult =
        try {
            validate(spec, time.today())

            resultPreparer.result(
                simulatorOutput = simulatorCore.simuler(initialSpec = spec),
                pid = spec.pid!!,
                harLoependePrivatAfp = ytelseService.getLoependeYtelser(spec).privatAfpVirkningFom != null
            )
            //TODO PEN222BeregningstjenesteFeiletException, PEN223BrukerHarIkkeLopendeAlderspensjonException, PEN226BrukerHarLopendeAPPaGammeltRegelverkException - Jira TPP-44
            //TODO Kopier ThrowableExceptionMapper fra PEN - Jira TPP-45
        } catch (e: BadRequestException) {
            problem(e, type = ProblemType.ANNEN_KLIENTFEIL)
        } catch (e: BadSpecException) {
            problem(e)
        } catch (e: DateTimeParseException) {
            problem(e, type = ProblemType.ANNEN_KLIENTFEIL)
        } catch (e: EgressException) {
            problem(e, type = ProblemType.SERVERFEIL)
        } catch (e: FeilISimuleringsgrunnlagetException) {
            problem(e, type = ProblemType.ANNEN_KLIENTFEIL)
        } catch (e: ImplementationUnrecoverableException) {
            problem(e, type = ProblemType.SERVERFEIL)
        } catch (e: InvalidArgumentException) {
            problem(e, type = ProblemType.ANNEN_KLIENTFEIL)
        } catch (e: InvalidEnumValueException) {
            problem(e, type = ProblemType.ANNEN_KLIENTFEIL)
        } catch (e: KanIkkeBeregnesException) {
            problem(e, type = ProblemType.ANNEN_KLIENTFEIL)
        } catch (e: KonsistensenIGrunnlagetErFeilException) {
            problem(e, type = ProblemType.ANNEN_KLIENTFEIL)
        } catch (e: PersonForGammelException) {
            problem(e, type = ProblemType.PERSON_FOR_HOEY_ALDER)
        } catch (e: PersonForUngException) {
            problem(e, type = ProblemType.ANNEN_KLIENTFEIL)
        } catch (e: RegelmotorValideringException) {
            problem(e, type = ProblemType.ANNEN_KLIENTFEIL)
        } catch (e: UtilstrekkeligOpptjeningException) {
            problem(e, type = ProblemType.UTILSTREKKELIG_OPPTJENING)
        } catch (e: UtilstrekkeligTrygdetidException) {
            problem(e, type = ProblemType.UTILSTREKKELIG_TRYGDETID)
        }

    private companion object {
        private fun problem(e: BadSpecException) =
            problem(e, type = e.problemType)

        fun problem(e: RuntimeException, type: ProblemType) =
            AlderspensjonOgPrivatAfpResult(
                suksess = false,
                alderspensjonsperiodeListe = emptyList(),
                privatAfpPeriodeListe = emptyList(),
                harNaavaerendeUttak = false,
                harTidligereUttak = false,
                harLoependePrivatAfp = false,
                problem = Problem(type, beskrivelse = e.message ?: "Ukjent feil - ${e.javaClass.simpleName}")
            )
    }
}

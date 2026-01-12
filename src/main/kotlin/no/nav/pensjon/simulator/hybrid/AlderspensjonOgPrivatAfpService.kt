package no.nav.pensjon.simulator.hybrid

import no.nav.pensjon.simulator.core.SimulatorCore
import no.nav.pensjon.simulator.core.exception.*
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.tech.validation.InvalidEnumValueException
import no.nav.pensjon.simulator.tech.web.BadRequestException
import no.nav.pensjon.simulator.tech.web.EgressException
import org.springframework.stereotype.Service
import java.time.format.DateTimeParseException

/**
 * Simuler alderspensjon og privat AFP.
 * Dette er en "hybrid" tjeneste, siden to pensjonstyper er involvert.
 */
@Service
class AlderspensjonOgPrivatAfpService(
    private val simulatorCore: SimulatorCore,
    private val resultPreparer: AlderspensjonOgPrivatAfpResultPreparer
) {
    fun simuler(spec: SimuleringSpec): AlderspensjonOgPrivatAfpResult =
        try {
            resultPreparer.result(
                simulatorOutput = simulatorCore.simuler(initialSpec = spec),
                pid = spec.pid!!
            )
        } catch (e: BadRequestException) {
            problem(e, type = ProblemType.ANNEN_KLIENTFEIL)
        } catch (e: BadSpecException) {
            problem(e, type = ProblemType.ANNEN_KLIENTFEIL)
        } catch (e: DateTimeParseException) {
            problem(e, type = ProblemType.ANNEN_KLIENTFEIL)
        } catch (e: FeilISimuleringsgrunnlagetException) {
            problem(e, type = ProblemType.ANNEN_KLIENTFEIL)
        } catch (e: ImplementationUnrecoverableException) {
            problem(e, type = ProblemType.ANNEN_KLIENTFEIL)
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
        } catch (e: EgressException) {
            problem(e, type = ProblemType.SERVERFEIL)
        }

    private companion object {
        private fun problem(e: RuntimeException, type: ProblemType) =
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

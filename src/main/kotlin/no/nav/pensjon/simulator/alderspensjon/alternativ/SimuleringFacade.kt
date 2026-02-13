package no.nav.pensjon.simulator.alderspensjon.alternativ

import no.nav.pensjon.simulator.alderspensjon.convert.SimulatorOutputConverter.pensjon
import no.nav.pensjon.simulator.alderspensjon.spec.SimuleringSpecValidator.validate
import no.nav.pensjon.simulator.core.SimulatorCore
import no.nav.pensjon.simulator.core.exception.*
import no.nav.pensjon.simulator.core.krav.UttakGradKode
import no.nav.pensjon.simulator.core.result.SimulatorOutput
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.core.ufoere.UfoereService
import no.nav.pensjon.simulator.normalder.NormertPensjonsalderService
import no.nav.pensjon.simulator.tech.time.Time
import no.nav.pensjon.simulator.tech.validation.InvalidEnumValueException
import no.nav.pensjon.simulator.tech.web.BadRequestException
import no.nav.pensjon.simulator.tech.web.EgressException
import no.nav.pensjon.simulator.validity.BadSpecException
import no.nav.pensjon.simulator.validity.Problem
import no.nav.pensjon.simulator.validity.ProblemType
import org.springframework.stereotype.Service
import java.time.format.DateTimeParseException

// PEN: SimpleSimuleringService
// Vil brukes av Nav-klienter og tjenestepensjonsordninger
@Service
class SimuleringFacade(
    private val simulator: SimulatorCore,
    private val alternativSimulering: AlternativSimuleringService,
    private val ufoereAlternativSimulering: UfoereAlternativSimuleringService,
    private val normalderService: NormertPensjonsalderService,
    private val ufoereService: UfoereService,
    private val time: Time
) {
    fun simulerAlderspensjon(
        spec: SimuleringSpec,
        inkluderPensjonHvisUbetinget: Boolean
    ): SimulertPensjonEllerAlternativ {
        val gjelderUfoereMedAfp = spec.gjelderLivsvarigAfp() && hasUfoereperiode(spec)

        return try {
            validate(spec, time.today())
            val result: SimulatorOutput = simulator.simuler(spec)

            SimulertPensjonEllerAlternativ(
                pensjon =
                    if (spec.onlyVilkaarsproeving)
                        null // irrelevant when finding uttak only
                    else pensjon(
                        source = result,
                        today = time.today(),
                        inntektVedFase1Uttak = spec.inntektUnderGradertUttakBeloep
                    ),
                alternativ = null
            )
        } catch (e: UtilstrekkeligOpptjeningException) {
            // Brukers angitte parametre ga "avslått" resultat; prøv med alternative parametre:
            alternativ(spec, gjelderUfoereMedAfp, inkluderPensjonHvisUbetinget, e)
                ?: problem(e, type = ProblemType.UTILSTREKKELIG_OPPTJENING)
        } catch (e: UtilstrekkeligTrygdetidException) {
            alternativ(spec, gjelderUfoereMedAfp, inkluderPensjonHvisUbetinget, e)
                ?: problem(e, type = ProblemType.UTILSTREKKELIG_TRYGDETID)
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
        }
    }

    private fun alternativ(
        spec: SimuleringSpec,
        gjelderUfoereMedAfp: Boolean,
        inkluderPensjonHvisUbetinget: Boolean,
        exception: RuntimeException
    ): SimulertPensjonEllerAlternativ? =
        if (gjelderUfoereMedAfp)
            if (spec.isGradert() && spec.heltUttakDato!!.isBefore(normalderService.normalderDato(spec.foedselDato!!)))
                if (spec.uttakGrad == UttakGradKode.P_20) // ingen lavere uttaksgrad mulig
                    ufoereAlternativSimulering.simulerAlternativHvisUtkanttilfelletInnvilges(spec)
                else
                    ufoereAlternativSimulering.simulerMedNesteLavereUttaksgrad(spec)
            else
                ufoereAlternativSimulering.simulerMedFallendeUttaksgrad(spec, exception)
        else if (spec.onlyVilkaarsproeving.not() && isGradertAndReducible(spec))
            alternativSimulering.simulerMedNesteLavereUttaksgrad(spec, inkluderPensjonHvisUbetinget)
        else
            alternativSimulering.simulerAlternativHvisUtkanttilfelletInnvilges(spec, inkluderPensjonHvisUbetinget)


    private fun hasUfoereperiode(spec: SimuleringSpec): Boolean =
        spec.pid?.let { ufoereService.hasUfoereperiode(it, spec.foersteUttakDato!!) } == true

    private companion object {

        private fun isGradertAndReducible(spec: SimuleringSpec): Boolean =
            spec.isGradert() && isReducible(spec.uttakGrad)

        private fun isReducible(grad: UttakGradKode): Boolean =
            grad !== UttakGradKode.P_20 // 20 % is lowest gradert uttak
                    && grad !== UttakGradKode.P_100 // 100 % is not gradert uttak and hence not "adjustable" to a lower grad

        private fun problem(e: BadSpecException) =
            problem(e, type = e.problemType)

        private fun problem(e: RuntimeException, type: ProblemType) =
            SimulertPensjonEllerAlternativ(
                pensjon = null,
                alternativ = null,
                problem = Problem(type, beskrivelse = e.message ?: "Ukjent feil - ${e.javaClass.simpleName}")
            )
    }
}

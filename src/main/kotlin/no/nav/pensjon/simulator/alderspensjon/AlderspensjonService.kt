package no.nav.pensjon.simulator.alderspensjon

import no.nav.pensjon.simulator.alderspensjon.alternativ.AlternativSimuleringService
import no.nav.pensjon.simulator.alderspensjon.alternativ.SimulertPensjonEllerAlternativ
import no.nav.pensjon.simulator.alderspensjon.api.samhandler.SamhandlerAlderspensjonResultMapper
import no.nav.pensjon.simulator.alderspensjon.convert.SimulatorOutputConverter
import no.nav.pensjon.simulator.alderspensjon.spec.AlderspensjonSpec
import no.nav.pensjon.simulator.alderspensjon.spec.OffentligSimuleringstypeDeducer
import no.nav.pensjon.simulator.alderspensjon.spec.SimuleringSpecSanitiser.sanitise
import no.nav.pensjon.simulator.alderspensjon.spec.SimuleringSpecValidator.validate
import no.nav.pensjon.simulator.core.SimulatorCore
import no.nav.pensjon.simulator.core.exception.*
import no.nav.pensjon.simulator.core.krav.UttakGradKode
import no.nav.pensjon.simulator.core.result.SimulatorOutput
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.person.GeneralPersonService
import no.nav.pensjon.simulator.tech.time.Time
import no.nav.pensjon.simulator.tech.validation.InvalidEnumValueException
import no.nav.pensjon.simulator.tech.web.BadRequestException
import no.nav.pensjon.simulator.tech.web.EgressException
import no.nav.pensjon.simulator.validity.BadSpecException
import no.nav.pensjon.simulator.validity.IngressErrorHandler.extractSafeMessage
import no.nav.pensjon.simulator.validity.InternDataInkonsistensException
import no.nav.pensjon.simulator.validity.Problem
import no.nav.pensjon.simulator.validity.ProblemType
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.format.DateTimeParseException

@Component
class AlderspensjonService(
    private val simulator: SimulatorCore,
    private val alternativSimuleringService: AlternativSimuleringService,
    private val personService: GeneralPersonService,
    private val simuleringstypeDeducer: OffentligSimuleringstypeDeducer,
    private val outputConverter: SimulatorOutputConverter,
    private val time: Time
) {
    // Used for V4
    //TODO merge this with SimuleringFacade?
    fun simulerAlderspensjon(spec: AlderspensjonSpec): AlderspensjonResult {
        val simuleringSpec = sanitise(
            AlderspensjonSpecMapper.simuleringSpec(
                source = spec,
                foedselsdato = personService.foedselsdato(spec.pid),
                simuleringstypeDeducer
            )
        )

        return try {
            validate(simuleringSpec, time.today())
            val simuleringResultat = simulerMedMuligAlternativ(simuleringSpec)

            SamhandlerAlderspensjonResultMapper.mapPensjonEllerAlternativ(
                source = simuleringResultat,
                angittFoersteUttakFom = foersteUttakFom(simuleringSpec),
                angittAndreUttakFom = andreUttakFom(simuleringSpec),
                onlyIncludeEntriesForUttakDatoer = false
            )
        } catch (e: BadRequestException) {
            problem(e, type = ProblemType.ANNEN_KLIENTFEIL)
        } catch (e: BadSpecException) {
            problem(e, type = e.problemType)
        } catch (e: DateTimeParseException) {
            problem(e, type = ProblemType.ANNEN_KLIENTFEIL)
        } catch (e: EgressException) {
            problem(e, type = if (e.isClientError) ProblemType.ANNEN_SERVERFEIL else ProblemType.TREDJEPARTSFEIL)
        } catch (e: InternDataInkonsistensException) {
            problem(e, type = ProblemType.INTERN_DATA_INKONSISTENS)
        } catch (e: InvalidArgumentException) {
            problem(e, type = ProblemType.ANNEN_KLIENTFEIL)
        } catch (e: InvalidEnumValueException) {
            problem(e, type = ProblemType.ANNEN_KLIENTFEIL)
        } catch (e: PersonForGammelException) {
            problem(e, type = ProblemType.PERSON_FOR_HOEY_ALDER)
        } catch (e: PersonForUngException) {
            problem(e, type = ProblemType.ANNEN_KLIENTFEIL)
        } catch (e: RegelmotorValideringException) {
            problem(e, type = ProblemType.REGELBRUDD)
        } catch (e: UtilstrekkeligOpptjeningException) {
            problem(e, type = ProblemType.UTILSTREKKELIG_OPPTJENING)
        } catch (e: UtilstrekkeligTrygdetidException) {
            problem(e, type = ProblemType.UTILSTREKKELIG_TRYGDETID)
        }
    }

    private fun simulerMedMuligAlternativ(spec: SimuleringSpec): SimulertPensjonEllerAlternativ {
        try {
            val result: SimulatorOutput = simulator.simuler(spec)

            return SimulertPensjonEllerAlternativ(
                pensjon = outputConverter.pensjon( // SimulatorOutput -> SimulertPensjon
                    source = result,
                    today = time.today(),
                    inntektVedTidsbegrensetOffentligAfpUttak = spec.inntektUnderGradertUttakBeloep
                ),
                alternativ = null
            )
        } catch (e: UtilstrekkeligOpptjeningException) {
            // Brukers angitte parametre ga "avslått" resultat; prøv med alternative parametre:
            return if (isReducible(spec.uttakGrad))
                alternativSimuleringService.simulerMedNesteLavereUttaksgrad(
                    spec, inkluderPensjonHvisUbetinget = true
                )
            else
                alternativSimuleringService.simulerAlternativHvisUtkanttilfelletInnvilges(
                    spec, inkluderPensjonHvisUbetinget = true
                ) ?: throw e
        } catch (e: UtilstrekkeligTrygdetidException) {
            // Brukers angitte parametre ga "avslått" resultat; prøv med alternative parametre:
            return if (isReducible(spec.uttakGrad))
                alternativSimuleringService.simulerMedNesteLavereUttaksgrad(
                    spec, inkluderPensjonHvisUbetinget = true
                )
            else
                alternativSimuleringService.simulerAlternativHvisUtkanttilfelletInnvilges(
                    spec, inkluderPensjonHvisUbetinget = true
                ) ?: throw e
        }
    }

    private companion object {

        private fun foersteUttakFom(spec: SimuleringSpec): LocalDate =
            spec.foersteUttakDato ?: spec.heltUttakDato!!

        private fun andreUttakFom(spec: SimuleringSpec): LocalDate? =
            spec.foersteUttakDato?.let { spec.heltUttakDato }

        private fun isReducible(grad: UttakGradKode): Boolean =
            grad != UttakGradKode.P_20 // 20 % is lowest gradert uttak
                    && grad != UttakGradKode.P_100 // 100 % is not gradert uttak and hence not "adjustable" to a lower grad

        private fun problem(e: RuntimeException, type: ProblemType) =
            AlderspensjonResult(
                simuleringSuksess = false,
                aarsakListeIkkeSuksess = emptyList(),
                alderspensjon = emptyList(),
                forslagVedForLavOpptjening = null,
                harUttak = false,
                problem = Problem(type, beskrivelse = extractSafeMessage(e))
                    .also { it.log(e) { "samhandler AP OBS" } }
            )
    }
}
package no.nav.pensjon.simulator.alderspensjon.api.tpo.direct

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.servlet.http.HttpServletRequest
import mu.KotlinLogging
import no.nav.pensjon.simulator.afp.offentlig.pre2025.Pre2025OffentligAfpAvslaattException
import no.nav.pensjon.simulator.alderspensjon.AlderspensjonService
import no.nav.pensjon.simulator.alderspensjon.api.samhandler.acl.v3.AlderspensjonResultMapperV3
import no.nav.pensjon.simulator.alderspensjon.api.samhandler.acl.v3.AlderspensjonResultV3
import no.nav.pensjon.simulator.alderspensjon.api.samhandler.acl.v3.AlderspensjonSpecMapperV3
import no.nav.pensjon.simulator.alderspensjon.api.samhandler.acl.v3.AlderspensjonSpecV3
import no.nav.pensjon.simulator.alderspensjon.api.tpo.direct.acl.v4.*
import no.nav.pensjon.simulator.alderspensjon.api.tpo.direct.acl.v4.AlderspensjonResultMapperV4.resultV4
import no.nav.pensjon.simulator.alderspensjon.spec.AlderspensjonSpec
import no.nav.pensjon.simulator.common.api.ControllerBase
import no.nav.pensjon.simulator.core.SimulatorCore
import no.nav.pensjon.simulator.core.exception.*
import no.nav.pensjon.simulator.core.result.SimulatorOutput
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.generelt.organisasjon.OrganisasjonsnummerProvider
import no.nav.pensjon.simulator.tech.sporing.web.SporingInterceptor
import no.nav.pensjon.simulator.tech.trace.TraceAid
import no.nav.pensjon.simulator.tech.validation.InvalidEnumValueException
import no.nav.pensjon.simulator.tech.web.BadRequestException
import no.nav.pensjon.simulator.tech.web.EgressException
import no.nav.pensjon.simulator.tjenestepensjon.TilknytningService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.format.DateTimeParseException

/**
 * REST-controller for simulering av alderspensjon.
 * Tjenestene er ment å brukes av tjenestepensjonsordninger (TPO).
 * TPO gjør kall til pensjonssimulator "direkte", dvs. ikke via PEN (men via API-gateway).
 */
@RestController
@RequestMapping("api")
@SecurityRequirement(name = "BearerAuthentication")
class TpoAlderspensjonController(
    private val service: AlderspensjonService,
    private val simulatorCore: SimulatorCore,
    private val specMapper: AlderspensjonSpecMapperV3,
    private val resultMapper: AlderspensjonResultMapperV3,
    private val traceAid: TraceAid,
    organisasjonsnummerProvider: OrganisasjonsnummerProvider,
    tilknytningService: TilknytningService
) : ControllerBase(traceAid, organisasjonsnummerProvider, tilknytningService) {
    private val log = KotlinLogging.logger {}

    @PostMapping("v3/simuler-alderspensjon")
    @Operation(
        summary = "Simuler alderspensjon for tjenestepensjonsordning (V3)",
        description = "Lager en prognose for utbetaling av alderspensjon (versjon 3 av tjenesten).",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Simulering av alderspensjon utført."
            ),
            ApiResponse(
                responseCode = "400",
                description = "Simulering kunne ikke utføres pga. uakseptabel input. Det kan være: " +
                        " (1) helt uttak ikke etter gradert uttak," +
                        " (2) inntekt ikke 1. i måneden," +
                        " (3) inntekter har lik startdato, " +
                        " (4) negativ inntekt."
            )
        ]
    )
    fun simulerAlderspensjonV3(@RequestBody specV3: AlderspensjonSpecV3): AlderspensjonResultV3 {
        traceAid.begin()
        log.debug { "$FUNCTION_ID_V3 request: $specV3" }
        countCall(FUNCTION_ID_V3)

        return try {
            val spec: SimuleringSpec = specMapper.fromDtoV3(specV3)
            val result: SimulatorOutput = simulatorCore.simuler(spec)

            resultMapper.map(
                simuleringResult = result,
                pid = spec.pid!!,
                foersteUttakFom = spec.foersteUttakDato,
                heltUttakFom = spec.heltUttakDato
            ).also {
                log.debug { "$FUNCTION_ID_V3 response: $it" }
            }
        } catch (e: BadRequestException) {
            log.warn(e) { "$FUNCTION_ID_V3 bad request - $specV3" }
            throw e // delegate handling to ExceptionHandler to avoid returning ResponseEntity<Any>
        } catch (e: BadSpecException) {
            log.warn { "$FUNCTION_ID_V3 bad spec - ${extractMessageRecursively(e)} - $specV3" } // not log.warn(e)
            throw e
        } catch (e: DateTimeParseException) {
            log.warn { "$FUNCTION_ID_V3 feil datoformat (forventet yyyy-mm-dd) - ${extractMessageRecursively(e)} - request: $specV3" }
            throw e
        } catch (e: FeilISimuleringsgrunnlagetException) {
            log.warn(e) { "$FUNCTION_ID_V3 feil i simuleringsgrunnlaget - request - $specV3" }
            throw e
        } catch (e: ImplementationUnrecoverableException) {
            log.error(e) { "$FUNCTION_ID_V3 unrecoverable error - request - $specV3" }
            throw e
        } catch (e: InvalidArgumentException) {
            log.warn(e) { "$FUNCTION_ID_V3 invalid argument - request - $specV3" }
            throw e
        } catch (e: InvalidEnumValueException) {
            log.warn(e) { "$FUNCTION_ID_V3 invalid enum value - request - $specV3" }
            throw e
        } catch (e: KanIkkeBeregnesException) {
            log.warn(e) { "$FUNCTION_ID_V3 kan ikke beregnes - request - $specV3" }
            throw e
        } catch (e: KonsistensenIGrunnlagetErFeilException) {
            log.warn(e) { "$FUNCTION_ID_V3 inkonsistent grunnlag - request - $specV3" }
            throw e
        } catch (e: PersonForGammelException) {
            log.warn(e) { "$FUNCTION_ID_V3 person for gammel - request - $specV3" }
            throw e
        } catch (e: PersonForUngException) {
            log.warn(e) { "$FUNCTION_ID_V3 person for ung - request - $specV3" }
            throw e
        } catch (e: Pre2025OffentligAfpAvslaattException) {
            log.warn(e) { "$FUNCTION_ID_V3 pre-2025 offentlig AFP avslått - request - $specV3" }
            throw e
        } catch (e: RegelmotorValideringException) {
            log.warn(e) { "$FUNCTION_ID_V3 regelmotorvalideringsfeil - request - $specV3" }
            throw e
        } catch (e: UtilstrekkeligOpptjeningException) {
            log.warn(e) { "$FUNCTION_ID_V3 utilstrekkelig opptjening - request - $specV3" }
            throw e
        } catch (e: UtilstrekkeligTrygdetidException) {
            log.warn(e) { "$FUNCTION_ID_V3 utilstrekkelig trygdetid - request - $specV3" }
            throw e
            /* TODO ref. PEN DefaultSimulerePensjonProvider.simulerFleksibelAp
        } catch (e: BrukerHarIkkeLopendeAlderspensjonException) {
            handleExceptionV3(e)
        } catch (e: BrukerHarLopendeAPPaGammeltRegelverkException) {
            handleExceptionV3(e)
            */
            /* TODO ref. PEN ThrowableExceptionMapper.handleException
        } catch (e: IllegalArgumentException) {
            handleExceptionV3(e)
        } catch (e: PidValidationException) {
            handleExceptionV3(e)
        } catch (e: PersonIkkeFunnetLokaltException) {
            handleExceptionV3(e)
            */
        } catch (e: EgressException) {
            handle(e)!!
        } finally {
            traceAid.end()
        }
    }

    @PostMapping("v4/simuler-alderspensjon")
    @Operation(
        summary = "Simuler alderspensjon",
        description = "Lager en prognose for utbetaling av alderspensjon.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Simulering av alderspensjon utført."
            ),
            ApiResponse(
                responseCode = "400",
                description = "Simulering kunne ikke utføres pga. uakseptabel input. Det kan være: " +
                        " (1) helt uttak ikke etter gradert uttak," +
                        " (2) inntekt ikke 1. i måneden," +
                        " (3) inntekter har lik startdato, " +
                        " (4) negativ inntekt."
            )
        ]
    )
    fun simulerAlderspensjon(
        @RequestBody specV4: AlderspensjonSpecV4,
        request: HttpServletRequest
    ): AlderspensjonResultV4 {
        traceAid.begin(request)
        countCall(FUNCTION_ID_V4)

        return try {
            val spec: AlderspensjonSpec = AlderspensjonSpecMapperV4.fromDto(specV4)
            request.setAttribute(SporingInterceptor.PID_ATTRIBUTE_NAME, spec.pid)
            verifiserAtBrukerTilknyttetTpLeverandoer(spec.pid)
            resultV4(timed(service::simulerAlderspensjon, spec, FUNCTION_ID_V4))
        } catch (e: BadSpecException) {
            log.warn { "$FUNCTION_ID_V4 feil i spesifikasjonen - ${e.message} - request: $specV4" }
            feilInfoResultV4(e)
        } catch (e: DateTimeParseException) {
            log.warn { "$FUNCTION_ID_V4 feil datoformat (forventet yyyy-mm-dd) - ${e.message} - request: $specV4" }
            feilInfoResultV4(e)
        } catch (e: FeilISimuleringsgrunnlagetException) {
            log.warn { "$FUNCTION_ID_V4 feil i simuleringsgrunnlaget - ${e.message} - request: $specV4" }
            feilInfoResultV4(e)
        } catch (e: InvalidArgumentException) {
            log.warn { "$FUNCTION_ID_V4 invalid argument - ${e.message} - request: $specV4" }
            feilInfoResultV4(e)
        } catch (e: InvalidEnumValueException) {
            log.warn { "$FUNCTION_ID_V4 invalid enum value - ${e.message} - request: $specV4" }
            feilInfoResultV4(e)
        } catch (e: RegelmotorValideringException) {
            log.warn { "$FUNCTION_ID_V4 feil i regelmotorvalidering - ${e.message} - request: $specV4" }
            feilInfoResultV4(e)
        } catch (e: UtilstrekkeligOpptjeningException) {
            log.warn { "$FUNCTION_ID_V4 utilstrekkelig opptjening - ${e.message} - request: $specV4" }
            feilInfoResultV4(e, PensjonSimuleringStatusKodeV4.AVSLAG_FOR_LAV_OPPTJENING)
        } catch (e: UtilstrekkeligTrygdetidException) {
            log.warn { "$FUNCTION_ID_V4 utilstrekkelig trygdetid - ${e.message} - request: $specV4" }
            feilInfoResultV4(e, PensjonSimuleringStatusKodeV4.AVSLAG_FOR_KORT_TRYGDETID)
        } catch (e: EgressException) {
            handle(e)!!
        } catch (e: BadRequestException) {
            badRequest(e)!!
        } finally {
            traceAid.end()
        }
    }

    @ExceptionHandler(
        value = [
            BadRequestException::class,
            BadSpecException::class,
            DateTimeParseException::class,
            FeilISimuleringsgrunnlagetException::class,
            InvalidArgumentException::class,
            InvalidEnumValueException::class,
            KanIkkeBeregnesException::class,
            KonsistensenIGrunnlagetErFeilException::class,
            PersonForGammelException::class,
            PersonForUngException::class,
            Pre2025OffentligAfpAvslaattException::class,
            RegelmotorValideringException::class,
            UtilstrekkeligOpptjeningException::class,
            UtilstrekkeligTrygdetidException::class
        ]
    )
    private fun handleBadRequest(e: RuntimeException): ResponseEntity<TpoSimuleringErrorV3> =
        ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorV3(e))

    @ExceptionHandler(
        value = [
            ImplementationUnrecoverableException::class
        ]
    )
    fun handleInternalServerError(e: RuntimeException): ResponseEntity<TpoSimuleringErrorV3> =
        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorV3(e))

    override fun errorMessage() = ERROR_MESSAGE

    private companion object {
        private const val ERROR_MESSAGE = "feil ved simulering av alderspensjon"
        private const val FUNCTION_ID_V4 = "apv4"
        private const val FUNCTION_ID_V3 = "tpo-ap-v3"

        private fun feilInfoResultV4(
            e: Exception,
            status: PensjonSimuleringStatusKodeV4 = PensjonSimuleringStatusKodeV4.ANNET
        ) =
            AlderspensjonResultV4(
                simuleringSuksess = false,
                aarsakListeIkkeSuksess = listOf(
                    PensjonSimuleringStatusV4(
                        statusKode = status.externalValue,
                        statusBeskrivelse = e.message ?: e.javaClass.simpleName
                    )
                ),
                alderspensjon = emptyList(),
                forslagVedForLavOpptjening = null,
                harUttak = false
            )

        private fun errorV3(e: RuntimeException) =
            TpoSimuleringErrorV3(
                feil = e.javaClass.simpleName
            )
    }

    data class TpoSimuleringErrorV3(val feil: String)
}

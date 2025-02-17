package no.nav.pensjon.simulator.alderspensjon.api.tpo.viapen

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import mu.KotlinLogging
import no.nav.pensjon.simulator.alderspensjon.api.tpo.viapen.acl.v1.TpoSimuleringResultMapperV1
import no.nav.pensjon.simulator.alderspensjon.api.tpo.viapen.acl.v1.TpoSimuleringResultV1
import no.nav.pensjon.simulator.alderspensjon.api.tpo.viapen.acl.v1.TpoSimuleringSpecMapperV1
import no.nav.pensjon.simulator.alderspensjon.api.tpo.viapen.acl.v1.TpoSimuleringSpecV1
import no.nav.pensjon.simulator.alderspensjon.api.tpo.viapen.acl.v2.TpoSimuleringResultMapperV2
import no.nav.pensjon.simulator.alderspensjon.api.tpo.viapen.acl.v2.TpoSimuleringResultV2
import no.nav.pensjon.simulator.alderspensjon.api.tpo.viapen.acl.v2.TpoSimuleringSpecMapperV2
import no.nav.pensjon.simulator.alderspensjon.api.tpo.viapen.acl.v2.TpoSimuleringSpecV2
import no.nav.pensjon.simulator.alderspensjon.api.tpo.viapen.acl.v3.TpoSimuleringResultMapperV3
import no.nav.pensjon.simulator.alderspensjon.api.tpo.viapen.acl.v3.TpoSimuleringResultV3
import no.nav.pensjon.simulator.alderspensjon.api.tpo.viapen.acl.v3.TpoSimuleringSpecMapperV3
import no.nav.pensjon.simulator.alderspensjon.api.tpo.viapen.acl.v3.TpoSimuleringSpecV3
import no.nav.pensjon.simulator.common.api.ControllerBase
import no.nav.pensjon.simulator.core.SimulatorCore
import no.nav.pensjon.simulator.core.afp.offentlig.pre2025.Pre2025OffentligAfpAvslaattException
import no.nav.pensjon.simulator.core.exception.*
import no.nav.pensjon.simulator.core.result.SimulatorOutput
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.tech.trace.TraceAid
import no.nav.pensjon.simulator.tech.validation.InvalidEnumValueException
import no.nav.pensjon.simulator.tech.web.BadRequestException
import no.nav.pensjon.simulator.tech.web.EgressException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.format.DateTimeParseException

/**
 * REST-controller for simulering av alderspensjon.
 * Tjenestene er ment å brukes av tjenestepensjonsordninger (TPO).
 * TPO gjør ikke kall til pensjonssimulator direkte, men må gå via pensjon-pen.
 */
@RestController
@RequestMapping("api/tpo")
@SecurityRequirement(name = "BearerAuthentication")
class TpoViaPenAlderspensjonController(
    private val simulatorCore: SimulatorCore,
    private val traceAid: TraceAid
) : ControllerBase(traceAid) {
    private val log = KotlinLogging.logger {}

    @PostMapping("v1/simuler-alderspensjon")
    @Operation(
        summary = "Simuler alderspensjon for tjenestepensjonsordning (V1)",
        description = "Lager en prognose for utbetaling av alderspensjon (versjon 1 av tjenesten).",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "100",
                description = "Simulering av alderspensjon utført."
            ),
            ApiResponse(
                responseCode = "400",
                description = "Simulering kunne ikke utføres pga. uakseptabel input. Det kan være: " +
                        " (1) helt uttak ikke etter gradert uttak," +
                        " (1) inntekt ikke 1. i måneden," +
                        " (3) inntekter har lik startdato, " +
                        " (4) negativ inntekt."
            )
        ]
    )
    fun simulerAlderspensjonV1(@RequestBody specV1: TpoSimuleringSpecV1): TpoSimuleringResultV1 {
        traceAid.begin()
        countCall(FUNCTION_ID_V1)

        return try {
            val spec: SimuleringSpec = TpoSimuleringSpecMapperV1.fromDto(specV1)
            val result: SimulatorOutput = simulatorCore.simuler(spec)
            TpoSimuleringResultMapperV1.toDto(result)
        } catch (e: BadRequestException) {
            log.warn(e) { "$FUNCTION_ID_V1 bad request - $specV1" }
            throw e // delegate handling to ExceptionHandler to avoid returning ResponseEntity<Any>
        } catch (e: BadSpecException) {
            log.warn { "$FUNCTION_ID_V1 bad spec - $specV1" } // not log.warn(e)
            throw e // delegate handling to ExceptionHandler to avoid returning ResponseEntity<Any>
        } catch (e: DateTimeParseException) {
            log.warn { "$FUNCTION_ID_V1 feil datoformat (forventet yyyy-mm-dd) - ${e.message} - request: $specV1" }
            throw e
        } catch (e: FeilISimuleringsgrunnlagetException) {
            log.warn(e) { "$FUNCTION_ID_V1 feil i simuleringsgrunnlaget - request - $specV1" }
            throw e // delegate handling to ExceptionHandler to avoid returning ResponseEntity<Any>
        } catch (e: ImplementationUnrecoverableException) {
            log.error(e) { "$FUNCTION_ID_V1 unrecoverable error - request - $specV1" }
            throw e // delegate handling to ExceptionHandler to avoid returning ResponseEntity<Any>
        } catch (e: InvalidArgumentException) {
            log.warn(e) { "$FUNCTION_ID_V1 invalid argument - request - $specV1" }
            throw e // delegate handling to ExceptionHandler to avoid returning ResponseEntity<Any>
        } catch (e: InvalidEnumValueException) {
            log.warn(e) { "$FUNCTION_ID_V1 invalid enum value - request - $specV1" }
            throw e
        } catch (e: KanIkkeBeregnesException) {
            log.warn(e) { "$FUNCTION_ID_V1 kan ikke beregnes - request - $specV1" }
            throw e // delegate handling to ExceptionHandler to avoid returning ResponseEntity<Any>
        } catch (e: KonsistensenIGrunnlagetErFeilException) {
            log.warn(e) { "$FUNCTION_ID_V1 inkonsistent grunnlag - request - $specV1" }
            throw e // delegate handling to ExceptionHandler to avoid returning ResponseEntity<Any>
        } catch (e: PersonForGammelException) {
            log.warn(e) { "$FUNCTION_ID_V1 person for gammel - request - $specV1" }
            throw e // delegate handling to ExceptionHandler to avoid returning ResponseEntity<Any>
        } catch (e: PersonForUngException) {
            log.warn(e) { "$FUNCTION_ID_V1 person for ung - request - $specV1" }
            throw e // delegate handling to ExceptionHandler to avoid returning ResponseEntity<Any>
        } catch (e: Pre2025OffentligAfpAvslaattException) {
            log.warn(e) { "$FUNCTION_ID_V1 pre-2025 offentlig AFP avslått - request - $specV1" }
            throw e // delegate handling to ExceptionHandler to avoid returning ResponseEntity<Any>
        } catch (e: RegelmotorValideringException) {
            log.warn(e) { "$FUNCTION_ID_V1 regelmotorvalideringsfeil - request - $specV1" }
            throw e // delegate handling to ExceptionHandler to avoid returning ResponseEntity<Any>
        } catch (e: UtilstrekkeligOpptjeningException) {
            log.warn(e) { "$FUNCTION_ID_V1 utilstrekkelig opptjening - request - $specV1" }
            throw e // delegate handling to ExceptionHandler to avoid returning ResponseEntity<Any>
        } catch (e: UtilstrekkeligTrygdetidException) {
            log.warn(e) { "$FUNCTION_ID_V1 utilstrekkelig trygdetid - request - $specV1" }
            throw e // delegate handling to ExceptionHandler to avoid returning ResponseEntity<Any>
            /* TODO ref. PEN SimulerAlderspensjonController.simuler
        } catch (e: BrukerHarIkkeLopendeAlderspensjonException) {
            handleExceptionV1(e)
        } catch (e: BrukerHarLopendeAPPaGammeltRegelverkException) {
            handleExceptionV1(e)
            */
            /* TODO ref. PEN ThrowableExceptionMapper.handleException
        } catch (e: IllegalArgumentException) {
            handleExceptionV1(e)
        } catch (e: PidValidationException) {
            handleExceptionV1(e)
        } catch (e: PersonIkkeFunnetLokaltException) {
            handleExceptionV1(e)
            */
        } catch (e: EgressException) {
            handle(e)!!
        } finally {
            traceAid.end()
        }
    }

    @PostMapping("v2/simuler-alderspensjon")
    @Operation(
        summary = "Simuler alderspensjon for tjenestepensjonsordning (V2)",
        description = "Lager en prognose for utbetaling av alderspensjon (versjon 2 av tjenesten).",
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
    fun simulerAlderspensjonV2(@RequestBody specV2: TpoSimuleringSpecV2): TpoSimuleringResultV2 {
        traceAid.begin()
        log.debug { "$FUNCTION_ID_V2 request: $specV2" }
        countCall(FUNCTION_ID_V2)

        return try {
            val spec: SimuleringSpec = TpoSimuleringSpecMapperV2.fromDto(specV2)
            val result: SimulatorOutput = simulatorCore.simuler(spec)
            TpoSimuleringResultMapperV2.toDto(result)
        } catch (e: BadRequestException) {
            log.warn(e) { "$FUNCTION_ID_V2 bad request - $specV2" }
            throw e // delegate handling to ExceptionHandler to avoid returning ResponseEntity<Any>
        } catch (e: BadSpecException) {
            log.warn { "$FUNCTION_ID_V2 bad spec - $specV2" } // not log.warn(e)
            throw e // delegate handling to ExceptionHandler to avoid returning ResponseEntity<Any>
        } catch (e: DateTimeParseException) {
            log.warn { "$FUNCTION_ID_V2 feil datoformat (forventet yyyy-mm-dd) - ${e.message} - request: $specV2" }
            throw e
        } catch (e: FeilISimuleringsgrunnlagetException) {
            log.warn(e) { "$FUNCTION_ID_V2 feil i simuleringsgrunnlaget - request - $specV2" }
            throw e // delegate handling to ExceptionHandler to avoid returning ResponseEntity<Any>
        } catch (e: ImplementationUnrecoverableException) {
            log.error(e) { "$FUNCTION_ID_V2 unrecoverable error - request - $specV2" }
            throw e // delegate handling to ExceptionHandler to avoid returning ResponseEntity<Any>
        } catch (e: InvalidArgumentException) {
            log.warn(e) { "$FUNCTION_ID_V2 invalid argument - request - $specV2" }
            throw e // delegate handling to ExceptionHandler to avoid returning ResponseEntity<Any>
        } catch (e: InvalidEnumValueException) {
            log.warn(e) { "$FUNCTION_ID_V2 invalid enum value - request - $specV2" }
            throw e
        } catch (e: KanIkkeBeregnesException) {
            log.warn(e) { "$FUNCTION_ID_V2 kan ikke beregnes - request - $specV2" }
            throw e // delegate handling to ExceptionHandler to avoid returning ResponseEntity<Any>
        } catch (e: KonsistensenIGrunnlagetErFeilException) {
            log.warn(e) { "$FUNCTION_ID_V2 inkonsistent grunnlag - request - $specV2" }
            throw e // delegate handling to ExceptionHandler to avoid returning ResponseEntity<Any>
        } catch (e: PersonForGammelException) {
            log.warn(e) { "$FUNCTION_ID_V2 person for gammel - request - $specV2" }
            throw e // delegate handling to ExceptionHandler to avoid returning ResponseEntity<Any>
        } catch (e: PersonForUngException) {
            log.warn(e) { "$FUNCTION_ID_V2 person for ung - request - $specV2" }
            throw e // delegate handling to ExceptionHandler to avoid returning ResponseEntity<Any>
        } catch (e: Pre2025OffentligAfpAvslaattException) {
            log.warn(e) { "$FUNCTION_ID_V2 pre-2025 offentlig AFP avslått - request - $specV2" }
            throw e // delegate handling to ExceptionHandler to avoid returning ResponseEntity<Any>
        } catch (e: RegelmotorValideringException) {
            log.warn(e) { "$FUNCTION_ID_V2 regelmotorvalideringsfeil - request - $specV2" }
            throw e // delegate handling to ExceptionHandler to avoid returning ResponseEntity<Any>
        } catch (e: UtilstrekkeligOpptjeningException) {
            log.warn(e) { "$FUNCTION_ID_V2 utilstrekkelig opptjening - request - $specV2" }
            throw e // delegate handling to ExceptionHandler to avoid returning ResponseEntity<Any>
        } catch (e: UtilstrekkeligTrygdetidException) {
            log.warn(e) { "$FUNCTION_ID_V2 utilstrekkelig trygdetid - request - $specV2" }
            throw e // delegate handling to ExceptionHandler to avoid returning ResponseEntity<Any>
        } catch (e: EgressException) {
            handle(e)!!
        } finally {
            traceAid.end()
        }
    }

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
    fun simulerAlderspensjonV3(@RequestBody specV3: TpoSimuleringSpecV3): TpoSimuleringResultV3 {
        traceAid.begin()
        log.debug { "$FUNCTION_ID_V3 request: $specV3" }
        countCall(FUNCTION_ID_V3)

        return try {
            val spec: SimuleringSpec = TpoSimuleringSpecMapperV3.fromDto(specV3)
            val result: SimulatorOutput = simulatorCore.simuler(spec)
            TpoSimuleringResultMapperV3.toDto(result)
        } catch (e: BadRequestException) {
            log.warn(e) { "$FUNCTION_ID_V3 bad request - $specV3" }
            throw e // delegate handling to ExceptionHandler to avoid returning ResponseEntity<Any>
        } catch (e: BadSpecException) {
            log.warn { "$FUNCTION_ID_V3 bad spec - $specV3" } // not log.warn(e)
            throw e // delegate handling to ExceptionHandler to avoid returning ResponseEntity<Any>
        } catch (e: DateTimeParseException) {
            log.warn { "$FUNCTION_ID_V3 feil datoformat (forventet yyyy-mm-dd) - ${e.message} - request: $specV3" }
            throw e
        } catch (e: FeilISimuleringsgrunnlagetException) {
            log.warn(e) { "$FUNCTION_ID_V3 feil i simuleringsgrunnlaget - request - $specV3" }
            throw e // delegate handling to ExceptionHandler to avoid returning ResponseEntity<Any>
        } catch (e: ImplementationUnrecoverableException) {
            log.error(e) { "$FUNCTION_ID_V3 unrecoverable error - request - $specV3" }
            throw e // delegate handling to ExceptionHandler to avoid returning ResponseEntity<Any>
        } catch (e: InvalidArgumentException) {
            log.warn(e) { "$FUNCTION_ID_V3 invalid argument - request - $specV3" }
            throw e // delegate handling to ExceptionHandler to avoid returning ResponseEntity<Any>
        } catch (e: InvalidEnumValueException) {
            log.warn(e) { "$FUNCTION_ID_V3 invalid enum value - request - $specV3" }
            throw e
        } catch (e: KanIkkeBeregnesException) {
            log.warn(e) { "$FUNCTION_ID_V3 kan ikke beregnes - request - $specV3" }
            throw e // delegate handling to ExceptionHandler to avoid returning ResponseEntity<Any>
        } catch (e: KonsistensenIGrunnlagetErFeilException) {
            log.warn(e) { "$FUNCTION_ID_V3 inkonsistent grunnlag - request - $specV3" }
            throw e // delegate handling to ExceptionHandler to avoid returning ResponseEntity<Any>
        } catch (e: PersonForGammelException) {
            log.warn(e) { "$FUNCTION_ID_V3 person for gammel - request - $specV3" }
            throw e // delegate handling to ExceptionHandler to avoid returning ResponseEntity<Any>
        } catch (e: PersonForUngException) {
            log.warn(e) { "$FUNCTION_ID_V3 person for ung - request - $specV3" }
            throw e // delegate handling to ExceptionHandler to avoid returning ResponseEntity<Any>
        } catch (e: Pre2025OffentligAfpAvslaattException) {
            log.warn(e) { "$FUNCTION_ID_V3 pre-2025 offentlig AFP avslått - request - $specV3" }
            throw e // delegate handling to ExceptionHandler to avoid returning ResponseEntity<Any>
        } catch (e: RegelmotorValideringException) {
            log.warn(e) { "$FUNCTION_ID_V3 regelmotorvalideringsfeil - request - $specV3" }
            throw e // delegate handling to ExceptionHandler to avoid returning ResponseEntity<Any>
        } catch (e: UtilstrekkeligOpptjeningException) {
            log.warn(e) { "$FUNCTION_ID_V3 utilstrekkelig opptjening - request - $specV3" }
            throw e // delegate handling to ExceptionHandler to avoid returning ResponseEntity<Any>
        } catch (e: UtilstrekkeligTrygdetidException) {
            log.warn(e) { "$FUNCTION_ID_V3 utilstrekkelig trygdetid - request - $specV3" }
            throw e // delegate handling to ExceptionHandler to avoid returning ResponseEntity<Any>
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
    private fun handleBadRequest(e: RuntimeException): ResponseEntity<TpoSimuleringErrorDto> =
        ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorDto(e))

    @ExceptionHandler(
        value = [
            ImplementationUnrecoverableException::class
        ]
    )
    fun handleInternalServerError(e: RuntimeException): ResponseEntity<TpoSimuleringErrorDto> =
        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorDto(e))

    override fun errorMessage() = ERROR_MESSAGE

    /**
     * Ref. PEN JsonErrorEntityBuilder.createErrorEntity
     */
    data class TpoSimuleringErrorDto(val feil: String)

    private companion object {
        private const val ERROR_MESSAGE = "feil ved simulering av alderspensjon for TPO via PEN"
        private const val FUNCTION_ID_V1 = "tpo-ap-v1"
        private const val FUNCTION_ID_V2 = "tpo-ap-v2"
        private const val FUNCTION_ID_V3 = "tpo-ap-v3"

        private fun errorDto(e: RuntimeException) =
            TpoSimuleringErrorDto(
                feil = e.javaClass.simpleName
            )
    }
}

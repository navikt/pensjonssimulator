package no.nav.pensjon.simulator.alderspensjon.api.nav.viapen

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import mu.KotlinLogging
import no.nav.pensjon.simulator.afp.offentlig.pre2025.Pre2025OffentligAfpAvslaattException
import no.nav.pensjon.simulator.alderspensjon.api.nav.viapen.acl.v2.result.ApForTpResultMapperV2.toApForTpResultV2
import no.nav.pensjon.simulator.alderspensjon.api.nav.viapen.acl.v2.result.ApForTpResultV2
import no.nav.pensjon.simulator.alderspensjon.api.nav.viapen.acl.v2.result.NavSimuleringResultMapperV2.toSimuleringResultV2
import no.nav.pensjon.simulator.alderspensjon.api.nav.viapen.acl.v2.result.NavSimuleringSpecAndResultV2
import no.nav.pensjon.simulator.alderspensjon.api.nav.viapen.acl.v2.spec.NavSimuleringSpecMapperV2
import no.nav.pensjon.simulator.alderspensjon.api.nav.viapen.acl.v2.spec.NavSimuleringSpecV2
import no.nav.pensjon.simulator.common.api.ControllerBase
import no.nav.pensjon.simulator.core.SimulatorCore
import no.nav.pensjon.simulator.core.exception.BadSpecException
import no.nav.pensjon.simulator.core.exception.FeilISimuleringsgrunnlagetException
import no.nav.pensjon.simulator.core.exception.ImplementationUnrecoverableException
import no.nav.pensjon.simulator.core.exception.InvalidArgumentException
import no.nav.pensjon.simulator.core.exception.KanIkkeBeregnesException
import no.nav.pensjon.simulator.core.exception.KonsistensenIGrunnlagetErFeilException
import no.nav.pensjon.simulator.core.exception.PersonForGammelException
import no.nav.pensjon.simulator.core.exception.PersonForUngException
import no.nav.pensjon.simulator.core.exception.RegelmotorValideringException
import no.nav.pensjon.simulator.core.exception.UtilstrekkeligOpptjeningException
import no.nav.pensjon.simulator.core.exception.UtilstrekkeligTrygdetidException
import no.nav.pensjon.simulator.core.result.SimulatorOutput
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.core.util.toNorwegianDate
import no.nav.pensjon.simulator.statistikk.StatistikkService
import no.nav.pensjon.simulator.tech.trace.TraceAid
import no.nav.pensjon.simulator.tech.validation.InvalidEnumValueException
import no.nav.pensjon.simulator.tech.web.BadRequestException
import no.nav.pensjon.simulator.tech.web.EgressException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.format.DateTimeParseException

@RestController
@RequestMapping("api/nav")
@SecurityRequirement(name = "BearerAuthentication")
class NavViaPenAlderspensjonController(
    private val simulator: SimulatorCore,
    private val specMapper: NavSimuleringSpecMapperV2,
    private val traceAid: TraceAid,
    statistikk: StatistikkService
) : ControllerBase(traceAid = traceAid, statistikk = statistikk) {
    private val log = KotlinLogging.logger {}

    /**
     * Supports PEN-service /selvbetjening/pensjonskalkulator/simuleralder/v2
     */
    @PostMapping("v2/simuler-alderspensjon")
    @Operation(
        summary = "Simuler alderspensjon V2 for Nav-klient",
        description = "Lager en prognose for utbetaling av alderspensjon, basert på Nav-lagret info og input fra bruker.",
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
    fun simulerAlderspensjon(@RequestBody specV2: NavSimuleringSpecV2): NavSimuleringSpecAndResultV2 {
        traceAid.begin()
        log.debug { "$AP_FUNCTION_ID request: $specV2" }
        countCall(AP_FUNCTION_ID)

        return try {
            val spec: SimuleringSpec = specMapper.fromSimuleringSpecV2(
                source = specV2,
                isHentPensjonsbeholdninger = false,
                isOutputSimulertBeregningsinformasjonForAllKnekkpunkter = false
            )

            registrerHendelse(simuleringstype = spec.type)
            val output: SimulatorOutput = simulator.simuler(spec)

            NavSimuleringSpecAndResultV2(
                simulering = specV2.apply {
                    epsPensjon = output.epsHarPensjon
                    heltUttakDato = output.heltUttakDato?.toNorwegianDate()
                },
                simuleringsresultat = toSimuleringResultV2(output)
            ).also {
                log.debug { "$AP_FUNCTION_ID response: $it" }
            }
        } catch (e: BadRequestException) {
            log.warn(e) { "$AP_FUNCTION_ID bad request - $specV2" }
            throw e // delegate handling to ExceptionHandler to avoid returning ResponseEntity<Any>
        } catch (e: BadSpecException) {
            log.warn { "$AP_FUNCTION_ID bad spec - ${extractMessageRecursively(e)} - $specV2" } // not log.warn(e)
            throw e
        } catch (e: DateTimeParseException) {
            log.warn { "$AP_FUNCTION_ID feil datoformat (forventet yyyy-mm-dd) - ${extractMessageRecursively(e)} - request: $specV2" }
            throw e
        } catch (e: FeilISimuleringsgrunnlagetException) {
            log.warn(e) { "$AP_FUNCTION_ID feil i simuleringsgrunnlaget - request - $specV2" }
            throw e
        } catch (e: ImplementationUnrecoverableException) {
            log.error(e) { "$AP_FUNCTION_ID unrecoverable error - request - $specV2" }
            throw e
        } catch (e: InvalidArgumentException) {
            log.warn(e) { "$AP_FUNCTION_ID invalid argument - request - $specV2" }
            throw e
        } catch (e: InvalidEnumValueException) {
            log.warn(e) { "$AP_FUNCTION_ID invalid enum value - request - $specV2" }
            throw e
        } catch (e: KanIkkeBeregnesException) {
            log.warn(e) { "$AP_FUNCTION_ID kan ikke beregnes - request - $specV2" }
            throw e
        } catch (e: KonsistensenIGrunnlagetErFeilException) {
            log.warn(e) { "$AP_FUNCTION_ID inkonsistent grunnlag - request - $specV2" }
            throw e
        } catch (e: PersonForGammelException) {
            log.warn(e) { "$AP_FUNCTION_ID person for gammel - request - $specV2" }
            throw e
        } catch (e: PersonForUngException) {
            log.warn(e) { "$AP_FUNCTION_ID person for ung - request - $specV2" }
            throw e
        } catch (e: Pre2025OffentligAfpAvslaattException) {
            log.warn(e) { "$AP_FUNCTION_ID pre-2025 offentlig AFP avslått - request - $specV2" }
            throw e
        } catch (e: RegelmotorValideringException) {
            log.warn(e) { "$AP_FUNCTION_ID regelmotorvalideringsfeil - request - $specV2" }
            throw e
        } catch (e: UtilstrekkeligOpptjeningException) {
            log.info(e) { "$AP_FUNCTION_ID utilstrekkelig opptjening - request - $specV2" }
            throw e
        } catch (e: UtilstrekkeligTrygdetidException) {
            log.info(e) { "$AP_FUNCTION_ID utilstrekkelig trygdetid - request - $specV2" }
            throw e
        } catch (e: EgressException) {
            handle(e)!!
        } finally {
            traceAid.end()
        }
    }

    /**
     * Supports PEN-service selvbetjening/simuler/tjenestepensjon.
     * Used to obtain 'simulert alderspensjon for simulering av tjenestepnsjon'.
     */
    @PostMapping("v2/simuler-tjenestepensjon")
    @Operation(
        summary = "Simuler alderspensjon som grunnlag for tjenestepensjon",
        description = "Lager en prognose for utbetaling av alderspensjon, basert på Nav-lagret info og input fra bruker.",
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
    fun simulerAlderspensjonForTjenestepensjon(@RequestBody specV2: NavSimuleringSpecV2): ApForTpResultV2 {
        traceAid.begin()
        log.debug { "$TP_FUNCTION_ID request: $specV2" }
        countCall(TP_FUNCTION_ID)

        return try {
            val spec: SimuleringSpec = specMapper.fromSimuleringSpecV2(
                source = specV2,
                isHentPensjonsbeholdninger = true,
                isOutputSimulertBeregningsinformasjonForAllKnekkpunkter = true
            )

            registrerHendelse(simuleringstype = spec.type)
            val output: SimulatorOutput = simulator.simuler(spec)

            toApForTpResultV2(output).also {
                log.debug { "$TP_FUNCTION_ID response: $it" }
            }
        } catch (e: BadRequestException) {
            log.warn(e) { "$TP_FUNCTION_ID bad request - $specV2" }
            throw e // delegate handling to ExceptionHandler to avoid returning ResponseEntity<Any>
        } catch (e: BadSpecException) {
            log.warn { "$TP_FUNCTION_ID bad spec - ${extractMessageRecursively(e)} - $specV2" } // not log.warn(e)
            throw e
        } catch (e: DateTimeParseException) {
            log.warn { "$TP_FUNCTION_ID feil datoformat (forventet yyyy-mm-dd) - ${extractMessageRecursively(e)} - request: $specV2" }
            throw e
        } catch (e: FeilISimuleringsgrunnlagetException) {
            log.warn(e) { "$TP_FUNCTION_ID feil i simuleringsgrunnlaget - request - $specV2" }
            throw e
        } catch (e: ImplementationUnrecoverableException) {
            log.error(e) { "$TP_FUNCTION_ID unrecoverable error - request - $specV2" }
            throw e
        } catch (e: InvalidArgumentException) {
            log.warn(e) { "$TP_FUNCTION_ID invalid argument - request - $specV2" }
            throw e
        } catch (e: InvalidEnumValueException) {
            log.warn(e) { "$TP_FUNCTION_ID invalid enum value - request - $specV2" }
            throw e
        } catch (e: KanIkkeBeregnesException) {
            log.warn(e) { "$TP_FUNCTION_ID kan ikke beregnes - request - $specV2" }
            throw e
        } catch (e: KonsistensenIGrunnlagetErFeilException) {
            log.warn(e) { "$TP_FUNCTION_ID inkonsistent grunnlag - request - $specV2" }
            throw e
        } catch (e: PersonForGammelException) {
            log.warn(e) { "$TP_FUNCTION_ID person for gammel - request - $specV2" }
            throw e
        } catch (e: PersonForUngException) {
            log.warn(e) { "$TP_FUNCTION_ID person for ung - request - $specV2" }
            throw e
        } catch (e: Pre2025OffentligAfpAvslaattException) {
            log.warn(e) { "$TP_FUNCTION_ID pre-2025 offentlig AFP avslått - request - $specV2" }
            throw e
        } catch (e: RegelmotorValideringException) {
            log.warn(e) { "$TP_FUNCTION_ID regelmotorvalideringsfeil - request - $specV2" }
            throw e
        } catch (e: UtilstrekkeligOpptjeningException) {
            log.info(e) { "$TP_FUNCTION_ID utilstrekkelig opptjening - request - $specV2" }
            throw e
        } catch (e: UtilstrekkeligTrygdetidException) {
            log.info(e) { "$TP_FUNCTION_ID utilstrekkelig trygdetid - request - $specV2" }
            throw e
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
    private fun handleBadRequest(e: RuntimeException): ResponseEntity<NavSimuleringErrorDto> =
        ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorDto(e))

    @ExceptionHandler(
        value = [
            ImplementationUnrecoverableException::class
        ]
    )
    fun handleInternalServerError(e: RuntimeException): ResponseEntity<NavSimuleringErrorDto> =
        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorDto(e))

    override fun errorMessage() = ERROR_MESSAGE

    /**
     * Ref. PEN JsonErrorEntityBuilder.createErrorEntity
     */
    data class NavSimuleringErrorDto(val feil: String)

    private companion object {
        private const val ERROR_MESSAGE = "feil ved simulering av alderspensjon V2 for Nav-klient"
        private const val AP_FUNCTION_ID = "nav-ap-v2"
        private const val TP_FUNCTION_ID = "nav-ap-tp"

        private fun errorDto(e: RuntimeException) =
            NavSimuleringErrorDto(
                feil = e.javaClass.simpleName
            )
    }
}

package no.nav.pensjon.simulator.alderspensjon.api.samhandler

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.servlet.http.HttpServletRequest
import mu.KotlinLogging
import no.nav.pensjon.simulator.afp.offentlig.pre2025.Pre2025OffentligAfpAvslaattException
import no.nav.pensjon.simulator.alderspensjon.api.samhandler.acl.v3.AlderspensjonResultMapperV3
import no.nav.pensjon.simulator.alderspensjon.api.samhandler.acl.v3.AlderspensjonResultV3
import no.nav.pensjon.simulator.alderspensjon.api.samhandler.acl.v3.AlderspensjonSpecMapperV3
import no.nav.pensjon.simulator.alderspensjon.api.samhandler.acl.v3.AlderspensjonSpecV3
import no.nav.pensjon.simulator.alderspensjon.api.samhandler.acl.v3.AlderspensjonSpecValidatorV3
import no.nav.pensjon.simulator.alderspensjon.api.samhandler.acl.v3.BadRequestReasonV3
import no.nav.pensjon.simulator.alderspensjon.api.samhandler.acl.v3.InternalServerErrorReasonV3
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
import org.springframework.web.bind.annotation.*
import java.time.format.DateTimeParseException

/**
 * REST-controller for simulering av alderspensjon for personer født før 1963.
 * Tjenestene er ment å brukes av samhandlere (tjenestepensjonsordninger).
 * Samhandlere gjør kall til pensjonssimulator via API-gateway.
 */
@RestController
@RequestMapping("api")
@SecurityRequirement(name = "BearerAuthentication")
class SamhandlerAlderspensjonControllerV3(
    private val simulatorCore: SimulatorCore,
    private val validator: AlderspensjonSpecValidatorV3,
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
        description = "Lager en prognose for utbetaling av alderspensjon (versjon 3 av tjenesten)." +
                "\\\n\\\n*Scope*: **nav:pensjon/v3/alderspensjon**"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Simulering av alderspensjon utført."
            ),
            ApiResponse(
                responseCode = "400",
                description = "Simulering kunne ikke utføres pga. uakseptable inndata. Det kan være:" +
                        "\\\n(1) helt uttak ikke etter gradert uttak," +
                        "\\\n(2) inntekt ikke 1. i måneden," +
                        "\\\n(3) inntekter har lik startdato, " +
                        "\\\n(4) negativ inntekt."
            )
        ]
    )
    fun simulerAlderspensjonV3(
        @RequestBody specV3: AlderspensjonSpecV3,
        request: HttpServletRequest
    ): AlderspensjonResultV3 {
        traceAid.begin()
        log.info { "direct V3 call" }
        log.debug { "$FUNCTION_ID_V3 request: $specV3" }
        countCall(FUNCTION_ID_V3)

        return try {
            validator.validate(specV3)
            val spec: SimuleringSpec = specMapper.fromDtoV3(specV3)
            request.setAttribute(SporingInterceptor.PID_ATTRIBUTE_NAME, spec.pid)
            verifiserAtBrukerTilknyttetTpLeverandoer(spec.pid!!)
            val result: SimulatorOutput = simulatorCore.simuler(spec)

            resultMapper.map(
                simuleringResult = result,
                pid = spec.pid,
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
        } catch (e: EgressException) {
            log.error(e) { "$FUNCTION_ID_V3 egress error - request - $specV3" }
            throw e
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
            //TODO PEN223BrukerHarIkkeLopendeAlderspensjonException, PEN226BrukerHarLopendeAPPaGammeltRegelverkException - Jira TPP-44
            //TODO Kopier ThrowableExceptionMapper fra PEN - Jira TPP-45
        ]
    )
    private fun handleBadRequest(e: RuntimeException): ResponseEntity<BadRequestReasonV3> =
        ResponseEntity.status(HttpStatus.BAD_REQUEST).body(badRequestReason(e))

    @ExceptionHandler(
        value = [
            ImplementationUnrecoverableException::class,
            EgressException::class,
            Exception::class
        ]
    )
    private fun handleInternalServerError(e: RuntimeException): ResponseEntity<InternalServerErrorReasonV3> =
        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(internalServerErrorReason(e))

    override fun errorMessage() = ERROR_MESSAGE

    private companion object {
        private const val ERROR_MESSAGE = "feil ved simulering av alderspensjon V3"
        private const val FUNCTION_ID_V3 = "sam-ap-v3"

        /**
         * PEN: JsonErrorEntityBuilder.createErrorEntity
         */
        private fun badRequestReason(e: RuntimeException) =
            BadRequestReasonV3(
                feil = e.message ?: "No errormessage",
                kode = e.javaClass.simpleName
            )

        private fun internalServerErrorReason(e: RuntimeException) =
            InternalServerErrorReasonV3(feil = e.message ?: "No errormessage")
    }
}

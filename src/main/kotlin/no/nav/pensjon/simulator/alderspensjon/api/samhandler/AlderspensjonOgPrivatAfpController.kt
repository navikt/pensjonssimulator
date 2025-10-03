package no.nav.pensjon.simulator.alderspensjon.api.samhandler

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import mu.KotlinLogging
import no.nav.pensjon.simulator.afp.offentlig.pre2025.Pre2025OffentligAfpAvslaattException
import no.nav.pensjon.simulator.alderspensjon.api.samhandler.acl.v3.AlderspensjonOgPrivatAfpResultMapperV3
import no.nav.pensjon.simulator.alderspensjon.api.samhandler.acl.v3.AlderspensjonOgPrivatAfpResultV3
import no.nav.pensjon.simulator.alderspensjon.api.samhandler.acl.v3.AlderspensjonSpecMapperV3
import no.nav.pensjon.simulator.alderspensjon.api.samhandler.acl.v3.AlderspensjonSpecV3
import no.nav.pensjon.simulator.common.api.ControllerBase
import no.nav.pensjon.simulator.core.SimulatorCore
import no.nav.pensjon.simulator.core.exception.*
import no.nav.pensjon.simulator.core.result.SimulatorOutput
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.generelt.organisasjon.OrganisasjonsnummerProvider
import no.nav.pensjon.simulator.tech.trace.TraceAid
import no.nav.pensjon.simulator.tech.validation.InvalidEnumValueException
import no.nav.pensjon.simulator.tech.web.BadRequestException
import no.nav.pensjon.simulator.tech.web.EgressException
import no.nav.pensjon.simulator.tjenestepensjon.TilknytningService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.format.DateTimeParseException

/**
 * REST-controller for simulering av alderspensjon og privat AFP.
 * Tjenesten er ment å brukes av samhandlere.
 */
@RestController
@RequestMapping("api")
@SecurityRequirement(name = "BearerAuthentication")
class AlderspensjonOgPrivatAfpController(
    private val simulatorCore: SimulatorCore,
    private val specMapper: AlderspensjonSpecMapperV3,
    private val resultMapper: AlderspensjonOgPrivatAfpResultMapperV3,
    private val traceAid: TraceAid,
    organisasjonsnummerProvider: OrganisasjonsnummerProvider,
    tilknytningService: TilknytningService
) : ControllerBase(traceAid, organisasjonsnummerProvider, tilknytningService) {
    private val log = KotlinLogging.logger {}

    @PostMapping("v3/simuler-alderspensjon-privat-afp")
    @Operation(
        summary = "Simuler alderspensjon og privat AFP for samhandler (V3)",
        description = "Lager en prognose for utbetaling av alderspensjon og privat AFP (versjon 3 av tjenesten).",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Simulering av alderspensjon og privat AFP utført."
            ),
            ApiResponse(
                responseCode = "400",
                description = "Simulering kunne ikke utføres pga. uakseptable inndata. Det kan være: " +
                        " (1) helt uttak ikke etter gradert uttak," +
                        " (2) inntekt ikke 1. i måneden," +
                        " (3) inntekter har lik startdato, " +
                        " (4) negativ inntekt."
            )
        ]
    )
    fun simulerAlderspensjonOgPrivatAfpV3(@RequestBody specV3: AlderspensjonSpecV3): AlderspensjonOgPrivatAfpResultV3 {
        traceAid.begin()
        log.debug { "$FUNCTION_ID_V3 request: $specV3" }
        countCall(FUNCTION_ID_V3)

        return try {
            val spec: SimuleringSpec = specMapper.fromDtoV3(specV3)
            val result: SimulatorOutput = simulatorCore.simuler(spec)

            resultMapper.map(simuleringResult = result, pid = spec.pid!!).also {
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
            handle(e)!!
        } finally {
            traceAid.end()
        }
    }

    override fun errorMessage() = ERROR_MESSAGE

    private companion object {
        private const val ERROR_MESSAGE = "feil ved simulering av alderspensjon"
        private const val FUNCTION_ID_V3 = "sam-ap-pafp"
    }
}

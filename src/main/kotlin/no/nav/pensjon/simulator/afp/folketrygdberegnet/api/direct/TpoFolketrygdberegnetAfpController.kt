package no.nav.pensjon.simulator.afp.folketrygdberegnet.api.direct

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.servlet.http.HttpServletRequest
import mu.KotlinLogging
import no.nav.pensjon.simulator.afp.folketrygdberegnet.api.direct.acl.v0.result.TpoFolketrygdberegnetAfpResultMapperV0.toResultV0
import no.nav.pensjon.simulator.afp.folketrygdberegnet.api.direct.acl.v0.result.TpoFolketrygdberegnetAfpResultV0
import no.nav.pensjon.simulator.afp.folketrygdberegnet.api.direct.acl.v0.spec.TpoFolketrygdberegnetAfpSpecMapperV0
import no.nav.pensjon.simulator.afp.folketrygdberegnet.api.direct.acl.v0.spec.TpoFolketrygdberegnetAfpSpecV0
import no.nav.pensjon.simulator.afp.offentlig.pre2025.Pre2025OffentligAfpAvslaattException
import no.nav.pensjon.simulator.alderspensjon.api.tpo.viapen.TpoViaPenAlderspensjonController.TpoSimuleringErrorDto
import no.nav.pensjon.simulator.common.api.ControllerBase
import no.nav.pensjon.simulator.core.SimulatorCore
import no.nav.pensjon.simulator.core.exception.*
import no.nav.pensjon.simulator.core.result.SimulatorOutput
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.statistikk.StatistikkService
import no.nav.pensjon.simulator.tech.trace.TraceAid
import no.nav.pensjon.simulator.tech.validation.InvalidEnumValueException
import no.nav.pensjon.simulator.tech.web.BadRequestException
import no.nav.pensjon.simulator.tech.web.EgressException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.format.DateTimeParseException

@RestController
@RequestMapping("api")
@SecurityRequirement(name = "BearerAuthentication")
class TpoFolketrygdberegnetAfpController(
    private val simulator: SimulatorCore,
    private val specMapper: TpoFolketrygdberegnetAfpSpecMapperV0,
    private val traceAid: TraceAid,
    statistikk: StatistikkService
) : ControllerBase(traceAid = traceAid, statistikk = statistikk) {
    private val log = KotlinLogging.logger {}

    /**
     * The purpose of this service is to replace the use of 'Din pensjon' where employees of
     * tjenestepensjonsordninger can log in as themselves and use 'bytt bruker' to simulate
     * on behalf of persons affiliated with the tjenestepensjonsordning.
     */
    @PostMapping("v0/simuler-folketrygdberegnet-afp")
    @Operation(
        summary = "Simuler folketrygdberegnet AFP V0",
        description = "Lager en prognose for folketrygdberegnet AFP, basert på Nav-lagret info og input fra tjenestepensjonsordning.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Simulering av folketrygdberegnet AFP utført."
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
    fun simulerFolketrygdberegnetAfp(
        @RequestBody specV0: TpoFolketrygdberegnetAfpSpecV0,
        request: HttpServletRequest
    ): TpoFolketrygdberegnetAfpResultV0? {
        traceAid.begin(request)
        countCall(FUNCTION_ID)

        return try {
            val spec: SimuleringSpec = specMapper.fromSimuleringSpecV0(specV0)
            registrerHendelse(simuleringstype = spec.type)
            val output: SimulatorOutput = simulator.simuler(spec)
            toResultV0(output)
        } catch (e: BadRequestException) {
            log.warn(e) { "$FUNCTION_ID bad request - ${e.message} - $specV0" }
            throw e // delegate handling to ExceptionHandler to avoid returning ResponseEntity<Any>
        } catch (e: BadSpecException) {
            log.warn { "$FUNCTION_ID bad spec - ${e.message} - request: $specV0" } // not log.warn(e)
            throw e
        } catch (e: DateTimeParseException) {
            log.warn { "$FUNCTION_ID feil datoformat (forventet yyyy-mm-dd) - ${e.message} - request: $specV0" }
            throw e
        } catch (e: FeilISimuleringsgrunnlagetException) {
            log.warn(e) { "$FUNCTION_ID feil i simuleringsgrunnlaget - request - $specV0" }
            throw e
        } catch (e: ImplementationUnrecoverableException) {
            log.error(e) { "$FUNCTION_ID unrecoverable error - request - $specV0" }
            throw e
        } catch (e: InvalidArgumentException) {
            log.warn(e) { "$FUNCTION_ID invalid argument - request - $specV0" }
            throw e
        } catch (e: InvalidEnumValueException) {
            log.warn(e) { "$FUNCTION_ID invalid enum value - request - $specV0" }
            throw e
        } catch (e: KanIkkeBeregnesException) {
            log.warn(e) { "$FUNCTION_ID kan ikke beregnes - request - $specV0" }
            throw e
        } catch (e: KonsistensenIGrunnlagetErFeilException) {
            log.warn(e) { "$FUNCTION_ID inkonsistent grunnlag - request - $specV0" }
            throw e
        } catch (e: PersonForGammelException) {
            log.warn(e) { "$FUNCTION_ID person for gammel - request - $specV0" }
            throw e
        } catch (e: PersonForUngException) {
            log.warn(e) { "$FUNCTION_ID person for ung - request - $specV0" }
            throw e
        } catch (e: Pre2025OffentligAfpAvslaattException) {
            log.warn(e) { "$FUNCTION_ID pre-2025 offentlig AFP avslått - request - $specV0" }
            throw e
        } catch (e: RegelmotorValideringException) {
            log.warn(e) { "$FUNCTION_ID regelmotorvalideringsfeil - request - $specV0" }
            throw e
        } catch (e: UtilstrekkeligOpptjeningException) {
            log.warn(e) { "$FUNCTION_ID utilstrekkelig opptjening - request - $specV0" }
            throw e
        } catch (e: UtilstrekkeligTrygdetidException) {
            log.warn(e) { "$FUNCTION_ID utilstrekkelig trygdetid - request - $specV0" }
            throw e
        } catch (e: EgressException) {
            handle(e)!!
        } finally {
            traceAid.end()
        }
    }

    override fun errorMessage() = ERROR_MESSAGE

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

    private companion object {
        private const val ERROR_MESSAGE = "feil ved simulering av folketrygdberegnet AFP V0"
        private const val FUNCTION_ID = "ftb-afp-tpo-v0"

        private fun errorDto(e: RuntimeException) =
            TpoSimuleringErrorDto(
                feil = e.javaClass.simpleName
            )
    }
}

package no.nav.pensjon.simulator.afp.folketrygdberegnet.api.viapen

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import mu.KotlinLogging
import no.nav.pensjon.simulator.afp.folketrygdberegnet.api.viapen.acl.v1.result.FolketrygdberegnetAfpResultMapperV1.toResultV1
import no.nav.pensjon.simulator.afp.folketrygdberegnet.api.viapen.acl.v1.result.FolketrygdberegnetAfpResultV1
import no.nav.pensjon.simulator.afp.folketrygdberegnet.api.viapen.acl.v1.spec.FolketrygdberegnetAfpSpecMapperV1
import no.nav.pensjon.simulator.afp.folketrygdberegnet.api.viapen.acl.v1.spec.FolketrygdberegnetAfpSpecV1
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
@RequestMapping("api/nav")
@SecurityRequirement(name = "BearerAuthentication")
class FolketrygdberegnetAfpController(
    private val simulator: SimulatorCore,
    private val specMapper: FolketrygdberegnetAfpSpecMapperV1,
    private val traceAid: TraceAid,
    statistikk: StatistikkService
) : ControllerBase(traceAid = traceAid, statistikk = statistikk) {
    private val log = KotlinLogging.logger {}

    /**
     * Supports PEN services selvbetjening/pensjonskalkulator/lagrefpp and foretaFolketrygdberegnetAfp.
     */
    @PostMapping("v1/simuler-folketrygdberegnet-afp")
    @Operation(
        summary = "Simuler folketrygdberegnet AFP",
        description = "Lager en prognose for folketrygdberegnet avtalefestet pensjon (AFP) i offentlig sektor." +
                "\\\n\\\n*Scope*:" +
                "\\\n– Uten delegering: **nav:pensjonssimulator:simulering**" +
                "\\\n– Med delegering: **nav:pensjon/simulering.read**"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Simulering av folketrygdberegnet AFP utført."
            ),
            ApiResponse(
                responseCode = "400",
                description = "Simulering kunne ikke utføres pga. uakseptabel input."
            )
        ]
    )
    fun simulerFramtidigePensjonspoeng(@RequestBody specV1: FolketrygdberegnetAfpSpecV1): FolketrygdberegnetAfpResultV1? {
        traceAid.begin()
        countCall(FUNCTION_ID)

        return try {
            val spec: SimuleringSpec = specMapper.fromSimuleringSpecV1(specV1)
            registrerHendelse(simuleringstype = spec.type)
            val output: SimulatorOutput = simulator.simuler(spec)
            toResultV1(output)
        } catch (e: BadRequestException) {
            log.warn(e) { "$FUNCTION_ID bad request - $specV1" }
            throw e // delegate handling to ExceptionHandler to avoid returning ResponseEntity<Any>
        } catch (e: BadSpecException) {
            log.warn { "$FUNCTION_ID bad spec - ${e.message} - request: $specV1" } // not log.warn(e)
            throw e
        } catch (e: DateTimeParseException) {
            log.warn { "$FUNCTION_ID feil datoformat (forventet yyyy-mm-dd) - ${e.message} - request: $specV1" }
            throw e
        } catch (e: FeilISimuleringsgrunnlagetException) {
            log.warn(e) { "$FUNCTION_ID feil i simuleringsgrunnlaget - request - $specV1" }
            throw e
        } catch (e: ImplementationUnrecoverableException) {
            log.error(e) { "$FUNCTION_ID unrecoverable error - request - $specV1" }
            throw e
        } catch (e: InvalidArgumentException) {
            log.warn(e) { "$FUNCTION_ID invalid argument - request - $specV1" }
            throw e
        } catch (e: InvalidEnumValueException) {
            log.warn(e) { "$FUNCTION_ID invalid enum value - request - $specV1" }
            throw e
        } catch (e: KanIkkeBeregnesException) {
            log.warn(e) { "$FUNCTION_ID kan ikke beregnes - request - $specV1" }
            throw e
        } catch (e: KonsistensenIGrunnlagetErFeilException) {
            log.warn(e) { "$FUNCTION_ID inkonsistent grunnlag - request - $specV1" }
            throw e
        } catch (e: PersonForGammelException) {
            log.warn(e) { "$FUNCTION_ID person for gammel - request - $specV1" }
            throw e
        } catch (e: PersonForUngException) {
            log.warn(e) { "$FUNCTION_ID person for ung - request - $specV1" }
            throw e
        } catch (e: Pre2025OffentligAfpAvslaattException) {
            log.warn(e) { "$FUNCTION_ID pre-2025 offentlig AFP avslått - request - $specV1" }
            throw e
        } catch (e: RegelmotorValideringException) {
            log.warn(e) { "$FUNCTION_ID regelmotorvalideringsfeil - request - $specV1" }
            throw e
        } catch (e: UtilstrekkeligOpptjeningException) {
            log.warn(e) { "$FUNCTION_ID utilstrekkelig opptjening - request - $specV1" }
            throw e
        } catch (e: UtilstrekkeligTrygdetidException) {
            log.warn(e) { "$FUNCTION_ID utilstrekkelig trygdetid - request - $specV1" }
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
        private const val ERROR_MESSAGE = "feil ved simulering av folketrygdberegnet AFP V1"
        private const val FUNCTION_ID = "ftb-afp-v1"

        private fun errorDto(e: RuntimeException) =
            TpoSimuleringErrorDto(
                feil = e.javaClass.simpleName
            )
    }
}

package no.nav.pensjon.simulator.beholdning.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.servlet.http.HttpServletRequest
import mu.KotlinLogging
import no.nav.pensjon.simulator.afp.offentlig.pre2025.Pre2025OffentligAfpAvslaattException
import no.nav.pensjon.simulator.beholdning.FolketrygdBeholdningService
import no.nav.pensjon.simulator.beholdning.FolketrygdBeholdningSpec
import no.nav.pensjon.simulator.beholdning.api.acl.FolketrygdBeholdningResultMapperV1.resultV1
import no.nav.pensjon.simulator.beholdning.api.acl.FolketrygdBeholdningResultV1
import no.nav.pensjon.simulator.beholdning.api.acl.FolketrygdBeholdningSpecMapperV1.fromSpecV1
import no.nav.pensjon.simulator.beholdning.api.acl.FolketrygdBeholdningSpecV1
import no.nav.pensjon.simulator.common.api.ControllerBase
import no.nav.pensjon.simulator.core.exception.*
import no.nav.pensjon.simulator.generelt.organisasjon.OrganisasjonsnummerProvider
import no.nav.pensjon.simulator.tech.sporing.web.SporingInterceptor
import no.nav.pensjon.simulator.tech.trace.TraceAid
import no.nav.pensjon.simulator.tech.web.BadRequestException
import no.nav.pensjon.simulator.tech.web.EgressException
import no.nav.pensjon.simulator.tjenestepensjon.TilknytningService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("api")
@SecurityRequirement(name = "BearerAuthentication")
class BeholdningController(
    private val service: FolketrygdBeholdningService,
    private val traceAid: TraceAid,
    organisasjonsnummerProvider: OrganisasjonsnummerProvider,
    tilknytningService: TilknytningService,
) : ControllerBase(traceAid, organisasjonsnummerProvider, tilknytningService) {
    private val log = KotlinLogging.logger {}

    @PostMapping("v1/simuler-folketrygdbeholdning")
    @Operation(
        summary = "Simuler folketrygdbeholdning",
        description = "Lager en prognose for pensjonsbeholdning i folketrygden." +
                "\\\n\\\n*Scope*:" +
                "\\\n– Uten delegering: **nav:pensjonssimulator:simulering**" +
                "\\\n– Med delegering: **nav:pensjon/simulering.read**"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Simulering av folketrygdbeholdning utført"
            )
        ]
    )
    fun simulerFolketrygdbeholdning(
        @RequestBody specV1: FolketrygdBeholdningSpecV1,
        request: HttpServletRequest
    ): FolketrygdBeholdningResultV1 {
        traceAid.begin()
        countCall(FUNCTION_ID)

        return try {
            val spec: FolketrygdBeholdningSpec = fromSpecV1(specV1)
            request.setAttribute(SporingInterceptor.PID_ATTRIBUTE_NAME, spec.pid)
            verifiserAtBrukerTilknyttetTpLeverandoer(spec.pid)
            resultV1(timed(service::simulerFolketrygdBeholdning, spec, FUNCTION_ID))
        } catch (e: BadRequestException) {
            log.warn(e) { "$FUNCTION_ID bad request - ${e.message} - $specV1" }
            throw e // delegate handling to ExceptionHandler to avoid returning ResponseEntity<Any>
        } catch (e: BadSpecException) {
            log.warn { "$FUNCTION_ID bad spec - ${e.message} - $specV1" } // not log.warn(e)
            throw e // delegate handling to ExceptionHandler to avoid returning ResponseEntity<Any>
        } catch (e: FeilISimuleringsgrunnlagetException) {
            log.warn(e) { "$FUNCTION_ID feil i simuleringsgrunnlaget - request - $specV1" }
            throw e // delegate handling to ExceptionHandler to avoid returning ResponseEntity<Any>
        } catch (e: ImplementationUnrecoverableException) {
            log.error(e) { "$FUNCTION_ID unrecoverable error - request - $specV1" }
            throw e // delegate handling to ExceptionHandler to avoid returning ResponseEntity<Any>
        } catch (e: InvalidArgumentException) {
            log.warn(e) { "$FUNCTION_ID invalid argument - request - $specV1" }
            throw e // delegate handling to ExceptionHandler to avoid returning ResponseEntity<Any>
        } catch (e: KanIkkeBeregnesException) {
            log.warn(e) { "$FUNCTION_ID kan ikke beregnes - request - $specV1" }
            throw e // delegate handling to ExceptionHandler to avoid returning ResponseEntity<Any>
        } catch (e: KonsistensenIGrunnlagetErFeilException) {
            log.warn(e) { "$FUNCTION_ID inkonsistent grunnlag - request - $specV1" }
            throw e // delegate handling to ExceptionHandler to avoid returning ResponseEntity<Any>
        } catch (e: PersonForGammelException) {
            log.warn(e) { "$FUNCTION_ID person for gammel - request - $specV1" }
            throw e // delegate handling to ExceptionHandler to avoid returning ResponseEntity<Any>
        } catch (e: PersonForUngException) {
            log.warn(e) { "$FUNCTION_ID person for ung - request - $specV1" }
            throw e // delegate handling to ExceptionHandler to avoid returning ResponseEntity<Any>
        } catch (e: Pre2025OffentligAfpAvslaattException) {
            log.warn(e) { "$FUNCTION_ID pre-2025 offentlig AFP avslått - request - $specV1" }
            throw e // delegate handling to ExceptionHandler to avoid returning ResponseEntity<Any>
        } catch (e: RegelmotorValideringException) {
            log.warn(e) { "$FUNCTION_ID regelmotorvalideringsfeil - request - $specV1" }
            throw e // delegate handling to ExceptionHandler to avoid returning ResponseEntity<Any>
        } catch (e: UtilstrekkeligOpptjeningException) {
            log.warn(e) { "$FUNCTION_ID utilstrekkelig opptjening - request - $specV1" }
            throw e // delegate handling to ExceptionHandler to avoid returning ResponseEntity<Any>
        } catch (e: UtilstrekkeligTrygdetidException) {
            log.warn(e) { "$FUNCTION_ID utilstrekkelig trygdetid - request - $specV1" }
            throw e // delegate handling to ExceptionHandler to avoid returning ResponseEntity<Any>
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
            FeilISimuleringsgrunnlagetException::class,
            InvalidArgumentException::class,
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
    fun handleBadRequest(e: RuntimeException): ResponseEntity<FolketrygdBeholdningErrorV1> =
        ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorV1(e))

    @ExceptionHandler(
        value = [
            ImplementationUnrecoverableException::class
        ]
    )
    fun handleInternalServerError(e: RuntimeException): ResponseEntity<FolketrygdBeholdningErrorV1> =
        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorV1(e))

    fun errorV1(e: RuntimeException) =
        FolketrygdBeholdningErrorV1(
            beskrivelse = extractMessageRecursively(e)
        )

    override fun errorMessage() = ERROR_MESSAGE

    data class FolketrygdBeholdningErrorV1(
        val beskrivelse: String
    )

    private companion object {
        private const val ERROR_MESSAGE = "feil ved simulering folketrygdbeholdning"
        private const val FUNCTION_ID = "ftb"
    }
}

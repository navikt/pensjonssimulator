package no.nav.pensjon.simulator.uttak.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.servlet.http.HttpServletRequest
import mu.KotlinLogging
import no.nav.pensjon.simulator.common.api.ControllerBase
import no.nav.pensjon.simulator.core.afp.offentlig.pre2025.Pre2025OffentligAfpAvslaattException
import no.nav.pensjon.simulator.core.exception.*
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.generelt.GenerelleDataHolder
import no.nav.pensjon.simulator.generelt.organisasjon.OrganisasjonsnummerProvider
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.tech.sporing.web.SporingInterceptor
import no.nav.pensjon.simulator.tech.trace.TraceAid
import no.nav.pensjon.simulator.tech.validation.InvalidEnumValueException
import no.nav.pensjon.simulator.tech.web.BadRequestException
import no.nav.pensjon.simulator.tech.web.EgressException
import no.nav.pensjon.simulator.tjenestepensjon.TilknytningService
import no.nav.pensjon.simulator.uttak.UttakService
import no.nav.pensjon.simulator.uttak.api.acl.TidligstMuligUttakResultV1
import no.nav.pensjon.simulator.uttak.api.acl.TidligstMuligUttakSpecV1
import no.nav.pensjon.simulator.uttak.api.acl.UttakResultMapperV1.resultV1
import no.nav.pensjon.simulator.uttak.api.acl.UttakSpecMapperV1.fromSpecV1
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDate

@RestController
@RequestMapping("api")
@SecurityRequirement(name = "BearerAuthentication")
class UttakController(
    private val service: UttakService,
    private val generelleDataHolder: GenerelleDataHolder,
    private val traceAid: TraceAid,
    organisasjonsnummerProvider: OrganisasjonsnummerProvider,
    tilknytningService: TilknytningService,
) : ControllerBase(traceAid, organisasjonsnummerProvider, tilknytningService) {
    private val log = KotlinLogging.logger {}

    @PostMapping("v1/tidligst-mulig-uttak")
    @Operation(
        summary = "Tidligst mulig uttak",
        description = "Finner den tidligst mulige dato for uttak av alderspensjon",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Utledning av uttaksdato utført"
            )
        ]
    )
    fun tidligstMuligUttak(
        @RequestBody specV1: TidligstMuligUttakSpecV1,
        request: HttpServletRequest
    ): TidligstMuligUttakResultV1 {
        traceAid.begin(request)
        log.debug { "$FUNCTION_ID request: $specV1" }
        countCall(FUNCTION_ID)

        return try {
            val pid = Pid(specV1.personId)

            if (pid.isValid)
                request.setAttribute(SporingInterceptor.PID_ATTRIBUTE_NAME, pid)
            else
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Ugyldig personId: '${specV1.personId}'")

            verifiserAtBrukerTilknyttetTpLeverandoer(pid)
            val foedselsdato: LocalDate = generelleDataHolder.getPerson(pid).foedselDato
            val spec: SimuleringSpec = fromSpecV1(specV1, foedselsdato)
            resultV1(timed(service::finnTidligstMuligUttak, spec, FUNCTION_ID))
        } catch (e: BadRequestException) {
            log.warn(e) { "$FUNCTION_ID bad request - $specV1" }
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
        } catch (e: InvalidEnumValueException) {
            log.warn(e) { "$FUNCTION_ID invalid enum value - request - $specV1" }
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
    fun handleBadRequest(e: RuntimeException): ResponseEntity<UttakErrorV1> =
        ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorV1(e))

    @ExceptionHandler(
        value = [
            ImplementationUnrecoverableException::class
        ]
    )
    fun handleInternalServerError(e: RuntimeException): ResponseEntity<UttakErrorV1> =
        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorV1(e))

    fun errorV1(e: RuntimeException) =
        UttakErrorV1(
            beskrivelse = extractMessageRecursively(e)
        )

    override fun errorMessage() = ERROR_MESSAGE

    data class UttakErrorV1(
        val beskrivelse: String
    )

    private companion object {
        private const val ERROR_MESSAGE = "feil ved utledning av tidligst mulig uttak"
        private const val FUNCTION_ID = "tmu"
    }
}

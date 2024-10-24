package no.nav.pensjon.simulator.uttak.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.servlet.http.HttpServletRequest
import mu.KotlinLogging
import no.nav.pensjon.simulator.common.api.ControllerBase
import no.nav.pensjon.simulator.generelt.organisasjon.OrganisasjonsnummerProvider
import no.nav.pensjon.simulator.tech.trace.TraceAid
import no.nav.pensjon.simulator.tech.validation.InvalidEnumValueException
import no.nav.pensjon.simulator.tech.web.BadRequestException
import no.nav.pensjon.simulator.tech.web.EgressException
import no.nav.pensjon.simulator.tjenestepensjon.TilknytningService
import no.nav.pensjon.simulator.uttak.TidligstMuligUttakSpec
import no.nav.pensjon.simulator.uttak.UttakService
import no.nav.pensjon.simulator.uttak.api.acl.TidligstMuligUttakResultV1
import no.nav.pensjon.simulator.uttak.api.acl.TidligstMuligUttakSpecV1
import no.nav.pensjon.simulator.uttak.api.acl.UttakResultMapperV1.resultV1
import no.nav.pensjon.simulator.uttak.api.acl.UttakSpecMapperV1.fromSpecV1
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("api")
@SecurityRequirement(name = "BearerAuthentication")
class UttakController(
    private val service: UttakService,
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
        traceAid.begin()
        log.debug { "$FUNCTION_ID request: $specV1" }
        countCall(FUNCTION_ID)

        return try {
            val spec: TidligstMuligUttakSpec = fromSpecV1(specV1)

            with(spec.pid) {
                if (this.isValid) {
                    request.setAttribute("pid", this)
                } else {
                    throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Ugyldig personId: '${specV1.personId}'")
                }
            }
            verifiserAtBrukerTilknyttetTpLeverandoer(spec.pid)

            resultV1(timed(service::finnTidligstMuligUttak, spec, FUNCTION_ID))
                .also { log.debug { "$FUNCTION_ID response: $it" } }
        } catch (e: EgressException) {
            handle(e)!!
        } catch (e: BadRequestException) {
            badRequest(e)!!
        } catch (e: InvalidEnumValueException) {
            badRequest(e)!!
        } finally {
            traceAid.end()
        }
    }

    override fun errorMessage() = ERROR_MESSAGE

    private companion object {
        private const val ERROR_MESSAGE = "feil ved utledning av tidligst mulig uttak"
        private const val FUNCTION_ID = "tmu"
    }
}

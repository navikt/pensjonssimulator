package no.nav.pensjon.simulator.alderspensjon.api.tpo.direct

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.servlet.http.HttpServletRequest
import mu.KotlinLogging
import no.nav.pensjon.simulator.alderspensjon.AlderspensjonService
import no.nav.pensjon.simulator.alderspensjon.api.tpo.direct.acl.v4.*
import no.nav.pensjon.simulator.alderspensjon.api.tpo.direct.acl.v4.AlderspensjonResultMapperV4.resultV4
import no.nav.pensjon.simulator.common.api.ControllerBase
import no.nav.pensjon.simulator.core.exception.FeilISimuleringsgrunnlagetException
import no.nav.pensjon.simulator.core.exception.RegelmotorValideringException
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.generelt.GenerelleDataHolder
import no.nav.pensjon.simulator.generelt.organisasjon.OrganisasjonsnummerProvider
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.tech.trace.TraceAid
import no.nav.pensjon.simulator.tech.validation.InvalidEnumValueException
import no.nav.pensjon.simulator.tech.web.BadRequestException
import no.nav.pensjon.simulator.tech.web.EgressException
import no.nav.pensjon.simulator.tjenestepensjon.TilknytningService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

/**
 * REST-controller for simulering av alderspensjon.
 * Tjenestene er ment å brukes av tjenestepensjonsordninger (TPO).
 * TPO gjør kall til pensjonssimulator "direkte" (via API-gateway).
 */
@RestController
@RequestMapping("api")
@SecurityRequirement(name = "BearerAuthentication")
class TpoAlderspensjonController(
    private val service: AlderspensjonService,
    private val generelleDataHolder: GenerelleDataHolder,
    private val traceAid: TraceAid,
    organisasjonsnummerProvider: OrganisasjonsnummerProvider,
    tilknytningService: TilknytningService
) : ControllerBase(traceAid, organisasjonsnummerProvider, tilknytningService) {
    private val log = KotlinLogging.logger {}

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
        traceAid.begin()
        countCall(FUNCTION_ID)

        return try {
            val foedselsdato: LocalDate = generelleDataHolder.getPerson(Pid(specV4.personId!!)).foedselDato
            val spec: SimuleringSpec = AlderspensjonSpecMapperV4.fromSpecV4(specV4, foedselsdato)
            request.setAttribute("pid", spec.pid)
            spec.pid?.let(::verifiserAtBrukerTilknyttetTpLeverandoer)
            resultV4(timed(service::simulerAlderspensjon, spec, FUNCTION_ID))
        } catch (e: FeilISimuleringsgrunnlagetException) {
            log.warn { "feil i simuleringsgrunnlaget - ${e.message} - request: $specV4" }
            feilInfoResultV4(e)
        } catch (e: RegelmotorValideringException) {
            log.warn { "feil i regelmotorvalidering - ${e.message} - request: $specV4" }
            feilInfoResultV4(e)
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
        private const val ERROR_MESSAGE = "feil ved simulering av alderspensjon"
        private const val FUNCTION_ID = "ap"

        private fun feilInfoResultV4(e: RuntimeException) =
            AlderspensjonResultV4(
                simuleringSuksess = false,
                aarsakListeIkkeSuksess = listOf(
                    PensjonSimuleringStatusV4(
                        statusKode = PensjonSimuleringStatusKodeV4.ANNET.externalValue,
                        statusBeskrivelse = e.message ?: e.javaClass.simpleName
                    )
                ),
                alderspensjon = emptyList(),
                forslagVedForLavOpptjening = null,
                harUttak = false
            )
    }
}

package no.nav.pensjon.simulator.alderspensjon.api.nav.viapen

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import mu.KotlinLogging
import no.nav.pensjon.simulator.alderspensjon.api.nav.viapen.acl.v2.result.NavSimuleringResultMapperV2.toSimuleringResultV2
import no.nav.pensjon.simulator.alderspensjon.api.nav.viapen.acl.v2.result.NavSimuleringSpecAndResultV2
import no.nav.pensjon.simulator.alderspensjon.api.nav.viapen.acl.v2.spec.NavSimuleringSpecMapperV2.fromSimuleringSpecV2
import no.nav.pensjon.simulator.alderspensjon.api.nav.viapen.acl.v2.spec.NavSimuleringSpecV2
import no.nav.pensjon.simulator.common.api.ControllerBase
import no.nav.pensjon.simulator.core.SimulatorCore
import no.nav.pensjon.simulator.core.SimulatorFlags
import no.nav.pensjon.simulator.core.result.SimulatorOutput
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.tech.trace.TraceAid
import no.nav.pensjon.simulator.tech.validation.InvalidEnumValueException
import no.nav.pensjon.simulator.tech.web.BadRequestException
import no.nav.pensjon.simulator.tech.web.EgressException
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api/nav")
@SecurityRequirement(name = "BearerAuthentication")
class NavViaPenAlderspensjonController(
    private val simulatorCore: SimulatorCore,
    private val traceAid: TraceAid
) : ControllerBase(traceAid) {
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
        log.debug { "$FUNCTION_ID request: $specV2" }
        countCall(FUNCTION_ID)

        return try {
            val spec: SimuleringSpec = fromSimuleringSpecV2(specV2)
            val output: SimulatorOutput = simulatorCore.simuler(spec, simulatorFlags)

            NavSimuleringSpecAndResultV2(
                simulering = specV2,
                simuleringsresultat = toSimuleringResultV2(output)
            )
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
        private const val ERROR_MESSAGE = "feil ved simulering av alderspensjon V2 for Nav-klient"
        private const val FUNCTION_ID = "nav-ap-v2"

        private val simulatorFlags =
            SimulatorFlags(
                inkluderLivsvarigOffentligAfp = false,
                inkluderPensjonBeholdninger = false,
                ignoreAvslag = false,
                outputSimulertBeregningInformasjonForAllKnekkpunkter = false
            )
    }
}

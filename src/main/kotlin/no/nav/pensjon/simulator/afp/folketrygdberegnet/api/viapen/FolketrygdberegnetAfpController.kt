package no.nav.pensjon.simulator.afp.folketrygdberegnet.api.viapen

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import mu.KotlinLogging
import no.nav.pensjon.simulator.afp.folketrygdberegnet.api.viapen.acl.v1.result.FolketrygdberegnetAfpResultMapperV1.toResultV1
import no.nav.pensjon.simulator.afp.folketrygdberegnet.api.viapen.acl.v1.result.FolketrygdberegnetAfpResultV1
import no.nav.pensjon.simulator.afp.folketrygdberegnet.api.viapen.acl.v1.spec.FolketrygdberegnetAfpSpecMapperV1.fromSimuleringSpecV1
import no.nav.pensjon.simulator.afp.folketrygdberegnet.api.viapen.acl.v1.spec.FolketrygdberegnetAfpSpecV1
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
class FolketrygdberegnetAfpController(
    private val simulator: SimulatorCore,
    private val traceAid: TraceAid
) : ControllerBase(traceAid) {
    private val log = KotlinLogging.logger {}

    /**
     * Supports PEN services selvbetjening/pensjonskalkulator/lagrefpp and foretaFolketrygdberegnetAfp.
     */
    @PostMapping("v1/simuler-folketrygdberegnet-afp")
    @Operation(
        summary = "Simuler folketrygdberegnet AFP V1",
        description = "Lager en prognose for folketrygdberegnet AFP, basert på Nav-lagret info og input fra bruker.",
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
    fun simulerFramtidigePensjonspoeng(@RequestBody specV1: FolketrygdberegnetAfpSpecV1): FolketrygdberegnetAfpResultV1? {
        traceAid.begin()
        log.debug { "$FUNCTION_ID request: $specV1" }
        countCall(FUNCTION_ID)

        return try {
            val spec: SimuleringSpec = fromSimuleringSpecV1(specV1)
            val output: SimulatorOutput = simulator.simuler(spec, simulatorFlags) //TODO check
            toResultV1(output)
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
        private const val ERROR_MESSAGE = "feil ved simulering av folketrygdberegnet AFP V1"
        private const val FUNCTION_ID = "ftb-afp-v1"

        private val simulatorFlags =
            SimulatorFlags(
                inkluderLivsvarigOffentligAfp = false,
                ignoreAvslag = false
            )
    }
}

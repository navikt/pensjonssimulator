package no.nav.pensjon.simulator.alderspensjon.tpo.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.servlet.http.HttpServletRequest
import mu.KotlinLogging
import no.nav.pensjon.simulator.alderspensjon.tpo.api.acl.v3in.TpoSimuleringSpecMapperV3
import no.nav.pensjon.simulator.alderspensjon.tpo.api.acl.v3in.TpoSimuleringSpecV3
import no.nav.pensjon.simulator.alderspensjon.tpo.api.acl.v3out.TpoSimuleringResultMapperV3
import no.nav.pensjon.simulator.alderspensjon.tpo.api.acl.v3out.TpoSimuleringResultV3
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

/**
 * REST-controller for simulering av alderspensjon.
 * Tjenestene er forbeholdt å anvendes av tjenestepensjonsordninger (TPO).
 */
@RestController
@RequestMapping("api/tpo")
@SecurityRequirement(name = "BearerAuthentication")
class TpoAlderspensjonController(
    private val simulatorCore: SimulatorCore,
    private val traceAid: TraceAid
) : ControllerBase(traceAid, organisasjonsnummerProvider = null, tpregisteretService = null) {
    private val log = KotlinLogging.logger {}

    @PostMapping("v3/simuler-alderspensjon")
    @Operation(
        summary = "Simuler alderspensjon for tjenestepensjonsordning (V3)",
        description = "Lager en prognose for utbetaling av alderspensjon (versjon 3 av tjenesten).",
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
    fun simulerAlderspensjonV3(
        @RequestBody specV3: TpoSimuleringSpecV3,
        request: HttpServletRequest
    ): TpoSimuleringResultV3 {
        traceAid.begin()
        log.debug { "$FUNCTION_ID request: $specV3" }
        countCall(FUNCTION_ID)

        return try {
            val spec: SimuleringSpec = TpoSimuleringSpecMapperV3.fromDto(specV3)
            val result: SimulatorOutput = simulatorCore.simuler(spec, simulatorFlags(spec))
            TpoSimuleringResultMapperV3.toDto(result)
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
        private const val ERROR_MESSAGE = "feil ved V3 simulering av alderspensjon for TPO"
        private const val FUNCTION_ID = "ap-v3"

        //TODO these flags are included in SimuleringSpec, hence redundant?
        private fun simulatorFlags(spec: SimuleringSpec) =
            SimulatorFlags(
                inkluderLivsvarigOffentligAfp = false,
                inkluderPensjonBeholdninger = spec.isHentPensjonsbeholdninger,
                ignoreAvslag = false,
                outputSimulertBeregningInformasjonForAllKnekkpunkter = spec.isOutputSimulertBeregningsinformasjonForAllKnekkpunkter
            )
    }
}

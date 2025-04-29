package no.nav.pensjon.simulator.alderspensjon.api.nav.direct

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import mu.KotlinLogging
import no.nav.pensjon.simulator.alderspensjon.alternativ.SimuleringFacade
import no.nav.pensjon.simulator.alderspensjon.alternativ.SimulertPensjonEllerAlternativ
import no.nav.pensjon.simulator.alderspensjon.api.nav.direct.acl.v3.result.NavSimuleringErrorV3
import no.nav.pensjon.simulator.alderspensjon.api.nav.direct.acl.v3.result.NavSimuleringResultMapperV3.toDto
import no.nav.pensjon.simulator.alderspensjon.api.nav.direct.acl.v3.result.NavSimuleringResultV3
import no.nav.pensjon.simulator.alderspensjon.api.nav.direct.acl.v3.result.NavVilkaarsproevingResultatV3
import no.nav.pensjon.simulator.alderspensjon.api.nav.direct.acl.v3.spec.NavSimuleringSpecMapperV3
import no.nav.pensjon.simulator.alderspensjon.api.nav.direct.acl.v3.spec.NavSimuleringSpecV3
import no.nav.pensjon.simulator.common.api.ControllerBase
import no.nav.pensjon.simulator.core.afp.offentlig.pre2025.Pre2025OffentligAfpAvslaattException
import no.nav.pensjon.simulator.core.exception.*
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.regel.client.GrunnbeloepService
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
class NavAlderspensjonController(
    private val service: SimuleringFacade,
    private val specMapper: NavSimuleringSpecMapperV3,
    private val grunnbeloepService: GrunnbeloepService,
    private val traceAid: TraceAid
) : ControllerBase(traceAid) {
    private val log = KotlinLogging.logger {}

    /**
     * This shall replace PEN service /springapi/simulering/alderspensjon
     */
    @PostMapping("v3/simuler-alderspensjon")
    @Operation(
        summary = "Simuler alderspensjon V3 for Nav-klient",
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
    fun simulerAlderspensjon(@RequestBody specV3: NavSimuleringSpecV3): NavSimuleringResultV3 {
        traceAid.begin()
        log.debug { "$FUNCTION_ID request: $specV3" }
        countCall(FUNCTION_ID)

        return try {
            val inntektSisteMaanedOver1G = specV3.afpInntektMaanedFoerUttak?.let(grunnbeloepService::hentSisteMaanedsInntektOver1G)
            val spec: SimuleringSpec = specMapper.fromNavSimuleringSpecV3(specV3, inntektSisteMaanedOver1G)

            val result: SimulertPensjonEllerAlternativ =
                service.simulerAlderspensjon(spec, inkluderPensjonHvisUbetinget = false)

            toDto(result)
        } catch (e: FeilISimuleringsgrunnlagetException) {
            resultWithErrorInfo("simuleringsgrunnlaget", e, specV3)
        } catch (e: ImplementationUnrecoverableException) {
            resultWithErrorInfo("systemet", e, specV3)
        } catch (e: InvalidArgumentException) {
            resultWithErrorInfo("argument", e, specV3)
        } catch (e: KanIkkeBeregnesException) {
            resultWithErrorInfo("beregningsgrunnlaget", e, specV3)
        } catch (e: KonsistensenIGrunnlagetErFeilException) {
            resultWithErrorInfo("grunnlagskonsistens", e, specV3)
        } catch (e: PersonForGammelException) {
            resultWithErrorInfo("personalder (for høy)", e, specV3)
        } catch (e: PersonForUngException) {
            resultWithErrorInfo("personalder (for lav)", e, specV3)
        } catch (e: Pre2025OffentligAfpAvslaattException) {
            resultWithErrorInfo("pre-2025 offentlig AFP grunnlag (avslått)", e, specV3)
        } catch (e: RegelmotorValideringException) {
            resultWithErrorInfo("regelmotorvalidering", e, specV3)
        } catch (e: UtilstrekkeligOpptjeningException) {
            resultWithErrorInfo("opptjening (utilstrekkelig)", e, specV3)
        } catch (e: UtilstrekkeligTrygdetidException) {
            resultWithErrorInfo("trygdetid (utilstrekkelig)", e, specV3)
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

    private fun resultWithErrorInfo(
        subject: String,
        e: RuntimeException,
        specV3: NavSimuleringSpecV3
    ): NavSimuleringResultV3 {
        log.warn(e) { "feil i $subject - ${e.message} - request: $specV3" }
        return resultWithErrorInfo(e)
    }

    override fun errorMessage() = ERROR_MESSAGE

    private companion object {
        private const val ERROR_MESSAGE = "feil ved simulering av alderspensjon for Nav-klient"
        private const val FUNCTION_ID = "nav-ap"

        private fun resultWithErrorInfo(e: RuntimeException) =
            NavSimuleringResultV3(
                alderspensjonListe = emptyList(),
                alderspensjonMaanedsbeloep = null,
                pre2025OffentligAfp = null,
                privatAfpListe = emptyList(),
                livsvarigOffentligAfpListe = emptyList(),
                vilkaarsproeving = NavVilkaarsproevingResultatV3(
                    vilkaarErOppfylt = false,
                    alternativ = null
                ),
                tilstrekkeligTrygdetidForGarantipensjon = null,
                trygdetid = 0,
                opptjeningGrunnlagListe = emptyList(),
                error = NavSimuleringErrorV3(
                    exception = e.javaClass.simpleName,
                    message = extractMessageRecursively(e)
                )
            )
    }
}

package no.nav.pensjon.simulator.afp_etterfulgt_ap.api.tpo.acl

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.servlet.http.HttpServletRequest
import mu.KotlinLogging
import no.nav.pensjon.simulator.afp_etterfulgt_ap.api.tpo.acl.v0.result.AarsakIkkeSuccessV0
import no.nav.pensjon.simulator.afp_etterfulgt_ap.api.tpo.acl.v0.result.AfpEtterfulgtAvAlderspensjonResultMapperV0.toDto
import no.nav.pensjon.simulator.afp_etterfulgt_ap.api.tpo.acl.v0.result.AfpEtterfulgtAvAlderspensjonResultMapperV0.tomResponsMedAarsak
import no.nav.pensjon.simulator.afp_etterfulgt_ap.api.tpo.acl.v0.result.AfpEtterfulgtAvAlderspensjonResultV0
import no.nav.pensjon.simulator.afp_etterfulgt_ap.api.tpo.acl.v0.spec.AfpEtterfulgtAvAlderspensjonSpecMapperV0
import no.nav.pensjon.simulator.afp_etterfulgt_ap.api.tpo.acl.v0.spec.AfpEtterfulgtAvAlderspensjonSpecV0
import no.nav.pensjon.simulator.afp_etterfulgt_ap.api.tpo.acl.v0.spec.AfpEtterfulgtAvAlderspensjonSpecValidator.validateSpec
import no.nav.pensjon.simulator.common.api.ControllerBase
import no.nav.pensjon.simulator.core.SimulatorCore
import no.nav.pensjon.simulator.core.afp.offentlig.pre2025.Pre2025OffentligAfpAvslaattException
import no.nav.pensjon.simulator.core.exception.*
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.generelt.organisasjon.OrganisasjonsnummerProvider
import no.nav.pensjon.simulator.opptjening.HentSisteLignetInntektService
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.regel.client.GrunnbeloepService
import no.nav.pensjon.simulator.tech.sporing.web.SporingInterceptor
import no.nav.pensjon.simulator.tech.trace.TraceAid
import no.nav.pensjon.simulator.tech.web.EgressException
import no.nav.pensjon.simulator.tjenestepensjon.TilknytningService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * REST-controller for simulering av afp etterfulgt alderspensjon Kap. 19.
 * Tjenesten er ment å brukes av tjenestepensjonsordninger (TPO).
 */
@RestController
@RequestMapping("api")
@SecurityRequirement(name = "BearerAuthentication")
class TpoAfpEtterfulgtAvAlderspensjonController(
    private val hentSisteLignetInntektService: HentSisteLignetInntektService,
    private val grunnbeloepService: GrunnbeloepService,
    private val simulatorCore: SimulatorCore,
    private val traceAid: TraceAid,
    organisasjonsnummerProvider: OrganisasjonsnummerProvider,
    tilknytningService: TilknytningService,
) : ControllerBase(traceAid, organisasjonsnummerProvider, tilknytningService) {
    private val log = KotlinLogging.logger {}

    @PostMapping("v0/simuler-afp-etterfulgt-av-alderspensjon")
    @Operation(
        summary = "Simuler afp etterfulgt av alderspensjon",
        description = "Lager en prognose for utbetaling av afp etterfulgt av alderspensjon.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Simulering av afp etterfulgt av alderspensjon utført."
            ),
            ApiResponse(
                responseCode = "400",
                description = "Simulering kunne ikke utføres pga. uakseptabel input."
            )
        ]
    )
    fun simulerAfpEtterfulgtAvAlderspensjonV0(
        @RequestBody specV0: AfpEtterfulgtAvAlderspensjonSpecV0,
        request: HttpServletRequest
    ): AfpEtterfulgtAvAlderspensjonResultV0 {
        traceAid.begin(request)
        countCall(FUNCTION_ID)
        request.setAttribute(SporingInterceptor.PID_ATTRIBUTE_NAME, specV0.personId)

        return try {
            val validatedSpecV0 = validateSpec(specV0)
            verifiserAtBrukerTilknyttetTpLeverandoer(Pid(validatedSpecV0.personId))

            val spec: SimuleringSpec = AfpEtterfulgtAvAlderspensjonSpecMapperV0.fromDto(
                validatedSpecV0,
                grunnbeloepService::hentAaretsGrunnbeloep,
                hentSisteLignetInntektService::hentSisteLignetInntekt
            )

            toDto(simulatorCore.simuler(spec), spec.inntektUnderGradertUttakBeloep)
        } catch (e: BadSpecException) {
            log.warn(e) { "$FUNCTION_ID bad request - ${e.message} - $specV0" }
            throw e
        } catch (e: FeilISimuleringsgrunnlagetException) {
            log.warn(e) { "$FUNCTION_ID feil i simuleringsgrunnlaget - request - $specV0" }
            tomResponsMedAarsak(AarsakIkkeSuccessV0.FEIL_I_GRUNNLAG)
        } catch (e: ImplementationUnrecoverableException) {
            log.error(e) { "$FUNCTION_ID unrecoverable error - request - $specV0" }
            throw e
        }  catch (e: KonsistensenIGrunnlagetErFeilException) {
            log.warn(e) { "$FUNCTION_ID inkonsistent grunnlag - request - $specV0" }
            tomResponsMedAarsak(AarsakIkkeSuccessV0.FEIL_I_GRUNNLAG)
        } catch (e: PersonForGammelException) {
            log.warn(e) { "$FUNCTION_ID person for gammel - request - $specV0" }
            tomResponsMedAarsak(AarsakIkkeSuccessV0.FOR_HOEY_ALDER)
        } catch (e: PersonForUngException) {
            log.warn(e) { "$FUNCTION_ID person for ung - request - $specV0" }
            tomResponsMedAarsak(AarsakIkkeSuccessV0.FOR_LAV_ALDER)
        } catch (e: Pre2025OffentligAfpAvslaattException){
            log.warn(e) { "$FUNCTION_ID pre-2025 offentlig AFP avslått - request - $specV0" }
            tomResponsMedAarsak(AarsakIkkeSuccessV0.AFP_ER_AVSLAATT)
        } catch (e: RegelmotorValideringException) {
            log.warn(e) { "$FUNCTION_ID regelmotorvalideringsfeil - request - $specV0" }
            throw e
        } catch (e: UtilstrekkeligOpptjeningException) {
            log.warn(e) { "$FUNCTION_ID utilstrekkelig opptjening - request - $specV0" }
            tomResponsMedAarsak(AarsakIkkeSuccessV0.UTILSTREKKELIG_OPPTJENING)
        } catch (e: UtilstrekkeligTrygdetidException) {
            log.warn(e) { "$FUNCTION_ID utilstrekkelig trygdetid - request - $specV0" }
            tomResponsMedAarsak(AarsakIkkeSuccessV0.UTILSTREKKELIG_TRYGDETID)
        } catch (e: EgressException) {
            handle(e)!!
        } finally {
            traceAid.end()
        }
    }

    @ExceptionHandler(
        value = [
            BadSpecException::class,
        ]
    )
    private fun handleBadRequest(e: RuntimeException): ResponseEntity<TpoSimuleringErrorDto> =
        ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorDto(e.message!!))

    @ExceptionHandler(
        value = [
            RegelmotorValideringException::class,
            ImplementationUnrecoverableException::class
        ]
    )
    fun handleInternalServerError(e: RuntimeException): ResponseEntity<TpoSimuleringErrorDto> =
        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorDto(e))

    override fun errorMessage() = ERROR_MESSAGE

    /**
     * Ref. PEN JsonErrorEntityBuilder.createErrorEntity
     */
    data class TpoSimuleringErrorDto(val feil: String)

    private companion object {
        private const val ERROR_MESSAGE = "feil ved simulering av AFP etterfulgt av alderspensjon V0"
        private const val FUNCTION_ID = "afp-etterf-av-ap-tpo-v0"
        private fun errorDto(e: RuntimeException) =
            TpoSimuleringErrorDto(
                feil = e.javaClass.simpleName
            )
        private fun errorDto(error: String) =
            TpoSimuleringErrorDto(
                feil = error
            )
    }
}
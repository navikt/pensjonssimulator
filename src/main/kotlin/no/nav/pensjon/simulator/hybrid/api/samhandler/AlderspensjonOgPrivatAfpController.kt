package no.nav.pensjon.simulator.hybrid.api.samhandler

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.servlet.http.HttpServletRequest
import mu.KotlinLogging
import no.nav.pensjon.simulator.afp.offentlig.pre2025.Pre2025OffentligAfpAvslaattException
import no.nav.pensjon.simulator.common.api.ControllerBase
import no.nav.pensjon.simulator.core.SimulatorCore
import no.nav.pensjon.simulator.core.exception.*
import no.nav.pensjon.simulator.core.result.SimulatorOutput
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.generelt.organisasjon.OrganisasjonsnummerProvider
import no.nav.pensjon.simulator.hybrid.api.samhandler.acl.v3.*
import no.nav.pensjon.simulator.statistikk.StatistikkService
import no.nav.pensjon.simulator.tech.sporing.web.SporingInterceptor
import no.nav.pensjon.simulator.tech.trace.TraceAid
import no.nav.pensjon.simulator.tech.validation.InvalidEnumValueException
import no.nav.pensjon.simulator.tech.web.BadRequestException
import no.nav.pensjon.simulator.tech.web.EgressException
import no.nav.pensjon.simulator.tjenestepensjon.TilknytningService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime
import java.time.format.DateTimeParseException

/**
 * REST-controller for simulering av alderspensjon og privat AFP ("hybrid" av disse to pensjonstypene).
 * Tjenesten er ment å brukes av samhandlere.
 * Samhandlere gjør kall til pensjonssimulator via API-gateway.
 */
@RestController
@RequestMapping("api")
@SecurityRequirement(name = "BearerAuthentication")
class AlderspensjonOgPrivatAfpController(
    private val simulatorCore: SimulatorCore,
    private val specMapper: AlderspensjonOgPrivatAfpSpecMapperV3,
    private val resultMapper: AlderspensjonOgPrivatAfpResultMapperV3,
    private val traceAid: TraceAid,
    statistikk: StatistikkService,
    organisasjonsnummerProvider: OrganisasjonsnummerProvider,
    tilknytningService: TilknytningService
) : ControllerBase(traceAid, statistikk, organisasjonsnummerProvider, tilknytningService) {
    private val log = KotlinLogging.logger {}

    @PostMapping("v3/simuler-alderspensjon-privat-afp")
    @Operation(
        summary = "Simuler alderspensjon og privat AFP for samhandler (V3)",
        description = "Lager en prognose for utbetaling av alderspensjon og privat avtalefestet pensjon" +
                " (versjon 3 av tjenesten)." +
                "\\\n\\\n*Scope*: **nav:pensjon/simulering/alderspensjonogprivatafp**"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Simulering av alderspensjon og privat AFP utført."
            ),
            ApiResponse(
                responseCode = "400",
                description = "Simulering kunne ikke utføres pga. uakseptable inndata."
            )
        ]
    )
    fun simulerAlderspensjonOgPrivatAfpV3(
        @RequestBody specV3: AlderspensjonOgPrivatAfpSpecV3,
        request: HttpServletRequest
    ): AlderspensjonOgPrivatAfpResultV3 {
        traceAid.begin()
        log.debug { "$FUNCTION_ID_V3 request: $specV3" }
        countCall(FUNCTION_ID_V3)

        return try {
            val spec: SimuleringSpec = specMapper.fromDto(specV3)
            registrerHendelse(simuleringstype = spec.type)
            request.setAttribute(SporingInterceptor.PID_ATTRIBUTE_NAME, spec.pid!!)
            val result: SimulatorOutput = simulatorCore.simuler(spec)

            resultMapper.toDto(simuleringResult = result, pid = spec.pid).also {
                log.debug { "$FUNCTION_ID_V3 response: $it" }
            }
        } catch (e: BadRequestException) {
            log.warn(e) { "$FUNCTION_ID_V3 bad request - $specV3" }
            throw e // delegate handling to ExceptionHandler to avoid returning ResponseEntity<Any>
        } catch (e: BadSpecException) {
            log.warn { "$FUNCTION_ID_V3 bad spec - ${extractMessageRecursively(e)} - $specV3" } // not log.warn(e)
            throw e
        } catch (e: DateTimeParseException) {
            log.warn { "$FUNCTION_ID_V3 feil datoformat (forventet yyyy-mm-dd) - ${extractMessageRecursively(e)} - request: $specV3" }
            throw e
        } catch (e: FeilISimuleringsgrunnlagetException) {
            log.warn(e) { "$FUNCTION_ID_V3 feil i simuleringsgrunnlaget - request - $specV3" }
            throw e
        } catch (e: ImplementationUnrecoverableException) {
            log.error(e) { "$FUNCTION_ID_V3 unrecoverable error - request - $specV3" }
            throw e
        } catch (e: InvalidArgumentException) {
            log.warn(e) { "$FUNCTION_ID_V3 invalid argument - request - $specV3" }
            throw e
        } catch (e: InvalidEnumValueException) {
            log.warn(e) { "$FUNCTION_ID_V3 invalid enum value - request - $specV3" }
            throw e
        } catch (e: KanIkkeBeregnesException) {
            log.warn(e) { "$FUNCTION_ID_V3 kan ikke beregnes - request - $specV3" }
            throw e
        } catch (e: KonsistensenIGrunnlagetErFeilException) {
            log.warn(e) { "$FUNCTION_ID_V3 inkonsistent grunnlag - request - $specV3" }
            throw e
        } catch (e: PersonForGammelException) {
            log.warn(e) { "$FUNCTION_ID_V3 person for gammel - request - $specV3" }
            throw e
        } catch (e: PersonForUngException) {
            log.warn(e) { "$FUNCTION_ID_V3 person for ung - request - $specV3" }
            throw e
        } catch (e: Pre2025OffentligAfpAvslaattException) {
            log.warn(e) { "$FUNCTION_ID_V3 pre-2025 offentlig AFP avslått - request - $specV3" }
            throw e
        } catch (e: RegelmotorValideringException) {
            log.warn(e) { "$FUNCTION_ID_V3 regelmotorvalideringsfeil - request - $specV3" }
            throw e
        } catch (e: UtilstrekkeligOpptjeningException) {
            log.warn(e) { "$FUNCTION_ID_V3 utilstrekkelig opptjening - request - $specV3" }
            throw e
        } catch (e: UtilstrekkeligTrygdetidException) {
            log.warn(e) { "$FUNCTION_ID_V3 utilstrekkelig trygdetid - request - $specV3" }
            throw e
        } catch (e: EgressException) {
            handle(e)!!
        } finally {
            traceAid.end()
        }
    }

    /**
     * PEN: DefaultSimulerePensjonProvider.simulerFleksibelAp
     *   + no.nav.tjeneste.ekstern.simulerepensjon.v1.feil.ForretningsmessigUnntak
     */
    @ExceptionHandler(
        value = [
            PersonForGammelException::class, // PEN: FoedtFoer1943
            UtilstrekkeligOpptjeningException::class, // PEN: ForLavtTidligUttak
            UtilstrekkeligTrygdetidException::class, // PEN: ForKortTrygdetid
            //TODO: PEN: UgyldigInput - Jira TPP-47
            BadRequestException::class,
            BadSpecException::class,
            DateTimeParseException::class,
            FeilISimuleringsgrunnlagetException::class,
            InvalidArgumentException::class,
            InvalidEnumValueException::class,
            KanIkkeBeregnesException::class,
            KonsistensenIGrunnlagetErFeilException::class,
            PersonForUngException::class,
            Pre2025OffentligAfpAvslaattException::class,
            RegelmotorValideringException::class
            //TODO PEN222BeregningstjenesteFeiletException, PEN223BrukerHarIkkeLopendeAlderspensjonException, PEN226BrukerHarLopendeAPPaGammeltRegelverkException - Jira TPP-44
            //TODO Kopier ThrowableExceptionMapper fra PEN - Jira TPP-45
        ]
    )
    private fun forretningsmessigUnntak(e: RuntimeException): ResponseEntity<ForretningsmessigUnntakReasonV3> =
        ResponseEntity.status(HttpStatus.BAD_REQUEST).body(forretningsmessigUnntakReason(e))

    @ExceptionHandler(
        value = [
            ImplementationUnrecoverableException::class
        ]
    )
    private fun handleInternalServerError(e: RuntimeException): ResponseEntity<InternalServerErrorReasonV3> =
        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(internalServerErrorReason())

    override fun errorMessage() = ERROR_MESSAGE

    private companion object {
        private const val TJENESTE = "simulering av alderspensjon/privat AFP"
        private const val ERROR_MESSAGE = "feil ved $TJENESTE"
        private const val FUNCTION_ID_V3 = "sam-ap-pafp"

        /**
         * PEN: DefaultSimulerePensjonProvider.createForretningsmessigUnntakWith
         */
        private fun forretningsmessigUnntakReason(e: RuntimeException) =
            ForretningsmessigUnntakReasonV3(
                feilkilde = TJENESTE,
                feilaarsak = "${e.javaClass.simpleName}: ${e.message}",
                feilmelding = feilmelding(e),
                tidspunkt = LocalDateTime.now()
            )

        private fun internalServerErrorReason() =
            InternalServerErrorReasonV3(
                feilkilde = TJENESTE,
                feilmelding = "intern feil i tjenesten",
                tidspunkt = LocalDateTime.now()
            )

        /**
         * PEN: DefaultSimulerePensjonProvider.simulerFleksibelAp
         */
        private fun feilmelding(e: RuntimeException) =
            when (e) {
                is PersonForGammelException -> "Bruker er født før 1943"
                is UtilstrekkeligOpptjeningException -> "Avslag på vilkårsprøving grunnet for lavt tidlig uttak"
                is UtilstrekkeligTrygdetidException -> "Avslag på vilkårsprøving grunnet for kort trygdetid"
                else -> "Annen årsak"
            }
    }
}

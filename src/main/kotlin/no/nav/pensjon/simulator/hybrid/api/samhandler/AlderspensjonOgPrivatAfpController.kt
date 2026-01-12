package no.nav.pensjon.simulator.hybrid.api.samhandler

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.servlet.http.HttpServletRequest
import mu.KotlinLogging
import no.nav.pensjon.simulator.afp.offentlig.pre2025.Pre2025OffentligAfpAvslaattException
import no.nav.pensjon.simulator.common.api.ControllerBase
import no.nav.pensjon.simulator.core.exception.*
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.generelt.organisasjon.OrganisasjonsnummerProvider
import no.nav.pensjon.simulator.hybrid.AlderspensjonOgPrivatAfpService
import no.nav.pensjon.simulator.hybrid.api.samhandler.acl.v3.*
import no.nav.pensjon.simulator.hybrid.api.samhandler.acl.v3.AlderspensjonOgPrivatAfpResultMapperV3.toDto
import no.nav.pensjon.simulator.statistikk.StatistikkService
import no.nav.pensjon.simulator.tech.sporing.web.SporingInterceptor
import no.nav.pensjon.simulator.tech.trace.TraceAid
import no.nav.pensjon.simulator.tech.validation.InvalidEnumValueException
import no.nav.pensjon.simulator.tech.web.BadRequestException
import no.nav.pensjon.simulator.tjenestepensjon.TilknytningService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
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
    private val service: AlderspensjonOgPrivatAfpService,
    private val specMapper: AlderspensjonOgPrivatAfpSpecMapperV3,
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
                description = "Simulering av alderspensjon og privat AFP utført, eller personen har utilstrekkelig opptjening/trygdetid"
            ),
            ApiResponse(
                responseCode = "400",
                description = "Simulering kunne ikke utføres pga. uakseptable inndata."
            ),
            ApiResponse(
                responseCode = "401",
                description = "Simulering kunne ikke utføres pga. manglende/feilaktig autentisering."
            ),
            ApiResponse(
                responseCode = "403",
                description = "Simulering kunne ikke utføres pga. manglende tilgang til tjenesten."
            ),
            ApiResponse(
                responseCode = "404",
                description = "Simulering kunne ikke utføres fordi angitt person ikke finnes i systemet."
            ),
            ApiResponse(
                responseCode = "500",
                description = "Simulering kunne ikke utføres pga. feil i systemet."
            )
        ]
    )
    fun simulerAlderspensjonOgPrivatAfpV3(
        @RequestBody specV3: AlderspensjonOgPrivatAfpSpecV3,
        request: HttpServletRequest
    ): ResponseEntity<AlderspensjonOgPrivatAfpResultV3> {
        traceAid.begin()
        log.debug { "$FUNCTION_ID_V3 request: $specV3" }
        countCall(FUNCTION_ID_V3)

        return try {
            val spec: SimuleringSpec = specMapper.fromDto(specV3)
            registrerHendelse(simuleringstype = spec.type)
            request.setAttribute(SporingInterceptor.PID_ATTRIBUTE_NAME, spec.pid!!)
            val result = toDto(service.simuler(spec))
            ResponseEntity.status(result.problem?.kode?.httpStatus ?: HttpStatus.OK).body(result)
        } finally {
            traceAid.end()
        }
    }

    override fun errorMessage() = ERROR_MESSAGE

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
    private fun clientError(e: RuntimeException): ResponseEntity<AlderspensjonOgPrivatAfpResultV3> =
        errorEntity(e, type = ProblemTypeV3.ANNEN_KLIENTFEIL)

    @ExceptionHandler(
        value = [
            ImplementationUnrecoverableException::class,
            Exception::class
        ]
    )
    private fun serverError(e: RuntimeException): ResponseEntity<AlderspensjonOgPrivatAfpResultV3> =
        errorEntity(e, type = ProblemTypeV3.SERVERFEIL)

    private companion object {
        private const val TJENESTE = "simulering av alderspensjon/privat AFP"
        private const val ERROR_MESSAGE = "feil ved $TJENESTE"
        private const val FUNCTION_ID_V3 = "sam-ap-pafp"

        private fun errorEntity(
            e: RuntimeException,
            type: ProblemTypeV3
        ): ResponseEntity<AlderspensjonOgPrivatAfpResultV3> =
            with(type) {
                ResponseEntity.status(this.httpStatus).body(problem(e, type = this))
            }

        private fun problem(e: RuntimeException, type: ProblemTypeV3) =
            AlderspensjonOgPrivatAfpResultV3(
                suksess = false,
                alderspensjonsperioder = emptyList(),
                privatAfpPerioder = emptyList(),
                harNaavaerendeUttak = false,
                harTidligereUttak = false,
                harLoependePrivatAfp = false,
                problem = ProblemV3(kode = type, beskrivelse = e.message ?: "Ukjent feil - ${e.javaClass.simpleName}")
            )
    }
}

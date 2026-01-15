package no.nav.pensjon.simulator.alderspensjon.api.samhandler

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.servlet.http.HttpServletRequest
import mu.KotlinLogging
import no.nav.pensjon.simulator.alderspensjon.AlderspensjonService
import no.nav.pensjon.simulator.alderspensjon.api.samhandler.acl.v4.*
import no.nav.pensjon.simulator.alderspensjon.api.samhandler.acl.v4.AlderspensjonResultMapperV4.resultV4
import no.nav.pensjon.simulator.alderspensjon.spec.AlderspensjonSpec
import no.nav.pensjon.simulator.common.api.ControllerBase
import no.nav.pensjon.simulator.core.domain.regler.enum.SimuleringTypeEnum
import no.nav.pensjon.simulator.core.exception.*
import no.nav.pensjon.simulator.generelt.organisasjon.OrganisasjonsnummerProvider
import no.nav.pensjon.simulator.statistikk.StatistikkService
import no.nav.pensjon.simulator.tech.sporing.web.SporingInterceptor
import no.nav.pensjon.simulator.tech.trace.TraceAid
import no.nav.pensjon.simulator.tech.validation.InvalidEnumValueException
import no.nav.pensjon.simulator.tech.web.BadRequestException
import no.nav.pensjon.simulator.tech.web.EgressException
import no.nav.pensjon.simulator.tjenestepensjon.TilknytningService
import no.nav.pensjon.simulator.validity.BadSpecException
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.format.DateTimeParseException

/**
 * REST-controller for simulering av alderspensjon for personer født 1963 eller senere.
 * Tjenestene er ment å brukes av samhandlere (tjenestepensjonsordninger).
 * Samhandlere gjør kall til pensjonssimulator via API-gateway.
 */
@RestController
@RequestMapping("api")
@SecurityRequirement(name = "BearerAuthentication")
class SamhandlerAlderspensjonControllerV4(
    private val service: AlderspensjonService,
    private val traceAid: TraceAid,
    statistikk: StatistikkService,
    organisasjonsnummerProvider: OrganisasjonsnummerProvider,
    tilknytningService: TilknytningService
) : ControllerBase(traceAid, statistikk, organisasjonsnummerProvider, tilknytningService) {
    private val log = KotlinLogging.logger {}

    @PostMapping("v4/simuler-alderspensjon")
    @Operation(
        summary = "Simuler alderspensjon for personer født i 1963 eller senere.",
        description = "Lager en prognose for utbetaling av alderspensjon for personer født i 1963 eller senere." +
                "\\\n\\\n*Scope*:" +
                "\\\n– Uten delegering: **nav:pensjonssimulator:simulering**" +
                "\\\n– Med delegering: **nav:pensjon/simulering.read**"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Simulering av alderspensjon utført."
            ),
            ApiResponse(
                responseCode = "400",
                description = "Simulering kunne ikke utføres pga. uakseptable inndata. Det kan være:" +
                        "\\\n(1) helt uttak ikke etter gradert uttak," +
                        "\\\n(2) inntekt ikke 1. i måneden," +
                        "\\\n(3) inntekter har lik startdato, " +
                        "\\\n(4) negativ inntekt."
            )
        ]
    )
    fun simulerAlderspensjon(
        @RequestBody specV4: AlderspensjonSpecV4,
        request: HttpServletRequest
    ): AlderspensjonResultV4 {
        traceAid.begin(request)
        countCall(FUNCTION_ID_V4)

        return try {
            val spec: AlderspensjonSpec = AlderspensjonSpecMapperV4.fromDto(specV4)
            registrerHendelse(simuleringstype = SimuleringTypeEnum.ALDER)
            request.setAttribute(SporingInterceptor.PID_ATTRIBUTE_NAME, spec.pid)
            verifiserAtBrukerTilknyttetTpLeverandoer(spec.pid)
            resultV4(timed(service::simulerAlderspensjon, spec, FUNCTION_ID_V4))
        } catch (e: BadSpecException) {
            log.warn { "$FUNCTION_ID_V4 feil i spesifikasjonen - ${e.message} - request: $specV4" }
            feilInfoResultV4(e)
        } catch (e: DateTimeParseException) {
            log.warn { "$FUNCTION_ID_V4 feil datoformat (forventet yyyy-mm-dd) - ${e.message} - request: $specV4" }
            feilInfoResultV4(e)
        } catch (e: FeilISimuleringsgrunnlagetException) {
            log.warn { "$FUNCTION_ID_V4 feil i simuleringsgrunnlaget - ${e.message} - request: $specV4" }
            feilInfoResultV4(e)
        } catch (e: InvalidArgumentException) {
            log.warn { "$FUNCTION_ID_V4 invalid argument - ${e.message} - request: $specV4" }
            feilInfoResultV4(e)
        } catch (e: InvalidEnumValueException) {
            log.warn { "$FUNCTION_ID_V4 invalid enum value - ${e.message} - request: $specV4" }
            feilInfoResultV4(e)
        } catch (e: RegelmotorValideringException) {
            log.warn { "$FUNCTION_ID_V4 feil i regelmotorvalidering - ${e.message} - request: $specV4" }
            feilInfoResultV4(e)
        } catch (e: UtilstrekkeligOpptjeningException) {
            log.info { "$FUNCTION_ID_V4 utilstrekkelig opptjening - ${e.message} - request: $specV4" }
            feilInfoResultV4(e, PensjonSimuleringStatusKodeV4.AVSLAG_FOR_LAV_OPPTJENING)
        } catch (e: UtilstrekkeligTrygdetidException) {
            log.info { "$FUNCTION_ID_V4 utilstrekkelig trygdetid - ${e.message} - request: $specV4" }
            feilInfoResultV4(e, PensjonSimuleringStatusKodeV4.AVSLAG_FOR_KORT_TRYGDETID)
        } catch (e: EgressException) {
            handle(e)!!
        } catch (e: BadRequestException) {
            badRequest(e)!!
        } finally {
            traceAid.end()
        }
    }

    override fun errorMessage() = ERROR_MESSAGE

    private companion object {
        private const val ERROR_MESSAGE = "feil ved simulering av alderspensjon V4"
        private const val FUNCTION_ID_V4 = "apv4"

        private fun feilInfoResultV4(
            e: Exception,
            status: PensjonSimuleringStatusKodeV4 = PensjonSimuleringStatusKodeV4.ANNET
        ) =
            AlderspensjonResultV4(
                simuleringSuksess = false,
                aarsakListeIkkeSuksess = listOf(
                    PensjonSimuleringStatusV4(
                        statusKode = status.externalValue,
                        statusBeskrivelse = e.message ?: e.javaClass.simpleName
                    )
                ),
                alderspensjon = emptyList(),
                forslagVedForLavOpptjening = null,
                harUttak = false
            )
    }
}

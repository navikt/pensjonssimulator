package no.nav.pensjon.simulator.common.api

import mu.KotlinLogging
import no.nav.pensjon.simulator.generelt.organisasjon.Organisasjonsnummer
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.tech.metric.Metrics
import no.nav.pensjon.simulator.tech.metric.Organisasjoner
import no.nav.pensjon.simulator.generelt.organisasjon.OrganisasjonsnummerProvider
import no.nav.pensjon.simulator.tech.trace.TraceAid
import no.nav.pensjon.simulator.tech.web.EgressException
import no.nav.pensjon.simulator.tjenestepensjon.TilknytningService
import org.intellij.lang.annotations.Language
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.lang.System.currentTimeMillis

abstract class ControllerBase(
    private val traceAid: TraceAid,
    private val organisasjonsnummerProvider: OrganisasjonsnummerProvider?,
    private val tilknytningService: TilknytningService?
) {
    constructor(traceAid: TraceAid) : this(
        traceAid,
        organisasjonsnummerProvider = null,
        tilknytningService = null
    )

    private val log = KotlinLogging.logger {}

    protected fun <R> timed(function: () -> R, functionName: String): R {
        val startTimeMillis = currentTimeMillis()
        val result = function()
        log.info { "$functionName took ${currentTimeMillis() - startTimeMillis} ms to process" }
        return result
    }

    protected fun <A, R> timed(function: (A) -> R, argument: A, functionName: String): R {
        val startTimeMillis = currentTimeMillis()
        val result = function(argument)
        log.info { "$functionName took ${currentTimeMillis() - startTimeMillis} ms to process" }
        return result
    }

    protected fun <T> handle(e: EgressException) =
        if (e.isClientError) // "client" is here the backend server itself (calling other services)
            handleInternalError<T>(e)
        else
            handleExternalError<T>(e)

    protected fun <T> badRequest(e: RuntimeException): T? {
        val message = extractMessageRecursively(e)
        log.info { "Bad request - $message" } // no error, so stacktrace is not logged

        throw ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            "Call ID: ${traceAid.callId()} | Error: ${errorMessage()} | Details: $message",
            e
        )
    }

    protected fun countCall(functionName: String) {
        Metrics.countIngressCall(
            organisasjonId = organisasjonsnummerProvider?.let { Organisasjoner.navn(it.provideOrganisasjonsnummer()) }
                ?: "intern",
            callId = functionName
        )
    }

    protected fun verifiserAtBrukerTilknyttetTpLeverandoer(pid: Pid) {
        organisasjonsnummerProvider?.let {
            val organisasjonsnummer: Organisasjonsnummer = it.provideOrganisasjonsnummer()

            if (organisasjonsnummer != NAV_ORG_NUMMER &&
                tilknytningService?.erPersonTilknyttetTjenestepensjonsordning(pid, organisasjonsnummer) == false
            ) {
                log.warn { "Brukeren er ikke tilknyttet angitt TP-leverandør $organisasjonsnummer" }
                throw ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Call ID: ${traceAid.callId()} | Error: Brukeren er ikke tilknyttet angitt TP-leverandør"
                )
            }
        }
    }

    abstract fun errorMessage(): String

    private fun <T> handleInternalError(e: EgressException): Nothing {
        logError(e, "Intern")

        throw ResponseStatusException(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Call ID: ${traceAid.callId()} | Error: ${errorMessage()} | Details: ${extractMessageRecursively(e)}",
            e
        )
    }

    private fun <T> handleExternalError(e: EgressException): T? {
        logError(e, "Ekstern")
        return serviceUnavailable(e)
    }

    private fun <T> serviceUnavailable(e: EgressException): T? {
        throw ResponseStatusException(
            HttpStatus.SERVICE_UNAVAILABLE,
            "Call ID: ${traceAid.callId()} | Error: ${errorMessage()} | Details: ${extractMessageRecursively(e)}",
            e
        )
    }

    private fun logError(e: EgressException, category: String) {
        log.error { "$category ${errorMessage()} : ${extractMessageRecursively(e)}" }
    }

    protected companion object {
        val NAV_ORG_NUMMER = Organisasjonsnummer("889640782")

        @Language("json")
        const val SERVICE_UNAVAILABLE_EXAMPLE = """{
    "timestamp": "2023-09-12T10:37:47.056+00:00",
    "status": 503,
    "error": "Service Unavailable",
    "message": "En feil inntraff",
    "path": "/api/ressurs"
}"""

        fun extractMessageRecursively(e: Throwable): String =
            StringBuilder(e.message ?: e.javaClass.simpleName).apply {
                e.cause?.let { append(" | Cause: ").append(extractMessageRecursively(it)) }
            }.toString()
    }
}

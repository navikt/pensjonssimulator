package no.nav.pensjon.simulator.common.api

import mu.KotlinLogging
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.tech.metric.Metrics
import no.nav.pensjon.simulator.tech.metric.Organisasjoner
import no.nav.pensjon.simulator.tech.sporing.OrganisasjonsnummerProvider
import no.nav.pensjon.simulator.tech.trace.TraceAid
import no.nav.pensjon.simulator.tech.web.EgressException
import no.nav.pensjon.simulator.tpregisteret.TpregisteretService
import org.intellij.lang.annotations.Language
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.lang.System.currentTimeMillis

abstract class ControllerBase(
    private val traceAid: TraceAid,
    private val organisasjonsnummerProvider: OrganisasjonsnummerProvider?,
    private val tpregisteretService: TpregisteretService,
) {

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
        val orgNummer = organisasjonsnummerProvider.provideOrganisasjonsnummer().value
        if (!tpregisteretService.erBrukerTilknyttetAngittTpLeverandoer(pid.value, orgNummer)) {
            log.warn { "Brukeren er ikke tilknyttet angitt TP-leverandør $orgNummer" }
            //TODO uncomment to enforce policy
//            throw ResponseStatusException(
//                HttpStatus.FORBIDDEN,
//                "Call ID: ${traceAid.callId()} | Error: Brukeren er ikke tilknyttet angitt TP-leverandør"
//            )
        }
    }

    abstract fun errorMessage(): String

    private fun <T> handleInternalError(e: EgressException): T? {
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

    private fun extractMessageRecursively(ex: Throwable): String {
        val builder = StringBuilder()
        builder.append(ex.message)

        if (ex.cause == null) {
            return builder.toString()
        }

        builder.append(" | Cause: ").append(extractMessageRecursively(ex.cause!!))
        return builder.toString()
    }

    protected companion object {
        @Language("json")
        const val SERVICE_UNAVAILABLE_EXAMPLE = """{
    "timestamp": "2023-09-12T10:37:47.056+00:00",
    "status": 503,
    "error": "Service Unavailable",
    "message": "En feil inntraff",
    "path": "/api/ressurs"
}"""
    }
}

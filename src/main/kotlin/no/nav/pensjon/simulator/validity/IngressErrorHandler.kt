package no.nav.pensjon.simulator.validity

import tools.jackson.databind.exc.MismatchedInputException
import java.time.format.DateTimeParseException

object IngressErrorHandler {

    fun extractExceptionNames(e: Throwable): String =
        StringBuilder(e.javaClass.simpleName).apply {
            e.cause?.let { append(" | Cause: ").append(extractExceptionNames(it)) }
        }.toString()

    fun extractSafeMessage(e: Throwable): String =
        when {
            e is NullPointerException -> e.message?.let(::strip)
            safeExceptions.any { it.isInstance(e) } -> e.message
            else -> e.cause?.let(::extractSafeMessage)
        } ?: e.javaClass.simpleName

    /**
     * NB: Use with caution, since 'message' may contain sensitive information.
     * Should only be used for internal logging purposes, not for user-facing messages.
     */
    fun extractUnsafeMessages(e: Throwable): String =
        StringBuilder(e.message ?: e.javaClass.simpleName).apply {
            e.cause?.let { append(" | Cause: ").append(extractUnsafeMessages(it)) }
        }.toString()

    /**
     * Kotlin null-check NPEs include internal method/class names; strip down to just the parameter name.
     */
    private fun strip(message: String): String =
        Regex("""parameter ([A-Za-z0-9_]+)\b""")
            .find(message)
            ?.groupValues
            ?.getOrNull(1)
            ?.let { "Missing required parameter: $it" }
            ?: message

    /**
     * Exception types whose messages are considered never to contain sensitive information
     * and are therefore safe to expose to external clients.
     */
    private val safeExceptions = setOf(
        DateTimeParseException::class.java,
        MismatchedInputException::class.java,
        NullPointerException::class.java
    )
}
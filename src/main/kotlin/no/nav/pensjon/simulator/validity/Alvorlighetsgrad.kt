package no.nav.pensjon.simulator.validity

import mu.KotlinLogging
import no.nav.pensjon.simulator.validity.IngressErrorHandler.extractUnsafeMessages

/**
 * Representerer alvorlighetsgrader ved unntakssituasjoner.
 * Hver grad har et tilhørende loggnivå.
 * ------------------------------------------------------------
 * Stacktrace logges kun ved alvorlighetsgrad 'feil'.
 * For øvrige alvorlighetsgrader logges isteden alle exception messages.
 * NB: Exception messages anses som 'unsafe', siden de kan inneholde sensitiv informasjon.
 */
enum class Alvorlighetsgrad(private val logger: (Throwable, () -> Any?) -> Unit) {
    INFORMASJON(logger = { t, message -> log.info { "${message()} - ${extractUnsafeMessages(t)}" } }),
    ADVARSEL(logger = { t, message -> log.warn { "${message()} - ${extractUnsafeMessages(t)}" } }),
    FEIL(logger = { t, message -> log.error(t) { message() } });

    fun log(t: Throwable, message: () -> Any?) {
        logger.invoke(t, message)
    }

    companion object {
        private val log = KotlinLogging.logger {}
    }
}
package no.nav.pensjon.simulator.tech.metric

import no.nav.pensjon.simulator.tech.sporing.Organisasjonsnummer

object Organisasjoner {

    private val organisasjonNavnForNummer =
        mapOf(
            "889640782" to "NAV",
            "938708606" to "KLP",
            "982583462" to "SPK",
            "931936492" to "Stb"
        )

    fun navn(organisasjonsnummer: Organisasjonsnummer): String =
        organisasjonNavnForNummer[organisasjonsnummer.value] ?: "?$organisasjonsnummer"
}

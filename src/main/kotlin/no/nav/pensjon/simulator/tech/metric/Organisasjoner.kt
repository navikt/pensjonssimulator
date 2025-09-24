package no.nav.pensjon.simulator.tech.metric

import no.nav.pensjon.simulator.generelt.organisasjon.Organisasjonsnummer

object Organisasjoner {

    private val organisasjonNavnForNummer =
        mapOf(
            "889640782" to "Nav",
            "927613298" to "Aks", // Aksio
            "940380014" to "Are", // Arendal kommunale pensjonskasse
            "980650383" to "Dra", // Drammen kommunale pensjonskasse
            "916833520" to "Gab", // Gabler Pensjonstjenester
            "938708606" to "KLP", // Kommunal Landspensjonskasse
            "982759412" to "OPF", // Oslo Pensjonsforsikring
            "982583462" to "SPK", // Statens pensjonskasse
            "931936492" to "Stb"  // Storebrand Pensjonstjenester
        )

    fun navn(organisasjonsnummer: Organisasjonsnummer): String =
        organisasjonNavnForNummer[organisasjonsnummer.value] ?: "?$organisasjonsnummer"

    val SPK: Organisasjonsnummer = organisasjonNavnForNummer
        .entries.first { it.value.equals("SPK", ignoreCase = true) }
        .let { Organisasjonsnummer(it.key) }
}

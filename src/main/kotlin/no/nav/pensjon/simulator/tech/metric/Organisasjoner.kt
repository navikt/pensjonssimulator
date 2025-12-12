package no.nav.pensjon.simulator.tech.metric

import no.nav.pensjon.simulator.generelt.organisasjon.Organisasjonsnummer

object Organisasjoner {

    val nav = Organisasjonsnummer("889640782")
    val KLP = Organisasjonsnummer("938708606")
    val SPK = Organisasjonsnummer("982583462")

    private val organisasjonNavnForNummer =
        mapOf(
            nav.value to "Nav",
            "927613298" to "Aks", // Aksio
            "940380014" to "Are", // Arendal kommunale pensjonskasse
            "980650383" to "Dra", // Drammen kommunale pensjonskasse
            "916833520" to "Gab", // Gabler Pensjonstjenester
            KLP.value to "KLP", // Kommunal Landspensjonskasse
            "982759412" to "OPF", // Oslo Pensjonsforsikring
            SPK.value to "SPK", // Statens pensjonskasse
            "958995369" to "StL", // Storebrand Livsforsikring
            "931936492" to "StP"  // Storebrand Pensjonstjenester
        )

    fun navn(organisasjonsnummer: Organisasjonsnummer): String =
        organisasjonNavnForNummer[organisasjonsnummer.value] ?: "?$organisasjonsnummer"
}

package no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.spk.dto

enum class SPKYtelse {
    PAASLAG, APOF2020, OT6370, SAERALDERSPAASLAG, OAFP, BTP;

    companion object {
        fun hentAlleUnntattType(vararg typer: SPKYtelse): List<String> {
            return entries.filter { it !in typer }.map { it.name }
        }
    }
}
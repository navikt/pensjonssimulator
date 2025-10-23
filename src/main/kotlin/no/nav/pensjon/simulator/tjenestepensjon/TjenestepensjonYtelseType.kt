package no.nav.pensjon.simulator.tjenestepensjon

enum class TjenestepensjonYtelseType(val kode: String, val ekskludert: Boolean = false) {
    ALDERSPENSJON_OPPTJENT_FOER_2020(kode = "APOF2020"),
    BETINGET_TJENESTEPENSJON(kode = "BTP", ekskludert = true),
    OFFENTLIG_AFP(kode = "OAFP", ekskludert = true),
    OVERGANGSTILLEGG(kode = "OT6370"),
    PAASLAG(kode = "PAASLAG"),
    SAERALDERSPAASLAG(kode = "SAERALDERSPAASLAG")
}

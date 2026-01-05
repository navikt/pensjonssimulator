package no.nav.pensjon.simulator.tjenestepensjon

enum class TjenestepensjonYtelseType(
    val kode: String,
    val erEkskludertForKlp: Boolean = false,
    val erEkskludertForSpk: Boolean = false
) {
    ALLE(kode = "ALLE"),
    ALDERSPENSJON_OPPTJENT_FOER_2020(kode = "APOF2020"),
    BETINGET_TJENESTEPENSJON(kode = "BTP", erEkskludertForKlp = true),
    OFFENTLIG_AFP(kode = "OAFP", erEkskludertForKlp = true, erEkskludertForSpk = true),
    OVERGANGSTILLEGG(kode = "OT6370"),
    PAASLAG(kode = "PAASLAG"),
    SAERALDERSPAASLAG(kode = "SAERALDERSPAASLAG")
}

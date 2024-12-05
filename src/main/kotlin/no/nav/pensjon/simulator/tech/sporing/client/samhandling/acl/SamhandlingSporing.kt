package no.nav.pensjon.simulator.tech.sporing.client.samhandling.acl

data class SamhandlingSporing(
    val person: String,
    val mottaker: String,
    val tema: String,
    val behandlingsGrunnlag: String,
    val uthentingsTidspunkt: String,
    val dataForespoersel: String,
    val leverteData: String
)

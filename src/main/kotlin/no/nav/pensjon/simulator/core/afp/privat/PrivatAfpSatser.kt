package no.nav.pensjon.simulator.core.afp.privat

/**
 * PEN: no.nav.service.pensjon.fpen.HentSatserAFPPrivatResponse
 */
data class PrivatAfpSatser(
    val forholdstall: Double = 0.0,
    val kompensasjonstilleggForholdstall: Double = 0.0,
    val justeringsbeloep: Int = 0,
    val referansebeloep: Int = 0
)

package no.nav.pensjon.simulator.afp.offentlig.tidsbegrenset

import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.domain.regler.simulering.Simuleringsresultat

data class TidsbegrensetOffentligAfpResult(
    val simuleringsresultat: Simuleringsresultat? = null,
    val registrerteFremtidigePensjonspoeng: Double? = null,
    val kravhode: Kravhode // kravhode med modifisert AFP-historikk
)
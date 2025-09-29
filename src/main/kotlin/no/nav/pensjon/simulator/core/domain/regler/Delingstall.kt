import no.nav.pensjon.simulator.core.domain.regler.Alder

data class Delingstall(
    var alder: Alder = Alder(0,0),
    var delingstall: Double = 0.0,
)

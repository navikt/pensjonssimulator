package no.nav.pensjon.simulator.core.result

import no.nav.pensjon.simulator.core.afp.privat.SimulertPrivatAfpPeriode
import no.nav.pensjon.simulator.core.out.OutputLivsvarigOffentligAfp
import no.nav.pensjon.simulator.core.domain.SivilstandType
import no.nav.pensjon.simulator.core.domain.regler.kode.SivilstandTypeCti
import no.nav.pensjon.simulator.core.domain.regler.simulering.Simuleringsresultat

// no.nav.domain.pensjon.kjerne.simulering.SimuleringEtter2011Resultat
class SimulatorOutput {
    //TODO data class
    var alderspensjon: SimulertAlderspensjon? = null
    var pre2025OffentligAfp: Simuleringsresultat? = null
    var livsvarigOffentligAfp: List<OutputLivsvarigOffentligAfp>? = null
    var sisteGyldigeOpptjeningAar: Int = 0
    var sivilstand = SivilstandTypeCti(SivilstandType.NULL.name)
    var grunnbeloep: Int = 0
    var epsHarPensjon: Boolean = false
    var epsHarInntektOver2G: Boolean = false
    val privatAfpPeriodeListe: MutableList<SimulertPrivatAfpPeriode> = mutableListOf()
    val opptjeningListe: MutableList<SimulertOpptjening> = mutableListOf()
}

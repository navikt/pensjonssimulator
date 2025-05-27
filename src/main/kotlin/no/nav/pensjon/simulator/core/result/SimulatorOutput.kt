package no.nav.pensjon.simulator.core.result

import no.nav.pensjon.simulator.core.afp.privat.SimulertPrivatAfpPeriode
import no.nav.pensjon.simulator.core.domain.regler.enum.SivilstandEnum
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Persongrunnlag
import no.nav.pensjon.simulator.core.domain.regler.simulering.Simuleringsresultat
import no.nav.pensjon.simulator.afp.offentlig.livsvarig.LivsvarigOffentligAfpOutput
import java.time.LocalDate

// no.nav.domain.pensjon.kjerne.simulering.SimuleringEtter2011Resultat
class SimulatorOutput {
    //TODO data class
    var alderspensjon: SimulertAlderspensjon? = null
    var pre2025OffentligAfp: Simuleringsresultat? = null
    var livsvarigOffentligAfp: List<LivsvarigOffentligAfpOutput>? = null
    var sisteGyldigeOpptjeningAar: Int = 0
    var sivilstand: SivilstandEnum = SivilstandEnum.NULL
    var grunnbeloep: Int = 0
    var epsHarPensjon: Boolean = false
    var epsHarInntektOver2G: Boolean = false
    val privatAfpPeriodeListe: MutableList<SimulertPrivatAfpPeriode> = mutableListOf()
    val opptjeningListe: MutableList<SimulertOpptjening> = mutableListOf()

    var foedselDato: LocalDate? = null
    var persongrunnlag: Persongrunnlag? = null

    var heltUttakDato: LocalDate? = null
}

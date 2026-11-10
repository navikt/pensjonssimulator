package no.nav.pensjon.simulator.core.result

import no.nav.pensjon.simulator.afp.privat.PrivatAfpPeriode
import no.nav.pensjon.simulator.core.domain.regler.enum.SivilstandEnum
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Persongrunnlag
import no.nav.pensjon.simulator.core.domain.regler.simulering.Simuleringsresultat
import no.nav.pensjon.simulator.afp.offentlig.livsvarig.LivsvarigOffentligAfpOutput
import java.time.LocalDate

data class SimulatorOutput(
    val alderspensjon: SimulertAlderspensjon? = null,
    val tidsbegrensetOffentligAfp: Simuleringsresultat? = null,
    val livsvarigOffentligAfp: List<LivsvarigOffentligAfpOutput>? = null,
    val sivilstand: SivilstandEnum = SivilstandEnum.NULL,
    val epsHarPensjon: Boolean = false,
    val epsHarInntektOver2G: Boolean = false,
    val privatAfpPeriodeListe: MutableList<PrivatAfpPeriode> = mutableListOf(),
    val opptjeningListe: MutableList<SimulertOpptjening> = mutableListOf(),
    val persongrunnlag: Persongrunnlag? = null,
    val heltUttakDato: LocalDate? = null,
    val registerData: RegisterData? = null
) {
    fun med(alderspensjon: SimulertAlderspensjon?): SimulatorOutput =
        copy(alderspensjon = alderspensjon)

    fun med(persongrunnlag: Persongrunnlag, heltUttakFom: LocalDate?): SimulatorOutput =
        copy(
            persongrunnlag = persongrunnlag,
            heltUttakDato = heltUttakFom
        )

    fun med(
        tidsbegrensetOffentligAfp: Simuleringsresultat?,
        livsvarigOffentligAfpListe: List<LivsvarigOffentligAfpOutput>?,
        registerData: RegisterData
    ): SimulatorOutput =
        copy(
            tidsbegrensetOffentligAfp = tidsbegrensetOffentligAfp,
            livsvarigOffentligAfp = livsvarigOffentligAfpListe,
            registerData = registerData
        )
}
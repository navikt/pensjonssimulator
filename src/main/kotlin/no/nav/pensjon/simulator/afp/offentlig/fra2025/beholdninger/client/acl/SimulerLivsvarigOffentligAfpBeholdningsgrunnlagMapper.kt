package no.nav.pensjon.simulator.afp.offentlig.fra2025.beholdninger.client.acl

import no.nav.pensjon.simulator.afp.offentlig.fra2025.LivsvarigOffentligAfpSpec
import no.nav.pensjon.simulator.afp.offentlig.fra2025.beholdninger.SimulerLivsvarigOffentligAfpBeholdningsperiode
import no.nav.pensjon.simulator.inntekt.Inntekt

object SimulerLivsvarigOffentligAfpBeholdningsgrunnlagMapper {

    fun toDto(spec: LivsvarigOffentligAfpSpec) =
        SimulerLivsvarigOffentligAfpBeholdningsgrunnlagSpec(
            personId = spec.pid.value,
            uttaksDato = spec.fom,
            fremtidigInntektListe = spec.fremtidigInntektListe.map(::inntektsperiode)
        )

    fun fromDto(
        result: SimulerLivsvarigOffentligAfpBeholdningsgrunnlagResult
    ): List<SimulerLivsvarigOffentligAfpBeholdningsperiode> =
        result.afpBeholdningsgrunnlag.map {
            SimulerLivsvarigOffentligAfpBeholdningsperiode(pensjonsbeholdning = it.belop, fom = it.fraOgMedDato)
        }

    private fun inntektsperiode(inntekt: Inntekt) =
        Inntektsperiode(
            fraOgMedDato = inntekt.fom,
            arligInntekt = inntekt.aarligBeloep
        )
}
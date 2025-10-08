package no.nav.pensjon.simulator.afp.offentlig.fra2025.beholdninger.client.acl

import no.nav.pensjon.simulator.afp.offentlig.fra2025.LivsvarigOffentligAfpSpec
import no.nav.pensjon.simulator.afp.offentlig.fra2025.beholdninger.SimulerLivsvarigOffentligAfpBeholdningsperiode

object SimulerLivsvarigOffentligAfpBeholdningsgrunnlagMapper {

    fun toDto(spec: LivsvarigOffentligAfpSpec) : SimulerLivsvarigOffentligAfpBeholdningsgrunnlagSpec =
        SimulerLivsvarigOffentligAfpBeholdningsgrunnlagSpec(
            spec.pid,
            spec.fom,
            spec.fremtidigInntektListe.map { Inntektsperiode(it.fom, it.aarligBeloep) })


    fun fromDto(response: SimulerLivsvarigOffentligAfpBeholdningsgrunnlagResult): List<SimulerLivsvarigOffentligAfpBeholdningsperiode> =
        response.afpBeholdningsgrunnlag.map {
            SimulerLivsvarigOffentligAfpBeholdningsperiode(it.belop, it.fraOgMedDato)
        }
}
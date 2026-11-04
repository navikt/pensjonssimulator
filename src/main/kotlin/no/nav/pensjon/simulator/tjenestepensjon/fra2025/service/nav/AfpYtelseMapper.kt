package no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.nav

import no.nav.pensjon.simulator.afp.offentlig.livsvarig.LivsvarigOffentligAfpSpec
import no.nav.pensjon.simulator.inntekt.LoependeInntekt
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.OffentligTjenestepensjonFra2025SimuleringSpec

object AfpYtelseMapper {
    fun toLivsvarigOffentligAfpSpec(request: OffentligTjenestepensjonFra2025SimuleringSpec): LivsvarigOffentligAfpSpec =
        LivsvarigOffentligAfpSpec(
            pid = request.pid,
            foedselsdato = request.foedselsdato,
            fom = request.uttaksdato,
            fremtidigInntektListe = request.fremtidigeInntekter.map {
                LoependeInntekt(
                    aarligBeloep = it.aarligInntekt,
                    fom = it.fom
                )
            }
        )
}
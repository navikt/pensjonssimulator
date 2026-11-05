package no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.nav

import no.nav.pensjon.simulator.afp.offentlig.livsvarig.LivsvarigOffentligAfpSpec
import no.nav.pensjon.simulator.core.krav.FremtidigInntekt
import no.nav.pensjon.simulator.inntekt.LoependeInntekt
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.OffentligTjenestepensjonFra2025SimuleringSpec

object AfpYtelseMapper {

    fun toLivsvarigOffentligAfpSpec(
        spec: OffentligTjenestepensjonFra2025SimuleringSpec,
        inntekterMapper: (spec: OffentligTjenestepensjonFra2025SimuleringSpec) -> List<FremtidigInntekt>
    ) =

        LivsvarigOffentligAfpSpec(
            pid = spec.pid,
            foedselsdato = spec.foedselsdato,
            fom = spec.uttaksdato,
            fremtidigInntektListe = inntekterMapper(spec)
        )
}
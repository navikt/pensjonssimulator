package no.nav.pensjon.simulator.tjenestepensjon.fra2025.api.acl.v1

import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.OffentligTjenestepensjonFra2025SimuleringSpec
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.TjenestepensjonInntektSpec

object OffentligTjenestepensjonFra2025SimuleringSpecMapperV1 {

    fun fromDto(source: SimulerOffentligTjenestepensjonFra2025SpecV1) =
        OffentligTjenestepensjonFra2025SimuleringSpec(
            pid = Pid(source.pid),
            foedselsdato = source.foedselsdato,
            uttaksdato = source.uttaksdato,
            sisteInntekt = source.sisteInntekt,
            utlandAntallAar = source.aarIUtlandetEtter16,
            afpErForespurt = source.brukerBaOmAfp,
            epsHarPensjon = source.epsPensjon,
            epsHarInntektOver2G = source.eps2G,
            fremtidigeInntekter = source.fremtidigeInntekter.map(::inntekt),
            gjelderApoteker = source.erApoteker
        )

    private fun inntekt(source: SimulerTjenestepensjonFremtidigInntektDto) =
        TjenestepensjonInntektSpec(
            fom = source.fraOgMed,
            aarligInntekt = source.aarligInntekt
        )
}
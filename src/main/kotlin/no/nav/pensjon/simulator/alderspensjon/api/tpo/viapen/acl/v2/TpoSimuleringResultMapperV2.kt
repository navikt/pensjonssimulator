package no.nav.pensjon.simulator.alderspensjon.api.tpo.viapen.acl.v2

import no.nav.pensjon.simulator.core.beregn.BeholdningPeriode
import no.nav.pensjon.simulator.core.result.PensjonPeriode
import no.nav.pensjon.simulator.core.result.SimulatorOutput
import no.nav.pensjon.simulator.core.result.SimulertAlderspensjon
import no.nav.pensjon.simulator.core.result.SimulertBeregningInformasjon

object TpoSimuleringResultMapperV2 {

    fun toDto(source: SimulatorOutput) =
        TpoSimuleringResultV2(
            ap = source.alderspensjon?.let(TpoSimuleringResultMapperV2::alderspensjon)
            // afpPrivat: V1, V3 only
            // sisteGyldigeOpptjeningsAr: V3 only
        )

    private fun alderspensjon(source: SimulertAlderspensjon) =
        TpoAlderspensjonV2(
            pensjonsperiodeListe = source.pensjonPeriodeListe.map(TpoSimuleringResultMapperV2::pensjonPeriode),
            pensjonsbeholdningListe = source.pensjonBeholdningListe.map(TpoSimuleringResultMapperV2::beholdningPeriode)
            // uttaksgradListe & simulertBeregningsinformasjonListe: V3 only
        )

    private fun pensjonPeriode(source: PensjonPeriode) =
        TpoPensjonPeriodeV2(
            alder = source.alderAar,
            belop = source.beloep,
            simulertBeregningsinformasjonListe = source.simulertBeregningInformasjonListe.map(TpoSimuleringResultMapperV2::beregningInformasjon)
        )

    private fun beholdningPeriode(source: BeholdningPeriode) =
        TpoPensjonBeholdningPeriodeV2(
            datoFom = source.datoFom,
            pensjonsbeholdning = source.pensjonsbeholdning,
            garantipensjonsbeholdning = source.garantipensjonsbeholdning,
            garantitilleggsbeholdning = source.garantitilleggsbeholdning
            // garantipensjonsniva: V3 only
        )

    private fun beregningInformasjon(source: SimulertBeregningInformasjon) =
        TpoBeregningInformasjonV2(
            startMnd = source.startMaaned, // V1, V2 only
            uttaksgrad = source.uttakGrad
            // datoFom, delytelser, simuleringsdata: V3 only
        )
}

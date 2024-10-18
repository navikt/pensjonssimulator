package no.nav.pensjon.simulator.alderspensjon.tpo.api.acl.v1out

import no.nav.pensjon.simulator.core.afp.privat.SimulertPrivatAfpPeriode
import no.nav.pensjon.simulator.core.result.PensjonPeriode
import no.nav.pensjon.simulator.core.result.SimulatorOutput
import no.nav.pensjon.simulator.core.result.SimulertAlderspensjon
import no.nav.pensjon.simulator.core.result.SimulertBeregningInformasjon

object TpoSimuleringResultMapperV1 {

    fun toDto(source: SimulatorOutput) =
        TpoSimuleringResultV1(
            ap = source.alderspensjon?.let(::alderspensjon),
            afpPrivat = source.privatAfpPeriodeListe.map(::privatAfpPeriode) // V1, V3 only
            // sisteGyldigeOpptjeningsAr: V3 only
        )

    private fun alderspensjon(source: SimulertAlderspensjon) =
        TpoAlderspensjonV1(
            pensjonsperiodeListe = source.pensjonPeriodeListe.map(::pensjonPeriode)
            // pensjonsbeholdningListe: V2, V3 only
            // uttaksgradListe: V3 only
            // simulertBeregningsinformasjonListe: V3 only
        )

    private fun privatAfpPeriode(source: SimulertPrivatAfpPeriode) =
        TpoPrivatAfpPeriodeV1(
            alder = source.alderAar, // V1 only
            belopArlig = source.aarligBeloep // V1 only
        )

    private fun pensjonPeriode(source: PensjonPeriode) =
        TpoPensjonPeriodeV1(
            alder = source.alderAar,
            belop = source.beloep,
            simulertBeregningsinformasjonListe = source.simulertBeregningInformasjonListe.map(::beregningInformasjon)
        )

    private fun beregningInformasjon(source: SimulertBeregningInformasjon) =
        TpoBeregningInformasjonV1(
            startMnd = source.startMaaned, // V1, V2 only
            uttaksgrad = source.uttakGrad,
            // datoFom, delytelser, simuleringsdata: V3 only
        )
}

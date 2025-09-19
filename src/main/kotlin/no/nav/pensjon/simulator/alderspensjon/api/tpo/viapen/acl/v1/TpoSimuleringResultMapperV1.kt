package no.nav.pensjon.simulator.alderspensjon.api.tpo.viapen.acl.v1

import no.nav.pensjon.simulator.afp.privat.PrivatAfpPeriode
import no.nav.pensjon.simulator.core.result.PensjonPeriode
import no.nav.pensjon.simulator.core.result.SimulatorOutput
import no.nav.pensjon.simulator.core.result.SimulertAlderspensjon
import no.nav.pensjon.simulator.core.result.SimulertBeregningInformasjon

/**
 * Anti-corruption layer (ACL).
 * Maps simuleringsresultat from domain to DTO for 'TPO V1' service.
 */
object TpoSimuleringResultMapperV1 {

    fun toDto(source: SimulatorOutput) =
        TpoSimuleringResultV1(
            ap = source.alderspensjon?.let(TpoSimuleringResultMapperV1::alderspensjon),
            afpPrivat = source.privatAfpPeriodeListe.map(TpoSimuleringResultMapperV1::privatAfpPeriode) // V1, V3 only
            // sisteGyldigeOpptjeningsAr: V3 only
        )

    private fun alderspensjon(source: SimulertAlderspensjon) =
        TpoAlderspensjonV1(
            pensjonsperiodeListe = source.pensjonPeriodeListe.map(TpoSimuleringResultMapperV1::pensjonPeriode)
            // pensjonsbeholdningListe: V3 only
            // uttaksgradListe: V3 only
            // simulertBeregningsinformasjonListe: V3 only
        )

    private fun privatAfpPeriode(source: PrivatAfpPeriode) =
        TpoPrivatAfpPeriodeV1(
            alder = source.alderAar, // V1 only
            belopArlig = source.aarligBeloep // V1 only
        )

    private fun pensjonPeriode(source: PensjonPeriode) =
        TpoPensjonPeriodeV1(
            alder = source.alderAar,
            belop = source.beloep,
            simulertBeregningsinformasjonListe = source.simulertBeregningInformasjonListe.map(
                TpoSimuleringResultMapperV1::beregningInformasjon
            )
        )

    private fun beregningInformasjon(source: SimulertBeregningInformasjon) =
        TpoBeregningInformasjonV1(
            startMnd = source.startMaaned, // V1 only
            uttaksgrad = source.uttakGrad,
            // datoFom, delytelser, simuleringsdata: V3 only
        )
}

package no.nav.pensjon.simulator.alderspensjon.api.tpo.viapen.acl.v2

import no.nav.pensjon.simulator.core.beregn.BeholdningPeriode
import no.nav.pensjon.simulator.core.result.PensjonPeriode
import no.nav.pensjon.simulator.core.result.SimulatorOutput
import no.nav.pensjon.simulator.core.result.SimulertAlderspensjon
import no.nav.pensjon.simulator.core.result.SimulertBeregningInformasjon

object TpoSimuleringResultMapperV2 {

    fun toDto(source: SimulatorOutput) =
        TpoSimuleringResultV2(
            alderspensjon = source.alderspensjon?.let(::alderspensjon)
            // afpPrivat: V1, V3 only
            // sisteGyldigeOpptjeningsAr: V3 only
        )

    private fun alderspensjon(source: SimulertAlderspensjon) =
        TpoAlderspensjonV2(
            pensjonsperiodeListe = source.pensjonPeriodeListe.map(::pensjonPeriode),
            pensjonsbeholdningListe = source.pensjonBeholdningListe.map(::beholdningPeriode)
            // uttaksgradListe & simulertBeregningsinformasjonListe: V3 only
        )

    private fun pensjonPeriode(source: PensjonPeriode) =
        TpoPensjonPeriodeV2(
            alderAar = source.alderAar,
            aarligBeloep = source.beloep,
            beregningInformasjonListe = source.simulertBeregningInformasjonListe.map(::beregningInformasjon)
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
            poengAarFoer1992 = source.pa_f92,
            poengAarEtter1991 = source.pa_e91,
            sluttpoengtall = source.spt,
            kapittel19AnvendtTrygdetid = source.tt_anv_kap19,
            basisGrunnpensjon = source.basisGrunnpensjon,
            basisTilleggspensjon = source.basisTilleggspensjon,
            basisPensjonstillegg = source.basisPensjonstillegg,
            forholdstall = source.forholdstall,
            skjermingstillegg = source.skjermingstillegg,
            ufoeregrad = source.ufoereGrad,
            inntektspensjon = source.inntektspensjon,
            garantipensjon = source.garantipensjon,
            garantitillegg = source.garantitillegg,
            delingstall = source.delingstall,
            startMaaned = source.startMaaned, // V1, V2 only
            uttaksgrad = source.uttakGrad
            // datoFom, delytelser, simuleringsdata: V3 only
        )
}

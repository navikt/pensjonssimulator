package no.nav.pensjon.simulator.alderspensjon.api.nav.viapen.acl.v2.result

import no.nav.pensjon.simulator.core.afp.privat.SimulertPrivatAfpPeriode
import no.nav.pensjon.simulator.core.beregn.BeholdningPeriode
import no.nav.pensjon.simulator.core.domain.SivilstandType
import no.nav.pensjon.simulator.core.domain.regler.beregning.Beregning
import no.nav.pensjon.simulator.core.domain.regler.beregning.Poengrekke
import no.nav.pensjon.simulator.core.domain.regler.beregning.Sluttpoengtall
import no.nav.pensjon.simulator.core.domain.regler.beregning.Tilleggspensjon
import no.nav.pensjon.simulator.core.domain.regler.simulering.Simuleringsresultat
import no.nav.pensjon.simulator.core.result.SimulatorOutput
import no.nav.pensjon.simulator.core.result.SimulertAlderspensjon
import no.nav.pensjon.simulator.core.result.SimulertBeregningInformasjon
import no.nav.pensjon.simulator.core.util.DateNoonExtension.noon
import no.nav.pensjon.simulator.core.util.toDate
import no.nav.pensjon.simulator.core.util.toNorwegianDate

/**
 * Maps result of 'AP for TP' (alderspensjon for tjenestepensjon-simulering)
 * from domain objects to DTOs version 2.
 */
object ApForTpResultMapperV2 {

    fun toApForTpResultV2(source: SimulatorOutput) =
        ApForTpResultV2(
            ap = source.alderspensjon?.let(::alderspensjon),
            afpPrivat = source.privatAfpPeriodeListe.map(::privatAfpPeriode),
            afpOffentlig = source.pre2025OffentligAfp?.let(::simuleringResultat),
            sivilstand = SivilstandType.valueOf(source.sivilstand.name)
        )

    private fun alderspensjon(source: SimulertAlderspensjon) =
        ApForTpAlderspensjonV2(
            pensjonsbeholdningListe = source.pensjonBeholdningListe.map(::beholdningPeriode),
            simulertBeregningsinformasjonListe = source.simulertBeregningInformasjonListe.map(::beregningInformasjon),
        )

    private fun privatAfpPeriode(source: SimulertPrivatAfpPeriode) =
        ApForTpPrivatAfpPeriodeV2(
            afpOpptjening = source.afpOpptjening,
            alder = source.alderAar,
            komptillegg = source.kompensasjonstillegg
        )

    private fun simuleringResultat(source: Simuleringsresultat) =
        ApForTpSimuleringResultatV2(
            beregning = source.beregning?.let(::beregning)
        )

    private fun beholdningPeriode(source: BeholdningPeriode) =
        ApForTpBeholdningPeriodeV2(
            datoFom = source.datoFom.toDate().noon(),
            pensjonsbeholdning = source.pensjonsbeholdning,
            garantipensjonsbeholdning = source.garantipensjonsbeholdning,
            garantitilleggsbeholdning = source.garantitilleggsbeholdning
        )

    private fun beregningInformasjon(source: SimulertBeregningInformasjon) =
        ApForTpBeregningInformasjonV2(
            datoFom = source.datoFom?.toDate()?.toNorwegianDate(),
            basisgp = source.basisGrunnpensjon,
            basistp = source.basisTilleggspensjon,
            basispt = source.basisPensjonstillegg,
            ufg = source.ufoereGrad,
            forholdstall = source.forholdstall,
            delingstall = source.delingstall,
            tt_anv_kap19 = source.tt_anv_kap19,
            pa_f92 = source.pa_f92,
            pa_e91 = source.pa_e91,
            spt = source.spt
        )

    private fun beregning(source: Beregning) =
        ApForTpBeregningV2(
            brutto = source.brutto,
            tilleggspensjonListe = source.tp?.let(::tilleggspensjon)?.let(::listOf) //TODO check list
        )

    private fun tilleggspensjon(source: Tilleggspensjon) =
        ApForTpTilleggspensjonV2(
            spt = source.spt?.let(::sluttpoengtall)
        )

    private fun sluttpoengtall(source: Sluttpoengtall) =
        ApForTpSluttpoengtallV2(
            poengrekke = source.poengrekke?.let(::poengrekke)!! //TODO null-check
        )

    private fun poengrekke(source: Poengrekke) =
        ApForTpPoengrekkeV2(
            tpi = source.tpi
        )
}

package no.nav.pensjon.simulator.core.anonym

import no.nav.pensjon.simulator.core.out.OutputLivsvarigOffentligAfp
import no.nav.pensjon.simulator.core.result.PensjonPeriode
import no.nav.pensjon.simulator.core.result.SimulatorOutput
import no.nav.pensjon.simulator.core.result.SimulertBeregningInformasjon
import no.nav.pensjon.simulator.core.afp.privat.SimulertPrivatAfpPeriode

object AnonymOutputMapper {

    /**
     * Maps simulator output to format suitable for uinnlogget/anonym kalkulator
     */
    fun mapSimuleringResult(source: SimulatorOutput) =
        AnonymSimuleringResult(
            alderspensjonKapittel19Andel = source.alderspensjon?.kapittel19Andel ?: 0.0,
            alderspensjonKapittel20Andel = source.alderspensjon?.kapittel20Andel ?: 0.0,
            alderspensjonPeriodeListe = source.alderspensjon?.pensjonPeriodeListe.orEmpty().map(AnonymOutputMapper::pensjonPeriode),
            privatAfpPeriodeListe = source.privatAfpPeriodeListe.map(AnonymOutputMapper::privatAfpPeriode),
            offentligAfpPeriodeListe = source.livsvarigOffentligAfp.orEmpty().map(AnonymOutputMapper::livsvarigOffentligAfp)
        )

    private fun pensjonPeriode(source: PensjonPeriode) =
        AnonymPensjonsperiode(
            beloep = source.beloep,
            alderAar = source.alderAar,
            simulertBeregningInformasjon = source.simulertBeregningInformasjonListe.firstOrNull()
                ?.let(AnonymOutputMapper::beregningInformasjon)
        )

    //private fun beregningInformasjon(source: LegacySimulertBeregningInformasjon) =
    private fun beregningInformasjon(source: SimulertBeregningInformasjon) =
        AnonymSimulertBeregningInformasjon(
            spt = source.spt,
            gp = source.grunnpensjon,
            tp = source.tilleggspensjon,
            ttAnvKap19 = source.tt_anv_kap19,
            ttAnvKap20 = source.tt_anv_kap20,
            paE91 = source.pa_e91,
            paF92 = source.pa_f92,
            forholdstall = source.forholdstall,
            delingstall = source.delingstall,
            pensjonBeholdningEtterUttak = source.pensjonBeholdningEtterUttak,
            inntektPensjon = source.inntektspensjon,
            garantipensjon = source.garantipensjon,
        )

    //private fun privatAfpPeriode(source: LegacySimulertPrivatAfpPeriode) =
    private fun privatAfpPeriode(source: SimulertPrivatAfpPeriode) =
        AnonymSimulertPrivatAfpPeriode(
            alderAar = source.alderAar,
            aarligBeloep = source.aarligBeloep,
            maanedligBeloep = source.maanedligBeloep
        )

    private fun livsvarigOffentligAfp(source: OutputLivsvarigOffentligAfp) =
        AnonymSimulertOffentligAfpPeriode(
            alderAar = source.alderAar,
            aarligBeloep = source.beloep,
            maanedligBeloep = source.beloep //TODO divide by 12?
        )
}

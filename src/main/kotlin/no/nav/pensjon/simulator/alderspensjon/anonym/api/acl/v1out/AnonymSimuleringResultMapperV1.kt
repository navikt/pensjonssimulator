package no.nav.pensjon.simulator.alderspensjon.anonym.api.acl.v1out

import no.nav.pensjon.simulator.core.afp.privat.SimulertPrivatAfpPeriode
import no.nav.pensjon.simulator.core.out.OutputLivsvarigOffentligAfp
import no.nav.pensjon.simulator.core.result.PensjonPeriode
import no.nav.pensjon.simulator.core.result.SimulatorOutput
import no.nav.pensjon.simulator.core.result.SimulertBeregningInformasjon

/**
 * Maps between data transfer objects (DTOs) and domain objects related to simulering.
 * The DTOs are specified by version 1 of the API offered to clients.
 */
object AnonymSimuleringResultMapperV1 {

    /**
     * Maps simulator output to format suitable for uinnlogget/anonym kalkulator
     */
    fun mapSimuleringResult(source: SimulatorOutput) =
        AnonymSimuleringResultV1(
            alderspensjonAndelKapittel19 = source.alderspensjon?.kapittel19Andel ?: 0.0,
            alderspensjonAndelKapittel20 = source.alderspensjon?.kapittel20Andel ?: 0.0,
            alderspensjonPerioder = source.alderspensjon?.pensjonPeriodeListe.orEmpty().map(AnonymSimuleringResultMapperV1::pensjonPeriode),
            afpPrivatPerioder = source.privatAfpPeriodeListe.map(AnonymSimuleringResultMapperV1::privatAfpPeriode),
            afpOffentligPerioder = source.livsvarigOffentligAfp.orEmpty().map(AnonymSimuleringResultMapperV1::livsvarigOffentligAfp)
        )

    private fun pensjonPeriode(source: PensjonPeriode) =
        AnonymPensjonPeriodeV1(
            belop = source.beloep,
            alder = source.alderAar,
            simulertBeregningsinformasjon = source.simulertBeregningInformasjonListe.firstOrNull()
                ?.let(AnonymSimuleringResultMapperV1::beregningInformasjon)
        )

    //private fun beregningInformasjon(source: LegacySimulertBeregningInformasjon) =
    private fun beregningInformasjon(source: SimulertBeregningInformasjon) =
        AnonymSimulertBeregningInformasjonV1(
            spt = source.spt,
            gp = source.grunnpensjon,
            tp = source.tilleggspensjon,
            ttAnvKap19 = source.tt_anv_kap19,
            ttAnvKap20 = source.tt_anv_kap20,
            paE91 = source.pa_e91,
            paF92 = source.pa_f92,
            forholdstall = source.forholdstall,
            delingstall = source.delingstall,
            pensjonsbeholdningEtterUttak = source.pensjonBeholdningEtterUttak,
            inntektspensjon = source.inntektspensjon,
            garantipensjon = source.garantipensjon,
        )

    //private fun privatAfpPeriode(source: LegacySimulertPrivatAfpPeriode) =
    private fun privatAfpPeriode(source: SimulertPrivatAfpPeriode) =
        AnonymSimulertPrivatAfpPeriodeV1(
            alder = source.alderAar,
            belopArlig = source.aarligBeloep,
            belopMnd = source.maanedligBeloep
        )

    private fun livsvarigOffentligAfp(source: OutputLivsvarigOffentligAfp) =
        AnonymSimulertOffentligAfpPeriodeV1(
            alder = source.alderAar,
            belopArlig = source.beloep,
            belopMnd = source.beloep //TODO divide by 12?
        )
}
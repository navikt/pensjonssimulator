package no.nav.pensjon.simulator.alderspensjon.api.nav.direct.anonym.acl.v1.result

import no.nav.pensjon.simulator.afp.offentlig.fra2025.LivsvarigOffentligAfpOutput
import no.nav.pensjon.simulator.afp.privat.PrivatAfpPeriode
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
        AnonymSimuleringResultEnvelopeV1(result = result(source))

    private fun result(source: SimulatorOutput) =
        AnonymSimuleringResultV1(
            alderspensjonAndelKapittel19 = source.alderspensjon?.kapittel19Andel ?: 0.0,
            alderspensjonAndelKapittel20 = source.alderspensjon?.kapittel20Andel ?: 0.0,
            alderspensjonPerioder = source.alderspensjon?.pensjonPeriodeListe.orEmpty().map(::pensjonPeriode),
            afpPrivatPerioder = source.privatAfpPeriodeListe.map(::privatAfpPeriode),
            afpOffentligPerioder = source.livsvarigOffentligAfp.orEmpty().map(::livsvarigOffentligAfp)
        )

    private fun pensjonPeriode(source: PensjonPeriode) =
        AnonymPensjonPeriodeV1(
            belop = source.beloep,
            alder = source.alderAar,
            simulertBeregningsinformasjon = source.simulertBeregningInformasjonListe.firstOrNull()
                ?.let(::beregningInformasjon)
        )

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

    private fun privatAfpPeriode(source: PrivatAfpPeriode) =
        AnonymSimulertPrivatAfpPeriodeV1(
            alder = source.alderAar,
            belopArlig = source.aarligBeloep,
            belopMnd = source.maanedligBeloep
        )

    private fun livsvarigOffentligAfp(source: LivsvarigOffentligAfpOutput) =
        AnonymSimulertOffentligAfpPeriodeV1(
            alder = source.alderAar,
            belopArlig = source.beloep,
            belopMnd = source.beloep //TODO divide by 12?
        )
}

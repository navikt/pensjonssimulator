package no.nav.pensjon.simulator.alderspensjon.tpo.api.acl.v3out

import no.nav.pensjon.simulator.core.afp.privat.SimulertPrivatAfpPeriode
import no.nav.pensjon.simulator.core.beregn.BeholdningPeriode
import no.nav.pensjon.simulator.core.beregn.GarantipensjonNivaa
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Uttaksgrad
import no.nav.pensjon.simulator.core.result.PensjonPeriode
import no.nav.pensjon.simulator.core.result.SimulatorOutput
import no.nav.pensjon.simulator.core.result.SimulertAlderspensjon
import no.nav.pensjon.simulator.core.result.SimulertBeregningInformasjon
import no.nav.pensjon.simulator.core.util.toLocalDate

object TpoSimuleringResultMapperV3 {

    fun toDto(source: SimulatorOutput) =
        TpoSimuleringResultV3(
            ap = source.alderspensjon?.let(::alderspensjon),
            afpPrivat = source.privatAfpPeriodeListe.map(::privatAfpPeriode),
            sisteGyldigeOpptjeningsAr = source.sisteGyldigeOpptjeningAar
        )

    private fun alderspensjon(source: SimulertAlderspensjon) =
        SimulertAlderspensjonV3(
            pensjonsperiodeListe = source.pensjonPeriodeListe.map(::pensjonPeriode),
            pensjonsbeholdningListe = source.pensjonBeholdningListe.map(::beholdningPeriode),
            uttaksgradListe = source.uttakGradListe.map(::uttakGrad),
            simulertBeregningsinformasjonListe = source.simulertBeregningInformasjonListe.map(::beregningInformasjon)
        )

    private fun privatAfpPeriode(source: SimulertPrivatAfpPeriode) =
        SimulertAfpPrivatperiodeV3(afpOpptjening = source.afpOpptjening)

    private fun pensjonPeriode(source: PensjonPeriode) =
        PensjonsperiodeV3(
            alder = source.alderAar,
            belop = source.beloep
        )

    private fun beholdningPeriode(source: BeholdningPeriode) =
        PensjonsbeholdningPeriodeV3(
            datoFom = source.datoFom,
            garantipensjonsbeholdning = source.garantipensjonsbeholdning,
            garantitilleggsbeholdning = source.garantitilleggsbeholdning,
            pensjonsbeholdning = source.pensjonsbeholdning,
            garantipensjonsniva = source.garantipensjonsniva?.let(::garantipensjonNivaa)
        )

    private fun uttakGrad(source: Uttaksgrad) =
        UttaksgradV3(
            uttaksgrad = source.uttaksgrad,
            fomDato = source.fomDato.toLocalDate(),
            tomDato = source.tomDato.toLocalDate()
        )

    private fun beregningInformasjon(source: SimulertBeregningInformasjon) =
        SimulertBeregningsinformasjonV3(
            datoFom = source.datoFom,
            uttaksgrad = source.uttakGrad,

            // Delytelser:
            gp = source.grunnpensjon,
            tp = source.tilleggspensjon,
            pt = source.pensjonstillegg,
            minstenivaTilleggIndividuelt = source.individueltMinstenivaaTillegg,
            inntektspensjon = source.inntektspensjon,
            garantipensjon = source.garantipensjon,
            garantitillegg = source.garantitillegg,
            skjermt = source.skjermingstillegg,

            // Simuleringsdata:
            pa_f92 = source.pa_f92,
            pa_e91 = source.pa_e91,
            spt = source.spt?.toInt(),
            tt_anv_kap19 = source.tt_anv_kap19,
            basisgp = source.basisGrunnpensjon?.toInt(),
            basistp = source.basisTilleggspensjon?.toInt(),
            basispt = source.basisPensjonstillegg?.toInt(),
            forholdstall = source.forholdstall?.toInt(),
            delingstall = source.delingstall?.toInt(),
            ufg = source.ufoereGrad
        )

    private fun garantipensjonNivaa(source: GarantipensjonNivaa) =
        GarantipensjonsnivaV3(
            belop = source.beloep,
            satsType = source.satsType,
            sats = source.sats,
            tt_anv = source.anvendtTrygdetid
        )
}

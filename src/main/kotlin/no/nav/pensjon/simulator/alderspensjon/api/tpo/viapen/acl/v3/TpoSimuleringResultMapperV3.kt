package no.nav.pensjon.simulator.alderspensjon.api.tpo.viapen.acl.v3

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
            ap = source.alderspensjon?.let(TpoSimuleringResultMapperV3::alderspensjon),
            afpPrivat = source.privatAfpPeriodeListe.map(TpoSimuleringResultMapperV3::privatAfpPeriode),
            sisteGyldigeOpptjeningsAr = source.sisteGyldigeOpptjeningAar
        )

    private fun alderspensjon(source: SimulertAlderspensjon) =
        TpoAlderspensjonV3(
            pensjonsperiodeListe = source.pensjonPeriodeListe.map(TpoSimuleringResultMapperV3::pensjonPeriode),
            pensjonsbeholdningListe = source.pensjonBeholdningListe.map(TpoSimuleringResultMapperV3::beholdningPeriode),
            uttaksgradListe = source.uttakGradListe.map(TpoSimuleringResultMapperV3::uttakGrad),
            simulertBeregningsinformasjonListe = source.simulertBeregningInformasjonListe.map(TpoSimuleringResultMapperV3::beregningInformasjon)
        )

    private fun privatAfpPeriode(source: SimulertPrivatAfpPeriode) =
        TpoPrivatAfpPeriodeV3(afpOpptjening = source.afpOpptjening)

    private fun pensjonPeriode(source: PensjonPeriode) =
        TpoPensjonPeriodeV3(
            alder = source.alderAar,
            belop = source.beloep
        )

    private fun beholdningPeriode(source: BeholdningPeriode) =
        TpoPensjonBeholdningPeriodeV3(
            datoFom = source.datoFom,
            garantipensjonsbeholdning = source.garantipensjonsbeholdning,
            garantitilleggsbeholdning = source.garantitilleggsbeholdning,
            pensjonsbeholdning = source.pensjonsbeholdning,
            garantipensjonsniva = source.garantipensjonsniva?.let(TpoSimuleringResultMapperV3::garantipensjonNivaa)
        )

    private fun uttakGrad(source: Uttaksgrad) =
        TpoUttakGradV3(
            uttaksgrad = source.uttaksgrad,
            fomDato = source.fomDato.toLocalDate(),
            tomDato = source.tomDato.toLocalDate()
        )

    private fun beregningInformasjon(source: SimulertBeregningInformasjon) =
        TpoBeregningInformasjonV3(
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
        TpoGarantipensjonNivaaV3(
            belop = source.beloep,
            satsType = source.satsType,
            sats = source.sats,
            tt_anv = source.anvendtTrygdetid
        )
}

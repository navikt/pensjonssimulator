package no.nav.pensjon.simulator.alderspensjon.api.tpo.viapen.acl.v3

import no.nav.pensjon.simulator.core.afp.privat.SimulertPrivatAfpPeriode
import no.nav.pensjon.simulator.core.beregn.BeholdningPeriode
import no.nav.pensjon.simulator.core.beregn.GarantipensjonNivaa
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Uttaksgrad
import no.nav.pensjon.simulator.core.result.PensjonPeriode
import no.nav.pensjon.simulator.core.result.SimulatorOutput
import no.nav.pensjon.simulator.core.result.SimulertAlderspensjon
import no.nav.pensjon.simulator.core.result.SimulertBeregningInformasjon
import no.nav.pensjon.simulator.core.util.toNorwegianLocalDate

object TpoSimuleringResultMapperV3 {

    fun toDto(source: SimulatorOutput) =
        TpoSimuleringResultV3(
            ap = source.alderspensjon?.let(::alderspensjon),
            afpPrivat = source.privatAfpPeriodeListe.map(::privatAfpPeriode),
            sisteGyldigeOpptjeningsAr = source.sisteGyldigeOpptjeningAar
        )

    private fun alderspensjon(source: SimulertAlderspensjon) =
        TpoAlderspensjonV3(
            pensjonsperiodeListe = source.pensjonPeriodeListe.map(::pensjonPeriode),
            pensjonsbeholdningListe = source.pensjonBeholdningListe.map(::beholdningPeriode),
            uttaksgradListe = source.uttakGradListe.map(::uttaksgrad),
            simulertBeregningsinformasjonListe = source.simulertBeregningInformasjonListe.map(::beregningInformasjon)
        )

    private fun privatAfpPeriode(source: SimulertPrivatAfpPeriode) =
        TpoPrivatAfpPeriodeV3(
            afpOpptjening = source.afpOpptjening,
            // The remaining values are strictly not returned to TPO, but included for comparison purposes:
            alderAar = source.alderAar,
            aarligBeloep = source.aarligBeloep,
            maanedligBeloep = source.maanedligBeloep,
            livsvarig = source.livsvarig,
            kronetillegg = source.kronetillegg,
            kompensasjonstillegg = source.kompensasjonstillegg,
            afpForholdstall = source.afpForholdstall,
            justeringBeloep = source.justeringBeloep
        )

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
            garantipensjonsniva = source.garantipensjonsniva?.let(::garantipensjonNivaa)
        )

    private fun uttaksgrad(source: Uttaksgrad) =
        TpoUttakGradV3(
            uttaksgrad = source.uttaksgrad,
            fomDato = source.fomDato?.toNorwegianLocalDate(),
            tomDato = source.tomDato?.toNorwegianLocalDate()
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
            spt = source.spt,
            tt_anv_kap19 = source.tt_anv_kap19,
            basisgp = source.basisGrunnpensjon,
            basistp = source.basisTilleggspensjon,
            basispt = source.basisPensjonstillegg,
            forholdstall = source.forholdstall,
            delingstall = source.delingstall,
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

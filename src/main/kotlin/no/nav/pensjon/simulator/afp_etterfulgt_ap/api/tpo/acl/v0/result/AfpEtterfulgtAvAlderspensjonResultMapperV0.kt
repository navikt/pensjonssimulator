package no.nav.pensjon.simulator.afp_etterfulgt_ap.api.tpo.acl.v0.result

import no.nav.pensjon.simulator.afp.offentlig.pre2025.AfpGrad
import no.nav.pensjon.simulator.core.domain.regler.beregning.Beregning
import no.nav.pensjon.simulator.core.domain.regler.beregning.Grunnpensjon
import no.nav.pensjon.simulator.core.domain.regler.beregning.Sertillegg
import no.nav.pensjon.simulator.core.domain.regler.beregning.Tilleggspensjon
import no.nav.pensjon.simulator.core.exception.ImplementationUnrecoverableException
import no.nav.pensjon.simulator.core.result.*
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.core.util.toNorwegianLocalDate
import java.time.LocalDate

object AfpEtterfulgtAvAlderspensjonResultMapperV0 {

    fun toDto(source: SimulatorOutput, spec: SimuleringSpec) =
        AfpEtterfulgtAvAlderspensjonResultV0(
            simuleringSuksess = true,
            aarsakListeIkkeSuksess = emptyList(),
            folketrygdberegnetAfp = folketrygdberegnetAfp(
                fom = source.pre2025OffentligAfp!!.virk!!.toNorwegianLocalDate(),
                afp = validAfpBeregning(source),
                spec
            ),
            alderspensjonFraFolketrygden = alderspensjonFraFolketrygdenListe(
                pensjon = validAlderspensjon(source),
                grunnbeloep = source.grunnbeloep,
                uttakDato = spec.foersteUttakDato!!
            )
        )

    fun tomResponsMedAarsak(aarsak: AarsakIkkeSuccessV0) =
        AfpEtterfulgtAvAlderspensjonResultV0(
            simuleringSuksess = false,
            aarsakListeIkkeSuksess = listOf(aarsak),
            folketrygdberegnetAfp = null,
            alderspensjonFraFolketrygden = emptyList()
        )

    private fun validAfpBeregning(source: SimulatorOutput): Beregning =
        source.pre2025OffentligAfp?.beregning
            ?: throw ImplementationUnrecoverableException("pre2025OffentligAfp-beregning mangler i beregningsresultatet")

    private fun validAlderspensjon(source: SimulatorOutput): SimulertAlderspensjon =
        source.alderspensjon
            ?: throw ImplementationUnrecoverableException("alderspensjon mangler i beregningsresultatet")

    private fun folketrygdberegnetAfp(
        fom: LocalDate,
        afp: Beregning,
        spec: SimuleringSpec
    ): FolketrygdberegnetAfpV0 {
        val beregnetTidligereInntekt = afp.tp!!.spt!!.poengrekke!!.tpi
        val sisteLignetInntektAar = spec.registerData?.sisteLignetInntektAar

        return FolketrygdberegnetAfpV0(
            fraOgMedDato = fom,
            beregnetTidligereInntekt = beregnetTidligereInntekt,
            sisteLignetInntektBrukt = sisteLignetInntektAar != null,
            sisteLignetInntektAar = sisteLignetInntektAar,
            afpGrad = AfpGrad.beregnAfpGrad(spec.inntektUnderGradertUttakBeloep, beregnetTidligereInntekt),
            afpAvkortetTil70Prosent = afp.gpAfpPensjonsregulert?.brukt == true,
            grunnpensjon = grunnpensjon(afp),
            tilleggspensjon = tilleggspensjon(afp),
            saertillegg = afp.st?.let(::saertillegg),
            maanedligAfpTillegg = afp.afpTillegg!!.netto,
            sumMaanedligUtbetaling = afp.netto
        )
    }

    private fun grunnpensjon(source: Beregning) =
        grunnpensjon(source = source.gp!!, grunnbeloep = source.g)

    private fun tilleggspensjon(source: Beregning) =
        source.tp?.let { tilleggspensjon(source = it, grunnbeloep = source.g) }

    private fun grunnpensjon(source: Grunnpensjon, grunnbeloep: Int) =
        GrunnpensjonV0(
            maanedligUtbetaling = source.netto,
            grunnbeloep = grunnbeloep,
            grunnpensjonsats = source.pSats_gp,
            trygdetid = source.anvendtTrygdetid!!.tt_anv
        )

    private fun tilleggspensjon(source: Tilleggspensjon, grunnbeloep: Int): TilleggspensjonV0 {
        val sluttpoengtall = source.spt!!
        val poengrekke = sluttpoengtall.poengrekke!!

        return TilleggspensjonV0(
            maanedligUtbetaling = source.netto,
            grunnbeloep = grunnbeloep,
            sluttpoengTall = sluttpoengtall.pt,
            antallPoengaarTilOgMed1991 = poengrekke.pa_f92,
            antallPoengaarFraOgMed1992 = poengrekke.pa_e91
        )
    }

    private fun saertillegg(source: Sertillegg) =
        SaertilleggV0(maanedligUtbetaling = source.netto)

    private fun alderspensjonFraFolketrygdenListe(
        pensjon: SimulertAlderspensjon,
        grunnbeloep: Int,
        uttakDato: LocalDate
    ): List<AlderspensjonFraFolketrygdenV0> =
        pensjon.pensjonPeriodeListe
            .filter { it.simulertBeregningInformasjonListe.isNotEmpty() }
            .map { simuleringsperiodeListe(it, pensjon, uttakDato) }
            .flatten()
            .map { alderspensjonFraFolketrygden(it, pensjon, grunnbeloep) }

    private fun simuleringsperiodeListe(
        periode: PensjonPeriode,
        pensjon: SimulertAlderspensjon,
        uttakDato: LocalDate
    ): List<Simuleringsperiode> =
        periode.maanedsutbetalinger.map {
            simuleringsperiode(
                maanedsutbetaling = it,
                periode,
                pensjon,
                uttakDato
            )
        }

    private fun alderspensjonFraFolketrygden(
        periode: Simuleringsperiode,
        pensjon: SimulertAlderspensjon,
        grunnbeloep: Int
    ) =
        AlderspensjonFraFolketrygdenV0(
            fraOgMedDato = periode.fom,
            sumMaanedligUtbetaling = periode.maanedsbeloep,
            andelKapittel19 = pensjon.kapittel19Andel,
            alderspensjonKapittel19 =
                if (pensjon.kapittel19Andel == 0.0) null
                else periode.simulertAlderspensjonInfo?.let { alderspensjonKapittel19(it, grunnbeloep) },
            andelKapittel20 = pensjon.kapittel20Andel,
            alderspensjonKapittel20 = periode.simulertAlderspensjonInfo?.let(::alderspensjonKapittel20)
        )

    private fun alderspensjonKapittel19(source: SimulertAlderspensjonInfo, grunnbeloep: Int) =
        AlderspensjonKapittel19V0(
            grunnpensjon = grunnpensjon(source, grunnbeloep),
            tilleggspensjon = tilleggspensjon(source, grunnbeloep),
            pensjonstillegg = pensjonstillegg(source)
        )

    private fun alderspensjonKapittel20(source: SimulertAlderspensjonInfo) =
        AlderspensjonKapittel20V0(
            inntektspensjon = inntektspensjon(source),
            garantipensjon = garantipensjon(source)
        )

    private fun grunnpensjon(source: SimulertAlderspensjonInfo, grunnbeloep: Int) =
        GrunnpensjonV0(
            maanedligUtbetaling = source.grunnpensjon!!,
            grunnbeloep = grunnbeloep,
            grunnpensjonsats = source.grunnpensjonsats,
            trygdetid = source.trygdetidKap19!!
        )

    private fun tilleggspensjon(source: SimulertAlderspensjonInfo, grunnbeloep: Int) =
        TilleggspensjonV0(
            maanedligUtbetaling = source.tilleggspensjon!!,
            grunnbeloep = grunnbeloep,
            sluttpoengTall = source.sluttpoengtall!!,
            antallPoengaarTilOgMed1991 = source.poengaarFoer92!!,
            antallPoengaarFraOgMed1992 = source.poengaarEtter91!!
        )

    private fun pensjonstillegg(source: SimulertAlderspensjonInfo) =
        PensjonstilleggV0(
            maanedligUtbetaling = source.pensjonstillegg!!,
            minstepensjonsnivaaSats = source.minstepensjonsnivaaSats
        )

    private fun inntektspensjon(source: SimulertAlderspensjonInfo) =
        InntektspensjonV0(
            maanedligUtbetaling = source.inntektspensjon!!,
            pensjonsbeholdningForUttak = source.pensjonBeholdningFoerUttak!!
        )

    private fun garantipensjon(source: SimulertAlderspensjonInfo) =
        GarantipensjonV0(
            maanedligUtbetaling = source.garantipensjon!!,
            garantipensjonsbeholdningForUttak = source.garantipensjonBeholdningFoerUttak,
            trygdetid = source.trygdetidKap20!!
        )

    private fun simuleringsperiode(
        maanedsutbetaling: Maanedsutbetaling,
        periode: PensjonPeriode,
        pensjon: SimulertAlderspensjon,
        uttakDato: LocalDate
    ) =
        Simuleringsperiode(
            fom = maanedsutbetaling.fom,
            maanedsbeloep = maanedsutbetaling.beloep,
            simulertAlderspensjonInfo = periode.simulertBeregningInformasjonListe
                .filter { it.datoFom == maanedsutbetaling.fom }
                .map { simulertAlderspensjonInfo(it, periode, maanedsutbetaling, pensjon, uttakDato) }
                .firstOrNull()
        )

    private fun simulertAlderspensjonInfo(
        beregningsinfo: SimulertBeregningInformasjon,
        periode: PensjonPeriode,
        maanedsutbetaling: Maanedsutbetaling,
        pensjon: SimulertAlderspensjon,
        uttakDato: LocalDate
    ) =
        SimulertAlderspensjonInfo(
            fom = beregningsinfo.datoFom,
            beloep = periode.beloep ?: 0,
            maanedsbeloep = maanedsutbetaling.beloep,
            inntektspensjon = beregningsinfo.inntektspensjonPerMaaned,
            garantipensjon = beregningsinfo.garantipensjonPerMaaned,
            delingstall = beregningsinfo.delingstall,
            pensjonBeholdningFoerUttak = periode.foerstePensjonsbeholdningFoerUttak,
            garantipensjonBeholdningFoerUttak = pensjon.garantipensjonsbeholdningVedDato(uttakDato),
            andelsbroekKap19 = pensjon.kapittel19Andel,
            andelsbroekKap20 = pensjon.kapittel20Andel,
            sluttpoengtall = beregningsinfo.spt,
            trygdetidKap19 = beregningsinfo.tt_anv_kap19,
            trygdetidKap20 = beregningsinfo.tt_anv_kap20,
            poengaarFoer92 = beregningsinfo.pa_f92,
            poengaarEtter91 = beregningsinfo.pa_e91,
            forholdstall = beregningsinfo.forholdstall,
            grunnpensjon = beregningsinfo.grunnpensjonPerMaaned,
            grunnpensjonsats = beregningsinfo.grunnpensjonsats,
            tilleggspensjon = beregningsinfo.tilleggspensjonPerMaaned,
            pensjonstillegg = beregningsinfo.pensjonstilleggPerMaaned,
            garantipensjonssats = beregningsinfo.garantipensjonssats,
            minstepensjonsnivaaSats = beregningsinfo.minstePensjonsnivaSats,
            skjermingstillegg = beregningsinfo.skjermingstillegg
        )

    private data class Simuleringsperiode(
        val fom: LocalDate,
        val maanedsbeloep: Int,
        val simulertAlderspensjonInfo: SimulertAlderspensjonInfo?,
    )

    private data class SimulertAlderspensjonInfo(
        val fom: LocalDate?,
        val beloep: Int,
        val maanedsbeloep: Int,
        val inntektspensjon: Int?,
        val garantipensjon: Int?,
        val delingstall: Double?,
        val pensjonBeholdningFoerUttak: Int?,
        val garantipensjonBeholdningFoerUttak: Int?,
        val andelsbroekKap19: Double?,
        val andelsbroekKap20: Double?,
        val sluttpoengtall: Double?,
        val trygdetidKap19: Int?,
        val trygdetidKap20: Int?,
        val poengaarFoer92: Int?,
        val poengaarEtter91: Int?,
        val forholdstall: Double?,
        val grunnpensjon: Int?,
        val tilleggspensjon: Int?,
        val pensjonstillegg: Int?,
        val skjermingstillegg: Int?,
        val grunnpensjonsats: Double?,
        val minstepensjonsnivaaSats: Double?,
        val garantipensjonssats: Double?,
    )
}

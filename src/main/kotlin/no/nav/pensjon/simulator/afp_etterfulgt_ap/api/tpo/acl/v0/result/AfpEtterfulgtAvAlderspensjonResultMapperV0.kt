package no.nav.pensjon.simulator.afp_etterfulgt_ap.api.tpo.acl.v0.result

import mu.KotlinLogging
import no.nav.pensjon.simulator.core.domain.regler.beregning.Beregning
import no.nav.pensjon.simulator.core.result.Maanedsutbetaling
import no.nav.pensjon.simulator.core.result.PensjonPeriode
import no.nav.pensjon.simulator.core.result.SimulatorOutput
import no.nav.pensjon.simulator.core.result.SimulertAlderspensjon
import no.nav.pensjon.simulator.core.util.toNorwegianLocalDate
import java.time.LocalDate
import java.util.*

object AfpEtterfulgtAvAlderspensjonResultMapperV0 {
    private val log = KotlinLogging.logger {}

    fun toDto(source: SimulatorOutput, inntektVedAfpUttak: Int): AfpEtterfulgtAvAlderspensjonResultV0 {
        val afp: Beregning = source.pre2025OffentligAfp?.beregning ?: return ufullstendigRespons()
        val alderspensjon = source.alderspensjon ?: return ufullstendigRespons()

        return AfpEtterfulgtAvAlderspensjonResultV0(
            simuleringSuksess = true,
            aarsakListeIkkeSuksess = emptyList(),
            folketrygdberegnetAfp = mapToFolketrygdberegnetAfp(
                source.pre2025OffentligAfp?.virk!!,
                afp,
                inntektVedAfpUttak
            ),
            alderspensjonFraFolketrygden = mapToAlderspensjon(alderspensjon, source.grunnbeloep)
        )
    }

    private fun ufullstendigRespons() = AfpEtterfulgtAvAlderspensjonResultV0(
        simuleringSuksess = false,
        aarsakListeIkkeSuksess = emptyList(),//TODO
        folketrygdberegnetAfp = null,
        alderspensjonFraFolketrygden = emptyList()
    )

    private fun mapToFolketrygdberegnetAfp(
        virk: Date,
        afp: Beregning,
        inntektVedAfpUttak: Int,
    ): FolketrygdberegnetAfpV0 {
        val tilleggspensjon = afp.tp
        val grunnpensjon = afp.gp!!
        val saertillegg = afp.st
        val tidligereInntekt = tilleggspensjon?.spt?.poengrekke?.tpi!!

        val res = FolketrygdberegnetAfpV0(
            fraOgMedDato = virk.toNorwegianLocalDate(),
            tidligereInntekt = tidligereInntekt,
            afpGrad = beregnAfpGrad(inntektVedAfpUttak, tidligereInntekt),
            grunnpensjon = GrunnpensjonV0(
                maanedligUtbetaling = grunnpensjon.netto,
                grunnbeloep = afp.g,
                grunnpensjonsats = grunnpensjon.pSats_gp,
                trygdetid = grunnpensjon.anvendtTrygdetid!!.tt_anv
            ),
            tilleggspensjon = tilleggspensjon.let {
                TilleggspensjonV0(
                    maanedligUtbetaling = tilleggspensjon.netto,
                    grunnbeloep = afp.g,
                    sluttpoengTall = it.spt!!.pt,
                    antallPoengaarTilOgMed1991 = it.spt!!.poengrekke!!.pa_f92,
                    antallPoengaarFraOgMed1992 = it.spt!!.poengrekke!!.pa_e91
                )
            },
            saertillegg = saertillegg?.let {
                SaertilleggV0(
                    maanedligUtbetaling = saertillegg.netto,
                    saertilleggsats = saertillegg.pSats_st
                )
            },
            maanedligAfpTillegg = afp.afpTillegg!!.netto,
            sumMaanedligUtbetaling = afp.netto
        )

        return res
    }

    private fun beregnAfpGrad(inntektVedAfpUttak: Int, tidligereInntekt: Int) =
        100 - (inntektVedAfpUttak.toDouble() / (tidligereInntekt + 1) * 100).toInt() //+ 1kr for å unngå deling på 0

    private fun mapToAlderspensjon(
        alderspensjon: SimulertAlderspensjon,
        grunnbeloep: Int
    ): List<AlderspensjonFraFolketrygdenV0> {

        val liste: List<Simuleringsperiode> = alderspensjon
            .pensjonPeriodeListe
            .filter { it.simulertBeregningInformasjonListe.isNotEmpty() }
            .map { alderspensjon(it, alderspensjon) }
            .flatten()

        log.info { "afp etterf ap output: $liste" }
        val res = liste.map { simuleringsperiode ->
            AlderspensjonFraFolketrygdenV0(
                fraOgMedDato = simuleringsperiode.fom,
                sumMaanedligUtbetaling = simuleringsperiode.maanedsbeloep,
                andelKapittel19 = simuleringsperiode.simulertAlderspensjonInfo?.andelsbroekKap19,
                alderspensjonKapittel19 = simuleringsperiode.simulertAlderspensjonInfo?.let { AlderspensjonKapittel19V0(
                    grunnpensjon = GrunnpensjonV0(
                        maanedligUtbetaling = it.grunnpensjon!!,
                        grunnbeloep = grunnbeloep,
                        grunnpensjonsats = it.grunnpensjonsats,
                        trygdetid = it.trygdetidKap19!!
                    ),
                    tilleggspensjon = TilleggspensjonV0(
                        maanedligUtbetaling = it.tilleggspensjon!!,
                        grunnbeloep = grunnbeloep,
                        sluttpoengTall = it.sluttpoengtall!!,
                        antallPoengaarTilOgMed1991 = it.poengaarFoer92!!,
                        antallPoengaarFraOgMed1992 = it.poengaarEtter91!!
                    ),
                    pensjonstillegg = PensjonstilleggV0(
                        maanedligUtbetaling = it.pensjonstillegg!!,
                        minstepensjonsnivaaSats = it.minstepensjonsnivaaSats,
                    ),
                    forholdstall = it.forholdstall!!
                )},
                andelKapittel20 = simuleringsperiode.simulertAlderspensjonInfo?.andelsbroekKap20,
                alderspensjonKapittel20 = simuleringsperiode.simulertAlderspensjonInfo?.let {
                    AlderspensjonKapittel20V0(
                        inntektspensjon = InntektspensjonV0(
                            maanedligUtbetaling = it.inntektspensjon!!,
                            pensjonsbeholdningFoerUttak = it.pensjonBeholdningFoerUttak!!,
                        ),
                        garantipensjon = GarantipensjonV0(
                            maanedligUtbetaling = it.garantipensjon!!,
                            garantipensjonssats = it.garantipensjonssats,
                            trygdetid = it.trygdetidKap20!!
                        ),
                        delingstall = it.delingstall!!
                    )
                }
            )
        }
        return res
    }


    fun alderspensjon(source: PensjonPeriode, simulertAlderspensjon: SimulertAlderspensjon?): List<Simuleringsperiode> {
        return source.maanedsutbetalinger.map { maanedsutbetaling: Maanedsutbetaling ->
            Simuleringsperiode(
                fom = maanedsutbetaling.fom,
                maanedsbeloep = maanedsutbetaling.beloep ?: 0,
                simulertAlderspensjonInfo = source.simulertBeregningInformasjonListe
                    .filter { it.datoFom == maanedsutbetaling.fom }
                    .map { info ->
                        SimulertAlderspensjonInfo(
                            fom = info.datoFom,
                            beloep = source.beloep ?: 0,
                            maanedsbeloep = maanedsutbetaling.beloep,
                            inntektspensjon = info.inntektspensjonPerMaaned,
                            garantipensjon = info.garantipensjonPerMaaned,
                            delingstall = info.delingstall,
                            pensjonBeholdningFoerUttak = source.simulertBeregningInformasjonListe
                                .firstOrNull { it.pensjonBeholdningFoerUttak != null }
                                ?.pensjonBeholdningFoerUttak,
                            andelsbroekKap19 = simulertAlderspensjon?.kapittel19Andel ?: 0.0,
                            andelsbroekKap20 = simulertAlderspensjon?.kapittel20Andel ?: 0.0,
                            sluttpoengtall = info.spt,
                            trygdetidKap19 = info.tt_anv_kap19,
                            trygdetidKap20 = info.tt_anv_kap20,
                            poengaarFoer92 = info.pa_f92,
                            poengaarEtter91 = info.pa_e91,
                            forholdstall = info.forholdstall,
                            grunnpensjon = info.grunnpensjonPerMaaned,
                            grunnpensjonsats = info.grunnpensjonsats,
                            tilleggspensjon = info.tilleggspensjonPerMaaned,
                            pensjonstillegg = info.pensjonstilleggPerMaaned,
                            garantipensjonssats = info.garantipensjonssats,
                            minstepensjonsnivaaSats = info.minstepensjonsnivaaSats,
                            skjermingstillegg = info.skjermingstillegg,
                        )
                    }
                    .firstOrNull()
            )
        }
    }

    data class Simuleringsperiode(
        val fom: LocalDate,
        val maanedsbeloep: Int,
        val simulertAlderspensjonInfo: SimulertAlderspensjonInfo?,
    )

    data class SimulertAlderspensjonInfo(
        val fom: LocalDate?,
        val beloep: Int,
        val maanedsbeloep: Int,
        val inntektspensjon: Int?,
        val garantipensjon: Int?,
        val delingstall: Double?,
        val pensjonBeholdningFoerUttak: Int?,
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
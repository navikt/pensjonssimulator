package no.nav.pensjon.simulator.afp_etterfulgt_ap.api.tpo.acl.v0.result

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.core.afp.AfpOrdningType
import no.nav.pensjon.simulator.core.domain.GrunnlagRolle
import no.nav.pensjon.simulator.core.domain.SimuleringType
import no.nav.pensjon.simulator.core.domain.SivilstatusType
import no.nav.pensjon.simulator.core.domain.regler.beregning.*
import no.nav.pensjon.simulator.core.domain.regler.enum.SivilstandEnum
import no.nav.pensjon.simulator.core.domain.regler.simulering.Simuleringsresultat
import no.nav.pensjon.simulator.core.domain.regler.trygdetid.AnvendtTrygdetid
import no.nav.pensjon.simulator.core.krav.UttakGradKode
import no.nav.pensjon.simulator.core.result.*
import no.nav.pensjon.simulator.core.spec.Pre2025OffentligAfpSpec
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.core.util.toNorwegianDate
import no.nav.pensjon.simulator.core.util.toNorwegianLocalDate
import no.nav.pensjon.simulator.person.Pid
import java.time.LocalDate

class AfpEtterfulgtAvAlderspensjonResultMapperV0Test : FunSpec({

    test("toDto should map values to AfpEtterfulgtAvAlderspensjonResultV0") {

        val foedseldato = LocalDate.of(1963, 2, 22)
        val nesteMaaned = LocalDate.now().plusMonths(1).withDayOfMonth(1)
        val simuleringSpec = mockSimuleringSpec(nesteMaaned, foedseldato)
        val simulatorOutput = mockSimulatorOutput(nesteMaaned, foedseldato)

        val dto: AfpEtterfulgtAvAlderspensjonResultV0 = AfpEtterfulgtAvAlderspensjonResultMapperV0.toDto(simulatorOutput, simuleringSpec)

        dto.simuleringSuksess shouldBe true
        dto.aarsakListeIkkeSuksess shouldBe emptyList()
        dto.folketrygdberegnetAfp shouldBe afpResultat(simulatorOutput, simuleringSpec)
        dto.alderspensjonFraFolketrygden shouldBe alderspensjonFraFolketrygdenResultatListe(simulatorOutput, nesteMaaned)
    }
})

private fun alderspensjonFraFolketrygdenResultatListe(
    simulatorOutput: SimulatorOutput,
    nesteMaaned: LocalDate
) = listOf(
    AlderspensjonFraFolketrygdenV0(
        fraOgMedDato = simulatorOutput.heltUttakDato!!,
        andelKapittel19 = simulatorOutput.alderspensjon!!.kapittel19Andel,
        alderspensjonKapittel19 = AlderspensjonKapittel19V0(
            grunnpensjon = GrunnpensjonV0(
                maanedligUtbetaling = simulatorOutput.alderspensjon!!.pensjonPeriodeListe[0].simulertBeregningInformasjonListe[0].grunnpensjonPerMaaned!!,
                grunnbeloep = simulatorOutput.grunnbeloep,
                grunnpensjonsats = simulatorOutput.alderspensjon!!.pensjonPeriodeListe[0].simulertBeregningInformasjonListe[0].grunnpensjonsats,
                trygdetid = simulatorOutput.alderspensjon!!.pensjonPeriodeListe[0].simulertBeregningInformasjonListe[0].tt_anv_kap19!!
            ),
            tilleggspensjon = TilleggspensjonV0(
                maanedligUtbetaling = simulatorOutput.alderspensjon!!.pensjonPeriodeListe[0].simulertBeregningInformasjonListe[0].tilleggspensjonPerMaaned!!,
                grunnbeloep = simulatorOutput.grunnbeloep,
                sluttpoengTall = simulatorOutput.alderspensjon!!.pensjonPeriodeListe[0].simulertBeregningInformasjonListe[0].spt!!,
                antallPoengaarTilOgMed1991 = simulatorOutput.alderspensjon!!.pensjonPeriodeListe[0].simulertBeregningInformasjonListe[0].pa_f92!!,
                antallPoengaarFraOgMed1992 = simulatorOutput.alderspensjon!!.pensjonPeriodeListe[0].simulertBeregningInformasjonListe[0].pa_e91!!
            ),
            pensjonstillegg = PensjonstilleggV0(
                maanedligUtbetaling = simulatorOutput.alderspensjon!!.pensjonPeriodeListe[0].simulertBeregningInformasjonListe[0].pensjonstilleggPerMaaned!!,
                minstepensjonsnivaaSats = simulatorOutput.alderspensjon!!.pensjonPeriodeListe[0].simulertBeregningInformasjonListe[0].minstePensjonsnivaSats!!,
            ),
            forholdstall = simulatorOutput.alderspensjon!!.pensjonPeriodeListe[0].simulertBeregningInformasjonListe[0].forholdstall!!
        ),
        andelKapittel20 = simulatorOutput.alderspensjon!!.kapittel20Andel,
        alderspensjonKapittel20 = AlderspensjonKapittel20V0(
            inntektspensjon = InntektspensjonV0(
                maanedligUtbetaling = simulatorOutput.alderspensjon!!.pensjonPeriodeListe[0].simulertBeregningInformasjonListe[0].inntektspensjonPerMaaned!!,
                pensjonsbeholdningFoerUttak = simulatorOutput.alderspensjon!!.pensjonPeriodeListe[0].simulertBeregningInformasjonListe[0].pensjonBeholdningFoerUttak!!,
            ),
            garantipensjon = GarantipensjonV0(
                maanedligUtbetaling = simulatorOutput.alderspensjon!!.pensjonPeriodeListe[0].simulertBeregningInformasjonListe[0].garantipensjonPerMaaned!!,
                garantipensjonssats = simulatorOutput.alderspensjon!!.pensjonPeriodeListe[0].simulertBeregningInformasjonListe[0].garantipensjonssats!!,
                trygdetid = simulatorOutput.alderspensjon!!.pensjonPeriodeListe[0].simulertBeregningInformasjonListe[0].tt_anv_kap20!!
            ),
            delingstall = simulatorOutput.alderspensjon!!.pensjonPeriodeListe[0].simulertBeregningInformasjonListe[0].delingstall!!,
        ),
        sumMaanedligUtbetaling = simulatorOutput.alderspensjon!!.pensjonPeriodeListe[0].maanedsutbetalinger[0].beloep
    ),
    AlderspensjonFraFolketrygdenV0(
        fraOgMedDato = nesteMaaned.plusYears(1),
        andelKapittel19 = 3.0,
        alderspensjonKapittel19 = null,
        andelKapittel20 = 7.0,
        alderspensjonKapittel20 = null,
        sumMaanedligUtbetaling = simulatorOutput.alderspensjon!!.pensjonPeriodeListe[0].maanedsutbetalinger[1].beloep
    )
)

private fun afpResultat(
    simulatorOutput: SimulatorOutput,
    simuleringSpec: SimuleringSpec
) = FolketrygdberegnetAfpV0(
    fraOgMedDato = simulatorOutput.pre2025OffentligAfp!!.virk!!.toNorwegianLocalDate(),
    beregnetTidligereInntekt = simulatorOutput.pre2025OffentligAfp!!.beregning!!.tp!!.spt!!.poengrekke!!.tpi,
    fremtidigAarligInntektTilAfpUttak = simuleringSpec.forventetInntektBeloep,
    afpGrad = 100 - (simuleringSpec.inntektUnderGradertUttakBeloep.toDouble() / (simulatorOutput.pre2025OffentligAfp!!.beregning!!.tp!!.spt!!.poengrekke!!.tpi + 1) * 100).toInt(),
    afpAvkortetTil70Prosent = simulatorOutput.pre2025OffentligAfp!!.beregning!!.gpAfpPensjonsregulert!!.brukt,
    grunnpensjon = GrunnpensjonV0(
        maanedligUtbetaling = simulatorOutput.pre2025OffentligAfp!!.beregning!!.gp!!.netto,
        grunnbeloep = simulatorOutput.pre2025OffentligAfp!!.beregning!!.g,
        grunnpensjonsats = simulatorOutput.pre2025OffentligAfp!!.beregning!!.gp!!.pSats_gp,
        trygdetid = simulatorOutput.pre2025OffentligAfp!!.beregning!!.gp!!.anvendtTrygdetid!!.tt_anv
    ),
    tilleggspensjon = TilleggspensjonV0(
        maanedligUtbetaling = simulatorOutput.pre2025OffentligAfp!!.beregning!!.tp!!.netto,
        grunnbeloep = simulatorOutput.pre2025OffentligAfp!!.beregning!!.g,
        sluttpoengTall = simulatorOutput.pre2025OffentligAfp!!.beregning!!.tp!!.spt!!.pt,
        antallPoengaarTilOgMed1991 = simulatorOutput.pre2025OffentligAfp!!.beregning!!.tp!!.spt!!.poengrekke!!.pa_f92,
        antallPoengaarFraOgMed1992 = simulatorOutput.pre2025OffentligAfp!!.beregning!!.tp!!.spt!!.poengrekke!!.pa_e91
    ),
    saertillegg = SaertilleggV0(
        maanedligUtbetaling = simulatorOutput.pre2025OffentligAfp!!.beregning!!.st!!.netto,
        saertilleggsats = simulatorOutput.pre2025OffentligAfp!!.beregning!!.st!!.pSats_st
    ),
    maanedligAfpTillegg = simulatorOutput.pre2025OffentligAfp!!.beregning!!.afpTillegg!!.netto,
    sumMaanedligUtbetaling = simulatorOutput.pre2025OffentligAfp!!.beregning!!.netto
)

private fun mockSimulatorOutput(
    nesteMaaned: LocalDate,
    foedseldato: LocalDate
): SimulatorOutput {
    val result = SimulatorOutput()
    result.pre2025OffentligAfp = Simuleringsresultat().apply {
        virk = nesteMaaned.toNorwegianDate()
        beregning = Beregning().apply {
            g = 33
            gpAfpPensjonsregulert = Grunnpensjon().apply { brukt = true }
            tp = Tilleggspensjon().apply {
                netto = 34
                spt = Sluttpoengtall().apply {
                    pt = 35.0
                    poengrekke = Poengrekke().apply {
                        pa_f92 = 36
                        pa_e91 = 37
                        tpi = 38
                    }
                }
            }
            gp = Grunnpensjon().apply {
                netto = 39
                pSats_gp = 40.0
                anvendtTrygdetid = AnvendtTrygdetid().apply {
                    tt_anv = 41
                }
            }
            st = Sertillegg().apply {
                netto = 42
                pSats_st = 43.0
            }
            afpTillegg = AfpTillegg().apply {
                netto = 43
            }
            netto = 44
        }
    }

    result.alderspensjon = SimulertAlderspensjon()
    result.alderspensjon!!.kapittel19Andel = 3.0
    result.alderspensjon!!.kapittel20Andel = 7.0
    result.alderspensjon!!.addPensjonsperiode(
        PensjonPeriode().apply {
            alderAar = 62
            beloep = 5
            maanedsutbetalinger =
                mutableListOf(Maanedsutbetaling(6, nesteMaaned), Maanedsutbetaling(7, nesteMaaned.plusYears(1)))
            simulertBeregningInformasjonListe = mutableListOf(SimulertBeregningInformasjon()
                .apply {
                    datoFom = nesteMaaned
                    aarligBeloep = 8
                    maanedligBeloep = 9
                    inntektspensjon = 12
                    garantipensjon = 14
                    delingstall = 15.0
                    pensjonBeholdningFoerUttak = 18
                    spt = 19.0
                    tt_anv_kap19 = 20
                    tt_anv_kap20 = 21
                    pa_f92 = 22
                    pa_e91 = 23
                    forholdstall = 24.0
                    grunnpensjonPerMaaned = 25
                    grunnpensjonsats = 26.0
                    tilleggspensjonPerMaaned = 27
                    pensjonstilleggPerMaaned = 28
                    garantipensjonssats = 29.0
                    minstePensjonsnivaSats = 30.0
                    skjermingstillegg = 31
                    startMaaned = 1
                    vinnendeBeregning = GrunnlagRolle.SOKER
                    uttakGrad = 100.0
                    kapittel20Pensjon = 10
                    vektetKapittel20Pensjon = 11
                    inntektspensjonPerMaaned = 13
                    garantipensjonPerMaaned = 15
                    garantitillegg = 17
                    pensjonBeholdningEtterUttak = 19
                    kapittel19Pensjon = 20
                    vektetKapittel19Pensjon = 21
                    basispensjon = 22
                    basisGrunnpensjon = 23.0
                    basisTilleggspensjon = 24.0
                    basisPensjonstillegg = 25.0
                    restBasisPensjon = 26
                })
        }
    )
    result.sisteGyldigeOpptjeningAar = 1
    result.sivilstand = SivilstandEnum.GIFT
    result.grunnbeloep = 2
    result.epsHarPensjon = true
    result.epsHarInntektOver2G = true
    result.foedselDato = foedseldato
    result.heltUttakDato = nesteMaaned
    return result
}

private fun mockSimuleringSpec(
    nesteMaaned: LocalDate,
    foedseldato: LocalDate
) = SimuleringSpec(
    type = SimuleringType.AFP_ETTERF_ALDER,
    sivilstatus = SivilstatusType.GIFT,
    epsHarPensjon = true,
    foersteUttakDato = nesteMaaned,
    heltUttakDato = nesteMaaned,
    pid = Pid("22426305678"),
    foedselDato = foedseldato,
    epsHarInntektOver2G = true,
    fremtidigInntektListe = mutableListOf(),
    brukFremtidigInntekt = false,
    inntektEtterHeltUttakBeloep = 1,
    inntektOver1GAntallAar = 0,
    inntektUnderGradertUttakBeloep = 2,
    inntektEtterHeltUttakAntallAar = null,
    forventetInntektBeloep = 3,
    utlandAntallAar = 4,
    simulerForTp = false,
    erAnonym = false,
    ignoreAvslag = false,
    isHentPensjonsbeholdninger = false,
    isOutputSimulertBeregningsinformasjonForAllKnekkpunkter = true,
    onlyVilkaarsproeving = false,
    utlandPeriodeListe = mutableListOf(),
    epsKanOverskrives = false,
    foedselAar = 0,
    flyktning = false,
    rettTilOffentligAfpFom = null,
    pre2025OffentligAfp = Pre2025OffentligAfpSpec(
        afpOrdning = AfpOrdningType.AFPSTAT,
        inntektMaanedenFoerAfpUttakBeloep = 5,
        inntektUnderAfpUttakBeloep = 6
    ),
    avdoed = null,
    isTpOrigSimulering = true,
    uttakGrad = UttakGradKode.P_100,
)

package no.nav.pensjon.simulator.afp.offentlig.pre2025.api.acl.v0.result

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.core.domain.SivilstatusType
import no.nav.pensjon.simulator.core.domain.regler.beregning.*
import no.nav.pensjon.simulator.core.domain.regler.enum.AFPtypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.GrunnlagsrolleEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.SimuleringTypeEnum
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
import kotlin.reflect.full.companionObject
import kotlin.reflect.full.memberProperties

class AfpEtterfulgtAvAlderspensjonResultMapperV0Test : StringSpec({

    "toDto mapper alle felter til AfpEtterfulgtAvAlderspensjonResultV0" {
        val foedselsdato = LocalDate.of(1963, 2, 22)
        val nesteMaaned = LocalDate.now().plusMonths(1).withDayOfMonth(1)
        val simuleringSpec = simuleringSpec(uttakDato = nesteMaaned, foedselsdato)
        val simulatorOutput = mockSimulatorOutput(fom = nesteMaaned, foedselsdato)

        val dto: AfpEtterfulgtAvAlderspensjonResultV0 =
            AfpEtterfulgtAvAlderspensjonResultMapperV0.toDto(simulatorOutput, simuleringSpec)

        with(dto) {
            simuleringSuksess shouldBe true
            aarsakListeIkkeSuksess shouldBe emptyList()
            folketrygdberegnetAfp shouldBe folketrygdberegnetAfp(
                pre2025OffentligAfp = simulatorOutput.pre2025OffentligAfp!!,
                inntektUnderGradertUttakBeloep = simuleringSpec.inntektUnderGradertUttakBeloep
            )
            alderspensjonFraFolketrygden shouldBe alderspensjonFraFolketrygdenResultatListe(
                output = simulatorOutput,
                element2Fom = nesteMaaned.plusYears(1)
            )
        }
    }

    "toDto haandterer alderspensjon med uten kapittel19" {
        val foedselsdato = LocalDate.of(1963, 2, 22)
        val nesteMaaned = LocalDate.now().plusMonths(1).withDayOfMonth(1)
        val simuleringSpec = simuleringSpec(uttakDato = nesteMaaned, foedselsdato)
        val simulatorOutput = mockSimulatorOutput(fom = nesteMaaned, foedselsdato, kapittel19Andel = 0.0)

        val dto: AfpEtterfulgtAvAlderspensjonResultV0 =
            AfpEtterfulgtAvAlderspensjonResultMapperV0.toDto(simulatorOutput, simuleringSpec)

        with(dto) {
            simuleringSuksess shouldBe true
            aarsakListeIkkeSuksess shouldBe emptyList()
            alderspensjonFraFolketrygden.forEach { it.alderspensjonKapittel19 shouldBe null }
        }
    }

    val alleAarsaker = AarsakIkkeSuccessV0::class.companionObject!!
        .memberProperties
        .mapNotNull { it.call(AarsakIkkeSuccessV0) as? AarsakIkkeSuccessV0 }

    alleAarsaker.forEach {
        "${it.statusKode} mappes til tom dto med fylt aarsak i listen" {
            val dto = AfpEtterfulgtAvAlderspensjonResultMapperV0.tomResponsMedAarsak(it)
            with(dto) {
                simuleringSuksess shouldBe false
                aarsakListeIkkeSuksess shouldBe listOf(it)
                folketrygdberegnetAfp shouldBe null
                alderspensjonFraFolketrygden shouldBe emptyList()
            }
        }
    }
})

private fun simuleringSpec(uttakDato: LocalDate, foedselsdato: LocalDate) =
    SimuleringSpec(
        type = SimuleringTypeEnum.AFP_ETTERF_ALDER,
        sivilstatus = SivilstatusType.GIFT,
        epsHarPensjon = true,
        foersteUttakDato = uttakDato,
        heltUttakDato = uttakDato,
        pid = Pid("22426305678"),
        foedselDato = foedselsdato,
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
            afpOrdning = AFPtypeEnum.AFPSTAT,
            inntektMaanedenFoerAfpUttakBeloep = 5,
            inntektUnderAfpUttakBeloep = 6
        ),
        avdoed = null,
        isTpOrigSimulering = true,
        uttakGrad = UttakGradKode.P_100,
    )

private fun mockSimulatorOutput(
    fom: LocalDate,
    foedselsdato: LocalDate,
    kapittel19Andel: Double = 3.0
) =
    SimulatorOutput().apply {
        pre2025OffentligAfp = pre2025OffentligAfp(fom)
        alderspensjon = alderspensjon(kapittel19Andel, foersteUtbetalingFom = fom)
        sisteGyldigeOpptjeningAar = 1
        sivilstand = SivilstandEnum.GIFT
        grunnbeloep = 2
        epsHarPensjon = true
        epsHarInntektOver2G = true
        foedselDato = foedselsdato
        heltUttakDato = fom
    }

private fun folketrygdberegnetAfp(
    pre2025OffentligAfp: Simuleringsresultat,
    inntektUnderGradertUttakBeloep: Int
): FolketrygdberegnetAfpV0 {
    val beregning = pre2025OffentligAfp.beregning!!
    val grunnpensjon = beregning.gp!!
    val tilleggspensjon = beregning.tp!!
    val sluttpoengtall = tilleggspensjon.spt!!
    val poengrekke = sluttpoengtall.poengrekke!!

    return FolketrygdberegnetAfpV0(
        fraOgMedDato = pre2025OffentligAfp.virk!!.toNorwegianLocalDate(),
        beregnetTidligereInntekt = poengrekke.tpi,
        sisteLignetInntektBrukt = false,
        sisteLignetInntektAar = null,
        afpGrad = 100 - (inntektUnderGradertUttakBeloep.toDouble() / poengrekke.tpi * 100).toInt(),
        afpAvkortetTil70Prosent = beregning.gpAfpPensjonsregulert!!.brukt,
        grunnpensjon = GrunnpensjonV0(
            maanedligUtbetaling = grunnpensjon.netto,
            grunnbeloep = beregning.g,
            grunnpensjonsats = grunnpensjon.pSats_gp,
            trygdetid = grunnpensjon.anvendtTrygdetid!!.tt_anv
        ),
        tilleggspensjon = TilleggspensjonV0(
            maanedligUtbetaling = tilleggspensjon.netto,
            grunnbeloep = beregning.g,
            sluttpoengTall = sluttpoengtall.pt,
            antallPoengaarTilOgMed1991 = poengrekke.pa_f92,
            antallPoengaarFraOgMed1992 = poengrekke.pa_e91
        ),
        saertillegg = SaertilleggV0(maanedligUtbetaling = beregning.st!!.netto),
        maanedligAfpTillegg = beregning.afpTillegg!!.netto,
        sumMaanedligUtbetaling = beregning.netto
    )
}

private fun alderspensjonFraFolketrygdenResultatListe(
    output: SimulatorOutput,
    element2Fom: LocalDate
): List<AlderspensjonFraFolketrygdenV0> {
    val alderspensjon = output.alderspensjon!!
    val periode = alderspensjon.pensjonPeriodeListe[0]
    val beregningsinfo = periode.simulertBeregningInformasjonListe[0]

    return listOf(
        AlderspensjonFraFolketrygdenV0(
            fraOgMedDato = output.heltUttakDato!!,
            andelKapittel19 = alderspensjon.kapittel19Andel,
            alderspensjonKapittel19 = AlderspensjonKapittel19V0(
                grunnpensjon = GrunnpensjonV0(
                    maanedligUtbetaling = beregningsinfo.grunnpensjonPerMaaned!!,
                    grunnbeloep = output.grunnbeloep,
                    grunnpensjonsats = beregningsinfo.grunnpensjonsats,
                    trygdetid = beregningsinfo.tt_anv_kap19!!
                ),
                tilleggspensjon = TilleggspensjonV0(
                    maanedligUtbetaling = beregningsinfo.tilleggspensjonPerMaaned!!,
                    grunnbeloep = output.grunnbeloep,
                    sluttpoengTall = beregningsinfo.spt!!,
                    antallPoengaarTilOgMed1991 = beregningsinfo.pa_f92!!,
                    antallPoengaarFraOgMed1992 = beregningsinfo.pa_e91!!
                ),
                pensjonstillegg = PensjonstilleggV0(
                    maanedligUtbetaling = beregningsinfo.pensjonstilleggPerMaaned!!,
                    minstepensjonsnivaaSats = beregningsinfo.minstePensjonsnivaSats!!,
                )
            ),
            andelKapittel20 = alderspensjon.kapittel20Andel,
            alderspensjonKapittel20 = AlderspensjonKapittel20V0(
                inntektspensjon = InntektspensjonV0(
                    maanedligUtbetaling = beregningsinfo.inntektspensjonPerMaaned!!,
                    pensjonsbeholdningForUttak = beregningsinfo.pensjonBeholdningFoerUttak!!,
                ),
                garantipensjon = GarantipensjonV0(
                    maanedligUtbetaling = beregningsinfo.garantipensjonPerMaaned!!,
                    garantipensjonsbeholdningForUttak = null,
                    trygdetid = beregningsinfo.tt_anv_kap20!!
                )
            ),
            sumMaanedligUtbetaling = periode.maanedsutbetalinger[0].beloep
        ),
        AlderspensjonFraFolketrygdenV0(
            fraOgMedDato = element2Fom,
            andelKapittel19 = 3.0,
            alderspensjonKapittel19 = null,
            andelKapittel20 = 7.0,
            alderspensjonKapittel20 = null,
            sumMaanedligUtbetaling = periode.maanedsutbetalinger[1].beloep
        )
    )
}

private fun alderspensjon(kapittel19Andel: Double, foersteUtbetalingFom: LocalDate) =
    SimulertAlderspensjon().apply {
        this.kapittel19Andel = kapittel19Andel
        this.kapittel20Andel = 7.0
        addPensjonsperiode(pensjonsperiode(foersteUtbetalingFom))
    }

private fun pensjonsperiode(foersteUtbetalingFom: LocalDate) =
    PensjonPeriode().apply {
        alderAar = 62
        beloep = 5
        maanedsutbetalinger = mutableListOf(
            Maanedsutbetaling(beloep = 6, fom = foersteUtbetalingFom),
            Maanedsutbetaling(beloep = 7, fom = foersteUtbetalingFom.plusYears(1))
        )
        simulertBeregningInformasjonListe = mutableListOf(simulertBeregningInformasjon(foersteUtbetalingFom))
    }

private fun pre2025OffentligAfp(fom: LocalDate) =
    Simuleringsresultat().apply {
        virk = fom.toNorwegianDate()
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
                anvendtTrygdetid = AnvendtTrygdetid().apply { tt_anv = 41 }
            }
            st = Sertillegg().apply {
                netto = 42
                pSats_st = 43.0
            }
            afpTillegg = AfpTillegg().apply { netto = 43 }
            netto = 44
        }
    }

private fun simulertBeregningInformasjon(fom: LocalDate) =
    SimulertBeregningInformasjon().apply {
        datoFom = fom
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
        vinnendeBeregning = GrunnlagsrolleEnum.SOKER
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
    }

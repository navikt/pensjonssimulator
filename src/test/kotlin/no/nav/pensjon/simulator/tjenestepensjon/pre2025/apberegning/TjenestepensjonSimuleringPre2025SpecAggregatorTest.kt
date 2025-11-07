package no.nav.pensjon.simulator.tjenestepensjon.pre2025.apberegning

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.afp.privat.PrivatAfpPeriode
import no.nav.pensjon.simulator.core.beregn.BeholdningPeriode
import no.nav.pensjon.simulator.core.beregn.GarantipensjonNivaa
import no.nav.pensjon.simulator.core.domain.SivilstatusType
import no.nav.pensjon.simulator.core.domain.regler.beregning.Beregning
import no.nav.pensjon.simulator.core.domain.regler.enum.SimuleringTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.simulering.Simuleringsresultat
import no.nav.pensjon.simulator.core.krav.UttakGradKode
import no.nav.pensjon.simulator.core.result.SimulatorOutput
import no.nav.pensjon.simulator.core.result.SimulertAlderspensjon
import no.nav.pensjon.simulator.core.result.SimulertBeregningInformasjon
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.StillingsprosentSpec
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.api.TjenestepensjonSimuleringPre2025Spec
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.api.acl.v2.StillingsprOffCodeV2
import java.time.LocalDate

class TjenestepensjonSimuleringPre2025SpecAggregatorTest : StringSpec({

    "Offentlig AFP etterfulgt av alderpensjon blir aggregert til spec" {
        val simResultat = mockSimulatorOutput(afpEtterfAvAlder = true)
        val spec: SimuleringSpec = mockSimuleringSpec()
        val stillingsprosent = StillingsprosentSpec(StillingsprOffCodeV2.P_100, StillingsprOffCodeV2.P_60)

        val result: TjenestepensjonSimuleringPre2025Spec = TjenestepensjonSimuleringPre2025SpecAggregator.aggregateSpec(
            simuleringResultat = simResultat,
            simuleringSpec = spec,
            stillingsprosentSpec = stillingsprosent,
            sisteGyldigeOpptjeningsaar = 2024
        )

        result.pid shouldBe spec.pid
        result.foedselsdato shouldBe spec.foedselDato
        result.simulertOffentligAfp?.brutto shouldBe simResultat.pre2025OffentligAfp?.beregning?.brutto
        result.sisteTpOrdningsTpNummer shouldBe "TPNR"
        result.simulertPrivatAfp.shouldBeNull()
        result.sivilstand.name shouldBe spec.sivilstatus.name
        result.inntekter shouldHaveSize 3
        result.inntekter[0].beloep shouldBe 500_000.0
        result.inntekter[1].beloep shouldBe 400_000.0
        result.inntekter[2].beloep shouldBe 0.0
        result.pensjonsbeholdningsperioder shouldHaveSize 1
        with(result.pensjonsbeholdningsperioder[0]) {
            fom shouldBe LocalDate.of(2020, 1, 1)
            pensjonsbeholdning shouldBe 1.0
            garantipensjonsbeholdning shouldBe 2.0
            garantitilleggsbeholdning shouldBe 3.0
        }
        result.simuleringsperioder shouldHaveSize 2
        with(result.simuleringsperioder[0]) {
            stillingsprosentOffentlig shouldBe 60
            simulerAFPOffentligEtterfulgtAvAlder shouldBe true
        }
        with(result.simuleringsperioder[1]) {
            stillingsprosentOffentlig shouldBe 0
            simulerAFPOffentligEtterfulgtAvAlder shouldBe true
        }
        result.simuleringsdata shouldHaveSize 1
        with(result.simuleringsdata[0]) {
            fom shouldBe LocalDate.of(2029, 11, 1)
            andvendtTrygdetid shouldBe 6
        }
    }

    "Privat AFP blir aggregert til spec" {
        val simResultat = mockSimulatorOutput(afpEtterfAvAlder = false)
        val spec: SimuleringSpec =
            mockSimuleringSpec(type = SimuleringTypeEnum.ALDER_M_AFP_PRIVAT, helt = LocalDate.of(2029, 11, 1))
        val stillingsprosent = StillingsprosentSpec(StillingsprOffCodeV2.P_100, StillingsprOffCodeV2.P_60)

        val result: TjenestepensjonSimuleringPre2025Spec = TjenestepensjonSimuleringPre2025SpecAggregator.aggregateSpec(
            simuleringResultat = simResultat,
            simuleringSpec = spec,
            stillingsprosentSpec = stillingsprosent,
            sisteGyldigeOpptjeningsaar = 2024
        )

        result.pid shouldBe spec.pid
        result.foedselsdato shouldBe spec.foedselDato
        result.sisteTpOrdningsTpNummer shouldBe "TPNR"
        result.simulertOffentligAfp.shouldBeNull()
        result.simulertPrivatAfp.shouldNotBeNull()
        with(result.simulertPrivatAfp) {
            kompensasjonstillegg shouldBe 25
            totalAfpBeholdning shouldBe 20
        }
        result.sivilstand.name shouldBe spec.sivilstatus.name
        result.inntekter shouldHaveSize 3
        result.inntekter[0].beloep shouldBe 500_000.0
        result.inntekter[1].beloep shouldBe 400_000.0
        result.inntekter[2].beloep shouldBe 0.0
        result.pensjonsbeholdningsperioder shouldHaveSize 1
        with(result.pensjonsbeholdningsperioder[0]) {
            fom shouldBe LocalDate.of(2020, 1, 1)
            pensjonsbeholdning shouldBe 1.0
            garantipensjonsbeholdning shouldBe 2.0
            garantitilleggsbeholdning shouldBe 3.0
        }
        result.simuleringsperioder shouldHaveSize 3
        result.simuleringsdata shouldHaveSize 1
        with(result.simuleringsdata[0]) {
            fom shouldBe LocalDate.of(2029, 11, 1)
            andvendtTrygdetid shouldBe 6
        }
    }
})

private fun mockSimuleringSpec(
    type: SimuleringTypeEnum = SimuleringTypeEnum.AFP_ETTERF_ALDER,
    foerste: LocalDate = LocalDate.of(2027, 10, 1),
    helt: LocalDate? = null
) = SimuleringSpec(
    type = type,
    sivilstatus = SivilstatusType.GIFT,
    epsHarPensjon = false,
    foersteUttakDato = foerste,
    heltUttakDato = helt,
    pid = Pid("12345678910"),
    foedselDato = LocalDate.of(1962, 10, 2),
    avdoed = null,
    isTpOrigSimulering = false,
    simulerForTp = false,
    uttakGrad = UttakGradKode.P_80,
    forventetInntektBeloep = 500_000,
    inntektUnderGradertUttakBeloep = 400_000,
    inntektEtterHeltUttakBeloep = 0,
    inntektEtterHeltUttakAntallAar = null,
    foedselAar = 1960,
    utlandAntallAar = 0,
    utlandPeriodeListe = mutableListOf(),
    fremtidigInntektListe = mutableListOf(),
    brukFremtidigInntekt = false,
    inntektOver1GAntallAar = 0,
    flyktning = null,
    epsHarInntektOver2G = false,
    rettTilOffentligAfpFom = null,
    pre2025OffentligAfp = null,
    erAnonym = false,
    ignoreAvslag = false,
    isHentPensjonsbeholdninger = true,
    isOutputSimulertBeregningsinformasjonForAllKnekkpunkter = false,
    onlyVilkaarsproeving = false,
    epsKanOverskrives = false,
    registerData = null
)

private fun mockSimulatorOutput(
    afpEtterfAvAlder: Boolean,
) = SimulatorOutput().apply {
    foedselDato = LocalDate.of(1962, 10, 2)
    alderspensjon = SimulertAlderspensjon().apply {
        pensjonBeholdningListe = listOf(
            BeholdningPeriode(
                datoFom = LocalDate.of(2020, 1, 1),
                pensjonsbeholdning = 1.0,
                garantipensjonsbeholdning = 2.0,
                garantitilleggsbeholdning = 3.0,
                garantipensjonsniva = GarantipensjonNivaa(
                    beloep = 4.0,
                    satsType = "ORDINAER",
                    sats = 5.0,
                    anvendtTrygdetid = 6
                )
            )
        )
        simulertBeregningInformasjonListe = listOf(
            SimulertBeregningInformasjon().apply {
                datoFom = LocalDate.of(2029, 11, 1)
                tt_anv_kap19 = 6
                aarligBeloep = 7
                maanedligBeloep = 8
                basisGrunnpensjon = 10.0
                basisPensjonstillegg = 11.0
                basisTilleggspensjon = 12.0
                tt_anv_kap20 = 13
                pa_f92 = 14
                pa_e91 = 15
                ufoereGrad = 16
                delingstall = 17.0
                forholdstall = 18.0
                spt = 19.0
            }
        )
        pre2025OffentligAfp = if (afpEtterfAvAlder) Simuleringsresultat().apply {
            beregning = Beregning().apply { brutto = 9 }
        } else null

        if (!afpEtterfAvAlder) {
            privatAfpPeriodeListe.add(
                PrivatAfpPeriode(
                    afpOpptjening = 20,
                    alderAar = 62,
                    aarligBeloep = 21,
                    maanedligBeloep = 22,
                    livsvarig = 23,
                    kronetillegg = 24,
                    kompensasjonstillegg = 25,
                    afpForholdstall = 26.0,
                    justeringBeloep = 27
                )
            )
        }
    }
}

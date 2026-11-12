package no.nav.pensjon.simulator.tjenestepensjon.pre2025.apberegning.aggregate.inntektliste

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.core.domain.SivilstatusType
import no.nav.pensjon.simulator.core.domain.regler.enum.SimuleringTypeEnum
import no.nav.pensjon.simulator.core.krav.UttakGradKode
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.person.Pid
import java.time.LocalDate

class InntektListeSpecMapperTest : StringSpec({

    "createSpec skal mappe alle felter i InntektListeSpec" {
        val foedselsdato = LocalDate.of(1960, 1, 1)
        val simuleringSpec = simuleringSpecInntekter(
            inntektUnderGradertUttakBeloep = 1,
            inntektEtterHeltUttakBeloep = 2,
            inntektEtterHeltUttakTom = LocalDate.of(2031, 12, 31),
        )

        val inntektSpec = InntektListeSpecMapper.createSpec(simuleringSpec, foedselsdato)

        with(inntektSpec) {
            this.foedselsdato shouldBe foedselsdato
            inntektFoerFoersteUttak shouldBe simuleringSpec.forventetInntektBeloep
            gradertUttak shouldBe true
            simuleringTypeErAfpEtterfAlder shouldBe true
            inntektUnderGradertUttakBeloep shouldBe simuleringSpec.inntektUnderGradertUttakBeloep
            inntektEtterHeltUttakBeloep shouldBe simuleringSpec.inntektEtterHeltUttakBeloep
            inntektEtterHeltUttakTom shouldBe simuleringSpec.inntektEtterHeltUttakTom
            foersteUttakDato shouldBe simuleringSpec.foersteUttakDato
            heltUttakDato shouldBe simuleringSpec.heltUttakDato
        }
    }

    "100% uttakgrad skal mappes til helt uttak" {
        val foedselsdato = LocalDate.of(1960, 1, 1)
        val simuleringSpec = simuleringSpecInntekter(uttaksgrad = UttakGradKode.P_100)

        InntektListeSpecMapper.createSpec(simuleringSpec, foedselsdato).gradertUttak shouldBe false
    }

    "kun AFP_ETTERF_ALDER skal føre til simuleringTypeErAfpEtterfAlder=true" {
        val foedselsdato = LocalDate.of(1960, 1, 1)
        for (simuleringType: SimuleringTypeEnum in SimuleringTypeEnum.entries) {
            if (simuleringType == SimuleringTypeEnum.AFP_ETTERF_ALDER) continue
            val simuleringSpec = simuleringSpecInntekter(type = simuleringType)
            val (_, _, _, simuleringTypeErAfpEtterfAlder) = InntektListeSpecMapper.createSpec(simuleringSpec, foedselsdato)
            simuleringTypeErAfpEtterfAlder shouldBe false
        }
    }
})

private fun simuleringSpecInntekter(
    type: SimuleringTypeEnum = SimuleringTypeEnum.AFP_ETTERF_ALDER,
    foersteUttakDato: LocalDate = LocalDate.of(2027, 10, 1),
    heltUttakDato: LocalDate? = LocalDate.of(2029, 11, 1),
    uttaksgrad: UttakGradKode = UttakGradKode.P_80,
    inntektUnderGradertUttakBeloep: Int = 400_000,
    inntektEtterHeltUttakBeloep: Int = 50_000,
    inntektEtterHeltUttakTom: LocalDate? = LocalDate.of(2030, 12, 31)
) = SimuleringSpec(
    type = type,
    sivilstatus = SivilstatusType.GIFT,
    epsHarPensjon = false,
    foersteUttakDato = foersteUttakDato,
    heltUttakDato = heltUttakDato,
    pid = Pid("12345678910"),
    foedselDato = LocalDate.of(1962, 10, 2),
    avdoed = null,
    isTpOrigSimulering = false,
    simulerForTp = false,
    uttakGrad = uttaksgrad,
    forventetInntektBeloep = 500_000,
    inntektUnderGradertUttakBeloep = inntektUnderGradertUttakBeloep,
    inntektEtterHeltUttakBeloep = inntektEtterHeltUttakBeloep,
    inntektEtterHeltUttakAntallAar = null,
    inntektEtterHeltUttakTom = inntektEtterHeltUttakTom,
    foedselAar = 1960,
    utlandAntallAar = 0,
    utlandPeriodeListe = mutableListOf(),
    fremtidigInntektListe = mutableListOf(),
    brukFremtidigInntekt = false,
    inntektOver1GAntallAar = 0,
    flyktning = null,
    epsHarInntektOver2G = false,
    livsvarigOffentligAfp = null,
    tidsbegrensetOffentligAfp = null,
    erAnonym = false,
    ignoreAvslag = false,
    brukBeregnetBeholdningVedPensjonsberegning = true,
    isOutputSimulertBeregningsinformasjonForAllKnekkpunkter = false,
    onlyVilkaarsproeving = false,
    epsKanOverskrives = false,
    registerData = null
)
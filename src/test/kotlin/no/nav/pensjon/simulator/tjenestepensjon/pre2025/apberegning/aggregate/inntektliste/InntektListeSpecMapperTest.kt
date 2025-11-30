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
        val simSpec = simuleringSpecInntekter(
            inntektUnderGradertUttakBeloep = 1,
            inntektEtterHeltUttakBeloep = 2,
            inntektEtterHeltUttakAntallAar = 3,
        )
        val inntektSpec = InntektListeSpecMapper.createSpec(simSpec, foedselsdato)

        inntektSpec.foedselsdato shouldBe foedselsdato
        inntektSpec.inntektFoerFoersteUttak shouldBe simSpec.forventetInntektBeloep
        inntektSpec.gradertUttak shouldBe true
        inntektSpec.simuleringTypeErAfpEtterfAlder shouldBe true
        inntektSpec.inntektUnderGradertUttakBeloep shouldBe simSpec.inntektUnderGradertUttakBeloep
        inntektSpec.inntektEtterHeltUttakBeloep shouldBe simSpec.inntektEtterHeltUttakBeloep
        inntektSpec.inntektEtterHeltUttakAntallAar shouldBe simSpec.inntektEtterHeltUttakAntallAar
        inntektSpec.foersteUttakDato shouldBe simSpec.foersteUttakDato
        inntektSpec.heltUttakDato shouldBe simSpec.heltUttakDato
    }

    "100% uttakgrad skal mappes til helt uttak" {
        val foedselsdato = LocalDate.of(1960, 1, 1)
        val simSpec = simuleringSpecInntekter(uttaksgrad = UttakGradKode.P_100)
        val inntektSpec = InntektListeSpecMapper.createSpec(simSpec, foedselsdato)

        inntektSpec.gradertUttak shouldBe false
    }

    "kun AFP_ETTERF_ALDER skal føre til simuleringTypeErAfpEtterfAlder=true" {
        val foedselsdato = LocalDate.of(1960, 1, 1)
        for (simuleringType: SimuleringTypeEnum in SimuleringTypeEnum.entries){
            if (simuleringType == SimuleringTypeEnum.AFP_ETTERF_ALDER) continue
            val simSpec = simuleringSpecInntekter(type = simuleringType)
            val (_, _, _, simuleringTypeErAfpEtterfAlder) = InntektListeSpecMapper.createSpec(simSpec, foedselsdato)
            simuleringTypeErAfpEtterfAlder shouldBe false
        }
    }

})

fun simuleringSpecInntekter(
    type: SimuleringTypeEnum = SimuleringTypeEnum.AFP_ETTERF_ALDER,
    foerste: LocalDate = LocalDate.of(2027, 10, 1),
    heltUttaksdato: LocalDate? = LocalDate.of(2029, 11, 1),
    uttaksgrad: UttakGradKode = UttakGradKode.P_80,
    inntektUnderGradertUttakBeloep: Int = 400_000,
    inntektEtterHeltUttakBeloep: Int = 50_000,
    inntektEtterHeltUttakAntallAar: Int? = null
) = SimuleringSpec(
    type = type,
    sivilstatus = SivilstatusType.GIFT,
    epsHarPensjon = false,
    foersteUttakDato = foerste,
    heltUttakDato = heltUttaksdato,
    pid = Pid("12345678910"),
    foedselDato = LocalDate.of(1962, 10, 2),
    avdoed = null,
    isTpOrigSimulering = false,
    simulerForTp = false,
    uttakGrad = uttaksgrad,
    forventetInntektBeloep = 500_000,
    inntektUnderGradertUttakBeloep = inntektUnderGradertUttakBeloep,
    inntektEtterHeltUttakBeloep = inntektEtterHeltUttakBeloep,
    inntektEtterHeltUttakAntallAar = inntektEtterHeltUttakAntallAar,
    foedselAar = 1960,
    utlandAntallAar = 0,
    utlandPeriodeListe = mutableListOf(),
    fremtidigInntektListe = mutableListOf(),
    brukFremtidigInntekt = false,
    inntektOver1GAntallAar = 0,
    flyktning = null,
    epsHarInntektOver2G = false,
    livsvarigOffentligAfp = null,
    pre2025OffentligAfp = null,
    erAnonym = false,
    ignoreAvslag = false,
    isHentPensjonsbeholdninger = true,
    isOutputSimulertBeregningsinformasjonForAllKnekkpunkter = false,
    onlyVilkaarsproeving = false,
    epsKanOverskrives = false,
    registerData = null
)

package no.nav.pensjon.simulator.tjenestepensjon.pre2025.apberegning.aggregate.simuleringsperioder

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.core.domain.SivilstatusType
import no.nav.pensjon.simulator.core.domain.regler.enum.SimuleringTypeEnum
import no.nav.pensjon.simulator.core.krav.UttakGradKode
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.StillingsprosentSpec
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.api.SimulertOffentligAfp
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.StillingsprosentCode
import java.time.LocalDate

class SimuleringsperioderSpecMapperTest : StringSpec({

    "mapper bruker alle felter i SimuleringsperioderSpec" {
        val foedselsdato = LocalDate.now()
        val simSpec = simuleringSpecSimuleringsperioder(antallAarMedInntektEtterHeltUttak = 5)
        val offentligAfpSpec = SimulertOffentligAfp(1, 2)
        val stillingsprosentSpec = StillingsprosentSpec(StillingsprosentCode.P_80, StillingsprosentCode.P_50)
        SimuleringsperioderSpecMapper.createSpec(simSpec, offentligAfpSpec, stillingsprosentSpec, foedselsdato)
            .apply {
                afpEtterfulgtAvAlder shouldBe true //baseres kun på simulert offentlig AFP
                this.foedselsdato shouldBe foedselsdato
                this.stillingsprosentSpec shouldBe stillingsprosentSpec
                this.folketrygdUttaksgrad shouldBe simSpec.uttakGrad.value.toInt()
                this.simuleringType shouldBe simSpec.type
                this.foersteUttakDato shouldBe simSpec.foersteUttakDato
                this.heltUttakDato shouldBe simSpec.heltUttakDato
                this.inntektEtterHeltUttakAntallAar shouldBe 5L
            }
    }

    "afp etterfulgt av alder satt til false ved manglende simulert afp" {
        val foedselsdato = LocalDate.now()
        val simSpec = simuleringSpecSimuleringsperioder(antallAarMedInntektEtterHeltUttak = 5)
        val stillingsprosentSpec = StillingsprosentSpec(StillingsprosentCode.P_80, StillingsprosentCode.P_50)
        SimuleringsperioderSpecMapper.createSpec(simSpec, null, stillingsprosentSpec, foedselsdato)
            .apply {
                afpEtterfulgtAvAlder shouldBe false
            }
    }
})

fun simuleringSpecSimuleringsperioder(antallAarMedInntektEtterHeltUttak: Int?) = SimuleringSpec(
    type = SimuleringTypeEnum.ALDER,
    sivilstatus = SivilstatusType.GIFT,
    epsHarPensjon = false,
    foersteUttakDato = LocalDate.of(2027, 10, 1),
    heltUttakDato = LocalDate.of(2029, 11, 1),
    pid = Pid("12345678910"),
    foedselDato = LocalDate.of(1962, 10, 2),
    avdoed = null,
    isTpOrigSimulering = false,
    simulerForTp = false,
    uttakGrad = UttakGradKode.P_80,
    forventetInntektBeloep = 500_000,
    inntektUnderGradertUttakBeloep = 0,
    inntektEtterHeltUttakBeloep = 0,
    inntektEtterHeltUttakAntallAar = antallAarMedInntektEtterHeltUttak,
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

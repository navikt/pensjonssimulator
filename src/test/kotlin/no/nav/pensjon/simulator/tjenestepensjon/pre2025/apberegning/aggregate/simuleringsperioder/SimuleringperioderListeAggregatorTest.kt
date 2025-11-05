package no.nav.pensjon.simulator.tjenestepensjon.pre2025.apberegning.aggregate.simuleringsperioder

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.core.domain.regler.enum.SimuleringTypeEnum
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.api.Simuleringsperiode
import java.time.LocalDate

class SimuleringperioderListeAggregatorTest : StringSpec({

    ("Aggregate simuleringsperioder for alderspensjon med gradert uttak") {
        SimuleringperioderListeAggregator.aggregate(
            SimuleringsperioderSpec(
                afpEtterfulgtAvAlder = false,
                foedselsdato = LocalDate.of(1960, 1, 1),
                stillingsprosentSpec = no.nav.pensjon.simulator.tjenestepensjon.pre2025.StillingsprosentSpec(
                    stillingsprosentOffGradertUttak = 100,
                    stillingsprosentOffHeltUttak = 60,
                ),
                folketrygdUttaksgrad = 50,
                simuleringType = SimuleringTypeEnum.ALDER,
                foersteUttakDato = LocalDate.of(2025, 1, 1),
                heltUttakDato = LocalDate.of(2029, 1, 1),
                inntektEtterHeltUttakAntallAar = 3L
            )
        ) shouldBe
                listOf(
                    Simuleringsperiode(
                        fom = LocalDate.of(2025, 1, 1),
                        folketrygdUttaksgrad = 50,
                        stillingsprosentOffentlig = 100,
                        simulerAFPOffentligEtterfulgtAvAlder = false
                    ),
                    Simuleringsperiode(
                        fom = LocalDate.of(2029, 1, 1),
                        folketrygdUttaksgrad = 100,
                        stillingsprosentOffentlig = 60,
                        simulerAFPOffentligEtterfulgtAvAlder = false
                    ),
                    Simuleringsperiode(
                        fom = LocalDate.of(2032, 1, 1),
                        folketrygdUttaksgrad = 100,
                        stillingsprosentOffentlig = 0,
                        simulerAFPOffentligEtterfulgtAvAlder = false
                    ),
                )
    }

    ("Aggregate simuleringsperioder uten stillingsprosenter for alderspensjon med gradert uttak") {
        SimuleringperioderListeAggregator.aggregate(
            SimuleringsperioderSpec(
                afpEtterfulgtAvAlder = false,
                foedselsdato = LocalDate.of(1960, 1, 1),
                stillingsprosentSpec = no.nav.pensjon.simulator.tjenestepensjon.pre2025.StillingsprosentSpec(
                    stillingsprosentOffGradertUttak = null,
                    stillingsprosentOffHeltUttak = null,
                ),
                folketrygdUttaksgrad = 50,
                simuleringType = SimuleringTypeEnum.ALDER,
                foersteUttakDato = LocalDate.of(2025, 1, 1),
                heltUttakDato = LocalDate.of(2029, 1, 1),
                inntektEtterHeltUttakAntallAar = 0L
            )
        ) shouldBe
                listOf(
                    Simuleringsperiode(
                        fom = LocalDate.of(2025, 1, 1),
                        folketrygdUttaksgrad = 50,
                        stillingsprosentOffentlig = 0,
                        simulerAFPOffentligEtterfulgtAvAlder = false
                    ),
                    Simuleringsperiode(
                        fom = LocalDate.of(2029, 1, 1),
                        folketrygdUttaksgrad = 100,
                        stillingsprosentOffentlig = 0,
                        simulerAFPOffentligEtterfulgtAvAlder = false
                    ),
                )
    }

    ("Aggregate simuleringsperioder for alderspensjon med helt uttak") {
        SimuleringperioderListeAggregator.aggregate(
            SimuleringsperioderSpec(
                afpEtterfulgtAvAlder = false,
                foedselsdato = LocalDate.of(1960, 1, 1),
                stillingsprosentSpec = no.nav.pensjon.simulator.tjenestepensjon.pre2025.StillingsprosentSpec(
                    stillingsprosentOffGradertUttak = 100,
                    stillingsprosentOffHeltUttak = 0,
                ),
                folketrygdUttaksgrad = 100,
                simuleringType = SimuleringTypeEnum.ALDER,
                foersteUttakDato = LocalDate.of(2027, 2, 1),
                heltUttakDato = null,
                inntektEtterHeltUttakAntallAar = 0L
            )
        ) shouldBe
                listOf(
                    Simuleringsperiode(
                        fom = LocalDate.of(2027, 2, 1),
                        folketrygdUttaksgrad = 100,
                        stillingsprosentOffentlig = 0,
                        simulerAFPOffentligEtterfulgtAvAlder = false
                    ),
                )
    }

    ("Aggregate simuleringsperioder for alderspensjon med helt uttak og høy stillingsprosent") {
        SimuleringperioderListeAggregator.aggregate(
            SimuleringsperioderSpec(
                afpEtterfulgtAvAlder = false,
                foedselsdato = LocalDate.of(1960, 1, 1),
                stillingsprosentSpec = no.nav.pensjon.simulator.tjenestepensjon.pre2025.StillingsprosentSpec(
                    stillingsprosentOffGradertUttak = 100,
                    stillingsprosentOffHeltUttak = 80,
                ),
                folketrygdUttaksgrad = 100,
                simuleringType = SimuleringTypeEnum.ALDER,
                foersteUttakDato = LocalDate.of(2027, 2, 1),
                heltUttakDato = null,
                inntektEtterHeltUttakAntallAar = 1L
            )
        ) shouldBe
                listOf(
                    Simuleringsperiode(
                        fom = LocalDate.of(2027, 2, 1),
                        folketrygdUttaksgrad = 100,
                        stillingsprosentOffentlig = 80,
                        simulerAFPOffentligEtterfulgtAvAlder = false
                    ),
                    Simuleringsperiode(
                        fom = LocalDate.of(2028, 2, 1),
                        folketrygdUttaksgrad = 100,
                        stillingsprosentOffentlig = 0,
                        simulerAFPOffentligEtterfulgtAvAlder = false
                    ),
                )
    }

    ("aggregate simuleringsperioder for afp etterfulgt av alderspensjon") {
        SimuleringperioderListeAggregator.aggregate(
            SimuleringsperioderSpec(
                afpEtterfulgtAvAlder = true,
                foedselsdato = LocalDate.of(1960, 1, 1),
                stillingsprosentSpec = no.nav.pensjon.simulator.tjenestepensjon.pre2025.StillingsprosentSpec(
                    stillingsprosentOffGradertUttak = 80,
                    stillingsprosentOffHeltUttak = 0,
                ),
                folketrygdUttaksgrad = 80,
                simuleringType = SimuleringTypeEnum.AFP_ETTERF_ALDER,
                foersteUttakDato = LocalDate.of(2025, 1, 1),
                heltUttakDato = LocalDate.of(2029, 1, 1),
                inntektEtterHeltUttakAntallAar = 0L
            )
        ) shouldBe
                listOf(
                    Simuleringsperiode(
                        fom = LocalDate.of(2025, 1, 1),
                        folketrygdUttaksgrad = 0,
                        stillingsprosentOffentlig = 80,
                        simulerAFPOffentligEtterfulgtAvAlder = true
                    ),
                    Simuleringsperiode(
                        fom = LocalDate.of(2027, 2, 1),
                        folketrygdUttaksgrad = 100,
                        stillingsprosentOffentlig = 0,
                        simulerAFPOffentligEtterfulgtAvAlder = true
                    ),
                )

    }

})

package no.nav.pensjon.simulator.api.nav.v1.acl.spec

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import no.nav.pensjon.simulator.api.nav.v1.acl.UttaksgradDto
import no.nav.pensjon.simulator.core.domain.SivilstatusType
import no.nav.pensjon.simulator.core.domain.regler.enum.AFPtypeEnum
import no.nav.pensjon.simulator.core.spec.Pre2025OffentligAfpSpec
import no.nav.pensjon.simulator.inntekt.InntektService
import no.nav.pensjon.simulator.person.GeneralPersonService
import no.nav.pensjon.simulator.testutil.TestObjects.pid
import no.nav.pensjon.simulator.testutil.TestObjects.tidsbegrensetOffentligAfpSimuleringSpec
import java.time.LocalDate
import java.time.YearMonth

class SimuleringSpecMapperForNavTest : ShouldSpec({

    context("tidsbegrenset offentlig AFP") {
        should("henter AFP-info fra info om gradert uttak") {
            SimuleringSpecMapperForNav(
                personService = arrangePerson(foedselsaarOgMaaned = YearMonth.of(1964, 1)),
                inntektService = arrangeInntekt(beloep = 199000),
                grunnbeloepService = mockk()
            ).fromDto(
                SimuleringSpecDto(
                    pid = pid.value,
                    sivilstatus = SivilstatusSpecDto.SKILT_PARTNER,
                    sisteInntekt = 500000,
                    simuleringstype = SimuleringstypeSpecDto.ALDERSPENSJON_MED_TIDSBEGRENSET_OFFENTLIG_AFP,
                    gradertUttak = GradertUttakSpecDto(
                        grad = UttaksgradDto.NULL,
                        uttakFomAlder = AlderSpecDto(aar = 63, maaneder = 0),
                        aarligInntekt = 250000
                    ),
                    heltUttak = HeltUttakSpecDto(
                        uttakFomAlder = AlderSpecDto(aar = 65, maaneder = 4),
                        aarligInntekt = 125000,
                        inntektTomAlder = AlderSpecDto(aar = 67, maaneder = 0) // 3 år med inntekt fra uttakFom-år
                    ),
                    offentligAfp = OffentligAfpSpecDto(
                        harInntektMaanedenFoerUttak = true,
                        afpOrdning = AfpOrdningTypeSpecDto.SPEKTER,
                        innvilgetLivsvarigAfp = null
                    )
                )
            ) shouldBe tidsbegrensetOffentligAfpSimuleringSpec(
                foedselsdato = LocalDate.of(1964, 1, 15),
                sivilstatus = SivilstatusType.SKPA,
                foersteUttakDato = LocalDate.of(2027, 2, 1),
                heltUttakDato = LocalDate.of(2029, 6, 1),
                forventetInntektBeloep = 500000,
                inntektUnderGradertUttakBeloep = 250000,
                inntektEtterHeltUttakBeloep = 125000,
                inntektEtterHeltUttakAntallAar = 3,
                pre2025OffentligAfp = Pre2025OffentligAfpSpec(
                    afpOrdning = AFPtypeEnum.NAVO,
                    inntektMaanedenFoerAfpUttakBeloep = 199000,
                    inntektUnderAfpUttakBeloep = 250000 // fra gradert uttak
                ),
                isOutputSimulertBeregningsinformasjonForAllKnekkpunkter = true
            )
        }

        should("bruker 'kommunal AFP' som default AFP-ordningstype") {
            SimuleringSpecMapperForNav(
                personService = arrangePerson(foedselsaarOgMaaned = YearMonth.of(1963, 2)),
                inntektService = arrangeInntekt(beloep = 198000),
                grunnbeloepService = mockk()
            ).fromDto(
                SimuleringSpecDto(
                    pid = pid.value,
                    sivilstatus = SivilstatusSpecDto.GJENLEVENDE_ETTER_SAMLIVSBRUDD,
                    sisteInntekt = 550000,
                    simuleringstype = SimuleringstypeSpecDto.ALDERSPENSJON_MED_TIDSBEGRENSET_OFFENTLIG_AFP,
                    gradertUttak = GradertUttakSpecDto(
                        grad = UttaksgradDto.NULL,
                        uttakFomAlder = AlderSpecDto(aar = 64, maaneder = 0),
                        aarligInntekt = 255000
                    ),
                    heltUttak = HeltUttakSpecDto(
                        uttakFomAlder = AlderSpecDto(aar = 65, maaneder = 11),
                        aarligInntekt = 129000,
                        inntektTomAlder = AlderSpecDto(aar = 68, maaneder = 2) // 4 år med inntekt fra uttakFom-år
                    ),
                    offentligAfp = OffentligAfpSpecDto(
                        harInntektMaanedenFoerUttak = true,
                        afpOrdning = null, // AFP-ordningstype ikke angitt
                        innvilgetLivsvarigAfp = null
                    )
                )
            ) shouldBe tidsbegrensetOffentligAfpSimuleringSpec(
                foedselsdato = LocalDate.of(1963, 2, 15),
                sivilstatus = SivilstatusType.GJES,
                foersteUttakDato = LocalDate.of(2027, 3, 1),
                heltUttakDato = LocalDate.of(2029, 2, 1),
                forventetInntektBeloep = 550000,
                inntektUnderGradertUttakBeloep = 255000,
                inntektEtterHeltUttakBeloep = 129000,
                inntektEtterHeltUttakAntallAar = 4,
                pre2025OffentligAfp = Pre2025OffentligAfpSpec(
                    afpOrdning = AFPtypeEnum.AFPKOM, // 'kommunal AFP' er default AFP-ordningstype
                    inntektMaanedenFoerAfpUttakBeloep = 198000,
                    inntektUnderAfpUttakBeloep = 255000
                ),
                isOutputSimulertBeregningsinformasjonForAllKnekkpunkter = true
            )
        }
    }
})

private fun arrangeInntekt(beloep: Int): InntektService =
    mockk {
        every { hentSisteMaanedsInntektOver1G(any()) } returns beloep
    }

private fun arrangePerson(foedselsaarOgMaaned: YearMonth): GeneralPersonService =
    mockk {
        every { foedselsdato(any()) } returns
                LocalDate.of(foedselsaarOgMaaned.year, foedselsaarOgMaaned.month, 15)
    }

package no.nav.pensjon.simulator.hybrid.api.samhandler.acl.v3

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.hybrid.*
import no.nav.pensjon.simulator.uttak.Uttaksgrad

class AlderspensjonOgPrivatAfpResultMapperV3Test : ShouldSpec({

    should("map problematic result to data transfer object") {
        AlderspensjonOgPrivatAfpResultMapperV3.toDto(
            AlderspensjonOgPrivatAfpResult(
                suksess = false,
                alderspensjonsperiodeListe = emptyList(),
                privatAfpPeriodeListe = emptyList(),
                harNaavaerendeUttak = false,
                harTidligereUttak = false,
                harLoependePrivatAfp = false,
                problem = Problem(
                    type = ProblemType.PERSON_FOR_HOEY_ALDER,
                    beskrivelse = "født før 1943"
                )
            )
        ) shouldBe AlderspensjonOgPrivatAfpResultV3(
            suksess = false,
            alderspensjonsperioder = emptyList(),
            privatAfpPerioder = emptyList(),
            harNaavaerendeUttak = false,
            harTidligereUttak = false,
            harLoependePrivatAfp = false,
            problem = ProblemV3(
                kode = ProblemTypeV3.PERSON_FOR_HOEY_ALDER,
                beskrivelse = "født før 1943"
            )
        )
    }

    should("map successful result to data transfer object") {
        AlderspensjonOgPrivatAfpResultMapperV3.toDto(
            AlderspensjonOgPrivatAfpResult(
                suksess = true,
                alderspensjonsperiodeListe = listOf(
                    Alderspensjonsperiode(
                        alderAar = 63,
                        beloep = 1,
                        fom = "2025-01-01",
                        uttaksperiodeListe = listOf(
                            Uttaksperiode(
                                startmaaned = 64,
                                uttaksgrad = Uttaksgrad.FEMTI_PROSENT
                            )
                        )
                    )
                ),
                privatAfpPeriodeListe = listOf(PrivatAfpPeriode(alderAar = 64, beloep = 1)),
                harNaavaerendeUttak = true,
                harTidligereUttak = true,
                harLoependePrivatAfp = true,
                problem = null
            )
        ) shouldBe AlderspensjonOgPrivatAfpResultV3(
            suksess = true,
            alderspensjonsperioder = listOf(
                ApOgPrivatAfpAlderspensjonsperiodeResultV3(
                    alder = 63,
                    beloep = 1,
                    datoFom = "2025-01-01",
                    uttaksperiode = listOf(
                        ApOgPrivatAfpUttaksperiodeResultV3(
                            startmaaned = 64,
                            uttaksgrad = 50
                        )
                    )
                )
            ),
            privatAfpPerioder = listOf(ApOgPrivatAfpPrivatAfpPeriodeResultV3(alder = 64, beloep = 1)),
            harNaavaerendeUttak = true,
            harTidligereUttak = true,
            harLoependePrivatAfp = true,
            problem = null
        )
    }
})

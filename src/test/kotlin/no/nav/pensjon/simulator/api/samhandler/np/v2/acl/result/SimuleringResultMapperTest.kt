package no.nav.pensjon.simulator.api.samhandler.np.v2.acl.result

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.orch.AlderspensjonOgPrivatAfpResult
import no.nav.pensjon.simulator.orch.Alderspensjonsperiode
import no.nav.pensjon.simulator.orch.PrivatAfpPeriode
import no.nav.pensjon.simulator.orch.Uttaksperiode
import no.nav.pensjon.simulator.uttak.Uttaksgrad
import no.nav.pensjon.simulator.validity.Problem
import no.nav.pensjon.simulator.validity.ProblemType

class SimuleringResultMapperTest : ShouldSpec({

    should("map problematic result to data transfer object") {
        SimuleringResultMapper.toDto(
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
        ) shouldBe SimuleringResultDto(
            suksess = false,
            alderspensjonsperioder = emptyList(),
            privatAfpPerioder = emptyList(),
            harNaavaerendeUttak = false,
            harTidligereUttak = false,
            harLoependePrivatAfp = false,
            problem = ProblemDto(
                kode = ProblemTypeDto.PERSON_FOR_HOEY_ALDER,
                beskrivelse = "født før 1943"
            )
        )
    }

    should("map successful result to data transfer object") {
        SimuleringResultMapper.toDto(
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
        ) shouldBe SimuleringResultDto(
            suksess = true,
            alderspensjonsperioder = listOf(
                ApOgPrivatAfpAlderspensjonsperiodeResultDto(
                    alder = 63,
                    beloep = 1,
                    datoFom = "2025-01-01",
                    uttaksperiode = listOf(
                        ApOgPrivatAfpUttaksperiodeResultDto(
                            startmaaned = 64,
                            uttaksgrad = 50
                        )
                    )
                )
            ),
            privatAfpPerioder = listOf(ApOgPrivatAfpPrivatAfpPeriodeResultDto(alder = 64, beloep = 1)),
            harNaavaerendeUttak = true,
            harTidligereUttak = true,
            harLoependePrivatAfp = true,
            problem = null
        )
    }
})

package no.nav.pensjon.simulator.orch

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import no.nav.pensjon.simulator.core.SimulatorCore
import no.nav.pensjon.simulator.core.exception.*
import no.nav.pensjon.simulator.core.krav.FremtidigInntekt
import no.nav.pensjon.simulator.core.result.SimulatorOutput
import no.nav.pensjon.simulator.tech.validation.InvalidEnumValueException
import no.nav.pensjon.simulator.tech.web.BadRequestException
import no.nav.pensjon.simulator.tech.web.EgressException
import no.nav.pensjon.simulator.testutil.TestObjects.simuleringSpec
import no.nav.pensjon.simulator.validity.Problem
import no.nav.pensjon.simulator.validity.ProblemType
import no.nav.pensjon.simulator.ytelse.YtelseService
import java.time.LocalDate
import java.time.format.DateTimeParseException

class AlderspensjonOgPrivatAfpServiceTest : ShouldSpec({

    val today = LocalDate.of(2024, 1, 1)

    fun validSpec() = simuleringSpec(foersteUttakDato = LocalDate.of(2029, 1, 1))

    fun serviceWithThrowingSimulator(exception: Exception) =
        AlderspensjonOgPrivatAfpService(
            mockk<SimulatorCore> { every { simuler(any()) } throws exception },
            mockk(),
            mockk()
        ) { today }

    fun errorResult(problemType: ProblemType, beskrivelse: String) =
        AlderspensjonOgPrivatAfpResult(
            suksess = false,
            alderspensjonsperiodeListe = emptyList(),
            privatAfpPeriodeListe = emptyList(),
            harNaavaerendeUttak = false,
            harTidligereUttak = false,
            harLoependePrivatAfp = false,
            problem = Problem(problemType, beskrivelse)
        )

    context("happy path") {
        should("return result from resultPreparer on success") {
            val spec = validSpec()
            val simulatorOutput = SimulatorOutput()
            val expectedResult = AlderspensjonOgPrivatAfpResult(
                suksess = true,
                alderspensjonsperiodeListe = emptyList(),
                privatAfpPeriodeListe = emptyList(),
                harNaavaerendeUttak = false,
                harTidligereUttak = false,
                harLoependePrivatAfp = true
            )
            val simulatorCore = mockk<SimulatorCore>()
            val ytelseService = mockk<YtelseService>()
            val resultPreparer = mockk<AlderspensjonOgPrivatAfpResultPreparer>()
            every { simulatorCore.simuler(any()) } returns simulatorOutput
            every { ytelseService.getLoependeYtelser(any()) } returns mockk {
                every { privatAfpVirkningFom } returns LocalDate.of(2029, 1, 1)
            }
            every {
                resultPreparer.result(
                    simulatorOutput,
                    spec.pid!!,
                    harLoependePrivatAfp = true
                )
            } returns expectedResult

            val result = AlderspensjonOgPrivatAfpService(simulatorCore, ytelseService, resultPreparer) { today }
                .simuler(spec)

            result shouldBe expectedResult
        }

        should("pass harLoependePrivatAfp=true when privatAfpVirkningFom is not null") {
            val harLoependeAfpSlot = slot<Boolean>()
            val simulatorCore = mockk<SimulatorCore> { every { simuler(any()) } returns SimulatorOutput() }
            val ytelseService = mockk<YtelseService> {
                every { getLoependeYtelser(any()) } returns mockk {
                    every { privatAfpVirkningFom } returns LocalDate.of(2029, 1, 1)
                }
            }
            val resultPreparer = mockk<AlderspensjonOgPrivatAfpResultPreparer> {
                every { result(any(), any(), capture(harLoependeAfpSlot)) } returns mockk()
            }

            AlderspensjonOgPrivatAfpService(simulatorCore, ytelseService, resultPreparer) { today }
                .simuler(validSpec())

            harLoependeAfpSlot.captured shouldBe true
        }

        should("pass harLoependePrivatAfp=false when privatAfpVirkningFom is null") {
            val harLoependeAfpSlot = slot<Boolean>()
            val simulatorCore = mockk<SimulatorCore> { every { simuler(any()) } returns SimulatorOutput() }
            val ytelseService = mockk<YtelseService> {
                every { getLoependeYtelser(any()) } returns mockk {
                    every { privatAfpVirkningFom } returns null
                }
            }
            val resultPreparer = mockk<AlderspensjonOgPrivatAfpResultPreparer> {
                every { result(any(), any(), capture(harLoependeAfpSlot)) } returns mockk()
            }

            AlderspensjonOgPrivatAfpService(simulatorCore, ytelseService, resultPreparer) { today }
                .simuler(validSpec())

            harLoependeAfpSlot.captured shouldBe false
        }
    }

    context("feil i spesifikasjonen") {
        should("return UGYLDIG_UTTAKSDATO problem when foersteUttakDato is before today") {
            val spec = simuleringSpec(foersteUttakDato = LocalDate.of(2023, 1, 1))

            val result = AlderspensjonOgPrivatAfpService(mockk(), mockk(), mockk()) { today }
                .simuler(spec)

            result shouldBe errorResult(
                ProblemType.UGYLDIG_UTTAKSDATO,
                "Dato for første uttak (2023-01-01) er for tidlig"
            )
        }

        should("return UGYLDIG_UTTAKSDATO problem when foersteUttakDato is null") {
            val spec = simuleringSpec(foersteUttakDato = null)

            val result = AlderspensjonOgPrivatAfpService(mockk(), mockk(), mockk()) { today }
                .simuler(spec)

            result shouldBe errorResult(ProblemType.UGYLDIG_UTTAKSDATO, "Dato for første uttak mangler")
        }

        should("return UGYLDIG_INNTEKT problem when fremtidig inntekt has negative beloep") {
            val spec = simuleringSpec(
                foersteUttakDato = LocalDate.of(2029, 1, 1),
                inntektSpecListe = listOf(
                    FremtidigInntekt(aarligInntektBeloep = -1, fom = LocalDate.of(2029, 1, 1))
                )
            )

            val result = AlderspensjonOgPrivatAfpService(mockk(), mockk(), mockk()) { today }
                .simuler(spec)

            result shouldBe errorResult(ProblemType.UGYLDIG_INNTEKT, "En fremtidig inntekt har negativt beløp")
        }
    }

    context("ukategorisert klientfeil") {
        should("return ANNEN_KLIENTFEIL problem on BadRequestException") {
            val result = serviceWithThrowingSimulator(BadRequestException("bad request"))
                .simuler(validSpec())

            result shouldBe errorResult(ProblemType.ANNEN_KLIENTFEIL, "bad request")
        }

        should("return ANNEN_KLIENTFEIL problem on DateTimeParseException") {
            val result = serviceWithThrowingSimulator(DateTimeParseException("parse error", "text", 0))
                .simuler(validSpec())

            result shouldBe errorResult(ProblemType.ANNEN_KLIENTFEIL, "parse error")
        }

        should("return ANNEN_KLIENTFEIL problem on FeilISimuleringsgrunnlagetException") {
            val result = serviceWithThrowingSimulator(FeilISimuleringsgrunnlagetException("feil i grunnlag"))
                .simuler(validSpec())

            result shouldBe errorResult(ProblemType.ANNEN_KLIENTFEIL, "feil i grunnlag")
        }

        should("return ANNEN_KLIENTFEIL problem on InvalidArgumentException") {
            val result = serviceWithThrowingSimulator(InvalidArgumentException("ugyldig argument"))
                .simuler(validSpec())

            result shouldBe errorResult(ProblemType.ANNEN_KLIENTFEIL, "ugyldig argument")
        }

        should("return ANNEN_KLIENTFEIL problem on InvalidEnumValueException") {
            val result = serviceWithThrowingSimulator(InvalidEnumValueException("ugyldig enum"))
                .simuler(validSpec())

            result shouldBe errorResult(ProblemType.ANNEN_KLIENTFEIL, "ugyldig enum")
        }

        should("return ANNEN_KLIENTFEIL problem on KanIkkeBeregnesException") {
            val result = serviceWithThrowingSimulator(KanIkkeBeregnesException("kan ikke beregnes"))
                .simuler(validSpec())

            result shouldBe errorResult(ProblemType.ANNEN_KLIENTFEIL, "kan ikke beregnes")
        }

        should("return ANNEN_KLIENTFEIL problem on KonsistensenIGrunnlagetErFeilException") {
            val cause = RuntimeException("inkonsistent")
            val result = serviceWithThrowingSimulator(KonsistensenIGrunnlagetErFeilException(cause))
                .simuler(validSpec())

            result.suksess shouldBe false
            result.problem?.type shouldBe ProblemType.ANNEN_KLIENTFEIL
        }

        should("return ANNEN_KLIENTFEIL problem on PersonForUngException") {
            val result = serviceWithThrowingSimulator(PersonForUngException("for ung"))
                .simuler(validSpec())

            result shouldBe errorResult(ProblemType.ANNEN_KLIENTFEIL, "for ung")
        }

        should("return ANNEN_KLIENTFEIL problem on RegelmotorValideringException") {
            val result = serviceWithThrowingSimulator(RegelmotorValideringException("validering feilet"))
                .simuler(validSpec())

            result shouldBe errorResult(ProblemType.ANNEN_KLIENTFEIL, "validering feilet")
        }
    }

    context("serverfeil") {
        should("return SERVERFEIL problem on EgressException") {
            val result = serviceWithThrowingSimulator(EgressException("egress error"))
                .simuler(validSpec())

            result shouldBe errorResult(ProblemType.SERVERFEIL, "egress error")
        }

        should("return SERVERFEIL problem on ImplementationUnrecoverableException") {
            val result = serviceWithThrowingSimulator(ImplementationUnrecoverableException("implementation error"))
                .simuler(validSpec())

            result shouldBe errorResult(ProblemType.SERVERFEIL, "implementation error")
        }
    }

    context("kategorisert feilsituasjon") {
        should("return PERSON_FOR_HOEY_ALDER problem on PersonForGammelException") {
            val result = serviceWithThrowingSimulator(PersonForGammelException("for gammel"))
                .simuler(validSpec())

            result shouldBe errorResult(ProblemType.PERSON_FOR_HOEY_ALDER, "for gammel")
        }

        should("return UTILSTREKKELIG_OPPTJENING problem on UtilstrekkeligOpptjeningException") {
            val result = serviceWithThrowingSimulator(UtilstrekkeligOpptjeningException("for lav opptjening"))
                .simuler(validSpec())

            result shouldBe errorResult(ProblemType.UTILSTREKKELIG_OPPTJENING, "for lav opptjening")
        }

        should("return UTILSTREKKELIG_TRYGDETID problem on UtilstrekkeligTrygdetidException") {
            val result = serviceWithThrowingSimulator(UtilstrekkeligTrygdetidException())
                .simuler(validSpec())

            result.suksess shouldBe false
            result.problem?.type shouldBe ProblemType.UTILSTREKKELIG_TRYGDETID
            result.problem?.beskrivelse shouldBe "Ukjent feil - UtilstrekkeligTrygdetidException"
        }
    }

    context("bad request") {
        should("return error result with empty lists and false flags") {
            val result = serviceWithThrowingSimulator(BadRequestException("error"))
                .simuler(validSpec())

            result.suksess shouldBe false
            result.alderspensjonsperiodeListe shouldBe emptyList()
            result.privatAfpPeriodeListe shouldBe emptyList()
            result.harNaavaerendeUttak shouldBe false
            result.harTidligereUttak shouldBe false
            result.harLoependePrivatAfp shouldBe false
        }
    }
})

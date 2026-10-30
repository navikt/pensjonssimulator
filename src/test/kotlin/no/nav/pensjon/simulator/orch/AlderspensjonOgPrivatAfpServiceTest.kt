package no.nav.pensjon.simulator.orch

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.mockk.CapturingSlot
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
import no.nav.pensjon.simulator.validity.InternDataInkonsistensException
import no.nav.pensjon.simulator.validity.Problem
import no.nav.pensjon.simulator.validity.ProblemType
import no.nav.pensjon.simulator.ytelse.YtelseService
import org.springframework.http.HttpStatus
import java.time.LocalDate
import java.time.format.DateTimeParseException

class AlderspensjonOgPrivatAfpServiceTest : ShouldSpec({

    context("happy path") {
        should("return result from resultPreparer on success") {
            val spec = validSpec
            val simulatorOutput = SimulatorOutput()
            val expectedResult = AlderspensjonOgPrivatAfpResult(
                suksess = true,
                alderspensjonsperiodeListe = emptyList(),
                privatAfpPeriodeListe = emptyList(),
                harNaavaerendeUttak = false,
                harTidligereUttak = false,
                harLoependePrivatAfp = true
            )
            val resultPreparer: AlderspensjonOgPrivatAfpResultPreparer = mockk {
                every {
                    result(simulatorOutput, pid = spec.pid!!, harLoependePrivatAfp = true, uttakListe = emptyList())
                } returns expectedResult
            }

            AlderspensjonOgPrivatAfpService(
                simulatorCore = arrangeCore(simulatorOutput),
                ytelseService = arrangePrivatAfpYtelse(dato = LocalDate.of(2029, 1, 1)),
                resultPreparer,
                kravService = mockk(relaxed = true)
            ) { today }.simuler(spec) shouldBe expectedResult
        }

        should("pass harLoependePrivatAfp=true when privatAfpVirkningFom is not null") {
            val harLoependeAfpSlot = slot<Boolean>()

            AlderspensjonOgPrivatAfpService(
                simulatorCore = arrangeCore(),
                ytelseService = arrangePrivatAfpYtelse(dato = LocalDate.of(2029, 1, 1)),
                resultPreparer = arrangeResultPreparer(harLoependeAfpSlot),
                kravService = mockk(relaxed = true)
            ) { today }.simuler(validSpec)

            harLoependeAfpSlot.captured shouldBe true
        }

        should("pass harLoependePrivatAfp=false when privatAfpVirkningFom is null") {
            val harLoependeAfpSlot = slot<Boolean>()

            AlderspensjonOgPrivatAfpService(
                simulatorCore = arrangeCore(),
                ytelseService = arrangePrivatAfpYtelse(dato = null),
                resultPreparer = arrangeResultPreparer(harLoependeAfpSlot),
                kravService = mockk(relaxed = true)
            ) { today }.simuler(validSpec)

            harLoependeAfpSlot.captured shouldBe false
        }
    }

    context("feil i spesifikasjonen") {
        context("første uttaksdato er før dagens dato") {
            should("inkludere problembeskrivelse i returverdien") {
                val spec = simuleringSpec(foersteUttakDato = LocalDate.of(2023, 1, 1))

                goodService.simuler(spec) shouldBe errorResult(
                    problemType = ProblemType.UGYLDIG_UTTAKSDATO,
                    beskrivelse = "Dato for første uttak (2023-01-01) er for tidlig"
                )
            }
        }

        context("første uttaksdato er ikke angitt") {
            should("inkludere problembeskrivelse i returverdien") {
                val spec = simuleringSpec(foersteUttakDato = null)

                goodService.simuler(spec) shouldBe errorResult(
                    problemType = ProblemType.UGYLDIG_UTTAKSDATO,
                    beskrivelse = "Dato for første uttak mangler"
                )
            }
        }

        context("inntekt har negativt beløp") {
            should("inkludere problembeskrivelse i returverdien") {
                val spec = simuleringSpec(
                    foersteUttakDato = LocalDate.of(2029, 1, 1),
                    inntektSpecListe = listOf(
                        FremtidigInntekt(
                            aarligInntektBeloep = -1,
                            fom = LocalDate.of(2029, 1, 1)
                        )
                    )
                )

                goodService.simuler(spec) shouldBe errorResult(
                    problemType = ProblemType.UGYLDIG_INNTEKT,
                    beskrivelse = "En fremtidig inntekt har negativt beløp"
                )
            }
        }
    }

    context("ukategorisert klientfeil") {
        should("return ANNEN_KLIENTFEIL problem on BadRequestException") {
            badService(simulatorException = BadRequestException("bad request"))
                .simuler(validSpec) shouldBe
                    errorResult(
                        problemType = ProblemType.ANNEN_KLIENTFEIL,
                        beskrivelse = "BadRequestException"
                    )
        }

        should("return ANNEN_KLIENTFEIL problem on DateTimeParseException") {
            badService(simulatorException = DateTimeParseException("parse error", "text", 0))
                .simuler(validSpec) shouldBe
                    errorResult(
                        problemType = ProblemType.ANNEN_KLIENTFEIL,
                        beskrivelse = "parse error"
                    )
        }

        should("return ANNEN_KLIENTFEIL problem on InvalidArgumentException") {
            badService(simulatorException = InvalidArgumentException("ugyldig argument"))
                .simuler(validSpec) shouldBe
                    errorResult(
                        problemType = ProblemType.ANNEN_KLIENTFEIL,
                        beskrivelse = "InvalidArgumentException"
                    )
        }

        should("return ANNEN_KLIENTFEIL problem on InvalidEnumValueException") {
            badService(simulatorException = InvalidEnumValueException("ugyldig enum"))
                .simuler(validSpec) shouldBe
                    errorResult(
                        problemType = ProblemType.ANNEN_KLIENTFEIL,
                        beskrivelse = "InvalidEnumValueException"
                    )
        }

        should("return ANNEN_KLIENTFEIL problem on PersonForUngException") {
            badService(simulatorException = PersonForUngException("for ung"))
                .simuler(validSpec) shouldBe
                    errorResult(
                        problemType = ProblemType.ANNEN_KLIENTFEIL,
                        beskrivelse = "PersonForUngException"
                    )
        }

        should("return REGELBRUDD problem on RegelmotorValideringException") {
            badService(simulatorException = RegelmotorValideringException("validering feilet"))
                .simuler(validSpec) shouldBe
                    errorResult(
                        problemType = ProblemType.REGELBRUDD,
                        beskrivelse = "RegelmotorValideringException"
                    )
        }
    }

    context("serverfeil") {
        should("return ANNEN_SERVERFEIL problem on EgressException due to error in application acting as client") {
            badService(simulatorException = EgressException("egress error", statusCode = HttpStatus.BAD_REQUEST))
                .simuler(validSpec) shouldBe
                    errorResult(
                        problemType = ProblemType.ANNEN_SERVERFEIL,
                        beskrivelse = "EgressException"
                    )
        }

        should("return TREDJEPARTSFEIL problem on EgressException due to error in remote server") {
            badService(
                simulatorException = EgressException(
                    message = "egress error",
                    statusCode = HttpStatus.INTERNAL_SERVER_ERROR
                )
            ).simuler(validSpec) shouldBe
                    errorResult(
                        problemType = ProblemType.TREDJEPARTSFEIL,
                        beskrivelse = "EgressException"
                    )
        }

        should("return INTERN_DATA_INKONSISTENS problem on InternDataInkonsistensException") {
            badService(simulatorException = InternDataInkonsistensException("data error"))
                .simuler(validSpec) shouldBe
                    errorResult(
                        problemType = ProblemType.INTERN_DATA_INKONSISTENS,
                        beskrivelse = "InternDataInkonsistensException"
                    )
        }
    }

    context("kategorisert feilsituasjon") {
        should("return PERSON_FOR_HOEY_ALDER problem on PersonForGammelException") {
            badService(simulatorException = PersonForGammelException("for gammel"))
                .simuler(validSpec) shouldBe
                    errorResult(
                        problemType = ProblemType.PERSON_FOR_HOEY_ALDER,
                        beskrivelse = "PersonForGammelException"
                    )
        }

        should("return UTILSTREKKELIG_OPPTJENING problem on UtilstrekkeligOpptjeningException") {
            badService(simulatorException = UtilstrekkeligOpptjeningException("for lav opptjening"))
                .simuler(validSpec) shouldBe
                    errorResult(
                        problemType = ProblemType.UTILSTREKKELIG_OPPTJENING,
                        beskrivelse = "UtilstrekkeligOpptjeningException"
                    )
        }

        should("return UTILSTREKKELIG_TRYGDETID problem on UtilstrekkeligTrygdetidException") {
            val result = badService(simulatorException = UtilstrekkeligTrygdetidException()).simuler(validSpec)

            with(result) {
                suksess shouldBe false
                problem?.type shouldBe ProblemType.UTILSTREKKELIG_TRYGDETID
                problem?.beskrivelse shouldBe "UtilstrekkeligTrygdetidException"
            }
        }
    }

    context("bad request") {
        should("return error result with empty lists and false flags") {
            val result = badService(simulatorException = BadRequestException("error")).simuler(validSpec)

            with(result) {
                suksess shouldBe false
                alderspensjonsperiodeListe shouldBe emptyList()
                privatAfpPeriodeListe shouldBe emptyList()
                harNaavaerendeUttak shouldBe false
                harTidligereUttak shouldBe false
                harLoependePrivatAfp shouldBe false
            }
        }
    }
})

private val today = LocalDate.of(2024, 1, 1)

private val validSpec = simuleringSpec(foersteUttakDato = LocalDate.of(2029, 1, 1))

private val goodService =
    AlderspensjonOgPrivatAfpService(
        simulatorCore = mockk(),
        ytelseService = mockk(),
        resultPreparer = mockk(),
        kravService = mockk(relaxed = true)
    ) { today }

private fun badService(simulatorException: Exception) =
    AlderspensjonOgPrivatAfpService(
        simulatorCore = mockk<SimulatorCore> { every { simuler(any()) } throws simulatorException },
        ytelseService = mockk(relaxed = true),
        resultPreparer = mockk(),
        kravService = mockk(relaxed = true)
    ) { today }

private fun arrangeCore(output: SimulatorOutput = SimulatorOutput()): SimulatorCore =
    mockk { every { simuler(any()) } returns output }

private fun arrangeResultPreparer(harLoependeAfpSlot: CapturingSlot<Boolean>): AlderspensjonOgPrivatAfpResultPreparer =
    mockk {
        every {
            result(
                simulatorOutput = any(),
                pid = any(),
                harLoependePrivatAfp = capture(harLoependeAfpSlot),
                uttakListe = any()
            )
        } returns mockk()
    }

private fun arrangePrivatAfpYtelse(dato: LocalDate?): YtelseService =
    mockk {
        every { getLoependeYtelser(any()) } returns mockk {
            every { privatAfpVirkningFom } returns dato
        }
        every { getPrivatAfpFoersteVirkningsdato(any()) } returns dato
    }

private fun errorResult(problemType: ProblemType, beskrivelse: String) =
    AlderspensjonOgPrivatAfpResult(
        suksess = false,
        alderspensjonsperiodeListe = emptyList(),
        privatAfpPeriodeListe = emptyList(),
        harNaavaerendeUttak = false,
        harTidligereUttak = false,
        harLoependePrivatAfp = false,
        problem = Problem(problemType, beskrivelse)
    )
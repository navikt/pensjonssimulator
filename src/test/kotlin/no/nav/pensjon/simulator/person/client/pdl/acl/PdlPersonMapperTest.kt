package no.nav.pensjon.simulator.person.client.pdl.acl

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.core.domain.regler.enum.LandkodeEnum
import no.nav.pensjon.simulator.person.Person
import no.nav.pensjon.simulator.person.Sivilstandstype
import java.time.LocalDate

class PdlPersonMapperTest : FunSpec({

    context("fromDto") {

        test("pick first in lists of fødselsdato, sivilstand, statsborgerskap") {
            PdlPersonMapper.fromDto(
                PdlPersonResult(
                    data = PdlPersonEnvelope(
                        hentPerson = PdlPerson(
                            foedselsdato = listOf(
                                PdlFoedselsdato(foedselsdato = LocalDate.of(1963, 4, 5)),
                                PdlFoedselsdato(foedselsdato = LocalDate.of(1963, 4, 3))
                            ),
                            sivilstand = listOf(
                                PdlSivilstand(type = "UGIFT"),
                                PdlSivilstand(type = "GIFT")
                            ),
                            statsborgerskap = listOf(
                                PdlStatsborgerskap(land = "LAO"),
                                PdlStatsborgerskap(land = "NOR")
                            )
                        )
                    ),
                    extensions = null,
                    errors = null
                )
            ) shouldBe Person(
                foedselsdato = LocalDate.of(1963, 4, 5),
                sivilstand = Sivilstandstype.UGIFT,
                statsborgerskap = LandkodeEnum.LAO
            )
        }

        test("returnerer fødselsdato fra gyldig respons") {
            val result = PdlPersonMapper.fromDto(personResult(foedselsdato = LocalDate.of(1990, 5, 15)))

            result?.foedselsdato shouldBe LocalDate.of(1990, 5, 15)
        }

        test("returnerer første fødselsdato når flere finnes") {
            val dto = PdlPersonResult(
                data = PdlPersonEnvelope(
                    hentPerson = PdlPerson(
                        foedselsdato = listOf(
                            PdlFoedselsdato(LocalDate.of(1963, 4, 5)),
                            PdlFoedselsdato(LocalDate.of(1963, 4, 3)),
                            PdlFoedselsdato(LocalDate.of(1992, 7, 17))
                        ),
                        sivilstand = null,
                        statsborgerskap = null
                    )
                ),
                extensions = null,
                errors = null
            )

            val result = PdlPersonMapper.fromDto(dto)

            result?.foedselsdato shouldBe LocalDate.of(1963, 4, 5)
        }

        test("returnerer null når data er null") {
            val dto = PdlPersonResult(
                data = null,
                extensions = null,
                errors = null
            )

            val result = PdlPersonMapper.fromDto(dto)

            result shouldBe null
        }

        test("returnerer null når hentPerson er null") {
            val dto = PdlPersonResult(
                data = PdlPersonEnvelope(hentPerson = null),
                extensions = null,
                errors = null
            )

            val result = PdlPersonMapper.fromDto(dto)

            result shouldBe null
        }

        test("returnerer Person med null fødselsdato når foedselsdato-liste er null") {
            val dto = PdlPersonResult(
                data = PdlPersonEnvelope(
                    hentPerson = PdlPerson(foedselsdato = null, sivilstand = null, statsborgerskap = null)
                ),
                extensions = null,
                errors = null
            )

            val result = PdlPersonMapper.fromDto(dto)

            result?.foedselsdato shouldBe null
        }

        test("returnerer Person med null fødselsdato når foedselsdato-liste er tom") {
            val dto = PdlPersonResult(
                data = PdlPersonEnvelope(
                    hentPerson = PdlPerson(foedselsdato = emptyList(), sivilstand = null, statsborgerskap = null)
                ),
                extensions = null,
                errors = null
            )

            val result = PdlPersonMapper.fromDto(dto)

            result?.foedselsdato shouldBe null
        }

        test("returnerer Person med null fødselsdato når første fødselsdato i listen er null") {
            val dto = PdlPersonResult(
                data = PdlPersonEnvelope(
                    hentPerson = PdlPerson(
                        foedselsdato = listOf(PdlFoedselsdato(foedselsdato = null)),
                        sivilstand = null,
                        statsborgerskap = null
                    )
                ),
                extensions = null,
                errors = null
            )

            val result = PdlPersonMapper.fromDto(dto)

            result?.foedselsdato shouldBe null
        }

        test("håndterer respons med errors men gyldig data") {
            val dto = PdlPersonResult(
                data = PdlPersonEnvelope(
                    hentPerson = PdlPerson(
                        foedselsdato = listOf(PdlFoedselsdato(LocalDate.of(1985, 3, 20))),
                        sivilstand = null,
                        statsborgerskap = null
                    )
                ),
                extensions = null,
                errors = listOf(PdlError("Some warning message"))
            )

            val result = PdlPersonMapper.fromDto(dto)

            result?.foedselsdato shouldBe LocalDate.of(1985, 3, 20)
        }

        test("håndterer respons med tom errors-liste") {
            val result = PdlPersonMapper.fromDto(personResult(foedselsdato = LocalDate.of(1985, 3, 20), errors = emptyList()))

            result?.foedselsdato shouldBe LocalDate.of(1985, 3, 20)
        }

        test("håndterer respons med extensions") {
            val dto = PdlPersonResult(
                data = PdlPersonEnvelope(
                    hentPerson = PdlPerson(
                        foedselsdato = listOf(PdlFoedselsdato(LocalDate.of(1985, 3, 20))),
                        sivilstand = null,
                        statsborgerskap = null
                    )
                ),
                extensions = PdlExtensions(
                    warnings = listOf(
                        PdlWarning(
                            query = "hentPerson",
                            id = "123",
                            code = "deprecated",
                            message = "Field is deprecated",
                            details = null
                        )
                    )
                ),
                errors = null
            )

            val result = PdlPersonMapper.fromDto(dto)

            result?.foedselsdato shouldBe LocalDate.of(1985, 3, 20)
        }

        test("håndterer skuddårsdato") {
            val result = PdlPersonMapper.fromDto(personResult(foedselsdato = LocalDate.of(2000, 2, 29)))

            result?.foedselsdato shouldBe LocalDate.of(2000, 2, 29)
        }

        test("håndterer ulike datoer") {
            val testDates = listOf(
                LocalDate.of(1950, 1, 1),
                LocalDate.of(2024, 12, 31),
                LocalDate.of(1975, 6, 15),
                LocalDate.of(2000, 2, 29)
            )

            testDates.forEach { expectedDate ->
                val result = PdlPersonMapper.fromDto(personResult(foedselsdato = expectedDate))

                result?.foedselsdato shouldBe expectedDate
            }
        }

        test("returnerer null ved kun error uten data") {
            val dto = PdlPersonResult(
                data = null,
                extensions = null,
                errors = listOf(PdlError("Person ikke funnet"))
            )

            val result = PdlPersonMapper.fromDto(dto)

            result shouldBe null
        }

        test("håndterer flere errors") {
            val dto = PdlPersonResult(
                data = PdlPersonEnvelope(
                    hentPerson = PdlPerson(
                        foedselsdato = listOf(PdlFoedselsdato(LocalDate.of(1990, 5, 15))),
                        sivilstand = null,
                        statsborgerskap = null
                    )
                ),
                extensions = null,
                errors = listOf(
                    PdlError("First error"),
                    PdlError("Second error"),
                    PdlError("Third error")
                )
            )

            val result = PdlPersonMapper.fromDto(dto)

            result?.foedselsdato shouldBe LocalDate.of(1990, 5, 15)
        }

        test("håndterer extensions med tom warnings-liste") {
            val result = PdlPersonMapper.fromDto(
                personResult(
                    foedselsdato = LocalDate.of(1990, 5, 15),
                    extensions = PdlExtensions(warnings = emptyList())
                )
            )

            result?.foedselsdato shouldBe LocalDate.of(1990, 5, 15)
        }

        test("håndterer extensions med null warnings") {
            val result = PdlPersonMapper.fromDto(
                personResult(
                    foedselsdato = LocalDate.of(1990, 5, 15),
                    extensions = PdlExtensions(warnings = null)
                )
            )

            result?.foedselsdato shouldBe LocalDate.of(1990, 5, 15)
        }

        test("mapper sivilstand GIFT") {
            val dto = PdlPersonResult(
                data = PdlPersonEnvelope(
                    hentPerson = PdlPerson(
                        foedselsdato = listOf(PdlFoedselsdato(LocalDate.of(1990, 1, 1))),
                        sivilstand = listOf(PdlSivilstand(type = "GIFT")),
                        statsborgerskap = null
                    )
                ),
                extensions = null,
                errors = null
            )

            val result = PdlPersonMapper.fromDto(dto)

            result?.sivilstand shouldBe Sivilstandstype.GIFT
        }

        test("mapper statsborgerskap NOR") {
            val dto = PdlPersonResult(
                data = PdlPersonEnvelope(
                    hentPerson = PdlPerson(
                        foedselsdato = listOf(PdlFoedselsdato(LocalDate.of(1990, 1, 1))),
                        sivilstand = null,
                        statsborgerskap = listOf(PdlStatsborgerskap(land = "NOR"))
                    )
                ),
                extensions = null,
                errors = null
            )

            val result = PdlPersonMapper.fromDto(dto)

            result?.statsborgerskap shouldBe LandkodeEnum.NOR
        }

        test("returnerer UOPPGITT sivilstand når sivilstand-liste er null") {
            val dto = PdlPersonResult(
                data = PdlPersonEnvelope(
                    hentPerson = PdlPerson(
                        foedselsdato = listOf(PdlFoedselsdato(LocalDate.of(1990, 1, 1))),
                        sivilstand = null,
                        statsborgerskap = null
                    )
                ),
                extensions = null,
                errors = null
            )

            val result = PdlPersonMapper.fromDto(dto)

            result?.sivilstand shouldBe Sivilstandstype.UOPPGITT
        }

        test("returnerer P_UKJENT statsborgerskap når statsborgerskap-liste er null") {
            val dto = PdlPersonResult(
                data = PdlPersonEnvelope(
                    hentPerson = PdlPerson(
                        foedselsdato = listOf(PdlFoedselsdato(LocalDate.of(1990, 1, 1))),
                        sivilstand = null,
                        statsborgerskap = null
                    )
                ),
                extensions = null,
                errors = null
            )

            val result = PdlPersonMapper.fromDto(dto)

            result?.statsborgerskap shouldBe LandkodeEnum.P_UKJENT
        }
    }
})

private fun personResult(
    foedselsdato: LocalDate,
    extensions: PdlExtensions? = null,
    errors: List<PdlError>? = null
) = PdlPersonResult(
    data = PdlPersonEnvelope(
        hentPerson = PdlPerson(
            foedselsdato = listOf(PdlFoedselsdato(foedselsdato)),
            sivilstand = null,
            statsborgerskap = null
        )
    ),
    extensions = extensions,
    errors = errors
)

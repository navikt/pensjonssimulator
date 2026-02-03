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
                                PdlFoedselsdato(foedselsdato = LocalDate.of(1963, 4, 5)), // first in the list
                                PdlFoedselsdato(foedselsdato = LocalDate.of(1963, 4, 3))
                            ),
                            sivilstand = listOf(
                                PdlSivilstand(type = "UGIFT"), // first in the list
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
            val dto = PdlPersonResult(
                data = PdlPersonEnvelope(
                    hentPerson = PdlPerson(
                        foedselsdato = listOf(
                            PdlFoedselsdato(LocalDate.of(1990, 5, 15))
                        )
                    )
                ),
                extensions = null,
                errors = null
            )

            val result = PdlPersonMapper.fromDto(dto)

            result shouldBe LocalDate.of(1990, 5, 15)
        }

        test("returnerer første fødselsdato når flere finnes") {
            val dto = PdlPersonResult(
                data = PdlPersonEnvelope(
                    hentPerson = PdlPerson(
                        foedselsdato = listOf(
                            PdlFoedselsdato(LocalDate.of(1963, 4, 5)),
                            PdlFoedselsdato(LocalDate.of(1963, 4, 3)),
                            PdlFoedselsdato(LocalDate.of(1992, 7, 17))
                        )
                    )
                ),
                extensions = null,
                errors = null
            )

            val result = PdlPersonMapper.fromDto(dto)

            result shouldBe LocalDate.of(1963, 4, 5)
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

        test("returnerer null når foedselsdato-liste er null") {
            val dto = PdlPersonResult(
                data = PdlPersonEnvelope(
                    hentPerson = PdlPerson(foedselsdato = null)
                ),
                extensions = null,
                errors = null
            )

            val result = PdlPersonMapper.fromDto(dto)

            result shouldBe null
        }

        test("returnerer null når foedselsdato-liste er tom") {
            val dto = PdlPersonResult(
                data = PdlPersonEnvelope(
                    hentPerson = PdlPerson(foedselsdato = emptyList())
                ),
                extensions = null,
                errors = null
            )

            val result = PdlPersonMapper.fromDto(dto)

            result shouldBe null
        }

        test("returnerer null når første fødselsdato i listen er null") {
            val dto = PdlPersonResult(
                data = PdlPersonEnvelope(
                    hentPerson = PdlPerson(
                        foedselsdato = listOf(
                            PdlFoedselsdato(foedselsdato = null)
                        )
                    )
                ),
                extensions = null,
                errors = null
            )

            val result = PdlPersonMapper.fromDto(dto)

            result shouldBe null
        }

        test("håndterer respons med errors men gyldig data") {
            val dto = PdlPersonResult(
                data = PdlPersonEnvelope(
                    hentPerson = PdlPerson(
                        foedselsdato = listOf(
                            PdlFoedselsdato(LocalDate.of(1985, 3, 20))
                        )
                    )
                ),
                extensions = null,
                errors = listOf(PdlError("Some warning message"))
            )

            val result = PdlPersonMapper.fromDto(dto)

            result shouldBe LocalDate.of(1985, 3, 20)
        }

        test("håndterer respons med tom errors-liste") {
            val dto = PdlPersonResult(
                data = PdlPersonEnvelope(
                    hentPerson = PdlPerson(
                        foedselsdato = listOf(
                            PdlFoedselsdato(LocalDate.of(1985, 3, 20))
                        )
                    )
                ),
                extensions = null,
                errors = emptyList()
            )

            val result = PdlPersonMapper.fromDto(dto)

            result shouldBe LocalDate.of(1985, 3, 20)
        }

        test("håndterer respons med extensions") {
            val dto = PdlPersonResult(
                data = PdlPersonEnvelope(
                    hentPerson = PdlPerson(
                        foedselsdato = listOf(
                            PdlFoedselsdato(LocalDate.of(1985, 3, 20))
                        )
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

            result shouldBe LocalDate.of(1985, 3, 20)
        }

        test("håndterer skuddårsdato") {
            val dto = PdlPersonResult(
                data = PdlPersonEnvelope(
                    hentPerson = PdlPerson(
                        foedselsdato = listOf(
                            PdlFoedselsdato(LocalDate.of(2000, 2, 29))
                        )
                    )
                ),
                extensions = null,
                errors = null
            )

            val result = PdlPersonMapper.fromDto(dto)

            result shouldBe LocalDate.of(2000, 2, 29)
        }

        test("håndterer ulike datoer") {
            val testDates = listOf(
                LocalDate.of(1950, 1, 1),
                LocalDate.of(2024, 12, 31),
                LocalDate.of(1975, 6, 15),
                LocalDate.of(2000, 2, 29)
            )

            testDates.forEach { expectedDate ->
                val dto = PdlPersonResult(
                    data = PdlPersonEnvelope(
                        hentPerson = PdlPerson(
                            foedselsdato = listOf(PdlFoedselsdato(expectedDate))
                        )
                    ),
                    extensions = null,
                    errors = null
                )

                val result = PdlPersonMapper.fromDto(dto)

                result shouldBe expectedDate
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
                        foedselsdato = listOf(
                            PdlFoedselsdato(LocalDate.of(1990, 5, 15))
                        )
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

            result shouldBe LocalDate.of(1990, 5, 15)
        }

        test("håndterer extensions med tom warnings-liste") {
            val dto = PdlPersonResult(
                data = PdlPersonEnvelope(
                    hentPerson = PdlPerson(
                        foedselsdato = listOf(
                            PdlFoedselsdato(LocalDate.of(1990, 5, 15))
                        )
                    )
                ),
                extensions = PdlExtensions(warnings = emptyList()),
                errors = null
            )

            val result = PdlPersonMapper.fromDto(dto)

            result shouldBe LocalDate.of(1990, 5, 15)
        }

        test("håndterer extensions med null warnings") {
            val dto = PdlPersonResult(
                data = PdlPersonEnvelope(
                    hentPerson = PdlPerson(
                        foedselsdato = listOf(
                            PdlFoedselsdato(LocalDate.of(1990, 5, 15))
                        )
                    )
                ),
                extensions = PdlExtensions(warnings = null),
                errors = null
            )

            val result = PdlPersonMapper.fromDto(dto)

            result shouldBe LocalDate.of(1990, 5, 15)
        }
    }
})

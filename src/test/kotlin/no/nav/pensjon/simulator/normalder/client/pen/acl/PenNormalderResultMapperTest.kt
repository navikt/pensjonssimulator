package no.nav.pensjon.simulator.normalder.client.pen.acl

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import no.nav.pensjon.simulator.alder.Alder
import no.nav.pensjon.simulator.normalder.VerdiStatus

class PenNormalderResultMapperTest : FunSpec({

    context("fromDto") {

        test("returnerer Aldersgrenser fra gyldig respons") {
            val dto = PenNormalderResult(
                normertPensjonsalderListe = listOf(
                    PenNormertPensjonsalder(
                        aarskull = 1963,
                        aar = 67,
                        maaned = 0,
                        nedreAar = 62,
                        nedreMaaned = 0,
                        oevreAar = 75,
                        oevreMaaned = 0,
                        type = "FAST"
                    )
                )
            )

            val result = PenNormalderResultMapper.fromDto(dto)

            result.size shouldBe 1
            result[0].aarskull shouldBe 1963
            result[0].normalder shouldBe Alder(67, 0)
            result[0].nedreAlder shouldBe Alder(62, 0)
            result[0].oevreAlder shouldBe Alder(75, 0)
            result[0].verdiStatus shouldBe VerdiStatus.FAST
        }

        test("returnerer flere Aldersgrenser fra respons med flere elementer") {
            val dto = PenNormalderResult(
                normertPensjonsalderListe = listOf(
                    PenNormertPensjonsalder(
                        aarskull = 1963,
                        aar = 67,
                        maaned = 0,
                        nedreAar = 62,
                        nedreMaaned = 0,
                        oevreAar = 75,
                        oevreMaaned = 0,
                        type = "FAST"
                    ),
                    PenNormertPensjonsalder(
                        aarskull = 1970,
                        aar = 67,
                        maaned = 3,
                        nedreAar = 62,
                        nedreMaaned = 3,
                        oevreAar = 75,
                        oevreMaaned = 3,
                        type = "PROGNOSE"
                    )
                )
            )

            val result = PenNormalderResultMapper.fromDto(dto)

            result.size shouldBe 2
            result[0].aarskull shouldBe 1963
            result[0].verdiStatus shouldBe VerdiStatus.FAST
            result[1].aarskull shouldBe 1970
            result[1].verdiStatus shouldBe VerdiStatus.PROGNOSE
        }

        test("returnerer tom liste fra respons med tom liste") {
            val dto = PenNormalderResult(
                normertPensjonsalderListe = emptyList()
            )

            val result = PenNormalderResultMapper.fromDto(dto)

            result.size shouldBe 0
        }

        test("kaster RuntimeException når normertPensjonsalderListe er null") {
            val dto = PenNormalderResult(
                normertPensjonsalderListe = null,
                message = "Feil ved henting av data",
                aarskull = 1963
            )

            val exception = shouldThrow<RuntimeException> {
                PenNormalderResultMapper.fromDto(dto)
            }

            exception.message shouldContain "Normalder-feil"
            exception.message shouldContain "1963"
            exception.message shouldContain "Feil ved henting av data"
        }

        test("kaster RuntimeException med null message og null aarskull") {
            val dto = PenNormalderResult(
                normertPensjonsalderListe = null,
                message = null,
                aarskull = null
            )

            val exception = shouldThrow<RuntimeException> {
                PenNormalderResultMapper.fromDto(dto)
            }

            exception.message shouldContain "Normalder-feil"
        }

        test("mapper FAST verdiStatus korrekt") {
            val dto = PenNormalderResult(
                normertPensjonsalderListe = listOf(
                    PenNormertPensjonsalder(
                        aarskull = 1960,
                        aar = 67,
                        maaned = 0,
                        nedreAar = 62,
                        nedreMaaned = 0,
                        oevreAar = 75,
                        oevreMaaned = 0,
                        type = "FAST"
                    )
                )
            )

            val result = PenNormalderResultMapper.fromDto(dto)

            result[0].verdiStatus shouldBe VerdiStatus.FAST
        }

        test("mapper PROGNOSE verdiStatus korrekt") {
            val dto = PenNormalderResult(
                normertPensjonsalderListe = listOf(
                    PenNormertPensjonsalder(
                        aarskull = 2000,
                        aar = 68,
                        maaned = 6,
                        nedreAar = 63,
                        nedreMaaned = 6,
                        oevreAar = 76,
                        oevreMaaned = 6,
                        type = "PROGNOSE"
                    )
                )
            )

            val result = PenNormalderResultMapper.fromDto(dto)

            result[0].verdiStatus shouldBe VerdiStatus.PROGNOSE
        }

        test("mapper måneder korrekt") {
            val dto = PenNormalderResult(
                normertPensjonsalderListe = listOf(
                    PenNormertPensjonsalder(
                        aarskull = 1975,
                        aar = 67,
                        maaned = 6,
                        nedreAar = 62,
                        nedreMaaned = 3,
                        oevreAar = 75,
                        oevreMaaned = 9,
                        type = "FAST"
                    )
                )
            )

            val result = PenNormalderResultMapper.fromDto(dto)

            result[0].normalder shouldBe Alder(67, 6)
            result[0].nedreAlder shouldBe Alder(62, 3)
            result[0].oevreAlder shouldBe Alder(75, 9)
        }

        test("kaster IllegalArgumentException ved ugyldig verdiStatus") {
            val dto = PenNormalderResult(
                normertPensjonsalderListe = listOf(
                    PenNormertPensjonsalder(
                        aarskull = 1980,
                        aar = 67,
                        maaned = 0,
                        nedreAar = 62,
                        nedreMaaned = 0,
                        oevreAar = 75,
                        oevreMaaned = 0,
                        type = "INVALID_TYPE"
                    )
                )
            )

            shouldThrow<IllegalArgumentException> {
                PenNormalderResultMapper.fromDto(dto)
            }
        }

        test("mapper flere ulike årskull") {
            val dto = PenNormalderResult(
                normertPensjonsalderListe = listOf(
                    PenNormertPensjonsalder(
                        aarskull = 1954,
                        aar = 67,
                        maaned = 0,
                        nedreAar = 62,
                        nedreMaaned = 0,
                        oevreAar = 75,
                        oevreMaaned = 0,
                        type = "FAST"
                    ),
                    PenNormertPensjonsalder(
                        aarskull = 1963,
                        aar = 67,
                        maaned = 0,
                        nedreAar = 62,
                        nedreMaaned = 0,
                        oevreAar = 75,
                        oevreMaaned = 0,
                        type = "FAST"
                    ),
                    PenNormertPensjonsalder(
                        aarskull = 1980,
                        aar = 67,
                        maaned = 6,
                        nedreAar = 62,
                        nedreMaaned = 6,
                        oevreAar = 75,
                        oevreMaaned = 6,
                        type = "PROGNOSE"
                    )
                )
            )

            val result = PenNormalderResultMapper.fromDto(dto)

            result.size shouldBe 3
            result.map { it.aarskull } shouldBe listOf(1954, 1963, 1980)
        }

        test("bevarer rekkefølge fra respons") {
            val dto = PenNormalderResult(
                normertPensjonsalderListe = listOf(
                    PenNormertPensjonsalder(
                        aarskull = 1980,
                        aar = 67,
                        maaned = 6,
                        nedreAar = 62,
                        nedreMaaned = 6,
                        oevreAar = 75,
                        oevreMaaned = 6,
                        type = "PROGNOSE"
                    ),
                    PenNormertPensjonsalder(
                        aarskull = 1954,
                        aar = 67,
                        maaned = 0,
                        nedreAar = 62,
                        nedreMaaned = 0,
                        oevreAar = 75,
                        oevreMaaned = 0,
                        type = "FAST"
                    )
                )
            )

            val result = PenNormalderResultMapper.fromDto(dto)

            result[0].aarskull shouldBe 1980
            result[1].aarskull shouldBe 1954
        }
    }
})

package no.nav.pensjon.simulator.afp_etterfulgt_ap.api.tpo.acl.v0.spec

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.core.exception.BadSpecException

class AfpEtterfulgtAvAlderspensjonSpecValidatorTest : StringSpec({
    val validDto = AfpEtterfulgtAvAlderspensjonSpecV0(
        personId = "12345678901",
        sivilstandVedPensjonering = "GIFT",
        uttakFraOgMedDato = "2025-01-01",
        fremtidigAarligInntektTilAfpUttak = 400000,
        inntektSisteMaanedOver1G = true,
        fremtidigAarligInntektUnderAfpUttak = 200000,
        aarIUtlandetEtter16 = 5,
        epsPensjon = false,
        eps2G = false
    )

    "should validate valid spec" {
        val result = AfpEtterfulgtAvAlderspensjonSpecValidator.validateSpec(validDto)
        result.personId shouldBe validDto.personId
    }

    "should throw on missing field" {
        val dto = validDto.copy(personId = null)
        val exception = shouldThrow<BadSpecException> {
            AfpEtterfulgtAvAlderspensjonSpecValidator.validateSpec(dto)
        }
        exception.message shouldBe "personId missing"
    }

    "should throw on invalid fnr" {
        val dto = validDto.copy(personId = "123")
        val exception = shouldThrow<BadSpecException> {
            AfpEtterfulgtAvAlderspensjonSpecValidator.validateSpec(dto)
        }
        exception.message shouldBe "personId er ugyldig"
    }

    "should throw on invalid sivilstand" {
        val dto = validDto.copy(sivilstandVedPensjonering = "UKJENT")
        val exception = shouldThrow<BadSpecException> {
            AfpEtterfulgtAvAlderspensjonSpecValidator.validateSpec(dto)
        }
        exception.message shouldBe "UKJENT er ukjent sivilstand. Tillate verdier: ${
            AfpEtterfulgtAvAlderspensjonSivilstandSpecV0.values().joinToString { it.name }
        }"
    }

    "should throw if uttakFraOgMedDato is not first of month" {
        val dto = validDto.copy(uttakFraOgMedDato = "2025-01-15")
        val exception = shouldThrow<BadSpecException> {
            AfpEtterfulgtAvAlderspensjonSpecValidator.validateSpec(dto)
        }
        exception.message shouldBe "uttakFraOgMedDato må være første dag i en måned"
    }

    "should throw if uttakFraOgMedDato is invalid date" {
        val dto = validDto.copy(uttakFraOgMedDato = "not-a-date")
        val exception = shouldThrow<BadSpecException> {
            AfpEtterfulgtAvAlderspensjonSpecValidator.validateSpec(dto)
        }
        exception.message shouldBe "uttakFraOgMedDato er ikke en gyldig dato"
    }
})

package no.nav.pensjon.simulator.afp.offentlig.pre2025

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.alder.Alder
import no.nav.pensjon.simulator.core.domain.regler.enum.AFPtypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.GrunnlagsrolleEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.SimuleringTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.PersonDetalj
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Persongrunnlag
import no.nav.pensjon.simulator.core.domain.regler.simulering.Simulering
import no.nav.pensjon.simulator.core.exception.PersonForUngException
import no.nav.pensjon.simulator.testutil.TestDateUtil.dateAtNoon
import no.nav.pensjon.simulator.validity.BadSpecException

class Pre2025OffentligAfpSpecValidatorTest : FunSpec({

    val normalder = Alder(aar = 67, maaneder = 0)
    val dato = dateAtNoon(2025, 1, 1)
    val soeker = mutableListOf(PersonDetalj().apply { grunnlagsrolleEnum = GrunnlagsrolleEnum.SOKER })

    test("Manglende simuleringType skal forhindres med BadSpecException") {
        shouldThrow<BadSpecException> {
            Pre2025OffentligAfpSpecValidator.validateInput(
                spec = Simulering().apply { simuleringTypeEnum = null },
                normalder
            )
        }.message shouldBe "Pre2025-AFP-spec mangler simuleringType"
    }

    test("Manglende uttaksdato skal forhindres med BadSpecException") {
        shouldThrow<BadSpecException> {
            Pre2025OffentligAfpSpecValidator.validateInput(
                spec = Simulering().apply {
                    simuleringTypeEnum = SimuleringTypeEnum.ALDER
                    uttaksdato = null
                },
                normalder
            )
        }.message shouldBe "Pre2025-AFP-spec mangler uttaksdato"
    }

    test("AFP-simulering med manglende AFP-ordning skal forhindres med BadSpecException") {
        shouldThrow<BadSpecException> {
            Pre2025OffentligAfpSpecValidator.validateInput(
                spec = Simulering().apply {
                    simuleringTypeEnum = SimuleringTypeEnum.AFP
                    uttaksdato = dato
                    afpOrdningEnum = null
                },
                normalder
            )
        }.message shouldBe "Pre2025-AFP-spec mangler AFP-ordning"
    }

    test("Manglende persongrunnlag skal forhindres med BadSpecException") {
        shouldThrow<BadSpecException> {
            Pre2025OffentligAfpSpecValidator.validateInput(
                spec = Simulering().apply {
                    simuleringTypeEnum = SimuleringTypeEnum.ALDER
                    uttaksdato = dato
                    persongrunnlagListe = mutableListOf()
                },
                normalder
            )
        }.message shouldBe "Pre2025-AFP-spec mangler persongrunnlag for søker"
    }

    test("Manglende persondetalj skal forhindres med BadSpecException") {
        shouldThrow<BadSpecException> {
            Pre2025OffentligAfpSpecValidator.validateInput(
                spec = Simulering().apply {
                    simuleringTypeEnum = SimuleringTypeEnum.ALDER
                    uttaksdato = dato
                    persongrunnlagListe = mutableListOf(Persongrunnlag().apply { personDetaljListe = mutableListOf() })
                },
                normalder
            )
        }.message shouldBe "Pre2025-AFP-spec mangler persongrunnlag for søker"
    }

    test("Manglende søker-persondetalj skal forhindres med BadSpecException") {
        shouldThrow<BadSpecException> {
            Pre2025OffentligAfpSpecValidator.validateInput(
                spec = Simulering().apply {
                    simuleringTypeEnum = SimuleringTypeEnum.ALDER
                    uttaksdato = dato
                    persongrunnlagListe = mutableListOf(Persongrunnlag().apply {
                        personDetaljListe =
                            mutableListOf(PersonDetalj().apply { grunnlagsrolleEnum = GrunnlagsrolleEnum.BARN })
                    })
                },
                normalder
            )
        }.message shouldBe "Pre2025-AFP-spec mangler persongrunnlag for søker"
    }

    test("Alderspensjonsimulering med søker yngre enn normalder skal forhindres med PersonForUngException") {
        shouldThrow<PersonForUngException> {
            Pre2025OffentligAfpSpecValidator.validateInput(
                spec = Simulering().apply {
                    simuleringTypeEnum = SimuleringTypeEnum.ALDER
                    uttaksdato = dateAtNoon(2025, 6, 1)
                    persongrunnlagListe = mutableListOf(Persongrunnlag().apply {
                        fodselsdato = dateAtNoon(1958, 6, 15) // blir 67 år 2 måneder 2025-08-15 => for ung
                        personDetaljListe = soeker
                    })
                },
                normalder = Alder(aar = 67, maaneder = 2)
            )
        }.message shouldBe "Alderspensjon;67;2"
    }

    test("AFP-simulering med søker yngre enn 62 år skal forhindres med PersonForUngException") {
        shouldThrow<PersonForUngException> {
            Pre2025OffentligAfpSpecValidator.validateInput(
                spec = Simulering().apply {
                    simuleringTypeEnum = SimuleringTypeEnum.AFP
                    afpOrdningEnum = AFPtypeEnum.AFPSTAT
                    uttaksdato = dateAtNoon(2025, 6, 1)
                    persongrunnlagListe = mutableListOf(Persongrunnlag().apply {
                        fodselsdato = dateAtNoon(1963, 6, 15) // blir 62 år 2025-06-15 => for ung
                        personDetaljListe = soeker
                    })
                },
                normalder // irrelevant i denne sammenheng
            )
        }.message shouldBe "AFP;62;0"
    }
})

package no.nav.pensjon.simulator.fpp

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.core.domain.regler.enum.AFPtypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.GrunnlagsrolleEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.SimuleringTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.PersonDetalj
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Persongrunnlag
import no.nav.pensjon.simulator.core.domain.regler.simulering.Simulering
import no.nav.pensjon.simulator.core.exception.ImplementationUnrecoverableException
import no.nav.pensjon.simulator.core.exception.PersonForUngException
import java.time.LocalDate

class FppSimuleringSpecValidatorTest : ShouldSpec({

    should("feilmelde udefinert simuleringstype") {
        shouldThrow<ImplementationUnrecoverableException> {
            FppSimuleringSpecValidator.validate(spec = Simulering().apply { simuleringTypeEnum = null })
        }.message shouldBe "simulering.simuleringType"
    }

    should("feilmelde udefinert uttaksdato") {
        shouldThrow<ImplementationUnrecoverableException> {
            FppSimuleringSpecValidator.validate(spec = Simulering().apply {
                simuleringTypeEnum = SimuleringTypeEnum.GJENLEVENDE
                uttaksdatoLd = null
            })
        }.message shouldBe "simulering.uttaksdato"
    }

    should("feilmelde udefinert AFP-ordning hvis simuleringstype er AFP") {
        shouldThrow<ImplementationUnrecoverableException> {
            FppSimuleringSpecValidator.validate(spec = Simulering().apply {
                simuleringTypeEnum = SimuleringTypeEnum.AFP
                uttaksdatoLd = uttaksdato()
                afpOrdningEnum = null
            })
        }.message shouldBe "simulering.afpordning"
    }

    should("feilmelde tom liste av persongrunnlag") {
        shouldThrow<ImplementationUnrecoverableException> {
            FppSimuleringSpecValidator.validate(spec = Simulering().apply {
                simuleringTypeEnum = SimuleringTypeEnum.ALDER_M_GJEN
                uttaksdatoLd = uttaksdato()
                persongrunnlagListe = emptyList()
            })
        }.message shouldBe "simulering.persongrunnlagListe"
    }

    should("feilmelde tom liste av persondetaljer") {
        shouldThrow<ImplementationUnrecoverableException> {
            FppSimuleringSpecValidator.validate(spec = Simulering().apply {
                simuleringTypeEnum = SimuleringTypeEnum.ALDER_M_GJEN
                uttaksdatoLd = uttaksdato()
                persongrunnlagListe = listOf(Persongrunnlag().apply { personDetaljListe = mutableListOf() })
            })
        }.message shouldBe "simulering.persongrunnlagListe.persondetaljliste"
    }

    should("feilmelde udefinert fødselsdato for søkeren") {
        shouldThrow<ImplementationUnrecoverableException> {
            FppSimuleringSpecValidator.validate(spec = Simulering().apply {
                simuleringTypeEnum = SimuleringTypeEnum.ALDER
                uttaksdatoLd = uttaksdato()
                persongrunnlagListe = soekergrunnlag(foedselsaar = null)
            })
        }.message shouldBe "simulering.persongrunnlagListe.soeker.fodselsdato"
    }

    should("feilmelde søker yngre enn 62 år hvis simuleringstype er AFP") {
        shouldThrow<PersonForUngException> {
            FppSimuleringSpecValidator.validate(spec = Simulering().apply {
                simuleringTypeEnum = SimuleringTypeEnum.AFP
                afpOrdningEnum = AFPtypeEnum.AFPSTAT
                uttaksdatoLd = uttaksdato(aar = 2025)
                persongrunnlagListe = soekergrunnlag(foedselsaar = 1964) // 61 år i 2025
            })
        }.message shouldBe "AFP 62"
    }

    should("feilmelde søker yngre enn 67 år hvis simuleringstype er alderspensjon") {
        shouldThrow<PersonForUngException> {
            FppSimuleringSpecValidator.validate(spec = Simulering().apply {
                simuleringTypeEnum = SimuleringTypeEnum.ALDER
                uttaksdatoLd = uttaksdato(aar = 2020)
                persongrunnlagListe = soekergrunnlag(foedselsaar = 1954) // 66 år i 2020

            })
        }.message shouldBe "Alderspensjon 67"
    }
})

private fun uttaksdato(aar: Int = 2026) =
    LocalDate.of(aar, 1, 1)

private fun soekergrunnlag(foedselsaar: Int?): List<Persongrunnlag> =
    listOf(
        Persongrunnlag().apply {
            personDetaljListe = mutableListOf(
                PersonDetalj().apply {
                    grunnlagsrolleEnum = GrunnlagsrolleEnum.SOKER
                    fodselsdatoLd = foedselsaar?.let { LocalDate.of(it, 1, 1) }
                })
        })

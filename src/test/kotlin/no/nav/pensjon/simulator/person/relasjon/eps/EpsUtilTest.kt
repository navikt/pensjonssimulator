package no.nav.pensjon.simulator.person.relasjon.eps

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.core.domain.regler.enum.GrunnlagsrolleEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.InntekttypeEnum
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Inntektsgrunnlag
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.PersonDetalj
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Persongrunnlag

/**
 * EPS = ektefelle/partner/samboer
 */
class EpsUtilTest : ShouldSpec({

    should("gi 'true' når samboer mottar pensjon fra folketrygden") {
        EpsUtil.epsMottarPensjon(
            personListe = listOf(
                persongrunnlag(
                    grunnlagsrolle = GrunnlagsrolleEnum.SAMBO, // samboer
                    inntektstype = InntekttypeEnum.PENF, // pensjon fra folketrygden
                    beloep = 1 // større enn 0
                )
            )
        ) shouldBe true
    }

    should("gi 'false' når en annen enn ektefelle/partner/samboer mottar pensjon fra folketrygden") {
        EpsUtil.epsMottarPensjon(
            personListe = listOf(persongrunnlag(grunnlagsrolle = GrunnlagsrolleEnum.MOR)) // ikke EPS
        ) shouldBe false
    }

    should("gi 'false' når EPS' pensjonsbeløp er mindre enn 1") {
        EpsUtil.epsMottarPensjon(
            personListe = listOf(persongrunnlag(beloep = 0))
        ) shouldBe false
    }

    should("gi 'false' når inntektstypen ikke er pensjon fra folketrygden") {
        EpsUtil.epsMottarPensjon(
            personListe = listOf(persongrunnlag(inntektstype = InntekttypeEnum.FPI)) // ikke pensjon fra folketrygden
        ) shouldBe false
    }
})

private fun persongrunnlag(
    grunnlagsrolle: GrunnlagsrolleEnum = GrunnlagsrolleEnum.EKTEF,
    inntektstype: InntekttypeEnum = InntekttypeEnum.PENF,
    beloep: Int = 1
) =
    Persongrunnlag().apply {
        personDetaljListe = mutableListOf(
            PersonDetalj().apply {
                grunnlagsrolleEnum = grunnlagsrolle
            }
        )
        inntektsgrunnlagListe = mutableListOf(
            Inntektsgrunnlag().apply {
                inntektTypeEnum = inntektstype
                belop = beloep
            }
        )
    }

package no.nav.pensjon.simulator.core.domain.regler.krav

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.core.domain.regler.enum.GrunnlagsrolleEnum
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.PersonDetalj
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Persongrunnlag

class KravhodeTest : FunSpec({
    test("hentPersongrunnlagForRolle henter persongrunnlaget med angitt rolle") {
        val persongrunnlag = Kravhode().apply {
            persongrunnlagListe = mutableListOf(
                Persongrunnlag().apply {
                    antallArUtland = 1
                    personDetaljListe = mutableListOf(
                        detalj(GrunnlagsrolleEnum.FAR),
                        detalj(GrunnlagsrolleEnum.MOR)
                    )
                },
                Persongrunnlag().apply {
                    antallArUtland = 5
                    personDetaljListe = mutableListOf(
                        detalj(GrunnlagsrolleEnum.EKTEF),
                        detalj(GrunnlagsrolleEnum.SOKER) // angitt rolle
                    )
                }
            )
        }.hentPersongrunnlagForRolle(GrunnlagsrolleEnum.SOKER, checkBruk = false)

        persongrunnlag?.antallArUtland shouldBe 5
    }

    test("hentPersongrunnlagForRolle henter persongrunnlaget merket 'bruk' dersom dette angis") {
        val persongrunnlag = Kravhode().apply {
            persongrunnlagListe = mutableListOf(
                Persongrunnlag().apply {
                    antallArUtland = 1
                    personDetaljListe = mutableListOf(detalj(GrunnlagsrolleEnum.SOKER, bruk = true))
                },
                Persongrunnlag().apply {
                    antallArUtland = 5
                    personDetaljListe = mutableListOf(detalj(GrunnlagsrolleEnum.SOKER, bruk = false))
                }
            )
        }.hentPersongrunnlagForRolle(GrunnlagsrolleEnum.SOKER, checkBruk = true)

        persongrunnlag?.antallArUtland shouldBe 1
    }

    test("hentPersongrunnlagForRolle => null hvis ingen rolle matcher") {
        val persongrunnlag = Kravhode().apply {
            persongrunnlagListe = mutableListOf(
                Persongrunnlag().apply {
                    personDetaljListe = mutableListOf(detalj(GrunnlagsrolleEnum.MOR, bruk = true))
                },
                Persongrunnlag().apply {
                    personDetaljListe = mutableListOf(detalj(GrunnlagsrolleEnum.FAR, bruk = true))
                }
            )
        }.hentPersongrunnlagForRolle(GrunnlagsrolleEnum.SOKER, checkBruk = true)

        persongrunnlag.shouldBeNull()
    }

    test("hentPersongrunnlagForRolle => null hvis bruk påkrevd og ingen er merket 'bruk'") {
        val persongrunnlag = Kravhode().apply {
            persongrunnlagListe = mutableListOf(
                Persongrunnlag().apply {
                    personDetaljListe = mutableListOf(detalj(GrunnlagsrolleEnum.SOKER, bruk = false))
                }
            )
        }.hentPersongrunnlagForRolle(GrunnlagsrolleEnum.SOKER, checkBruk = true)

        persongrunnlag.shouldBeNull()
    }
})

private fun detalj(rolle: GrunnlagsrolleEnum, bruk: Boolean = true) =
    PersonDetalj().apply {
        grunnlagsrolleEnum = rolle
        this.bruk = bruk
    }

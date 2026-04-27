package no.nav.pensjon.simulator.core.domain.regler.krav

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.core.domain.regler.enum.GrunnlagsrolleEnum
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.PersonDetalj
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Persongrunnlag
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Uttaksgrad
import java.time.LocalDate

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

    test("'set uttaksgradListe' should sort list in reverse start date order") {
        val uttaksgradListe = Kravhode().apply {
            uttaksgradListe = mutableListOf(
                Uttaksgrad().apply {
                    uttaksgrad = 50
                    fomDatoLd = LocalDate.of(2022, 1, 1) // second
                },
                Uttaksgrad().apply {
                    uttaksgrad = 20
                    fomDatoLd = null // first
                },
                Uttaksgrad().apply {
                    uttaksgrad = 80
                    fomDatoLd = LocalDate.of(2023, 1, 1) // third
                },
                Uttaksgrad().apply {
                    uttaksgrad = 40
                    fomDatoLd = LocalDate.of(2024, 1, 1) // fourth
                }
            )
        }.uttaksgradListe

        uttaksgradListe[0].uttaksgrad shouldBe 40 // fourth
        uttaksgradListe[1].uttaksgrad shouldBe 80 // third
        uttaksgradListe[2].uttaksgrad shouldBe 50 // second
        uttaksgradListe[3].uttaksgrad shouldBe 20 // first
    }
})

private fun detalj(rolle: GrunnlagsrolleEnum, bruk: Boolean = true) =
    PersonDetalj().apply {
        grunnlagsrolleEnum = rolle
        this.bruk = bruk
    }

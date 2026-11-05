package no.nav.pensjon.simulator.person

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.core.domain.regler.enum.GrunnlagsrolleEnum
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.PersonDetalj
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Persongrunnlag

class PersongrunnlagUtilTest : ShouldSpec({

    context("persongrunnlagForRolle") {
        should("gi 'null' hvis ingen persondetalj med angitt rolle") {
            PersongrunnlagUtil.persongrunnlagForRolle(
                grunnlagListe = listOf(
                    Persongrunnlag().apply {
                        personDetaljListe = persondetaljerMed(rolle = GrunnlagsrolleEnum.MOR)
                    }),
                rolle = GrunnlagsrolleEnum.SOKER
            ) shouldBe null
        }

        should("gi persongrunnlag hvis en persondetalj har angitt rolle") {
            val persongrunnlag = Persongrunnlag().apply {
                personDetaljListe = persondetaljerMed(rolle = GrunnlagsrolleEnum.FAR)
            }

            PersongrunnlagUtil.persongrunnlagForRolle(
                grunnlagListe = listOf(persongrunnlag),
                rolle = GrunnlagsrolleEnum.FAR
            ) shouldBe persongrunnlag
        }
    }
})

private fun persondetaljerMed(rolle: GrunnlagsrolleEnum): MutableList<PersonDetalj> =
    mutableListOf(
        PersonDetalj().apply { grunnlagsrolleEnum = rolle }
    )
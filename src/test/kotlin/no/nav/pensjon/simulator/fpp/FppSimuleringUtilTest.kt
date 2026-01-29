package no.nav.pensjon.simulator.fpp

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.core.domain.regler.enum.GrunnlagsrolleEnum
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.PersonDetalj
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Persongrunnlag

class FppSimuleringUtilTest : ShouldSpec({

    context("persongrunnlagForRolle") {
        should("gi 'null' hvis ingen persondetalj med angitt rolle") {
            FppSimuleringUtil.persongrunnlagForRolle(
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

            FppSimuleringUtil.persongrunnlagForRolle(
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

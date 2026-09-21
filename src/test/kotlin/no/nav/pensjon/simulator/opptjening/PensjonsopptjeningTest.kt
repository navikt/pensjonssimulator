package no.nav.pensjon.simulator.opptjening

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Omsorgsgrunnlag

class PensjonsopptjeningTest : ShouldSpec({

    context("utenBeholdninger") {
        should("fjerne beholdninger") {
            val omsorgsgrunnlagListe = listOf(Omsorgsgrunnlag())

            Pensjonsopptjening(
                beholdningListe = listOf(Pensjonsbeholdning(aar = 1999, totalbeloep = 2.1)),
                opptjeningGrunnlagListe = emptyList(),
                inntektGrunnlagListe = emptyList(),
                dagpengerGrunnlagListe = emptyList(),
                omsorgGrunnlagListe = omsorgsgrunnlagListe,
                forstegangstjeneste = null
            ).utenBeholdninger() shouldBe Pensjonsopptjening(
                beholdningListe = emptyList(),
                opptjeningGrunnlagListe = emptyList(),
                inntektGrunnlagListe = emptyList(),
                dagpengerGrunnlagListe = emptyList(),
                omsorgGrunnlagListe = omsorgsgrunnlagListe,
                forstegangstjeneste = null
            )
        }
    }
})
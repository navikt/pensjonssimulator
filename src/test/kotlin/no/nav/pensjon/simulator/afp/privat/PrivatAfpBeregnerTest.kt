package no.nav.pensjon.simulator.afp.privat

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.AfpPrivatBeregning
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.AfpPrivatLivsvarig
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.BeregningsResultatAfpPrivat
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.testutil.TestObjects.persongrunnlag
import java.time.LocalDate
import java.util.TreeSet

class PrivatAfpBeregnerTest : FunSpec({

    test("beregnPrivatAfp uten knekkpunkter skal returnere forrige beregningsresultat") {
        val result = PrivatAfpBeregner(
            context = mockk(),
            generelleDataHolder = mockk(),
            knekkpunktFinder = utenKnekkpunkter()
        ).beregnPrivatAfp(
            PrivatAfpSpec(
                kravhode = Kravhode().apply { persongrunnlagListe = mutableListOf(persongrunnlag) },
                virkningFom = LocalDate.of(2025, 1, 1),
                foersteUttakDato = LocalDate.of(2024, 1, 1),
                forrigePrivatAfpBeregningResult = BeregningsResultatAfpPrivat().apply {
                    afpPrivatBeregning =
                        AfpPrivatBeregning().apply {
                            afpPrivatLivsvarig = AfpPrivatLivsvarig().apply { justeringsbelop = 1 }
                        }
                },
                gjelderOmsorg = false,
                sakId = null
            )
        )

        result.gjeldendeBeregningsresultatAfpPrivat?.afpPrivatBeregning?.afpPrivatLivsvarig?.justeringsbelop shouldBe 1
        result.afpPrivatBeregningsresultatListe[0] shouldBe result.gjeldendeBeregningsresultatAfpPrivat
    }
})

private fun utenKnekkpunkter(): PrivatAfpKnekkpunktFinder =
    mockk<PrivatAfpKnekkpunktFinder>().apply {
        every {
            findKnekkpunktDatoer(any(), any(), any(), any())
        } returns TreeSet()
    }

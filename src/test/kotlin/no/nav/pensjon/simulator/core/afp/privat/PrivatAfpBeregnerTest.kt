package no.nav.pensjon.simulator.core.afp.privat

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.AfpPrivatBeregning
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.AfpPrivatLivsvarig
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.BeregningsResultatAfpPrivat
import no.nav.pensjon.simulator.core.domain.regler.enum.GrunnlagsrolleEnum
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.PersonDetalj
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Persongrunnlag
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import java.time.LocalDate
import java.util.*

class PrivatAfpBeregnerTest : FunSpec({

    test("beregnPrivatAfp should return forrigePrivatAfpBeregningResult when no knekkpunktDatoer") {
        val soekerGrunnlag = Persongrunnlag().apply {
            personDetaljListe = mutableListOf(PersonDetalj().apply {
                grunnlagsrolleEnum = GrunnlagsrolleEnum.SOKER
                bruk = true
            })
        }
        val beregningResultat = BeregningsResultatAfpPrivat().apply {
            afpPrivatBeregning = AfpPrivatBeregning().apply {
                afpPrivatLivsvarig = AfpPrivatLivsvarig().apply {
                    justeringsbelop = 123
                }
            }
        }
        val knekkpunktFinder = mockk<PrivatAfpKnekkpunktFinder>().apply {
            every { findKnekkpunktDatoer(any(), any(), any(), any()) } returns TreeSet() // no knekkpunktDatoer
        }

        val result = PrivatAfpBeregner(
            context = mockk(),
            generelleDataHolder = mockk(),
            knekkpunktFinder
        ).beregnPrivatAfp(
            PrivatAfpSpec(
                kravhode = Kravhode().apply { persongrunnlagListe.add(soekerGrunnlag) },
                virkningFom = LocalDate.of(2021, 1, 1),
                foersteUttakDato = LocalDate.of(2022, 1, 1),
                forrigePrivatAfpBeregningResult = beregningResultat,
                gjelderOmsorg = false,
                sakId = null
            )
        )

        result.gjeldendeBeregningsresultatAfpPrivat?.afpPrivatBeregning
            ?.afpPrivatLivsvarig?.justeringsbelop shouldBe 123
    }
})

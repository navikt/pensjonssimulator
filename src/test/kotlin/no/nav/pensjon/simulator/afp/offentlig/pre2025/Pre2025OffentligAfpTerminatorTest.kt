package no.nav.pensjon.simulator.afp.offentlig.pre2025

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.core.domain.regler.enum.GrunnlagsrolleEnum
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.AfpHistorikk
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.PersonDetalj
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Persongrunnlag
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import java.time.LocalDate

class Pre2025OffentligAfpTerminatorTest : FunSpec({

    test("terminatePre2025OffentligAfp should clear afpHistorikkListe if calculated virkning-t.o.m. < virkning-f.o.m.") {
        val historikk = AfpHistorikk().apply {
            virkFomLd = LocalDate.of(2026, 2, 2) // after calculated virkning-t.o.m.
            virkTomLd = null
        }
        val grunnlag = persongrunnlag(historikk)

        val result = Pre2025OffentligAfpTerminator.terminatePre2025OffentligAfp(
            kravhode = Kravhode().apply { persongrunnlagListe = mutableListOf(grunnlag) },
            foersteUttakDato = LocalDate.of(2025, 1, 1) // => calculated virkning-t.o.m. = 2025-01-31 (day before)
        )

        result.simuleringResult shouldBe null
        historikk.virkTomLd shouldBe null
        grunnlag.afpHistorikkListe.size shouldBe 0
    }

    test("terminatePre2025OffentligAfp should set afpHistorikk virkning-t.o.m. if calculated virkning-t.o.m. > virkning-f.o.m.") {
        val historikk = AfpHistorikk().apply {
            virkFomLd = LocalDate.of(2024, 1, 1) // before calculated virkning-t.o.m.
            virkTomLd = null
        }
        val grunnlag = persongrunnlag(historikk)

        val result = Pre2025OffentligAfpTerminator.terminatePre2025OffentligAfp(
            kravhode = Kravhode().apply { persongrunnlagListe = mutableListOf(grunnlag) },
            foersteUttakDato = LocalDate.of(2025, 2, 1) // => calculated virkning-t.o.m. = 2025-01-31 (day before)
        )

        result.simuleringResult shouldBe null
        historikk.virkTomLd shouldBe LocalDate.of(2025, 1, 31)
        grunnlag.afpHistorikkListe.size shouldBe 1
    }
})

private fun persongrunnlag(historikk: AfpHistorikk) =
    Persongrunnlag().apply {
        personDetaljListe = mutableListOf(
            PersonDetalj().apply {
                grunnlagsrolleEnum = GrunnlagsrolleEnum.SOKER
                bruk = true
            })
        afpHistorikkListe = mutableListOf(historikk)
    }

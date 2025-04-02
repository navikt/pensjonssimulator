package no.nav.pensjon.simulator.core.afp.offentlig.pre2025

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.core.domain.regler.enum.GrunnlagsrolleEnum
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.AfpHistorikk
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.PersonDetalj
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Persongrunnlag
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.testutil.TestDateUtil.dateAtNoon
import java.time.LocalDate
import java.util.*

class Pre2025OffentligAfpTerminatorTest : FunSpec({

    test("terminatePre2025OffentligAfp should clear afpHistorikkListe if calculated virkning-t.o.m. < virkning-f.o.m.") {
        val historikk = AfpHistorikk().apply {
            virkFom = dateAtNoon(2026, Calendar.FEBRUARY, 2) // after calculated virkning-t.o.m.
            virkTom = null
        }
        val grunnlag = persongrunnlag(historikk)

        val result = Pre2025OffentligAfpTerminator.terminatePre2025OffentligAfp(
            kravhode = Kravhode().apply { persongrunnlagListe = mutableListOf(grunnlag) },
            foersteUttakDato = LocalDate.of(2025, 1, 1) // => calculated virkning-t.o.m. = 2025-01-31 (day before)
        )

        result.simuleringResult shouldBe null
        historikk.virkTom shouldBe null
        grunnlag.afpHistorikkListe.size shouldBe 0
    }

    test("terminatePre2025OffentligAfp should set afpHistorikk virkning-t.o.m. if calculated virkning-t.o.m. > virkning-f.o.m.") {
        val historikk = AfpHistorikk().apply {
            virkFom = dateAtNoon(2024, Calendar.JANUARY, 1) // before calculated virkning-t.o.m.
            virkTom = null
        }
        val grunnlag = persongrunnlag(historikk)

        val result = Pre2025OffentligAfpTerminator.terminatePre2025OffentligAfp(
            kravhode = Kravhode().apply { persongrunnlagListe = mutableListOf(grunnlag) },
            foersteUttakDato = LocalDate.of(2025, 2, 1) // => calculated virkning-t.o.m. = 2025-01-31 (day before)
        )

        result.simuleringResult shouldBe null
        historikk.virkTom shouldBe dateAtNoon(2025, Calendar.JANUARY, 31)
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

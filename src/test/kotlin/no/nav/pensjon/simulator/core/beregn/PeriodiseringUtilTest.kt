package no.nav.pensjon.simulator.core.beregn

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.core.domain.regler.PenPerson
import no.nav.pensjon.simulator.core.domain.regler.enum.GrunnlagsrolleEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.KravlinjeTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Pensjonsbeholdning
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.PersonDetalj
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Persongrunnlag
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravlinje
import java.time.LocalDate

class PeriodiseringUtilTest : FunSpec({

    test("periodiserGrunnlagAndModifyKravhode should retain beholdninger with fom before or equal to virkning date") {
        val sokerGrunnlag = sokerPersongrunnlag()

        val kravhode = Kravhode().apply {
            persongrunnlagListe = mutableListOf(sokerGrunnlag)
        }

        val beholdninger = listOf(
            Pensjonsbeholdning().apply {
                fomLd = LocalDate.of(2024, 1, 1)
            },
            Pensjonsbeholdning().apply {
                fomLd = LocalDate.of(2025, 1, 1)
            },
            Pensjonsbeholdning().apply {
                fomLd = LocalDate.of(2026, 1, 1)
            }
        )

        val result = PeriodiseringUtil.periodiserGrunnlagAndModifyKravhode(
            virkningFom = LocalDate.of(2025, 1, 1),
            kravhode = kravhode,
            beholdningListe = beholdninger,
            sakType = null
        )

        result.hentPersongrunnlagForSoker().beholdninger.size shouldBe 2
    }

    test("periodiserGrunnlagAndModifyKravhode should set full uttak with 100% uttaksgrad") {
        val sokerGrunnlag = sokerPersongrunnlag()

        val kravhode = Kravhode().apply {
            persongrunnlagListe = mutableListOf(sokerGrunnlag)
            uttaksgradListe = mutableListOf()
        }

        val result = PeriodiseringUtil.periodiserGrunnlagAndModifyKravhode(
            virkningFom = LocalDate.of(2025, 1, 1),
            kravhode = kravhode,
            beholdningListe = emptyList(),
            sakType = null
        )

        result.uttaksgradListe.size shouldBe 1
        result.uttaksgradListe[0].uttaksgrad shouldBe 100
    }

    test("periodiserGrunnlagAndModifyKravhode should remove non-alderspensjon kravlinjer") {
        val sokerGrunnlag = sokerPersongrunnlag()

        val kravhode = Kravhode().apply {
            persongrunnlagListe = mutableListOf(sokerGrunnlag)
            kravlinjeListe = mutableListOf(
                Kravlinje().apply { kravlinjeTypeEnum = KravlinjeTypeEnum.AP },
                Kravlinje().apply { kravlinjeTypeEnum = KravlinjeTypeEnum.UT },
                Kravlinje().apply { kravlinjeTypeEnum = KravlinjeTypeEnum.GJP }
            )
        }

        val result = PeriodiseringUtil.periodiserGrunnlagAndModifyKravhode(
            virkningFom = LocalDate.of(2025, 1, 1),
            kravhode = kravhode,
            beholdningListe = emptyList(),
            sakType = null
        )

        result.kravlinjeListe.size shouldBe 1
        result.kravlinjeListe[0].kravlinjeTypeEnum shouldBe KravlinjeTypeEnum.AP
    }
})

private fun sokerPersongrunnlag() = Persongrunnlag().apply {
    fodselsdatoLd = LocalDate.of(1960, 1, 1)
    penPerson = PenPerson().apply { penPersonId = 1L }
    personDetaljListe = mutableListOf(
        PersonDetalj().apply {
            bruk = true
            grunnlagsrolleEnum = GrunnlagsrolleEnum.SOKER
            virkFom = LocalDate.of(2020, 1, 1)
        }
    )
}

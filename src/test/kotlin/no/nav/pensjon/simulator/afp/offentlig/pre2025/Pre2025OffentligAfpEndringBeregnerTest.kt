package no.nav.pensjon.simulator.afp.offentlig.pre2025

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import no.nav.pensjon.simulator.core.domain.regler.PenPerson
import no.nav.pensjon.simulator.core.domain.regler.enum.GrunnlagsrolleEnum
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.AfpHistorikk
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.PersonDetalj
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Persongrunnlag
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.normalder.NormertPensjonsalderService
import no.nav.pensjon.simulator.testutil.TestDateUtil.dateAtNoon
import java.time.LocalDate
import java.util.Calendar
import java.util.Date

class Pre2025OffentligAfpEndringBeregnerTest : FunSpec({

    /**
     * I denne testen er normalderDato før foersteUttakDato.
     * Da settes virkningTom til dagen før normalderDato.
     * Siden virkningTom da er før AFP-historikkens virkFom, så tømmes AFP-historikken.
     * NB: Kun første element i AFP-historikk-listen tas i betraktning.
     */
    test("beregnAfp should remove AFP-historikk if calculated virkningTom is before AFP virkFom") {
        // virkningTom (2024-12-31) er før afpVirkFom (2025-02-01) => AFP-listen skal tømmes
        val persongrunnlag = persongrunnlag(afpVirkFom = dateAtNoon(2025, Calendar.FEBRUARY, 1))

        Pre2025OffentligAfpEndringBeregner(
            normalderService = mockk<NormertPensjonsalderService>().apply {
                every { normalderDato(LocalDate.of(1963, 1, 1)) } returns LocalDate.of(2025, 1, 1)
            }
        ).beregnAfp(
            kravhode = Kravhode().apply { persongrunnlagListe = mutableListOf(persongrunnlag) },
            foersteUttakDato = LocalDate.of(2026, 1, 1) // => virkningTom = normalderDato - 1 dag = 2024-12-31
        ).simuleringResult shouldBe null

        persongrunnlag.afpHistorikkListe.size shouldBe 0 // AFP-listen er tømt
    }

    /**
     * I denne testen er foersteUttakDato før normalderDato.
     * Da settes virkningTom til dagen før foersteUttakDato.
     * Siden virkningTom da er etter AFP-historikkens virkFom, settes AFP-historikkens virkTom til virkningTom
     * NB: Kun første element i AFP-historikk-listen tas i betraktning (og påvirkes).
     */
    test("beregnAfp should set AFP virkning t.o.m.-dato if calculated virkningTom is after AFP virkFom") {
        // virkningTom (2024-06-30) er etter afpVirkFom (2024-02-01) => AFP virkTom skal settes
        val persongrunnlag = persongrunnlag(afpVirkFom = dateAtNoon(2024, Calendar.FEBRUARY, 1))

        Pre2025OffentligAfpEndringBeregner(
            normalderService = mockk<NormertPensjonsalderService>().apply {
                every { normalderDato(LocalDate.of(1963, 1, 1)) } returns LocalDate.of(2025, 1, 1)
            }
        ).beregnAfp(
            kravhode = Kravhode().apply { persongrunnlagListe = mutableListOf(persongrunnlag) },
            foersteUttakDato = LocalDate.of(2024, 7, 1) // => virkningTom = foersteUttakDato - 1 dag = 2024-06-30
        ).simuleringResult shouldBe null

        with(persongrunnlag) {
            afpHistorikkListe.size shouldBe 2
            afpHistorikkListe[0].virkTom shouldBe dateAtNoon(2024, Calendar.JUNE, 30)
            afpHistorikkListe[1].virkTom shouldBe null
        }
    }
})

private fun persongrunnlag(afpVirkFom: Date) =
    Persongrunnlag().apply {
        afpHistorikkListe = mutableListOf(
            AfpHistorikk().apply {
                virkFom = afpVirkFom
                virkTom = null // skal settes til virkningTom (2024-12-31)
            },
            // Denne skal ignoreres (kun første element tas i betraktning):
            AfpHistorikk().apply {
                virkFom = dateAtNoon(2023, Calendar.JANUARY, 1)
                virkTom = null
            }
        )
        penPerson = PenPerson()
        fodselsdato = dateAtNoon(1963, Calendar.JANUARY, 1)
        personDetaljListe = mutableListOf(
            PersonDetalj().apply {
                bruk = true
                grunnlagsrolleEnum = GrunnlagsrolleEnum.SOKER
                penRolleTom = dateAtNoon(2026, Calendar.JANUARY, 1)
            }
        )
    }

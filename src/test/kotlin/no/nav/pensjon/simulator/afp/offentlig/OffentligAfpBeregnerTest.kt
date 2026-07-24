package no.nav.pensjon.simulator.afp.offentlig

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import no.nav.pensjon.simulator.afp.offentlig.livsvarig.LivsvarigOffentligAfpService
import no.nav.pensjon.simulator.afp.offentlig.livsvarig.grunnlag.LivsvarigOffentligAfpResult
import no.nav.pensjon.simulator.afp.offentlig.tidsbegrenset.TidsbegrensetOffentligAfpEndringBeregner
import no.nav.pensjon.simulator.afp.offentlig.tidsbegrenset.TidsbegrensetOffentligAfpFoerstegangBeregner
import no.nav.pensjon.simulator.afp.offentlig.tidsbegrenset.TidsbegrensetOffentligAfpResult
import no.nav.pensjon.simulator.core.domain.regler.PenPerson
import no.nav.pensjon.simulator.core.domain.regler.enum.GrunnlagsrolleEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.SimuleringTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.AfpHistorikk
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.PersonDetalj
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Persongrunnlag
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.domain.regler.simulering.Simuleringsresultat
import no.nav.pensjon.simulator.core.ytelse.LoependeYtelser
import no.nav.pensjon.simulator.testutil.TestObjects.pid
import no.nav.pensjon.simulator.testutil.TestObjects.simuleringSpec
import java.time.LocalDate

class OffentligAfpBeregnerTest : ShouldSpec({

    val foedselsdatoSomMedfoererTidsbegrensetAfp = LocalDate.of(1961, 1, 1) // før 1963

    val persongrunnlagMedAfp =
        Persongrunnlag().apply {
            afpHistorikkListe = listOf(AfpHistorikk().apply { virkTomLd = LocalDate.of(2025, 1, 1) })
            penPerson = PenPerson()
            fodselsdatoLd = LocalDate.of(1961, 1, 1)
            personDetaljListe = mutableListOf(
                PersonDetalj().apply {
                    bruk = true
                    grunnlagsrolleEnum = GrunnlagsrolleEnum.SOKER
                    penRolleTom = LocalDate.of(2026, 1, 1)
                }
            )
        }

    val noYtelser =
        LoependeYtelser(
            soekerVirkningFom = LocalDate.of(2021, 1, 1), // don't care
            privatAfpVirkningFom = null,
            sisteBeregning = null,
            forrigeAlderspensjonBeregningResultat = null,
            forrigePrivatAfpBeregningResultat = null,
            forrigeVedtakListe = mutableListOf(),
            avdoed = null
        )

    context("beregnAfp for tidsbegrenset offentlig AFP") {
        should("return tidsbegrenset result and modified kravhode") {
            val originalKravhode = Kravhode()
            val modifiedKravhode = Kravhode()
            val simuleringResult = Simuleringsresultat()
            val afpResult = TidsbegrensetOffentligAfpResult(simuleringResult, modifiedKravhode)

            OffentligAfpBeregner(
                tidsbegrensetFoerstegangBeregner = arrangeTidsbegrensetFoerstegang(afpResult),
                tidsbegrensetEndringBeregner = mockk(),
                livsvarigBeregner = mockk()
            ).beregnAfp(
                spec = simuleringSpec(type = SimuleringTypeEnum.AFP_ETTERF_ALDER), // tidsbegrenset offentlig AFP
                kravhode = originalKravhode,
                ytelser = noYtelser,
                foedselsdato = foedselsdatoSomMedfoererTidsbegrensetAfp,
                pid
            ) shouldBe OffentligAfpResult(
                tidsbegrenset = TidsbegrensetOffentligAfpResult(simuleringResult, modifiedKravhode),
                livsvarig = null,
                modifiedKravhode
            )
        }
    }

    context("beregnAfp ved endring uten livsvarig offentlig AFP") {
        should("return tidsbegrenset result and original kravhode") {
            val kravhode = Kravhode()
            val simuleringResult = Simuleringsresultat()
            val afpResult = TidsbegrensetOffentligAfpResult(simuleringResult, kravhode)

            OffentligAfpBeregner(
                tidsbegrensetFoerstegangBeregner = mockk(),
                tidsbegrensetEndringBeregner = arrangeTidsbegrensetEndring(afpResult),
                livsvarigBeregner = mockk()
            ).beregnAfp(
                spec = simuleringSpec(type = SimuleringTypeEnum.ENDR_ALDER), // endring uten livsvarig offentlig AFP
                kravhode,
                ytelser = noYtelser,
                foedselsdato = foedselsdatoSomMedfoererTidsbegrensetAfp,
                pid
            ) shouldBe OffentligAfpResult(
                tidsbegrenset = TidsbegrensetOffentligAfpResult(simuleringResult, kravhode),
                livsvarig = null,
                kravhode
            )
        }
    }

    context("beregnAfp når terminering av tidsbegrenset offentlig AFP kreves") {
        should("return tidsbegrenset result and original kravhode") {
            val kravhode = Kravhode().apply { persongrunnlagListe = mutableListOf(persongrunnlagMedAfp) }

            OffentligAfpBeregner(
                tidsbegrensetFoerstegangBeregner = mockk(),
                tidsbegrensetEndringBeregner = mockk(),
                livsvarigBeregner = mockk()
            ).beregnAfp(
                spec = simuleringSpec(type = SimuleringTypeEnum.ALDER), // krever terminering av tidsbegrenset offentlig AFP
                kravhode,
                ytelser = noYtelser,
                foedselsdato = foedselsdatoSomMedfoererTidsbegrensetAfp,
                pid
            ) shouldBe OffentligAfpResult(
                tidsbegrenset = TidsbegrensetOffentligAfpResult(simuleringResult = null, kravhode), // terminert
                livsvarig = null,
                kravhode
            )
        }
    }

    /**
     * For personer født før 1963 kan offentlig AFP være involvert selv om simuleringen gjelder ren alderspensjon.
     * Det skyldes at eventuell løpende AFP må termineres før alderspensjon kan starte.
     * Dette gjelder ikke for personer født 1963 eller senere, siden de har livsvarig offentlig AFP som kan tas ut
     * uavhengig av alderspensjon.
     */
    context("beregnAfp når ingen AFP involvert") {
        should("return original kravhode and no AFP") {
            val kravhode = Kravhode()

            OffentligAfpBeregner(
                tidsbegrensetFoerstegangBeregner = mockk(),
                tidsbegrensetEndringBeregner = mockk(),
                livsvarigBeregner = mockk()
            ).beregnAfp(
                spec = simuleringSpec(type = SimuleringTypeEnum.ALDER), // => ingen AFP involvert hvis født 1963 eller senere
                kravhode,
                ytelser = noYtelser,
                foedselsdato = LocalDate.of(1963, 1, 1), // => født 1963 eller senere
                pid
            ) shouldBe OffentligAfpResult(
                tidsbegrenset = null,
                livsvarig = null,
                kravhode
            )
        }
    }

    context("beregnAfp for livsvarig offentlig AFP") {
        should("return livsvarig result and original kravhode") {
            val kravhode = Kravhode()
            val result = LivsvarigOffentligAfpResult(pid = pid.value, afpYtelseListe = listOf())

            OffentligAfpBeregner(
                tidsbegrensetFoerstegangBeregner = mockk(),
                tidsbegrensetEndringBeregner = mockk(),
                livsvarigBeregner = arrangeLivsvarig(result)
            ).beregnAfp(
                spec = simuleringSpec(type = SimuleringTypeEnum.ALDER_MED_AFP_OFFENTLIG_LIVSVARIG),
                kravhode,
                ytelser = noYtelser,
                foedselsdato = LocalDate.of(1963, 1, 1),
                pid
            ) shouldBe OffentligAfpResult(
                tidsbegrenset = null,
                livsvarig = result,
                kravhode
            )
        }
    }
})

private fun arrangeLivsvarig(result: LivsvarigOffentligAfpResult): LivsvarigOffentligAfpService =
    mockk {
        every { beregnAfp(any(), any(), any(), any(), any(), any()) } returns result
    }

private fun arrangeTidsbegrensetFoerstegang(result: TidsbegrensetOffentligAfpResult): TidsbegrensetOffentligAfpFoerstegangBeregner =
    mockk {
        every { beregnAfp(any(), any(), any()) } returns result
    }

private fun arrangeTidsbegrensetEndring(result: TidsbegrensetOffentligAfpResult): TidsbegrensetOffentligAfpEndringBeregner =
    mockk {
        every { beregnAfp(any(), any()) } returns result
    }
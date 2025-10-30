package no.nav.pensjon.simulator.afp.offentlig

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import no.nav.pensjon.simulator.afp.offentlig.fra2025.LivsvarigOffentligAfpService
import no.nav.pensjon.simulator.afp.offentlig.fra2025.grunnlag.LivsvarigOffentligAfpResult
import no.nav.pensjon.simulator.afp.offentlig.pre2025.Pre2025OffentligAfpEndringBeregner
import no.nav.pensjon.simulator.afp.offentlig.pre2025.Pre2025OffentligAfpFoerstegangBeregner
import no.nav.pensjon.simulator.afp.offentlig.pre2025.Pre2025OffentligAfpResult
import no.nav.pensjon.simulator.core.domain.regler.PenPerson
import no.nav.pensjon.simulator.core.domain.regler.enum.GrunnlagsrolleEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.SimuleringTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.AfpHistorikk
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.PersonDetalj
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Persongrunnlag
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.domain.regler.simulering.Simuleringsresultat
import no.nav.pensjon.simulator.core.ytelse.LoependeYtelser
import no.nav.pensjon.simulator.testutil.TestDateUtil.dateAtNoon
import no.nav.pensjon.simulator.testutil.TestObjects.pid
import no.nav.pensjon.simulator.testutil.TestObjects.simuleringSpec
import java.time.LocalDate
import java.util.*

class OffentligAfpBeregnerTest : ShouldSpec({

    val pre2025Foedselsdato = LocalDate.of(1961, 1, 1) // født før 1963 => "gammel" (pre-2025) AFP gjelder

    val persongrunnlagMedAfp =
        Persongrunnlag().apply {
            afpHistorikkListe = listOf(AfpHistorikk().apply { virkTom = dateAtNoon(2025, Calendar.JANUARY, 1) })
            penPerson = PenPerson()
            fodselsdato = dateAtNoon(1961, Calendar.JANUARY, 1)
            personDetaljListe = mutableListOf(
                PersonDetalj().apply {
                    bruk = true
                    grunnlagsrolleEnum = GrunnlagsrolleEnum.SOKER
                    penRolleTom = dateAtNoon(2026, Calendar.JANUARY, 1)
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

    context("beregnAfp for pre-2025 offentlig AFP") {
        should("return pre-2025 result and modified kravhode") {
            val originalKravhode = Kravhode()
            val modifiedKravhode = Kravhode()
            val simuleringResult = Simuleringsresultat()
            val afpResult = Pre2025OffentligAfpResult(simuleringResult, modifiedKravhode)

            OffentligAfpBeregner(
                pre2025FoerstegangBeregner = arrangePre2025Foerstegang(afpResult),
                pre2025EndringBeregner = mockk(),
                livsvarigBeregner = mockk()
            ).beregnAfp(
                spec = simuleringSpec(type = SimuleringTypeEnum.AFP_ETTERF_ALDER), // pre-2025 offentlig AFP
                kravhode = originalKravhode,
                ytelser = noYtelser,
                foedselsdato = pre2025Foedselsdato,
                pid
            ) shouldBe OffentligAfpResult(
                pre2025 = Pre2025OffentligAfpResult(simuleringResult, modifiedKravhode),
                livsvarig = null,
                modifiedKravhode
            )
        }
    }

    context("beregnAfp ved endring uten livsvarig offentlig AFP") {
        should("return pre-2025 result and original kravhode") {
            val kravhode = Kravhode()
            val simuleringResult = Simuleringsresultat()
            val afpResult = Pre2025OffentligAfpResult(simuleringResult, kravhode)

            OffentligAfpBeregner(
                pre2025FoerstegangBeregner = mockk(),
                pre2025EndringBeregner = arrangePre2025Endring(afpResult),
                livsvarigBeregner = mockk()
            ).beregnAfp(
                spec = simuleringSpec(type = SimuleringTypeEnum.ENDR_ALDER), // endring uten livsvarig offentlig AFP
                kravhode,
                ytelser = noYtelser,
                foedselsdato = pre2025Foedselsdato,
                pid
            ) shouldBe OffentligAfpResult(
                pre2025 = Pre2025OffentligAfpResult(simuleringResult, kravhode),
                livsvarig = null,
                kravhode
            )
        }
    }

    context("beregnAfp når terminering av pre-2025 offentlig AFP kreves") {
        should("return pre-2025 result and original kravhode") {
            val kravhode = Kravhode().apply { persongrunnlagListe = mutableListOf(persongrunnlagMedAfp) }

            OffentligAfpBeregner(
                pre2025FoerstegangBeregner = mockk(),
                pre2025EndringBeregner = mockk(),
                livsvarigBeregner = mockk()
            ).beregnAfp(
                spec = simuleringSpec(type = SimuleringTypeEnum.ALDER), // krever terminering av pre-2025 offentlig AFP
                kravhode,
                ytelser = noYtelser,
                foedselsdato = pre2025Foedselsdato,
                pid
            ) shouldBe OffentligAfpResult(
                pre2025 = Pre2025OffentligAfpResult(simuleringResult = null, kravhode), // terminert
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
                pre2025FoerstegangBeregner = mockk(),
                pre2025EndringBeregner = mockk(),
                livsvarigBeregner = mockk()
            ).beregnAfp(
                spec = simuleringSpec(type = SimuleringTypeEnum.ALDER), // => ingen AFP involvert hvis født 1963 eller senere
                kravhode,
                ytelser = noYtelser,
                foedselsdato = LocalDate.of(1963, 1, 1), // => født 1963 eller senere
                pid
            ) shouldBe OffentligAfpResult(
                pre2025 = null,
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
                pre2025FoerstegangBeregner = mockk(),
                pre2025EndringBeregner = mockk(),
                livsvarigBeregner = arrangeLivsvarig(result)
            ).beregnAfp(
                spec = simuleringSpec(type = SimuleringTypeEnum.ALDER_MED_AFP_OFFENTLIG_LIVSVARIG),
                kravhode,
                ytelser = noYtelser,
                foedselsdato = LocalDate.of(1963, 1, 1),
                pid
            ) shouldBe OffentligAfpResult(
                pre2025 = null,
                livsvarig = result,
                kravhode
            )
        }
    }
})

private fun arrangeLivsvarig(result: LivsvarigOffentligAfpResult): LivsvarigOffentligAfpService =
    mockk<LivsvarigOffentligAfpService>().apply {
        every { beregnAfp(any(), any(), any(), any(), any(), any()) } returns result
    }

private fun arrangePre2025Foerstegang(result: Pre2025OffentligAfpResult): Pre2025OffentligAfpFoerstegangBeregner =
    mockk<Pre2025OffentligAfpFoerstegangBeregner>().apply {
        every { beregnAfp(any(), any(), any()) } returns result
    }

private fun arrangePre2025Endring(result: Pre2025OffentligAfpResult): Pre2025OffentligAfpEndringBeregner =
    mockk<Pre2025OffentligAfpEndringBeregner>().apply {
        every { beregnAfp(any(), any()) } returns result
    }

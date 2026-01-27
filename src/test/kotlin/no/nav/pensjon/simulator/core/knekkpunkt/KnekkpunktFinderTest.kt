package no.nav.pensjon.simulator.core.knekkpunkt

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.maps.shouldContainKey
import io.kotest.matchers.maps.shouldHaveSize
import io.kotest.matchers.maps.shouldNotContainKey
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.pensjon.simulator.alder.Alder
import no.nav.pensjon.simulator.core.domain.SivilstatusType
import no.nav.pensjon.simulator.core.domain.regler.PenPerson
import no.nav.pensjon.simulator.core.domain.regler.Trygdetid
import no.nav.pensjon.simulator.core.domain.regler.enum.GrunnlagsrolleEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.SimuleringTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Opptjeningsgrunnlag
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.PersonDetalj
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Persongrunnlag
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Uttaksgrad
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.krav.UttakGradKode
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.normalder.NormertPensjonsalderService
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.tech.time.Time
import no.nav.pensjon.simulator.testutil.TestDateUtil.dateAtNoon
import no.nav.pensjon.simulator.trygdetid.TrygdetidBeregnerProxy
import no.nav.pensjon.simulator.trygdetid.TrygdetidCombo
import java.time.LocalDate
import java.util.*

class KnekkpunktFinderTest : FunSpec({

    // =====================================================
    // Tests for basic knekkpunkt finding with opptjeningsgrunnlag
    // =====================================================

    test("finnKnekkpunkter should add knekkpunkter based on soker opptjeningsgrunnlag with pi > 0") {
        val finder = createKnekkpunktFinder()
        val spec = createKnekkpunktSpec(
            foersteUttakDato = LocalDate.of(2025, 1, 1), // Set early enough so knekkpunkter are not stripped
            soekerOpptjeningGrunnlagListe = mutableListOf(
                Opptjeningsgrunnlag().apply {
                    ar = 2024 // Creates knekkpunkt at 2026 (ar + 2)
                    pi = 500000 // positive pi should trigger knekkpunkt
                },
                Opptjeningsgrunnlag().apply {
                    ar = 2025 // Creates knekkpunkt at 2027
                    pi = 600000
                }
            )
        )

        val result = finder.finnKnekkpunkter(spec)

        // Knekkpunkter should be at ar + OPPTJENING_ETTERSLEP_ANTALL_AAR (2)
        result shouldContainKey LocalDate.of(2026, 1, 1)
        result shouldContainKey LocalDate.of(2027, 1, 1)
        result[LocalDate.of(2026, 1, 1)]!! shouldContain KnekkpunktAarsak.OPPTJBRUKER
        result[LocalDate.of(2027, 1, 1)]!! shouldContain KnekkpunktAarsak.OPPTJBRUKER
    }

    test("finnKnekkpunkter should not add knekkpunkter for opptjeningsgrunnlag with pi = 0") {
        val finder = createKnekkpunktFinder()
        val spec = createKnekkpunktSpec(
            soekerOpptjeningGrunnlagListe = mutableListOf(
                Opptjeningsgrunnlag().apply {
                    ar = 2035
                    pi = 0 // zero pi should not trigger knekkpunkt
                }
            )
        )

        val result = finder.finnKnekkpunkter(spec)

        // 2022 should not be present since pi = 0
        // but normalder-based knekkpunkt should still be there
        result.keys.filter { it == LocalDate.of(2035, 1, 1) }.size shouldBe 0
    }

    test("finnKnekkpunkter should add knekkpunkter based on avdod opptjeningsgrunnlag when avdod is present") {
        val finder = createKnekkpunktFinder()
        val spec = createKnekkpunktSpec(
            foersteUttakDato = LocalDate.of(2025, 1, 1), // Set early enough so knekkpunkter are not stripped
            avdoedGrunnlag = createPersongrunnlag(GrunnlagsrolleEnum.AVDOD).apply {
                dodsdato = dateAtNoon(2020, Calendar.DECEMBER, 15)
                opptjeningsgrunnlagListe = mutableListOf(
                    Opptjeningsgrunnlag().apply {
                        ar = 2024 // Creates knekkpunkt at 2026 (ar + 2)
                        pi = 400000
                    }
                )
            },
            avdoedVirkningFom = LocalDate.of(2021, 1, 1)
        )

        val result = finder.finnKnekkpunkter(spec)

        // Knekkpunkt for avdod at 2024 + 2 = 2026
        result shouldContainKey LocalDate.of(2026, 1, 1)
        result[LocalDate.of(2026, 1, 1)]!! shouldContain KnekkpunktAarsak.OPPTJAVDOD
    }

    // =====================================================
    // Tests for knekkpunkter based on uttaksgrad
    // =====================================================

    test("finnKnekkpunkter should add knekkpunkter based on uttaksgradliste") {
        val finder = createKnekkpunktFinder()
        val spec = createKnekkpunktSpec(
            uttaksgradListe = mutableListOf(
                Uttaksgrad().apply {
                    fomDato = dateAtNoon(2030, Calendar.MARCH, 1)
                    uttaksgrad = 50
                },
                Uttaksgrad().apply {
                    fomDato = dateAtNoon(2032, Calendar.JUNE, 1)
                    uttaksgrad = 100
                }
            )
        )

        val result = finder.finnKnekkpunkter(spec)

        result shouldContainKey LocalDate.of(2030, 3, 1)
        result shouldContainKey LocalDate.of(2032, 6, 1)
        result[LocalDate.of(2030, 3, 1)]!! shouldContain KnekkpunktAarsak.UTG
        result[LocalDate.of(2032, 6, 1)]!! shouldContain KnekkpunktAarsak.UTG
    }

    // =====================================================
    // Tests for normalder knekkpunkt
    // =====================================================

    test("finnKnekkpunkter should add knekkpunkt at normalder date") {
        val normalderDato = LocalDate.of(2030, 2, 1)
        val finder = createKnekkpunktFinder(normalderDato = normalderDato)
        val spec = createKnekkpunktSpec()

        val result = finder.finnKnekkpunkter(spec)

        result shouldContainKey normalderDato
        result[normalderDato]!! shouldContain KnekkpunktAarsak.OPPTJBRUKER
    }

    // =====================================================
    // Tests for trygdetid knekkpunkter
    // =====================================================

    test("finnKnekkpunkter should add TTBRUKER knekkpunkt at foersteBeregningDato") {
        val finder = createKnekkpunktFinder()
        val spec = createKnekkpunktSpec(
            foersteUttakDato = LocalDate.of(2028, 1, 1)
        )

        val result = finder.finnKnekkpunkter(spec)

        result shouldContainKey LocalDate.of(2028, 1, 1)
        result[LocalDate.of(2028, 1, 1)]!! shouldContain KnekkpunktAarsak.TTBRUKER
    }

    test("finnKnekkpunkter should add TTAVDOD knekkpunkt when avdod is present") {
        val finder = createKnekkpunktFinder()
        val spec = createKnekkpunktSpec(
            avdoedGrunnlag = createPersongrunnlag(GrunnlagsrolleEnum.AVDOD).apply {
                dodsdato = dateAtNoon(2020, Calendar.DECEMBER, 15)
            },
            avdoedVirkningFom = LocalDate.of(2021, 1, 1)
        )

        val result = finder.finnKnekkpunkter(spec)

        // Should have TTAVDOD knekkpunkt
        val ttAvdodKnekkpunkter = result.filterValues { it.contains(KnekkpunktAarsak.TTAVDOD) }
        ttAvdodKnekkpunkter.shouldHaveSize(1)
    }

    test("finnKnekkpunkter should add knekkpunkter when trygdetid changes") {
        val trygdetidBeregner = mockk<TrygdetidBeregnerProxy>()

        // First call returns 30 years, subsequent calls return 31, 32, etc.
        every { trygdetidBeregner.fastsettTrygdetidForPeriode(any(), any(), any(), any()) } returnsMany listOf(
            TrygdetidCombo(Trygdetid().apply { tt = 30 }, null),
            TrygdetidCombo(Trygdetid().apply { tt = 31 }, null), // Different from previous
            TrygdetidCombo(Trygdetid().apply { tt = 31 }, null)  // Same as previous
        )

        val finder = createKnekkpunktFinder(trygdetidBeregner = trygdetidBeregner)
        val spec = createKnekkpunktSpec(
            foersteUttakDato = LocalDate.of(2025, 1, 1)
        )

        val result = finder.finnKnekkpunkter(spec)

        // Should have knekkpunkt at 2025-01-01 (foersteBeregningDato)
        result shouldContainKey LocalDate.of(2025, 1, 1)
        // Should have knekkpunkt at 2026-01-01 (trygdetid changed from 30 to 31)
        result shouldContainKey LocalDate.of(2026, 1, 1)
    }

    test("finnKnekkpunkter should stop adding trygdetid knekkpunkter when full trygdetid is reached") {
        val trygdetidBeregner = mockk<TrygdetidBeregnerProxy>()

        // Returns full trygdetid (40 years) immediately
        every { trygdetidBeregner.fastsettTrygdetidForPeriode(any(), any(), any(), any()) } returns
            TrygdetidCombo(Trygdetid().apply { tt = 40 }, Trygdetid().apply { tt = 40 })

        val finder = createKnekkpunktFinder(trygdetidBeregner = trygdetidBeregner)
        val spec = createKnekkpunktSpec(
            foersteUttakDato = LocalDate.of(2025, 1, 1)
        )

        finder.finnKnekkpunkter(spec)

        // Called twice: once for initial foersteBeregningDato, once for first loop iteration
        // Then breaks because full trygdetid was reached
        // This is much fewer calls than would happen if trygdetid was not full (would iterate until age 76)
        verify(exactly = 2) { trygdetidBeregner.fastsettTrygdetidForPeriode(any(), eq(GrunnlagsrolleEnum.SOKER), any(), any()) }
    }

    // =====================================================
    // Tests for stripping knekkpunkter before forsteBerDato
    // =====================================================

    test("finnKnekkpunkter should strip knekkpunkter before foersteBeregningDato") {
        val finder = createKnekkpunktFinder()
        val spec = createKnekkpunktSpec(
            foersteUttakDato = LocalDate.of(2030, 1, 1),
            soekerOpptjeningGrunnlagListe = mutableListOf(
                Opptjeningsgrunnlag().apply {
                    ar = 2020 // This would create knekkpunkt at 2022, before foersteUttak
                    pi = 500000
                }
            )
        )

        val result = finder.finnKnekkpunkter(spec)

        // Knekkpunkt at 2022-01-01 should be stripped because it's before foersteBeregningDato (2030)
        result shouldNotContainKey LocalDate.of(2022, 1, 1)
    }

    // =====================================================
    // Tests for simulerForTp trimming
    // =====================================================

    test("finnKnekkpunkter should trim knekkpunkter for simulerForTp at normalder when no UTG after normalder") {
        val normalderDato = LocalDate.of(2030, 2, 1)
        val finder = createKnekkpunktFinder(normalderDato = normalderDato)
        val spec = createKnekkpunktSpec(
            foersteUttakDato = LocalDate.of(2028, 1, 1),
            simulerForTp = true,
            soekerOpptjeningGrunnlagListe = mutableListOf(
                Opptjeningsgrunnlag().apply {
                    ar = 2030 // Creates knekkpunkt at 2032, after normalder
                    pi = 500000
                }
            )
        )

        val result = finder.finnKnekkpunkter(spec)

        // Knekkpunkter after normalder (2030-02-01) should be trimmed when simulerForTp is true
        // and there's no UTG knekkpunkt after normalder
        result shouldNotContainKey LocalDate.of(2032, 1, 1)
    }

    test("finnKnekkpunkter should keep UTG knekkpunkter after normalder when simulerForTp is true") {
        val normalderDato = LocalDate.of(2030, 2, 1)
        val finder = createKnekkpunktFinder(normalderDato = normalderDato)
        val spec = createKnekkpunktSpec(
            foersteUttakDato = LocalDate.of(2028, 1, 1),
            simulerForTp = true,
            uttaksgradListe = mutableListOf(
                Uttaksgrad().apply {
                    fomDato = dateAtNoon(2031, Calendar.JUNE, 1) // After normalder
                    uttaksgrad = 100
                }
            )
        )

        val result = finder.finnKnekkpunkter(spec)

        // UTG knekkpunkt after normalder should be kept
        result shouldContainKey LocalDate.of(2031, 6, 1)
    }

    // =====================================================
    // Tests for forrigeAlderspensjonBeregningResultatVirkningFom
    // =====================================================

    test("finnKnekkpunkter should use forrigeBeregningResultatVirkning when provided") {
        val finder = createKnekkpunktFinder(today = LocalDate.of(2025, 6, 1))
        val spec = createKnekkpunktSpec(
            foersteUttakDato = LocalDate.of(2025, 1, 1),
            forrigeAlderspensjonBeregningResultatVirkningFom = LocalDate.of(2025, 3, 1)
        )

        val result = finder.finnKnekkpunkter(spec)

        // foersteBeregningDato should be the minimum of:
        // - 1.1 next year after latest of today(2025-06-01) and forrigeVirk(2025-03-01) = 2026-01-01
        // - foersteUttakDato = 2025-01-01
        // - normalderDato (if after latestOfTodayAndForrigeVirk)
        // So foersteBeregningDato = 2025-01-01 (foersteUttakDato)
        result shouldContainKey LocalDate.of(2025, 1, 1)
    }

    test("finnKnekkpunkter should use normalderDato when it is after today and forrigeVirk") {
        val normalderDato = LocalDate.of(2025, 8, 1)
        val finder = createKnekkpunktFinder(
            today = LocalDate.of(2025, 1, 1),
            normalderDato = normalderDato
        )
        val spec = createKnekkpunktSpec(
            foersteUttakDato = LocalDate.of(2030, 1, 1), // Later than normalder
            forrigeAlderspensjonBeregningResultatVirkningFom = LocalDate.of(2025, 1, 1)
        )

        val result = finder.finnKnekkpunkter(spec)

        // normalderDato (2025-08-01) is after today and forrigeVirk, and before foersteUttak
        // So it should be the foersteBeregningDato
        result shouldContainKey normalderDato
    }

    // =====================================================
    // Tests for pre-2025 offentlig AFP
    // =====================================================

    test("finnKnekkpunkter should use heltUttakDato for pre-2025 offentlig AFP") {
        val finder = createKnekkpunktFinder()
        val spec = createKnekkpunktSpec(
            type = SimuleringTypeEnum.AFP_ETTERF_ALDER,
            foersteUttakDato = LocalDate.of(2025, 1, 1),
            heltUttakDato = LocalDate.of(2030, 1, 1)
        )

        val result = finder.finnKnekkpunkter(spec)

        // For pre-2025 offentlig AFP, foersteBeregningDato should be heltUttakDato
        result shouldContainKey LocalDate.of(2030, 1, 1)
    }

    // =====================================================
    // Tests for multiple knekkpunktAarsaker on same date
    // =====================================================

    test("finnKnekkpunkter should accumulate multiple aarsaker on same date") {
        val finder = createKnekkpunktFinder(normalderDato = LocalDate.of(2030, 1, 1))
        val spec = createKnekkpunktSpec(
            foersteUttakDato = LocalDate.of(2030, 1, 1),
            uttaksgradListe = mutableListOf(
                Uttaksgrad().apply {
                    fomDato = dateAtNoon(2030, Calendar.JANUARY, 1) // Same date as normalder and foersteUttak
                    uttaksgrad = 100
                }
            )
        )

        val result = finder.finnKnekkpunkter(spec)

        // Should have multiple aarsaker on 2030-01-01
        val aarsakerOnDate = result[LocalDate.of(2030, 1, 1)]!!
        aarsakerOnDate shouldContain KnekkpunktAarsak.UTG
        aarsakerOnDate shouldContain KnekkpunktAarsak.OPPTJBRUKER // From normalder
        aarsakerOnDate shouldContain KnekkpunktAarsak.TTBRUKER // From trygdetid
    }

    // =====================================================
    // Tests for empty cases
    // =====================================================

    test("finnKnekkpunkter should return knekkpunkter even with minimal data") {
        val finder = createKnekkpunktFinder()
        val spec = createKnekkpunktSpec()

        val result = finder.finnKnekkpunkter(spec)

        // Should at least have normalder knekkpunkt and trygdetid knekkpunkt
        result.isNotEmpty() shouldBe true
    }

    // =====================================================
    // Tests for knekkpunkter ordering
    // =====================================================

    test("finnKnekkpunkter should return knekkpunkter sorted by date") {
        val finder = createKnekkpunktFinder()
        val spec = createKnekkpunktSpec(
            foersteUttakDato = LocalDate.of(2025, 1, 1),
            uttaksgradListe = mutableListOf(
                Uttaksgrad().apply {
                    fomDato = dateAtNoon(2028, Calendar.MARCH, 1)
                    uttaksgrad = 50
                },
                Uttaksgrad().apply {
                    fomDato = dateAtNoon(2026, Calendar.JUNE, 1)
                    uttaksgrad = 30
                },
                Uttaksgrad().apply {
                    fomDato = dateAtNoon(2030, Calendar.JANUARY, 1)
                    uttaksgrad = 100
                }
            )
        )

        val result = finder.finnKnekkpunkter(spec)

        // Keys should be sorted
        val dates = result.keys.toList()
        dates shouldBe dates.sorted()
    }

    // =====================================================
    // Tests for null/undefined trygdetid handling
    // =====================================================

    test("finnKnekkpunkter should handle null trygdetid as full trygdetid") {
        val trygdetidBeregner = mockk<TrygdetidBeregnerProxy>()

        // Returns null trygdetid (considered as full trygdetid)
        every { trygdetidBeregner.fastsettTrygdetidForPeriode(any(), any(), any(), any()) } returns
            TrygdetidCombo(null, null)

        val finder = createKnekkpunktFinder(trygdetidBeregner = trygdetidBeregner)
        val spec = createKnekkpunktSpec(
            foersteUttakDato = LocalDate.of(2025, 1, 1)
        )

        finder.finnKnekkpunkter(spec)

        // Called twice: once for initial foersteBeregningDato, once for first loop iteration
        // Then breaks because null is considered full trygdetid
        // This is much fewer calls than would happen if trygdetid was not full (would iterate until age 76)
        verify(exactly = 2) { trygdetidBeregner.fastsettTrygdetidForPeriode(any(), eq(GrunnlagsrolleEnum.SOKER), any(), any()) }
    }

    test("finnKnekkpunkter should detect difference when one trygdetid is null and other is not") {
        val trygdetidBeregner = mockk<TrygdetidBeregnerProxy>()

        every { trygdetidBeregner.fastsettTrygdetidForPeriode(any(), any(), any(), any()) } returnsMany listOf(
            TrygdetidCombo(null, null),
            TrygdetidCombo(Trygdetid().apply { tt = 30 }, null) // Different from null
        )

        val finder = createKnekkpunktFinder(trygdetidBeregner = trygdetidBeregner)
        val spec = createKnekkpunktSpec(
            foersteUttakDato = LocalDate.of(2025, 1, 1)
        )

        val result = finder.finnKnekkpunkter(spec)

        // Should have knekkpunkt at 2026-01-01 because trygdetid changed from null to 30
        result shouldContainKey LocalDate.of(2026, 1, 1)
    }
})

// =====================================================
// Helper functions
// =====================================================

private fun createKnekkpunktFinder(
    trygdetidBeregner: TrygdetidBeregnerProxy = mockTrygdetidBeregner(),
    normalderDato: LocalDate = LocalDate.of(2030, 2, 1),
    today: LocalDate = LocalDate.of(2025, 1, 1)
): KnekkpunktFinder {
    val normalderService = mockk<NormertPensjonsalderService>()
    every { normalderService.normalderDato(any()) } returns normalderDato
    every { normalderService.normalder(any<LocalDate>()) } returns Alder(67, 0)

    val time = mockk<Time>()
    every { time.today() } returns today

    return KnekkpunktFinder(trygdetidBeregner, normalderService, time)
}

private fun mockTrygdetidBeregner(): TrygdetidBeregnerProxy {
    val trygdetidBeregner = mockk<TrygdetidBeregnerProxy>()
    every { trygdetidBeregner.fastsettTrygdetidForPeriode(any(), any(), any(), any()) } returns
        TrygdetidCombo(Trygdetid().apply { tt = 40 }, Trygdetid().apply { tt = 40 }) // Full trygdetid
    return trygdetidBeregner
}

private fun createKnekkpunktSpec(
    type: SimuleringTypeEnum = SimuleringTypeEnum.ALDER,
    foersteUttakDato: LocalDate? = LocalDate.of(2028, 1, 1),
    heltUttakDato: LocalDate? = null,
    soekerOpptjeningGrunnlagListe: MutableList<Opptjeningsgrunnlag> = mutableListOf(),
    avdoedGrunnlag: Persongrunnlag? = null,
    avdoedVirkningFom: LocalDate? = null,
    uttaksgradListe: MutableList<Uttaksgrad> = mutableListOf(),
    forrigeAlderspensjonBeregningResultatVirkningFom: LocalDate? = null,
    simulerForTp: Boolean = false,
    sakId: Long? = null
): KnekkpunktSpec {
    val soekerGrunnlag = createPersongrunnlag(GrunnlagsrolleEnum.SOKER).apply {
        opptjeningsgrunnlagListe = soekerOpptjeningGrunnlagListe
    }

    val kravhode = Kravhode().apply {
        persongrunnlagListe = mutableListOf(soekerGrunnlag)
        avdoedGrunnlag?.let { persongrunnlagListe.add(it) }
        this.uttaksgradListe = uttaksgradListe
    }

    val simulering = createSimuleringSpec(
        type = type,
        foersteUttakDato = foersteUttakDato,
        heltUttakDato = heltUttakDato,
        simulerForTp = simulerForTp
    )

    return KnekkpunktSpec(
        kravhode = kravhode,
        simulering = simulering,
        soekerVirkningFom = foersteUttakDato ?: LocalDate.of(2028, 1, 1),
        avdoedVirkningFom = avdoedVirkningFom,
        forrigeAlderspensjonBeregningResultatVirkningFom = forrigeAlderspensjonBeregningResultatVirkningFom,
        sakId = sakId
    )
}

private fun createPersongrunnlag(rolle: GrunnlagsrolleEnum): Persongrunnlag =
    Persongrunnlag().apply {
        fodselsdato = dateAtNoon(1963, Calendar.JANUARY, 1)
        penPerson = PenPerson().apply { penPersonId = if (rolle == GrunnlagsrolleEnum.SOKER) 1L else 2L }
        personDetaljListe = mutableListOf(
            PersonDetalj().apply {
                bruk = true
                grunnlagsrolleEnum = rolle
            }
        )
    }

private fun createSimuleringSpec(
    type: SimuleringTypeEnum = SimuleringTypeEnum.ALDER,
    foersteUttakDato: LocalDate? = LocalDate.of(2028, 1, 1),
    heltUttakDato: LocalDate? = null,
    simulerForTp: Boolean = false
): SimuleringSpec = SimuleringSpec(
    type = type,
    sivilstatus = SivilstatusType.UGIF,
    epsHarPensjon = false,
    foersteUttakDato = foersteUttakDato,
    heltUttakDato = heltUttakDato,
    pid = Pid("12345678910"),
    foedselDato = LocalDate.of(1963, 1, 1),
    avdoed = null,
    isTpOrigSimulering = false,
    simulerForTp = simulerForTp,
    uttakGrad = UttakGradKode.P_100,
    forventetInntektBeloep = 500000,
    inntektUnderGradertUttakBeloep = 0,
    inntektEtterHeltUttakBeloep = 0,
    inntektEtterHeltUttakAntallAar = 0,
    foedselAar = 1963,
    utlandAntallAar = 0,
    utlandPeriodeListe = mutableListOf(),
    fremtidigInntektListe = mutableListOf(),
    brukFremtidigInntekt = false,
    inntektOver1GAntallAar = 0,
    flyktning = false,
    epsHarInntektOver2G = false,
    livsvarigOffentligAfp = null,
    pre2025OffentligAfp = null,
    erAnonym = false,
    ignoreAvslag = false,
    isHentPensjonsbeholdninger = false,
    isOutputSimulertBeregningsinformasjonForAllKnekkpunkter = false,
    onlyVilkaarsproeving = false,
    epsKanOverskrives = false
)

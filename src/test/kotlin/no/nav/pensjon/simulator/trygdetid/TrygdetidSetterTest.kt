package no.nav.pensjon.simulator.trygdetid

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import no.nav.pensjon.simulator.core.domain.regler.TTPeriode
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.BeregningsResultatAlderspensjon2025
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Persongrunnlag
import no.nav.pensjon.simulator.core.domain.reglerextend.copy
import no.nav.pensjon.simulator.core.util.toNorwegianDateAtNoon
import no.nav.pensjon.simulator.testutil.TestObjects.simuleringSpec
import java.time.LocalDate

class TrygdetidSetterTest : ShouldSpec({

    val time = { LocalDate.of(2025, 1, 1) }
    val adjuster: TrygdetidAdjuster = mockk()

    should("sette trygdetid i normaltilfeller") {
        with(TrygdetidSetter(adjuster, time).settTrygdetid(spec = spec(utlandAntallAar = 0))) {
            trygdetidPerioder shouldHaveSize 1
            trygdetidPerioderKapittel20 shouldHaveSize 1
            trygdetidPerioder[0].fom shouldBe trygdetidFom
            trygdetidPerioderKapittel20[0].fom shouldBe trygdetidFom
        }
    }

    should("begrense utenlandsopphold for kapittel 19 men ikke for kapittel 20") {
        with(TrygdetidSetter(adjuster, time).settTrygdetid(spec = spec(utlandAntallAar = 51))) {
            trygdetidPerioder shouldHaveSize 1
            trygdetidPerioderKapittel20 shouldHaveSize 1
            // Trygdetidstart hvis ikke begrenset = fødselsår + minstealder + utenlandsopphold = 1963 + 16 + 51 = 2030
            // Maks. antall utenlandsår = uttaksalder - minstealder - opptjeningsår = 65 - 16 - 0 = 49
            // Dvs. trygdetidstart hvis begrenset = 1963 + 16 + 49 = 2028
            trygdetidPerioder[0].fom shouldBe LocalDate.of(2028, 1, 15).toNorwegianDateAtNoon()
            trygdetidPerioderKapittel20[0].fom shouldBe LocalDate.of(2030, 1, 15).toNorwegianDateAtNoon()
        }
    }

    context("settTrygdetidMedTidligereBeregningsresultat") {

        should("justere eksisterende trygdetidperioder og legge til ny periode når tom er null") {
            val existingPeriode = TTPeriode().apply {
                fom = LocalDate.of(2020, 1, 1).toNorwegianDateAtNoon()
            }
            val persongrunnlag = Persongrunnlag().apply {
                fodselsdato = foedselsdato
                trygdetidPerioder = mutableListOf(existingPeriode)
                trygdetidPerioderKapittel20 = mutableListOf(existingPeriode.copy())
            }
            val forrigeResultat = BeregningsResultatAlderspensjon2025()

            every { adjuster.conditionallyAdjustLastTrygdetidPeriode(any(), any()) } just runs

            val result = TrygdetidSetter(adjuster, time).settTrygdetid(
                spec = specMedForrigeResultat(
                    persongrunnlag = persongrunnlag,
                    forrigeResultat = forrigeResultat,
                    tom = null
                )
            )

            // Verify adjuster was called for both lists
            verify(exactly = 1) { adjuster.conditionallyAdjustLastTrygdetidPeriode(persongrunnlag.trygdetidPerioder, null) }
            verify(exactly = 1) { adjuster.conditionallyAdjustLastTrygdetidPeriode(persongrunnlag.trygdetidPerioderKapittel20, null) }

            // Should have added new period (existing + new = 2)
            result.trygdetidPerioder shouldHaveSize 2
            result.trygdetidPerioderKapittel20 shouldHaveSize 2

            // New period should start from today (fom = time.today())
            val newPeriode = result.trygdetidPerioder[1]
            newPeriode.fom shouldBe LocalDate.of(2025, 1, 1).toNorwegianDateAtNoon()
            newPeriode.ikkeProRata shouldBe true
        }

        should("justere eksisterende trygdetidperioder og legge til ny periode når tom er etter fom") {
            val existingPeriode = TTPeriode().apply {
                fom = LocalDate.of(2020, 1, 1).toNorwegianDateAtNoon()
            }
            val persongrunnlag = Persongrunnlag().apply {
                fodselsdato = foedselsdato
                trygdetidPerioder = mutableListOf(existingPeriode)
                trygdetidPerioderKapittel20 = mutableListOf(existingPeriode.copy())
            }
            val forrigeResultat = BeregningsResultatAlderspensjon2025()
            val tom = LocalDate.of(2030, 6, 1) // tom is after fom (2025-01-01)

            every { adjuster.conditionallyAdjustLastTrygdetidPeriode(any(), any()) } just runs

            val result = TrygdetidSetter(adjuster, time).settTrygdetid(
                spec = specMedForrigeResultat(
                    persongrunnlag = persongrunnlag,
                    forrigeResultat = forrigeResultat,
                    tom = tom
                )
            )

            // Should have added new period
            result.trygdetidPerioder shouldHaveSize 2
            result.trygdetidPerioderKapittel20 shouldHaveSize 2

            // New period should have correct tom
            val newPeriode = result.trygdetidPerioder[1]
            newPeriode.tom shouldBe tom.toNorwegianDateAtNoon()
        }

        should("ikke legge til ny periode når tom er før fom") {
            val existingPeriode = TTPeriode().apply {
                fom = LocalDate.of(2020, 1, 1).toNorwegianDateAtNoon()
            }
            val persongrunnlag = Persongrunnlag().apply {
                fodselsdato = foedselsdato
                trygdetidPerioder = mutableListOf(existingPeriode)
                trygdetidPerioderKapittel20 = mutableListOf(existingPeriode.copy())
            }
            val forrigeResultat = BeregningsResultatAlderspensjon2025()
            val tom = LocalDate.of(2024, 6, 1) // tom is before fom (2025-01-01)

            every { adjuster.conditionallyAdjustLastTrygdetidPeriode(any(), any()) } just runs

            val result = TrygdetidSetter(adjuster, time).settTrygdetid(
                spec = specMedForrigeResultat(
                    persongrunnlag = persongrunnlag,
                    forrigeResultat = forrigeResultat,
                    tom = tom
                )
            )

            // Should NOT have added new period (only existing)
            result.trygdetidPerioder shouldHaveSize 1
            result.trygdetidPerioderKapittel20 shouldHaveSize 1
        }

        should("ikke legge til ny periode når tom er lik fom") {
            val existingPeriode = TTPeriode().apply {
                fom = LocalDate.of(2020, 1, 1).toNorwegianDateAtNoon()
            }
            val persongrunnlag = Persongrunnlag().apply {
                fodselsdato = foedselsdato
                trygdetidPerioder = mutableListOf(existingPeriode)
                trygdetidPerioderKapittel20 = mutableListOf(existingPeriode.copy())
            }
            val forrigeResultat = BeregningsResultatAlderspensjon2025()
            val tom = LocalDate.of(2025, 1, 1) // tom == fom (today)

            every { adjuster.conditionallyAdjustLastTrygdetidPeriode(any(), any()) } just runs

            val result = TrygdetidSetter(adjuster, time).settTrygdetid(
                spec = specMedForrigeResultat(
                    persongrunnlag = persongrunnlag,
                    forrigeResultat = forrigeResultat,
                    tom = tom
                )
            )

            // Should NOT have added new period (tom.isAfter(fom) is false when tom == fom)
            result.trygdetidPerioder shouldHaveSize 1
            result.trygdetidPerioderKapittel20 shouldHaveSize 1
        }
    }

    context("norskTrygdetidPeriode returnerer null") {

        should("returnere null og ikke legge til periode når fom er lik tom") {
            // foedselsdato = 1963-01-15
            // trygdetidStartAlderAar = 16 + utlandAntallAar
            // fom = 1963-01-15 + trygdetidStartAlderAar years
            // If utlandAntallAar = 0, fom = 1979-01-15
            // For fom to be >= tom, we need tom <= 1979-01-15
            val tom = LocalDate.of(1979, 1, 15) // Same as fom when utlandAntallAar = 0

            val result = TrygdetidSetter(adjuster, time).settTrygdetid(
                spec = spec(utlandAntallAar = 0, tom = tom)
            )

            // norskTrygdetidPeriode returns null when fom >= tom, so no periods should be added
            result.trygdetidPerioder.shouldBeEmpty()
            result.trygdetidPerioderKapittel20.shouldBeEmpty()
        }

        should("returnere null og ikke legge til periode når fom er etter tom") {
            // foedselsdato = 1963-01-15
            // If utlandAntallAar = 0, fom = 1979-01-15
            // Set tom to be before fom
            val tom = LocalDate.of(1978, 6, 1) // Before fom (1979-01-15)

            val result = TrygdetidSetter(adjuster, time).settTrygdetid(
                spec = spec(utlandAntallAar = 0, tom = tom)
            )

            // norskTrygdetidPeriode returns null when fom >= tom, so no periods should be added
            result.trygdetidPerioder.shouldBeEmpty()
            result.trygdetidPerioderKapittel20.shouldBeEmpty()
        }

        should("returnere null for kapittel 20 men ikke kapittel 19 ved høy utenlandsopphold og begrenset tom") {
            // For kapittel 19: utlandAntallAar is limited by kapittel19UtlandAntallAar
            // For kapittel 20: utlandAntallAar is used directly
            // foedselsdato = 1963-01-15
            // foersteUttakDato = 2029-01-01 (age 65)
            // maxAntallAarUtland for kap19 = 65 - 16 - 0 = 49
            // If angittUtlandAntallAar = 60:
            //   Kap19 fom = 1963 + 16 + 49 = 2028-01-15
            //   Kap20 fom = 1963 + 16 + 60 = 2039-01-15
            // With tom = 2035-01-01:
            //   Kap19: fom (2028-01-15) < tom (2035-01-01) -> period added
            //   Kap20: fom (2039-01-15) >= tom (2035-01-01) -> null, no period added
            val tom = LocalDate.of(2035, 1, 1)

            val result = TrygdetidSetter(adjuster, time).settTrygdetid(
                spec = spec(utlandAntallAar = 60, tom = tom)
            )

            // Kapittel 19 should have a period (limited utlandAntallAar makes fom < tom)
            result.trygdetidPerioder shouldHaveSize 1
            result.trygdetidPerioder[0].fom shouldBe LocalDate.of(2028, 1, 15).toNorwegianDateAtNoon()

            // Kapittel 20 should have no period (fom >= tom)
            result.trygdetidPerioderKapittel20.shouldBeEmpty()
        }
    }
})

/**
 * Fødselsår 1963 medfører trygdetid fra 1979 (1963 + 16 år).
 */
private val foedselsdato = LocalDate.of(1963, 1, 15).toNorwegianDateAtNoon()
private val trygdetidFom = LocalDate.of(1979, 1, 15).toNorwegianDateAtNoon()

private fun spec(utlandAntallAar: Int, tom: LocalDate? = null) =
    TrygdetidGrunnlagSpec(
        persongrunnlag = Persongrunnlag().apply { fodselsdato = foedselsdato },
        utlandAntallAar,
        tom = tom,
        forrigeAlderspensjonBeregningResultat = null,
        simuleringSpec = simuleringSpec() // Første uttak: 2029-01-01 (dvs. ved alder 65)
    )

private fun specMedForrigeResultat(
    persongrunnlag: Persongrunnlag,
    forrigeResultat: BeregningsResultatAlderspensjon2025,
    tom: LocalDate?
) =
    TrygdetidGrunnlagSpec(
        persongrunnlag = persongrunnlag,
        utlandAntallAar = 0,
        tom = tom,
        forrigeAlderspensjonBeregningResultat = forrigeResultat,
        simuleringSpec = simuleringSpec()
    )

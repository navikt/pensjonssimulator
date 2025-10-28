package no.nav.pensjon.simulator.trygdetid

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Persongrunnlag
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
})

/**
 * Fødselsår 1963 medfører trygdetid fra 1979 (1963 + 16 år).
 */
private val foedselsdato = LocalDate.of(1963, 1, 15).toNorwegianDateAtNoon()
private val trygdetidFom = LocalDate.of(1979, 1, 15).toNorwegianDateAtNoon()

private fun spec(utlandAntallAar: Int) =
    TrygdetidGrunnlagSpec(
        persongrunnlag = Persongrunnlag().apply { fodselsdato = foedselsdato },
        utlandAntallAar,
        tom = null,
        forrigeAlderspensjonBeregningResultat = null,
        simuleringSpec = simuleringSpec() // Første uttak: 2029-01-01 (dvs. ved alder 65)
    )

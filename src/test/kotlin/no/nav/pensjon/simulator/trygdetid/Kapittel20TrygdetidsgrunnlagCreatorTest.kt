package no.nav.pensjon.simulator.trygdetid

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.core.domain.regler.enum.LandkodeEnum
import no.nav.pensjon.simulator.testutil.Assert
import java.time.LocalDate

class Kapittel20TrygdetidsgrunnlagCreatorTest : FunSpec({

    /**
     * kapittel20TrygdetidsperiodeListe skal gi:
     * - første trygdetidsdato = 31.12. i året der søkeren fyller 16 år (minste alder for trygdetid)
     * - siste trygdetidsdato = dagen før uttaksstart (så lenge den er før 66-årsgrensen)
     * Med fødselsadto 15.6.1970 fyller søkeren 16 år den 15.6.1986; første trygdetidsdato blir 31.12.1986
     * Med uttaksstart 1.7.2033 blir siste trygdetidsdato 30.6.2033
     * ------------------
     * Videre skal utenlandsperioder ikke regnes med. Med utenlandsopphold 1.1.1999–31.12.2000 blir det
     * opphold i trygdetiden i denne perioden.
     */
    test("kapittel20TrygdetidsperiodeListe gir siste trygdetidsdato = dagen før uttaksstart") {
        val result = Kapittel20TrygdetidsgrunnlagCreator.kapittel20TrygdetidsperiodeListe(
            utlandPeriodeListe = mutableListOf(
                UtlandPeriode(
                    fom = LocalDate.of(1999, 1, 1),
                    tom = LocalDate.of(2000, 12, 31),
                    land = LandkodeEnum.LUX,
                    arbeidet = false
                )
            ),
            foedselsdato = LocalDate.of(1970, 6, 15), // blir 66 i 2036
            foersteUttakDato = LocalDate.of(2033, 7, 1), // dvs. før 2036 (66 år)
        )

        result.size shouldBe 2
        Assert.trygdetidsperiode(
            periode = result[0],
            expectedFom = LocalDate.of(1986, 6, 15),
            expectedTom = LocalDate.of(1998, 12, 31)
        )
        Assert.trygdetidsperiode(
            periode = result[1],
            expectedFom = LocalDate.of(2001, 1, 1),
            expectedTom = LocalDate.of(2033, 6, 30)
        )
    }

    /**
     * kapittel20TrygdetidsperiodeListe begrenser trygdetid oppad til 31.12. året søkeren blir 66 år.
     * Med fødselsdato 29.5.1962 blir søkeren 66 år i 2028. Trygdetiden slutter dermed senest 31.12.2028.
     * ------------------
     * Videre skal utenlandsperioder ikke regnes med. Med utenlandsopphold 1962–1999 starter trygdetiden 1.1.2000.
     */
    test("kapittel20TrygdetidsperiodeListe begrenser trygdetid oppad til 31.12. året søkeren blir 66 år") {
        val result = Kapittel20TrygdetidsgrunnlagCreator.kapittel20TrygdetidsperiodeListe(
            utlandPeriodeListe = mutableListOf(
                UtlandPeriode(
                    fom = LocalDate.of(1962, 5, 29),
                    tom = LocalDate.of(1999, 12, 31),
                    // trygdetid starter dermed 2000-01-01
                    land = LandkodeEnum.LUX,
                    arbeidet = false
                )
            ),
            foedselsdato = LocalDate.of(1962, 5, 29), // blir 66 i 2028
            foersteUttakDato = LocalDate.of(2029, 6, 1) // etter 2028
        )

        result.size shouldBe 1
        Assert.trygdetidsperiode(periode = result[0], expectedFomAar = 2000, expectedTomAar = 2028)
    }
})

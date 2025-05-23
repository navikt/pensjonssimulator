package no.nav.pensjon.simulator.core.trygd

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Persongrunnlag
import no.nav.pensjon.simulator.testutil.TestDateUtil.dateAtNoon
import no.nav.pensjon.simulator.testutil.TestObjects.simuleringSpec
import java.time.LocalDate
import java.util.*

class TrygdetidSetterTest : FunSpec({

    /**
     * Når intet utenlandsopphold:
     * Trygdetidperiode starter ved: fodselsdato + nedre aldersgrense for trygdetid = 1963 + 16 = 1979
     */
    test("settTrygdetid uten utenlandsopphold") {
        TrygdetidSetter(time = { LocalDate.of(2025, 1, 1) }).settTrygdetid(
            spec = TrygdetidGrunnlagSpec(
                persongrunnlag = Persongrunnlag().apply { fodselsdato = dateAtNoon(1963, Calendar.JANUARY, 1) },
                utlandAntallAar = 0,
                tom = null,
                forrigeAlderspensjonBeregningResultat = null,
                simuleringSpec = simuleringSpec()
            )
        ).trygdetidPerioder[0].fom shouldBe dateAtNoon(1979, Calendar.JANUARY, 1)
    }

    /**
     * Ved langt utenlandsopphold:
     * Trygdetidperiode starter ved: fodselsdato + nedre aldersgrense for trygdetid + max antall år utenlands
     * = 1963 + 16 + 49 = 2028
     */
    test("settTrygdetid med langt utenlandsopphold") {
        TrygdetidSetter(time = { LocalDate.of(2025, 1, 1) }).settTrygdetid(
            spec = TrygdetidGrunnlagSpec(
                persongrunnlag = Persongrunnlag().apply { fodselsdato = dateAtNoon(1963, Calendar.JANUARY, 1) },
                utlandAntallAar = 55, // maxAntallAarUtland = 49
                tom = null,
                forrigeAlderspensjonBeregningResultat = null,
                simuleringSpec = simuleringSpec()
            )
        ).trygdetidPerioder[0].fom shouldBe dateAtNoon(2028, Calendar.JANUARY, 1)
    }
})

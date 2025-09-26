package no.nav.pensjon.simulator.trygdetid

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.core.domain.regler.enum.LandkodeEnum
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Opptjeningsgrunnlag
import no.nav.pensjon.simulator.core.trygd.UtlandPeriode
import no.nav.pensjon.simulator.testutil.Assert
import java.time.LocalDate

class Kapittel19TrygdetidsgrunnlagCreatorTest : FunSpec({

    /**
     * kapittel19TrygdetidsperiodeListe skal:
     * - ikke regne med utenlandsperioder
     * - hvis simuleringstype er 'pre-2025 offentlig AFP etterfulgt av alderspensjon',
     *   regne med trygdetid t.o.m. 31.12. i året som er 2 år før året for uttak av alderspensjon
     *   (ref. lovdata.no/lov/1997-02-28-19/§3-5: "Trygdetid i alderspensjoner medregnes fra utløpet av det året
     *    fastsettingen av formues- og inntektsskatt for det aktuelle året er ferdig.")
     */
    test("kapittel19TrygdetidsperiodeListe") {
        val result = Kapittel19TrygdetidsgrunnlagCreator.kapittel19TrygdetidsperiodeListe(
            opptjeningsgrunnlagListe = mutableListOf(
                Opptjeningsgrunnlag().apply {
                    ar = 2021
                    pp = 1.1
                },
                Opptjeningsgrunnlag().apply {
                    ar = 2022
                    pp = 1.2
                },
                Opptjeningsgrunnlag().apply {
                    ar = 2023
                    pp = 1.3
                }),
            utlandPeriodeListe = mutableListOf(
                UtlandPeriode(
                    fom = LocalDate.of(1962, 5, 29),
                    tom = LocalDate.of(1999, 12, 31),
                    // trygdetid starter dermed 2000-01-01
                    land = LandkodeEnum.LUX,
                    arbeidet = false
                )
            ),
            foedselsdato = LocalDate.of(1962, 5, 29),
            foersteUttakDato = LocalDate.of(2029, 6, 1)
        )

        result.size shouldBe 5
        Assert.trygdetidsperiode(periode = result[0], expectedFomAar = 2000, expectedTomAar = 2020)
        Assert.trygdetidsperiode(periode = result[1], expectedFomAar = 2021, expectedTomAar = 2021)
        Assert.trygdetidsperiode(periode = result[2], expectedFomAar = 2022, expectedTomAar = 2022)
        Assert.trygdetidsperiode(periode = result[3], expectedFomAar = 2023, expectedTomAar = 2023)
        Assert.trygdetidsperiode(periode = result[4], expectedFomAar = 2024, expectedTomAar = 2028)
    }
})

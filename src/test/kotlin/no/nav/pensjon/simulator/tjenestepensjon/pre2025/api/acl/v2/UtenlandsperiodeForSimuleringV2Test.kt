package no.nav.pensjon.simulator.tjenestepensjon.pre2025.api.acl.v2

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.core.domain.regler.enum.LandkodeEnum
import no.nav.pensjon.simulator.trygdetid.UtlandPeriode
import java.time.LocalDate
import java.time.Month

class UtenlandsperiodeForSimuleringV2Test : ShouldSpec({

    should("konvertere regulær landkode til opplistet landverdi") {
        UtenlandsperiodeForSimuleringV2(
            land = "SWE",
            periodeFom = LocalDate.of(2021, Month.JANUARY, 1)
        ).utlandPeriode() shouldBe UtlandPeriode(
            fom = LocalDate.of(2021, Month.JANUARY, 1),
            tom = null,
            land = LandkodeEnum.SWE,
            arbeidet = false
        )

        UtenlandsperiodeForSimuleringV2(
            land = "DNK",
            periodeFom = LocalDate.of(2022, Month.FEBRUARY, 2),
            periodeTom = LocalDate.of(2028, Month.NOVEMBER, 22),
            arbeidetIUtland = true
        ).utlandPeriode() shouldBe UtlandPeriode(
            fom = LocalDate.of(2022, Month.FEBRUARY, 2),
            tom = LocalDate.of(2028, Month.NOVEMBER, 22),
            land = LandkodeEnum.DNK,
            arbeidet = true
        )
    }

    should("konvertere irregulær landkode til opplistet landverdi") {
        UtenlandsperiodeForSimuleringV2(
            land = "???",
            periodeFom = LocalDate.of(2023, Month.MARCH, 3)
        ).utlandPeriode() shouldBe UtlandPeriode(
            fom = LocalDate.of(2023, Month.MARCH, 3),
            tom = null,
            land = LandkodeEnum.P_UKJENT,
            arbeidet = false
        )

        UtenlandsperiodeForSimuleringV2(
            land = "349",
            periodeFom = LocalDate.of(2021, Month.JANUARY, 1),
            arbeidetIUtland = true
        ).utlandPeriode() shouldBe UtlandPeriode(
            fom = LocalDate.of(2021, Month.JANUARY, 1),
            tom = null,
            land = LandkodeEnum.P_SPANSKE_OMR_AFRIKA,
            arbeidet = true
        )
    }
})
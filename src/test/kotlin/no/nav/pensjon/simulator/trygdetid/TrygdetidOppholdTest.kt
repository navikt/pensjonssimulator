package no.nav.pensjon.simulator.trygdetid

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.equality.shouldBeEqualToComparingFields
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.core.domain.regler.TTPeriode
import no.nav.pensjon.simulator.core.domain.regler.enum.LandkodeEnum
import no.nav.pensjon.simulator.core.util.toNorwegianDateAtNoon
import java.time.LocalDate

class TrygdetidOppholdTest : ShouldSpec({

    context("withPeriodeTom") {
        should("create new instance with default boolean values and periode with given t.o.m.-dato") {
            TrygdetidOpphold(
                periode = TTPeriode().apply {
                    fom = LocalDate.of(2021, 1, 1).toNorwegianDateAtNoon()
                    tom = LocalDate.of(2022, 12, 31).toNorwegianDateAtNoon()
                    poengIInnAr = true // NB value is ignored
                    poengIUtAr = true // NB value is ignored
                    landEnum = LandkodeEnum.EST
                    ikkeProRata = true // NB value is ignored
                    bruk = false // NB value is ignored
                },
                arbeidet = true
            ).withPeriodeTom(
                dato = LocalDate.of(2023, 11, 30).toNorwegianDateAtNoon()
            ) shouldBeEqualToComparingFields
                    TrygdetidOpphold(
                        periode = TTPeriode().apply {
                            fom = LocalDate.of(2021, 1, 1).toNorwegianDateAtNoon()
                            tom = LocalDate.of(2023, 11, 30).toNorwegianDateAtNoon()
                            poengIInnAr = false // NB always false
                            poengIUtAr = false // NB always false
                            landEnum = LandkodeEnum.EST
                            ikkeProRata = false // NB always false
                            bruk = true // NB always true
                        },
                        arbeidet = true
                    )
        }
    }

    context("dayBefore") {
        should("give the day before start of periode") {
            opphold(fom = LocalDate.of(2021, 1, 1)).dayBefore() shouldBe
                    LocalDate.of(2020, 12, 31).toNorwegianDateAtNoon()
        }
    }

    context("dayAfter") {
        should("give the day after end of periode") {
            opphold(tom = LocalDate.of(2020, 12, 31)).dayAfter() shouldBe
                    LocalDate.of(2021, 1, 1).toNorwegianDateAtNoon()
        }

        should("give null if periode is endless") {
            opphold(tom = null).dayAfter() shouldBe null
        }
    }

    context("endsBefore") {
        should("true when end is before start of other periode") {
            opphold(tom = fortid).endsBefore(
                other = opphold(fom = framtid)
            ) shouldBe true
        }

        should("false when end coincides with start of other periode") {
            opphold(tom = idag).endsBefore(
                other = opphold(fom = idag)
            ) shouldBe false
        }

        should("false when periode is endless") {
            opphold(tom = null).endsBefore(
                other = opphold(fom = fortid)
            ) shouldBe false
        }
    }

    context("startsBeforeAndEndsIn") {
        should("give 'true' when starts before and the ends coincide") {
            opphold(fom = fortid, tom = framtid).startsBeforeAndEndsIn(
                other = opphold(fom = idag, tom = framtid)
            ) shouldBe true
        }

        /**
         * NB: This is controversial.
         */
        should("give 'false' when starts before and the other is endless") {
            opphold(fom = fortid, tom = snart).startsBeforeAndEndsIn(
                other = opphold(fom = idag, tom = null)
            ) shouldBe false
        }

        should("give 'false' when starts coincide") {
            opphold(fom = fortid, tom = idag).startsBeforeAndEndsIn(
                other = opphold(fom = fortid, tom = framtid)
            ) shouldBe false
        }

        should("give 'false' when ends after") {
            opphold(fom = fortid, tom = framtid).startsBeforeAndEndsIn(
                other = opphold(fom = idag, tom = snart)
            ) shouldBe false
        }

        should("give 'false' when endless") {
            opphold(fom = fortid, tom = null).startsBeforeAndEndsIn(
                other = opphold(fom = idag, tom = framtid)
            ) shouldBe false
        }

        should("give 'false' when both are endless") {
            opphold(fom = fortid, tom = null).startsBeforeAndEndsIn(
                other = opphold(fom = idag, tom = null)
            ) shouldBe false
        }
    }

    context("startsBeforeAndEndsAfter") {
        should("give 'false' when starts before and the ends coincide") {
            opphold(fom = fortid, tom = snart).startsBeforeAndEndsAfter(
                other = opphold(fom = idag, tom = snart)
            ) shouldBe false
        }

        /**
         * NB: This is controversial.
         */
        should("give 'true' when starts before and the other is endless") {
            opphold(fom = fortid, tom = framtid).startsBeforeAndEndsAfter(
                other = opphold(fom = idag, tom = null)
            ) shouldBe true
        }

        should("give 'false' when starts coincide") {
            opphold(fom = fortid, tom = framtid).startsBeforeAndEndsAfter(
                other = opphold(fom = fortid, tom = idag)
            ) shouldBe false
        }

        should("give 'true' when endless") {
            opphold(fom = fortid, tom = null).startsBeforeAndEndsAfter(
                other = opphold(fom = idag, tom = framtid)
            ) shouldBe true
        }

        /**
         * NB: This is controversial.
         */
        should("give 'true' when both are endless") {
            opphold(fom = fortid, tom = null).startsBeforeAndEndsAfter(
                other = opphold(fom = idag, tom = null)
            ) shouldBe true
        }
    }

    context("startsAndEndsIn") {
        should("give 'false' when starts before") {
            opphold(fom = fortid, tom = snart).startsAndEndsIn(
                other = opphold(fom = idag, tom = framtid)
            ) shouldBe false
        }

        /**
         * NB: This is controversial.
         */
        should("give 'false' when starts in and the other is endless") {
            opphold(fom = idag, tom = framtid).startsAndEndsIn(
                other = opphold(fom = fortid, tom = null)
            ) shouldBe false
        }

        should("give 'true' when starts and ends respectively coincide") {
            opphold(fom = idag, tom = snart).startsAndEndsIn(
                other = opphold(fom = idag, tom = snart)
            ) shouldBe true
        }

        should("give 'false' when ends after") {
            opphold(fom = snart, tom = framtid).startsAndEndsIn(
                other = opphold(fom = fortid, tom = idag)
            ) shouldBe false
        }

        should("give 'false' when endless") {
            opphold(fom = snart, tom = null).startsAndEndsIn(
                other = opphold(fom = idag, tom = framtid)
            ) shouldBe false
        }

        should("give 'false' when both are endless") {
            opphold(fom = fortid, tom = null).startsAndEndsIn(
                other = opphold(fom = idag, tom = null)
            ) shouldBe false
        }
    }

    context("startsInAndEndsAfter") {
        should("give 'false' when starts before") {
            opphold(fom = fortid, tom = framtid).startsInAndEndsAfter(
                other = opphold(fom = idag, tom = idag)
            ) shouldBe false
        }

        should("give 'false' when starts in and the other is endless") {
            opphold(fom = idag, tom = framtid).startsInAndEndsAfter(
                other = opphold(fom = fortid, tom = null)
            ) shouldBe false
        }

        should("give 'true' when starts coincide and ends after") {
            opphold(fom = fortid, tom = framtid).startsInAndEndsAfter(
                other = opphold(fom = fortid, tom = idag)
            ) shouldBe true
        }

        should("give 'true' when starts in and is endless") {
            opphold(fom = idag, tom = null).startsInAndEndsAfter(
                other = opphold(fom = fortid, tom = framtid)
            ) shouldBe true
        }

        should("give 'false' when both are endless") {
            opphold(fom = idag, tom = null).startsInAndEndsAfter(
                other = opphold(fom = fortid, tom = null)
            ) shouldBe false
        }
    }
})

private val fortid: LocalDate = date(month = 1)
private val idag: LocalDate = date(month = 6)
private val snart: LocalDate = date(month = 7)
private val framtid: LocalDate = date(month = 12)

private fun date(month: Int): LocalDate =
    LocalDate.of(2021, month, 1)

private fun opphold(fom: LocalDate? = null, tom: LocalDate? = null) =
    TrygdetidOpphold(
        periode = TTPeriode().apply {
            this.fom = fom?.toNorwegianDateAtNoon()
            this.tom = tom?.toNorwegianDateAtNoon()
        },
        arbeidet = false
    )

package no.nav.pensjon.simulator.core.beregn

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.core.domain.regler.Trygdetid
import no.nav.pensjon.simulator.core.domain.regler.enum.BeholdningtypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.GrunnlagsrolleEnum
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.*
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.testutil.TestDateUtil.dateAtNoon
import java.time.LocalDate
import java.util.*

class BehandlingPeriodeUtilTest : FunSpec({

    val personDetaljer = mutableListOf(
        PersonDetalj().apply {
            bruk = true
            virkFom = dateAtNoon(2101, Calendar.JANUARY, 1) // any non-null value will do
            grunnlagsrolleEnum = GrunnlagsrolleEnum.MOR // => usePersongrunnlag = true
        }
    )

    test("periodiserGrunnlag should sort uttaksgrader in reverse start date order") {
        val kravhode = BehandlingPeriodeUtil.periodiserGrunnlag(
            virkningFom = null,
            virkningTom = null,
            originalKravhode = Kravhode().apply {
                uttaksgradListe = mutableListOf(
                        Uttaksgrad().apply {
                            uttaksgrad = 50
                            fomDato = dateAtNoon(2022, Calendar.JANUARY, 1) // second
                        },
                        Uttaksgrad().apply {
                            uttaksgrad = 20
                            fomDato = null // first
                        },
                        Uttaksgrad().apply {
                            uttaksgrad = 80
                            fomDato = dateAtNoon(2023, Calendar.JANUARY, 1) // third
                        },
                        Uttaksgrad().apply {
                            uttaksgrad = 40
                            fomDato = dateAtNoon(2024, Calendar.JANUARY, 1) // fourth
                        }
                )
            },
            periodiserFomTomDatoUtenUnntak = false,
            sakType = null // sakType has no effect
        )

        with(kravhode) {
            uttaksgradListe[0].uttaksgrad shouldBe 40 // fourth
            uttaksgradListe[1].uttaksgrad shouldBe 80 // third
            uttaksgradListe[2].uttaksgrad shouldBe 50 // second
            uttaksgradListe[3].uttaksgrad shouldBe 20 // first
        }
    }

    test("periodiserGrunnlag should remove trygdetider not covering virkning-start") {
        val kravhode = BehandlingPeriodeUtil.periodiserGrunnlag(
            virkningFom = LocalDate.of(2024, 1, 1), // virkning-start
            virkningTom = null, // virkningTom has no effect
            originalKravhode = Kravhode().apply {
                persongrunnlagListe = mutableListOf(
                    Persongrunnlag().apply {
                        personDetaljListe = personDetaljer
                        trygdetider = mutableListOf(
                            Trygdetid().apply {
                                virkFom = dateAtNoon(2023, Calendar.JANUARY, 1)
                                virkTom = dateAtNoon(2024, Calendar.JANUARY, 1) // covering
                            },
                            Trygdetid().apply {
                                virkFom = dateAtNoon(2024, Calendar.JANUARY, 2)
                                virkTom = dateAtNoon(2024, Calendar.DECEMBER, 31)
                            }
                        )
                    }
                )
            },
            periodiserFomTomDatoUtenUnntak = false,
            sakType = null // sakType has no effect
        )

        with(kravhode.persongrunnlagListe[0]) {
            trygdetider.size shouldBe 1
        }
    }

    /**
     * Følgende persongrunnlagsdata må ha overlappende gyldighet med virkningsperioden:
     * - utenlandsopphold
     * - faste utgifter for institusjonsopphold
     * - barnetilleggvurderingsperioder
     * - beholdninger
     * Hvis ikke, skal de fjernes
     */
    test("periodiserGrunnlag should remove non-intersecting persongrunnlag elements") {
        val kravhode = BehandlingPeriodeUtil.periodiserGrunnlag(
            virkningFom = LocalDate.of(2024, 1, 1), // virknings-
            virkningTom = LocalDate.of(2025, 1, 1), // -perioden
            originalKravhode = Kravhode().apply {
                persongrunnlagListe = mutableListOf(
                    Persongrunnlag().apply {
                        personDetaljListe = personDetaljer
                        utenlandsoppholdListe = mutableListOf(
                            Utenlandsopphold().apply {
                                fom = dateAtNoon(2023, Calendar.JANUARY, 1)
                                tom = dateAtNoon(2023, Calendar.DECEMBER, 31)
                            },
                            Utenlandsopphold().apply {
                                fom = dateAtNoon(2024, Calendar.JANUARY, 1) // overlap
                                tom = dateAtNoon(2024, Calendar.JANUARY, 1)
                            }
                        )
                        instOpphFasteUtgifterperiodeListe = mutableListOf(
                            InstOpphFasteUtgifterperiode().apply {
                                fom = dateAtNoon(2020, Calendar.JANUARY, 1) // overlap
                                tom = dateAtNoon(2029, Calendar.DECEMBER, 31)
                            },
                            InstOpphFasteUtgifterperiode().apply {
                                fom = dateAtNoon(2026, Calendar.JANUARY, 1)
                                tom = dateAtNoon(2026, Calendar.DECEMBER, 31)
                            }
                        )
                        barnetilleggVurderingsperioder = mutableListOf(
                            BarnetilleggVurderingsperiode().apply {
                                fomDato = dateAtNoon(2027, Calendar.JANUARY, 2)
                                tomDato = dateAtNoon(2027, Calendar.DECEMBER, 31)
                            },
                            BarnetilleggVurderingsperiode().apply {
                                fomDato = dateAtNoon(2025, Calendar.JANUARY, 1) // overlap
                                tomDato = dateAtNoon(2025, Calendar.DECEMBER, 31)
                            }
                        )
                        beholdninger = mutableListOf(
                            Pensjonsbeholdning().apply {
                                beholdningsTypeEnum = BeholdningtypeEnum.PEN_B
                                fom = dateAtNoon(2024, Calendar.JANUARY, 1) // overlap
                                tom = dateAtNoon(2025, Calendar.JANUARY, 1)
                            },
                            Pensjonsbeholdning().apply {
                                beholdningsTypeEnum = BeholdningtypeEnum.PEN_B
                                fom = dateAtNoon(2028, Calendar.JANUARY, 1)
                                tom = dateAtNoon(2028, Calendar.DECEMBER, 31)
                            }
                        )
                    }
                )
            },
            periodiserFomTomDatoUtenUnntak = false,
            sakType = null // sakType has no effect
        )

        with(kravhode.persongrunnlagListe[0]) {
            utenlandsoppholdListe.size shouldBe 1
            instOpphFasteUtgifterperiodeListe.size shouldBe 1
            barnetilleggVurderingsperioder.size shouldBe 1
            beholdninger.size shouldBe 1
        }
    }
})

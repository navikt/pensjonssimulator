package no.nav.pensjon.simulator.core.beregn

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.core.domain.regler.Trygdetid
import no.nav.pensjon.simulator.core.domain.regler.enum.BeholdningtypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.GrunnlagsrolleEnum
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.*
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import java.time.LocalDate

class BehandlingPeriodeUtilTest : FunSpec({

    val personDetaljer = mutableListOf(
        PersonDetalj().apply {
            bruk = true
            virkFom = LocalDate.of(2101, 1, 1) // any non-null value will do
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
                        fomDatoLd = LocalDate.of(2022, 1, 1) // second
                    },
                    Uttaksgrad().apply {
                        uttaksgrad = 20
                        fomDatoLd = null // first
                    },
                    Uttaksgrad().apply {
                        uttaksgrad = 80
                        fomDatoLd = LocalDate.of(2023, 1, 1) // third
                    },
                    Uttaksgrad().apply {
                        uttaksgrad = 40
                        fomDatoLd = LocalDate.of(2024, 1, 1) // fourth
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
                                virkFomLd = LocalDate.of(2023, 1, 1)
                                virkTomLd = LocalDate.of(2024, 1, 1) // covering
                            },
                            Trygdetid().apply {
                                virkFomLd = LocalDate.of(2024, 1, 2)
                                virkTomLd = LocalDate.of(2024, 12, 31)
                            }
                        )
                    }
                )
            },
            periodiserFomTomDatoUtenUnntak = false,
            sakType = null // sakType has no effect
        )

        kravhode.persongrunnlagListe[0].trygdetider shouldHaveSize 1
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
                                fomLd = LocalDate.of(2023, 1, 1)
                                tomLd = LocalDate.of(2023, 12, 31)
                            },
                            Utenlandsopphold().apply {
                                fomLd = LocalDate.of(2024, 1, 1) // overlap
                                tomLd = LocalDate.of(2024, 1, 1)
                            }
                        )
                        instOpphFasteUtgifterperiodeListe = mutableListOf(
                            InstOpphFasteUtgifterperiode().apply {
                                fomLd = LocalDate.of(2020, 1, 1) // overlap
                                tomLd = LocalDate.of(2029, 12, 31)
                            },
                            InstOpphFasteUtgifterperiode().apply {
                                fomLd = LocalDate.of(2026, 1, 1)
                                tomLd = LocalDate.of(2026, 12, 31)
                            }
                        )
                        barnetilleggVurderingsperioder = mutableListOf(
                            BarnetilleggVurderingsperiode().apply {
                                fomDatoLd = LocalDate.of(2027, 1, 2)
                                tomDatoLd = LocalDate.of(2027, 12, 31)
                            },
                            BarnetilleggVurderingsperiode().apply {
                                fomDatoLd = LocalDate.of(2025, 1, 1) // overlap
                                tomDatoLd = LocalDate.of(2025, 12, 31)
                            }
                        )
                        beholdninger = mutableListOf(
                            Pensjonsbeholdning().apply {
                                beholdningsTypeEnum = BeholdningtypeEnum.PEN_B
                                fomLd = LocalDate.of(2024, 1, 1) // overlap
                                tomLd = LocalDate.of(2025, 1, 1)
                            },
                            Pensjonsbeholdning().apply {
                                beholdningsTypeEnum = BeholdningtypeEnum.PEN_B
                                fomLd = LocalDate.of(2028, 1, 1)
                                tomLd = LocalDate.of(2028, 12, 31)
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

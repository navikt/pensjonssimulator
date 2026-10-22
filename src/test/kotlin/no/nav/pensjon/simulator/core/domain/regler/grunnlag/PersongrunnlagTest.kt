package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.maps.shouldHaveSize
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.core.domain.regler.Trygdetid
import java.time.LocalDate

class PersongrunnlagTest : ShouldSpec({

    context("latestTrygdetid") {
        should("velge trygdetid med nyeste virkningsdato") {
            Persongrunnlag().apply {
                trygdetider = mutableListOf(
                    trygdetid(aar = 2021),
                    trygdetid(aar = 2023), // nyeste
                    trygdetid(aar = 2022)
                )
            }.latestTrygdetid()?.virkFomLd?.year shouldBe 2023
        }
    }

    context("settTrygdetid") {
        should("beregne trygdetid, legge den til i liste, oppdatere nåværende trygdetid") {
            val initiellTrygdetid = Trygdetid().apply { tt = 10 }

            val persongrunnlag = Persongrunnlag().apply {
                fodselsdatoLd = LocalDate.of(1960, 1, 1)
                utenlandsoppholdListe = mutableListOf(
                    Utenlandsopphold().apply {
                        fomLd = LocalDate.of(1976, 2, 16)
                        tomLd = LocalDate.of(1991, 6, 18)
                    }
                )
                flyktning = false
                trygdetider = mutableListOf(initiellTrygdetid)
            }

            with(persongrunnlag) {
                settTrygdetid() shouldBe 36
                trygdetider shouldHaveSize 2
                trygdetider[0].tt shouldBe 10
                trygdetider[1].tt shouldBe 36
                trygdetid?.tt shouldBe 36
            }
        }
    }

    context("terminerUfoereperioder") {
        should("sette sluttdato på endeløse uføreperioder") {
            val persongrunnlag = Persongrunnlag().apply {
                uforeHistorikk = Uforehistorikk().apply {
                    uforeperiodeListe = mutableListOf(
                        Uforeperiode().apply {
                            ufgFomLd = LocalDate.of(2020, 1, 1)
                            ufgTomLd = LocalDate.of(2021, 12, 31)
                        },
                        Uforeperiode().apply {
                            ufgFomLd = LocalDate.of(2023, 1, 1)
                            ufgTomLd = null // endeløs
                        }
                    )
                }
            }

            persongrunnlag.terminerUfoereperioder(
                tom = LocalDate.of(2025, 12, 31)
            )

            with(persongrunnlag.uforeHistorikk!!) {
                uforeperiodeListe[0].ufgTomLd?.year shouldBe 2021 // uendret
                uforeperiodeListe[1].ufgTomLd?.year shouldBe 2025 // endret (terminert)
            }
        }
    }

    context("sistePensjonsbeholdningPerAar") {
        context("ingen faktiske (registrerte) beholdninger") {
            should("returnere beregnede beholdninger") {
                val persongrunnlag = Persongrunnlag().apply {
                    beholdninger = mutableListOf(
                        Pensjonsbeholdning().apply { ar = 2021 },
                        Pensjonsbeholdning().apply { ar = 2022 }
                    )
                }

                val sistePensjonsbeholdningPerAar = persongrunnlag.sistePensjonsbeholdningPerAar()

                sistePensjonsbeholdningPerAar shouldHaveSize 2
                sistePensjonsbeholdningPerAar[2021]?.aar shouldBe 2021
            }
        }

        context("faktiske (registrerte) beholdninger kun før beregnede beholdninger") {
            should("returnere alle faktiske og beregnede beholdninger") {
                val persongrunnlag = Persongrunnlag().apply {
                    faktiskeBeholdninger = mutableListOf(
                        Pensjonsbeholdning().apply { ar = 2020 }
                    )
                    beholdninger = mutableListOf(
                        Pensjonsbeholdning().apply { ar = 2021 },
                        Pensjonsbeholdning().apply { ar = 2022 }
                    )
                }

                val sistePensjonsbeholdningPerAar = persongrunnlag.sistePensjonsbeholdningPerAar()

                sistePensjonsbeholdningPerAar shouldHaveSize 3
                sistePensjonsbeholdningPerAar[2022]?.aar shouldBe 2022
            }
        }

        context("faktiske (registrerte) beholdninger overlapper helt med beregnede beholdninger") {
            should("returnere faktiske beholdninger") {
                val persongrunnlag = Persongrunnlag().apply {
                    faktiskeBeholdninger = mutableListOf(
                        Pensjonsbeholdning().apply { ar = 2021; totalbelop = 1001.0 },
                        Pensjonsbeholdning().apply { ar = 2022; totalbelop = 1002.0 }
                    )
                    beholdninger = mutableListOf(
                        Pensjonsbeholdning().apply { ar = 2021; totalbelop = 2001.0 },
                        Pensjonsbeholdning().apply { ar = 2022; totalbelop = 2002.0 }
                    )
                }

                val sistePensjonsbeholdningPerAar = persongrunnlag.sistePensjonsbeholdningPerAar()

                sistePensjonsbeholdningPerAar shouldHaveSize 2
                sistePensjonsbeholdningPerAar[2021]?.totalbeloep shouldBe 1001.0
                sistePensjonsbeholdningPerAar[2022]?.totalbeloep shouldBe 1002.0
            }
        }

        context("faktiske (registrerte) beholdninger overlapper delvis med beregnede beholdninger") {
            should("returnere faktiske beholdninger, og beregnede for år der faktiske mangler") {
                val persongrunnlag = Persongrunnlag().apply {
                    faktiskeBeholdninger = mutableListOf(
                        Pensjonsbeholdning().apply { ar = 2021; totalbelop = 1001.0 },
                        Pensjonsbeholdning().apply { ar = 2022; totalbelop = 1002.0 }
                    )
                    beholdninger = mutableListOf(
                        Pensjonsbeholdning().apply { ar = 2022; totalbelop = 2001.0 },
                        Pensjonsbeholdning().apply { ar = 2023; totalbelop = 2002.0 }
                    )
                }

                val sistePensjonsbeholdningPerAar = persongrunnlag.sistePensjonsbeholdningPerAar()

                sistePensjonsbeholdningPerAar shouldHaveSize 3
                sistePensjonsbeholdningPerAar[2021]?.totalbeloep shouldBe 1001.0
                sistePensjonsbeholdningPerAar[2022]?.totalbeloep shouldBe 1002.0
                sistePensjonsbeholdningPerAar[2023]?.totalbeloep shouldBe 2002.0
            }
        }

        context("flere faktiske (registrerte) beholdninger for enkeltår") {
            should("velge siste beholdning (høyest f.o.m.-dato) for året") {
                val persongrunnlag = Persongrunnlag().apply {
                    faktiskeBeholdninger = mutableListOf(
                        pensjonsbeholdning(aar = 2022, maaned = 5, beloep = 2005), // siste beholdning i 2022
                        pensjonsbeholdning(aar = 2021, maaned = 1, beloep = 1001),
                        pensjonsbeholdning(aar = 2021, maaned = 12, beloep = 1012), // siste beholdning i 2021
                        pensjonsbeholdning(aar = 2021, maaned = 5, beloep = 1005),
                        pensjonsbeholdning(aar = 2022, maaned = 1, beloep = 2001)
                    )
                }

                val sistePensjonsbeholdningPerAar = persongrunnlag.sistePensjonsbeholdningPerAar()

                sistePensjonsbeholdningPerAar shouldHaveSize 2
                sistePensjonsbeholdningPerAar[2021]?.totalbeloep shouldBe 1012.0
                sistePensjonsbeholdningPerAar[2022]?.totalbeloep shouldBe 2005.0
            }
        }
    }

    context("'copy' constructor") {
        should("kopiere beholdninger, inkludert faktiske") {
            val original = Persongrunnlag().apply {
                beholdninger = mutableListOf(
                    Pensjonsbeholdning().apply { ar = 2025 },
                    Pensjonsbeholdning().apply { ar = 2026 }
                )
                faktiskeBeholdninger = listOf(
                    Pensjonsbeholdning().apply { ar = 2022 },
                    Pensjonsbeholdning().apply { ar = 2023 },
                    Pensjonsbeholdning().apply { ar = 2024 }
                )
            }

            val kopi = Persongrunnlag(original)

            with(kopi) {
                beholdninger shouldHaveSize 2
                beholdninger[0].ar shouldBe 2025
                beholdninger[1].ar shouldBe 2026
                faktiskeBeholdninger shouldHaveSize 3
                faktiskeBeholdninger[0].ar shouldBe 2022
                faktiskeBeholdninger[1].ar shouldBe 2023
                faktiskeBeholdninger[2].ar shouldBe 2024
            }
        }
    }
})

private fun pensjonsbeholdning(aar: Int, maaned: Int, beloep: Int) =
    Pensjonsbeholdning().apply {
        fomLd = LocalDate.of(aar, maaned, 1)
        totalbelop = beloep.toDouble()
    }

private fun trygdetid(aar: Int) =
    Trygdetid().apply { virkFomLd = LocalDate.of(aar, 1, 1) }

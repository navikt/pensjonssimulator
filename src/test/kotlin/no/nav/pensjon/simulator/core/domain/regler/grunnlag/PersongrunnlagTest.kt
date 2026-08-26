package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldHaveSize
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
})

private fun trygdetid(aar: Int) =
    Trygdetid().apply { virkFomLd = LocalDate.of(aar, 1, 1) }

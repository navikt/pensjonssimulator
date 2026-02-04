package no.nav.pensjon.simulator.core.virkning

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import no.nav.pensjon.simulator.core.domain.regler.PenPerson
import no.nav.pensjon.simulator.core.domain.regler.enum.GrunnlagsrolleEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.KravlinjeTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.ForsteVirkningsdatoGrunnlag
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.PersonDetalj
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Persongrunnlag
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravlinje
import no.nav.pensjon.simulator.testutil.TestDateUtil.dateAtNoon
import java.time.LocalDate
import java.util.*

class FoersteVirkningDatoRepopulatorTest : FunSpec({

    // Tests for copy() - indirectly tested through mapFoersteVirkningDatoGrunnlagTransfer
    test("copy kopierer alle felter fra eksisterende datogrunnlag") {
        val soeker = createPenPerson(1L)
        val annenPerson = createPenPerson(2L)
        val originalGrunnlag = ForsteVirkningsdatoGrunnlag().apply {
            virkningsdato = dateAtNoon(2024, Calendar.JUNE, 1)
            kravFremsattDato = dateAtNoon(2024, Calendar.MAY, 15)
            bruker = soeker
            this.annenPerson = annenPerson
            kravlinjeTypeEnum = KravlinjeTypeEnum.AP
        }

        val kravhode = Kravhode().apply {
            persongrunnlagListe = mutableListOf(
                createSoekerPersongrunnlag(soeker).apply {
                    forsteVirkningsdatoGrunnlagListe = mutableListOf(originalGrunnlag)
                }
            )
            kravlinjeListe = mutableListOf() // Tom liste - ingen nye grunnlag legges til
        }

        FoersteVirkningDatoRepopulator.mapFoersteVirkningDatoGrunnlagTransfer(kravhode)

        val copiedGrunnlag = kravhode.persongrunnlagListe[0].forsteVirkningsdatoGrunnlagListe[0]
        copiedGrunnlag.virkningsdato shouldBe dateAtNoon(2024, Calendar.JUNE, 1)
        copiedGrunnlag.kravFremsattDato shouldBe dateAtNoon(2024, Calendar.MAY, 15)
        copiedGrunnlag.bruker shouldBe soeker
        copiedGrunnlag.annenPerson shouldBe annenPerson
        copiedGrunnlag.kravlinjeTypeEnum shouldBe KravlinjeTypeEnum.AP
    }

    // Tests for datoGrunnlag() - indirectly tested
    test("datoGrunnlag oppretter nytt grunnlag basert på kravlinje") {
        val soeker = createPenPerson(1L)
        val kravhode = Kravhode().apply {
            onsketVirkningsdato = LocalDate.of(2024, 6, 1)
            kravFremsattDato = dateAtNoon(2024, Calendar.MAY, 15)
            persongrunnlagListe = mutableListOf(createSoekerPersongrunnlag(soeker))
            kravlinjeListe = mutableListOf(
                Kravlinje().apply {
                    kravlinjeTypeEnum = KravlinjeTypeEnum.AP
                }
            )
        }

        FoersteVirkningDatoRepopulator.mapFoersteVirkningDatoGrunnlagTransfer(kravhode)

        val grunnlagListe = kravhode.persongrunnlagListe[0].forsteVirkningsdatoGrunnlagListe
        grunnlagListe shouldHaveSize 1
        grunnlagListe[0].virkningsdato shouldNotBe null
        grunnlagListe[0].kravFremsattDato shouldBe dateAtNoon(2024, Calendar.MAY, 15)
        grunnlagListe[0].bruker shouldBe soeker
        grunnlagListe[0].kravlinjeTypeEnum shouldBe KravlinjeTypeEnum.AP
    }

    test("datoGrunnlag setter annenPerson kun for forsørgingstillegg (ET)") {
        val soeker = createPenPerson(1L)
        val relatert = createPenPerson(2L)
        val kravhode = Kravhode().apply {
            onsketVirkningsdato = LocalDate.of(2024, 6, 1)
            persongrunnlagListe = mutableListOf(createSoekerPersongrunnlag(soeker))
            kravlinjeListe = mutableListOf(
                Kravlinje().apply {
                    kravlinjeTypeEnum = KravlinjeTypeEnum.ET
                    relatertPerson = relatert
                }
            )
        }

        FoersteVirkningDatoRepopulator.mapFoersteVirkningDatoGrunnlagTransfer(kravhode)

        val grunnlag = kravhode.persongrunnlagListe[0].forsteVirkningsdatoGrunnlagListe[0]
        grunnlag.annenPerson shouldBe relatert
        grunnlag.kravlinjeTypeEnum shouldBe KravlinjeTypeEnum.ET
    }

    test("datoGrunnlag setter annenPerson kun for forsørgingstillegg (BT)") {
        val soeker = createPenPerson(1L)
        val relatert = createPenPerson(2L)
        val kravhode = Kravhode().apply {
            onsketVirkningsdato = LocalDate.of(2024, 6, 1)
            persongrunnlagListe = mutableListOf(createSoekerPersongrunnlag(soeker))
            kravlinjeListe = mutableListOf(
                Kravlinje().apply {
                    kravlinjeTypeEnum = KravlinjeTypeEnum.BT
                    relatertPerson = relatert
                }
            )
        }

        FoersteVirkningDatoRepopulator.mapFoersteVirkningDatoGrunnlagTransfer(kravhode)

        val grunnlag = kravhode.persongrunnlagListe[0].forsteVirkningsdatoGrunnlagListe[0]
        grunnlag.annenPerson shouldBe relatert
    }

    test("datoGrunnlag setter ikke annenPerson for ikke-forsørgingstillegg") {
        val soeker = createPenPerson(1L)
        val relatert = createPenPerson(2L)
        val kravhode = Kravhode().apply {
            onsketVirkningsdato = LocalDate.of(2024, 6, 1)
            persongrunnlagListe = mutableListOf(createSoekerPersongrunnlag(soeker))
            kravlinjeListe = mutableListOf(
                Kravlinje().apply {
                    kravlinjeTypeEnum = KravlinjeTypeEnum.AP
                    relatertPerson = relatert
                }
            )
        }

        FoersteVirkningDatoRepopulator.mapFoersteVirkningDatoGrunnlagTransfer(kravhode)

        val grunnlag = kravhode.persongrunnlagListe[0].forsteVirkningsdatoGrunnlagListe[0]
        grunnlag.annenPerson shouldBe null
    }

    // Tests for hasMatchingAnnenPerson() - indirectly tested
    test("hasMatchingAnnenPerson forhindrer duplikater med samme kravlinjetype og bruker") {
        val soeker = createPenPerson(1L)
        val eksisterendeGrunnlag = ForsteVirkningsdatoGrunnlag().apply {
            bruker = soeker
            kravlinjeTypeEnum = KravlinjeTypeEnum.AP
        }
        val kravhode = Kravhode().apply {
            onsketVirkningsdato = LocalDate.of(2024, 6, 1)
            persongrunnlagListe = mutableListOf(
                createSoekerPersongrunnlag(soeker).apply {
                    forsteVirkningsdatoGrunnlagListe = mutableListOf(eksisterendeGrunnlag)
                }
            )
            kravlinjeListe = mutableListOf(
                Kravlinje().apply {
                    kravlinjeTypeEnum = KravlinjeTypeEnum.AP
                }
            )
        }

        FoersteVirkningDatoRepopulator.mapFoersteVirkningDatoGrunnlagTransfer(kravhode)

        // Skal ikke legge til duplikat
        kravhode.persongrunnlagListe[0].forsteVirkningsdatoGrunnlagListe shouldHaveSize 1
    }

    test("hasMatchingAnnenPerson tillater forskjellige kravlinjetyper") {
        val soeker = createPenPerson(1L)
        val eksisterendeGrunnlag = ForsteVirkningsdatoGrunnlag().apply {
            bruker = soeker
            kravlinjeTypeEnum = KravlinjeTypeEnum.AP
        }
        val kravhode = Kravhode().apply {
            onsketVirkningsdato = LocalDate.of(2024, 6, 1)
            persongrunnlagListe = mutableListOf(
                createSoekerPersongrunnlag(soeker).apply {
                    forsteVirkningsdatoGrunnlagListe = mutableListOf(eksisterendeGrunnlag)
                }
            )
            kravlinjeListe = mutableListOf(
                Kravlinje().apply {
                    kravlinjeTypeEnum = KravlinjeTypeEnum.UP
                }
            )
        }

        FoersteVirkningDatoRepopulator.mapFoersteVirkningDatoGrunnlagTransfer(kravhode)

        kravhode.persongrunnlagListe[0].forsteVirkningsdatoGrunnlagListe shouldHaveSize 2
    }

    test("hasMatchingAnnenPerson sjekker annenPerson for forsørgingstillegg") {
        val soeker = createPenPerson(1L)
        val annenPerson1 = createPenPerson(2L)
        val annenPerson2 = createPenPerson(3L)
        val eksisterendeGrunnlag = ForsteVirkningsdatoGrunnlag().apply {
            bruker = soeker
            annenPerson = annenPerson1
            kravlinjeTypeEnum = KravlinjeTypeEnum.ET
        }
        val kravhode = Kravhode().apply {
            onsketVirkningsdato = LocalDate.of(2024, 6, 1)
            persongrunnlagListe = mutableListOf(
                createSoekerPersongrunnlag(soeker).apply {
                    forsteVirkningsdatoGrunnlagListe = mutableListOf(eksisterendeGrunnlag)
                }
            )
            kravlinjeListe = mutableListOf(
                Kravlinje().apply {
                    kravlinjeTypeEnum = KravlinjeTypeEnum.ET
                    relatertPerson = annenPerson2
                }
            )
        }

        FoersteVirkningDatoRepopulator.mapFoersteVirkningDatoGrunnlagTransfer(kravhode)

        // Skal legge til fordi annenPerson er forskjellig
        kravhode.persongrunnlagListe[0].forsteVirkningsdatoGrunnlagListe shouldHaveSize 2
    }

    // Tests for gjelderAvdoed(Persongrunnlag)
    test("gjelderAvdoed returnerer true for persongrunnlag med rolle AVDOD") {
        val soeker = createPenPerson(1L)
        val avdoed = createPenPerson(2L)
        val avdoedPersongrunnlag = Persongrunnlag().apply {
            penPerson = avdoed
            personDetaljListe = mutableListOf(
                PersonDetalj().apply {
                    grunnlagsrolleEnum = GrunnlagsrolleEnum.AVDOD
                    bruk = true
                }
            )
        }
        val soekerGrunnlag = ForsteVirkningsdatoGrunnlag().apply {
            bruker = soeker
            kravlinjeTypeEnum = KravlinjeTypeEnum.GJP // Gjelder avdød
        }
        val kravhode = Kravhode().apply {
            persongrunnlagListe = mutableListOf(
                createSoekerPersongrunnlag(soeker).apply {
                    forsteVirkningsdatoGrunnlagListe = mutableListOf(soekerGrunnlag)
                },
                avdoedPersongrunnlag
            )
            kravlinjeListe = mutableListOf()
        }

        FoersteVirkningDatoRepopulator.mapFoersteVirkningDatoGrunnlagTransfer(kravhode)

        // Avdød persongrunnlag skal få kopiert søkers datogrunnlag som gjelder avdød
        avdoedPersongrunnlag.forsteVirkningsdatoGrunnlagListe shouldHaveSize 1
    }

    test("gjelderAvdoed returnerer true for persongrunnlag med rolle FAR") {
        val soeker = createPenPerson(1L)
        val far = createPenPerson(2L)
        val farPersongrunnlag = Persongrunnlag().apply {
            penPerson = far
            personDetaljListe = mutableListOf(
                PersonDetalj().apply {
                    grunnlagsrolleEnum = GrunnlagsrolleEnum.FAR
                    bruk = true
                }
            )
        }
        val soekerGrunnlag = ForsteVirkningsdatoGrunnlag().apply {
            bruker = soeker
            kravlinjeTypeEnum = KravlinjeTypeEnum.BP // Barnepensjon - gjelder avdød
        }
        val kravhode = Kravhode().apply {
            persongrunnlagListe = mutableListOf(
                createSoekerPersongrunnlag(soeker).apply {
                    forsteVirkningsdatoGrunnlagListe = mutableListOf(soekerGrunnlag)
                },
                farPersongrunnlag
            )
            kravlinjeListe = mutableListOf()
        }

        FoersteVirkningDatoRepopulator.mapFoersteVirkningDatoGrunnlagTransfer(kravhode)

        farPersongrunnlag.forsteVirkningsdatoGrunnlagListe shouldHaveSize 1
    }

    test("gjelderAvdoed returnerer true for persongrunnlag med rolle MOR") {
        val soeker = createPenPerson(1L)
        val mor = createPenPerson(2L)
        val morPersongrunnlag = Persongrunnlag().apply {
            penPerson = mor
            personDetaljListe = mutableListOf(
                PersonDetalj().apply {
                    grunnlagsrolleEnum = GrunnlagsrolleEnum.MOR
                    bruk = true
                }
            )
        }
        val soekerGrunnlag = ForsteVirkningsdatoGrunnlag().apply {
            bruker = soeker
            kravlinjeTypeEnum = KravlinjeTypeEnum.BP
        }
        val kravhode = Kravhode().apply {
            persongrunnlagListe = mutableListOf(
                createSoekerPersongrunnlag(soeker).apply {
                    forsteVirkningsdatoGrunnlagListe = mutableListOf(soekerGrunnlag)
                },
                morPersongrunnlag
            )
            kravlinjeListe = mutableListOf()
        }

        FoersteVirkningDatoRepopulator.mapFoersteVirkningDatoGrunnlagTransfer(kravhode)

        morPersongrunnlag.forsteVirkningsdatoGrunnlagListe shouldHaveSize 1
    }

    test("gjelderAvdoed returnerer false for persongrunnlag uten avdød-rolle") {
        val soeker = createPenPerson(1L)
        val ektef = createPenPerson(2L)
        val ektfPersongrunnlag = Persongrunnlag().apply {
            penPerson = ektef
            personDetaljListe = mutableListOf(
                PersonDetalj().apply {
                    grunnlagsrolleEnum = GrunnlagsrolleEnum.EKTEF
                    bruk = true
                }
            )
        }
        val soekerGrunnlag = ForsteVirkningsdatoGrunnlag().apply {
            bruker = soeker
            kravlinjeTypeEnum = KravlinjeTypeEnum.GJP
        }
        val kravhode = Kravhode().apply {
            persongrunnlagListe = mutableListOf(
                createSoekerPersongrunnlag(soeker).apply {
                    forsteVirkningsdatoGrunnlagListe = mutableListOf(soekerGrunnlag)
                },
                ektfPersongrunnlag
            )
            kravlinjeListe = mutableListOf()
        }

        FoersteVirkningDatoRepopulator.mapFoersteVirkningDatoGrunnlagTransfer(kravhode)

        // Ektefelle skal ikke få kopiert datogrunnlag
        ektfPersongrunnlag.forsteVirkningsdatoGrunnlagListe shouldHaveSize 0
    }

    test("gjelderAvdoed ignorerer persondetalj med bruk=false") {
        val soeker = createPenPerson(1L)
        val avdoed = createPenPerson(2L)
        val avdoedPersongrunnlag = Persongrunnlag().apply {
            penPerson = avdoed
            personDetaljListe = mutableListOf(
                PersonDetalj().apply {
                    grunnlagsrolleEnum = GrunnlagsrolleEnum.AVDOD
                    bruk = false // Ikke i bruk
                }
            )
        }
        val soekerGrunnlag = ForsteVirkningsdatoGrunnlag().apply {
            bruker = soeker
            kravlinjeTypeEnum = KravlinjeTypeEnum.GJP
        }
        val kravhode = Kravhode().apply {
            persongrunnlagListe = mutableListOf(
                createSoekerPersongrunnlag(soeker).apply {
                    forsteVirkningsdatoGrunnlagListe = mutableListOf(soekerGrunnlag)
                },
                avdoedPersongrunnlag
            )
            kravlinjeListe = mutableListOf()
        }

        FoersteVirkningDatoRepopulator.mapFoersteVirkningDatoGrunnlagTransfer(kravhode)

        // Avdød med bruk=false skal ikke få kopiert datogrunnlag
        avdoedPersongrunnlag.forsteVirkningsdatoGrunnlagListe shouldHaveSize 0
    }

    // Tests for gjelderAvdoed(ForsteVirkningsdatoGrunnlag)
    test("gjelderAvdoed for datogrunnlag returnerer true for BP kravlinjetype") {
        val soeker = createPenPerson(1L)
        val avdoed = createPenPerson(2L)
        val avdoedPersongrunnlag = createAvdoedPersongrunnlag(avdoed)
        val soekerGrunnlag = ForsteVirkningsdatoGrunnlag().apply {
            bruker = soeker
            kravlinjeTypeEnum = KravlinjeTypeEnum.BP
        }
        val kravhode = Kravhode().apply {
            persongrunnlagListe = mutableListOf(
                createSoekerPersongrunnlag(soeker).apply {
                    forsteVirkningsdatoGrunnlagListe = mutableListOf(soekerGrunnlag)
                },
                avdoedPersongrunnlag
            )
            kravlinjeListe = mutableListOf()
        }

        FoersteVirkningDatoRepopulator.mapFoersteVirkningDatoGrunnlagTransfer(kravhode)

        avdoedPersongrunnlag.forsteVirkningsdatoGrunnlagListe shouldHaveSize 1
        avdoedPersongrunnlag.forsteVirkningsdatoGrunnlagListe[0].kravlinjeTypeEnum shouldBe KravlinjeTypeEnum.BP
    }

    test("gjelderAvdoed for datogrunnlag returnerer true for GJP kravlinjetype") {
        val soeker = createPenPerson(1L)
        val avdoed = createPenPerson(2L)
        val avdoedPersongrunnlag = createAvdoedPersongrunnlag(avdoed)
        val soekerGrunnlag = ForsteVirkningsdatoGrunnlag().apply {
            bruker = soeker
            kravlinjeTypeEnum = KravlinjeTypeEnum.GJP
        }
        val kravhode = Kravhode().apply {
            persongrunnlagListe = mutableListOf(
                createSoekerPersongrunnlag(soeker).apply {
                    forsteVirkningsdatoGrunnlagListe = mutableListOf(soekerGrunnlag)
                },
                avdoedPersongrunnlag
            )
            kravlinjeListe = mutableListOf()
        }

        FoersteVirkningDatoRepopulator.mapFoersteVirkningDatoGrunnlagTransfer(kravhode)

        avdoedPersongrunnlag.forsteVirkningsdatoGrunnlagListe shouldHaveSize 1
    }

    test("gjelderAvdoed for datogrunnlag returnerer true for GJR kravlinjetype") {
        val soeker = createPenPerson(1L)
        val avdoed = createPenPerson(2L)
        val avdoedPersongrunnlag = createAvdoedPersongrunnlag(avdoed)
        val soekerGrunnlag = ForsteVirkningsdatoGrunnlag().apply {
            bruker = soeker
            kravlinjeTypeEnum = KravlinjeTypeEnum.GJR
        }
        val kravhode = Kravhode().apply {
            persongrunnlagListe = mutableListOf(
                createSoekerPersongrunnlag(soeker).apply {
                    forsteVirkningsdatoGrunnlagListe = mutableListOf(soekerGrunnlag)
                },
                avdoedPersongrunnlag
            )
            kravlinjeListe = mutableListOf()
        }

        FoersteVirkningDatoRepopulator.mapFoersteVirkningDatoGrunnlagTransfer(kravhode)

        avdoedPersongrunnlag.forsteVirkningsdatoGrunnlagListe shouldHaveSize 1
    }

    test("gjelderAvdoed for datogrunnlag returnerer true for UT_GJT kravlinjetype") {
        val soeker = createPenPerson(1L)
        val avdoed = createPenPerson(2L)
        val avdoedPersongrunnlag = createAvdoedPersongrunnlag(avdoed)
        val soekerGrunnlag = ForsteVirkningsdatoGrunnlag().apply {
            bruker = soeker
            kravlinjeTypeEnum = KravlinjeTypeEnum.UT_GJT
        }
        val kravhode = Kravhode().apply {
            persongrunnlagListe = mutableListOf(
                createSoekerPersongrunnlag(soeker).apply {
                    forsteVirkningsdatoGrunnlagListe = mutableListOf(soekerGrunnlag)
                },
                avdoedPersongrunnlag
            )
            kravlinjeListe = mutableListOf()
        }

        FoersteVirkningDatoRepopulator.mapFoersteVirkningDatoGrunnlagTransfer(kravhode)

        avdoedPersongrunnlag.forsteVirkningsdatoGrunnlagListe shouldHaveSize 1
    }

    test("gjelderAvdoed for datogrunnlag returnerer false for AP kravlinjetype") {
        val soeker = createPenPerson(1L)
        val avdoed = createPenPerson(2L)
        val avdoedPersongrunnlag = createAvdoedPersongrunnlag(avdoed)
        val soekerGrunnlag = ForsteVirkningsdatoGrunnlag().apply {
            bruker = soeker
            kravlinjeTypeEnum = KravlinjeTypeEnum.AP
        }
        val kravhode = Kravhode().apply {
            persongrunnlagListe = mutableListOf(
                createSoekerPersongrunnlag(soeker).apply {
                    forsteVirkningsdatoGrunnlagListe = mutableListOf(soekerGrunnlag)
                },
                avdoedPersongrunnlag
            )
            kravlinjeListe = mutableListOf()
        }

        FoersteVirkningDatoRepopulator.mapFoersteVirkningDatoGrunnlagTransfer(kravhode)

        // AP gjelder ikke avdød, så avdød skal ikke få kopiert dette grunnlaget
        avdoedPersongrunnlag.forsteVirkningsdatoGrunnlagListe shouldHaveSize 0
    }
})

private fun createPenPerson(id: Long) = PenPerson(penPersonId = id)

private fun createSoekerPersongrunnlag(soeker: PenPerson) = Persongrunnlag().apply {
    penPerson = soeker
    personDetaljListe = mutableListOf(
        PersonDetalj().apply {
            grunnlagsrolleEnum = GrunnlagsrolleEnum.SOKER
            bruk = true
        }
    )
}

private fun createAvdoedPersongrunnlag(avdoed: PenPerson) = Persongrunnlag().apply {
    penPerson = avdoed
    personDetaljListe = mutableListOf(
        PersonDetalj().apply {
            grunnlagsrolleEnum = GrunnlagsrolleEnum.AVDOD
            bruk = true
        }
    )
}

package no.nav.pensjon.simulator.trygdetid

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import no.nav.pensjon.simulator.core.domain.regler.enum.LandkodeEnum
import java.time.LocalDate
import java.util.Date

class TrygdetidGrunnlagFactoryTest : FunSpec({

    context("trygdetidPeriode med LocalDate fom og tom") {

        test("oppretter TTPeriode med standardverdier for Norge") {
            val fom = LocalDate.of(2000, 1, 1)
            val tom = LocalDate.of(2010, 12, 31)

            val result = TrygdetidGrunnlagFactory.trygdetidPeriode(fom, tom)

            with(result) {
                fomLd shouldBe fom
                tomLd shouldBe tom
                poengIInnAr shouldBe false
                poengIUtAr shouldBe false
                landEnum shouldBe LandkodeEnum.NOR
                ikkeProRata shouldBe false
                bruk shouldBe true
            }
        }

        test("håndterer null fom") {
            val tom = LocalDate.of(2010, 12, 31)

            val result = TrygdetidGrunnlagFactory.trygdetidPeriode(null as LocalDate?, tom)

            result.fomLd shouldBe null
            result.tomLd shouldBe tom
        }

        test("håndterer null tom") {
            val fom = LocalDate.of(2000, 1, 1)

            val result = TrygdetidGrunnlagFactory.trygdetidPeriode(fom, null as LocalDate?)

            result.fomLd shouldBe fom
            result.tomLd shouldBe null
        }

        test("håndterer både fom og tom som null") {
            val result = TrygdetidGrunnlagFactory.trygdetidPeriode(null as LocalDate?, null as LocalDate?)

            result.fomLd shouldBe null
            result.tomLd shouldBe null
            result.landEnum shouldBe LandkodeEnum.NOR
        }
    }

    context("trygdetidPeriode med Date fom og tom") {

        test("oppretter TTPeriode med standardverdier for Norge") {
            val fom = LocalDate.of(2000, 1, 1)
            val tom = LocalDate.of(2010, 12, 31)

            val result = TrygdetidGrunnlagFactory.trygdetidPeriode(fom, tom)

            with(result) {
                fomLd shouldNotBe null
                fomLd shouldBe fom
                tomLd shouldNotBe null
                tomLd shouldBe tom
                poengIInnAr shouldBe false
                poengIUtAr shouldBe false
                landEnum shouldBe LandkodeEnum.NOR
                ikkeProRata shouldBe false
                bruk shouldBe true
            }
        }

        test("håndterer null verdier") {
            val result = TrygdetidGrunnlagFactory.trygdetidPeriode(fom = null as LocalDate?, tom = null as LocalDate?)

            result.fomLd shouldBe null
            result.tomLd shouldBe null
        }
    }

    context("trygdetidPeriode med land, ikkeProRata og bruk (Date versjon)") {

        test("oppretter TTPeriode med angitte verdier") {
            val fom = LocalDate.of(2000, 1, 1)
            val tom = LocalDate.of(2010, 12, 31)

            val result = TrygdetidGrunnlagFactory.trygdetidPeriode(
                fom = fom,
                tom = tom,
                land = LandkodeEnum.SWE,
                ikkeProRata = true,
                bruk = false
            )

            result.fomLd shouldBe fom
            result.tomLd shouldBe tom
            result.poengIInnAr shouldBe false
            result.poengIUtAr shouldBe false
            result.landEnum shouldBe LandkodeEnum.SWE
            result.ikkeProRata shouldBe true
            result.bruk shouldBe false
        }

        test("håndterer null land") {
            val fom = LocalDate.of(2000, 1, 1)

            val result = TrygdetidGrunnlagFactory.trygdetidPeriode(
                fom = fom,
                tom = null,
                land = null,
                ikkeProRata = false,
                bruk = true
            )

            result.landEnum shouldBe null
        }

        test("håndterer null bruk") {
            val fom = LocalDate.of(2000, 1, 1)

            val result = TrygdetidGrunnlagFactory.trygdetidPeriode(
                fom = fom,
                tom = null,
                land = LandkodeEnum.NOR,
                ikkeProRata = false,
                bruk = null
            )

            result.bruk shouldBe null
        }

        test("håndterer null tom") {
            val fom = LocalDate.of(2000, 1, 1)

            val result = TrygdetidGrunnlagFactory.trygdetidPeriode(
                fom = fom,
                tom = null,
                land = LandkodeEnum.NOR,
                ikkeProRata = true,
                bruk = true
            )

            result.fomLd shouldBe fom
            result.tomLd shouldBe null
        }
    }

    context("trygdetidPeriode med land, ikkeProRata og bruk (LocalDate versjon)") {

        test("oppretter TTPeriode med angitte verdier via LocalDate") {
            val fom = LocalDate.of(2005, 6, 15)
            val tom = LocalDate.of(2015, 3, 20)

            val result = TrygdetidGrunnlagFactory.trygdetidPeriode(
                fom = fom,
                tom = tom,
                land = LandkodeEnum.DNK,
                ikkeProRata = true,
                bruk = true
            )

            result.fomLd shouldBe fom
            result.tomLd shouldBe tom
            result.landEnum shouldBe LandkodeEnum.DNK
            result.ikkeProRata shouldBe true
            result.bruk shouldBe true
        }

        test("håndterer null tom via LocalDate") {
            val fom = LocalDate.of(2005, 6, 15)

            val result = TrygdetidGrunnlagFactory.trygdetidPeriode(
                fom = fom,
                tom = null,
                land = LandkodeEnum.FIN,
                ikkeProRata = false,
                bruk = null
            )

            result.fomLd shouldBe fom
            result.tomLd shouldBe null
            result.landEnum shouldBe LandkodeEnum.FIN
        }
    }

    context("trygdetidPeriode med kun land (Date versjon)") {

        test("oppretter TTPeriode med angitt land og standardverdier") {
            val fom = LocalDate.of(2000, 1, 1)
            val tom = LocalDate.of(2010, 12, 31)

            val result = TrygdetidGrunnlagFactory.trygdetidPeriode(fom, tom, LandkodeEnum.SWE)

            result.fomLd shouldNotBe null
            result.fomLd shouldBe fom
            result.tomLd shouldNotBe null
            result.tomLd shouldBe tom
            result.poengIInnAr shouldBe false
            result.poengIUtAr shouldBe false
            result.landEnum shouldBe LandkodeEnum.SWE
            result.ikkeProRata shouldBe false
            result.bruk shouldBe true
        }

        test("kopierer Date-objekter med land") {
            val fom = LocalDate.of(2000, 1, 1)
            val tom = LocalDate.of(2010, 12, 31)

            val result = TrygdetidGrunnlagFactory.trygdetidPeriode(fom, tom, LandkodeEnum.NOR)

            // Skal være kopier, ikke samme objekt
            (result.fomLd === fom) shouldBe false
            (result.tomLd === tom) shouldBe false
        }

        test("håndterer null fom og tom med land") {
            val result = TrygdetidGrunnlagFactory.trygdetidPeriode(null as Date?, null as Date?, LandkodeEnum.GBR)

            result.fomLd shouldBe null
            result.tomLd shouldBe null
            result.landEnum shouldBe LandkodeEnum.GBR
            result.ikkeProRata shouldBe false
            result.bruk shouldBe true
        }

        test("håndterer null land") {
            val fom = LocalDate.of(2000, 1, 1)

            TrygdetidGrunnlagFactory.trygdetidPeriode(
                fom = fom,
                tom = null as LocalDate?,
                land = null as LandkodeEnum?
            ).landEnum shouldBe null
        }
    }

    context("trygdetidPeriode med kun land (LocalDate versjon)") {

        test("oppretter TTPeriode med angitt land via LocalDate") {
            val fom = LocalDate.of(2000, 1, 1)
            val tom = LocalDate.of(2010, 12, 31)

            val result = TrygdetidGrunnlagFactory.trygdetidPeriode(fom, tom, LandkodeEnum.DEU)

            result.fomLd shouldBe fom
            result.tomLd shouldBe tom
            result.landEnum shouldBe LandkodeEnum.DEU
            result.ikkeProRata shouldBe false
            result.bruk shouldBe true
        }

        test("håndterer null fom og tom med land via LocalDate") {
            val result =
                TrygdetidGrunnlagFactory.trygdetidPeriode(null as LocalDate?, null as LocalDate?, LandkodeEnum.FRA)

            result.fomLd shouldBe null
            result.tomLd shouldBe null
            result.landEnum shouldBe LandkodeEnum.FRA
        }
    }

    context("anonymSimuleringTrygdetidPeriode med Date") {

        test("oppretter TTPeriode med ikkeProRata=true for anonym simulering") {
            val fom = LocalDate.of(2000, 1, 1)
            val tom = LocalDate.of(2010, 12, 31)

            val result = TrygdetidGrunnlagFactory.anonymSimuleringTrygdetidPeriode(fom, tom)

            result.fomLd shouldNotBe null
            result.fomLd shouldBe fom
            result.tomLd shouldNotBe null
            result.tomLd shouldBe tom
            result.poengIInnAr shouldBe false
            result.poengIUtAr shouldBe false
            result.landEnum shouldBe LandkodeEnum.NOR
            result.ikkeProRata shouldBe true // Forskjell fra vanlig trygdetidPeriode
            result.bruk shouldBe true
        }

        test("kopierer Date-objekter for anonym simulering") {
            val fom = LocalDate.of(2000, 1, 1)
            val tom = LocalDate.of(2010, 12, 31)

            val result = TrygdetidGrunnlagFactory.anonymSimuleringTrygdetidPeriode(fom, tom)

            // Skal være kopier, ikke samme objekt
            (result.fomLd === fom) shouldBe false
            (result.tomLd === tom) shouldBe false
        }

        test("håndterer null verdier for anonym simulering") {
            val result = TrygdetidGrunnlagFactory.anonymSimuleringTrygdetidPeriode(null as Date?, null as Date?)

            result.fomLd shouldBe null
            result.tomLd shouldBe null
            result.landEnum shouldBe LandkodeEnum.NOR
            result.ikkeProRata shouldBe true
        }
    }

    context("anonymSimuleringTrygdetidPeriode med LocalDate") {

        test("oppretter TTPeriode med ikkeProRata=true via LocalDate") {
            val fom = LocalDate.of(2000, 1, 1)
            val tom = LocalDate.of(2010, 12, 31)

            val result = TrygdetidGrunnlagFactory.anonymSimuleringTrygdetidPeriode(fom, tom)

            result.fomLd shouldBe fom
            result.tomLd shouldBe tom
            result.ikkeProRata shouldBe true
            result.landEnum shouldBe LandkodeEnum.NOR
        }

        test("håndterer null verdier via LocalDate for anonym simulering") {
            val result =
                TrygdetidGrunnlagFactory.anonymSimuleringTrygdetidPeriode(null as LocalDate?, null as LocalDate?)

            result.fomLd shouldBe null
            result.tomLd shouldBe null
            result.ikkeProRata shouldBe true
        }
    }

    context("forskjell mellom trygdetidPeriode og anonymSimuleringTrygdetidPeriode") {

        test("trygdetidPeriode har ikkeProRata=false, anonymSimuleringTrygdetidPeriode har ikkeProRata=true") {
            val fom = LocalDate.of(2000, 1, 1)
            val tom = LocalDate.of(2010, 12, 31)

            val vanlig = TrygdetidGrunnlagFactory.trygdetidPeriode(fom, tom)
            val anonym = TrygdetidGrunnlagFactory.anonymSimuleringTrygdetidPeriode(fom, tom)

            vanlig.ikkeProRata shouldBe false
            anonym.ikkeProRata shouldBe true

            // Ellers er de like
            vanlig.landEnum shouldBe anonym.landEnum
            vanlig.bruk shouldBe anonym.bruk
            vanlig.poengIInnAr shouldBe anonym.poengIInnAr
            vanlig.poengIUtAr shouldBe anonym.poengIUtAr
        }
    }

    context("ulike landkoder") {

        test("støtter norske perioder") {
            val result = TrygdetidGrunnlagFactory.trygdetidPeriode(
                LocalDate.of(2000, 1, 1),
                LocalDate.of(2010, 1, 1),
                LandkodeEnum.NOR
            )
            result.landEnum shouldBe LandkodeEnum.NOR
        }

        test("støtter svenske perioder") {
            val result = TrygdetidGrunnlagFactory.trygdetidPeriode(
                LocalDate.of(2000, 1, 1),
                LocalDate.of(2010, 1, 1),
                LandkodeEnum.SWE
            )
            result.landEnum shouldBe LandkodeEnum.SWE
        }

        test("støtter danske perioder") {
            val result = TrygdetidGrunnlagFactory.trygdetidPeriode(
                LocalDate.of(2000, 1, 1),
                LocalDate.of(2010, 1, 1),
                LandkodeEnum.DNK
            )
            result.landEnum shouldBe LandkodeEnum.DNK
        }

        test("støtter finske perioder") {
            val result = TrygdetidGrunnlagFactory.trygdetidPeriode(
                LocalDate.of(2000, 1, 1),
                LocalDate.of(2010, 1, 1),
                LandkodeEnum.FIN
            )
            result.landEnum shouldBe LandkodeEnum.FIN
        }
    }
})

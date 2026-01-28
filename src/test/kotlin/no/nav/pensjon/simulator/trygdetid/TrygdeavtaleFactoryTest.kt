package no.nav.pensjon.simulator.trygdetid

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.core.domain.regler.enum.AvtaleDatoEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.AvtaleKritEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.AvtaletypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.LandkodeEnum
import no.nav.pensjon.simulator.core.util.toNorwegianDateAtNoon
import java.time.LocalDate

class TrygdeavtaleFactoryTest : ShouldSpec({

    context("newTrygdeavtaleForSimuleringUtland") {

        /**
         * Opprettet trygdeavtale for simulering utland skal ha:
         * - avtaledatoEnum = verdi for siste gyldige trygdeavtale med EØS
         * - avtaleKriterieEnum = YRK_TRYGD (yrkesaktiv, 1 års trygdetid)
         * - avtaleTypeEnum = EOS_NOR (EØS/Norge)
         * - bostedslandEnum = NOR (Norge)
         * - kravDatoIAvtaleland = angitt dato med klokkeslett 12
         * - omfattesavAvtalensPersonkrets = true
         */
        should("set 'kravdato i avtaleland' according to input") {
            val dato = LocalDate.of(2025, 1, 1)

            val trygdeavtale = TrygdeavtaleFactory.newTrygdeavtaleForSimuleringUtland(avtalelandKravdato = dato)

            with(trygdeavtale) {
                avtaledatoEnum shouldBe AvtaleDatoEnum.EOS1994
                avtaleKriterieEnum shouldBe AvtaleKritEnum.YRK_TRYGD
                avtaleTypeEnum shouldBe AvtaletypeEnum.EOS_NOR
                bostedslandEnum shouldBe LandkodeEnum.NOR
                kravDatoIAvtaleland shouldBe dato.toNorwegianDateAtNoon() // according to input
                omfattesavAvtalensPersonkrets shouldBe true
            }
        }
    }

    context("newTrygdeavtaledetaljerForSimuleringUtland") {

        /**
         * Opprettet trygdeavtaledetaljer for simulering utland skal ha:
         * - erArt10BruktGP = false (Art.10 ikke anvendes på grunnpensjon)
         * - erArt10BruktTP = false (Art.10 ikke anvendes på tilleggspensjon)
         * - fpa_nordisk = 0 (ingen faktiske poengår i annet nordisk land)
         */
        should("sette erArt10BruktGP til false") {
            val trygdeavtaledetaljer = TrygdeavtaleFactory.newTrygdeavtaledetaljerForSimuleringUtland()

            trygdeavtaledetaljer.erArt10BruktGP shouldBe false
        }

        should("sette erArt10BruktTP til false") {
            val trygdeavtaledetaljer = TrygdeavtaleFactory.newTrygdeavtaledetaljerForSimuleringUtland()

            trygdeavtaledetaljer.erArt10BruktTP shouldBe false
        }

        should("sette fpa_nordisk til 0") {
            val trygdeavtaledetaljer = TrygdeavtaleFactory.newTrygdeavtaledetaljerForSimuleringUtland()

            trygdeavtaledetaljer.fpa_nordisk shouldBe 0
        }

        should("la øvrige felt være uberørt (standardverdier)") {
            val trygdeavtaledetaljer = TrygdeavtaleFactory.newTrygdeavtaledetaljerForSimuleringUtland()

            with(trygdeavtaledetaljer) {
                arbeidsinntektMinst1G shouldBe null
                poengarListe.shouldBeEmpty()
                ftt_andreEOSLand shouldBe null
                ftt_garanti shouldBe null
                ftt_annetNordiskLand shouldBe null
                sumPensjonAndreAvtaleland shouldBe 0
                inntektsprovetPensjonAvtaleland shouldBe null
                barnepensjonForordning1408_71Enum shouldBe null
            }
        }

        should("opprette ny instans ved hvert kall") {
            val detaljer1 = TrygdeavtaleFactory.newTrygdeavtaledetaljerForSimuleringUtland()
            val detaljer2 = TrygdeavtaleFactory.newTrygdeavtaledetaljerForSimuleringUtland()

            // Verifiser at det er forskjellige instanser
            (detaljer1 === detaljer2) shouldBe false

            // Men med samme verdier
            detaljer1.erArt10BruktGP shouldBe detaljer2.erArt10BruktGP
            detaljer1.erArt10BruktTP shouldBe detaljer2.erArt10BruktTP
            detaljer1.fpa_nordisk shouldBe detaljer2.fpa_nordisk
        }
    }
})
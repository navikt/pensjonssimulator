package no.nav.pensjon.simulator.core

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import no.nav.pensjon.simulator.core.domain.regler.Merknad
import no.nav.pensjon.simulator.core.domain.regler.Opptjening
import no.nav.pensjon.simulator.core.domain.regler.Pakkseddel
import no.nav.pensjon.simulator.core.domain.regler.Trygdetid
import no.nav.pensjon.simulator.core.domain.regler.beregning.Poengtall
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.AfpPrivatBeregning
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.AfpPrivatLivsvarig
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.BeregningsResultatAfpPrivat
import no.nav.pensjon.simulator.core.domain.regler.enum.GrunnlagkildeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.OpptjeningtypeEnum
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Opptjeningsgrunnlag
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Pensjonsbeholdning
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.PersonOpptjeningsgrunnlag
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Uttaksgrad
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.domain.regler.to.RevurderingAlderspensjon2025Request
import no.nav.pensjon.simulator.core.domain.regler.to.TrygdetidResponse
import no.nav.pensjon.simulator.core.domain.regler.to.VilkarsprovAlderpensjon2011Request
import no.nav.pensjon.simulator.core.domain.regler.to.VilkarsprovAlderpensjon2016Request
import no.nav.pensjon.simulator.core.domain.regler.to.VilkarsprovAlderpensjon2025Request
import no.nav.pensjon.simulator.core.exception.KanIkkeBeregnesException
import no.nav.pensjon.simulator.core.exception.RegelmotorValideringException
import no.nav.pensjon.simulator.testutil.TestDateUtil.dateAtMidnight
import no.nav.pensjon.simulator.testutil.TestDateUtil.dateAtNoon
import tools.jackson.databind.ObjectMapper
import java.time.LocalDate
import java.util.*

class SimulatorContextUtilTest : FunSpec({

    test("finishOpptjeningInit resets poengtall values except 'veiet grunnbeløp' and 'uforeår'") {
        val pensjonsbeholdning = pensjonsbeholdning()

        SimulatorContextUtil.finishOpptjeningInit(ArrayList(listOf(pensjonsbeholdning)))

        with(pensjonsbeholdning.opptjening?.poengtall!!) {
            gv shouldBe 1 // 'veiet grunnbeløp' retained
            uforear shouldBe true // 'uforeår' retained
            ar shouldBe 0 // value has been reset
            pp shouldBe 0.0 // value has been reset
        }
    }

    test("validerOgFerdigstillResponse resets 'trygdetid 67-75'") {
        val response = TrygdetidResponse().apply {
            trygdetid = Trygdetid().apply {
                tt_67_70 = 1
                tt_67_75 = 2
                virkFom = dateAtNoon(2024, 5, 6)
                virkTom = dateAtNoon(2030, 1, 2)
            }
        }

        SimulatorContextUtil.validerOgFerdigstillResponse(
            result = response,
            kravGjelderUfoeretrygd = false,
            spec = "",
            objectMapper = ObjectMapper(),
            call = ""
        )

        with(response.trygdetid!!) {
            tt_67_70 shouldBe 1
            tt_67_75 shouldBe 0 // reset
            virkFom shouldBe dateAtNoon(2024, 5, 6)
            virkTom shouldBe dateAtNoon(2030, 1, 2)
        }
    }

    test("validerOgFerdigstillResponse undefines 'virkning-datoer' if uføretrygd") {
        val response = TrygdetidResponse().apply {
            trygdetid = Trygdetid().apply {
                tt = 1
                virkFom = dateAtNoon(2024, 5, 6)
                virkTom = dateAtNoon(2030, 1, 2)
            }
        }

        SimulatorContextUtil.validerOgFerdigstillResponse(
            result = response,
            kravGjelderUfoeretrygd = true,
            spec = "",
            objectMapper = ObjectMapper(),
            call = ""
        )

        with(response.trygdetid!!) {
            tt shouldBe 1
            virkFom shouldBe null // undefined
            virkTom shouldBe null // ditto
        }
    }

    test("tidsbegrensedeBeholdninger setter fom og tom datoer basert på år") {
        val beholdning1 = Pensjonsbeholdning().apply { ar = 2020 }
        val beholdning2 = Pensjonsbeholdning().apply { ar = 2021 }
        val beholdningListe = mutableListOf(beholdning1, beholdning2)

        val result = SimulatorContextUtil.tidsbegrensedeBeholdninger(beholdningListe)

        result shouldBe beholdningListe
        beholdning1.fom shouldBe dateAtNoon(2020, Calendar.JANUARY, 1)
        beholdning1.tom shouldBe dateAtNoon(2020, Calendar.DECEMBER, 31)
        beholdning2.fom shouldBe dateAtNoon(2021, Calendar.JANUARY, 1)
        beholdning2.tom shouldBe dateAtNoon(2021, Calendar.DECEMBER, 31)
    }

    test("preprocess VilkarsprovAlderpensjon2011Request setter datoer til noon") {
        val request = VilkarsprovAlderpensjon2011Request().apply {
            fom = dateAtMidnight(2024, Calendar.JUNE, 1)
            tom = dateAtMidnight(2025, Calendar.DECEMBER, 31)
            afpVirkFom = dateAtMidnight(2024, Calendar.JANUARY, 1)
        }

        SimulatorContextUtil.preprocess(request)

        request.fom shouldBe dateAtNoon(2024, Calendar.JUNE, 1)
        request.tom shouldBe dateAtNoon(2025, Calendar.DECEMBER, 31)
        request.afpVirkFom shouldBe dateAtNoon(2024, Calendar.JANUARY, 1)
    }

    test("preprocess VilkarsprovAlderpensjon2011Request runder nettoPerAr opp") {
        val request = VilkarsprovAlderpensjon2011Request().apply {
            afpPrivatLivsvarig = AfpPrivatLivsvarig().apply {
                nettoPerAr = 12345.4
            }
        }

        SimulatorContextUtil.preprocess(request)

        request.afpPrivatLivsvarig?.nettoPerAr shouldBe 12346.0
    }

    test("preprocess VilkarsprovAlderpensjon2016Request setter datoer til noon") {
        val request = VilkarsprovAlderpensjon2016Request().apply {
            virkFom = dateAtMidnight(2024, Calendar.JUNE, 1)
            afpVirkFom = dateAtMidnight(2024, Calendar.JANUARY, 1)
        }

        SimulatorContextUtil.preprocess(request)

        request.virkFom shouldBe dateAtNoon(2024, Calendar.JUNE, 1)
        request.afpVirkFom shouldBe dateAtNoon(2024, Calendar.JANUARY, 1)
    }

    test("preprocess VilkarsprovAlderpensjon2016Request runder nettoPerAr opp") {
        val request = VilkarsprovAlderpensjon2016Request().apply {
            afpPrivatLivsvarig = AfpPrivatLivsvarig().apply {
                nettoPerAr = 99999.1
            }
        }

        SimulatorContextUtil.preprocess(request)

        request.afpPrivatLivsvarig?.nettoPerAr shouldBe 100000.0
    }

    test("preprocess VilkarsprovAlderpensjon2025Request setter datoer til noon") {
        val request = VilkarsprovAlderpensjon2025Request().apply {
            fom = dateAtMidnight(2025, Calendar.MARCH, 15)
            afpVirkFom = dateAtMidnight(2025, Calendar.JANUARY, 1)
        }

        SimulatorContextUtil.preprocess(request)

        request.fom shouldBe dateAtNoon(2025, Calendar.MARCH, 15)
        request.afpVirkFom shouldBe dateAtNoon(2025, Calendar.JANUARY, 1)
    }

    test("preprocess VilkarsprovAlderpensjon2025Request runder nettoPerAr opp") {
        val request = VilkarsprovAlderpensjon2025Request().apply {
            afpPrivatLivsvarig = AfpPrivatLivsvarig().apply {
                nettoPerAr = 50000.01
            }
        }

        SimulatorContextUtil.preprocess(request)

        request.afpPrivatLivsvarig?.nettoPerAr shouldBe 50001.0
    }

    test("postprocess BeregningsResultatAfpPrivat setter virkTom til null") {
        val result = BeregningsResultatAfpPrivat().apply {
            virkTom = dateAtNoon(2030, Calendar.DECEMBER, 31)
        }

        SimulatorContextUtil.postprocess(result)

        result.virkTom shouldBe null
    }

    test("postprocess BeregningsResultatAfpPrivat runder nettoPerAr opp") {
        val result = BeregningsResultatAfpPrivat().apply {
            afpPrivatBeregning = AfpPrivatBeregning().apply {
                afpPrivatLivsvarig = AfpPrivatLivsvarig().apply {
                    nettoPerAr = 75000.5
                }
            }
        }

        SimulatorContextUtil.postprocess(result)

        result.afpPrivatBeregning?.afpPrivatLivsvarig?.nettoPerAr shouldBe 75001.0
    }

    test("validerResponse kaster ikke unntak når pakkseddel er OK") {
        val pakkseddel = Pakkseddel() // tom merknadListe = OK

        SimulatorContextUtil.validerResponse(pakkseddel)
        // Ingen unntak kastet
    }

    test("validerResponse kaster RegelmotorValideringException når kontrollTjeneste feiler") {
        val pakkseddel = Pakkseddel().apply {
            merknadListe = listOf(Merknad().apply {
                kode = "FEIL_KODE"
                argumentListe = listOf("arg1", "arg2")
            })
        }

        shouldThrow<RegelmotorValideringException> {
            SimulatorContextUtil.validerResponse(pakkseddel)
        }
    }

    test("validerResponse inkluderer alle merknader i exception-melding") {
        val pakkseddel = Pakkseddel().apply {
            merknadListe = listOf(
                Merknad().apply {
                    kode = "FEIL_1"
                    argumentListe = listOf("verdi1")
                },
                Merknad().apply {
                    kode = "FEIL_2"
                    argumentListe = listOf("verdi2", "verdi3")
                }
            )
        }

        val exception = shouldThrow<RegelmotorValideringException> {
            SimulatorContextUtil.validerResponse(pakkseddel)
        }

        exception.message shouldBe "FEIL_1:verdi1, FEIL_2:verdi2¤verdi3"
    }

    test("validerResponse inkluderer merknadListe i exception") {
        val merknad1 = Merknad().apply { kode = "KODE_1" }
        val merknad2 = Merknad().apply { kode = "KODE_2" }
        val pakkseddel = Pakkseddel().apply {
            merknadListe = listOf(merknad1, merknad2)
        }

        val exception = shouldThrow<RegelmotorValideringException> {
            SimulatorContextUtil.validerResponse(pakkseddel)
        }

        exception.merknadListe.size shouldBe 2
        exception.merknadListe[0].kode shouldBe "KODE_1"
        exception.merknadListe[1].kode shouldBe "KODE_2"
    }

    test("validerResponse med spec kaster ikke unntak når pakkseddel er OK") {
        val pakkseddel = Pakkseddel()

        SimulatorContextUtil.validerResponse(
            pakkseddel = pakkseddel,
            spec = "test-spec",
            objectMapper = ObjectMapper(),
            call = "testCall"
        )
        // Ingen unntak kastet
    }

    test("validerResponse med spec kaster RegelmotorValideringException ved feil") {
        val pakkseddel = Pakkseddel().apply {
            merknadListe = listOf(Merknad().apply { kode = "TEST_FEIL" })
        }

        shouldThrow<RegelmotorValideringException> {
            SimulatorContextUtil.validerResponse(
                pakkseddel = pakkseddel,
                spec = "test-spec",
                objectMapper = ObjectMapper(),
                call = "testCall"
            )
        }
    }

    test("validerResponse med RevurderingAlderspensjon2025Request håndterer feil") {
        val pakkseddel = Pakkseddel().apply {
            merknadListe = listOf(Merknad().apply { kode = "REVURDERING_FEIL" })
        }
        val request = RevurderingAlderspensjon2025Request().apply {
            virkFom = dateAtNoon(2025, Calendar.JANUARY, 1)
            kravhode = Kravhode().apply {
                uttaksgradListe = mutableListOf(
                    Uttaksgrad().apply {
                        uttaksgrad = 50
                        fomDato = dateAtNoon(2025, Calendar.JANUARY, 1)
                        tomDato = dateAtNoon(2025, Calendar.DECEMBER, 31)
                    }
                )
            }
        }

        val exception = shouldThrow<RegelmotorValideringException> {
            SimulatorContextUtil.validerResponse(
                pakkseddel = pakkseddel,
                spec = request,
                objectMapper = ObjectMapper(),
                call = "RevurderingAlderspensjon2025"
            )
        }

        exception.message shouldBe "REVURDERING_FEIL:"
    }

    test("validerResponse med tom argumentListe gir korrekt melding") {
        val pakkseddel = Pakkseddel().apply {
            merknadListe = listOf(Merknad().apply {
                kode = "FEIL_UTEN_ARGS"
                argumentListe = emptyList()
            })
        }

        val exception = shouldThrow<RegelmotorValideringException> {
            SimulatorContextUtil.validerResponse(pakkseddel)
        }

        exception.message shouldBe "FEIL_UTEN_ARGS:"
    }

    test("validerResponse med null kode i merknad håndteres") {
        val pakkseddel = Pakkseddel().apply {
            merknadListe = listOf(Merknad().apply {
                kode = null
                argumentListe = listOf("arg")
            })
        }

        val exception = shouldThrow<RegelmotorValideringException> {
            SimulatorContextUtil.validerResponse(pakkseddel)
        }

        exception.message shouldBe "null:arg"
    }

    test("personOpptjeningsgrunnlag oppretter grunnlag med opptjening og fødselsdato") {
        val opptjening = Opptjeningsgrunnlag().apply {
            ar = 2020
            pi = 500000
        }
        val foedselsdato = LocalDate.of(1960, 5, 15)

        val result = SimulatorContextUtil.personOpptjeningsgrunnlag(opptjening, foedselsdato)

        result.opptjening shouldBe opptjening
        result.fodselsdato shouldNotBe null
    }

    test("personOpptjeningsgrunnlag håndterer null fødselsdato") {
        val opptjening = Opptjeningsgrunnlag().apply {
            ar = 2020
        }

        val result = SimulatorContextUtil.personOpptjeningsgrunnlag(opptjening, null)

        result.opptjening shouldBe opptjening
        result.fodselsdato shouldBe null
    }

    test("updatePersonOpptjeningsFieldFromReglerResponse kopierer data fra output til input") {
        val inputOpptjening = Opptjeningsgrunnlag().apply {
            ar = 2020
            pi = 0
            pia = 0
            pp = 0.0
            bruk = false
        }
        val inputGrunnlag = PersonOpptjeningsgrunnlag().apply {
            fnr = "12345678901"
            opptjening = inputOpptjening
        }

        val outputOpptjening = Opptjeningsgrunnlag().apply {
            ar = 2020
            pi = 500000
            pia = 450000
            pp = 5.5
            bruk = true
            opptjeningTypeEnum = OpptjeningtypeEnum.PPI
            grunnlagKildeEnum = GrunnlagkildeEnum.POPP
        }
        val outputGrunnlag = PersonOpptjeningsgrunnlag().apply {
            fnr = "12345678901"
            opptjening = outputOpptjening
        }

        SimulatorContextUtil.updatePersonOpptjeningsFieldFromReglerResponse(
            reglerInput = listOf(inputGrunnlag),
            reglerOutput = listOf(outputGrunnlag)
        )

        with(inputOpptjening) {
            ar shouldBe 2020
            pi shouldBe 500000
            pia shouldBe 450000
            pp shouldBe 5.5
            bruk shouldBe true
            opptjeningTypeEnum shouldBe OpptjeningtypeEnum.PPI
            grunnlagKildeEnum shouldBe GrunnlagkildeEnum.POPP
        }
    }

    test("updatePersonOpptjeningsFieldFromReglerResponse håndterer flere grunnlag") {
        val inputOpptjening1 = Opptjeningsgrunnlag().apply { ar = 2020; pi = 0 }
        val inputOpptjening2 = Opptjeningsgrunnlag().apply { ar = 2021; pi = 0 }
        val inputGrunnlag1 = PersonOpptjeningsgrunnlag().apply {
            fnr = "12345678901"
            opptjening = inputOpptjening1
        }
        val inputGrunnlag2 = PersonOpptjeningsgrunnlag().apply {
            fnr = "12345678901"
            opptjening = inputOpptjening2
        }

        val outputOpptjening1 = Opptjeningsgrunnlag().apply { ar = 2020; pi = 400000 }
        val outputOpptjening2 = Opptjeningsgrunnlag().apply { ar = 2021; pi = 450000 }
        val outputGrunnlag1 = PersonOpptjeningsgrunnlag().apply {
            fnr = "12345678901"
            opptjening = outputOpptjening1
        }
        val outputGrunnlag2 = PersonOpptjeningsgrunnlag().apply {
            fnr = "12345678901"
            opptjening = outputOpptjening2
        }

        SimulatorContextUtil.updatePersonOpptjeningsFieldFromReglerResponse(
            reglerInput = listOf(inputGrunnlag1, inputGrunnlag2),
            reglerOutput = listOf(outputGrunnlag1, outputGrunnlag2)
        )

        inputOpptjening1.pi shouldBe 400000
        inputOpptjening2.pi shouldBe 450000
    }
})

private fun pensjonsbeholdning() =
    Pensjonsbeholdning().apply {
        opptjening = Opptjening().apply {
            poengtall = Poengtall().apply {
                gv = 1
                uforear = true
                ar = 3
                pp = 1.2
            }
        }
    }

package no.nav.pensjon.simulator.opptjening.client.popp.acl

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.core.domain.regler.enum.BeholdningtypeEnum
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Pensjonsbeholdning
import no.nav.pensjon.simulator.core.util.toNorwegianDateAtNoon
import java.time.LocalDate

class PoppBeholdningTest : ShouldSpec({

    context("toPensjonsbeholdning") {
        should("mappe til domenerepresentasjon av 'pensjonsbeholdning'") {
            val result: Pensjonsbeholdning =
                PoppBeholdning(
                    belop = 123.4,
                    fomDato = LocalDate.of(2026, 1, 1).toNorwegianDateAtNoon(),
                    tomDato = LocalDate.of(2026, 12, 31).toNorwegianDateAtNoon(),
                    beholdningGrunnlag = 1.2,
                    beholdningGrunnlagAvkortet = 0.9,
                    beholdningInnskudd = 2.3,
                    lonnsvekstregulering = PoppLoennsvekstregulering(
                        reguleringsbelop = 1.2,
                        reguleringsDato = LocalDate.of(2026, 5, 1).toNorwegianDateAtNoon()
                    ),
                    inntektOpptjeningBelop = PoppInntektOpptjeningBelop(belop = 16.7),
                    omsorgOpptjeningBelop = PoppOmsorgsopptjeningBeloep(belop = 17.8),
                    dagpengerOpptjeningBelop = PoppDagpengerOpptjeningBeloep(
                        belopOrdinar = 20.1,
                        belopFiskere = 21.2
                    ),
                    forstegangstjenesteOpptjeningBelop = PoppFoerstegangstjenesteOpptjeningBeloep(belop = 15.6),
                    uforeOpptjeningBelop = PoppUfoereOpptjeningBeloep(
                        belop = 18.9,
                        proRataBeregnetUp = true,
                        poengtall = 4.1,
                        uforegrad = 5,
                        antattInntekt = 6.2,
                        antattInntektProRata = 7.3,
                        andelProrata = 8.4,
                        poengarTellerProRata = 6,
                        poengarNevnerProRata = 7,
                        antFremtidigArProRata = 8,
                        poengAntattArligInntekt = 9.5,
                        yrkesskadegrad = 9,
                        antattInntektYrke = 10.6
                    ),
                ).toPensjonsbeholdning()

            with(result) {
                fomLd shouldBe LocalDate.of(2026, 1, 1)
                tomLd shouldBe LocalDate.of(2026, 12, 31)
                ar shouldBe 2026
                totalbelop shouldBe 123.4
                with(opptjening!!) {
                    ar shouldBe 2024
                    opptjeningsgrunnlag shouldBe 1.2
                    anvendtOpptjeningsgrunnlag shouldBe 0.9
                    arligOpptjening shouldBe 2.3
                    lonnsvekstInformasjon shouldBe null // ikke mappet
                    pSatsOpptjening shouldBe 0.0 // ditto
                    with(poengtall!!) {
                        // Ingen av feltene i poengtall mappes:
                        pp shouldBe 0.0
                        pia shouldBe 0
                        pi shouldBe 0
                        ar shouldBe 0
                        bruktIBeregning shouldBe false
                        gv shouldBe 0
                        poengtallTypeEnum shouldBe null
                        maksUforegrad shouldBe 0
                        uforear shouldBe false
                        merknadListe shouldBe mutableListOf()
                        verdi shouldBe 0.0
                        justertBelop shouldBe 0.0
                        omsorg shouldBe false
                        inntektIAvtaleland shouldBe false
                        opptjeningsar shouldBe 0
                    }
                    inntektUtenDagpenger shouldBe 16.7
                    dagpenger shouldBe 20.1
                    dagpengerFiskerOgFangstmenn shouldBe 21.2
                    omsorg shouldBe 17.8
                    forstegangstjeneste shouldBe 15.6
                    arligOpptjeningOmsorg shouldBe 0.0 // ikke mappet
                    arligOpptjeningUtenOmsorg shouldBe 0.0 // ditto
                    with(uforeOpptjening!!) {
                        belop shouldBe 18.9
                        proRataBeregnetUP shouldBe true
                        poengtall shouldBe 4.1
                        ufg shouldBe 5
                        antattInntekt shouldBe 6.2
                        antattInntekt_proRata shouldBe 7.3
                        andel_proRata shouldBe 8.4
                        poengarTeller_proRata shouldBe 6
                        poengarNevner_proRata shouldBe 7
                        antFremtidigeAr_proRata shouldBe 8
                        with(yrkesskadeopptjening!!) {
                            paa shouldBe 9.5
                            yug shouldBe 9
                            antattInntektYrke shouldBe 10.6
                        }
                    }
                }
                with(lonnsvekstInformasjon!!) {
                    lonnsvekst shouldBe 0.0 // ikke mappet
                    reguleringsDatoLd shouldBe LocalDate.of(2026, 5, 1)
                    uttaksgradVedRegulering shouldBe 0 // ikke mappet
                }
                with(reguleringsInformasjon!!) {
                    lonnsvekst shouldBe 0.0 // ikke mappet
                    fratrekksfaktor shouldBe 0.0 // ditto
                    gammelG shouldBe 0 // ditto
                    nyG shouldBe 0 // ditto
                    reguleringsfaktor shouldBe 0.0 // ditto
                    gjennomsnittligUttaksgradSisteAr shouldBe 0.0 // ditto
                    reguleringsbelop shouldBe 1.2
                    prisOgLonnsvekst shouldBe 0.0 // ikke mappet
                }
                formelKodeEnum shouldBe null // ikke mappet
                beholdningsTypeEnum shouldBe BeholdningtypeEnum.PEN_B // hardkodet
                merknadListe shouldBe mutableListOf() // ikke mappet
            }
        }
    }
})
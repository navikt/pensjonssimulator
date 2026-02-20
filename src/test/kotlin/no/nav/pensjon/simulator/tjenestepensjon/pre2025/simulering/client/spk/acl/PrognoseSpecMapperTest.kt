package no.nav.pensjon.simulator.tjenestepensjon.pre2025.simulering.client.spk.acl

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.simulering.Inntekt
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.simulering.Opptjeningsperiode
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.simulering.Pensjonsbeholdningsperiode
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.simulering.Simuleringsdata
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.simulering.Simuleringsperiode
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.simulering.SimulertOffentligAfp
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.simulering.SimulertPrivatAfp
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.simulering.SivilstandKode
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.simulering.TjenestepensjonSimuleringPre2025Spec
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.simulering.TpForhold
import java.time.LocalDate

class PrognoseSpecMapperTest : FunSpec({

    val pid = Pid("12345678910")
    val foedselsdato = LocalDate.of(1963, 1, 15)

    fun spec(
        simulertOffentligAfp: SimulertOffentligAfp? = null,
        simulertPrivatAfp: SimulertPrivatAfp? = null,
        sivilstand: SivilstandKode = SivilstandKode.UGIFT,
        inntekter: List<Inntekt> = emptyList(),
        pensjonsbeholdningsperioder: List<Pensjonsbeholdningsperiode> = emptyList(),
        simuleringsperioder: List<Simuleringsperiode> = emptyList(),
        simuleringsdata: List<Simuleringsdata> = emptyList(),
        tpForhold: List<TpForhold> = emptyList()
    ) = TjenestepensjonSimuleringPre2025Spec(
        pid = pid,
        foedselsdato = foedselsdato,
        sisteTpOrdningsTpNummer = "3010",
        simulertOffentligAfp = simulertOffentligAfp,
        simulertPrivatAfp = simulertPrivatAfp,
        sivilstand = sivilstand,
        inntekter = inntekter,
        pensjonsbeholdningsperioder = pensjonsbeholdningsperioder,
        simuleringsperioder = simuleringsperioder,
        simuleringsdata = simuleringsdata,
        tpForhold = tpForhold
    )

    // --- top-level fields ---

    test("toDto maps fnr from pid") {
        PrognoseSpecMapper.toDto(spec()).fnr shouldBe "12345678910"
    }

    test("toDto maps fodselsdato") {
        PrognoseSpecMapper.toDto(spec()).fodselsdato shouldBe foedselsdato
    }

    test("toDto maps sisteTpnr") {
        PrognoseSpecMapper.toDto(spec()).sisteTpnr shouldBe "3010"
    }

    test("toDto sets sprak to norsk") {
        PrognoseSpecMapper.toDto(spec()).sprak shouldBe "norsk"
    }

    // --- sivilstandkode ---

    test("toDto maps sivilstand UGIFT to UGIF") {
        PrognoseSpecMapper.toDto(spec(sivilstand = SivilstandKode.UGIFT)).sivilstandkode shouldBe SivilstandCodeEnumDto.UGIF
    }

    test("toDto maps sivilstand GIFT to GIFT") {
        PrognoseSpecMapper.toDto(spec(sivilstand = SivilstandKode.GIFT)).sivilstandkode shouldBe SivilstandCodeEnumDto.GIFT
    }

    test("toDto maps sivilstand ENKE to ENKE") {
        PrognoseSpecMapper.toDto(spec(sivilstand = SivilstandKode.ENKE)).sivilstandkode shouldBe SivilstandCodeEnumDto.ENKE
    }

    // --- simulertAFPOffentlig ---

    test("toDto maps null simulertOffentligAfp to null") {
        PrognoseSpecMapper.toDto(spec(simulertOffentligAfp = null)).simulertAFPOffentlig shouldBe null
    }

    test("toDto maps simulertOffentligAfp brutto and tpi") {
        val afp = SimulertOffentligAfp(brutto = 50000, tidligerePensjonsgivendeInntekt = 400000)

        val dto = PrognoseSpecMapper.toDto(spec(simulertOffentligAfp = afp))

        with(dto.simulertAFPOffentlig!!) {
            simulertAFPOffentligBrutto shouldBe 50000
            tpi shouldBe 400000
        }
    }

    // --- simulertAFPPrivat ---

    test("toDto maps null simulertPrivatAfp to null") {
        PrognoseSpecMapper.toDto(spec(simulertPrivatAfp = null)).simulertAFPPrivat shouldBe null
    }

    test("toDto maps simulertPrivatAfp fields") {
        val afp = SimulertPrivatAfp(totalAfpBeholdning = 100000, kompensasjonstillegg = 1234.5)

        val dto = PrognoseSpecMapper.toDto(spec(simulertPrivatAfp = afp))

        with(dto.simulertAFPPrivat!!) {
            afpOpptjeningTotalbelop shouldBe 100000
            kompensasjonstillegg shouldBe 1234.5
        }
    }

    // --- inntektListe ---

    test("toDto maps inntekter to inntektListe") {
        val inntekter = listOf(
            Inntekt(fom = LocalDate.of(2025, 1, 1), beloep = 600000.0),
            Inntekt(fom = LocalDate.of(2026, 1, 1), beloep = 650000.0)
        )

        val dto = PrognoseSpecMapper.toDto(spec(inntekter = inntekter))

        with(dto) {
            inntektListe shouldHaveSize 2
            with(inntektListe[0]) {
                datoFom shouldBe LocalDate.of(2025, 1, 1)
                inntekt shouldBe 600000.0
            }
            with(inntektListe[1]) {
                datoFom shouldBe LocalDate.of(2026, 1, 1)
                inntekt shouldBe 650000.0
            }
        }
    }

    test("toDto maps empty inntekter to empty list") {
        PrognoseSpecMapper.toDto(spec(inntekter = emptyList())).inntektListe.shouldBeEmpty()
    }

    // --- pensjonsbeholdningsperiodeListe ---

    test("toDto maps pensjonsbeholdningsperioder") {
        val perioder = listOf(
            Pensjonsbeholdningsperiode(
                fom = LocalDate.of(2030, 1, 1),
                pensjonsbeholdning = 1500000.7,
                garantipensjonsbeholdning = 200000.9,
                garantitilleggsbeholdning = 50000.3
            )
        )

        val dto = PrognoseSpecMapper.toDto(spec(pensjonsbeholdningsperioder = perioder))

        with(dto) {
            pensjonsbeholdningsperiodeListe shouldHaveSize 1
            with(pensjonsbeholdningsperiodeListe[0]) {
                datoFom shouldBe LocalDate.of(2030, 1, 1)
                pensjonsbeholdning shouldBe 1500000
                garantipensjonsbeholdning shouldBe 200000
                garantitilleggsbeholdning shouldBe 50000
            }
        }
    }

    test("toDto maps null pensjonsbeholdning values to 0") {
        val perioder = listOf(
            Pensjonsbeholdningsperiode(
                fom = LocalDate.of(2030, 1, 1),
                pensjonsbeholdning = null,
                garantipensjonsbeholdning = null,
                garantitilleggsbeholdning = null
            )
        )

        val dto = PrognoseSpecMapper.toDto(spec(pensjonsbeholdningsperioder = perioder))

        with(dto) {
            pensjonsbeholdningsperiodeListe[0].pensjonsbeholdning shouldBe 0
            pensjonsbeholdningsperiodeListe[0].garantipensjonsbeholdning shouldBe 0
            pensjonsbeholdningsperiodeListe[0].garantitilleggsbeholdning shouldBe 0
        }
    }

    // --- simuleringsperiodeListe ---

    test("toDto maps simuleringsperioder") {
        val perioder = listOf(
            Simuleringsperiode(
                fom = LocalDate.of(2030, 2, 1),
                folketrygdUttaksgrad = 50,
                stillingsprosentOffentlig = 80,
                simulerAFPOffentligEtterfulgtAvAlder = true
            )
        )

        val dto = PrognoseSpecMapper.toDto(spec(simuleringsperioder = perioder))

        with(dto) {
            simuleringsperiodeListe shouldHaveSize 1
            simuleringsperiodeListe[0].datoFom shouldBe LocalDate.of(2030, 2, 1)
            simuleringsperiodeListe[0].folketrygdUttaksgrad shouldBe 50
            simuleringsperiodeListe[0].stillingsprosentOffentlig shouldBe 80
            simuleringsperiodeListe[0].simulerAFPOffentligEtterfulgtAvAlder shouldBe true
        }
    }

    // --- simuleringsdataListe ---

    test("toDto maps simuleringsdata") {
        val data = listOf(
            Simuleringsdata(
                fom = LocalDate.of(2030, 3, 1),
                andvendtTrygdetid = 40,
                poengAarTom1991 = 10,
                poengAarFom1992 = 20,
                ufoeregradVedOmregning = 0,
                basisGrunnpensjon = 100000.0,
                basisPensjonstillegg = 50000.0,
                basisTilleggspensjon = 60000.0,
                delingstallUttak = 15.5,
                forholdstallUttak = 1.05,
                sluttpoengtall = 5.5
            )
        )

        val dto = PrognoseSpecMapper.toDto(spec(simuleringsdata = data))

        with(dto) {
            simuleringsdataListe shouldHaveSize 1
            with(simuleringsdataListe[0]) {
                datoFom shouldBe LocalDate.of(2030, 3, 1)
                andvendtTrygdetid shouldBe 40
                poengArTom1991 shouldBe 10
                poengArFom1992 shouldBe 20
                uforegradVedOmregning shouldBe 0
                basisgp shouldBe 100000.0
                basispt shouldBe 50000.0
                basistp shouldBe 60000.0
                delingstallUttak shouldBe 15.5
                forholdstallUttak shouldBe 1.05
                sluttpoengtall shouldBe 5.5
            }
        }
    }

    test("toDto maps simuleringsdata with null optional fields") {
        val data = listOf(
            Simuleringsdata(
                fom = LocalDate.of(2030, 3, 1),
                andvendtTrygdetid = 40,
                poengAarTom1991 = null,
                poengAarFom1992 = null,
                ufoeregradVedOmregning = 0,
                basisGrunnpensjon = null,
                basisPensjonstillegg = null,
                basisTilleggspensjon = null,
                delingstallUttak = null,
                forholdstallUttak = 1.0,
                sluttpoengtall = null
            )
        )

        val dto = PrognoseSpecMapper.toDto(spec(simuleringsdata = data))

        with(dto) {
            simuleringsdataListe shouldHaveSize 1
            with(simuleringsdataListe[0]) {
                poengArTom1991 shouldBe null
                poengArFom1992 shouldBe null
                basisgp shouldBe null
                basispt shouldBe null
                basistp shouldBe null
                delingstallUttak shouldBe null
                sluttpoengtall shouldBe null
            }
        }
    }
    // --- tpForholdListe ---

    test("toDto maps tpForhold with opptjeningsperioder") {
        val forhold = listOf(
            TpForhold(
                tpNr = "3010",
                opptjeningsperioder = listOf(
                    Opptjeningsperiode(
                        fom = LocalDate.of(2010, 1, 1),
                        tom = LocalDate.of(2020, 12, 31),
                        stillingsprosent = 100,
                        aldersgrense = 67,
                        faktiskHovedloenn = 500000,
                        stillingsuavhengigTilleggsloenn = 10000
                    )
                )
            )
        )

        val dto = PrognoseSpecMapper.toDto(spec(tpForhold = forhold))

        with(dto) {
            tpForholdListe shouldHaveSize 1
            tpForholdListe[0].tpnr shouldBe "3010"
            tpForholdListe[0].opptjeningsperiodeListe shouldHaveSize 1
            with(tpForholdListe[0].opptjeningsperiodeListe[0]) {
                datoFom shouldBe LocalDate.of(2010, 1, 1)
                datoTom shouldBe LocalDate.of(2020, 12, 31)
                stillingsprosent shouldBe 100.0
                aldersgrense shouldBe 67
                faktiskHovedlonn shouldBe 500000
                stillingsuavhengigTilleggslonn shouldBe 10000
            }
        }
    }

    test("toDto maps opptjeningsperiode with null tom and null optional fields") {
        val forhold = listOf(
            TpForhold(
                tpNr = "3010",
                opptjeningsperioder = listOf(
                    Opptjeningsperiode(
                        fom = LocalDate.of(2010, 1, 1),
                        tom = null,
                        stillingsprosent = 80,
                        aldersgrense = null,
                        faktiskHovedloenn = null,
                        stillingsuavhengigTilleggsloenn = null
                    )
                )
            )
        )

        val dto = PrognoseSpecMapper.toDto(spec(tpForhold = forhold))

        with(dto) {
            tpForholdListe shouldHaveSize 1
            tpForholdListe[0].opptjeningsperiodeListe shouldHaveSize 1
            with(tpForholdListe[0].opptjeningsperiodeListe[0]) {
                datoTom shouldBe null
                stillingsprosent shouldBe 80.0
                aldersgrense shouldBe null
                faktiskHovedlonn shouldBe null
                stillingsuavhengigTilleggslonn shouldBe null
            }
        }
    }
})

package no.nav.pensjon.simulator.tjenestepensjon.pre2025.simulering.acl

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.YtelseCode
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.api.*
import java.time.LocalDate

class HentPrognoseMapperTest : FunSpec({

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

    // --- toDto: top-level fields ---

    test("toDto maps fnr from pid") {
        val dto = HentPrognoseMapper.toDto(spec())
        dto.fnr shouldBe "12345678910"
    }

    test("toDto maps fodselsdato") {
        val dto = HentPrognoseMapper.toDto(spec())
        dto.fodselsdato shouldBe foedselsdato
    }

    test("toDto maps sisteTpnr") {
        val dto = HentPrognoseMapper.toDto(spec())
        dto.sisteTpnr shouldBe "3010"
    }

    test("toDto sets sprak to norsk") {
        val dto = HentPrognoseMapper.toDto(spec())
        dto.sprak shouldBe "norsk"
    }

    // --- toDto: sivilstandkode ---

    test("toDto maps sivilstand UGIFT to UGIF") {
        val dto = HentPrognoseMapper.toDto(spec(sivilstand = SivilstandKode.UGIFT))
        dto.sivilstandkode shouldBe SivilstandCodeEnumDto.UGIF
    }

    test("toDto maps sivilstand GIFT to GIFT") {
        val dto = HentPrognoseMapper.toDto(spec(sivilstand = SivilstandKode.GIFT))
        dto.sivilstandkode shouldBe SivilstandCodeEnumDto.GIFT
    }

    test("toDto maps sivilstand ENKE to ENKE") {
        val dto = HentPrognoseMapper.toDto(spec(sivilstand = SivilstandKode.ENKE))
        dto.sivilstandkode shouldBe SivilstandCodeEnumDto.ENKE
    }

    // --- toDto: simulertAFPOffentlig ---

    test("toDto maps null simulertOffentligAfp to null") {
        val dto = HentPrognoseMapper.toDto(spec(simulertOffentligAfp = null))
        dto.simulertAFPOffentlig shouldBe null
    }

    test("toDto maps simulertOffentligAfp brutto and tpi") {
        val afp = SimulertOffentligAfp(brutto = 50000, tidligerePensjonsgivendeInntekt = 400000)
        val dto = HentPrognoseMapper.toDto(spec(simulertOffentligAfp = afp))
        dto.simulertAFPOffentlig!!.simulertAFPOffentligBrutto shouldBe 50000
        dto.simulertAFPOffentlig!!.tpi shouldBe 400000
    }

    // --- toDto: simulertAFPPrivat ---

    test("toDto maps null simulertPrivatAfp to null") {
        val dto = HentPrognoseMapper.toDto(spec(simulertPrivatAfp = null))
        dto.simulertAFPPrivat shouldBe null
    }

    test("toDto maps simulertPrivatAfp fields") {
        val afp = SimulertPrivatAfp(totalAfpBeholdning = 100000, kompensasjonstillegg = 1234.5)
        val dto = HentPrognoseMapper.toDto(spec(simulertPrivatAfp = afp))
        dto.simulertAFPPrivat!!.afpOpptjeningTotalbelop shouldBe 100000
        dto.simulertAFPPrivat!!.kompensasjonstillegg shouldBe 1234.5
    }

    // --- toDto: inntektListe ---

    test("toDto maps inntekter to inntektListe") {
        val inntekter = listOf(
            Inntekt(fom = LocalDate.of(2025, 1, 1), beloep = 600000.0),
            Inntekt(fom = LocalDate.of(2026, 1, 1), beloep = 650000.0)
        )
        val dto = HentPrognoseMapper.toDto(spec(inntekter = inntekter))
        dto.inntektListe shouldHaveSize 2
        dto.inntektListe[0].datoFom shouldBe LocalDate.of(2025, 1, 1)
        dto.inntektListe[0].inntekt shouldBe 600000.0
        dto.inntektListe[1].datoFom shouldBe LocalDate.of(2026, 1, 1)
        dto.inntektListe[1].inntekt shouldBe 650000.0
    }

    test("toDto maps empty inntekter to empty list") {
        val dto = HentPrognoseMapper.toDto(spec(inntekter = emptyList()))
        dto.inntektListe.shouldBeEmpty()
    }

    // --- toDto: pensjonsbeholdningsperiodeListe ---

    test("toDto maps pensjonsbeholdningsperioder") {
        val perioder = listOf(
            Pensjonsbeholdningsperiode(
                fom = LocalDate.of(2030, 1, 1),
                pensjonsbeholdning = 1500000.7,
                garantipensjonsbeholdning = 200000.9,
                garantitilleggsbeholdning = 50000.3
            )
        )
        val dto = HentPrognoseMapper.toDto(spec(pensjonsbeholdningsperioder = perioder))
        dto.pensjonsbeholdningsperiodeListe shouldHaveSize 1
        dto.pensjonsbeholdningsperiodeListe[0].datoFom shouldBe LocalDate.of(2030, 1, 1)
        dto.pensjonsbeholdningsperiodeListe[0].pensjonsbeholdning shouldBe 1500000
        dto.pensjonsbeholdningsperiodeListe[0].garantipensjonsbeholdning shouldBe 200000
        dto.pensjonsbeholdningsperiodeListe[0].garantitilleggsbeholdning shouldBe 50000
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
        val dto = HentPrognoseMapper.toDto(spec(pensjonsbeholdningsperioder = perioder))
        dto.pensjonsbeholdningsperiodeListe[0].pensjonsbeholdning shouldBe 0
        dto.pensjonsbeholdningsperiodeListe[0].garantipensjonsbeholdning shouldBe 0
        dto.pensjonsbeholdningsperiodeListe[0].garantitilleggsbeholdning shouldBe 0
    }

    // --- toDto: simuleringsperiodeListe ---

    test("toDto maps simuleringsperioder") {
        val perioder = listOf(
            Simuleringsperiode(
                fom = LocalDate.of(2030, 2, 1),
                folketrygdUttaksgrad = 50,
                stillingsprosentOffentlig = 80,
                simulerAFPOffentligEtterfulgtAvAlder = true
            )
        )
        val dto = HentPrognoseMapper.toDto(spec(simuleringsperioder = perioder))
        dto.simuleringsperiodeListe shouldHaveSize 1
        dto.simuleringsperiodeListe[0].datoFom shouldBe LocalDate.of(2030, 2, 1)
        dto.simuleringsperiodeListe[0].folketrygdUttaksgrad shouldBe 50
        dto.simuleringsperiodeListe[0].stillingsprosentOffentlig shouldBe 80
        dto.simuleringsperiodeListe[0].simulerAFPOffentligEtterfulgtAvAlder shouldBe true
    }

    // --- toDto: simuleringsdataListe ---

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
        val dto = HentPrognoseMapper.toDto(spec(simuleringsdata = data))
        dto.simuleringsdataListe shouldHaveSize 1
        val d = dto.simuleringsdataListe[0]
        d.datoFom shouldBe LocalDate.of(2030, 3, 1)
        d.andvendtTrygdetid shouldBe 40
        d.poengArTom1991 shouldBe 10
        d.poengArFom1992 shouldBe 20
        d.uforegradVedOmregning shouldBe 0
        d.basisgp shouldBe 100000.0
        d.basispt shouldBe 50000.0
        d.basistp shouldBe 60000.0
        d.delingstallUttak shouldBe 15.5
        d.forholdstallUttak shouldBe 1.05
        d.sluttpoengtall shouldBe 5.5
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
        val dto = HentPrognoseMapper.toDto(spec(simuleringsdata = data))
        val d = dto.simuleringsdataListe[0]
        d.poengArTom1991 shouldBe null
        d.poengArFom1992 shouldBe null
        d.basisgp shouldBe null
        d.basispt shouldBe null
        d.basistp shouldBe null
        d.delingstallUttak shouldBe null
        d.sluttpoengtall shouldBe null
    }

    // --- toDto: tpForholdListe ---

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
        val dto = HentPrognoseMapper.toDto(spec(tpForhold = forhold))
        dto.tpForholdListe shouldHaveSize 1
        dto.tpForholdListe[0].tpnr shouldBe "3010"
        val op = dto.tpForholdListe[0].opptjeningsperiodeListe[0]
        op.datoFom shouldBe LocalDate.of(2010, 1, 1)
        op.datoTom shouldBe LocalDate.of(2020, 12, 31)
        op.stillingsprosent shouldBe 100.0
        op.aldersgrense shouldBe 67
        op.faktiskHovedlonn shouldBe 500000
        op.stillingsuavhengigTilleggslonn shouldBe 10000
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
        val dto = HentPrognoseMapper.toDto(spec(tpForhold = forhold))
        val op = dto.tpForholdListe[0].opptjeningsperiodeListe[0]
        op.datoTom shouldBe null
        op.stillingsprosent shouldBe 80.0
        op.aldersgrense shouldBe null
        op.faktiskHovedlonn shouldBe null
        op.stillingsuavhengigTilleggslonn shouldBe null
    }

    // --- fromDto: happy path ---

    test("fromDto maps tpnr and navnOrdning") {
        val response = HentPrognoseResponseDto(
            tpnr = "3010",
            navnOrdning = "SPK",
            inkluderteOrdningerListe = emptyList(),
            utbetalingsperiodeListe = emptyList()
        )
        val result = HentPrognoseMapper.fromDto(response)
        result.tpnr shouldBe "3010"
        result.navnOrdning shouldBe "SPK"
    }

    test("fromDto maps inkluderteOrdningerListe") {
        val response = HentPrognoseResponseDto(
            tpnr = "3010",
            navnOrdning = "SPK",
            inkluderteOrdningerListe = listOf("3010", "3020")
        )
        val result = HentPrognoseMapper.fromDto(response)
        result.inkluderteOrdningerListe shouldBe listOf("3010", "3020")
    }

    test("fromDto maps leverandorUrl") {
        val response = HentPrognoseResponseDto(
            tpnr = "3010",
            navnOrdning = "SPK",
            leverandorUrl = "https://spk.no"
        )
        val result = HentPrognoseMapper.fromDto(response)
        result.leverandorUrl shouldBe "https://spk.no"
    }

    test("fromDto maps utbetalingsperiode fields") {
        val response = HentPrognoseResponseDto(
            tpnr = "3010",
            navnOrdning = "SPK",
            utbetalingsperiodeListe = listOf(
                Utbetalingsperiode(
                    uttaksgrad = 100,
                    arligUtbetaling = 250000.0,
                    datoFom = LocalDate.of(2030, 1, 1),
                    datoTom = LocalDate.of(2035, 12, 31),
                    ytelsekode = "AP"
                )
            )
        )
        val result = HentPrognoseMapper.fromDto(response)
        result.utbetalingsperiodeListe shouldHaveSize 1
        val p = result.utbetalingsperiodeListe[0]
        p.uttaksgrad shouldBe 100
        p.arligUtbetaling shouldBe 250000.0
        p.datoFom shouldBe LocalDate.of(2030, 1, 1)
        p.datoTom shouldBe LocalDate.of(2035, 12, 31)
        p.ytelsekode shouldBe YtelseCode.AP
    }

    test("fromDto maps ytelsekode AFP") {
        val response = HentPrognoseResponseDto(
            tpnr = "3010",
            navnOrdning = "SPK",
            utbetalingsperiodeListe = listOf(
                Utbetalingsperiode(
                    uttaksgrad = 50, arligUtbetaling = 100000.0,
                    datoFom = LocalDate.of(2030, 1, 1), datoTom = null, ytelsekode = "AFP"
                )
            )
        )
        val result = HentPrognoseMapper.fromDto(response)
        result.utbetalingsperiodeListe[0].ytelsekode shouldBe YtelseCode.AFP
    }

    test("fromDto maps ytelsekode SERALDER") {
        val response = HentPrognoseResponseDto(
            tpnr = "3010",
            navnOrdning = "SPK",
            utbetalingsperiodeListe = listOf(
                Utbetalingsperiode(
                    uttaksgrad = 100, arligUtbetaling = 300000.0,
                    datoFom = LocalDate.of(2030, 1, 1), datoTom = null, ytelsekode = "SERALDER"
                )
            )
        )
        val result = HentPrognoseMapper.fromDto(response)
        result.utbetalingsperiodeListe[0].ytelsekode shouldBe YtelseCode.SERALDER
    }

    test("fromDto maps null datoTom") {
        val response = HentPrognoseResponseDto(
            tpnr = "3010",
            navnOrdning = "SPK",
            utbetalingsperiodeListe = listOf(
                Utbetalingsperiode(
                    uttaksgrad = 100, arligUtbetaling = 200000.0,
                    datoFom = LocalDate.of(2030, 1, 1), datoTom = null, ytelsekode = "AP"
                )
            )
        )
        val result = HentPrognoseMapper.fromDto(response)
        result.utbetalingsperiodeListe[0].datoTom shouldBe null
    }

    test("fromDto filters out null entries in utbetalingsperiodeListe") {
        val response = HentPrognoseResponseDto(
            tpnr = "3010",
            navnOrdning = "SPK",
            utbetalingsperiodeListe = listOf(
                null,
                Utbetalingsperiode(
                    uttaksgrad = 100, arligUtbetaling = 200000.0,
                    datoFom = LocalDate.of(2030, 1, 1), datoTom = null, ytelsekode = "AP"
                ),
                null
            )
        )
        val result = HentPrognoseMapper.fromDto(response)
        result.utbetalingsperiodeListe shouldHaveSize 1
    }

    test("fromDto maps empty utbetalingsperiodeListe") {
        val response = HentPrognoseResponseDto(
            tpnr = "3010",
            navnOrdning = "SPK",
            utbetalingsperiodeListe = emptyList()
        )
        val result = HentPrognoseMapper.fromDto(response)
        result.utbetalingsperiodeListe.shouldBeEmpty()
    }

    test("fromDto maps brukerErIkkeMedlemAvTPOrdning") {
        val response = HentPrognoseResponseDto(
            tpnr = "",
            navnOrdning = "",
            brukerErIkkeMedlemAvTPOrdning = true
        )
        val result = HentPrognoseMapper.fromDto(response)
        result.brukerErIkkeMedlemAvTPOrdning shouldBe true
    }

    test("fromDto maps brukerErMedlemAvTPOrdningSomIkkeStoettes") {
        val response = HentPrognoseResponseDto(
            tpnr = "",
            navnOrdning = "",
            brukerErMedlemAvTPOrdningSomIkkeStoettes = true
        )
        val result = HentPrognoseMapper.fromDto(response)
        result.brukerErMedlemAvTPOrdningSomIkkeStoettes shouldBe true
    }

    test("fromDto defaults membership booleans to false") {
        val response = HentPrognoseResponseDto(tpnr = "3010", navnOrdning = "SPK")
        val result = HentPrognoseMapper.fromDto(response)
        result.brukerErIkkeMedlemAvTPOrdning shouldBe false
        result.brukerErMedlemAvTPOrdningSomIkkeStoettes shouldBe false
    }
})

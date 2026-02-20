package no.nav.pensjon.simulator.tjenestepensjon.pre2025.api.acl.v1

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.simulering.SivilstandKode
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.simulering.TjenestepensjonSimuleringPre2025Spec
import java.time.LocalDate

class SimulerOffentligTjenestepensjonMapperTest : StringSpec({

    "maps all fields including nested lists and optionals" {
        val fnr = "12345678901"
        val specV1 = SimulerOffentligTjenestepensjonSpecV1(
            fnr = fnr,
            fodselsdato = LocalDate.of(1988, 2, 3),
            sisteTpnr = "3010",
            sprak = "NO",
            simulertAFPOffentlig = SimulertAFPOffentligV1(
                simulertAFPOffentligBrutto = 123,
                tpi = 456
            ),
            simulertAFPPrivat = SimulertAFPPrivatV1(
                afpOpptjeningTotalbelop = 789,
                kompensasjonstillegg = 0.12
            ),
            sivilstandkode = SivilstandCodeEnumV1.GIFT,
            inntektListe = listOf(
                InntektV1(datoFom = LocalDate.of(2022, 1, 1), inntekt = 123.0)
            ),
            pensjonsbeholdningsperiodeListe = listOf(
                PensjonsbeholdningsperiodeV1(
                    datoFom = LocalDate.of(2020, 1, 1),
                    pensjonsbeholdning = 456.0,
                    garantipensjonsbeholdning = 789.0,
                    garantitilleggsbeholdning = 0.123,
                )
            ),
            simuleringsperiodeListe = listOf(
                SimuleringsperiodeV1(
                    datoFom = LocalDate.of(2030, 1, 1),
                    folketrygdUttaksgrad = 50,
                    stillingsprosentOffentlig = 80,
                    simulerAFPOffentligEtterfulgtAvAlder = true
                )
            ),
            simuleringsdataListe = listOf(
                SimuleringsdataV1(
                    datoFom = LocalDate.of(2030, 1, 1),
                    andvendtTrygdetid = 40,
                    poengArTom1991 = 2,
                    poengArFom1992 = 3,
                    uforegradVedOmregning = 0,
                    basisgp = 10000.0,
                    basispt = 20000.0,
                    basistp = 30000.0,
                    delingstallUttak = 18.50,
                    forholdstallUttak = 1.05,
                    sluttpoengtall = 4.20,
                )
            ),
            tpForholdListe = listOf(
                TpForholdV1(
                    tpnr = "3010",
                    opptjeningsperiodeListe = listOf(
                        OpptjeningsperiodeV1(
                            datoFom = LocalDate.of(2020, 1, 1),
                            datoTom = LocalDate.of(2021, 12, 31),
                            stillingsprosent = 100,
                            aldersgrense = 70,
                            faktiskHovedlonn = 500000,
                            stillingsuavhengigTilleggslonn = 20000,
                        )
                    )
                )
            ),
        )

        val result: TjenestepensjonSimuleringPre2025Spec =
            SimulerOffentligTjenestepensjonMapperV1.fromDto(specV1)

        // scalars
        result.pid shouldBe Pid(fnr)
        result.foedselsdato shouldBe LocalDate.of(1988, 2, 3)
        result.sisteTpOrdningsTpNummer shouldBe "3010"
        result.sivilstand shouldBe SivilstandKode.GIFT

        // simulert AFPer
        result.simulertOffentligAfp!!.brutto shouldBe 123
        result.simulertOffentligAfp.tidligerePensjonsgivendeInntekt shouldBe 456
        result.simulertPrivatAfp!!.totalAfpBeholdning shouldBe 789
        result.simulertPrivatAfp.kompensasjonstillegg shouldBe 0.12

        // lists -> sizes + first element spot-checks
        result.inntekter.size shouldBe 1
        result.inntekter.first().fom shouldBe LocalDate.of(2022, 1, 1)
        result.inntekter.first().beloep shouldBe 123.0

        result.pensjonsbeholdningsperioder.size shouldBe 1
        result.pensjonsbeholdningsperioder.first().pensjonsbeholdning shouldBe 456.0
        result.pensjonsbeholdningsperioder.first().garantipensjonsbeholdning shouldBe 789.0
        result.pensjonsbeholdningsperioder.first().garantitilleggsbeholdning shouldBe 0.123

        result.simuleringsperioder.size shouldBe 1
        result.simuleringsperioder.first().folketrygdUttaksgrad shouldBe 50
        result.simuleringsperioder.first().stillingsprosentOffentlig shouldBe 80
        result.simuleringsperioder.first().simulerAFPOffentligEtterfulgtAvAlder shouldBe true

        result.simuleringsdata.size shouldBe 1
        result.simuleringsdata.first().andvendtTrygdetid shouldBe 40
        result.simuleringsdata.first().delingstallUttak shouldBe 18.50
        result.simuleringsdata.first().forholdstallUttak shouldBe 1.05

        result.tpForhold.size shouldBe 1
        result.tpForhold.first().tpNr shouldBe "3010"
        result.tpForhold.first().opptjeningsperioder.size shouldBe 1
        with(result.tpForhold.first().opptjeningsperioder.first()) {
            fom shouldBe LocalDate.of(2020, 1, 1)
            tom shouldBe LocalDate.of(2021, 12, 31)
            stillingsprosent shouldBe 100
            aldersgrense shouldBe 70
            faktiskHovedloenn shouldBe 500000
            stillingsuavhengigTilleggsloenn shouldBe 20000
        }
    }

    "handles null optionals: sisteTpnr -> \"\", afp sections -> null, tpForholdListe -> emptyList" {
        val specV1 = SimulerOffentligTjenestepensjonSpecV1(
            fnr = "11111111111",
            fodselsdato = LocalDate.of(1990, 1, 1),
            sisteTpnr = null,                         // <- null
            simulertAFPOffentlig = null,              // <- null
            simulertAFPPrivat = null,                 // <- null
            sivilstandkode = SivilstandCodeEnumV1.UGIF,
            inntektListe = emptyList(),
            pensjonsbeholdningsperiodeListe = emptyList(),
            simuleringsperiodeListe = emptyList(),
            simuleringsdataListe = emptyList(),
            tpForholdListe = null,                     // <- null
            sprak = "NO"
        )

        val result = SimulerOffentligTjenestepensjonMapperV1.fromDto(specV1)

        result.sisteTpOrdningsTpNummer shouldBe ""
        result.simulertOffentligAfp shouldBe null
        result.simulertPrivatAfp shouldBe null
        result.tpForhold.shouldBeEmpty()
    }

    "enum mapping uses name() equality" {
        val specV1 = SimulerOffentligTjenestepensjonSpecV1(
            fnr = "22222222222",
            fodselsdato = LocalDate.of(1991, 1, 1),
            sisteTpnr = "3060",
            simulertAFPOffentlig = null,
            simulertAFPPrivat = null,
            sivilstandkode = SivilstandCodeEnumV1.ENKE,
            inntektListe = emptyList(),
            pensjonsbeholdningsperiodeListe = emptyList(),
            simuleringsperiodeListe = emptyList(),
            simuleringsdataListe = emptyList(),
            tpForholdListe = emptyList(),
            sprak = "NO"
        )

        val result = SimulerOffentligTjenestepensjonMapperV1.fromDto(specV1)

        result.sivilstand shouldBe SivilstandKode.ENKE
    }
})
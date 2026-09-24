package no.nav.pensjon.simulator.core.spec

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.every
import io.mockk.mockk
import no.nav.pensjon.simulator.alder.Alder
import no.nav.pensjon.simulator.core.domain.Avdoed
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.AbstraktBeregningsResultat
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.BeregningsResultatAlderspensjon2016
import no.nav.pensjon.simulator.core.domain.regler.enum.SimuleringTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.ytelse.LoependeYtelser
import no.nav.pensjon.simulator.krav.KravService
import no.nav.pensjon.simulator.normalder.NormertPensjonsalderService
import no.nav.pensjon.simulator.testutil.TestObjects.pid
import no.nav.pensjon.simulator.testutil.TestObjects.simuleringSpec
import no.nav.pensjon.simulator.ytelse.InformasjonOmAvdoed
import java.time.LocalDate

class SpecOverriderTest : ShouldSpec({

    context("alderspensjon kombinert med tidsbegrenset AFP i offentlig sektor") {
        context("ugyldig dato for helt uttak") {
            should("sette dato for helt uttak til første dag i måneden etter at personen oppnår normalder") {
                SpecOverrider(
                    kravService = mockk(),
                    normalderService = arrangeNormalder(alderAar = 67)
                ).behandleSpec(
                    spec = simuleringSpec(
                        type = SimuleringTypeEnum.AFP_ETTERF_ALDER,
                        heltUttakDato = LocalDate.of(2026, 1, 1) // ugyldig (for tidlig)
                    ),
                    ytelser = ingenYtelser(),
                    foedselsdato = LocalDate.of(1960, 6, 15)
                ).heltUttakDato shouldBe LocalDate.of(2027, 7, 1) // 1960 + 67 år
            }
        }

        context("gyldig dato for helt uttak") {
            should("beholde angitt dato for helt uttak") {
                SpecOverrider(
                    kravService = mockk(),
                    normalderService = arrangeNormalder(alderAar = 68)
                ).behandleSpec(
                    spec = simuleringSpec(
                        type = SimuleringTypeEnum.AFP_ETTERF_ALDER,
                        heltUttakDato = LocalDate.of(2028, 7, 1) // 1960 + 68 år (gyldig)
                    ),
                    ytelser = ingenYtelser(),
                    foedselsdato = LocalDate.of(1960, 6, 15)
                ).heltUttakDato shouldBe LocalDate.of(2028, 7, 1) // uendret
            }
        }

        context("udefinert fødselsdato") {
            should("ikke gjøre endringer") {
                val spec = simuleringSpec(
                    type = SimuleringTypeEnum.AFP_ETTERF_ALDER,
                    heltUttakDato = LocalDate.of(2026, 7, 1)
                )

                SpecOverrider(
                    kravService = mockk(),
                    normalderService = arrangeNormalder(alderAar = 67)
                ).behandleSpec(
                    spec,
                    ytelser = ingenYtelser(),
                    foedselsdato = null
                ) shouldBeSameInstanceAs spec
            }
        }
    }

    context("eksisterende vedtak inneholder informasjon om avdød") {
        context("spesifikasjonen inneholder ikke informasjon om avdød") {
            should("sette informasjon om avdød fra vedtaket") {
                SpecOverrider(
                    kravService = mockk(),
                    normalderService = mockk()
                ).behandleSpec(
                    spec = simuleringSpec(avdoed = null),
                    ytelser = ytelser(avdoed = avdoed(doedsaar = 2021)),
                    foedselsdato = null
                ).avdoed shouldBe Avdoed(
                    pid = pid,
                    antallAarUtenlands = 2,
                    inntektFoerDoed = 0, // hardkodet
                    doedDato = doedsdato(aar = 2021),
                    erMedlemAvFolketrygden = true,
                    harInntektOver1G = true
                )
            }
        }

        context("spesifikasjonen inneholder informasjon om avdød") {
            should("beholde angitt informasjon om avdød") {
                val angittAvdoed = Avdoed(
                    pid = pid,
                    antallAarUtenlands = 2,
                    inntektFoerDoed = 0,
                    doedDato = doedsdato(aar = 2022),
                    erMedlemAvFolketrygden = true,
                    harInntektOver1G = false
                )

                SpecOverrider(
                    kravService = mockk(),
                    normalderService = mockk()
                ).behandleSpec(
                    spec = simuleringSpec(avdoed = angittAvdoed),
                    ytelser = ytelser(avdoed = avdoed(doedsaar = 2023)), // ignoreres
                    foedselsdato = null
                ).avdoed shouldBe angittAvdoed
            }
        }
    }

    context("eksisterende krav (vedtak) er uten utenlandsopphold") {
        should("ignorere etterfølgende utenlandsopphold") {
            SpecOverrider(
                kravService = arrangeKrav(kravId = 1, harUtenlandsopphold = false),
                normalderService = mockk()
            ).behandleSpec(
                spec = simuleringSpec(utlandAntallAar = 10),
                ytelser = ytelser(kravId = 1),
                foedselsdato = null
            ).utlandAntallAar shouldBe 0
        }
    }

    context("eksisterende krav (vedtak) er med utenlandsopphold") {
        should("beholde angitt utenlandsopphold") {
            SpecOverrider(
                kravService = arrangeKrav(kravId = 2, harUtenlandsopphold = true),
                normalderService = mockk()
            ).behandleSpec(
                spec = simuleringSpec(utlandAntallAar = 10),
                ytelser = ytelser(kravId = 2),
                foedselsdato = null
            ).utlandAntallAar shouldBe 10
        }
    }
})

private fun doedsdato(aar: Int) =
    LocalDate.of(aar, 2, 3)

private fun avdoed(doedsaar: Int) =
    InformasjonOmAvdoed(
        pid = pid,
        doedsdato = LocalDate.of(doedsaar, 2, 3),
        foersteVirkningsdato = null,
        aarligPensjonsgivendeInntektErMinst1G = true,
        harTilstrekkeligMedlemskapIFolketrygden = true,
        antallAarUtenlands = 2,
        erFlyktning = null
    )

private fun ytelser(
    avdoed: InformasjonOmAvdoed? = null,
    forrigeAlderspensjonsberegningsresultat: AbstraktBeregningsResultat? = null
) =
    LoependeYtelser(
        soekerVirkningFom = LocalDate.MIN,
        privatAfpVirkningFom = null,
        sisteBeregning = null,
        forrigeAlderspensjonsberegningsresultat,
        forrigePrivatAfpBeregningResultat = null,
        forrigeVedtakListe = mutableListOf(),
        avdoed
    )

private fun ytelser(kravId: Int) =
    ytelser(
        forrigeAlderspensjonsberegningsresultat =
            BeregningsResultatAlderspensjon2016().apply { this.kravId = kravId.toLong() }
    )

private fun ingenYtelser() =
    ytelser(forrigeAlderspensjonsberegningsresultat = null)

private fun arrangeKrav(kravId: Int, harUtenlandsopphold: Boolean): KravService =
    mockk {
        every {
            fetchKravhode(kravId.toLong())
        } returns Kravhode().apply { boddEllerArbeidetIUtlandet = harUtenlandsopphold }
    }

private fun arrangeNormalder(alderAar: Int): NormertPensjonsalderService =
    mockk {
        every { normalder(any<LocalDate>()) } returns Alder(aar = alderAar, maaneder = 0)
    }
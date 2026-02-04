package no.nav.pensjon.simulator.statistikk.api.acl

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.core.domain.regler.enum.SimuleringTypeEnum
import no.nav.pensjon.simulator.generelt.organisasjon.Organisasjonsnummer
import no.nav.pensjon.simulator.statistikk.SimuleringHendelse
import no.nav.pensjon.simulator.statistikk.SimuleringStatistikk

class StatistikkMapperV1Test : FunSpec({

    context("toDtoV1") {

        test("mapper tom liste til tom statistikk") {
            val result = StatistikkMapperV1.toDtoV1(emptyList())

            result.statistikk.size shouldBe 0
        }

        test("mapper enkelt element") {
            val statistikk = listOf(
                SimuleringStatistikk(
                    hendelse = SimuleringHendelse(
                        organisasjonsnummer = Organisasjonsnummer("123456789"),
                        simuleringstype = SimuleringTypeEnum.ALDER
                    ),
                    antall = 100
                )
            )

            val result = StatistikkMapperV1.toDtoV1(statistikk)

            result.statistikk.size shouldBe 1
            result.statistikk[0].hendelse.organisasjonsnummer shouldBe "123456789"
            result.statistikk[0].hendelse.simuleringstype shouldBe SimuleringTypeEnumV1.ALDER
            result.statistikk[0].antall shouldBe 100
        }

        test("mapper flere elementer") {
            val statistikk = listOf(
                SimuleringStatistikk(
                    hendelse = SimuleringHendelse(
                        organisasjonsnummer = Organisasjonsnummer("111111111"),
                        simuleringstype = SimuleringTypeEnum.ALDER
                    ),
                    antall = 50
                ),
                SimuleringStatistikk(
                    hendelse = SimuleringHendelse(
                        organisasjonsnummer = Organisasjonsnummer("222222222"),
                        simuleringstype = SimuleringTypeEnum.ALDER_M_AFP_PRIVAT
                    ),
                    antall = 75
                ),
                SimuleringStatistikk(
                    hendelse = SimuleringHendelse(
                        organisasjonsnummer = Organisasjonsnummer("333333333"),
                        simuleringstype = SimuleringTypeEnum.ENDR_ALDER
                    ),
                    antall = 25
                )
            )

            val result = StatistikkMapperV1.toDtoV1(statistikk)

            result.statistikk.size shouldBe 3
            result.statistikk[0].hendelse.organisasjonsnummer shouldBe "111111111"
            result.statistikk[0].hendelse.simuleringstype shouldBe SimuleringTypeEnumV1.ALDER
            result.statistikk[0].antall shouldBe 50
            result.statistikk[1].hendelse.organisasjonsnummer shouldBe "222222222"
            result.statistikk[1].hendelse.simuleringstype shouldBe SimuleringTypeEnumV1.ALDER_M_AFP_PRIVAT
            result.statistikk[1].antall shouldBe 75
            result.statistikk[2].hendelse.organisasjonsnummer shouldBe "333333333"
            result.statistikk[2].hendelse.simuleringstype shouldBe SimuleringTypeEnumV1.ENDR_ALDER
            result.statistikk[2].antall shouldBe 25
        }

        test("bevarer rekkefølge") {
            val statistikk = listOf(
                SimuleringStatistikk(
                    hendelse = SimuleringHendelse(
                        organisasjonsnummer = Organisasjonsnummer("333333333"),
                        simuleringstype = SimuleringTypeEnum.BARN
                    ),
                    antall = 10
                ),
                SimuleringStatistikk(
                    hendelse = SimuleringHendelse(
                        organisasjonsnummer = Organisasjonsnummer("111111111"),
                        simuleringstype = SimuleringTypeEnum.AFP
                    ),
                    antall = 30
                )
            )

            val result = StatistikkMapperV1.toDtoV1(statistikk)

            result.statistikk[0].hendelse.organisasjonsnummer shouldBe "333333333"
            result.statistikk[1].hendelse.organisasjonsnummer shouldBe "111111111"
        }
    }

    context("hendelseAntallV1") {

        test("mapper SimuleringStatistikk til HendelseAntallV1") {
            val statistikk = SimuleringStatistikk(
                hendelse = SimuleringHendelse(
                    organisasjonsnummer = Organisasjonsnummer("987654321"),
                    simuleringstype = SimuleringTypeEnum.GJENLEVENDE
                ),
                antall = 42
            )

            val result = StatistikkMapperV1.hendelseAntallV1(statistikk)

            result.hendelse.organisasjonsnummer shouldBe "987654321"
            result.hendelse.simuleringstype shouldBe SimuleringTypeEnumV1.GJENLEVENDE
            result.antall shouldBe 42
        }

        test("mapper alle SimuleringTypeEnum verdier korrekt") {
            val simuleringTyper = listOf(
                SimuleringTypeEnum.AFP to SimuleringTypeEnumV1.AFP,
                SimuleringTypeEnum.AFP_ETTERF_ALDER to SimuleringTypeEnumV1.AFP_ETTERF_ALDER,
                SimuleringTypeEnum.AFP_FPP to SimuleringTypeEnumV1.AFP_FPP,
                SimuleringTypeEnum.ALDER to SimuleringTypeEnumV1.ALDER,
                SimuleringTypeEnum.ALDER_KAP_20 to SimuleringTypeEnumV1.ALDER_KAP_20,
                SimuleringTypeEnum.ALDER_M_AFP_PRIVAT to SimuleringTypeEnumV1.ALDER_M_AFP_PRIVAT,
                SimuleringTypeEnum.ALDER_MED_AFP_OFFENTLIG_LIVSVARIG to SimuleringTypeEnumV1.ALDER_MED_AFP_OFFENTLIG_LIVSVARIG,
                SimuleringTypeEnum.ALDER_M_GJEN to SimuleringTypeEnumV1.ALDER_M_GJEN,
                SimuleringTypeEnum.BARN to SimuleringTypeEnumV1.BARN,
                SimuleringTypeEnum.ENDR_ALDER to SimuleringTypeEnumV1.ENDR_ALDER,
                SimuleringTypeEnum.ENDR_ALDER_M_GJEN to SimuleringTypeEnumV1.ENDR_ALDER_M_GJEN,
                SimuleringTypeEnum.ENDR_AP_M_AFP_PRIVAT to SimuleringTypeEnumV1.ENDR_AP_M_AFP_PRIVAT,
                SimuleringTypeEnum.ENDR_AP_M_AFP_OFFENTLIG_LIVSVARIG to SimuleringTypeEnumV1.ENDR_AP_M_AFP_OFFENTLIG_LIVSVARIG,
                SimuleringTypeEnum.GJENLEVENDE to SimuleringTypeEnumV1.GJENLEVENDE
            )

            simuleringTyper.forEach { (domainType, v1Type) ->
                val statistikk = SimuleringStatistikk(
                    hendelse = SimuleringHendelse(
                        organisasjonsnummer = Organisasjonsnummer("123456789"),
                        simuleringstype = domainType
                    ),
                    antall = 1
                )

                val result = StatistikkMapperV1.hendelseAntallV1(statistikk)

                result.hendelse.simuleringstype shouldBe v1Type
            }
        }

        test("mapper antall korrekt for ulike verdier") {
            listOf(0, 1, 100, 999999).forEach { antall ->
                val statistikk = SimuleringStatistikk(
                    hendelse = SimuleringHendelse(
                        organisasjonsnummer = Organisasjonsnummer("123456789"),
                        simuleringstype = SimuleringTypeEnum.ALDER
                    ),
                    antall = antall
                )

                val result = StatistikkMapperV1.hendelseAntallV1(statistikk)

                result.antall shouldBe antall
            }
        }
    }
})

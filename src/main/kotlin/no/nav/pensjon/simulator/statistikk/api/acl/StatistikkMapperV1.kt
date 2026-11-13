package no.nav.pensjon.simulator.statistikk.api.acl

import no.nav.pensjon.simulator.core.domain.regler.enum.SimuleringTypeEnum
import no.nav.pensjon.simulator.statistikk.AntallCombo
import no.nav.pensjon.simulator.statistikk.SimuleringHendelse
import no.nav.pensjon.simulator.statistikk.SimuleringStatistikk
import no.nav.pensjon.simulator.tech.time.Kalendermaaned

object StatistikkMapperV1 {

    fun toDtoV1(source: List<SimuleringStatistikk>) =
        StatistikkTransferObjectV1(statistikk = source.map(::hendelseAntallV1))

    fun hendelseAntallV1(source: SimuleringStatistikk) =
        HendelseAntallV1(
            hendelse = hendelse(source.hendelse),
            antall = source.antall
        )

    fun toDto(tertial: Int, source: Map<Kalendermaaned, AntallCombo>) =
        TertialstatistikkV1(
            tertial,
            statistikkPerMaaned = antallPerMaaned(source)
        )

    private fun antallPerMaaned(source: Map<Kalendermaaned, AntallCombo>): Map<String, Map<String, Int>> =
        source.map { (key, value) ->
            key.navn to mapOf(
                "kalkulator" to value.navAntall,
                "totalt" to value.totaltAntall
            )
        }.toMap()

    private fun hendelse(source: SimuleringHendelse) =
        SimuleringHendelseV1(
            organisasjonsnummer = source.organisasjonsnummer.value,
            simuleringstype = when (source.simuleringstype) {
                SimuleringTypeEnum.ENDR_AP_M_AFP_PRIVAT -> SimuleringTypeEnumV1.ENDR_ALDER

                else -> SimuleringTypeEnumV1.valueOf(source.simuleringstype.name)
            }
        )
}

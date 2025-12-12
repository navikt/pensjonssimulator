package no.nav.pensjon.simulator.statistikk.api.acl

import no.nav.pensjon.simulator.statistikk.SimuleringHendelse
import no.nav.pensjon.simulator.statistikk.SimuleringStatistikk

object StatistikkMapperV1 {

    fun toDtoV1(source: List<SimuleringStatistikk>) =
        StatistikkTransferObjectV1(statistikk = source.map(::hendelseAntall))

    private fun hendelseAntall(source: SimuleringStatistikk) =
        HendelseAntallV1(
            hendelse = hendelse(source.hendelse),
            antall = source.antall
        )

    private fun hendelse(source: SimuleringHendelse) =
        SimuleringHendelseV1(
            organisasjonsnummer = source.organisasjonsnummer.value,
            simuleringstype = SimuleringTypeEnumV1.valueOf(source.simuleringstype.name)
        )
}

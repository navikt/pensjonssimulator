package no.nav.pensjon.simulator.statistikk.api

import io.swagger.v3.oas.annotations.Hidden
import no.nav.pensjon.simulator.statistikk.StatistikkService
import no.nav.pensjon.simulator.statistikk.api.acl.HendelseAntallV1
import no.nav.pensjon.simulator.statistikk.api.acl.StatistikkMapperV1.hendelseAntallV1
import no.nav.pensjon.simulator.statistikk.api.acl.StatistikkMapperV1.toDtoV1
import no.nav.pensjon.simulator.statistikk.api.acl.StatistikkTransferObjectV1
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api")
class StatistikkController(val statistikk: StatistikkService) {

    @GetMapping("v1/statistikk")
    @Hidden
    fun hentStatistikk(): StatistikkTransferObjectV1 =
        toDtoV1(source = statistikk.hent())

    @GetMapping("v1/statistikk-snapshot/{aar-maaned}")
    @Hidden
    fun hentSnapshot(@PathVariable(value = "aar-maaned") aarMaaned: Int): List<HendelseAntallV1> =
        statistikk.getSnapshot(aarMaaned).map(::hendelseAntallV1)
}

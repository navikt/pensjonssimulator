package no.nav.pensjon.simulator.uttak.api

import no.nav.pensjon.simulator.uttak.UttakService
import no.nav.pensjon.simulator.uttak.api.acl.UttakSpecMapperV1.fromSpecV1
import no.nav.pensjon.simulator.uttak.api.acl.TidligstMuligUttakResultV1
import no.nav.pensjon.simulator.uttak.api.acl.TidligstMuligUttakSpecV1
import no.nav.pensjon.simulator.uttak.api.acl.UttakResultMapperV1.resultV1
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api")
class UttakController(private val uttakService: UttakService) {

    @PostMapping("v1/tidligst-mulig-uttak")
    fun tidligstMuligUttak(@RequestBody spec: TidligstMuligUttakSpecV1): TidligstMuligUttakResultV1 =
        resultV1(uttakService.finnTidligstMuligUttak(fromSpecV1(spec)))
}

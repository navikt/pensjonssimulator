package no.nav.pensjon.simulator.uttak.api

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api")
class UttakController {

    @PostMapping("v1/tidligst-mulig-uttak")
    fun tidligstMuligUttak(@RequestBody spec: TidligstMuligUttakSpecV1): AlderV1 =
        AlderV1(65, 4)
}

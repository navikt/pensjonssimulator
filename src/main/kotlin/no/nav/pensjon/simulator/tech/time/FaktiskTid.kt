package no.nav.pensjon.simulator.tech.time

import org.springframework.stereotype.Component
import java.time.LocalTime

@Component
class FaktiskTid : Tid {
    override fun naa(): LocalTime = LocalTime.now()
}
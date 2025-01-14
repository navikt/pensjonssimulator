package no.nav.pensjon.simulator.tech.time

import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class ActualTime : Time {
    override fun today(): LocalDate = LocalDate.now()
}

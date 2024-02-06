package no.nav.pensjon.simulator

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration
import org.springframework.boot.runApplication

@SpringBootApplication(exclude = [UserDetailsServiceAutoConfiguration::class])
class PensjonssimulatorApplication

fun main(args: Array<String>) {
    runApplication<PensjonssimulatorApplication>(*args)
}

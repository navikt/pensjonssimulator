package no.nav.pensjon.simulator

import io.prometheus.client.hotspot.DefaultExports
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration
import org.springframework.boot.runApplication

@SpringBootApplication(exclude = [UserDetailsServiceAutoConfiguration::class])
class PensjonssimulatorApplication

fun main(args: Array<String>) {
    DefaultExports.initialize()
    runApplication<PensjonssimulatorApplication>(*args)
}

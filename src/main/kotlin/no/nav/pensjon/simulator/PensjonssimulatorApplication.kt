package no.nav.pensjon.simulator

import io.prometheus.client.hotspot.DefaultExports
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration
import org.springframework.boot.runApplication

@SpringBootApplication(exclude = [UserDetailsServiceAutoConfiguration::class])
open class PensjonssimulatorApplication

fun main(args: Array<String>) {
    DefaultExports.initialize()
    runApplication<PensjonssimulatorApplication>(*args)
}

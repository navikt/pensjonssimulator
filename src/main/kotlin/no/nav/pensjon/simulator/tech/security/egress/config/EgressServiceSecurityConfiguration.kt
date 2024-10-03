package no.nav.pensjon.simulator.tech.security.egress.config

import no.nav.pensjon.simulator.tech.security.egress.token.EgressAccessTokenFacade
import no.nav.pensjon.simulator.tech.security.egress.token.RawJwt
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.*
import java.util.function.Supplier

@Configuration
open class EgressServiceSecurityConfiguration {

    @Bean
    open fun egressServiceListsByAudience(
        @Value("\${ps.pen.service-id}") pensjonsfagligKjerneServiceId: String,
        @Value("\${ps.sporingslogg.service-id}") sporingsloggServiceId: String,
        @Value("\${tjenestepensjon.service-id}") tpregisteretServiceId: String,
    ) =
        EgressServicesByAudience(mapOf(
            pensjonsfagligKjerneServiceId to EgressService.PENSJONSFAGLIG_KJERNE,
            sporingsloggServiceId to EgressService.SPORINGSLOGG,
            tpregisteretServiceId to EgressService.TP_REGISTERET,
        ))

    @Bean
    open fun egressTokenSuppliersByService(
        servicesByAudience: EgressServicesByAudience,
        egressTokenGetter: EgressAccessTokenFacade
    ): EgressTokenSuppliersByService {
        val suppliersByService: MutableMap<EgressService, Supplier<RawJwt>> = EnumMap(EgressService::class.java)

        servicesByAudience.entries.forEach { (audience, service) ->
            suppliersByService[service] =
                Supplier<RawJwt> { egressTokenGetter.getAccessToken(service.authType, audience) }
        }

        return EgressTokenSuppliersByService(suppliersByService)
    }
}

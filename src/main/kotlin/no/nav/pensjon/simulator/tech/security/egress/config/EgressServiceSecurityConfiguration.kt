package no.nav.pensjon.simulator.tech.security.egress.config

import no.nav.pensjon.simulator.tech.security.egress.token.EgressAccessTokenFacade
import no.nav.pensjon.simulator.tech.security.egress.token.RawJwt
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.*
import java.util.function.Supplier

@Configuration
class EgressServiceSecurityConfiguration {

    @Bean
    fun egressServiceListsByAudience(@Value("\${ps.pen.service-id}") pensjonsfagligKjerneServiceId: String) =
        EgressServicesByAudience(mapOf(pensjonsfagligKjerneServiceId to EgressService.PENSJONSFAGLIG_KJERNE))

    @Bean
    fun egressTokenSuppliersByService(
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

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
        @Value("\${ps.persondata.service-id}") persondataServiceId: String,
        @Value("\${ps.fss-gw.service-id}") fssGatewayServiceId: String,
        @Value("\${tjenestepensjon.service-id}") tpRegisterServiceId: String,
        @Value("\${ps.tp-simulering.service-id}") tpSimuleringServiceId: String,
        @Value("\${ps.popp.service-id}") opptjeningServiceId: String,
        @Value("\${ps.maskinporten.scope.3}") spkServiceId: String,
    ) =
        EgressServicesByAudience(
            mapOf(
                pensjonsfagligKjerneServiceId to EgressService.PENSJONSFAGLIG_KJERNE,
                persondataServiceId to EgressService.PERSONDATA,
                fssGatewayServiceId to EgressService.FSS_GATEWAY,
                tpRegisterServiceId to EgressService.TP_REGISTERET,
                tpSimuleringServiceId to EgressService.TJENESTEPENSJON_SIMULERING,
                opptjeningServiceId to EgressService.PENSJONSOPPTJENING,
                spkServiceId to EgressService.SPK,
            )
        )

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

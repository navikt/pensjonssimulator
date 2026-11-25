package no.nav.pensjon.simulator.tech.security.egress.config

import no.nav.pensjon.simulator.tech.security.egress.token.EgressAccessTokenFacade
import no.nav.pensjon.simulator.tech.security.egress.token.RawJwt
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.*
import java.util.function.Function

@Configuration
class EgressServiceSecurityConfiguration {

    @Bean
    fun egressServiceListsByAudience(
        @Value($$"${ps.pen.service-id}") pensjonsfagligKjerneServiceId: String,
        @Value($$"${ps.pensjonpersondata.service-id}") pensjonPersondataServiceId: String,
        @Value($$"${ps.persondata.service-id}") persondataServiceId: String,
        @Value($$"${ps.fss-gw.service-id}") fssGatewayServiceId: String,
        @Value($$"${ps.sporingslogg.service-id}") sporingsloggServiceId: String,
        @Value($$"${tjenestepensjon.service-id}") tpRegisterServiceId: String,
        @Value($$"${ps.popp.service-id}") opptjeningServiceId: String,
        @Value($$"${ps.maskinporten.consume.spk.scope}") spkServiceId: String,
        @Value($$"${ps.maskinporten.consume.klp.scope}") klpServiceId: String,
        @Value($$"${ps.maskinporten.consume.storebrand.scope}") storebrandServiceId: String,
        @Value($$"${ps.pensjon-opptjening-afp-api.service-id}") pensjonOpptjeningAfpApiServiceId: String,
    ) =
        EgressServicesByAudience(
            mapOf(
                pensjonsfagligKjerneServiceId to EgressService.PENSJONSFAGLIG_KJERNE,
                pensjonPersondataServiceId to EgressService.PENSJON_PERSONDATA,
                persondataServiceId to EgressService.PERSONDATA,
                fssGatewayServiceId to EgressService.FSS_GATEWAY,
                sporingsloggServiceId to EgressService.SPORINGSLOGG,
                tpRegisterServiceId to EgressService.TP_REGISTERET,
                opptjeningServiceId to EgressService.PENSJONSOPPTJENING,
                spkServiceId to EgressService.SPK,
                klpServiceId to EgressService.KLP,
                storebrandServiceId to EgressService.STOREBRAND,
                pensjonOpptjeningAfpApiServiceId to EgressService.AFP_BEHOLDNING_API
            )
        )

    @Bean
    fun egressTokenSuppliersByService(
        servicesByAudience: EgressServicesByAudience,
        egressTokenGetter: EgressAccessTokenFacade
    ): EgressTokenSuppliersByService {
        val suppliersByService: MutableMap<EgressService, Function<EgressTokenTypeDecisionBasis, RawJwt>> =
            EnumMap(EgressService::class.java)

        servicesByAudience.entries.forEach { (audience, service) ->
            suppliersByService[service] = Function<EgressTokenTypeDecisionBasis, RawJwt> {
                egressTokenGetter.getAccessToken(service.authType, audience, tokenTypeDecisionBasis = it)
            }
        }

        return EgressTokenSuppliersByService(suppliersByService)
    }
}
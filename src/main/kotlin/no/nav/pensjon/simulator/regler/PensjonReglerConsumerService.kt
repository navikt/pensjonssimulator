package no.nav.pensjon.simulator.regler

import org.springframework.stereotype.Component

//TODO use RegelClient instead
interface PensjonReglerConsumerService {
    fun <K, T : Any> regelServiceApi(
        regelRequest: T,
        responseClass: Class<*>,
        serviceName: String,
        map: Map<String, Any>?,
        sakId: String?
    ): K
}

@Component
class DummyPensjonReglerConsumerService : PensjonReglerConsumerService {
    override fun <K, T : Any> regelServiceApi(
        regelRequest: T,
        responseClass: Class<*>,
        serviceName: String,
        map: Map<String, Any>?,
        sakId: String?
    ): K {
        TODO("Not yet implemented")
    }
}

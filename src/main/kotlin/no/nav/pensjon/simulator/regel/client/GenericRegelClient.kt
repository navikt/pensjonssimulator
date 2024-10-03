package no.nav.pensjon.simulator.regel.client

// PensjonReglerConsumerService
interface GenericRegelClient {
    // regelServiceApi
    fun <K, T : Any> makeRegelCall(
        request: T,
        responseClass: Class<*>,
        serviceName: String,
        map: Map<String, Any>?,
        sakId: String?
    ): K
}

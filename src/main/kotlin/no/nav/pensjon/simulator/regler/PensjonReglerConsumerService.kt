package no.nav.pensjon.simulator.regler

interface PensjonReglerConsumerService {
    fun <K, T : Any> regelServiceApi(
        regelRequest: T,
        responseClass: Class<*>,
        serviceName: String,
        map: Map<String, Any>?,
        sakId: String?
    ): K
}

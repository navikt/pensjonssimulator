package no.nav.pensjon.simulator.regel.client

interface GenericRegelClient {

    fun <K, T : Any> makeRegelCall(
        request: T,
        responseClass: Class<*>,
        serviceName: String
    ): K
}
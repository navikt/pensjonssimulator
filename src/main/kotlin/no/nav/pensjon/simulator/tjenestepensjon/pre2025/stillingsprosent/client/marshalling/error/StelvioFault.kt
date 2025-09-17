package no.nav.pensjon.simulator.tjenestepensjon.pre2025.stillingsprosent.client.marshalling.error

abstract class StelvioFault : Throwable() {
    lateinit var errorMessage: String
    lateinit var errorSource: String
    lateinit var errorType: String
    lateinit var rootCause: String
    lateinit var dateTimeStamp: String
}
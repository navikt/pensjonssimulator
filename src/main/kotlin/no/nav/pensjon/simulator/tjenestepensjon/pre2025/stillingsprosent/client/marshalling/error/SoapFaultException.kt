package no.nav.pensjon.simulator.tjenestepensjon.pre2025.stillingsprosent.client.marshalling.error

class SoapFaultException(exception: String, message: String) : Throwable("Exception:$exception message:$message")
package no.nav.pensjon.simulator.core.exception

// PEN070KonsistensenIGrunnlagetErFeilException
// Can be replaced by RegelmotorValideringException?
class KonsistensenIGrunnlagetErFeilException(
    message: String? = "Konsistensen i grunnlaget er feil",
    e: Throwable
) : RuntimeException(message, e) {
    constructor(e: Throwable) : this(e.toString(), e)
}
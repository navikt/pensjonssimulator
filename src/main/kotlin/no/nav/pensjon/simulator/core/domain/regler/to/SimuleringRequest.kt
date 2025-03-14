package no.nav.pensjon.simulator.core.domain.regler.to

import no.nav.pensjon.simulator.core.domain.regler.simulering.Simulering
import no.nav.pensjon.simulator.core.util.DateNoonExtension.noon
import java.util.*

class SimuleringRequest() : ServiceRequest() {

    var simulering: Simulering? = null
    var fom: Date? = null
    var ektefelleMottarPensjon = false
    var beregnForsorgingstillegg = false
    var beregnInstitusjonsopphold = false

    constructor(
        simulering: Simulering?,
        fom: Date?,
        ektefelleMottarPensjon: Boolean,
        beregnForsorgingstillegg: Boolean,
        beregnInstitusjonsopphold: Boolean
    ) : this() {
        this.simulering = simulering
        this.fom = fom?.noon()
        this.ektefelleMottarPensjon = ektefelleMottarPensjon
        this.beregnForsorgingstillegg = beregnForsorgingstillegg
        this.beregnInstitusjonsopphold = beregnInstitusjonsopphold
    }

    constructor(simulering: Simulering?, fom: Date?) : this() {
        this.simulering = simulering
        this.fom = fom
    }
}

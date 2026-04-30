package no.nav.pensjon.simulator.core.domain.regler.to

import no.nav.pensjon.simulator.core.domain.regler.enum.SimuleringTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.simulering.Simulering
import java.time.LocalDate

class SimuleringRequest() : ServiceRequest() {
    var simulering: Simulering? = null
    var fomLd: LocalDate? = null
    var ektefelleMottarPensjon = false
    var beregnForsorgingstillegg = false
    var beregnInstitusjonsopphold = false

    constructor(
        simulering: Simulering?,
        fom: LocalDate?,
        ektefelleMottarPensjon: Boolean,
        beregnForsorgingstillegg: Boolean,
        beregnInstitusjonsopphold: Boolean
    ) : this() {
        this.simulering = simulering
        this.fomLd = fom
        this.ektefelleMottarPensjon = ektefelleMottarPensjon
        this.beregnForsorgingstillegg = beregnForsorgingstillegg
        this.beregnInstitusjonsopphold = beregnInstitusjonsopphold
        //TODO: FJERN
        simulering?.simuleringTypeEnum.also { // Satsstabell skal kun settes for alderspensjon, og skal være null for alle andre simuleringstyper}
            if (it == SimuleringTypeEnum.AFP) this.satstabell = null
        }
    }

    constructor(simulering: Simulering?, fom: LocalDate?) : this() {
        this.simulering = simulering
        this.fomLd = fom
    }
}

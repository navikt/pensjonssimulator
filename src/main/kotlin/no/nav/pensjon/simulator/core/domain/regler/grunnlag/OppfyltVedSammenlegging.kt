package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import no.nav.pensjon.simulator.core.domain.regler.kode.AvtalelandCti
import java.io.Serializable

class OppfyltVedSammenlegging(

        var oppfylt: Boolean = false,

        /**
         * Angir avtaleland.
         */
        var avtaleland: AvtalelandCti? = null
) : Serializable {
    /*
     * Copy Constructor
     *
     *  oppfyltVedSammenlegging a `OppfyltVedSammenlegging` object
     */
    constructor(oppfyltVedSammenlegging: OppfyltVedSammenlegging) : this() {
        this.oppfylt = oppfyltVedSammenlegging.oppfylt
        if (oppfyltVedSammenlegging.avtaleland != null) {
            this.avtaleland = AvtalelandCti(oppfyltVedSammenlegging.avtaleland)
        }
    }
}

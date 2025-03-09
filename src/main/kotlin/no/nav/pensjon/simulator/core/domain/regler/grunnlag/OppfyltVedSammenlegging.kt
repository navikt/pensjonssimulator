package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import no.nav.pensjon.simulator.core.domain.regler.enum.AvtaleLandEnum
import no.nav.pensjon.simulator.core.domain.regler.kode.AvtalelandCti

// Checked 2025-02-28
class OppfyltVedSammenlegging {
    /**
     * Angir om kriteriet er oppfylt eller ikke.
     */
    var oppfylt = false

    /**
     * Angir avtaleland.
     */
    var avtaleland: AvtalelandCti? = null
    var avtalelandEnum: AvtaleLandEnum? = null

    constructor()

    constructor(source: OppfyltVedSammenlegging) : this() {
        oppfylt = source.oppfylt
        avtaleland = source.avtaleland?.let(::AvtalelandCti)
        avtalelandEnum = source.avtalelandEnum
    }
}

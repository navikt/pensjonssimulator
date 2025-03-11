package no.nav.pensjon.simulator.core.domain.regler.vedtak

import no.nav.pensjon.simulator.core.domain.regler.grunnlag.OppfyltVedSammenlegging

// 2025-03-10
class MedlemskapForUTEtterTrygdeavtaler : AbstraktVilkar() {
    /**
     * Inneholder informasjon om bruker har inngang gjennom sammenlegging av trygdetid i avtaleland og Norge.
     * Registreres manuelt av saksbehandler.
     */
    var oppfyltVedSammenlegging: OppfyltVedSammenlegging? = null

    /**
     * Inneholder informasjon om bruker har inngang gjennom sammenlegging av trygdetid i avtaleland og Norge på minst 5 år.
     * Registreres manuelt av saksbehandler.
     */
    var oppfyltVedSammenleggingFemAr: OppfyltVedSammenlegging? = null

/*
    constructor() : super()

    constructor(medlemskapForUTEtterTrygdeavtaler: MedlemskapForUTEtterTrygdeavtaler?) : super(
        medlemskapForUTEtterTrygdeavtaler!!
    ) {
        if (medlemskapForUTEtterTrygdeavtaler.oppfyltVedSammenlegging != null) {
            this.oppfyltVedSammenlegging =
                OppfyltVedSammenlegging(medlemskapForUTEtterTrygdeavtaler.oppfyltVedSammenlegging!!)
        }
        if (medlemskapForUTEtterTrygdeavtaler.oppfyltVedSammenleggingFemAr != null) {
            oppfyltVedSammenleggingFemAr =
                OppfyltVedSammenlegging(medlemskapForUTEtterTrygdeavtaler.oppfyltVedSammenleggingFemAr!!)
        }
    }

    constructor(oppfyltVedSammenlegging: OppfyltVedSammenlegging?) : super() {
        this.oppfyltVedSammenlegging = oppfyltVedSammenlegging
    }
*/
}

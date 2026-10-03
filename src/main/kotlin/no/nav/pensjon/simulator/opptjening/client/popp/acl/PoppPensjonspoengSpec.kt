package no.nav.pensjon.simulator.opptjening.client.popp.acl

import no.nav.pensjon.simulator.opptjening.PensjonspoengSpec

data class PoppPensjonspoengSpec(
    val fnr: String,
    val fomAr: Int?,
    val tomAr: Int?,
    val pensjonspoengType: String?
) {
    companion object {
        fun fromInternalValue(source: PensjonspoengSpec) =
            PoppPensjonspoengSpec(
                fnr = source.pid.value,
                fomAr = source.fomAar,
                tomAr = source.tomAar,
                pensjonspoengType = source.pensjonspoengType?.name
            )
    }
}
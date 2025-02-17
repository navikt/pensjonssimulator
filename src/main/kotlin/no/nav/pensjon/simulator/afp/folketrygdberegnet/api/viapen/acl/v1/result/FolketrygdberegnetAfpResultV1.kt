package no.nav.pensjon.simulator.afp.folketrygdberegnet.api.viapen.acl.v1.result

import java.util.Date

/**
 * Folketrygdberegnet avtalefestet pensjon i offentlig sektor.
 * Maps 1-to-1 with no.nav.pensjon.pen.domain.api.beregning.FolketrygdberegnetAfp in PEN.
 */
data class FolketrygdberegnetAfpResultV1(
    val totalbelopAfp: Int? = null,
    val virkFom: Date? = null,
    val tidligereArbeidsinntekt: Int? = null,
    val grunnbelop: Int? = null,
    val sluttpoengtall: Double? = null,
    val trygdetid: Int? = null,
    val poengar: Int? = null,
    val poeangar_f92: Int? = null,
    val poeangar_e91: Int? = null,
    val grunnpensjon: Int? = null,
    val tilleggspensjon: Int? = null,
    val afpTillegg: Int? = null,
    val fpp: Double? = null,
    val sertillegg: Int? = null
)

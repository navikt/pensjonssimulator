package no.nav.pensjon.simulator.afp.offentlig.pre2025

import java.time.LocalDate

/**
 * Corresponds 1-to-1 with PEN's no.nav.pensjon.pen.domain.api.beregning.FolketrygdberegnetAfp
 */
data class FolketrygdberegnetAfp(
    val totalbelopAfp: Int?, // totalt AFP-beløp
    val virkFom: LocalDate?, // virkningsdato (fra og med)
    val tidligereArbeidsinntekt: Int?,
    val grunnbelop: Int?, // folketrygdens grunnbeløp
    val sluttpoengtall: Double?,
    val trygdetid: Int?,
    val poengar: Int?, // poengår
    val poeangar_f92: Int?, // poengår før 1992
    val poeangar_e91: Int?, // poengår etter 1991
    val grunnpensjon: Int?,
    val tilleggspensjon: Int?,
    val afpTillegg: Int?, // AFP-tillegg
    val fpp: Double?, // framtidige pensjonspoeng
    val sertillegg: Int? // særtillegg
)
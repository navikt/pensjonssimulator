package no.nav.pensjon.simulator.afp.folketrygdberegnet.api.viapen.acl.v1.result

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING
import java.time.LocalDate

/**
 * Folketrygdberegnet avtalefestet pensjon i offentlig sektor.
 * Maps 1-to-1 with no.nav.pensjon.pen.domain.api.beregning.FolketrygdberegnetAfp in PEN.
 */
data class FolketrygdberegnetAfpResultV1(
    val totalbelopAfp: Int? = null,
    @JsonFormat(shape = STRING, pattern = "yyyy-MM-dd", timezone = "CET") val virkFom: LocalDate? = null,
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

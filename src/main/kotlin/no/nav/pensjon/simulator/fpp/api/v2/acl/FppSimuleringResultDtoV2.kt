package no.nav.pensjon.simulator.fpp.api.v2.acl

import no.nav.pensjon.simulator.fpp.api.v1.acl.AfpTypeDto
import no.nav.pensjon.simulator.fpp.api.v1.acl.ProblemDto
import java.time.LocalDate

data class FppSimuleringResultDtoV2(
    val afpOrdning: AfpTypeDto?,
    val beregnetAfp: FolketrygdberegnetAfpDtoV2?,
    val problem: ProblemDto? = null
)

data class FolketrygdberegnetAfpDtoV2(
    val totalbelopAfp: Int?,
    val virkFom: LocalDate?,
    val tidligereArbeidsinntekt: Int?,
    val grunnbelop: Int?,
    val sluttpoengtall: Double?,
    val trygdetid: Int?,
    val poengar: Int?,
    val poeangar_f92: Int?,
    val poeangar_e91: Int?,
    val grunnpensjon: Int?,
    val tilleggspensjon: Int?,
    val afpTillegg: Int?,
    val fpp: Double?,
    val sertillegg: Int?,
    val grad: Int?,
    val erAvkortet: Boolean?
)

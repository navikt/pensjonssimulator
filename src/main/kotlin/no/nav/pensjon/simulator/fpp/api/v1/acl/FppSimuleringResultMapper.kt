package no.nav.pensjon.simulator.fpp.api.v1.acl

import no.nav.pensjon.simulator.afp.offentlig.pre2025.FolketrygdberegnetAfp
import no.nav.pensjon.simulator.fpp.FppSimuleringResult
import no.nav.pensjon.simulator.validity.Problem

object FppSimuleringResultMapper {

    fun toDto(source: FppSimuleringResult) =
        FppSimuleringResultDto(
            afpOrdning = AfpTypeDto.entries.firstOrNull { it.internalValue == source.afpOrdning } ?: AfpTypeDto.AFPSTAT,
            beregnetAfp = source.beregnetAfp?.let(::folketrygdberegnetAfp),
            problem = source.problem?.let(::problem)
        )

    /**
     * Corresponds 1-to-1 with PEN's no.nav.pensjon.pen.domain.api.beregning.FolketrygdberegnetAfp
     */
    private fun folketrygdberegnetAfp(source: FolketrygdberegnetAfp) =
        FolketrygdberegnetAfpDto(
            totalbelopAfp = source.totalbelopAfp,
            virkFom = source.virkFom,
            tidligereArbeidsinntekt = source.tidligereArbeidsinntekt,
            grunnbelop = source.grunnbelop,
            sluttpoengtall = source.sluttpoengtall,
            trygdetid = source.trygdetid,
            poengar = source.poengar,
            poeangar_f92 = source.poeangar_f92,
            poeangar_e91 = source.poeangar_e91,
            grunnpensjon = source.grunnpensjon,
            tilleggspensjon = source.tilleggspensjon,
            afpTillegg = source.afpTillegg,
            fpp = source.fpp,
            grad = source.grad,
            sertillegg = source.sertillegg
        )

    private fun problem(source: Problem) =
        ProblemDto(
            type = ProblemtypeDto.entries.firstOrNull { it.internalValue == source.type }
                ?: ProblemtypeDto.ANNEN_SERVERFEIL,
            beskrivelse = source.beskrivelse
        )
}